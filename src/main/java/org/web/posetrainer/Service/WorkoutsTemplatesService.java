package org.web.posetrainer.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.web.posetrainer.Entity.WorkoutTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
@Service
public class WorkoutsTemplatesService {
    private static final String COLLECTION_NAME = "workouts_templates";
    private final Firestore firestore;

    public WorkoutsTemplatesService(Firestore firestore) {
        this.firestore = firestore;
    }

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }
    public String createWorkout(WorkoutTemplate workout) throws Exception {

        DocumentReference docRef = db().collection(COLLECTION_NAME).document();

        workout.setId(docRef.getId());
        workout.setVersion(1);
        workout.setUpdatedAt(System.currentTimeMillis());

        if (workout.getItems() == null) {
            workout.setItems(new ArrayList<>());
        }

        docRef.set(workout).get();
        return workout.getId();
    }

    // --------------------------
    // Lấy chi tiết
    // --------------------------
    public WorkoutTemplate getWorkout(String id) throws Exception {
        DocumentSnapshot snap = db().collection(COLLECTION_NAME)
                .document(id).get().get();

        return snap.exists() ? snap.toObject(WorkoutTemplate.class) : null;
    }


    // --------------------------
    // Cập nhật Workout
    // --------------------------
    public void updateWorkout(WorkoutTemplate workout) throws Exception {
        workout.setUpdatedAt(System.currentTimeMillis());
        workout.setVersion(workout.getVersion() + 1);

        db().collection(COLLECTION_NAME)
                .document(workout.getId())
                .set(workout)
                .get();
    }

    // --------------------------
    // Xóa Workout
    // --------------------------
    public void deleteWorkout(String id) throws Exception {
        db().collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .get();
    }


    // --------------------------
    // Thêm 1 exercise ID
    // --------------------------
    public void addWorkoutItem(String workoutId, WorkoutTemplate.WorkoutItem item) throws Exception {
        DocumentReference doc = db().collection(COLLECTION_NAME).document(workoutId);

        // Tải dữ liệu cũ
        WorkoutTemplate workout = getWorkout(workoutId);
        if (workout == null) return;

        if (workout.getItems() == null) {
            workout.setItems(new ArrayList<>());
        }

        // Set order theo size hiện tại
        item.setOrder(workout.getItems().size() + 1);

        workout.getItems().add(item);
        workout.setUpdatedAt(System.currentTimeMillis());
        workout.setVersion(workout.getVersion() + 1);

        doc.set(workout).get();
    }
    // --------------------------
    // Xóa 1 exercise ID
    // --------------------------
    public void removeWorkoutItem(String workoutId, String exerciseId) throws Exception {
        WorkoutTemplate workout = getWorkout(workoutId);
        if (workout == null) return;

        List<WorkoutTemplate.WorkoutItem> newItems =
                workout.getItems().stream()
                        .filter(i -> !i.getExerciseId().equals(exerciseId))
                        .collect(Collectors.toList());

        // Cập nhật lại order
        for (int i = 0; i < newItems.size(); i++) {
            newItems.get(i).setOrder(i + 1);
        }

        workout.setItems(newItems);
        workout.setUpdatedAt(System.currentTimeMillis());
        workout.setVersion(workout.getVersion() + 1);

        db().collection(COLLECTION_NAME).document(workoutId).set(workout).get();
    }
    public static List<WorkoutTemplate> getAll() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<WorkoutTemplate> list = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            WorkoutTemplate w = doc.toObject(WorkoutTemplate.class);
            list.add(w);
        }
        return list;
    }

}
