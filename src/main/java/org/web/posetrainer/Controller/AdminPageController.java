package org.web.posetrainer.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.web.posetrainer.Service.CollectionsService;
import org.web.posetrainer.Service.CommunityService;
import org.web.posetrainer.Service.ExcerciseService;
import org.web.posetrainer.Service.UserService;
import org.web.posetrainer.Service.WorkoutsTemplatesService;

import java.util.concurrent.ExecutionException;
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
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model,
                            @ModelAttribute(value = "displayName") String displayName) {
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
    @GetMapping("/exercises")
    public String showExerciseList(Authentication auth, Model model,
                                   @ModelAttribute(value = "displayName") String displayName)
            throws ExecutionException, InterruptedException {

        if (auth != null) {
            model.addAttribute("uid", auth.getName());
            String name = (displayName != null && !displayName.isEmpty()) ? displayName : "Admin";
            model.addAttribute("displayName", name);
        }

        model.addAttribute("exercises", excerciseService.getAll());
        return "exercise-list";
    }
    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/workouts")
    public String showWorkoutList(Authentication auth, Model model,
                                  @ModelAttribute(value = "displayName") String displayName)
            throws ExecutionException, InterruptedException {
        if (auth != null) {
            model.addAttribute("uid", auth.getName());
            model.addAttribute("displayName", displayName != null && !displayName.isEmpty() ? displayName : "Admin");
        }
        model.addAttribute("workouts", WorkoutsTemplatesService.getAll());
        model.addAttribute("exercises", excerciseService.getAll());
        return "workout-list";
    }
    @GetMapping("/collections")
    public String showCollectionList(Authentication auth, Model model,
                                     @ModelAttribute(value = "displayName") String displayName)
            throws ExecutionException, InterruptedException {

        if (auth != null) {
            model.addAttribute("uid", auth.getName());
            model.addAttribute("displayName", displayName != null && !displayName.isEmpty() ? displayName : "Admin");
        }

        model.addAttribute("collections", collectionsService.getAll());
        model.addAttribute("workouts", WorkoutsTemplatesService.getAll());
        return "collection-list";
    }
    @GetMapping("/users")
    public String showUserList(Authentication auth, Model model,
                               @ModelAttribute(value = "displayName") String displayName)
            throws ExecutionException, InterruptedException {

        if (auth != null) {
            model.addAttribute("uid", auth.getName());
            model.addAttribute("displayName", displayName != null && !displayName.isEmpty() ? displayName : "Admin");
        }

        model.addAttribute("users", userService.getAll());
        return "user-list";
    }

    @GetMapping("/community")
    public String showCommunityPosts(Authentication auth, Model model,
                                     @ModelAttribute(value = "displayName") String displayName)
            throws ExecutionException, InterruptedException {

        if (auth != null) {
            model.addAttribute("uid", auth.getName());
            model.addAttribute("displayName", displayName != null && !displayName.isEmpty() ? displayName : "Admin");
        }

        model.addAttribute("posts", communityService.getAll());
        return "community-list";
    }
    @GetMapping("/profile")
    public String showProfile(Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String uid = auth.getName();
        userService.getUserByUid(uid).ifPresentOrElse(
                user -> model.addAttribute("user", user),
                () -> model.addAttribute("error", "Không tìm thấy thông tin người dùng")
        );

        return "user-profile";
    }

}
