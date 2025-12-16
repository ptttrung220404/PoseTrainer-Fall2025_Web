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
import org.springframework.security.core.GrantedAuthority;

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
    private final PageService pageService;
    private final AuthService authService;
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
                                     @RequestParam(defaultValue = "created_desc") String sort)
            throws ExecutionException, InterruptedException {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        authService.applyAuth(auth, model, displayName);
        List<Community> filteredPosts = pageService.filterAndSortPosts(communityService.getAll(), keyword, sort);
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




}
