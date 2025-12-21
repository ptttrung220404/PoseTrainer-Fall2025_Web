package org.web.posetrainer.Service;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.web.posetrainer.DTO.PagedResponse;
import org.web.posetrainer.Entity.Collections;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class CollectionsService {
    private static final String COLLECTION_NAME = "collections";

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    private String generateCollectionId() {
        byte[] bytes = new byte[4]; // 4 bytes = 8 hex chars
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder("col_");
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    public String createCollection(Collections col) throws Exception {
        String docId = generateCollectionId();
        DocumentReference docRef = db().collection(COLLECTION_NAME).document(docId);
        long now = Instant.now().getEpochSecond();
        col.setId(docId);
        col.setCreatedAt(now);
        col.setUpdatedAt(now);

        if (col.getWorkoutTemplateIds() == null) {
            col.setWorkoutTemplateIds(new ArrayList<>());
        }
        if (col.getTags() == null) {
            col.setTags(new ArrayList<>());
        }

        docRef.set(col).get();
        return docId;
    }
    public List<Collections> getAll() throws ExecutionException, InterruptedException {
        Firestore firestore = db();
        List<Collections> list = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            Collections c = doc.toObject(Collections.class);
            if (c != null) {
                c.setId(doc.getId()); // gán docId vào field id
                list.add(c);
            }
        }
        return list;
    }

    public PagedResponse<Collections> getPaged(int page, int size) throws ExecutionException, InterruptedException {
        return PagedResponse.of(getAll(), page, size);
    }
    public Collections getById(String docId) throws Exception {
        DocumentSnapshot snap = db()
                .collection(COLLECTION_NAME)
                .document(docId)
                .get()
                .get();

        return snap.exists() ? snap.toObject(Collections.class) : null;
    }
    public void updateCollection(String docId, Collections col) throws Exception {
        col.setId(docId);
        long now = Instant.now().getEpochSecond();
        col.setUpdatedAt(now);


        db().collection(COLLECTION_NAME)
                .document(docId)
                .set(col, SetOptions.merge())
                .get();
    }
    public void deleteCollection(String docId) throws Exception {
        db().collection(COLLECTION_NAME)
                .document(docId)
                .delete()
                .get();
    }
    public void addWorkoutToCollection(String colId, String workoutId) throws Exception {
        Collections col = getById(colId);
        if (col == null) return;

        if (col.getWorkoutTemplateIds() == null) {
            col.setWorkoutTemplateIds(new ArrayList<>());
        }

        if (!col.getWorkoutTemplateIds().contains(workoutId)) {
            col.getWorkoutTemplateIds().add(workoutId);
        }
        long now = Instant.now().getEpochSecond();
        col.setUpdatedAt(now);


        db().collection(COLLECTION_NAME)
                .document(colId)
                .set(col)
                .get();
    }
    public void removeWorkoutFromCollection(String colId, String workoutId) throws Exception {
        Collections col = getById(colId);
        if (col == null) return;

        if (col.getWorkoutTemplateIds() != null) {
            col.getWorkoutTemplateIds().remove(workoutId);
        }
        long now = Instant.now().getEpochSecond();
        col.setUpdatedAt(now);


        db().collection(COLLECTION_NAME)
                .document(colId)
                .set(col)
                .get();
    }
    public void updateCollectionThumbnail(String id, String thumbnailUrl)
            throws ExecutionException, InterruptedException {

        Map<String, Object> updates = new HashMap<>();
        updates.put("thumbnailUrl", thumbnailUrl);
        long now = Instant.now().getEpochSecond();

        updates.put("updatedAt", now);

        db().collection(COLLECTION_NAME)
                .document(id)
                .set(updates, SetOptions.merge())
                .get();
    }

}
