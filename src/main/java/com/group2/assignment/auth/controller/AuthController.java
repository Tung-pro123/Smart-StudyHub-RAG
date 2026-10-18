package com.group2.assignment.auth.controller;

import com.group2.assignment.auth.dto.LoginRequestDto;
import com.group2.assignment.auth.dto.RegisterRequestDto;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.service.AuthService;
import com.group2.assignment.auth.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
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
        return "auth/profile";
    }
}
