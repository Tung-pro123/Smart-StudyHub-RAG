# Nội dung slide 18–22 — Ngô Ân

> **Khối trình bày**: Khối 6 — Vận hành Admin, Phân hệ Doanh thu VNPay & Đối soát đơn hàng, QA 333 tests, Setup & Q&A  
> **Thời lượng**: 4 phút liên tục  

---

## Slide 18 — Admin Command Center: Quản trị hệ thống, Doanh thu VNPay & Đối soát đơn hàng

- **Dashboard thời gian thực**: Theo dõi sinh viên theo gói, môn học, Chunks tài liệu và lượt gọi AI RAG hôm nay (GMT+7).
- **Phân hệ Báo cáo Doanh thu & Cổng thanh toán VNPay Sandbox (`AYM70LAS`)**:
  - Thẻ KPI Doanh thu tự động nhảy số: Tổng thu (VNĐ), phân tách gói PRO (49.000đ), gói SQUAD (129.000đ) và doanh thu hôm nay.
  - Bảng Lưu vết Đơn hàng: Lưu vết Mã đơn hàng `TxnRef`, tài khoản sinh viên, số tiền, ngân hàng (NCB/Visa), mã giao dịch VNPay `vnpayTransactionNo` và trạng thái `THÀNH CÔNG`.
  - **Nút "Xuất CSV Đối Soát"** (`/admin/revenue/export-csv`): Tải file CSV định dạng UTF-8 BOM mở bằng Microsoft Excel hiển thị đầy đủ tiếng Việt có dấu.
  - **Cổng đối soát VNPay Merchant Portal**: Minh chứng đối chiếu 1-1 trên `sandbox.vnpayment.vn/merchantv2` (tài khoản `thanhtung22012006@gmail.com`).
- **Quản lý người dùng (`/admin/users`)**:
  - Tìm kiếm, lọc và phân trang 15 sinh viên; quản lý nâng/hạ gói FREE / PRO / SQUAD.
  - Nút **"Reset Quota RAG"**: Khôi phục lại 50 câu hỏi/ngày cho sinh viên khi cần hỗ trợ đột xuất.

Ảnh minh chứng tại `docs/evidence/2026-11-04-final-presentation/an/`: `dashboard_an.png`, `upgrade-pro_an.png`, `upgrade-squad_an.png`, `downgrade-free_an.png`, `reset-quota_an.png`.

---

## Slide 19 — Centralized Gemini System Key, Bảo mật Masking & Kiểm tra kết nối

- **Cơ chế Centralized System Key**: Quản trị viên cấu hình Gemini API Key một lần duy nhất tại trang Cài đặt (`/admin/settings`), toàn bộ hệ thống tự động chia sẻ an toàn cho Chatbot RAG, Quiz và Planner.
- **Bảo mật API Key Masking (`AIzaSy...****`)**: Tự động che giấu ký tự nhạy cảm trên giao diện web, triệt tiêu nguy cơ lộ key khi trình chiếu slide hoặc quay video demo.
- **Kiểm tra kết nối thực tế (Ping Test)**: Nút bấm Ping gửi yêu cầu tới máy chủ Google AI API, đo thời gian đáp ứng thực tế (ms) đảm bảo hệ thống sẵn sàng.
- **Ping Gemini thật ngày 04/11/2026: thành công, 1206 ms.** Ảnh minh chứng `ping-success_an.png`.

---

## Slide 20 — Báo cáo đảm bảo chất lượng toàn diện (QA Report — 333 Tests PASS)

- **333 tests — 0 failures — 0 errors — 0 skipped — BUILD SUCCESS 100%.**
  - Bao gồm 6 test cases kiểm thử tích hợp thanh toán VNPay Sandbox & tính toán doanh thu (`VnPayPaymentAndRevenueTest`).
  - Riêng `AdminHubTest`: **40/40 PASS**.
- **Các kịch bản kiểm thử trọng yếu đã vượt qua**:
  - **VNPay Integrity & Revenue Test**: Kiểm thử tạo URL thanh toán HMAC-SHA512, xác thực phản hồi Sandbox, tính toán tổng doanh thu và logic cộng dồn 30 ngày hạn dùng gói cước.
  - **Quota Concurrency Test**: Kiểm thử chịu tải đồng thời, chống gian lận vượt quá 50 câu RAG/ngày.
  - **Data Isolation Test**: Kiểm thử cách ly môn học cá nhân và đề thi Solo trong môn nhóm `[SQUAD]`.
  - **RBAC Security Test**: Chặn 100% sinh viên thường truy cập `/admin/*` và `/h2-console`.
  - **Leaderboard Guard Test**: Đề thi Solo tuyệt đối không rò rỉ điểm số lên Squad Leaderboard.

---

## Slide 21 — Hướng dẫn cài đặt và khởi chạy dự án (User Guide — Setup & Code)

- **Môi trường yêu cầu**: Java 17 LTS theo cấu hình dự án, Maven 3.8 trở lên, H2 Database tự tạo file dữ liệu cục bộ (`./data/studyhubdb`).
- **Quy trình triển khai 3 bước siêu tốc**:
  1. Clone mã nguồn từ GitHub: `git clone https://github.com/Tung-pro123/Smart-StudyHub-RAG.git`
  2. Cấu hình biến môi trường file `.env`: Điền `GEMINI_API_KEY`, `GOOGLE_CLIENT_ID` và `GOOGLE_CLIENT_SECRET`.
  3. Khởi chạy với 1 lệnh duy nhất: `mvn spring-boot:run` $\rightarrow$ Mở trình duyệt tại `http://localhost:8080`.
- Tài khoản quản trị mặc định: `admin` / `password123`.

---

## Slide 22 — Tổng kết, Lời cảm ơn & Mời Hội đồng bước vào phần Q&A

- **Tổng kết giá trị sản phẩm**: Kết hợp trọn vẹn cả nội dung Assignment (Thymeleaf UI Toolkit + AI Agent trong IDE) lẫn Project cuối kỳ (RAG AI không ảo giác, Google OAuth2, Quiz, Planner, Squad Hub và Cổng thanh toán VNPay Sandbox).
- Sản phẩm hoàn thiện thực chiến, kiểm thử tự động 100%, sẵn sàng đưa vào ứng dụng thực tế.
- **Lời cảm ơn**: Thay mặt toàn thể 6 thành viên Nhóm 2, em xin gửi lời cảm ơn chân thành tới quý Thầy Cô trong Hội đồng Giảng viên.
- Toàn thể nhóm xin trân trọng kính mời quý Thầy Cô bước vào phần Hỏi - Đáp (Q&A).
