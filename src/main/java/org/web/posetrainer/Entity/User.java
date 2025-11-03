package org.web.posetrainer.Entity;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String uid;
    private String email;
    private String displayName;
    private String photoURL;
    private List<String> providerIds;
    private long createdAt;
    private long lastLoginAt;
    private NotificationSettings notification;
    private List<String> roles;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationSettings implements Serializable {
        private String fcmToken;
        private boolean allowNotification;
    }
}
