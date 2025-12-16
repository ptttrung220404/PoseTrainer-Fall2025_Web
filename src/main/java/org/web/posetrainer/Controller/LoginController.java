package org.web.posetrainer.Controller;
import com.google.firebase.auth.SessionCookieOptions;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.web.posetrainer.DTO.LoginRequest;
import org.web.posetrainer.DTO.LoginResponse;
import org.web.posetrainer.Service.AuthService;
import org.springframework.security.core.Authentication;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Controller
public class LoginController {
    private final AuthService authService;
    private final FirebaseAuth firebaseAuth;
    public LoginController(AuthService authService, FirebaseAuth firebaseAuth) {
        this.authService = authService;
        this.firebaseAuth = firebaseAuth;
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
                          RedirectAttributes redirect) {
        try {
            // Step 1: Sign in with email/password
            LoginResponse loginResp = authService.login(request);
            List<String> roles = loginResp.getRoles();
            boolean isAdmin = false;
            boolean isSuperAdmin = false;
            if (roles != null && !roles.isEmpty()) {
                isSuperAdmin = roles.stream().anyMatch(r -> r.equalsIgnoreCase("SUPER_ADMIN"));
                isAdmin = isSuperAdmin || roles.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"));
            }
            if (!isAdmin) {
                redirect.addFlashAttribute("error", "Tài khoản không có quyền truy cập trang quản trị!");
                return "redirect:/login";
            }

            String idToken = loginResp.getIdToken();
            long expiresIn = Long.parseLong(loginResp.getExpiresIn()); // 3600s
            SessionCookieOptions options = SessionCookieOptions.builder()
                    .setExpiresIn(expiresIn * 1000)  // ms
                    .build();

            String sessionCookie = firebaseAuth.createSessionCookie(idToken, options);

            ResponseCookie cookie = ResponseCookie.from("SESSION", sessionCookie)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(expiresIn)
                    .build();


            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            redirect.addFlashAttribute("displayName", loginResp.getDisplayName());

            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            // Login lỗi -> quay lại /login kèm thông báo
            redirect.addFlashAttribute("error", "Email hoặc mật khẩu không đúng");
            return "redirect:/login";
        }
    }
    @GetMapping("/forgot")
    public String showForgot(@RequestParam(value = "error", required = false) String error,
                            Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "forgot-password"; // -> templates/login.html
    }
    @PostMapping("/forgot")
    public String forgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // 1️⃣ Kiểm tra email có tồn tại trong Firebase Auth không
            try {
                firebaseAuth.getUserByEmail(email); // nếu không tồn tại sẽ throw FirebaseAuthException
            } catch (com.google.firebase.auth.FirebaseAuthException ex) {
                redirectAttributes.addFlashAttribute("error",
                        "Email này chưa được đăng ký trong hệ thống.");
                return "redirect:/forgot";
            }

            // 2️⃣ Gửi email khôi phục mật khẩu
            authService.sendPasswordResetEmail(email);
            redirectAttributes.addFlashAttribute("success",
                    "Email khôi phục mật khẩu đã được gửi!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Có lỗi xảy ra, vui lòng thử lại.");
        }

        return "redirect:/forgot";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response, RedirectAttributes redirectAttributes) {

        try {
            // Lấy UID từ Authentication
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String uid = principal.toString();

            // Revoke session (logout toàn bộ thiết bị)
            firebaseAuth.revokeRefreshTokens(uid);

        } catch (Exception ignored) {}

        // Xóa cookie SESSION
        ResponseCookie delete = ResponseCookie.from("SESSION", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, delete.toString());

        // Clear Spring Security Context
        SecurityContextHolder.clearContext();

        return "redirect:/login?logout";
    }
//        // Xóa cookie ID_TOKEN
//        String cookie = "ID_TOKEN=; Path=/; Max-Age=0; HttpOnly";
//        response.addHeader(HttpHeaders.SET_COOKIE, cookie);
//
//        redirectAttributes.addFlashAttribute("message", "Đăng xuất thành công");
//        return "redirect:/login";

}

