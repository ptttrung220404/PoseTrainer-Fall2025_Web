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
@RequestMapping("/api/admin/workouts")
@PreAuthorize("hasRole('ADMIN')")
public class WorkoutsTemplatesController {
}
