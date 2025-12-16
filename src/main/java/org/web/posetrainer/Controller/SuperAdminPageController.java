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
@RequestMapping("/super_admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class SuperAdminPageController {
    private final ExcerciseService excerciseService;
    private final WorkoutsTemplatesService workoutsService;
    private final CollectionsService collectionsService;
    private final UserService userService;
    private final CommunityService communityService;
    private final DashboardService dashboardService;
    private final PageService pageService;
    private final AuthService authService;

    @GetMapping("/admins")
    public String showAdminList(Authentication auth, Model model,
                                @ModelAttribute(value = "displayName") String displayName,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "12") int size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "created_desc") String sort)
            throws ExecutionException, InterruptedException {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
//        if (auth != null) {
//            model.addAttribute("uid", auth.getName());
//            model.addAttribute("displayName", displayName != null && !displayName.isEmpty() ? displayName : "Admin");
//        }
        authService.applyAuth(auth, model, displayName);
        List<User> filteredUsers = pageService.filterAndSortUsers(userService.getAllAdmin(), keyword, sort);
        model.addAttribute("userPage", PagedResponse.of(filteredUsers, page, size));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        return "admin-list";
    }
}
