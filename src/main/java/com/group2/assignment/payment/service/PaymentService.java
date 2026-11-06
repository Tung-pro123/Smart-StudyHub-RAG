package com.group2.assignment.payment.service;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.auth.service.AuthService;
import com.group2.assignment.payment.config.VnPayConfig;
import com.group2.assignment.payment.entity.PaymentTransaction;
import com.group2.assignment.payment.repository.PaymentTransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final VnPayConfig vnPayConfig;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    public static final long PRO_PRICE_VND = 49000L;
    public static final long SQUAD_PRICE_VND = 129000L;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Tạo URL điều hướng sang cổng VNPay Sandbox
     */
    @Transactional
    public String createVnPayPaymentUrl(User user, String targetTier, String bankCode, HttpServletRequest request) {
        String cleanTier = targetTier != null ? targetTier.trim().toUpperCase() : "PRO";
        long amount = "SQUAD".equals(cleanTier) ? SQUAD_PRICE_VND : PRO_PRICE_VND;

        String txnRef = "ORDER_" + cleanTier + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);
        String createDate = now.format(DATE_FORMATTER);
        String expireDate = now.plusMinutes(15).format(DATE_FORMATTER);

        String ipAddr = getClientIp(request);
        String returnUrl = resolveReturnUrl(request);

        String username = user != null ? user.getUsername() : "anonymous";
        String orderInfo = "Nang cap goi " + cleanTier + " cho tai khoan " + username;

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", VnPayConfig.VNP_VERSION);
        vnpParams.put("vnp_Command", VnPayConfig.VNP_COMMAND);
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay tính bằng xu (nhân 100)
        vnpParams.put("vnp_CurrCode", VnPayConfig.VNP_CURRENCY);
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", VnPayConfig.VNP_LOCALE);
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", ipAddr);
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        if (bankCode != null && !bankCode.isBlank()) {
            vnpParams.put("vnp_BankCode", bankCode.trim().toUpperCase());
        }

        // Lưu đơn hàng trạng thái PENDING vào CSDL
        PaymentTransaction tx = PaymentTransaction.builder()
                .txnRef(txnRef)
                .user(user)
                .username(username)
                .targetTier(cleanTier)
                .amount(amount)
                .bankCode(bankCode != null ? bankCode.trim().toUpperCase() : null)
                .orderInfo(orderInfo)
                .status("PENDING")
                .createdAt(now)
                .build();
        paymentTransactionRepository.save(tx);

        log.info("Khởi tạo giao dịch VNPay [{}]: user={}, tier={}, amount={} VND", txnRef, username, cleanTier, amount);

        return vnPayConfig.buildPaymentUrl(vnpParams);
    }

    /**
     * Xử lý kết quả callback trả về từ VNPay
     */
    @Transactional
    public PaymentTransaction processVnPayReturn(Map<String, String> returnParams, HttpSession session) {
        String vnpSecureHash = returnParams.get("vnp_SecureHash");
        String txnRef = returnParams.get("vnp_TxnRef");
        String responseCode = returnParams.get("vnp_ResponseCode");
        String transactionNo = returnParams.get("vnp_TransactionNo");
        String bankCode = returnParams.get("vnp_BankCode");

        log.info("Tiếp nhận VNPay Return: txnRef={}, responseCode={}, transactionNo={}", txnRef, responseCode, transactionNo);

        // 1. Kiểm tra chữ ký bảo mật
        boolean isValidSignature = vnPayConfig.verifyReturnHash(returnParams, vnpSecureHash);
        if (!isValidSignature) {
            log.error("Chữ ký VNPay không hợp lệ hoặc bị giả mạo! TxnRef: {}", txnRef);
            if (txnRef != null) {
                paymentTransactionRepository.findByTxnRef(txnRef).ifPresent(tx -> {
                    tx.setStatus("FAILED");
                    tx.setResponseCode("INVALID_SIGNATURE");
                    paymentTransactionRepository.save(tx);
                });
            }
            throw new IllegalArgumentException("Chữ ký số VNPay không hợp lệ, phát hiện can thiệp dữ liệu!");
        }

        // 2. Tìm hoặc khởi tạo bản ghi giao dịch
        PaymentTransaction tx = null;
        if (txnRef != null) {
            tx = paymentTransactionRepository.findByTxnRef(txnRef).orElse(null);
        }

        if (tx == null) {
            String targetTier = "PRO";
            if (txnRef != null && txnRef.contains("SQUAD")) {
                targetTier = "SQUAD";
            }
            long amount = "SQUAD".equals(targetTier) ? SQUAD_PRICE_VND : PRO_PRICE_VND;
            tx = PaymentTransaction.builder()
                    .txnRef(txnRef != null ? txnRef : "VNPAY_" + System.currentTimeMillis())
                    .targetTier(targetTier)
                    .amount(amount)
                    .username("unknown")
                    .status("PENDING")
                    .build();
        }

        tx.setVnpayTransactionNo(transactionNo);
        tx.setResponseCode(responseCode);
        if (bankCode != null && !bankCode.isBlank()) {
            tx.setBankCode(bankCode);
        }

        // 3. Kiểm tra mã phản hồi (00 là Thành Công)
        if ("00".equals(responseCode)) {
            tx.setStatus("SUCCESS");
            paymentTransactionRepository.save(tx);

            // Nâng cấp gói cước cho User
            String username = tx.getUsername();
            if (username == null || "anonymous".equalsIgnoreCase(username) || "unknown".equalsIgnoreCase(username)) {
                if (session != null && session.getAttribute(AuthController.SESSION_USER) != null) {
                    User sessionUser = (User) session.getAttribute(AuthController.SESSION_USER);
                    username = sessionUser.getUsername();
                }
            }

            if (username != null && !"anonymous".equalsIgnoreCase(username) && !"unknown".equalsIgnoreCase(username)) {
                User updatedUser = authService.upgradeSubscriptionTier(username, tx.getTargetTier());
                // Gia hạn hoặc cộng dồn 30 ngày sử dụng (Subscription Lifecycle)
                LocalDateTime base = (updatedUser.getTierExpiresAt() != null && updatedUser.getTierExpiresAt().isAfter(LocalDateTime.now()))
                        ? updatedUser.getTierExpiresAt()
                        : LocalDateTime.now();
                updatedUser.setTierExpiresAt(base.plusDays(30));
                updatedUser = userRepository.save(updatedUser);

                tx.setUser(updatedUser);
                tx.setUsername(updatedUser.getUsername());
                paymentTransactionRepository.save(tx);

                // Cập nhật lại session ngay lập tức
                if (session != null) {
                    session.setAttribute(AuthController.SESSION_USER, updatedUser);
                }
                log.info("Nâng cấp thành công gói [{}] cho sinh viên [{}] qua VNPay (Hạn dùng: {})", 
                        tx.getTargetTier(), username, updatedUser.getTierExpiresAt());
            }
        } else {
            tx.setStatus("FAILED");
            paymentTransactionRepository.save(tx);
            log.warn("Giao dịch VNPay thất bại hoặc bị hủy: txnRef={}, code={}", txnRef, responseCode);
        }

        return tx;
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip != null ? ip : "127.0.0.1";
    }

    private String resolveReturnUrl(HttpServletRequest request) {
        String configured = vnPayConfig.getReturnUrl();
        if (request == null) {
            return configured;
        }
        // Tự động thích ứng theo Scheme, Host, Port thực tế của phiên truy cập
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        boolean isDefaultPort = ("http".equalsIgnoreCase(scheme) && serverPort == 80) ||
                                ("https".equalsIgnoreCase(scheme) && serverPort == 443);
        String portPart = isDefaultPort ? "" : ":" + serverPort;
        return scheme + "://" + serverName + portPart + "/payment/vnpay-return";
    }
}

