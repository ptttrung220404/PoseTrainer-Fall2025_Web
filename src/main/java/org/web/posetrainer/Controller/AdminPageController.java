package org.web.posetrainer.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.web.posetrainer.Entity.Excercise;
import org.web.posetrainer.Service.ExcerciseService;
import org.web.posetrainer.Service.WorkoutsTemplatesService;

import java.util.concurrent.ExecutionException;
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPageController {
    private final ExcerciseService excerciseService;
    private final WorkoutsTemplatesService workoutsService;

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
    @GetMapping("/exercises/{id}")
    public String showExerciseDetail(@PathVariable String id, Model model)
            throws ExecutionException, InterruptedException {

        Excercise ex = excerciseService.getById(id);
        if (ex == null) {
            return "error/404";
        }

        model.addAttribute("exercise", ex);
        return "exercise-detail"; // templates/exercise-detail.html
    }

    // Màn form thêm exercise
    @GetMapping("/exercises/new")
    public String showExerciseForm() {
        System.out.println(">>> Enter showExerciseForm view");
        return "exercise-form"; // templates/exercise-form.html
    }
    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }
    @GetMapping("/workouts")
    public String showWorkoutList(Model model) throws ExecutionException, InterruptedException {
        model.addAttribute("workouts", WorkoutsTemplatesService.getAll());
        return "workout-list";
    }
}
