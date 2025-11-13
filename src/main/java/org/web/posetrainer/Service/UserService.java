package org.web.posetrainer.Service;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.web.posetrainer.Entity.User;
import org.web.posetrainer.Entity.Workouts;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    private static final String COLLECTION_NAME = "users";
    private final Firestore firestore;

    public UserService(Firestore firestore) {
        this.firestore = firestore;
    }
    public Optional<User> getUserByUid(String uid) {
        try {
            ApiFuture<DocumentSnapshot> future = firestore
                    .collection(COLLECTION_NAME)
                    .document(uid)
                    .get();

            DocumentSnapshot document = future.get();
            if (document.exists()) {
                User user = document.toObject(User.class);
                if (user != null) {
                    user.setUid(document.getId()); // gán UID (vì Firestore không tự fill)
                }
                return Optional.ofNullable(user);
            } else {
                System.out.println(">>> Firestore: users/" + uid + " not found");
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println(">>> Firestore getUserByUid failed: " + e.getMessage());
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
    public boolean saveUser(User user) {
        try {
            firestore.collection(COLLECTION_NAME)
                    .document(user.getUid())
                    .set(user)
                    .get();
            System.out.println(">>> Saved user: " + user.getUid());
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println(">>> Firestore saveUser failed: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
