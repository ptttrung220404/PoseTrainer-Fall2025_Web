package org.web.posetrainer.Authentication;

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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FirebaseAuthFilter extends OncePerRequestFilter {
    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthFilter(FirebaseApp app) {
        this.firebaseAuth = FirebaseAuth.getInstance(app);
    }

    

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.substring(7);
            try {
                FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken, true); // check revocation
                String uid = decoded.getUid();

                // Lấy role từ custom claims (nếu có), vd: { "roles": ["ADMIN","TRAINER"] }
                Object rolesObj = decoded.getClaims().get("roles");
                List<String> roles = rolesObj instanceof List ? (List<String>) rolesObj : List.of("USER");
                var authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList());

                var authentication = new UsernamePasswordAuthenticationToken(uid, idToken, authorities);
                // Bạn có thể set thêm details (email, name):
                // authentication.setDetails(Map.of("email", decoded.getEmail()));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception ex) {
                // Token không hợp lệ → để rỗng context, security sẽ chặn ở layer authorize
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}