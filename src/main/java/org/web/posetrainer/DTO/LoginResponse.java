package org.web.posetrainer.DTO;
import lombok.Data;
import java.util.List;

@Data
public class LoginResponse {
    private String localId;       // uid
    private String email;
    private String displayName;
    private String idToken;       // <-- JWT dùng cho Authorization: Bearer ...
    private String refreshToken;
    private String expiresIn;
    private String profilePicture;
    // (tuỳ chọn) trả thêm roles hiện có từ Firestore
    private List<String> roles;
}
