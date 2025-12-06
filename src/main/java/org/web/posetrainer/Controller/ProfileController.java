package org.web.posetrainer.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.web.posetrainer.Entity.User;
import org.web.posetrainer.Service.AuthService;
import org.web.posetrainer.Service.UserService;

import jakarta.servlet.http.HttpServletResponse;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/profile")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/change-password")
    public ResponseEntity<?> adminChangePassword(@RequestParam String newPassword,
                                                 Principal principal) {
        try {
            String uid = principal.getName(); // Firebase UID của admin
            authService.adminChangeOwnPassword(uid, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(
            Authentication auth,
            @RequestBody Map<String, Object> request) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String uid = auth.getName();

        try {
            User updatedUser = new User();
            updatedUser.setUid(uid);

            if (request.containsKey("displayName")) {
                updatedUser.setDisplayName((String) request.get("displayName"));
            }
            if (request.containsKey("photoURL")) {
                updatedUser.setPhotoURL((String) request.get("photoURL"));
            }
            if (request.containsKey("notification")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> notifMap = (Map<String, Object>) request.get("notification");
                if (notifMap != null) {
                    User.NotificationSettings notif = new User.NotificationSettings();
                    if (notifMap.containsKey("fcmToken")) {
                        notif.setFcmToken((String) notifMap.get("fcmToken"));
                    }
                    if (notifMap.containsKey("allowNotification")) {
                        notif.setAllowNotification((Boolean) notifMap.get("allowNotification"));
                    }
                    updatedUser.setNotification(notif);
                }
            }

            boolean success = userService.updateUser(uid, updatedUser);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "message", "Cập nhật thông tin thành công",
                        "uid", uid
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of("error", "Cập nhật thất bại"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


}

