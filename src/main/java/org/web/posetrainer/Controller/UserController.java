package org.web.posetrainer.Controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.web.posetrainer.DTO.CreateUserRequest;
import org.web.posetrainer.Entity.User;
import org.web.posetrainer.Service.MailAsyncService;
import org.web.posetrainer.Service.MailService;
import org.web.posetrainer.Service.UserService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin/users")

@PreAuthorize("hasRole('ADMIN')")

public class UserController {
    private final FirebaseAuth firebaseAuth;
    private final Firestore firestore;
    private final UserService userService;
    private final MailService mailService;
    private final MailAsyncService mailAsyncService;

    public UserController(FirebaseAuth firebaseAuth, Firestore firestore, UserService userService, MailService mailService, MailAsyncService mailAsyncService) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
        this.userService = userService;
        this.mailService = mailService;
        this.mailAsyncService = mailAsyncService;
    }


    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateUserRequest req) {
        String uid = null;
        try {
            // 1) Tạo user trong Firebase Auth
            CreateRequest cr = new CreateRequest()
                    .setEmail(req.getEmail())
                    .setPassword(req.getPassword());

            if (req.getDisplayName() != null) cr.setDisplayName(req.getDisplayName());
            if (req.getPhotoURL() != null) cr.setPhotoUrl(req.getPhotoURL());

            UserRecord ur = firebaseAuth.createUser(cr);
            uid = ur.getUid();

            // (tuỳ chọn) gán custom claims để FE có thể dùng
            List<String> roles = (req.getRoles() == null || req.getRoles().isEmpty())
                    ? List.of("USER")
                    : req.getRoles();
            firebaseAuth.setCustomUserClaims(uid, Map.of("roles", roles));

            // 2) Lưu document Firestore users/{uid}
            User userDoc = new User();
            userDoc.setUid(uid);
            userDoc.setEmail(req.getEmail());
            userDoc.setDisplayName(req.getDisplayName());
            userDoc.setPhotoURL(req.getPhotoURL());
            userDoc.setRoles(roles);
            userDoc.setActive(req.getActive() != null ? req.getActive() : true);
            if (req.getNotification() != null) {
                userDoc.setNotification(new User.NotificationSettings(
                        req.getNotification().getFcmToken(),
                        req.getNotification().isAllowNotification()
                ));
            }
            long now = System.currentTimeMillis() * 1000;
            userDoc.setCreatedAt(now);
            userDoc.setLastLoginAt(0L);

            ApiFuture<?> fut = firestore.collection("users")
                    .document(uid)
                    .set(userDoc);
            fut.get(); // đợi ghi xong để chắc chắn

            // 3) Trả kết quả
            return ResponseEntity.ok(Map.of(
                    "uid", uid,
                    "email", userDoc.getEmail(),
                    "displayName", userDoc.getDisplayName(),
                    "roles", userDoc.getRoles(),
                    "active", userDoc.isActive()
            ));

        } catch (Exception e) {
            // Rollback: nếu đã tạo Auth mà Firestore lỗi -> xoá user trong Auth
            if (uid != null) {
                try {
                    firebaseAuth.deleteUser(uid);
                } catch (Exception ignore) {
                }
            }
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "CREATE_USER_FAILED",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{uid}")
    public ResponseEntity<?> get(@PathVariable String uid) throws ExecutionException, InterruptedException {
        var snap = firestore.collection("users").document(uid).get().get();
        if (!snap.exists()) return ResponseEntity.notFound().build();
        User user = snap.toObject(User.class);
        if (user != null) user.setUid(uid);
        return ResponseEntity.ok(user);
    }

    // (tuỳ chọn) cập nhật role nhanh
    @PatchMapping("/{uid}/roles")
    public ResponseEntity<?> updateRoles(@PathVariable String uid, @RequestBody Map<String, List<String>> body)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        List<String> roles = body.getOrDefault("roles", List.of("USER"));
        firebaseAuth.setCustomUserClaims(uid, Map.of("roles", roles));
        firestore.collection("users").document(uid).update("roles", roles).get();
        return ResponseEntity.ok(Map.of("uid", uid, "roles", roles));
    }

    // Khóa/mở khóa tài khoản
//    @PatchMapping("/{uid}/active")
//    public ResponseEntity<?> updateActiveStatus(
//            @PathVariable String uid,
//            @RequestBody Map<String, Boolean> body)
//            throws ExecutionException, InterruptedException, FirebaseAuthException {
//        Boolean active = body.get("active");
//        if (active == null) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "error", "INVALID_REQUEST",
//                    "message", "Trường 'active' là bắt buộc"
//            ));
//        }
//
//        DocumentSnapshot userDoc = firestore
//                .collection("users")
//                .document(uid)
//                .get()
//                .get();
//
//        if (!userDoc.exists()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
//                    "error", "USER_NOT_FOUND"
//            ));
//        }
//
//        // Cập nhật trong Firestore
//        firestore.collection("users").document(uid).update("active", active).get();
//
//        // Cập nhật trong Firebase Auth: disable/enable user
//        UpdateRequest updateRequest = new UpdateRequest(uid).setDisabled(!active);
//        firebaseAuth.updateUser(updateRequest);
//
//        try {
//            UserRecord userRecord = firebaseAuth.getUser(uid);
//            String email = userRecord.getEmail();
//            if (email != null) {
//                mailAsyncService.sendAccountStatusMail(email, active);
//            }
//        } catch (Exception e) {
//            e.printStackTrace(); // log thôi, không fail API
//        }
//        return ResponseEntity.ok(Map.of(
//                "uid", uid,
//                "active", active,
//                "message", active ? "Tài khoản đã được mở khóa" : "Tài khoản đã được khóa"
//        ));
//    }
    @PatchMapping("/{uid}/active")
    public ResponseEntity<?> updateActiveStatus(
            @PathVariable String uid,
            @RequestBody Map<String, Boolean> body)
            throws ExecutionException, InterruptedException, FirebaseAuthException {

        Boolean active = body.get("active");
        if (active == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "INVALID_REQUEST",
                    "message", "Trường 'active' là bắt buộc"
            ));
        }

        // 1. Kiểm tra user trong Firebase Auth (nguồn chính)
        UserRecord userRecord;
        try {
            userRecord = firebaseAuth.getUser(uid);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "USER_NOT_FOUND"
            ));
        }

        // 2. Disable / Enable account trong Firebase Auth
        boolean disabled = !active;
        UpdateRequest updateRequest = new UpdateRequest(uid)
                .setDisabled(disabled);
        firebaseAuth.updateUser(updateRequest);

        // 3. Mirror trạng thái sang Firestore
        firestore.collection("users")
                .document(uid)
                .update("active", active)
                .get();

        // 4. Gửi mail thông báo (không fail API)
        try {
            String email = userRecord.getEmail();
            if (email != null) {
                mailAsyncService.sendAccountStatusMail(email, active);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(Map.of(
                "uid", uid,
                "active", active,
                "message", active
                        ? "Tài khoản đã được kích hoạt"
                        : "Tài khoản đã bị vô hiệu hóa"
        ));
    }

}
