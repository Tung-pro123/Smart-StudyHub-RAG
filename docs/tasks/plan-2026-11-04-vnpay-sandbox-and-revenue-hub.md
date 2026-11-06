# 💳 KẾ HOẠCH CHI TIẾT: TÍCH HỢP CỔNG THANH TOÁN VNPAY SANDBOX & BÁO CÁO DOANH THU ADMIN

> **Dự án**: Smart StudyHub RAG (HSF302)  
> **Người chủ trì**: Lê Thanh Tùng (Team Lead - Group 2)  
> **Mục tiêu**: 
> 1. Tích hợp cổng thanh toán trực tuyến **VNPay Sandbox** thực tế cho quy trình nâng cấp gói cước (`PRO` 49.000đ & `SQUAD` 129.000đ).
> 2. Xây dựng phân hệ **Báo cáo Doanh thu & Lịch sử Đơn hàng** ngay trong Admin Dashboard (`/admin/dashboard`).
> 3. Đáp ứng trọn vẹn 100% tiêu chí *"Thanh toán online"* trong đề bài Project cuối kỳ.

---

## 🏗️ I. THIẾT KẾ KIẾN TRÚC & QUY TRÌNH THANH TOÁN (PAYMENT FLOW)

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│ 1. KHỞI TẠO ĐƠN HÀNG (STUDENT BROWSER)                                                          │
│    • Sinh viên bấm "Nâng cấp Gói Pro (49k)" hoặc "Gói Squad (129k)" qua VNPay                    │
│    • Chọn phương thức: Thẻ ATM Nội địa (NCB) hoặc Thẻ Quốc tế (Visa)                             │
│                                                                                                  │
│ 2. XỬ LÝ BACKEND SPRING BOOT (PAYMENT CONTROLLER)                                                │
│    • Sinh mã giao dịch duy nhất: vnp_TxnRef = "ORDER_PRO_" + timestamp + "_" + random           │
│    • Tính số tiền: vnp_Amount = amount * 100 (Ví dụ: 49.000đ ➔ 4900000)                        │
│    • Sắp xếp tham số Alphabet A-Z & Băm chữ ký bảo mật: HMAC-SHA512 với vnp_HashSecret           │
│    • Điều hướng (302 Redirect) trình duyệt sang: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html│
│                                                                                                  │
│ 3. THANH TOÁN TRÊN CỔNG VNPAY SANDBOX                                                            │
│    • Giao diện VNPay Sandbox hiển thị số tiền 49.000 VNĐ                                         │
│    • Nhập thông tin thẻ test:                                                                    │
│      - NCB: 9704198526191432198 | Tên: Tùy ý (NGUYEN VAN A) | Ngày: 07/15 | OTP: 123456         │
│      - VISA: 4456530000001005 | CVV: 123 | Hết hạn: 12/26 | OTP: 123456                         │
│    • Bấm Xác nhận ➔ VNPay xử lý thành công (vnp_ResponseCode = "00")                             │
│                                                                                                  │
│ 4. TIẾP NHẬN RETURN CALLBACK & NÂNG CẤP TÀI KHOẢN                                                │
│    • VNPay redirect về: http://localhost:8080/payment/vnpay-return?...                           │
│    • Kiểm tra tính toàn vẹn chữ ký HMAC-SHA512 chống giả mạo URL                                 │
│    • Cập nhật gói cước người dùng: authService.upgradeSubscriptionTier(username, targetTier)     │
│    • Lưu bản ghi lịch sử vào CSDL: PaymentTransaction (TxnRef, Amount, Bank, Status=SUCCESS)     │
│    • Hiển thị Toast thông báo chúc mừng thanh toán thành công                                    │
│                                                                                                  │
│ 5. BÁO CÁO DOANH THU & ĐƠN HÀNG TẠI ADMIN HUB                                                    │
│    • Admin truy cập /admin/dashboard ➔ Thẻ KPI "💰 Tổng Doanh Thu" tự động nhảy số               │
│    • Bảng "Lịch Sử Đơn Hàng": Hiển thị chi tiết từng đơn hàng vừa thanh toán thành công          │
└──────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ II. DANH SÁCH CÁC FILE TRIỂN KHAI MÃ NGUỒN

### 1. Phân hệ Cấu hình & Utility (`com.group2.assignment.payment.config`)
* **`VnPayConfig.java`**:
  * Các hằng số URL Sandbox: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`.
  * Return URL: `http://localhost:8080/payment/vnpay-return`.
  * `vnp_TmnCode`: `I5L22N4F` (hoặc cấu hình từ biến môi trường `${VNPAY_TMN_CODE}`).
  * `vnp_HashSecret`: `NJ22IGZBHGJISKIBWBTHXAATUSXQIAHL` (từ biến môi trường `${VNPAY_HASH_SECRET}`).
  * Phương thức `hmacSHA512(key, data)`: Chuẩn hóa băm an toàn SHA-512.
  * Phương thức `hashAllFields(Map<String, String> fields)`: Tự động sắp xếp key theo alphabet và build chuỗi ký.

### 2. Phân hệ CSDL & Quản lý Đơn hàng (`com.group2.assignment.payment.entity / repository`)
* **`PaymentTransaction.java`**:
  * Bảng: `payment_transactions`.
  * Thuộc tính: `id`, `txnRef` (mã đơn), `user` (người mua), `targetTier` (PRO / SQUAD), `amount` (số tiền VNĐ), `bankCode` (NCB / VISA), `vnpayTransactionNo` (mã VNPay), `status` (SUCCESS / FAILED), `responseCode` (00), `orderInfo`, `createdAt`.
