package org.web.posetrainer.Controller;
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
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<?> createWorkout(@RequestBody WorkoutTemplate request) throws Exception {
        String id = workoutsTemplatesService.createWorkout(request);
        return ResponseEntity.ok(Map.of("id", id));
    }
    @PostMapping("/{id}/add-item")
    public String addWorkoutItem(
            @PathVariable String id,
            @ModelAttribute WorkoutTemplate.WorkoutItem item
    ) throws Exception {
        workoutsTemplatesService.addWorkoutItem(id, item);
        return "redirect:/admin/workouts/" + id;
    }

    @PostMapping("/{id}/delete-item")
    public String deleteItem(
            @PathVariable String id,
            @RequestParam String exerciseId
    ) throws Exception {
        workoutsTemplatesService.removeWorkoutItem(id, exerciseId);
        return "redirect:/admin/workouts/" + id;
    }
    @GetMapping
    public List<WorkoutTemplate> getAll() throws ExecutionException, InterruptedException {
        return WorkoutsTemplatesService.getAll();
    }

}
