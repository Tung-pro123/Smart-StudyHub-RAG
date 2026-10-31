package com.group2.assignment.settings.controller;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.settings.SettingsConstants;
import com.group2.assignment.settings.dto.PingResponseDto;
import com.group2.assignment.settings.service.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository users;
    private final AdminAnalyticsService analytics;
    private final SubscriptionQuotaService quota;
    private final SystemAiKeyService systemKey;
    @org.springframework.beans.factory.annotation.Value("${settings.gemini.model:${studyhub.ai.model:gemini-2.5-flash}}")
    private String geminiModel;

    @ModelAttribute
    public void authorize(HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) {
        User current = (User) session.getAttribute(AuthController.SESSION_USER);
        User persisted = current == null || current.getId() == null ? null : users.findById(current.getId()).orElse(null);
        if (persisted == null || !"ROLE_ADMIN".equals(persisted.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ quản trị viên được truy cập.");
        }
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        String token = (String) session.getAttribute(SettingsConstants.SESSION_CSRF_TOKEN);
        if ("POST".equals(request.getMethod())) {
            String submitted = request.getParameter("csrfToken");
            if (token == null || submitted == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),
                    submitted.getBytes(StandardCharsets.UTF_8))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vui lòng tải lại trang rồi thử lại.");
            }
        } else if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(SettingsConstants.SESSION_CSRF_TOKEN, token);
        }
        model.addAttribute("csrfToken", token);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.web.servlet.ModelAndView adminError(ResponseStatusException exception) {
        var view = new org.springframework.web.servlet.ModelAndView("admin/error");
        view.setStatus(exception.getStatusCode());
        view.addObject("statusCode", exception.getStatusCode().value());
        view.addObject("errorMessage", exception.getReason());
        return view;
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("stats", analytics.getStats());
        model.addAttribute("recentUsers", analytics.getRecentUsers());
        model.addAttribute("recentCourses", analytics.getRecentCourses());
        model.addAttribute("ping", systemKey.getLastPing());
        model.addAttribute("geminiModel", geminiModel);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(@RequestParam(defaultValue = "") String keyword,
                        @RequestParam(defaultValue = "") String tier, Model model) {
        if (!java.util.List.of("", "FREE", "PRO", "SQUAD").contains(tier)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gói cước không hợp lệ.");
        }
        model.addAttribute("keyword", keyword.trim());
        model.addAttribute("tier", tier);
        model.addAttribute("students", users.searchStudents(keyword.trim(), tier));
        model.addAttribute("today", java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));
        return "admin/users";
    }

    @PostMapping("/users/{id}/upgrade-pro")
    public String upgrade(@PathVariable Long id, RedirectAttributes redirect) {
        quota.changeTier(id, "PRO");
        redirect.addFlashAttribute("successMessage", "Đã nâng cấp PRO cho sinh viên.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/upgrade-squad")
    public String upgradeSquad(@PathVariable Long id, RedirectAttributes redirect) {
        quota.changeTier(id, "SQUAD");
        redirect.addFlashAttribute("successMessage", "Đã kích hoạt SQUAD cho sinh viên.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reset-rag")
    public String resetRag(@PathVariable Long id, RedirectAttributes redirect) {
        quota.resetRagQuota(id);
        redirect.addFlashAttribute("successMessage", "Đã reset hạn ngạch RAG hôm nay cho sinh viên.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/downgrade-free")
    public String downgrade(@PathVariable Long id, RedirectAttributes redirect) {
        quota.changeTier(id, "FREE");
        redirect.addFlashAttribute("successMessage", "Đã chuyển sinh viên về gói FREE.");
        return "redirect:/admin/users";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("configured", systemKey.isConfigured());
        model.addAttribute("maskedKey", systemKey.getMaskedKey());
        model.addAttribute("ping", systemKey.getLastPing());
        return "admin/settings";
    }

    @PostMapping("/settings/save")
    public String save(@RequestParam String apiKey, RedirectAttributes redirect) {
        try {
            systemKey.update(apiKey);
            redirect.addFlashAttribute("successMessage", "Đã cập nhật System Key trong bộ nhớ máy chủ.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/settings/test-ping")
    public String ping(RedirectAttributes redirect) {
        PingResponseDto ping = systemKey.testPing();
        redirect.addFlashAttribute(ping.success() ? "successMessage" : "errorMessage", ping.message());
        return "redirect:/admin/settings";
    }
}
