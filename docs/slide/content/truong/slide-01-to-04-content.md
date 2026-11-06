# 📑 SLIDE CONTENT: SLIDE 1 ➔ SLIDE 4 (NGÔ NHẬT TRƯỜNG - MỞ MÀN, BỐI CẢNH, UI & AUTH)
> **Khối trình bày**: Khối 1 — Mở màn buổi bảo vệ, Bối cảnh & Đặt vấn đề, Assignment (UI Toolkit + AI Agent) & Cổng vào Authentication (Login/Logout)  
> **Thời lượng**: 4 phút  
> **Tác phong**: Đĩnh đạc, rõ ràng, phong thái tự tin khi mở màn buổi bảo vệ  

---

### SLIDE 1: TRANG BÌA (TITLE SLIDE) & GIỚI THIỆU NHÓM 2
* **Tiêu đề lớn**: SMART STUDYHUB RAG
* **Phụ đề**: Nền Tảng Quản Lý Học Tập Đa Môn & Luyện Thi A+ Ứng Dụng RAG AI Không Ảo Giác
* **Thông tin nhóm**: Nhóm 2 — Môn HSF302 — FPT University
* **Danh sách 6 thành viên & Chức danh chuyên trách**:
  1. **Lê Thanh Tùng** — Project Manager & Tech Lead
  2. **Ngô Nhật Trường** — Fullstack Dev (Auth, Google OAuth2 & UI Toolkit)
  3. **Nguyễn Văn Gia Bình** — AI Engineer (PDF Chunking & RAG Engine)
  4. **Huỳnh Nhật** — Fullstack Dev (AI Quiz Generator & PDF Export)
  5. **Lê Nguyễn Minh Thắng** — Fullstack Dev (AI Study Planner)
  6. **Ngô Ân** — Security & Admin Lead (Admin Command Center, Doanh Thu VNPay & QA)

---

### SLIDE 2: BỐI CẢNH, ĐẶT VẤN ĐỀ & GIẢI PHÁP TỔNG THỂ
* **Thực trạng & Nỗi đau sinh viên mùa thi**:
  * Tài liệu giáo trình cồng kềnh: Hàng trăm trang PDF, slide bài giảng phân mảnh khắp nơi.
  * ChatGPT & AI thông thường bị "ảo giác" (hallucination): Trả lời chung chung, không có nguồn đối chứng, sinh viên không dám tin cậy khi ôn thi.
  * Tự học cá nhân cô độc: Thiếu động lực, dễ nản lòng, học nhóm khó kiểm soát tiến độ.
* **Giải pháp Smart StudyHub RAG**:
  * **Kho môn học tập trung**: Tự tạo và quản lý môn học cá nhân hóa.
  * **RAG Engine đối chứng 100%**: Chỉ trả lời dựa trên giáo trình đã nạp kèm số trang cụ thể.
  * **Study Squad**: Học nhóm 5 bạn cùng chia sẻ tài liệu và thi đua bảng xếp hạng Leaderboard.

---

### SLIDE 3: PHẦN ASSIGNMENT — TÍCH HỢP UI TOOLKIT & QUY TRÌNH CODE AI AGENT
* **Assignment 1 — UI Toolkit trong Thymeleaf**:
  * Lựa chọn: **Tailwind CSS (JIT Engine) + DaisyUI v4 + FontAwesome 6**.
  * Chuẩn thiết kế **White-Card Palette** thanh lịch, tương phản cao, chống mỏi mắt thay vì Bootstrap cổ điển.
  * Kế thừa bố cục DRY thông qua Thymeleaf Layout (`layout/main.html`) và Fragments (`navbar.html`, `sidebar.html`).
* **Assignment 2 — Quy trình code cùng AI Agent trong IDE**:
  * Môi trường phát triển: **Google Antigravity IDE** tích hợp **Gemini 2.5 Flash** & **Claude 3.5 Sonnet**.
  * Quy trình 3 bước chuẩn kỹ sư: **Spec-Driven** (`06-prompt-engineering-guide.md`) $\rightarrow$ **Test-Driven Development (TDD)** (AI sinh test trước) $\rightarrow$ **Pair-Debugging** (xử lý concurrency và data isolation).

---

### SLIDE 4: PHÂN HỆ AUTHENTICATION — CỔNG VÀO HỆ THỐNG, PROFILE & ĐĂNG XUẤT
* **Đăng nhập Google OAuth2 (OpenID Connect)**:
  * Đăng nhập 1-chạm tiện lợi, tự động đồng bộ Email, Họ tên và Avatar từ Google Account.
* **Đăng nhập & Đăng ký Form truyền thống**:
  * Mã hóa mật khẩu một chiều bằng thuật toán an toàn **BCrypt** (10 salt rounds).
  * Tiện ích phục vụ chấm bài / Demo: **Nút 1-Click Login** tự động điền tài khoản cho cả 3 cấp độ (Free, Pro, Squad Leader).
* **Bảo mật phiên (Session Security), Hồ sơ Sinh viên (Profile) & Đăng xuất (Logout)**:
  * Phân quyền vai trò (RBAC): Sinh viên vào Dashboard môn học, Admin vào Command Center; chặn 100% sinh viên truy cập `/admin/*`.
  * Trang Hồ sơ cá nhân (`/profile`): Hiển thị hạn dùng gói cước (`tierExpiresAt`), bảng hóa đơn điện tử VNPay và nút **Đăng xuất (Logout)** an toàn.
* **Ảnh minh chứng đưa vào slide**: Giao diện Login 50/50, Popup Google OAuth2, Trang Profile White-card, Code layout fragment Thymeleaf.
