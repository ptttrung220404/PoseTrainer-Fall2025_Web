package org.web.posetrainer.Service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;
import org.web.posetrainer.DTO.PagedResponse;
import org.web.posetrainer.DTO.PostMailInfo;
import org.web.posetrainer.Entity.Community;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
@Service
public class CommunityService {
    private static final String COLLECTION_NAME = "community";
    private final Firestore firestore;
    private final UserService userService;
    long now = Instant.now().getEpochSecond();
    public CommunityService(Firestore firestore, UserService userService) {
        this.firestore = firestore;
        this.userService = userService;
    }
    public List<Community> getAll() throws ExecutionException, InterruptedException {
        var querySnap = firestore.collection(COLLECTION_NAME).get().get();
        List<Community> result = new ArrayList<>();
        for (DocumentSnapshot doc : querySnap.getDocuments()) {
            Community community = doc.toObject(Community.class);
            if (community != null) {
                community.setId(doc.getId());
                result.add(community);
            }
        }
        return result;
    }

    public PagedResponse<Community> getPaged(int page, int size) throws ExecutionException, InterruptedException {
        return PagedResponse.of(getAll(), page, size);
    }
    public Community getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot snap = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get();
        if (!snap.exists()) return null;
        Community community = snap.toObject(Community.class);
        if (community != null) {
            community.setId(snap.getId());
            // Load comments if needed
            loadComments(community);
        }
        return community;
    }

    public PostMailInfo toggleVisibility(String id, boolean isVisible) throws ExecutionException, InterruptedException {
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("isVisible", isVisible);
//        firestore.collection(COLLECTION_NAME)
//                .document(id)
//                .update(updates)
//                .get();
        DocumentSnapshot snapshot = firestore
                .collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get();
        if (!snapshot.exists()) {
            throw new IllegalArgumentException("Post not found");
        }
        String content = snapshot.getString("content");
        Map<String, Object> author =
                (Map<String, Object>) snapshot.get("author");

        if (author == null || !author.containsKey("uid")) {
            throw new IllegalStateException("Author information missing");
        }
        String authorUid = (String) author.get("uid");
        DocumentSnapshot userSnapshot = firestore
                .collection("users")
                .document(authorUid)
                .get()
                .get();
        String email = userSnapshot.exists()
                ? userSnapshot.getString("email")
                : null;
        Map<String, Object> updates = new HashMap<>();
        updates.put("isVisible", isVisible);


        updates.put("AdminUpdatedAt", now);
        if (!isVisible) {
            updates.put("hiddenReason", "Vi phạm Tiêu Chuẩn Cộng Đồng");
        } else {
            updates.put("hiddenReason", null);
        }
        firestore.collection(COLLECTION_NAME)
                .document(id)
                .update(updates)
                .get();
        return new PostMailInfo(
                authorUid,
                content,
                isVisible
        );
    }

    public List<Community.Comment> getComments(String postId) throws ExecutionException, InterruptedException {
        try {
            QuerySnapshot querySnap = firestore.collection(COLLECTION_NAME)
                    .document(postId)
                    .collection("comments")
                    .orderBy("createdAt")
                    .get()
                    .get();
            
            List<Community.Comment> comments = new ArrayList<>();
            for (DocumentSnapshot doc : querySnap.getDocuments()) {
                Community.Comment comment = doc.toObject(Community.Comment.class);
                if (comment != null) {
                    comment.setId(doc.getId());
                    comments.add(comment);
                }
            }
            return comments;
        } catch (Exception e) {
            // If comments subcollection doesn't exist or no index, return empty list
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getLikesWithUserInfo(String postId) throws ExecutionException, InterruptedException {
        DocumentSnapshot snap = firestore.collection(COLLECTION_NAME)
                .document(postId)
                .get()
                .get();
        if (!snap.exists()) return new ArrayList<>();
        
        Community community = snap.toObject(Community.class);
        List<Map<String, Object>> likesWithUserInfo = new ArrayList<>();
        
        if (community != null && community.getLikedBy() != null) {
            for (String uid : community.getLikedBy()) {
                Map<String, Object> likeInfo = new HashMap<>();
                likeInfo.put("uid", uid);
                
                // Lấy thông tin user từ UserService
                userService.getUserByUid(uid).ifPresentOrElse(
                    user -> {
                        likeInfo.put("displayName", user.getDisplayName() != null ? user.getDisplayName() : "Người dùng");
                        likeInfo.put("photoURL", user.getPhotoURL());
                        likeInfo.put("email", user.getEmail());
                    },
                    () -> {
                        // Nếu không tìm thấy user, dùng UID làm tên
                        likeInfo.put("displayName", "Người dùng (" + uid + ")");
                        likeInfo.put("photoURL", null);
                        likeInfo.put("email", null);
                    }
                );
                
                likesWithUserInfo.add(likeInfo);
            }
        }
        return likesWithUserInfo;
    }

    public List<String> getLikes(String postId) throws ExecutionException, InterruptedException {
        DocumentSnapshot snap = firestore.collection(COLLECTION_NAME)
                .document(postId)
                .get()
                .get();
        if (!snap.exists()) return new ArrayList<>();
        
        Community community = snap.toObject(Community.class);
        if (community != null && community.getLikedBy() != null) {
            return community.getLikedBy();
        }
        return new ArrayList<>();
    }

    private void loadComments(Community community) throws ExecutionException, InterruptedException {
        if (community != null && community.getId() != null) {
            List<Community.Comment> comments = getComments(community.getId());
            community.setComments(comments);
        }
    }
}
