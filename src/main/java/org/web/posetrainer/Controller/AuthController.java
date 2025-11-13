package org.web.posetrainer.Controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web.posetrainer.DTO.LoginRequest;
import org.web.posetrainer.Entity.Workouts;
import org.web.posetrainer.Service.AuthService;
import org.web.posetrainer.Service.UserService;
import org.web.posetrainer.Service.WorkoutsService;
import org.springframework.security.core.GrantedAuthority;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
public class AuthController {
    //    @GetMapping("/me")
//    public ResponseEntity<?> me(Authentication auth) {
//        if (auth == null || !auth.isAuthenticated()) {
//            return ResponseEntity.status(401).body(Map.of(
//                    "error", "UNAUTHORIZED",
//                    "message", "Missing or invalid token"
//            ));
//        }
//
//        // roles -> List<String> thay vì object GrantedAuthority
//        var roles = auth.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .toList();
//
//        // nếu trong filter bạn có setDetails(Map.of("email",..., "name", ...))
//        Object details = auth.getDetails();
//        Map<?, ?> detailsMap = (details instanceof Map<?, ?> m) ? m : Map.of();
//
//        return ResponseEntity.ok(Map.of(
//                "uid", auth.getName(),
//                "roles", roles,
//                "details", detailsMap
//        ));
//    }
    private final UserService userService;
    private final AuthService authService;
    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String uid = auth.getName();
        return userService.getUserByUid(uid)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "User not found")));
    }
//    public Map<String, Object> me(Authentication auth) {
////        if (auth == null || !auth.isAuthenticated()) {
////            return Map.of(
////                    "error", "UNAUTHORIZED",
////                    "message", "Missing or invalid token"
////            );
////        }
////
////        System.out.println(">>> AUTH = " + auth);
////        System.out.println(">>> AUTHORITIES = " + auth.getAuthorities());
////       // return Map.of("uid", auth.getName(), "roles", auth.getAuthorities());
////        var roles = auth.getAuthorities().stream()
////                .map(a -> a.getAuthority())  // ví dụ ROLE_ADMIN
////                .toList();
////        Object details = auth.getDetails();
////        Map<?, ?> detailMap = (details instanceof Map<?, ?> m) ? m : Map.of();
////        return Map.of(
////                "uid", auth.getName(),
////                "roles", roles,
////                "details", detailMap
////        );
//
//    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            var resp = authService.login(request);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "INVALID_CREDENTIALS",
                    "message", e.getMessage()
            ));
        }
    }
}
