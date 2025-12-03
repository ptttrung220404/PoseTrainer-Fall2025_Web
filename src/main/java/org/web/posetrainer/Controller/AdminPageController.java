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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.Comparator;
import java.util.Locale;
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
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model,
                            @ModelAttribute(value = "displayName") String displayName) throws ExecutionException, InterruptedException {
        List<User> users = userService.getAll();
        int totalUser = 0;
        for (User user : users) {
            if(user.getRoles().contains("user")) {
                totalUser++;
            }
        }

        model.addAttribute("totalUsers", totalUser);


        if (auth != null) {
            model.addAttribute("uid", auth.getName());
            model.addAttribute("roles", auth.getAuthorities());
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "Admin";
        }
        model.addAttribute("displayName", displayName);
        return "dashboard"; // -> templates/dashboard.html
    }
    @GetMapping("/dashboard/login-stats")
    @ResponseBody
    public Map<String, Integer> loginStats() throws Exception {
        return dashboardService.getLoginStatsByDate();
    }

    @GetMapping("/exercises")
    public String showExerciseList(Authentication auth, Model model,
                                   @ModelAttribute(value = "displayName") String displayName,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "updated_desc") String sort)
            throws ExecutionException, InterruptedException {

        if (auth != null) {
            model.addAttribute("uid", auth.getName());
            String name = (displayName != null && !displayName.isEmpty()) ? displayName : "Admin";
            model.addAttribute("displayName", name);
        }

        List<Excercise> filteredExercises = filterAndSortExercises(excerciseService.getAll(), keyword, sort);
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
        if (auth != null) {
            model.addAttribute("uid", auth.getName());
            model.addAttribute("displayName", displayName != null && !displayName.isEmpty() ? displayName : "Admin");
        }
        List<WorkoutTemplate> filteredWorkouts = filterAndSortWorkouts(workoutsService.getAll(), keyword, sort);
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

        if (auth != null) {
            model.addAttribute("uid", auth.getName());
            model.addAttribute("displayName", displayName != null && !displayName.isEmpty() ? displayName : "Admin");
        }

        List<Collections> filteredCollections = filterAndSortCollections(collectionsService.getAll(), keyword, sort);
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

        if (auth != null) {
            model.addAttribute("uid", auth.getName());
            model.addAttribute("displayName", displayName != null && !displayName.isEmpty() ? displayName : "Admin");
        }

        List<User> filteredUsers = filterAndSortUsers(userService.getAll(), keyword, sort);
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
                                     @RequestParam(defaultValue = "created_desc") String sort)
            throws ExecutionException, InterruptedException {

        if (auth != null) {
            model.addAttribute("uid", auth.getName());
            model.addAttribute("displayName", displayName != null && !displayName.isEmpty() ? displayName : "Admin");
        }

        List<Community> filteredPosts = filterAndSortPosts(communityService.getAll(), keyword, sort);
        model.addAttribute("communityPage", PagedResponse.of(filteredPosts, page, size));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        return "community-list";
    }

    @GetMapping("/profile")
    public String showProfile(Authentication auth, Model model,
                              @ModelAttribute(value = "displayName") String displayName) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String uid = auth.getName();
        model.addAttribute("uid", uid);

        userService.getUserByUid(uid).ifPresentOrElse(
                user -> {
                    model.addAttribute("user", user);
                    String userDisplayName = user.getDisplayName();
                    if (userDisplayName != null && !userDisplayName.trim().isEmpty()) {
                        model.addAttribute("displayName", userDisplayName.trim());
                    } else if (displayName != null && !displayName.trim().isEmpty()) {
                        model.addAttribute("displayName", displayName);
                    } else {
                        model.addAttribute("displayName", "Admin");
                    }
                },
                () -> {
                    model.addAttribute("error", "Không tìm thấy thông tin người dùng");
                    model.addAttribute("displayName", displayName != null && !displayName.isEmpty() ? displayName : "Admin");
                }
        );

        return "user-profile";
    }

}
    private List<Excercise> filterAndSortExercises(List<Excercise> source, String keyword, String sort) {
        SortOption option = resolveSort(sort, "updated", "desc");
        Comparator<Excercise> comparator = switch (option.field()) {
            case "name" -> Comparator.comparing(ex -> safeString(ex.getName()), String.CASE_INSENSITIVE_ORDER);
            case "level" -> Comparator.comparing(ex -> safeString(ex.getLevel()), String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparingLong(Excercise::getUpdatedAt);
        };
        if (option.isDesc()) {
            comparator = comparator.reversed();
        }
        return source.stream()
                .filter(ex -> matchesKeyword(keyword, ex.getName(), ex.getSlug()))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<WorkoutTemplate> filterAndSortWorkouts(List<WorkoutTemplate> source, String keyword, String sort) {
        SortOption option = resolveSort(sort, "updated", "desc");
        Comparator<WorkoutTemplate> comparator = switch (option.field()) {
            case "title" -> Comparator.comparing(w -> safeString(w.getTitle()), String.CASE_INSENSITIVE_ORDER);
            case "level" -> Comparator.comparing(w -> safeString(w.getLevel()), String.CASE_INSENSITIVE_ORDER);
            case "duration" -> Comparator.comparingInt(WorkoutTemplate::getEstDurationMin);
            default -> Comparator.comparingLong(WorkoutTemplate::getUpdatedAt);
        };
        if (option.isDesc()) {
            comparator = comparator.reversed();
        }
        return source.stream()
                .filter(w -> matchesKeyword(keyword, w.getTitle(), w.getDescription(), w.getId()))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<Collections> filterAndSortCollections(List<Collections> source, String keyword, String sort) {
        SortOption option = resolveSort(sort, "updated", "desc");
        Comparator<Collections> comparator = switch (option.field()) {
            case "title" -> Comparator.comparing(c -> safeString(c.getTitle()), String.CASE_INSENSITIVE_ORDER);
            case "category" -> Comparator.comparing(c -> safeString(c.getCategory()), String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparingLong(Collections::getUpdatedAt);
        };
        if (option.isDesc()) {
            comparator = comparator.reversed();
        }
        return source.stream()
                .filter(c -> matchesKeyword(keyword, c.getTitle(), c.getCategory()))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<User> filterAndSortUsers(List<User> source, String keyword, String sort) {
        SortOption option = resolveSort(sort, "created", "desc");
        Comparator<User> comparator = switch (option.field()) {
            case "name" -> Comparator.comparing(u -> safeString(u.getDisplayName()), String.CASE_INSENSITIVE_ORDER);
            case "email" -> Comparator.comparing(u -> safeString(u.getEmail()), String.CASE_INSENSITIVE_ORDER);
            case "lastLogin" -> Comparator.comparingLong(User::getLastLoginAt);
            default -> Comparator.comparingLong(User::getCreatedAt);
        };
        if (option.isDesc()) {
            comparator = comparator.reversed();
        }
        return source.stream()
                .filter(u -> matchesKeyword(keyword, u.getDisplayName(), u.getEmail()))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<Community> filterAndSortPosts(List<Community> source, String keyword, String sort) {
        SortOption option = resolveSort(sort, "created", "desc");
        Comparator<Community> comparator = switch (option.field()) {
            case "author" -> Comparator.comparing(Community::getDisplayName, String.CASE_INSENSITIVE_ORDER);
            case "likes" -> Comparator.comparingLong(Community::getLikesCount);
            case "comments" -> Comparator.comparingLong(Community::getCommentsCount);
            default -> Comparator.comparingLong(c -> timestampToMillis(c.getCreatedAt()));
        };
        if (option.isDesc()) {
            comparator = comparator.reversed();
        }
        return source.stream()
                .filter(c -> matchesKeyword(keyword, c.getDisplayName(), c.getContent()))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private boolean matchesKeyword(String keyword, String... candidates) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String needle = keyword.toLowerCase(Locale.ROOT);
        for (String candidate : candidates) {
            if (candidate != null && candidate.toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private long timestampToMillis(com.google.cloud.Timestamp ts) {
        return ts == null ? 0L : ts.toDate().getTime();
    }

    private SortOption resolveSort(String sort, String defaultField, String defaultDir) {
        if (sort == null || sort.isBlank()) {
            return new SortOption(defaultField, defaultDir);
        }
        String[] parts = sort.split("_");
        if (parts.length != 2) {
            return new SortOption(defaultField, defaultDir);
        }
        String direction = "asc".equalsIgnoreCase(parts[1]) ? "asc" : "desc";
        return new SortOption(parts[0], direction);
    }

    private record SortOption(String field, String direction) {
        boolean isDesc() {
            return "desc".equalsIgnoreCase(direction);
        }
    }
}
