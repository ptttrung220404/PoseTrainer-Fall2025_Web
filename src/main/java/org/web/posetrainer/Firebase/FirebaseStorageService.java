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
    private final String bucketName;

    public FirebaseStorageService(com.google.firebase.FirebaseApp app) {
        this.bucketName = app.getOptions().getStorageBucket();
    }


    public Excercise.Media uploadExerciseMedia(String exerciseId,
                                               MultipartFile video,
                                               MultipartFile thumbnail) throws IOException {

        Bucket bucket = StorageClient.getInstance().bucket(bucketName);
        String basePath = "exercises/" + exerciseId + "/";

        String videoUrl = null;
        String thumbUrl = null;

        if (video != null && !video.isEmpty()) {
            String objectName = basePath + "demo.mp4"; // hoặc lấy từ video.getOriginalFilename()
            Blob blob = bucket.create(objectName, video.getBytes(), video.getContentType());
            videoUrl = buildDownloadUrl(objectName);
        }

        if (thumbnail != null && !thumbnail.isEmpty()) {
            String objectName = basePath + "thumbnail.jpg"; // hoặc dùng tên file gốc
            Blob blob = bucket.create(objectName, thumbnail.getBytes(), thumbnail.getContentType());
            thumbUrl = buildDownloadUrl(objectName);
        }

        Excercise.Media media = new Excercise.Media();
        media.setDemoVideoUrl(videoUrl);
        media.setThumbnailUrl(thumbUrl);
        return media;
    }

    private String buildDownloadUrl(String objectName) {
        // dạng: https://firebasestorage.googleapis.com/v0/b/<bucket>/o/<path>?alt=media
        return "https://firebasestorage.googleapis.com/v0/b/"
                + bucketName
                + "/o/"
                + URLEncoder.encode(objectName, StandardCharsets.UTF_8)
                + "?alt=media";
    }
}
