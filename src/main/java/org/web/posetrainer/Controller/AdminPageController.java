package org.web.posetrainer.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.web.posetrainer.DTO.PagedResponse;
import org.web.posetrainer.Entity.Collections;
import org.web.posetrainer.Entity.Community;
import org.web.posetrainer.Entity.Excercise;
import org.web.posetrainer.Entity.User;
import org.web.posetrainer.Entity.WorkoutTemplate;
import org.web.posetrainer.Service.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.Comparator;
import java.util.stream.Collectors;
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPageController {
    private final ExcerciseService excerciseService;
    private final WorkoutsTemplatesService workoutsService;
    private final CollectionsService collectionsService;
    private final UserService userService;
    private final CommunityService communityService;
    private final DashboardService dashboardService;
    private final PageService pageService;
    private final AuthService authService;
    private final FeedbackService feedbackService;
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model,
                            @ModelAttribute(value = "displayName") String displayName) throws ExecutionException, InterruptedException {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        // Thống kê cơ bản
        List<User> users = userService.getAll();
        int totalUser = 0;
        for (User user : users) {
            if(user.getRoles().contains("user")) {
                totalUser++;
            }
        }

        model.addAttribute("totalUsers", totalUser);
        model.addAttribute("newUsersLast14Days", dashboardService.getNewUsersCountLast14Days());
        model.addAttribute("totalExercises", dashboardService.getTotalExercises());
        model.addAttribute("totalCollections", dashboardService.getTotalCollections());
        model.addAttribute("totalWorkouts", dashboardService.getTotalWorkouts());

        authService.applyAuth(auth, model, displayName);
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "Admin";
        }
        model.addAttribute("displayName", displayName);
        return "dashboard";
    }
    @GetMapping("/dashboard/login-stats")
    @ResponseBody
    public Map<String, Integer> loginStats() throws Exception {
        return dashboardService.getLoginStatsLast30Days();
    }

    @GetMapping("/exercises")
    public String showExerciseList(Authentication auth, Model model,
                                   @ModelAttribute(value = "displayName") String displayName,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "updated_desc") String sort)
            throws ExecutionException, InterruptedException {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        authService.applyAuth(auth, model, displayName);

        List<Excercise> filteredExercises = pageService.filterAndSortExercises(excerciseService.getAll(), keyword, sort);
        model.addAttribute("exercisePage", PagedResponse.of(filteredExercises, page, size));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        return "exercise-list";
    }
    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/workouts")
    public String showWorkoutList(Authentication auth, Model model,
                                  @ModelAttribute(value = "displayName") String displayName,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "updated_desc") String sort)
            throws ExecutionException, InterruptedException {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        authService.applyAuth(auth, model, displayName);
        List<WorkoutTemplate> filteredWorkouts = pageService.filterAndSortWorkouts(workoutsService.getAll(), keyword, sort);
        model.addAttribute("workoutPage", PagedResponse.of(filteredWorkouts, page, size));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("exercises", excerciseService.getAll());
        return "workout-list";
    }
    @GetMapping("/collections")
    public String showCollectionList(Authentication auth, Model model,
                                     @ModelAttribute(value = "displayName") String displayName,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(defaultValue = "updated_desc") String sort)
            throws ExecutionException, InterruptedException {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        authService.applyAuth(auth, model, displayName);

        List<Collections> filteredCollections = pageService.filterAndSortCollections(collectionsService.getAll(), keyword, sort);
        model.addAttribute("collectionsPage", PagedResponse.of(filteredCollections, page, size));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("workouts", workoutsService.getAll());
        return "collection-list";
    }
    @GetMapping("/users")
    public String showUserList(Authentication auth, Model model,
                               @ModelAttribute(value = "displayName") String displayName,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "12") int size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "created_desc") String sort)
            throws ExecutionException, InterruptedException {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        authService.applyAuth(auth, model, displayName);

        List<User> filteredUsers = pageService.filterAndSortUsers(userService.getAll(), keyword, sort);
        model.addAttribute("userPage", PagedResponse.of(filteredUsers, page, size));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        return "user-list";
    }

    @GetMapping("/community")
    public String showCommunityPosts(Authentication auth, Model model,
                                     @ModelAttribute(value = "displayName") String displayName,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "12") int size,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String authorUid,
                                     @RequestParam(defaultValue = "created_desc") String sort)
            throws ExecutionException, InterruptedException {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        authService.applyAuth(auth, model, displayName);

        // Build author filter options (users who have posted)
        List<Community> allPosts = communityService.getAll();
        HashSet<String> authorUids = new HashSet<>();
        for (Community p : allPosts) {
            String uid = null;
            if (p.getAuthor() != null && p.getAuthor().getUid() != null) {
                uid = p.getAuthor().getUid();
            } else if (p.getUid() != null) {
                uid = p.getUid();
            }
            if (uid != null && !uid.isBlank()) {
                authorUids.add(uid);
            }
        }
        List<Map<String, String>> postAuthors = new ArrayList<>();
        for (String uid : authorUids) {
            Map<String, String> a = new HashMap<>();
            a.put("uid", uid);
            userService.getUserByUid(uid).ifPresentOrElse(
                    u -> {
                        a.put("displayName", u.getDisplayName() != null ? u.getDisplayName() : "Người dùng");
                        a.put("email", u.getEmail());
                    },
                    () -> {
                        a.put("displayName", "Người dùng");
                        a.put("email", null);
                    }
            );
            postAuthors.add(a);
        }
        postAuthors.sort(Comparator.comparing(m -> String.valueOf(m.getOrDefault("displayName", "")), String.CASE_INSENSITIVE_ORDER));
        model.addAttribute("postAuthors", postAuthors);
        model.addAttribute("authorFilter", authorUid);

        // Apply author filter first, then keyword/sort
        List<Community> base = allPosts;
        if (authorUid != null && !authorUid.isBlank() && !"all".equalsIgnoreCase(authorUid)) {
            String needle = authorUid.trim();
            base = allPosts.stream().filter(p -> {
                String uid = (p.getAuthor() != null) ? p.getAuthor().getUid() : p.getUid();
                return uid != null && uid.equals(needle);
            }).collect(Collectors.toList());
        }

        List<Community> filteredPosts = pageService.filterAndSortPosts(base, keyword, sort);
        model.addAttribute("communityPage", PagedResponse.of(filteredPosts, page, size));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        return "community-list";
    }
    @GetMapping("/profile")
    public String showProfile(Authentication auth, Model model, @ModelAttribute(value = "displayName") String displayName) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        authService.applyAuth(auth, model, displayName);
        String uid = auth.getName();
        userService.getUserByUid(uid).ifPresentOrElse(
                user -> model.addAttribute("user", user),
                () -> model.addAttribute("error", "Không tìm thấy thông tin người dùng")
        );

        return "user-profile";
    }

    @GetMapping("/feedbacks")
    public String showFeedbackList(Authentication auth, Model model,
                                   @ModelAttribute(value = "displayName") String displayName,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String type,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String senderUid,
                                   @RequestParam(defaultValue = "created_desc") String sort)
            throws ExecutionException, InterruptedException {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        authService.applyAuth(auth, model, displayName);

        List<Map<String, Object>> allFeedbacks = feedbackService.getAllFeedbacksWithUserInfo();

        // Build "users who sent feedback" dropdown options from feedback list
        Map<String, Map<String, String>> senderMap = new HashMap<>();
        for (Map<String, Object> fb : allFeedbacks) {
            String uid = String.valueOf(fb.getOrDefault("uid", ""));
            if (uid == null || uid.isBlank()) continue;
            if (senderMap.containsKey(uid)) continue;

            Map<String, String> s = new HashMap<>();
            s.put("uid", uid);
            String dn = (String) fb.getOrDefault("userDisplayName", null);
            String em = (String) fb.getOrDefault("userEmail", null);
            s.put("displayName", dn != null && !dn.isBlank() ? dn : "Người dùng");
            s.put("email", em);
            senderMap.put(uid, s);
        }
        List<Map<String, String>> feedbackSenders = new ArrayList<>(senderMap.values());
        feedbackSenders.sort(Comparator.comparing(m -> String.valueOf(m.getOrDefault("displayName", "")), String.CASE_INSENSITIVE_ORDER));
        model.addAttribute("feedbackSenders", feedbackSenders);
        model.addAttribute("senderFilter", senderUid);

        // Simple filtering and sorting (can be enhanced later)
        List<Map<String, Object>> filteredFeedbacks = allFeedbacks;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            filteredFeedbacks = allFeedbacks.stream()
                    .filter(fb -> {
                        String id = String.valueOf(fb.getOrDefault("id", ""));
                        String uid = String.valueOf(fb.getOrDefault("uid", ""));
                        String postId = String.valueOf(fb.getOrDefault("postId", ""));
                        String exerciseId = String.valueOf(fb.getOrDefault("exerciseId", ""));
                        String content = (String) fb.getOrDefault("content", "");
                        String userEmail = (String) fb.getOrDefault("userEmail", "");
                        String userDisplayName = (String) fb.getOrDefault("userDisplayName", "");
                        String fbType = (String) fb.getOrDefault("type", "");
                        return id.toLowerCase().contains(lowerKeyword) ||
                                uid.toLowerCase().contains(lowerKeyword) ||
                                postId.toLowerCase().contains(lowerKeyword) ||
                                exerciseId.toLowerCase().contains(lowerKeyword) ||
                                content.toLowerCase().contains(lowerKeyword) ||
                                userEmail.toLowerCase().contains(lowerKeyword) ||
                                (userDisplayName != null && userDisplayName.toLowerCase().contains(lowerKeyword)) ||
                                fbType.toLowerCase().contains(lowerKeyword);
                    })
                    .collect(Collectors.toList());
        }

        // Filter by feedback type (exercise/app/post)
        if (type != null && !type.trim().isEmpty() && !"all".equalsIgnoreCase(type)) {
            String typeNeedle = type.trim().toLowerCase();
            filteredFeedbacks = filteredFeedbacks.stream()
                    .filter(fb -> {
                        String fbType = String.valueOf(fb.getOrDefault("type", ""));
                        return fbType != null && fbType.toLowerCase().equals(typeNeedle);
                    })
                    .collect(Collectors.toList());
        }

        // Filter by status (pending/accepted/rejected/resolved/...)
        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
            String statusNeedle = status.trim().toLowerCase();
            filteredFeedbacks = filteredFeedbacks.stream()
                    .filter(fb -> {
                        String fbStatus = String.valueOf(fb.getOrDefault("status", ""));
                        return fbStatus != null && fbStatus.toLowerCase().equals(statusNeedle);
                    })
                    .collect(Collectors.toList());
        }

        // Filter by feedback sender uid
        if (senderUid != null && !senderUid.trim().isEmpty() && !"all".equalsIgnoreCase(senderUid)) {
            String senderNeedle = senderUid.trim();
            filteredFeedbacks = filteredFeedbacks.stream()
                    .filter(fb -> {
                        String fbUid = String.valueOf(fb.getOrDefault("uid", ""));
                        return senderNeedle.equals(fbUid);
                    })
                    .collect(Collectors.toList());
        }

        // Sorting
        if ("created_desc".equals(sort)) {
            filteredFeedbacks.sort((a, b) -> {
                Long aTime = (Long) a.getOrDefault("createdAt", 0L);
                Long bTime = (Long) b.getOrDefault("createdAt", 0L);
                return Long.compare(bTime, aTime);
            });
        } else if ("created_asc".equals(sort)) {
            filteredFeedbacks.sort((a, b) -> {
                Long aTime = (Long) a.getOrDefault("createdAt", 0L);
                Long bTime = (Long) b.getOrDefault("createdAt", 0L);
                return Long.compare(aTime, bTime);
            });
        }

        model.addAttribute("feedbackPage", PagedResponse.of(filteredFeedbacks, page, size));
        model.addAttribute("keyword", keyword);
        model.addAttribute("typeFilter", type);
        model.addAttribute("statusFilter", status);
        model.addAttribute("sort", sort);
        return "feedback-list";
    }


}
