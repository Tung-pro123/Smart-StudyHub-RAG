package com.group2.assignment.payment;

import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.auth.service.AuthService;
import com.group2.assignment.payment.config.VnPayConfig;
import com.group2.assignment.payment.entity.PaymentTransaction;
import com.group2.assignment.payment.repository.PaymentTransactionRepository;
import com.group2.assignment.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VnPayPaymentAndRevenueTest {

    @Autowired
    private VnPayConfig vnPayConfig;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByUsernameIgnoreCase("vnpay_tester").orElseGet(() -> {
            User user = User.builder()
                    .username("vnpay_tester")
                    .email("vnpay_tester@fpt.edu.vn")
                    .fullName("Nguyễn Văn A (Tester)")
                    .password("secret123")
                    .role("ROLE_STUDENT")
                    .subscriptionTier("FREE")
                    .build();
            return userRepository.save(user);
        });
        testUser.setSubscriptionTier("FREE");
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Băm HMAC-SHA512 hoạt động chuẩn xác và không rỗng")
    void testHmacSha512Algorithm() {
        String key = "NDJFXTYRTNJRRTHQNDDBUVGDXPTYPFMY";
        String data = "vnp_Amount=4900000&vnp_Command=pay&vnp_TmnCode=AYM70LAS";
        String hash = VnPayConfig.hmacSHA512(key, data);

        assertNotNull(hash);
        assertEquals(128, hash.length(), "HMAC-SHA512 phải sinh ra chuỗi hex 128 ký tự");
    }

    @Test
    @DisplayName("Khởi tạo URL VNPay gói PRO (49.000 VNĐ) với mã ngân hàng NCB thành công")
    void testCreateVnPayPaymentUrlProNcb() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setServerPort(6969);
        request.setScheme("http");

        String url = paymentService.createVnPayPaymentUrl(testUser, "PRO", "NCB", request);

        assertNotNull(url);
        assertTrue(url.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?"));
        assertTrue(url.contains("vnp_Amount=4900000"));
        assertTrue(url.contains("vnp_BankCode=NCB"));
        assertTrue(url.contains("vnp_Command=pay"));
        assertTrue(url.contains("vnp_TmnCode=" + vnPayConfig.getTmnCode()));
        assertTrue(url.contains("vnp_SecureHash="));

        // Kiểm tra bản ghi PENDING đã được tạo trong CSDL
        var txList = paymentTransactionRepository.findByUsernameOrderByCreatedAtDesc(testUser.getUsername());
        assertFalse(txList.isEmpty());
        PaymentTransaction tx = txList.get(0);
        assertEquals("PENDING", tx.getStatus());
        assertEquals("PRO", tx.getTargetTier());
        assertEquals(49000L, tx.getAmount());
        assertEquals("NCB", tx.getBankCode());
    }

    @Test
    @DisplayName("Khởi tạo URL VNPay gói SQUAD (129.000 VNĐ) với mã ngân hàng VISA thành công")
    void testCreateVnPayPaymentUrlSquadVisa() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setServerPort(6969);
        request.setScheme("http");

        String url = paymentService.createVnPayPaymentUrl(testUser, "SQUAD", "VISA", request);

        assertNotNull(url);
        assertTrue(url.contains("vnp_Amount=12900000"));
        assertTrue(url.contains("vnp_BankCode=VISA"));
        assertTrue(url.contains("vnp_SecureHash="));

        var txList = paymentTransactionRepository.findByUsernameOrderByCreatedAtDesc(testUser.getUsername());
        assertFalse(txList.isEmpty());
        PaymentTransaction tx = txList.get(0);
        assertEquals("SQUAD", tx.getTargetTier());
        assertEquals(129000L, tx.getAmount());
        assertEquals("VISA", tx.getBankCode());
    }

    @Test
    @DisplayName("Xử lý Return Callback thành công (00) -> Nâng cấp tài khoản lên PRO & Lưu trạng thái SUCCESS")
    void testProcessVnPayReturnSuccess() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        paymentService.createVnPayPaymentUrl(testUser, "PRO", "NCB", request);

        PaymentTransaction pendingTx = paymentTransactionRepository
                .findByUsernameOrderByCreatedAtDesc(testUser.getUsername()).get(0);

        // Giả lập params callback gửi về từ VNPay
        Map<String, String> returnParams = new HashMap<>();
        returnParams.put("vnp_Amount", "4900000");
        returnParams.put("vnp_BankCode", "NCB");
        returnParams.put("vnp_CardType", "ATM");
        returnParams.put("vnp_OrderInfo", pendingTx.getOrderInfo());
        returnParams.put("vnp_PayDate", "20261104200000");
        returnParams.put("vnp_ResponseCode", "00");
        returnParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        returnParams.put("vnp_TransactionNo", "14589230");
        returnParams.put("vnp_TransactionStatus", "00");
        returnParams.put("vnp_TxnRef", pendingTx.getTxnRef());

        // Sinh chữ ký hợp lệ
        String secureHash = vnPayConfig.hashAllFields(returnParams);
        returnParams.put("vnp_SecureHash", secureHash);

        MockHttpSession session = new MockHttpSession();

        PaymentTransaction resultTx = paymentService.processVnPayReturn(returnParams, session);

        assertEquals("SUCCESS", resultTx.getStatus());
        assertEquals("00", resultTx.getResponseCode());
        assertEquals("14589230", resultTx.getVnpayTransactionNo());

        // Kiểm tra tài khoản đã được nâng cấp lên PRO
        User updatedUser = userRepository.findByUsernameIgnoreCase(testUser.getUsername()).orElseThrow();
        assertEquals("PRO", updatedUser.getSubscriptionTier());
    }

    @Test
    @DisplayName("Callback bị can thiệp chữ ký (Invalid Hash) -> Bị chặn và ném Exception bảo mật")
    void testProcessVnPayReturnInvalidSignature() {
        Map<String, String> returnParams = new HashMap<>();
        returnParams.put("vnp_Amount", "4900000");
        returnParams.put("vnp_TxnRef", "FAKE_ORDER_123");
        returnParams.put("vnp_ResponseCode", "00");
        returnParams.put("vnp_SecureHash", "fake_invalid_hash_signature_123456");

        MockHttpSession session = new MockHttpSession();

        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processVnPayReturn(returnParams, session);
        });
    }

    @Test
    @DisplayName("Tính tổng doanh thu Admin chính xác theo tổng các đơn hàng SUCCESS")
    void testAdminTotalRevenue() {
        Long initialRevenue = paymentTransactionRepository.getTotalRevenue();
        Long initialOrders = paymentTransactionRepository.countByStatus("SUCCESS");

        // Tạo 2 giao dịch thành công (49k + 129k) và 1 giao dịch thất bại (49k)
        PaymentTransaction tx1 = PaymentTransaction.builder()
                .txnRef("TEST_REV_1_" + UUID.randomUUID())
                .user(testUser)
                .username(testUser.getUsername())
                .targetTier("PRO")
                .amount(49000L)
                .bankCode("NCB")
                .status("SUCCESS")
                .build();
        PaymentTransaction tx2 = PaymentTransaction.builder()
                .txnRef("TEST_REV_2_" + UUID.randomUUID())
                .user(testUser)
                .username(testUser.getUsername())
                .targetTier("SQUAD")
                .amount(129000L)
                .bankCode("VISA")
                .status("SUCCESS")
                .build();
        PaymentTransaction tx3 = PaymentTransaction.builder()
                .txnRef("TEST_REV_3_" + UUID.randomUUID())
                .user(testUser)
                .username(testUser.getUsername())
                .targetTier("PRO")
                .amount(49000L)
                .bankCode("NCB")
                .status("FAILED")
                .build();

        paymentTransactionRepository.save(tx1);
        paymentTransactionRepository.save(tx2);
        paymentTransactionRepository.save(tx3);

        Long newRevenue = paymentTransactionRepository.getTotalRevenue();
        Long newOrders = paymentTransactionRepository.countByStatus("SUCCESS");

        assertEquals(initialRevenue + 49000L + 129000L, newRevenue, "Doanh thu phải tăng đúng bằng tổng số tiền các đơn SUCCESS");
        assertEquals(initialOrders + 2L, newOrders, "Số đơn thành công phải tăng thêm 2 đơn");
    }
}

