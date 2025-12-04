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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserService userService;
    private final ExcerciseService excerciseService;
    private final CollectionsService collectionsService;
    private final WorkoutsTemplatesService workoutsService;

    public Map<String, Integer> getLoginStatsByDate() throws ExecutionException, InterruptedException {
        List<User> users = userService.getAll();
        Map<String, Integer> dateMap = new TreeMap<>();

        for (User user : users) {
            long ts = user.getLastLoginAt();
            if (ts == 0) continue;

            LocalDate date = Instant.ofEpochMilli(ts)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            String key = date.toString();
            dateMap.put(key, dateMap.getOrDefault(key, 0) + 1);
        }

        return dateMap;
    }

    // Số người đăng ký mới trong 14 ngày gần nhất
    public int getNewUsersCountLast14Days() throws ExecutionException, InterruptedException {
        List<User> users = userService.getAll();
        long fourteenDaysAgo = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000);
        int count = 0;
        
        for (User user : users) {
            if (user.getCreatedAt() >= fourteenDaysAgo) {
                count++;
            }
        }
        return count;
    }

    // Tổng số exercises
    public int getTotalExercises() throws ExecutionException, InterruptedException {
        return excerciseService.getAll().size();
    }

    // Tổng số collections
    public int getTotalCollections() throws ExecutionException, InterruptedException {
        return collectionsService.getAll().size();
    }

    // Tổng số workouts
    public int getTotalWorkouts() throws ExecutionException, InterruptedException {
        return workoutsService.getAll().size();
    }

    // Thống kê đăng nhập trong 30 ngày gần nhất (cho biểu đồ)
    public Map<String, Integer> getLoginStatsLast30Days() throws ExecutionException, InterruptedException {
        List<User> users = userService.getAll();
        Map<String, Integer> dateMap = new LinkedHashMap<>();
        
        // Khởi tạo map với 30 ngày gần nhất
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dateMap.put(date.toString(), 0);
        }

        // Đếm số lượt đăng nhập theo ngày
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        for (User user : users) {
            long ts = user.getLastLoginAt();
            if (ts == 0 || ts < thirtyDaysAgo) continue;

            LocalDate date = Instant.ofEpochMilli(ts)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            String key = date.toString();
            if (dateMap.containsKey(key)) {
                dateMap.put(key, dateMap.get(key) + 1);
            }
        }

        return dateMap;
    }
}
