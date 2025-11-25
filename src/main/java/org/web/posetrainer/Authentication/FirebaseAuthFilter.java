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

    // Ưu tiên dùng constructor nhận FirebaseAuth
    public FirebaseAuthFilter(FirebaseAuth firebaseAuth, Firestore firestore) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
    }

    // Giữ constructor cũ cho tương thích nếu đâu đó vẫn truyền FirebaseApp
    public FirebaseAuthFilter(FirebaseApp app, Firestore firestore) {
        this(FirebaseAuth.getInstance(app), firestore);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        System.out.println(">>> FirebaseAuthFilter ENTER, path=" + req.getRequestURI());

        // ==========================
        //  PHẦN 5 - LẤY TOKEN TỪ HEADER HOẶC COOKIE
        // ==========================
        String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
        String idToken = null;

        System.out.println(">>> Authorization header = " + authHeader);

        // 1) Header Authorization: Bearer xxx
        if (authHeader != null) {
            String[] parts = authHeader.trim().split("\\s+");
            if (parts.length == 2 && parts[0].equalsIgnoreCase("Bearer")) {
                idToken = parts[1];
            } else {
                System.out.println(">>> Bad Authorization format");
            }
        }

        // 2) Nếu chưa có token -> thử lấy từ cookie ID_TOKEN (Thymeleaf login)
        if (idToken == null && req.getCookies() != null) {
            Arrays.stream(req.getCookies())
                    .filter(c -> "ID_TOKEN".equals(c.getName()))
                    .findFirst()
                    .ifPresent(c -> {
                        System.out.println(">>> Token loaded from cookie");
                        // lambda không cho gán biến ngoài, nên dùng mảng 1 phần tử
                    });
            for (var c : req.getCookies()) {
                if ("ID_TOKEN".equals(c.getName())) {
                    idToken = c.getValue();
                    System.out.println(">>> Token loaded from cookie");
                    break;
                }
            }
        }

        // 3) Không có token -> bỏ qua, để Security xử lý 401/403 như thường
        if (idToken == null) {
            chain.doFilter(req, res);
            return;
        }

        try {
            // 4) Verify ID token
            FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken, true);
            String uid = decoded.getUid();
            System.out.println(">>> VERIFIED uid=" + uid);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            // 4a) roles từ custom claims (nếu có)
            Object rolesObj = decoded.getClaims().get("roles");
            if (rolesObj instanceof List<?> list && !list.isEmpty()) {
                for (Object r : list) {
                    String roleUp = String.valueOf(r).toUpperCase(Locale.ROOT);
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + roleUp));
                }
            }

            // 4b) roles từ Firestore (ghi đè nếu có)
            try {
                DocumentSnapshot snap = firestore.collection("users")
                        .document(uid)
                        .get()
                        .get(3, TimeUnit.SECONDS);

                if (snap.exists()) {
                    // Ưu tiên mảng roles: ["ADMIN","TRAINER"]
                    @SuppressWarnings("unchecked")
                    List<String> rolesArr = (List<String>) snap.get("roles");
                    if (rolesArr != null && !rolesArr.isEmpty()) {
                        authorities.clear(); // Firestore là nguồn sự thật
                        for (String r : rolesArr) {
                            String roleUp = r.toUpperCase(Locale.ROOT);
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleUp));
                        }
                    } else {
                        // Hoặc field role: "ADMIN"
                        String role = snap.getString("role");
                        if (role != null) {
                            String roleUp = role.toUpperCase(Locale.ROOT);
                            if ("ADMIN".equals(roleUp) || "USER".equals(roleUp)) {
                                authorities.clear(); // ưu tiên Firestore
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleUp));
                            }
                        }
                    }
                } else {
                    System.out.println(">>> Firestore: users/" + uid + " not found");
                }
            } catch (Exception e) {
                System.out.println(">>> Firestore role lookup failed: " + e.getMessage());
            }

            // 5) Set Authentication
            // 5) Set Authentication
            var authToken = new UsernamePasswordAuthenticationToken(uid, idToken, authorities);

            String email = decoded.getEmail();
            String name = decoded.getName();

            if (name == null || name.isBlank()) {
                name = email; // fallback
            }

            authToken.setDetails(Map.of(
                    "email", email,
                    "name", name
            ));

            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println(">>> Authorities = " + authorities);

        } catch (Exception e) {
            System.out.println(">>> VERIFY FAILED: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            SecurityContextHolder.clearContext();
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Invalid or expired ID token\"}");
            return;
        }

        chain.doFilter(req, res);
    }
}
