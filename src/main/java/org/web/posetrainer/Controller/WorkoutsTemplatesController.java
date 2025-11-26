package org.web.posetrainer.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.web.posetrainer.Entity.Excercise;
import org.web.posetrainer.Entity.WorkoutTemplate;
import org.web.posetrainer.Service.ExcerciseService;
import org.web.posetrainer.Firebase.FirebaseStorageService;
import org.web.posetrainer.Service.WorkoutsTemplatesService;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin/workouts")
@PreAuthorize("hasRole('ADMIN')")
public class WorkoutsTemplatesController {

    private final WorkoutsTemplatesService workoutsTemplatesService;
    private final FirebaseStorageService storageService;

    public WorkoutsTemplatesController(WorkoutsTemplatesService workoutsTemplatesService, FirebaseStorageService storageService) {
        this.workoutsTemplatesService = workoutsTemplatesService;
        this.storageService = storageService;
    }


    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createWorkout(@RequestBody WorkoutTemplate request, Principal principal) throws Exception {

        String adminId = principal.getName();
        request.setCreatedBy(adminId);
        String id = workoutsTemplatesService.createWorkout(request);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/{docId}")
    public ResponseEntity<?> getById(@PathVariable String docId) {
        try {
            WorkoutTemplate workout = workoutsTemplatesService.getWorkout(docId);
            if (workout == null) return ResponseEntity.notFound().build();

            return ResponseEntity.ok(workout);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{docId}")
    public ResponseEntity<?> updateWorkout(
            @PathVariable String docId,
            @RequestBody WorkoutTemplate request
    ) {
        try {
            WorkoutTemplate existing = workoutsTemplatesService.getWorkout(docId);
            if (existing == null) return ResponseEntity.notFound().build();

            existing.setTitle(request.getTitle());
            existing.setDescription(request.getDescription());
            existing.setLevel(request.getLevel());
            existing.setFocus(request.getFocus());
            existing.setGoalFit(request.getGoalFit());
            existing.setItems(request.getItems());
            existing.setEstDurationMin(request.getEstDurationMin());
            existing.setIsPublic(request.getIsPublic());

            existing.setUpdatedAt(System.currentTimeMillis());
            existing.setVersion(existing.getVersion() + 1);
            workoutsTemplatesService.updateWorkout(docId, existing);
            return ResponseEntity.ok(existing);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{docId}/items")
    public ResponseEntity<?> updateWorkoutItems(
            @PathVariable String docId,
            @RequestBody WorkoutTemplate request
    ) {
        try {
            WorkoutTemplate existing = workoutsTemplatesService.getWorkout(docId);
            if (existing == null) return ResponseEntity.notFound().build();

            existing.setItems(request.getItems());
            existing.setUpdatedAt(System.currentTimeMillis());
            existing.setVersion(existing.getVersion() + 1);

            workoutsTemplatesService.updateWorkout(docId, existing);

            return ResponseEntity.ok(existing);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/{docId}/add-item")
    public ResponseEntity<?> addWorkoutItem(
            @PathVariable String docId,
            @RequestBody WorkoutTemplate.WorkoutItem item
    ) {
        try {
            workoutsTemplatesService.addWorkoutItem(docId, item);
            return ResponseEntity.ok(Map.of("status", "added"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @DeleteMapping("/{docId}/delete-item")
    public ResponseEntity<?> deleteItem(
            @PathVariable String docId,
            @RequestParam String exerciseId
    ) {
        try {
            workoutsTemplatesService.removeWorkoutItem(docId, exerciseId);
            return ResponseEntity.ok(Map.of("status", "deleted"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{docId}")
    public ResponseEntity<?> deleteWorkout(@PathVariable String docId) {
        try {
            WorkoutTemplate existing = workoutsTemplatesService.getWorkout(docId);

            if (existing == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Workout not found"));
            }

            workoutsTemplatesService.deleteWorkout(docId);

            return ResponseEntity.ok(Map.of(
                    "message", "Workout deleted successfully",
                    "deletedId", docId
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<WorkoutTemplate> getAll() throws ExecutionException, InterruptedException {
        return WorkoutsTemplatesService.getAll();
    }

}
