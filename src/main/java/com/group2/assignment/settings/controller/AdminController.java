package com.group2.assignment.settings.controller;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.payment.repository.PaymentTransactionRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import com.group2.assignment.payment.entity.PaymentTransaction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private static final int PAGE_SIZE = 15;
    private final UserRepository users;
    private final AdminAnalyticsService analytics;
    private final SubscriptionQuotaService quota;
    private final SystemAiKeyService systemKey;
    private final PaymentTransactionRepository paymentTransactionRepository;
    @org.springframework.beans.factory.annotation.Value("${settings.gemini.model:${studyhub.ai.model:gemini-3.5-flash-lite}}")
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
        Long totalRevenue = paymentTransactionRepository.getTotalRevenue();
        Long totalPaidOrders = paymentTransactionRepository.countByStatus("SUCCESS");
        Long proRevenue = paymentTransactionRepository.getRevenueByTier("PRO");
        Long squadRevenue = paymentTransactionRepository.getRevenueByTier("SQUAD");
        LocalDateTime startOfDay = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).atStartOfDay();
        Long todayRevenue = paymentTransactionRepository.getRevenueSince(startOfDay);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0L);
        model.addAttribute("totalPaidOrders", totalPaidOrders != null ? totalPaidOrders : 0L);
        model.addAttribute("proRevenue", proRevenue != null ? proRevenue : 0L);
        model.addAttribute("squadRevenue", squadRevenue != null ? squadRevenue : 0L);
        model.addAttribute("todayRevenue", todayRevenue != null ? todayRevenue : 0L);
        model.addAttribute("recentTransactions", paymentTransactionRepository.findTop20ByOrderByCreatedAtDesc());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(@RequestParam(defaultValue = "") String keyword,
                        @RequestParam(defaultValue = "") String tier,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        validateUserFilter(page, tier);
        var studentPage = users.searchStudents(keyword.trim(), tier, PageRequest.of(page, PAGE_SIZE));
        if (page > 0 && page >= studentPage.getTotalPages()) {
            page = Math.max(0, studentPage.getTotalPages() - 1);
            studentPage = users.searchStudents(keyword.trim(), tier, PageRequest.of(page, PAGE_SIZE));
        }
        model.addAttribute("keyword", keyword.trim());
        model.addAttribute("tier", tier);
        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("studentPage", studentPage);
        model.addAttribute("firstPage", Math.max(0, page - 2));
        model.addAttribute("lastPage", Math.min(studentPage.getTotalPages() - 1, page + 2));
        model.addAttribute("today", java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));
        return "admin/users";
    }

    @PostMapping("/users/{id}/upgrade-pro")
    public String upgrade(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                          @RequestParam(defaultValue = "") String tier, @RequestParam(defaultValue = "0") int page,
                          RedirectAttributes redirect) {
        validateUserFilter(page, tier);
        quota.changeTier(id, "PRO");
        redirect.addFlashAttribute("successMessage", "Đã nâng cấp PRO cho sinh viên.");
        return redirectToUsers(keyword, tier, page, redirect);
    }

    @PostMapping("/users/{id}/upgrade-squad")
    public String upgradeSquad(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                               @RequestParam(defaultValue = "") String tier, @RequestParam(defaultValue = "0") int page,
                               RedirectAttributes redirect) {
        validateUserFilter(page, tier);
        quota.changeTier(id, "SQUAD");
        redirect.addFlashAttribute("successMessage", "Đã kích hoạt SQUAD cho sinh viên.");
        return redirectToUsers(keyword, tier, page, redirect);
    }

    @PostMapping("/users/{id}/reset-rag")
    public String resetRag(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "") String tier, @RequestParam(defaultValue = "0") int page,
                           RedirectAttributes redirect) {
        validateUserFilter(page, tier);
        quota.resetRagQuota(id);
        redirect.addFlashAttribute("successMessage", "Đã reset hạn ngạch RAG hôm nay cho sinh viên.");
        return redirectToUsers(keyword, tier, page, redirect);
    }

    @PostMapping("/users/{id}/downgrade-free")
    public String downgrade(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
                            @RequestParam(defaultValue = "") String tier, @RequestParam(defaultValue = "0") int page,
                            RedirectAttributes redirect) {
        validateUserFilter(page, tier);
        quota.changeTier(id, "FREE");
        redirect.addFlashAttribute("successMessage", "Đã chuyển sinh viên về gói FREE.");
        return redirectToUsers(keyword, tier, page, redirect);
    }

    private void validateUserFilter(int page, String tier) {
        if (page < 0 || page > Integer.MAX_VALUE / PAGE_SIZE || !java.util.List.of("", "FREE", "PRO", "SQUAD").contains(tier)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trang hoặc gói cước không hợp lệ.");
        }
    }

    private String redirectToUsers(String keyword, String tier, int page, RedirectAttributes redirect) {
        if (!keyword.isBlank()) redirect.addAttribute("keyword", keyword.trim());
        if (!tier.isEmpty()) redirect.addAttribute("tier", tier);
        if (page > 0) redirect.addAttribute("page", page);
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

    /**
     * Xuất toàn bộ lịch sử giao dịch VNPay ra file CSV để đối soát tài chính
     */
    @GetMapping(value = "/revenue/export-csv")
    @ResponseBody
    public ResponseEntity<String> exportRevenueCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append('\ufeff'); // UTF-8 BOM cho Excel tiếng Việt
        csv.append("Mã Đơn Hàng (TxnRef),Sinh Viên,Gói Cước,Số Tiền (VNĐ),Ngân Hàng,Mã GD VNPay,Trạng Thái,Thời Gian\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        for (PaymentTransaction tx : paymentTransactionRepository.findAllByOrderByCreatedAtDesc()) {
            csv.append('"').append(tx.getTxnRef() != null ? tx.getTxnRef() : "").append("\",")
               .append('"').append(tx.getUsername() != null ? tx.getUsername() : "").append("\",")
               .append('"').append(tx.getTargetTier() != null ? tx.getTargetTier() : "").append("\",")
               .append(tx.getAmount() != null ? tx.getAmount() : 0).append(',')
               .append('"').append(tx.getBankCode() != null ? tx.getBankCode() : "VNPay").append("\",")
               .append('"').append(tx.getVnpayTransactionNo() != null ? tx.getVnpayTransactionNo() : "").append("\",")
               .append('"').append(tx.getStatus() != null ? tx.getStatus() : "").append("\",")
               .append('"').append(tx.getCreatedAt() != null ? tx.getCreatedAt().format(dtf) : "").append("\"\n");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"vnpay-transactions-report.csv\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(csv.toString());
    }
}
