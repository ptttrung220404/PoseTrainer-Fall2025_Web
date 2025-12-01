package org.web.posetrainer.Service;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.web.posetrainer.Entity.Excercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
@Service
public class ExcerciseService {
    private static final String COLLECTION_NAME = "exercises";
    private final Firestore firestore;

    public ExcerciseService(Firestore firestore) {
        this.firestore = firestore;
    }

    public String createExcercise(Excercise excercise) throws ExecutionException, InterruptedException {
        excercise.setUpdatedAt(System.currentTimeMillis());
        // tạo id mới nếu chưa có
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        excercise.setId(docRef.getId());

        ApiFuture<?> write = docRef.set(excercise);

        write.get(); // đợi ghi xong
        return docRef.getId();
    }

    public Excercise getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot snap = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get();
        if (!snap.exists()) return null;
        Excercise ex = snap.toObject(Excercise.class);
        if (ex != null) ex.setId(snap.getId());
        return ex;
    }

    public List<Excercise> getAll() throws ExecutionException, InterruptedException {
        var querySnap = firestore.collection(COLLECTION_NAME).get().get();
        List<Excercise> result = new ArrayList<>();
        for (DocumentSnapshot doc : querySnap.getDocuments()) {
            Excercise ex = doc.toObject(Excercise.class);
            if (ex != null) {
                ex.setId(doc.getId());
                result.add(ex);
            }
        }
        return result;
    }
    public void updateExcercise(String id, Excercise excercise)
            throws ExecutionException, InterruptedException {

        Map<String, Object> updates = new HashMap<>();

        // chỉ update các field mà modal cho phép chỉnh sửa
        if (excercise.getName() != null) updates.put("name", excercise.getName());
        if (excercise.getSlug() != null) updates.put("slug", excercise.getSlug());
        if (excercise.getLevel() != null) updates.put("level", excercise.getLevel());
        if (excercise.getMuscles() != null) updates.put("muscles", excercise.getMuscles());
        if (excercise.getTags() != null) updates.put("tags", excercise.getTags());
        updates.put("isPublic", excercise.getIsPublic());

        // update time
        updates.put("updatedAt", System.currentTimeMillis());

        firestore.collection(COLLECTION_NAME)
                .document(id)
                .set(updates, SetOptions.merge())
                .get();
    }
    public void updateExcerciseMediaPartial(String id, Map<String, Object> mediaUpdates)
            throws ExecutionException, InterruptedException {

        Map<String, Object> updates = new HashMap<>();

        // update từng field trong media.*
        for (var entry : mediaUpdates.entrySet()) {
            updates.put("media." + entry.getKey(), entry.getValue());
        }

        updates.put("updatedAt", System.currentTimeMillis());

        firestore.collection("exercises")
                .document(id)
                .update(updates)
                .get();
    }

}
