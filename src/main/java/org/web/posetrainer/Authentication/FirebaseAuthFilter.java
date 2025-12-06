package org.web.posetrainer.Authentication;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

//
@Override
protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
        throws ServletException, IOException {

    System.out.println(">>> FirebaseAuthFilter ENTER path=" + req.getRequestURI());

    String bearerToken = extractBearerToken(req);
    String sessionCookie = extractSessionCookie(req);

    FirebaseToken decoded = null;
    String tokenUsed = null;

    try {
        // =============================
        // 1) ƯU TIÊN: VERIFY SESSION COOKIE
        // =============================
        if (sessionCookie != null) {
            System.out.println(">>> Using SESSION cookie");
            decoded = firebaseAuth.verifySessionCookie(sessionCookie, true);
            tokenUsed = sessionCookie;
        }

        // =============================
        // 2) FALLBACK: VERIFY ID TOKEN (Bearer Header)
        // =============================
        else if (bearerToken != null) {
            System.out.println(">>> Using Bearer token");
            decoded = firebaseAuth.verifyIdToken(bearerToken, true);
            tokenUsed = bearerToken;
        }

        // =============================
        // 3) Không có token → bỏ qua
        // =============================
        else {
            chain.doFilter(req, res);
            return;
        }

        String uid = decoded.getUid();
        System.out.println(">>> VERIFIED UID = " + uid);

        List<SimpleGrantedAuthority> authorities = loadAuthorities(uid);

        // Authentication object
        var authToken = new UsernamePasswordAuthenticationToken(
                uid,
                tokenUsed,
                authorities
        );

        authToken.setDetails(Map.of(
                "email", decoded.getEmail(),
                "name", decoded.getName() == null ? decoded.getEmail() : decoded.getName()
        ));

        SecurityContextHolder.getContext().setAuthentication(authToken);
        System.out.println(">>> Authorities = " + authorities);

    } catch (Exception e) {
        System.out.println(">>> TOKEN VERIFY FAILED: " + e);
        SecurityContextHolder.clearContext();
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Invalid or expired token\"}");
        return;
    }

    chain.doFilter(req, res);
}

    // ============================================================
    // Helper: Extract Bearer token
    // ============================================================
    private String extractBearerToken(HttpServletRequest req) {
        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }

    // ============================================================
    // Helper: Extract SESSION cookie
    // ============================================================
    private String extractSessionCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if ("SESSION".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return path.equals("/login")
                || path.equals("/forgot")
                || path.startsWith("/auth/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/")
                || path.equals("/favicon.ico")
                || path.equals("/error");
    }

    // ============================================================
    // Load authorities from Firestore (như code của bạn)
    // ============================================================
    private List<SimpleGrantedAuthority> loadAuthorities(String uid) {
        List<SimpleGrantedAuthority> auths = new ArrayList<>();

        try {
            DocumentSnapshot snap = firestore.collection("users")
                    .document(uid)
                    .get()
                    .get(3, TimeUnit.SECONDS);

            if (snap.exists()) {
                List<String> roles = (List<String>) snap.get("roles");

                if (roles != null && !roles.isEmpty()) {
                    for (String r : roles) {
                        auths.add(new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()));
                    }
                } else {
                    String role = snap.getString("role");
                    if (role != null) {
                        auths.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(">>> Firestore role lookup failed: " + e.getMessage());
        }

        return auths.isEmpty()
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
                : auths;
    }
}
