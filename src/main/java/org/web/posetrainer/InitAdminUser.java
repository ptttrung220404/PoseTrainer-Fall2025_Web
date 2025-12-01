package org.web.posetrainer;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Script để tạo tài khoản admin đầu tiên
 * Chạy class này một lần để tạo admin user
 * 
 * Cách chạy:
 * 1. Sửa email và password bên dưới
 * 2. Chạy: java -cp target/classes org.web.posetrainer.InitAdminUser
 * Hoặc chạy main method trong IDE
 */
public class InitAdminUser {
    
    // ============ THAY ĐỔI THÔNG TIN Ở ĐÂY ============
    private static final String ADMIN_EMAIL = "admin@posetrainer.com";
    private static final String ADMIN_PASSWORD = "admin123456";
    private static final String ADMIN_DISPLAY_NAME = "Administrator";
    // ===================================================
    
    public static void main(String[] args) {
        try {
            // 1. Khởi tạo Firebase
            if (FirebaseApp.getApps().isEmpty()) {
                // Tìm file serviceAccountKey.json trong resources
                InputStream serviceAccount = null;
                try {
                    // Thử load từ classpath (khi chạy từ IDE hoặc JAR)
                    serviceAccount = InitAdminUser.class.getClassLoader()
                            .getResourceAsStream("serviceAccountKey.json");
                    
                    // Nếu không tìm thấy, thử load từ file system
                    if (serviceAccount == null) {
                        Path resourcePath = Paths.get("src/main/resources/serviceAccountKey.json");
                        if (resourcePath.toFile().exists()) {
                            serviceAccount = new FileInputStream(resourcePath.toFile());
                        } else {
                            throw new RuntimeException("Không tìm thấy file serviceAccountKey.json. " +
                                    "Đảm bảo file nằm trong src/main/resources/");
                        }
                    }
                    
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .setStorageBucket("posetrainer-capstonefall2025.firebasestorage.app")
                            .build();
                    
                    FirebaseApp.initializeApp(options);
                    System.out.println("✅ Firebase initialized successfully");
                } finally {
                    if (serviceAccount != null) {
                        serviceAccount.close();
                    }
                }
            }
            
            FirebaseAuth auth = FirebaseAuth.getInstance();
            Firestore firestore = FirestoreClient.getFirestore();
            
            // 2. Kiểm tra xem user đã tồn tại chưa
            UserRecord existingUser = null;
            try {
                existingUser = auth.getUserByEmail(ADMIN_EMAIL);
                System.out.println("⚠️  User với email " + ADMIN_EMAIL + " đã tồn tại!");
                System.out.println("   UID: " + existingUser.getUid());
                
                // Cập nhật role thành ADMIN nếu chưa có
                Map<String, Object> claims = existingUser.getCustomClaims();
                @SuppressWarnings("unchecked")
                List<String> roles = claims != null ? (List<String>) claims.get("roles") : null;
                
                if (roles == null || !roles.contains("ADMIN")) {
                    System.out.println("🔄 Đang cập nhật role thành ADMIN...");
                    auth.setCustomUserClaims(existingUser.getUid(), Map.of("roles", List.of("ADMIN")));
                    
                    // Cập nhật trong Firestore
                    firestore.collection("users")
                            .document(existingUser.getUid())
                            .update("roles", List.of("ADMIN"))
                            .get();
                    
                    System.out.println("✅ Đã cập nhật role thành ADMIN!");
                } else {
                    System.out.println("✅ User đã có role ADMIN");
                }
                
                return;
            } catch (FirebaseAuthException e) {
                if (e.getErrorCode().equals("user-not-found")) {
                    System.out.println("📝 User chưa tồn tại, đang tạo mới...");
                } else {
                    throw e;
                }
            }
            
            // 3. Tạo user mới trong Firebase Auth
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(ADMIN_EMAIL)
                    .setPassword(ADMIN_PASSWORD)
                    .setDisplayName(ADMIN_DISPLAY_NAME)
                    .setEmailVerified(true);
            
            UserRecord userRecord = auth.createUser(request);
            String uid = userRecord.getUid();
            System.out.println("✅ Đã tạo user trong Firebase Auth");
            System.out.println("   UID: " + uid);
            
            // 4. Gán role ADMIN
            auth.setCustomUserClaims(uid, Map.of("roles", List.of("ADMIN")));
            System.out.println("✅ Đã gán role ADMIN");
            
            // 5. Tạo document trong Firestore
            Map<String, Object> userDoc = Map.of(
                    "uid", uid,
                    "email", ADMIN_EMAIL,
                    "displayName", ADMIN_DISPLAY_NAME,
                    "roles", List.of("ADMIN"),
                    "active", true,
                    "createdAt", System.currentTimeMillis(),
                    "lastLoginAt", 0L
            );
            
            firestore.collection("users")
                    .document(uid)
                    .set(userDoc)
                    .get();
            
            System.out.println("✅ Đã tạo document trong Firestore");
            
            // 6. Kết quả
            System.out.println("\n" + "=".repeat(50));
            System.out.println("🎉 TẠO TÀI KHOẢN ADMIN THÀNH CÔNG!");
            System.out.println("=".repeat(50));
            System.out.println("Email: " + ADMIN_EMAIL);
            System.out.println("Password: " + ADMIN_PASSWORD);
            System.out.println("Display Name: " + ADMIN_DISPLAY_NAME);
            System.out.println("Role: ADMIN");
            System.out.println("=".repeat(50));
            System.out.println("\nBạn có thể đăng nhập tại: http://localhost:8080/login");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

