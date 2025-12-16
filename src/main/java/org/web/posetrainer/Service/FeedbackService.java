package org.web.posetrainer.Service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;
import org.web.posetrainer.Entity.Excercise;
import org.web.posetrainer.Entity.Feedbacks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class FeedbackService {
    private static final String COLLECTION_NAME = "feedbacks";
    private final UserService userService;
    private final Firestore firestore;

    public FeedbackService(UserService userService, Firestore firestore) {
        this.userService = userService;
        this.firestore = firestore;
    }
    public List<Feedbacks> getAllFeedbacks() throws ExecutionException, InterruptedException {
        var querySnap = firestore.collection(COLLECTION_NAME).get().get();
        List<Feedbacks> result = new ArrayList<>();
        for (DocumentSnapshot doc : querySnap.getDocuments()) {
            Feedbacks fb = doc.toObject(Feedbacks.class);
            if (fb != null) {
                fb.setId(doc.getId());
                result.add(fb);
            }
        }
        return result;
    }
    public Feedbacks getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot snap = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get();
        if (!snap.exists()) return null;
        Feedbacks fb = snap.toObject(Feedbacks.class);
        if (fb != null) fb.setId(snap.getId());
        return fb;
    }

}
