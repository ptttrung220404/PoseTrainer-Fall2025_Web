package org.web.posetrainer.Controller;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.web.posetrainer.DTO.LoginRequest;
import org.web.posetrainer.DTO.LoginResponse;
import org.web.posetrainer.Service.AuthService;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;

@Controller
public class LoginController {
    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }
    @GetMapping("/login")
    public String showLogin(@RequestParam(value = "error", required = false) String error,
                            Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "login"; // -> templates/login.html
    }
    @PostMapping("/login")
    public String doLogin(@ModelAttribute("loginRequest") @Valid LoginRequest request,
                          HttpServletResponse response,
                          RedirectAttributes redirectAttributes) {
        try {
            LoginResponse resp = authService.login(request);

            // Lưu idToken vào cookie (để filter có thể đọc)
            // Ở đây mình đặt tên cookie là ID_TOKEN
            String idToken = resp.getIdToken();
            long maxAge = 3600L; // mặc định 1h
            try {
                maxAge = Long.parseLong(resp.getExpiresIn());
            } catch (Exception ignore) {}

            String cookie = String.format("ID_TOKEN=%s; Path=/; Max-Age=%d; HttpOnly", idToken, maxAge);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie);

            // Gửi tên hiển thị sang dashboard (dùng FlashAttribute)
            redirectAttributes.addFlashAttribute("displayName", resp.getDisplayName());
            return "redirect:/admin/dashboard";

        } catch (Exception e) {
            // Login lỗi -> quay lại /login kèm thông báo
            redirectAttributes.addFlashAttribute("error", "Email hoặc mật khẩu không đúng");
            return "redirect:/login";
        }
    }

}
