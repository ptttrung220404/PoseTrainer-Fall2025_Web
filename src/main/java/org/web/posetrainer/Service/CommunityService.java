package org.web.posetrainer.Service;

import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;

@Service
public class CommunityService {
    private static final String COLLECTION_NAME = "community";
    private final Firestore firestore;

    public CommunityService(Firestore firestore) {
        this.firestore = firestore;
    }

}
