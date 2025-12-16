package org.web.posetrainer.Controller;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.web.posetrainer.DTO.PostMailInfo;
import org.web.posetrainer.Entity.Community;
import org.web.posetrainer.Service.CommunityService;
import org.web.posetrainer.Service.MailAsyncService;
import org.web.posetrainer.Service.MailService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin/community")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;
    private final MailService mailService;
    private final Firestore firestore;
    private final MailAsyncService mailAsyncService;

    @GetMapping
    public List<Community> getAll() throws ExecutionException, InterruptedException {
        return communityService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            Community post = communityService.getById(id);
            if (post == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/visibility")
    public ResponseEntity<?> toggleVisibility(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> request
    ) {
        try {
            Boolean isVisible = request.get("isVisible");
            if (isVisible == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "isVisible field is required"));
            }
            PostMailInfo mailInfo =
                    communityService.toggleVisibility(id, isVisible);
            try {
                sendPostVisibilityMail(mailInfo);
            } catch (Exception e) {
                // log thôi, không throw
                e.printStackTrace();
            }
            return ResponseEntity.ok(Map.of(
                    "id", id,
                    "isVisible", isVisible,
                    "message", isVisible
                            ? "Bài viết đã được hiển thị"
                            : "Bài viết đã bị ẩn do vi phạm tiêu chuẩn cộng đồng"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private void sendPostVisibilityMail(PostMailInfo info)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot userSnapshot =
                firestore.collection("users")
                        .document(info.authorUid())
                        .get()
                        .get();

        if (!userSnapshot.exists()) return;

        String email = userSnapshot.getString("email");
        if (email == null || email.isBlank()) return;

        mailAsyncService.sendPostVisibilityMail(
                email,
                info.content(),
                info.isVisible()
        );
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable String id) {
        try {
            List<Community.Comment> comments = communityService.getComments(id);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<?> getLikes(@PathVariable String id) {
        try {
            List<Map<String, Object>> likesWithUserInfo = communityService.getLikesWithUserInfo(id);
            return ResponseEntity.ok(Map.of(
                    "postId", id,
                    "likes", likesWithUserInfo,
                    "count", likesWithUserInfo.size()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
