package org.web.posetrainer.Firebase;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.web.posetrainer.Entity.Excercise;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
@Service
public class FirebaseStorageService {
    private final Bucket bucket;

    public FirebaseStorageService(FirebaseApp app) {
        this.bucket = StorageClient.getInstance(app).bucket();
        System.out.println(">>> Firebase Storage bucket = " + bucket.getName());
    }

    public String uploadExerciseVideo(MultipartFile file, Excercise excercise) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File video rỗng");
        }

        String folder = "exercise_videos";
        String safeSlug = excercise.getSlug() != null && !excercise.getSlug().isBlank()
                ? excercise.getSlug()
                : UUID.randomUUID().toString();

        String objectName = folder + "/" + safeSlug + "_" + file.getOriginalFilename();

        // Tạo blob trên storage
        Blob blob = bucket.create(
                objectName,
                file.getBytes(),
                file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE
        );

        // Thêm token để tạo link download public
        String token = UUID.randomUUID().toString();
        blob.toBuilder()
                .setMetadata(Map.of("firebaseStorageDownloadTokens", token))
                .build()
                .update();

        String encodedName = URLEncoder.encode(objectName, StandardCharsets.UTF_8);
        String downloadUrl = String.format(
                "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media&token=%s",
                bucket.getName(),
                encodedName,
                token
        );

        return downloadUrl;
    }
}
