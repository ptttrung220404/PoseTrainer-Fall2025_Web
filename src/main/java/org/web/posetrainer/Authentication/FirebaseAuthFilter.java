package org.web.posetrainer.Authentication;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final FirebaseAuth firebaseAuth;
    private final Firestore firestore;

    // Ưu tiên dùng constructor nhận FirebaseAuth (rõ ràng, dễ test)
    public FirebaseAuthFilter(FirebaseAuth firebaseAuth, Firestore firestore) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
    }

    // Giữ constructor cũ cho tương thích (nếu bạn vẫn truyền FirebaseApp ở SecurityConfig)
    public FirebaseAuthFilter(FirebaseApp app, Firestore firestore) {
        this(FirebaseAuth.getInstance(app), firestore);
    }

    

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        System.out.println(">>> FirebaseAuthFilter ENTER, path=" + req.getRequestURI());
        final String h = req.getHeader(HttpHeaders.AUTHORIZATION);
        System.out.println(">>> Authorization header = " + h);

        // Parse "Bearer <token>" robust
        String idToken = null;
        if (h != null) {
            String[] parts = h.trim().split("\\s+");
            if (parts.length == 2 && parts[0].equalsIgnoreCase("Bearer")) {
                idToken = parts[1];
            } else {
                System.out.println(">>> Bad Authorization format");
            }
        } else {
            System.out.println(">>> No Authorization header");
        }

        if (idToken != null) {
            try {
                // 1) Verify ID token
                FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken, true);
                String uid = decoded.getUid();
                System.out.println(">>> VERIFIED uid=" + uid);

                // 2) Lấy authorities
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                // 2a) từ custom claims nếu có (roles: ["ADMIN", ...])
                Object rolesObj = decoded.getClaims().get("roles");
                if (rolesObj instanceof List<?> list && !list.isEmpty()) {
                    for (Object r : list) {
                        String roleStr = String.valueOf(r).toUpperCase(Locale.ROOT);
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleStr));
                    }
                } else {
                    // 2b) Đọc từ Firestore: users/{uid}.role == "ADMIN" (hoặc "USER")
                    try {
                        DocumentSnapshot snap = firestore.collection("users")
                                .document(uid)
                                .get()
                                .get(3, TimeUnit.SECONDS);

                        if (snap.exists()) {
                            String role = snap.getString("role");
                            if (role != null) {
                                String roleUp = role.toUpperCase(Locale.ROOT);
                                if ("ADMIN".equals(roleUp) || "USER".equals(roleUp)) {
                                    authorities.add(new SimpleGrantedAuthority("ROLE_" + roleUp));
                                }
                            }
                            // Nếu bạn chỉ muốn ADMIN mới có quyền: có thể KHÔNG add USER vào authorities
                        } else {
                            System.out.println(">>> Firestore: users/" + uid + " not found");
                        }
                    } catch (Exception e) {
                        System.out.println(">>> Firestore role lookup failed: " + e.getMessage());
                    }
                }

                // 3) Set Authentication vào SecurityContext
                var authToken = new UsernamePasswordAuthenticationToken(uid, idToken, authorities);
                authToken.setDetails(Map.of(
                        "email", decoded.getEmail(),
                        "name", decoded.getName()
                ));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println(">>> Authorities = " + authorities);

            } catch (Exception e) {
                // Verify thất bại -> trả 401 (khác với 403 thiếu quyền)
                System.out.println(">>> VERIFY FAILED: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                SecurityContextHolder.clearContext();
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Invalid or expired ID token\"}");
                return;
            }
        }

        chain.doFilter(req, res);
    }
}


//                List<String> roles = rolesObj instanceof List ? (List<String>) rolesObj : List.of("USER");
//                var authorities = roles.stream()
//                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
//                        .collect(Collectors.toList());
//
//                var authentication = new UsernamePasswordAuthenticationToken(uid, idToken, authorities);
//                // Bạn có thể set thêm details (email, name):
//                // authentication.setDetails(Map.of("email", decoded.getEmail()));
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            } catch (Exception ex) {
//                // Token không hợp lệ → để rỗng context, security sẽ chặn ở layer authorize
//                SecurityContextHolder.clearContext();
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
