package org.web.posetrainer.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class Community {

    public String id;
    public String uid;
    public Author author;

    public String content;
    public String imageUrl;
    public String imagePath;

    public List<String> imageUrls;
    public List<String> imagePaths;

    public long likesCount;
    public long commentsCount;

    public Timestamp createdAt;
    public Timestamp updatedAt;

    public List<String> likedBy;

    @JsonProperty("isVisible")
    @PropertyName("isVisible")
    public boolean isVisible = true;

    // NOT stored in Firestore - only for UI
    @Exclude
    public boolean likedByMe = false;

    // NOT stored in Firestore - for UI detail view
    @Exclude
    public List<Comment> comments = new ArrayList<>();

    // ====== Firestore requires a no-arg constructor ======
    public Community() {
        this.imageUrls = new ArrayList<>();
        this.imagePaths = new ArrayList<>();
        this.likedBy = new ArrayList<>();
    }

    // Combine old imageUrl → imageUrls
    @Exclude
    public List<String> getImageUrls() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls;
        }
        List<String> urls = new ArrayList<>();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            urls.add(imageUrl);
        }
        return urls;
    }

    // ====== Utility getters for UI ======

    @Exclude
    public String getDisplayName() {
        return (author != null && author.displayName != null && !author.displayName.isEmpty())
                ? author.displayName
                : "Người dùng";
    }

    @Exclude
    public String getPhotoURL() {
        return (author != null) ? author.photoURL : null;
    }

    @Exclude
    public String getTimeString() {
        if (createdAt == null) return "";
        return createdAt.toDate().toInstant()
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @JsonProperty("isVisible")
    @PropertyName("isVisible")
    public boolean getIsVisible() {
        return isVisible;
    }

    @JsonProperty("isVisible")
    @PropertyName("isVisible")
    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    // ====== Nested classes ======

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Author {
        public String uid;
        public String displayName;
        public String photoURL;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Like {
        public String uid;
        public Timestamp createdAt;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Comment {
        public String id;
        public String postId;
        public String uid;
        public String displayName;
        public String photoURL;
        public String text;
        public Timestamp createdAt;
    }
}
