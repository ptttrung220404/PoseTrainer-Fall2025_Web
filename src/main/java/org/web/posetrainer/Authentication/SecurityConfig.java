package org.web.posetrainer.Authentication;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final FirebaseApp firebaseApp;
    private final Firestore firestore;

    public SecurityConfig(FirebaseApp firebaseApp, Firestore firestore) {
        this.firestore = firestore;
        System.out.println("FirebaseApp loaded: " + firebaseApp.getName());
        this.firebaseApp = firebaseApp;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           FirebaseAuth firebaseAuth,
                                           Firestore firestore) throws Exception {
        var firebaseFilter = new FirebaseAuthFilter(firebaseAuth, firestore);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**", "/login","/forgot").permitAll()
                        .requestMatchers("/favicon.ico", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/health", "/actuator/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/me").authenticated()
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated()
                )
                .securityMatcher("/admin/**", "/me")
                .addFilterBefore(firebaseFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            System.out.println("Auth failed for: " + req.getRequestURI());
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                );

        return http.build();
    }



    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("*")); // chỉnh theo domain của bạn
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}