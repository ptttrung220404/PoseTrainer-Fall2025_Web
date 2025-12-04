package org.web.posetrainer.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.web.posetrainer.Entity.Collections;
import org.web.posetrainer.Service.CollectionsService;
import org.web.posetrainer.Firebase.FirebaseStorageService;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin/collections")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CollectionsController {
    private final CollectionsService collectionsService;
    private final FirebaseStorageService firebaseStorageService;

    @GetMapping
    public List<Collections> getAll() throws ExecutionException, InterruptedException {
        return collectionsService.getAll();
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCollection(@RequestPart("data") Collections request, @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail, Principal principal) throws Exception {

        String adminId = principal.getName();
        request.setCreatedBy(adminId);

        // 1) Tạo collection trước → sinh docId
        String id = collectionsService.createCollection(request);

        // 2) Upload thumbnail nếu có
        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            thumbnailUrl = firebaseStorageService.uploadCollectionThumbnail(id, thumbnail);

            // 3) Update lại collection với thumbnailUrl
            collectionsService.updateCollectionThumbnail(id, thumbnailUrl);
        }

        return ResponseEntity.ok(Map.of("id", id, "thumbnailUrl", thumbnailUrl));
    }

    @GetMapping("/{docId}")
    public ResponseEntity<?> getById(@PathVariable String docId) {
        try {
            Collections collection = collectionsService.getById(docId);
            if (collection == null) return ResponseEntity.notFound().build();

            return ResponseEntity.ok(collection);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(value = "/{docId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCollection(@PathVariable String docId, @RequestBody Collections request) {
        try {
            Collections existing = collectionsService.getById(docId);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            existing.setTitle(request.getTitle());
            existing.setDescription(request.getDescription());
            existing.setCategory(request.getCategory());
            existing.setWorkoutTemplateIds(request.getWorkoutTemplateIds());
            existing.setIsPublic(request.getIsPublic());
            existing.setTags(request.getTags());

            collectionsService.updateCollection(docId, existing);
            return ResponseEntity.ok(existing);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(value = "/{docId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateThumbnail(@PathVariable String docId, @RequestPart("thumbnail") MultipartFile thumbnail) {
        try {
            if (thumbnail == null || thumbnail.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Thumbnail file is required"));
            }
            Collections existing = collectionsService.getById(docId);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }
            String thumbnailUrl = firebaseStorageService.uploadCollectionThumbnail(docId, thumbnail);
            collectionsService.updateCollectionThumbnail(docId, thumbnailUrl);
            return ResponseEntity.ok(Map.of("id", docId, "thumbnailUrl", thumbnailUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{docId}/workouts/{workoutId}")
    public ResponseEntity<?> addWorkoutToCollection(@PathVariable String docId, @PathVariable String workoutId) {
        try {
            collectionsService.addWorkoutToCollection(docId, workoutId);
            return ResponseEntity.ok(Map.of("status", "added", "workoutId", workoutId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{docId}/workouts/{workoutId}")
    public ResponseEntity<?> removeWorkoutFromCollection(@PathVariable String docId, @PathVariable String workoutId) {
        try {
            collectionsService.removeWorkoutFromCollection(docId, workoutId);
            return ResponseEntity.ok(Map.of("status", "removed", "workoutId", workoutId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{docId}")
    public ResponseEntity<?> deleteCollection(@PathVariable String docId) {
        try {
            Collections existing = collectionsService.getById(docId);
            if (existing == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Collection not found"));
            }
            collectionsService.deleteCollection(docId);
            return ResponseEntity.ok(Map.of("message", "Collection deleted successfully", "deletedId", docId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

}
