package com.group2.assignment.settings.controller;

import com.group2.assignment.settings.SettingsConstants;
import com.group2.assignment.settings.dto.ApiKeyDto;
import com.group2.assignment.settings.dto.PingResponseDto;
import com.group2.assignment.settings.service.ApiKeyService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {
    private final ApiKeyService apiKeyService;

    @ModelAttribute
    public void preventCaching(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
    }

    @GetMapping
    public String index(Model model, HttpSession session) {
        if (session.getAttribute(SettingsConstants.SESSION_CSRF_TOKEN) == null) {
            session.setAttribute(SettingsConstants.SESSION_CSRF_TOKEN, UUID.randomUUID().toString());
        }
        model.addAttribute("csrfToken", session.getAttribute(SettingsConstants.SESSION_CSRF_TOKEN));
        model.addAttribute("provider", apiKeyService.getProvider(session));
        model.addAttribute("hasApiKey", apiKeyService.getApiKey(session) != null);
        return "settings/index";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ApiKeyDto dto, @RequestParam(required = false) String csrfToken,
                       HttpSession session, RedirectAttributes redirect) {
        checkCsrf(session, csrfToken);
        try {
            apiKeyService.save(dto, session);
            redirect.addFlashAttribute("successMessage", "Đã lưu API key trong phiên hiện tại. Bạn có thể kiểm tra kết nối.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping({"/test-ping", "/test-connection"})
    @ResponseBody
    public PingResponseDto testPing(@ModelAttribute ApiKeyDto dto, @RequestParam(required = false) String csrfToken,
                                   HttpSession session) {
        checkCsrf(session, csrfToken);
        return apiKeyService.testConnection(dto, session);
    }

    @PostMapping("/clear")
    public String clear(@RequestParam(required = false) String csrfToken, HttpSession session,
                        RedirectAttributes redirect) {
        checkCsrf(session, csrfToken);
        apiKeyService.clear(session);
        redirect.addFlashAttribute("successMessage", "Đã xóa API key khỏi phiên hiện tại.");
        return "redirect:/settings";
    }

    private void checkCsrf(HttpSession session, String submitted) {
        Object expected = session.getAttribute(SettingsConstants.SESSION_CSRF_TOKEN);
        if (!(expected instanceof String token) || submitted == null
                || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), submitted.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Phiên không hợp lệ. Vui lòng tải lại trang cài đặt.");
        }
    }
}
