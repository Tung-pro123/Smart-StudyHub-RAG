# 📑 SLIDE CONTENT: SLIDE 18 ➔ SLIDE 22 (NGÔ ÂN - ADMIN COMMAND CENTER, QA & SETUP)
> **Khối trình bày**: Khối 6 — Admin Dashboard, Masking System Key, Báo cáo QA 327 tests, User Guide & Q&A  
> **Thời lượng**: 3.5 phút  

---

### SLIDE 18: ADMIN COMMAND CENTER — GIÁM SÁT KPI, DOANH THU VNPAY & ĐỐI SOÁT ĐƠN HÀNG
* **Bảng điều khiển trung tâm (Real-time Dashboard)**:
  * Theo dõi tức thời sức khỏe hệ thống qua các chỉ số cốt lõi: Tổng sinh viên, Tổng môn học, Tổng số Chunks tài liệu, Tổng lượt gọi AI RAG trong ngày.
  * Biểu đồ phân bổ tỷ lệ người dùng giữa 3 gói cước: Free vs Pro vs Squad.
* **Phân hệ Báo cáo Doanh thu & Cổng Thanh toán VNPay Sandbox**:
  * **Thẻ KPI Doanh thu nhảy số thời gian thực**: Tổng thu (VNĐ), phân tách chi tiết gói PRO (49.000đ) vs gói SQUAD (129.000đ), doanh thu hôm nay và số đơn thành công.
  * **Bảng Lưu vết Đơn hàng & Lịch sử Giao dịch**: Theo dõi mã đơn hàng `TxnRef`, tài khoản sinh viên, số tiền, ngân hàng (NCB/Visa), mã giao dịch VNPay `vnpayTransactionNo`, trạng thái `THÀNH CÔNG`.
  * **Xuất Báo cáo Tài chính CSV**: Nút tải file CSV chuẩn UTF-8 BOM mở bằng Microsoft Excel hiển thị tiếng Việt có dấu trọn vẹn để kế toán đối soát.
  * **Đối soát 1-1 với VNPay Merchant Portal**: Minh chứng đối chiếu trực tiếp trên cổng `sandbox.vnpayment.vn/merchantv2` khớp 100% từng mã giao dịch.
* **Quản trị người dùng & Hỗ trợ kỹ thuật (`/admin/users`)**:
  * Nâng cấp hoặc hạ cấp gói cước linh hoạt (hỗ trợ sinh viên kích hoạt gói nhanh).
  * Nút **"Reset Quota RAG"**: Khôi phục lại 50 câu hỏi/ngày cho sinh viên khi cần hỗ trợ đột xuất.

---

### SLIDE 19: CENTRALIZED AI KEY, BẢO MẬT MASKING & ĐO LATENCY PING
* **Cơ chế Centralized System Key**:
  * Quản trị viên chỉ cần cấu hình Gemini API Key một lần duy nhất tại trang Quản trị (`/admin/settings`).
  * Toàn bộ hệ thống tự động chia sẻ key an toàn cho các phân hệ AI (Chatbot RAG, Quiz Generator, Study Planner) mà sinh viên không cần phải tự tạo hay nhập key cá nhân.
* **Bảo mật API Key Masking (`AIzaSy...****`)**:
  * Tự động che giấu các ký tự nhạy cảm của khóa API trên giao diện web, triệt tiêu nguy cơ bị lộ key khi trình chiếu slide hoặc quay video demo.
* **Đo độ trễ mạng thực tế (Latency Ping Test)**:
  * Nút bấm Ping trực tiếp tới máy chủ Google AI API, đo chính xác thời gian đáp ứng (mili-giây ms), đảm bảo đường truyền sẵn sàng phục vụ sinh viên.

---

### SLIDE 20: BÁO CÁO ĐẢM BẢO CHẤT LƯỢNG TOÀN DIỆN (QA REPORT)
* **Kết quả kiểm thử tự động (Automated Regression Suite)**:
  * **333 / 333 Unit & Integration Tests PASS (100%)** không một lỗi nào (bao gồm 6 bài test kiểm thử tích hợp thanh toán VNPay Sandbox & tính toán doanh thu).
* **Các kịch bản kiểm thử trọng yếu đã vượt qua**:
  * **VNPay Integrity & Revenue Test**: Kiểm thử tạo URL thanh toán HMAC-SHA512, xác thực phản hồi Sandbox, tính toán tổng doanh thu và logic cộng dồn 30 ngày hạn dùng gói cước.
  * **Quota Concurrency Test**: Kiểm thử chịu tải đồng thời, chống gian lận vượt quá 50 câu RAG/ngày.
  * **Data Isolation Test**: Kiểm thử cách ly môn học cá nhân và đề thi Solo trong môn nhóm `[SQUAD]`.
  * **RBAC Security Test**: Chặn 100% sinh viên thường truy cập `/admin/*` và `/h2-console`.
  * **Leaderboard Guard Test**: Đề thi Solo tuyệt đối không rò rỉ điểm số lên Squad Leaderboard.

---

### SLIDE 21: HƯỚNG DẪN CÀI ĐẶT & CHẠY DỰ ÁN (USER GUIDE — SETUP & CODE)
* **Yêu cầu môi trường chuẩn**:
  * Java Development Kit: **JDK 17 LTS**.
  * Quản lý gói: **Apache Maven 3.8+**.
  * Cơ sở dữ liệu: **H2 Database** tự tạo file cục bộ (không cần cài đặt MySQL/PostgreSQL phức tạp).
* **Quy trình triển khai 3 bước siêu tốc**:
  1. Clone mã nguồn: `git clone https://github.com/Tung-pro123/Smart-StudyHub-RAG.git`
  2. Cấu hình biến môi trường file `.env`: Điền `GOOGLE_CLIENT_ID` và `GEMINI_API_KEY`.
  3. Khởi chạy với 1 lệnh duy nhất: `mvn spring-boot:run` $\rightarrow$ Ứng dụng sẵn sàng tại `http://localhost:8080`.

---

### SLIDE 22: KẾT LUẬN, LỜI CẢM ƠN & MỜI HỘI ĐỒNG BƯỚC VÀO PHẦN Q&A
* **Tổng kết giá trị dự án Group 2 đạt được**:
  * Đáp ứng trọn vẹn 100% các tiêu chí khắt khe của cả đề bài Assignment (Thymeleaf UI Toolkit + AI Agent) lẫn Project cuối kỳ (RAG AI không ảo giác + Google OAuth2 + Shared Squad Hub + PDFBox).
  * Sản phẩm hoàn thiện thực chiến, giao diện thẩm mỹ cao, mã nguồn kiểm thử tự động 100%.
* **Lời cảm ơn**:
  * Trân trọng cảm ơn Thầy/Cô trong Hội đồng Giảng viên đã lắng nghe và hướng dẫn nhóm trong suốt học kỳ vừa qua.
  * Nhóm 2 xin trân trọng kính mời quý Thầy/Cô bước vào phần Hỏi - Đáp (Q&A).

