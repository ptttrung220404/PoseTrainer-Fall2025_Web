package org.web.posetrainer.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.web.posetrainer.Entity.Workouts;

import java.util.concurrent.ExecutionException;

public class WorkoutsTemplatesService {
    private static final String COLLECTION_NAME = "workouts_templates";

    public String saveWorkouts(Workouts workouts) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(workouts.getTitle()).set(workouts);
        return collectionApiFuture.get().getUpdateTime().toString();
    }

    public Workouts getWorkoutsDetails(String title) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference docRef = dbFirestore.collection(COLLECTION_NAME).document(title);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        Workouts workouts = null;
        if (document.exists()) {
            workouts = document.toObject(Workouts.class);
            return workouts;
        }else{
            return null;
        }

    }
    public String updateWorkouts(Workouts workouts) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(workouts.getTitle()).set(workouts);
        return collectionApiFuture.get().getUpdateTime().toString();
    }
    public String deleteWorkouts(String title) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(title).delete();
        return title +"is deleted";
    }
}
