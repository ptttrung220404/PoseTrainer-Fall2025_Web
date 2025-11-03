package org.web.posetrainer.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web.posetrainer.Entity.Workouts;
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
    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        System.out.println(">>> AUTH = " + auth);
        System.out.println(">>> AUTHORITIES = " + auth.getAuthorities());
        return Map.of("uid", auth.getName(), "roles", auth.getAuthorities());
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