* **`PaymentTransactionRepository.java`**:
  * `Optional<PaymentTransaction> findByTxnRef(String txnRef);`
  * `List<PaymentTransaction> findTop20ByOrderByCreatedAtDesc();`
  * `Long getTotalRevenue();` (Tính tổng doanh thu các đơn SUCCESS).
  * `Long countByStatus(String status);` (Đếm số đơn thành công).

### 3. Phân hệ Dịch vụ & Điều hướng (`com.group2.assignment.payment.service / controller`)
* **`PaymentService.java`**:
  * `String createVnPayPaymentUrl(User user, String tier, String bankCode, HttpServletRequest request);`
  * `PaymentTransaction processVnPayReturn(Map<String, String> params, HttpSession session);`
* **`PaymentController.java`**:
  * `@PostMapping("/payment/create-vnpay")`: Tiếp nhận yêu cầu thanh toán từ modal, redirect sang VNPay.
  * `@GetMapping("/payment/vnpay-return")`: Tiếp nhận kết quả thanh toán từ VNPay, nâng cấp gói và lưu lịch sử.
* **`AdminController.java` & `AdminDashboardDto`**:
  * Bổ sung dữ liệu `totalRevenue` và danh sách `recentTransactions` vào Model gửi sang trang Admin Dashboard.

### 4. Giao diện người dùng (`templates/`)
* **`templates/index.html` & `templates/planner/index.html`**:
  * Cập nhật Modal Bảng Giá 3 Gói Cước (`modal-pricing-upgrade`):
    * Bổ sung 2 nút bấm: **"Thanh toán VNPay (ATM NCB)"** và **"Thanh toán VNPay (Visa)"**.
    * Thêm **Hộp gợi ý thông tin thẻ Test 1-click Copy** (Số thẻ, Ngày, OTP) để khi demo chỉ cần bấm copy dán vào là xong ngay.
* **`templates/admin/dashboard.html`**:
  * Thêm thẻ KPI: **💰 Tổng Doanh Thu (VNPay Sandbox)** với định dạng tiền tệ Việt Nam.
  * Thêm bảng: **"Lịch Sử Giao Dịch & Đơn Hàng VNPay"** với các cột: Mã đơn, Khách hàng, Gói cước, Số tiền, Ngân hàng, Mã giao dịch VNPay, Trạng thái (Huy hiệu xanh `THÀNH CÔNG`), Ngày thanh toán.

### 5. Bộ kiểm thử tự động (`src/test/java/com/group2/assignment/payment/`)
* **`VnPayPaymentAndRevenueTest.java`**:
  * Test 1: Tạo URL thanh toán VNPay có đầy đủ tham số và chữ ký hợp lệ.
  * Test 2: Băm HMAC-SHA512 mã hóa chính xác theo chuẩn RFC 2104.
  * Test 3: Xử lý Return Callback thành công (`00`) ➔ Nâng gói cước lên PRO và lưu bản ghi `PaymentTransaction`.
  * Test 4: Chữ ký không hợp lệ (giả mạo hash) ➔ Chặn nâng gói, báo lỗi bảo mật.
  * Test 5: Tính tổng doanh thu Admin chính xác.

---

## 📋 III. KẾ HOẠCH TRIỂN KHAI TỪNG BƯỚC (STEP-BY-STEP ROADMAP)

| Bước | Nội dung công việc | File tác động | Dự kiến |
| :---: | :--- | :--- | :---: |
| **Bước 1** | Cấu hình tham số VNPay trong `application.properties` & Tạo `VnPayConfig.java` | `application.properties`, `VnPayConfig.java` | 3 phút |
| **Bước 2** | Tạo Entity `PaymentTransaction` & Repository | `PaymentTransaction.java`, `PaymentTransactionRepository.java` | 3 phút |
| **Bước 3** | Viết `PaymentService` & `PaymentController` (tạo URL + xử lý callback) | `PaymentService.java`, `PaymentController.java` | 5 phút |
| **Bước 4** | Cập nhật Modal Bảng Giá (thêm nút VNPay + Hộp thông tin thẻ test copy 1-click) | `templates/index.html`, `templates/planner/index.html` | 4 phút |
| **Bước 5** | Cập nhật Admin Dashboard (thẻ KPI Doanh thu + Bảng lịch sử đơn hàng) | `AdminController.java`, `templates/admin/dashboard.html` | 4 phút |
| **Bước 6** | Viết Unit Test `VnPayPaymentAndRevenueTest` & Chạy kiểm thử toàn bộ hệ thống | `src/test/java/.../VnPayPaymentAndRevenueTest.java` | 3 phút |
| **Bước 7** | Chạy thử E2E: Thanh toán gói Pro 49k bằng thẻ NCB ➔ Kiểm tra tài khoản lên PRO ➔ Mở Admin xem doanh thu nhảy số! | Trình duyệt thực tế | 2 phút |

---

## 💳 IV. BẢNG THÔNG TIN THẺ TEST PHỤC VỤ DEMO

### 1. Thẻ ATM Nội địa (Ngân hàng NCB)
* **Ngân hàng**: `NCB` (Ngân hàng Quốc Dân)
* **Số thẻ**: `9704198526191432198`
* **Tên chủ thẻ**: Tùy ý (Ví dụ: `NGUYEN VAN A` hoặc `LE THANH TUNG`)
* **Ngày phát hành**: `07/15`
* **Mật khẩu OTP**: `123456`

### 2. Thẻ Quốc tế (Visa)
* **Số thẻ**: `4456530000001005`
* **Tên chủ thẻ**: Tùy ý (Ví dụ: `LE THANH TUNG`)
* **Hết hạn (EXP)**: `12/26`
* **Mã bảo mật CVV**: `123`
* **Mật khẩu OTP**: `123456`

