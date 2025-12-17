package org.web.posetrainer.Service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;
import org.web.posetrainer.Entity.Excercise;
import org.web.posetrainer.Entity.Feedbacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FeedbackService {
    private static final String COLLECTION_NAME = "feedbacks";
    private final UserService userService;
    private final Firestore firestore;
    private final CommunityService communityService;
    public FeedbackService(UserService userService, Firestore firestore, CommunityService communityService) {
        this.userService = userService;
        this.firestore = firestore;
        this.communityService = communityService;
    }
    public List<Map<String, Object>> getAllFeedbacksWithUserInfo() throws ExecutionException, InterruptedException {
        var querySnap = firestore.collection(COLLECTION_NAME).get().get();
        List<Map<String, Object>> result = new ArrayList<>();
        for (DocumentSnapshot doc : querySnap.getDocuments()) {
            Feedbacks fb = doc.toObject(Feedbacks.class);
            if (fb != null) {
                fb.setId(doc.getId());
                Map<String, Object> feedbackMap = new HashMap<>();
                feedbackMap.put("id", fb.getId());
                feedbackMap.put("uid", fb.getUid());
                feedbackMap.put("type", fb.getType());
                feedbackMap.put("exerciseId", fb.getExerciseId());
                feedbackMap.put("exerciseName", fb.getExerciseName());
                feedbackMap.put("postId", fb.getPostId());
                feedbackMap.put("postContent", fb.getPostContent());
                feedbackMap.put("content", fb.getContent());
                feedbackMap.put("status", fb.getStatus());
                feedbackMap.put("createdAt", fb.getCreatedAt());
                feedbackMap.put("updatedAt", fb.getUpdatedAt());

                // Get user info
                if (fb.getUid() != null) {
                    userService.getUserByUid(fb.getUid()).ifPresentOrElse(
                            user -> {
                                feedbackMap.put("userDisplayName", user.getDisplayName());
                                feedbackMap.put("userEmail", user.getEmail());
                            },
                            () -> {
                                feedbackMap.put("userDisplayName", null);
                                feedbackMap.put("userEmail", null);
                            }
                    );
                } else {
                    feedbackMap.put("userDisplayName", null);
                    feedbackMap.put("userEmail", null);
                }

                // Default post author fields
                feedbackMap.put("postAuthorUid", null);
                feedbackMap.put("postAuthorName", null);
                feedbackMap.put("postAuthorEmail", null);

                // If feedback is about a post, enrich with post author info for filtering/UI
                if ("post".equals(fb.getType()) && fb.getPostId() != null && !fb.getPostId().isBlank()) {
                    try {
                        DocumentSnapshot postSnap = firestore.collection("community")
                                .document(fb.getPostId())
                                .get()
                                .get();
                        if (postSnap.exists()) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> author = (Map<String, Object>) postSnap.get("author");
                            if (author != null) {
                                String authorUid = (String) author.get("uid");
                                String authorName = (String) author.get("displayName");
                                feedbackMap.put("postAuthorUid", authorUid);
                                feedbackMap.put("postAuthorName", authorName);
                                if (authorUid != null) {
                                    userService.getUserByUid(authorUid).ifPresentOrElse(
                                            user -> feedbackMap.put("postAuthorEmail", user.getEmail()),
                                            () -> feedbackMap.put("postAuthorEmail", null)
                                    );
                                }
                            }
                        }
                    } catch (Exception e) {
                        // ignore enrichment errors (still return base feedback)
                        e.printStackTrace();
                    }
                }

                result.add(feedbackMap);
            }
        }
        return result;
    }
    public Map<String, Object> getByIdWithUserInfo(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot snap = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get();
        if (!snap.exists()) return null;
        Feedbacks fb = snap.toObject(Feedbacks.class);
        if (fb == null) return null;
        fb.setId(snap.getId());

        Map<String, Object> feedbackMap = new HashMap<>();
        feedbackMap.put("id", fb.getId());
        feedbackMap.put("uid", fb.getUid());
        feedbackMap.put("type", fb.getType());
        feedbackMap.put("exerciseId", fb.getExerciseId());
        feedbackMap.put("exerciseName", fb.getExerciseName());
        feedbackMap.put("postId", fb.getPostId());
        feedbackMap.put("postContent", fb.getPostContent());
        feedbackMap.put("content", fb.getContent());
        feedbackMap.put("status", fb.getStatus());
        feedbackMap.put("createdAt", fb.getCreatedAt());
        feedbackMap.put("updatedAt", fb.getUpdatedAt());

        // Get user info (feedback sender)
        if (fb.getUid() != null) {
            userService.getUserByUid(fb.getUid()).ifPresentOrElse(
                    user -> {
                        feedbackMap.put("userDisplayName", user.getDisplayName());
                        feedbackMap.put("userEmail", user.getEmail());
                        feedbackMap.put("userPhotoURL", user.getPhotoURL());
                    },
                    () -> {
                        feedbackMap.put("userDisplayName", null);
                        feedbackMap.put("userEmail", null);
                        feedbackMap.put("userPhotoURL", null);
                    }
            );
        } else {
            feedbackMap.put("userDisplayName", null);
            feedbackMap.put("userEmail", null);
            feedbackMap.put("userPhotoURL", null);
        }

        // Get post info and author if feedback is about a post
        if ("post".equals(fb.getType()) && fb.getPostId() != null) {
            try {
                var post = communityService.getById(fb.getPostId());
                if (post != null) {
                    feedbackMap.put("postId", post.getId());
                    if (post.getAuthor() != null) {
                        feedbackMap.put("postAuthorUid", post.getAuthor().getUid());
                        feedbackMap.put("postAuthorName", post.getAuthor().getDisplayName());
                        // Get author email from UserService
                        if (post.getAuthor().getUid() != null) {
                            userService.getUserByUid(post.getAuthor().getUid()).ifPresentOrElse(
                                    user -> feedbackMap.put("postAuthorEmail", user.getEmail()),
                                    () -> feedbackMap.put("postAuthorEmail", null)
                            );
                        } else {
                            feedbackMap.put("postAuthorEmail", null);
                        }
                    } else {
                        feedbackMap.put("postAuthorUid", null);
                        feedbackMap.put("postAuthorName", null);
                        feedbackMap.put("postAuthorEmail", null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // If error getting post, just set null values
                feedbackMap.put("postAuthorUid", null);
                feedbackMap.put("postAuthorName", null);
                feedbackMap.put("postAuthorEmail", null);
            }
        }

        return feedbackMap;
    }
    public boolean updateStatus(String id, String status) throws ExecutionException, InterruptedException {
        try {
            long updatedAt = System.currentTimeMillis() / 1000;
            firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .update("status", status, "updatedAt", updatedAt)
                    .get();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
