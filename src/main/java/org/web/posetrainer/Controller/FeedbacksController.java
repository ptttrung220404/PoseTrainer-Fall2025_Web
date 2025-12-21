package org.web.posetrainer.Controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.web.posetrainer.Service.FeedbackService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin/feedbacks")
@PreAuthorize("hasRole('ADMIN')")
@AllArgsConstructor
public class FeedbacksController {
    private final FeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllFeedbacks() throws ExecutionException, InterruptedException {
        List<Map<String, Object>> feedbacks = feedbackService.getAllFeedbacksWithUserInfo();
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFeedbackById(@PathVariable String id) throws ExecutionException, InterruptedException {
        Map<String, Object> feedback = feedbackService.getByIdWithUserInfo(id);
        if (feedback == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(feedback);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        String status = body.get("status");
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "INVALID_REQUEST",
                    "message", "Trường 'status' là bắt buộc"
            ));
        }

        // Validate status values
        if (!status.equals("pending") && !status.equals("accepted") &&
                !status.equals("rejected") && !status.equals("resolved")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "INVALID_STATUS",
                    "message", "Status phải là: pending, accepted, rejected, hoặc resolved"
            ));
        }

        boolean success = feedbackService.updateStatus(id, status);
        if (!success) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "UPDATE_FAILED",
                    "message", "Không thể cập nhật trạng thái feedback"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "id", id,
                "status", status,
                "message", "Cập nhật trạng thái thành công"
        ));
    }
}
