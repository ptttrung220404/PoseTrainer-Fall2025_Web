package org.web.posetrainer.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.web.posetrainer.DTO.PagedResponse;
import org.web.posetrainer.Entity.WorkoutTemplate;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class WorkoutsTemplatesService {
    long now = Instant.now().getEpochSecond();
    private static final String COLLECTION_NAME = "workouts_templates";

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    // --------------------------
    // Tạo mã workout tùy chỉnh
    // --------------------------
    private String generateWorkoutId() {
        byte[] bytes = new byte[5];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder("wt_");
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // --------------------------
    // Tạo Workout mới (theo docId)
    // --------------------------
    public String createWorkout(WorkoutTemplate workout) throws Exception {
        String docId = generateWorkoutId();
        DocumentReference docRef = db().collection(COLLECTION_NAME).document(docId);

        workout.setId(docId);                    // set ID = docId
        workout.setVersion(1);

        workout.setUpdatedAt(now) ;

        if (workout.getItems() == null) {
            workout.setItems(new ArrayList<>());
        }

        docRef.set(workout).get();
        return docId;
    }

    // --------------------------
    // Lấy chi tiết Workout
    // --------------------------
    public WorkoutTemplate getWorkout(String docId) throws Exception {
        DocumentSnapshot snap = db().collection(COLLECTION_NAME)
                .document(docId).get().get();

        return snap.exists() ? snap.toObject(WorkoutTemplate.class) : null;
    }

    // --------------------------
    // Cập nhật Workout (docId)
    // Controller đã tăng version → service KHÔNG tăng nữa
    // --------------------------
    public void updateWorkout(String docId, WorkoutTemplate workout) throws Exception {
        workout.setId(docId);  // đảm bảo object vẫn có id
        workout.setUpdatedAt(now);
        db().collection(COLLECTION_NAME)
                .document(docId)     // CHỈ DÙNG docId từ controller
                .set(workout, SetOptions.merge())
                .get();
    }

    // --------------------------
    // Xóa Workout
    // --------------------------
    public void deleteWorkout(String docId) throws Exception {
        db().collection(COLLECTION_NAME)
                .document(docId)
                .delete()
                .get();
    }

    // --------------------------
    // Thêm 1 item
    // --------------------------
    public void addWorkoutItem(String docId, WorkoutTemplate.WorkoutItem item) throws Exception {

        WorkoutTemplate workout = getWorkout(docId);
        if (workout == null) return;

        if (workout.getItems() == null) {
            workout.setItems(new ArrayList<>());
        }

        // Set order theo index
        item.setOrder(workout.getItems().size() + 1);

        workout.getItems().add(item);
        workout.setUpdatedAt(now);
        workout.setVersion(workout.getVersion() + 1);

        db().collection(COLLECTION_NAME)
                .document(docId)
                .set(workout)
                .get();
    }

    // --------------------------
    // Xóa 1 item theo exerciseId
    // --------------------------
    public void removeWorkoutItem(String docId, String exerciseId) throws Exception {

        WorkoutTemplate workout = getWorkout(docId);
        if (workout == null) return;

        List<WorkoutTemplate.WorkoutItem> newItems =
                workout.getItems()
                        .stream()
                        .filter(i -> !i.getExerciseId().equals(exerciseId))
                        .collect(Collectors.toList());

        // Re-order lại
        for (int i = 0; i < newItems.size(); i++) {
            newItems.get(i).setOrder(i + 1);
        }

        workout.setItems(newItems);
        workout.setUpdatedAt(now);
        workout.setVersion(workout.getVersion() + 1);

        db().collection(COLLECTION_NAME)
                .document(docId)
                .set(workout)
                .get();
    }

    // --------------------------
    // Lấy tất cả workout
    // --------------------------
    public List<WorkoutTemplate> getAll() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<WorkoutTemplate> list = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            WorkoutTemplate w = doc.toObject(WorkoutTemplate.class);
            if (w != null) { w.setId(doc.getId()); // gán docId vào field id
                list.add(w); }
        }
        return list;
    }

    public PagedResponse<WorkoutTemplate> getPaged(int page, int size) throws ExecutionException, InterruptedException {
        return PagedResponse.of(getAll(), page, size);
    }
    public void updateWorkoutThumbnail(String id, String thumbnailUrl)
            throws ExecutionException, InterruptedException {

        Map<String, Object> updates = new HashMap<>();
        updates.put("thumbnailUrl", thumbnailUrl);
        updates.put("updatedAt", now);

        db().collection(COLLECTION_NAME)
                .document(id)
                .set(updates, SetOptions.merge())
                .get();
    }

}
