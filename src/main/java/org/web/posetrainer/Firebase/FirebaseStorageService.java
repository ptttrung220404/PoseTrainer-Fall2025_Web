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


    public String uploadExerciseVideo(String exerciseId, MultipartFile video) throws IOException {
        if (video == null || video.isEmpty()) return null;

        Bucket bucket = StorageClient.getInstance().bucket(bucketName);
        String objectName = "exercises/" + exerciseId + "/demo.mp4";

        bucket.create(objectName, video.getBytes(), video.getContentType());
        return buildDownloadUrl(objectName);
    }

    public String uploadExerciseThumbnail(String exerciseId, MultipartFile thumbnail) throws IOException {
        if (thumbnail == null || thumbnail.isEmpty()) return null;

        Bucket bucket = StorageClient.getInstance().bucket(bucketName);
        String objectName = "exercises/" + exerciseId + "/thumbnail.jpg";

        bucket.create(objectName, thumbnail.getBytes(), thumbnail.getContentType());
        return buildDownloadUrl(objectName);
    }

    public String uploadCollectionThumbnail(String collectionId, MultipartFile thumbnail)
            throws IOException {

        if (thumbnail == null || thumbnail.isEmpty()) {
            return null;
        }

        Bucket bucket = StorageClient.getInstance().bucket(bucketName);
        String basePath = "collections/" + collectionId + "/";

        // Tên file cố định như Exercise
        String objectName = basePath + "thumbnail.jpg";

        // Upload lên Firebase Storage
        bucket.create(objectName, thumbnail.getBytes(), thumbnail.getContentType());

        // Trả về public media link
        return buildDownloadUrl(objectName);
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
