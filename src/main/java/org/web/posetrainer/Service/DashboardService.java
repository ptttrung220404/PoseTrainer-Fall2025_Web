package org.web.posetrainer.Service;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Service;
import org.web.posetrainer.Entity.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
@Service
public class DashboardService {
    UserService userService;
    public Map<String, Integer> getLoginStatsByDate() throws ExecutionException, InterruptedException {

        List<User> users = userService.getAll();

        Map<String, Integer> dateMap = new TreeMap<>();

        for (User user : users) {
            long ts = user.getLastLoginAt();
            if (ts == 0) continue; // user chưa login lần nào

            // Chuyển timestamp về dạng yyyy-MM-dd
            LocalDate date = Instant.ofEpochMilli(ts)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String key = date.toString(); // yyyy-MM-dd

            dateMap.put(key, dateMap.getOrDefault(key, 0) + 1);
        }

        return dateMap;
    }

}
