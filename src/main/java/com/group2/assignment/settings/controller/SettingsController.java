package com.group2.assignment.settings.controller;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Old bookmarks redirect; personal API keys are no longer accepted. */
@Controller
@RequestMapping("/settings")
public class SettingsController {
    @GetMapping
    public String index(HttpSession session) {
        User user = (User) session.getAttribute(AuthController.SESSION_USER);
        return user != null && "ROLE_ADMIN".equals(user.getRole()) ? "redirect:/admin/settings" : "redirect:/courses";
    }

    @PostMapping({"/save", "/clear", "/test-ping", "/test-connection"})
    public void retired() {
        throw new ResponseStatusException(HttpStatus.GONE, "Khóa AI do quản trị viên cấu hình tập trung.");
    }
}
