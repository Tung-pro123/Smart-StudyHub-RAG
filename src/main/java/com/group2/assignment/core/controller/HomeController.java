package com.group2.assignment.core.controller;

import com.group2.assignment.auth.controller.AuthController;
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
        if (session.getAttribute(AuthController.SESSION_USER) != null) {
            return "redirect:/courses";
        }
        return "landing";
    }

    /**
     * Alias tương thích ngược cho Dashboard môn học
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/courses";
    }
}
