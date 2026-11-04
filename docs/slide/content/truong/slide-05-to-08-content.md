# 📑 SLIDE CONTENT: SLIDE 5 ➔ SLIDE 8 (NGÔ NHẬT TRƯỜNG - AUTH & UI)
> **Khối trình bày**: Khối 2 — Assignment UI Toolkit, AI Agent IDE & Phân hệ Authentication / Profile  
> **Thời lượng**: 3 phút  

---

### SLIDE 5: PHẦN ASSIGNMENT 1 — TÍCH HỢP UI TOOLKIT TRONG THYMELEAF
* **Công nghệ lựa chọn**: **Tailwind CSS (JIT Engine) + DaisyUI v4 + FontAwesome 6**.
* **Tại sao không dùng Bootstrap truyền thống?**
  * Tùy biến linh hoạt: Dễ dàng dựng phong cách White-Card Palette và Glassmorphism cao cấp.
  * Tối ưu hiệu năng: JIT Engine chỉ biên dịch các class thực dùng, tốc độ tải trang cực nhanh.
  * Hệ thống component của DaisyUI: Button, Badge, Modal, Collapse, Progress Bar được xây dựng sẵn chuẩn ngữ nghĩa.
* **Kỹ thuật tích hợp trong Thymeleaf**:
  * Kế thừa bố cục DRY thông qua Thymeleaf Layout: `layout/main.html`.
  * Đóng gói các phân hệ tái sử dụng bằng Thymeleaf Fragments: `fragments/navbar.html`, `sidebar.html`, `footer.html`.

---

### SLIDE 6: PHẦN ASSIGNMENT 2 — QUY TRÌNH CODE VỚI AI AGENT TRONG IDE
* **Môi trường & Công cụ AI**:
  * **Google Antigravity IDE** kết hợp mô hình đa nhiệm **Gemini 2.5 Flash** & **Claude 3.5 Sonnet**.
* **Quy trình phát triển 3 bước nghiêm ngặt**:
  1. **Spec-Driven & Prompt Engineering**: Định nghĩa rõ ràng Input, Output, Business Rules trước khi bắt đầu code (`docs/06-prompt-engineering-guide.md`).
  2. **Test-Driven Development (TDD)**: Yêu cầu AI Agent sinh bộ Unit Test kiểm thử logic nghiệp vụ trước khi triển khai Service implementation.
  3. **Pair-Debugging**: Tận dụng AI Agent phân tích stack trace để khắc phục các bài toán concurrency (Quota race condition) và cách ly dữ liệu cá nhân (Data Isolation).

---

### SLIDE 7: PHÂN HỆ AUTHENTICATION — ĐĂNG NHẬP ĐA KÊNH
* **Đăng nhập Google OAuth2 (OpenID Connect)**:
  * Đăng nhập 1 chạm tiện lợi, bảo mật tuyệt đối không cần quản lý mật khẩu.
  * Tự động đồng bộ Email, Tên hiển thị và Avatar từ Google Account.
* **Đăng nhập & Đăng ký Form truyền thống**:
  * Mã hóa mật khẩu một chiều bằng thuật toán an toàn **BCrypt** (10 salt rounds).
  * Tiện ích phục vụ chấm bài / Demo: **Nút 1-Click Login** tự động điền tài khoản cho cả 3 cấp độ (Free, Pro, Squad Leader).

---

### SLIDE 8: BẢO MẬT PHIÊN (SESSION SECURITY), RBAC & PROFILE CÁ NHÂN
* **Phân quyền truy cập theo vai trò (Role-Based Access Control)**:
  * Tự động điều hướng sau đăng nhập: Sinh viên (`ROLE_STUDENT`) vào Dashboard môn học; Quản trị viên (`ROLE_ADMIN`) vào Trung tâm điều hành Admin.
  * Bảo vệ các route nhạy cảm: Chặn sinh viên truy cập `/admin/*` và `/h2-console`.
* **Trang Hồ sơ cá nhân (Profile)**:
  * Quản lý thông tin, đổi avatar đại diện.
  * Hiển thị trực quan huy hiệu gói cước và hạn ngạch RAG ngày.
* **Ảnh minh chứng đưa vào slide**: Giao diện Login 50/50, Popup Google OAuth2, Trang Profile cá nhân, Code layout fragment Thymeleaf.

