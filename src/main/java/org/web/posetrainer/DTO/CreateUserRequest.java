package org.web.posetrainer.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {
    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    private String displayName;
    private String photoURL;

    // ví dụ ["ADMIN"] hoặc ["USER"]
    private List<String> roles;

    // tuỳ chọn
    private NotificationSettings notification;

    @Data
    public static class NotificationSettings {
        private String fcmToken;
        private boolean allowNotification = true;
    }
}
