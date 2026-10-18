package com.group2.assignment.auth.controller;

import com.group2.assignment.auth.dto.ChangePasswordDto;
import com.group2.assignment.auth.dto.LoginRequestDto;
import com.group2.assignment.auth.dto.OAuth2UserInfo;
import com.group2.assignment.auth.dto.RegisterRequestDto;
import com.group2.assignment.auth.dto.UpdateProfileDto;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.service.AuthService;
import com.group2.assignment.auth.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public static final String AUTH_COOKIE_NAME = "STUDYHUB_JWT";
    public static final String SESSION_USER = "currentUser";

    @GetMapping("/login")
    public String showLoginPage(Model model, HttpSession session) {
        if (session.getAttribute(SESSION_USER) != null) {
            return "redirect:/";
        }
        model.addAttribute("loginDto", new LoginRequestDto());
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(
            @ModelAttribute("loginDto") LoginRequestDto dto,
            HttpServletResponse response,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            String token = authService.login(dto);

            // Lưu JWT vào Cookie trình duyệt (1 ngày)
            Cookie cookie = new Cookie(AUTH_COOKIE_NAME, token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            // Lưu thông tin người dùng vào Session
            User user = authService.findByUsername(dto.getUsername()).orElse(null);
            session.setAttribute(SESSION_USER, user);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Xin chào mừng " + (user != null ? user.getFullName() : dto.getUsername()) + " đã đăng nhập thành công!");

            String redirectUrl = (String) session.getAttribute("authRedirectUrl");
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                session.removeAttribute("authRedirectUrl");
                return "redirect:" + redirectUrl;
            }
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model, HttpSession session) {
        if (session.getAttribute(SESSION_USER) != null) {
            return "redirect:/";
        }
        model.addAttribute("registerDto", new RegisterRequestDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(
            @ModelAttribute("registerDto") RegisterRequestDto dto,
            RedirectAttributes redirectAttributes) {
        try {
            authService.register(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký tài khoản thành công! Vui lòng đăng nhập với thông tin vừa tạo.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) {
        // Xóa Cookie JWT
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        // Hủy Session
        session.removeAttribute(SESSION_USER);
        session.invalidate();

        redirectAttributes.addFlashAttribute("successMessage", "Bạn đã đăng xuất an toàn khỏi hệ thống!");
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        if (!model.containsAttribute("updateProfileDto")) {
            model.addAttribute("updateProfileDto", UpdateProfileDto.builder()
                    .fullName(user.getFullName())
                    .avatar(user.getAvatar())
                    .build());
        }
        if (!model.containsAttribute("changePasswordDto")) {
            model.addAttribute("changePasswordDto", new ChangePasswordDto());
        }
        return "auth/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute("updateProfileDto") UpdateProfileDto dto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(SESSION_USER);
        if (currentUser == null) {
            return "redirect:/login";
        }
        try {
            User updatedUser = authService.updateProfile(currentUser.getUsername(), dto);
            session.setAttribute(SESSION_USER, updatedUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin hồ sơ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @ModelAttribute("changePasswordDto") ChangePasswordDto dto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(SESSION_USER);
        if (currentUser == null) {
            return "redirect:/login";
        }
        try {
            authService.changePassword(currentUser.getUsername(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công! Mật khẩu mới đã được cập nhật.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    /**
     * OAuth2 Login Success Handler
     * Xử lý khi đăng nhập Google thành công
     */
    @GetMapping("/oauth2/success")
    public String handleOAuth2Success(
            @AuthenticationPrincipal OAuth2User oauth2User,
            HttpServletResponse response,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            if (oauth2User == null) {
                return "redirect:/login?error=oauth2_failed";
            }

            // Trích xuất thông tin từ Google
            OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                    .googleId(oauth2User.getName())
                    .email(oauth2User.getAttribute("email"))
                    .fullName(oauth2User.getAttribute("name"))
                    .avatarUrl(oauth2User.getAttribute("picture"))
                    .provider("google")
                    .build();

            // Xử lý đăng nhập OAuth2
            User user = authService.processOAuth2Login(userInfo);

            // Tạo JWT token
            String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getFullName());

            // Lưu JWT vào Cookie
            Cookie cookie = new Cookie(AUTH_COOKIE_NAME, token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            // Lưu user vào session
            session.setAttribute(SESSION_USER, user);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng nhập Google thành công! Chào mừng " + user.getFullName());

            return "redirect:/";
        } catch (Exception e) {
            return "redirect:/login?error=oauth2_failed";
        }
    }
}
