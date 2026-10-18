# 📝 NHẬT KÝ THỰC NGHIỆM AI - LEADER TÙNG (CORE & LAYOUT)

**Thành viên thực hiện**: Tùng (Project Manager & Tech Lead)  
**Phạm vi phụ trách**: Module Core (Package-by-Feature Architecture, Database Migration V1, CRUD Courses, Master Layout Glassmorphism)  
**Công cụ AI sử dụng**: Cursor / Gemini / Copilot  

---

## 🚀 TIẾN TRÌNH ÁP DỤNG AI THEO TỪNG GIAI ĐOẠN

### 🔹 GIAI ĐOẠN 1: DÙNG AI TÁI CẤU TRÚC KIẾN TRÚC PACKAGE-BY-FEATURE
* **Mục tiêu**: Tái cơ cấu từ mô hình Layered Architecture (`model/`, `repository/`, `service/`) sang mô hình **Package-by-Feature** chuẩn doanh nghiệp (`core`, `rag`, `planner`, `quiz`, `settings`) để 6 thành viên làm độc lập không bị conflict.
* **Câu Prompt đã dùng**:
  ```text
  Tôi đang làm dự án Spring Boot 3 cho môn HSF302. Hãy tái cấu trúc toàn bộ dự án từ mô hình tầng (Layered) sang mô hình Package-by-Feature (mỗi feature gồm entity, repository, service, controller). Đảm bảo Entity Course và Chunk nằm ở package core để các module rag, planner, quiz chỉ cần import từ core, loại bỏ hoàn toàn circular dependency.
  ```
* **Kết quả nhận được từ AI**:
  - AI đã phân tách các package sạch sẽ và cập nhật toàn bộ package declaration / import statements.
  - Maven compile kiểm tra 24 file Java thành công không lỗi cú pháp.
* **Đánh giá & Điều chỉnh của bản thân**: Kiểm tra kỹ các mối quan hệ `@OneToMany`, `@ManyToOne` và cấu hình Flyway migration script `V1__initial_schema.sql` cho 10 bảng trong H2 Database.

---

### 🔹 GIAI ĐOẠN 2: DÙNG AI THIẾT KẾ MASTER LAYOUT GLASSMORPHISM VÀ CRUD COURSES
* **Mục tiêu**: Xây dựng giao diện trang chủ và danh sách môn học bằng Thymeleaf Fragments kết hợp hiệu ứng Glassmorphism hiện đại (Tailwind CSS + DaisyUI).
* **Câu Prompt đã dùng**:
  ```text
  Hãy viết file index.html và layout.html bằng Thymeleaf sử dụng Tailwind CSS và DaisyUI. Thiết kế giao diện theo phong cách Modern Glassmorphism (nền tối, card mờ viền sáng, gradient sống động, navbar hỗ trợ toggle dark/light theme, hiển thị lưới thẻ môn học đẹp mắt).
  ```
* **Kết quả nhận được từ AI**:
  - Giao diện sinh ra có đầy đủ các fragment `head.html`, `navbar.html`, `footer.html`.
  - Thẻ card môn học có hiệu ứng hover mượt mà và badge trạng thái.
* **Đánh giá & Điều chỉnh của bản thân**: Tinh chỉnh lại các URL action trong form tạo môn học và binding `th:object="${course}"` đúng chuẩn Spring MVC.

---

### 🔹 GIAI ĐOẠN 3: DÙNG AI THIẾT LẬP GITHUB ACTIONS PR BOT TỰ ĐỘNG
* **Mục tiêu**: Tạo workflow `.github/workflows/pr-bot.yml` tự động kiểm tra quy chuẩn tên nhánh, title Conventional Commits bằng Tiếng Việt, chạy `mvn clean test`, tự động Approve và Squash & Merge vào `main`.
* **Câu Prompt đã dùng**:
  ```text
  Viết GitHub Actions workflow tự động chạy khi có Pull Request vào main. Workflow gồm 2 job:
  1. verify: Kiểm tra tên branch theo regex feature/..., tiêu đề PR tiếng Việt theo Conventional Commits, chạy mvn clean test.
  2. auto-approve-and-merge: Tự động gh pr review --approve, sau đó gh pr merge --squash và tự động xóa branch.
  ```
* **Kết quả nhận được từ AI**: Workflow hoàn chỉnh với full quyền `contents: write`, `pull-requests: write`.
* **Ảnh chụp màn hình**: *(Chèn ảnh chụp PR test xanh và badge Verified)*

---

## 💡 ĐÚC KẾT KINH NGHIỆM CỦA LEADER
* **AI hỗ trợ tốt nhất**: Tự động sinh boilerplate code, cấu trúc SQL migration và viết regex kiểm tra chuẩn mực CI/CD.
* **Kinh nghiệm chỉ huy**: Phải luôn phân rõ ranh giới Package trước khi cho các thành viên code thì AI mới sinh ra code khớp nhau 100%.
