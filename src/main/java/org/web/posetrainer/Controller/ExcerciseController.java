package org.web.posetrainer.Controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.web.posetrainer.Entity.Excercise;
import org.web.posetrainer.Service.ExcerciseService;
import org.web.posetrainer.Firebase.FirebaseStorageService;

import java.io.IOException;
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

    /**
     * Tạo excercise mới + upload video
     * Request: multipart/form-data với:
     * - phần "data": JSON của Excercise (metadata)
     * - phần "video": file video
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createExcercise(
            @RequestPart("data") Excercise excercise,
            @RequestPart("video") MultipartFile videoFile
    ) throws IOException, ExecutionException, InterruptedException {
        System.out.println(">>> Enter createExcercise controller");

        // 1) Upload video lên Firebase Storage, lấy URL
        String videoUrl = storageService.uploadExerciseVideo(videoFile, excercise);

        // 2) Gán URL vào media.demoVideoUrl
        Excercise.Media media = excercise.getMedia();
        if (media == null) {
            media = new Excercise.Media();
        }
        media.setDemoVideoUrl(videoUrl);
        excercise.setMedia(media);

        // 3) Lưu Excercise vào Firestore
        String id = excerciseService.createExcercise(excercise);

        return ResponseEntity.ok(Map.of(
                "id", id,
                "videoUrl", videoUrl
        ));
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

}
