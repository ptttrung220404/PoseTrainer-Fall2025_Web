package org.web.posetrainer.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Feedbacks {
    private String id;
    private String uid; // UID của người dùng gửi feedback
    private String type; // "exercise", "app", hoặc "post"
    private String exerciseId; // ID của bài tập (nếu type = "exercise")
    private String exerciseName; // Tên bài tập (để hiển thị, không cần query lại)
    private String postId; // ID của bài viết (nếu type = "post")
    private String postContent; // Nội dung bài viết (để hiển thị, không cần query lại)
    private String content; // Nội dung feedback
    private String status; // "pending", "read", "resolved"
    private long createdAt; // Timestamp khi tạo feedback
    private long updatedAt; // Timestamp khi cập nh
}
