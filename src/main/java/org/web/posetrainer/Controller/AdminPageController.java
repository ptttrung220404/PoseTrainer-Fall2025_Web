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
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model,
                            @ModelAttribute(value = "displayName") String displayName) {
        if (auth != null) {
            model.addAttribute("uid", auth.getName());
            model.addAttribute("roles", auth.getAuthorities());
        }
        model.addAttribute("displayName", displayName);
        return "dashboard"; // -> templates/dashboard.html
    }
    @GetMapping("/exercises")
    public String showExerciseList(Model model) throws ExecutionException, InterruptedException {
        System.out.println(">>> Enter showExerciseList view");
        model.addAttribute("exercises", excerciseService.getAll());
        return "exercise-list"; // templates/exercise-list.html
    }
    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/workouts")
    public String showWorkoutList(Model model) throws ExecutionException, InterruptedException {
        model.addAttribute("workouts", WorkoutsTemplatesService.getAll());
        model.addAttribute("exercises", excerciseService.getAll());
        return "workout-list";
    }
    @GetMapping("/collections")
    public String showCollectionList(Model model) throws ExecutionException, InterruptedException {
        model.addAttribute("collections", collectionsService.getAll());
        model.addAttribute("workouts", WorkoutsTemplatesService.getAll());
        return "collection-list";
    }

    @GetMapping("/users")
    public String showUserList(Model model) throws ExecutionException, InterruptedException {
        model.addAttribute("users", userService.getAll());
        return "user-list";
    }
}
