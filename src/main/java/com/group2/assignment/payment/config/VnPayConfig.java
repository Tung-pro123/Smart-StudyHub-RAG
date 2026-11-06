package com.group2.assignment.payment.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Getter
public class VnPayConfig {

    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND = "pay";
    public static final String VNP_CURRENCY = "VND";
    public static final String VNP_LOCALE = "vn";

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpayUrl;

    @Value("${vnpay.tmn-code:AYM70LAS}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:NDJFXTYRTNJRRTHQNDDBUVGDXPTYPFMY}")
    private String hashSecret;

    @Value("${vnpay.return-url:http://localhost:6969/payment/vnpay-return}")
    private String returnUrl;

    /**
     * Băm chuỗi dữ liệu bằng thuật toán HMAC-SHA512
     */
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new IllegalArgumentException("Key và Data không được để null");
            }
            Mac hmacSha512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmacSha512.init(secretKey);
            byte[] hash = hmacSha512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                hexString.append(String.format("%02x", b & 0xff));
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Lỗi khi băm chữ ký HMAC-SHA512: " + ex.getMessage(), ex);
        }
    }

    /**
     * Sắp xếp và băm các tham số theo chuẩn VNPay 2.1.0
     */
    public String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        try {
            for (String fieldName : fieldNames) {
                String fieldValue = fields.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append('&');
                    }
                    sb.append(fieldName);
                    sb.append('=');
                    sb.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                }
            }
            return hmacSHA512(this.hashSecret, sb.toString());
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi khi sắp xếp và mã hóa tham số VNPay: " + e.getMessage(), e);
        }
    }

    /**
     * Xây dựng URL thanh toán hoàn chỉnh kèm chữ ký vnp_SecureHash
     */
    public String buildPaymentUrl(Map<String, String> vnpParams) {
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    if (hashData.length() > 0) {
                        hashData.append('&');
                        query.append('&');
                    }
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                }
            }
            String vnpSecureHash = hmacSHA512(this.hashSecret, hashData.toString());
            return this.vnpayUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnpSecureHash;
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi khi sinh URL VNPay: " + e.getMessage(), e);
        }
    }

    /**
     * Xác thực tính toàn vẹn chữ ký VNPay trả về
     */
    public boolean verifyReturnHash(Map<String, String> fields, String vnpSecureHash) {
        if (vnpSecureHash == null || vnpSecureHash.isBlank()) {
            return false;
        }
        Map<String, String> verifyMap = new HashMap<>(fields);
        verifyMap.remove("vnp_SecureHash");
        verifyMap.remove("vnp_SecureHashType");

        String calculatedHash = hashAllFields(verifyMap);
        return calculatedHash.equalsIgnoreCase(vnpSecureHash.trim());
    }
}

