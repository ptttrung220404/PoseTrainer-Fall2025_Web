package org.web.posetrainer.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.web.posetrainer.Entity.Community;
import org.web.posetrainer.Service.CommunityService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin/community")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

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
            communityService.toggleVisibility(id, isVisible);
            return ResponseEntity.ok(Map.of(
                    "id", id,
                    "isVisible", isVisible,
                    "message", "Visibility updated successfully"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
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
