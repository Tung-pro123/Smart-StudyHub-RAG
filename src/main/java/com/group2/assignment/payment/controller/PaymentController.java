package com.group2.assignment.payment.controller;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.payment.entity.PaymentTransaction;
import com.group2.assignment.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Khởi tạo giao dịch thanh toán nâng cấp gói qua VNPay Sandbox
     */
    @PostMapping("/create-vnpay")
    public String createVnPayPayment(
            @RequestParam(name = "tier", defaultValue = "PRO") String tier,
            @RequestParam(name = "bankCode", required = false) String bankCode,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập trước khi thực hiện thanh toán nâng cấp gói cước.");
            return "redirect:/login";
        }

        try {
            String paymentUrl = paymentService.createVnPayPaymentUrl(currentUser, tier, bankCode, request);
            return "redirect:" + paymentUrl;
        } catch (Exception e) {
            log.error("Lỗi khởi tạo cổng VNPay: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể kết nối đến cổng thanh toán VNPay: " + e.getMessage());
            return "redirect:/courses";
        }
    }

    /**
     * Nhận kết quả điều hướng (Return Callback) từ VNPay sau khi thanh toán
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(
            @RequestParam Map<String, String> allParams,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            PaymentTransaction tx = paymentService.processVnPayReturn(allParams, session);

            if ("SUCCESS".equalsIgnoreCase(tx.getStatus())) {
                String tierName = "SQUAD".equalsIgnoreCase(tx.getTargetTier()) ? "Study Squad (Nhóm)" : "Pro (Cá nhân)";
                redirectAttributes.addFlashAttribute("successMessage",
                        "🎉 Thanh toán VNPay thành công! Bạn đã nâng cấp lên gói " + tierName +
                        ". Mã đơn: " + tx.getTxnRef() + " (Mã GD VNPay: " + tx.getVnpayTransactionNo() + ")");

                if ("SQUAD".equalsIgnoreCase(tx.getTargetTier())) {
                    return "redirect:/squad";
                }
                return "redirect:/courses";
            } else {
                String reason = "Mã lỗi phản hồi từ cổng: " + tx.getResponseCode();
                if ("24".equals(tx.getResponseCode())) {
                    reason = "Giao dịch đã bị khách hàng hủy bỏ.";
                } else if ("11".equals(tx.getResponseCode())) {
                    reason = "Đã hết hạn chờ thanh toán.";
                }
                redirectAttributes.addFlashAttribute("errorMessage", "Thanh toán VNPay không thành công! " + reason);
                return "redirect:/courses";
            }
        } catch (IllegalArgumentException e) {
            log.error("Lỗi xác thực phản hồi VNPay: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Cảnh báo bảo mật: " + e.getMessage());
            return "redirect:/courses";
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi xử lý kết quả VNPay: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật kết quả giao dịch.");
            return "redirect:/courses";
        }
    }
}

