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
@RequestMapping("/api/admin/exercises")
@PreAuthorize("hasRole('ADMIN')")
public class ExcerciseController {

    private final ExcerciseService excerciseService;
    private final FirebaseStorageService storageService;

    public ExcerciseController(ExcerciseService excerciseService,
                               FirebaseStorageService storageService) {
        this.excerciseService = excerciseService;
        this.storageService = storageService;
    }
    @GetMapping
    public List<Excercise> getAll() throws ExecutionException, InterruptedException {
        return excerciseService.getAll();
    }
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createExcerciseJson(@RequestBody Excercise excercise)
            throws ExecutionException, InterruptedException {

        System.out.println(">>> Enter createExcerciseJson");

        String id = excerciseService.createExcercise(excercise);
        return ResponseEntity.ok(Map.of("id", id));
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) throws ExecutionException, InterruptedException {
        Excercise ex = excerciseService.getById(id);
        if (ex == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ex);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @RequestBody Excercise payload) throws ExecutionException, InterruptedException {
        // TODO: viết hàm update trong ExcerciseService
        excerciseService.updateExcercise(id, payload);
        return ResponseEntity.ok().build();
    }
    @PutMapping(value = "/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMedia(
            @PathVariable String id,
            @RequestPart(value = "video", required = false) MultipartFile videoFile,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile
    ) throws IOException, ExecutionException, InterruptedException {

        System.out.println(">>> Enter updateMedia for exercise id=" + id);

        // 1) Upload lên Storage
        Excercise.Media media = storageService.uploadExerciseMedia(id, videoFile, thumbnailFile);

        // 2) Update Firestore
        excerciseService.updateExcerciseMedia(id, media);

        return ResponseEntity.ok(Map.of(
                "id", id,
                "videoUrl", media.getDemoVideoUrl(),
                "thumbnailUrl", media.getThumbnailUrl()
        ));
    }

}
