package org.web.posetrainer.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.web.posetrainer.DTO.LoginRequest;
import org.web.posetrainer.Entity.User;
import org.web.posetrainer.Service.AuthService;
import org.web.posetrainer.Service.UserService;
import org.web.posetrainer.Firebase.FirebaseStorageService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

import java.security.Principal;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/profile")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;
    private final AuthService authService;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseStorageService firebaseStorageService;

    @PostMapping("/change-password")
    public ResponseEntity<?> adminChangePassword(@RequestBody Map<String, String> body,
                                                 Principal principal,
                                                 @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
                                                 jakarta.servlet.http.HttpServletResponse response) {
        try {
            String uid = principal.getName(); // Firebase UID của admin
            String currentPassword = body.getOrDefault("currentPassword", "").trim();
            String newPassword = body.getOrDefault("newPassword", "").trim();

            if (newPassword.isEmpty() || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới tối thiểu 6 ký tự"));
            }

            // Verify current password (Admin SDK can't verify by itself, so use Identity Toolkit login)
            Optional<User> currentUserOpt = userService.getUserByUid(uid);
            if (currentUserOpt.isEmpty() || currentUserOpt.get().getEmail() == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Không tìm thấy email người dùng"));
            }
            String email = currentUserOpt.get().getEmail();

            if (currentPassword.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập mật khẩu hiện tại"));
            }

            // Will throw if invalid
            LoginRequest verifyReq = new LoginRequest();
            verifyReq.setEmail(email);
            verifyReq.setPassword(currentPassword);
            authService.login(verifyReq);

            // Update password and revoke tokens so SESSION cookie is invalidated (verifySessionCookie(..., true))
            firebaseAuth.updateUser(new UserRecord.UpdateRequest(uid).setPassword(newPassword));
            firebaseAuth.revokeRefreshTokens(uid);

            // Clear SESSION cookie so browser goes back to /login on next request
            ResponseCookie clear = ResponseCookie.from("SESSION", "")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(0)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, clear.toString());

            return ResponseEntity.ok(Map.of(
                    "message", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu hiện tại không đúng hoặc có lỗi xảy ra"));
        }
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(Authentication auth,
                                          @RequestPart("avatar") MultipartFile avatar) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String uid = auth.getName();
        try {
            if (avatar == null || avatar.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn ảnh"));
            }
            // Basic validation
            String ct = avatar.getContentType() == null ? "" : avatar.getContentType().toLowerCase();
            if (!ct.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "File phải là ảnh"));
            }
            // 3MB limit
            if (avatar.getSize() > 3 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ảnh tối đa 3MB"));
            }

            String url = firebaseStorageService.uploadUserAvatar(uid, avatar);
            if (url == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Upload thất bại"));
            }

            // Update Firestore user document
            User u = new User();
            u.setUid(uid);
            u.setPhotoURL(url);
            userService.updateUser(uid, u);

            // Update Firebase Auth user record photoURL (optional but useful)
            firebaseAuth.updateUser(new UserRecord.UpdateRequest(uid).setPhotoUrl(url));

            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật ảnh đại diện thành công",
                    "photoURL", url
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
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

