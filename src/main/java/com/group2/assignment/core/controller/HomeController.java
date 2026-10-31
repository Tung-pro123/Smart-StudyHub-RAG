package com.group2.assignment.core.controller;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    /**
     * Cổng chào tiếp thị: Khách vãng lai xem Landing Page, người dùng đã đăng nhập vào thẳng /courses
     */
    @GetMapping("/")
    public String index(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        if (currentUser != null) {
            if ("ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/courses";
        }
        return "landing";
    }

    /**
     * Alias tương thích ngược cho Dashboard môn học
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        if (currentUser != null && "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/courses";
    }
}
