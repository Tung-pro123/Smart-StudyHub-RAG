# Kế Hoạch Triển Khai Landing Page & Tái Cấu Trúc Điều Hướng (Smart StudyHub RAG)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Xây dựng trang Landing Page độc lập chuẩn thẩm mỹ hiện đại (Dark Mode, Glassmorphism) dựa trên 2 file mẫu trong `docs/reference`, đồng thời tái cấu trúc URL điều hướng thông minh (Guest -> Landing, Logged in -> Course Dashboard `/courses`).

**Architecture:** Áp dụng mô hình Spring MVC Controller + Thymeleaf View Template engine. Điều hướng tầng Controller dựa trên Session State (`currentUser`); chuyển catalog môn học về URL RESTful chuẩn `GET /courses` và dùng `/` làm cổng chào tiếp thị đón khách vãng lai.

**Tech Stack:** Spring Boot 3, Spring MVC, Thymeleaf, Tailwind CSS (CDN Play), DaisyUI v4, FontAwesome 6, Google Fonts (Plus Jakarta Sans).

**Spec:** Quyết định thiết kế qua phỏng vấn `/grill-me` ngày 2026-10-21 và tài liệu mẫu `docs/reference/code.html` + `docs/reference/screen.png`.

## Global Constraints

- Tuân thủ nguyên tắc Ponytail (giải pháp gọn gàng nhất, không thêm thư viện thừa, tận dụng tối đa CDN đã có).
- Giữ nguyên giao diện Dark Mode & Glassmorphism độc lập của Landing Page theo thiết kế `screen.png`.
- Tinh chỉnh nội dung thương hiệu từ "SaasSpace" sang "Smart StudyHub RAG" (HSF302).
- Giữ backward-compatibility: mọi đường dẫn `/dashboard` tự động redirect về `/courses`.

## Review Focus

- Trường hợp người dùng đã đăng nhập truy cập vào `/`: phải tự động chuyển tiếp mượt mà vào `/courses`.
- Trường hợp khách vãng lai (chưa đăng nhập) bấm nút "Bắt đầu" / "Đăng nhập" trên Landing Page: chuyển đúng về `/login` hoặc `/register`.
- Trường hợp người dùng đang ở trang con (ví dụ `/courses/1`) bấm logo hoặc nút "Môn học" trên Navbar: chuyển đúng về `/courses` thay vì bị đẩy ngược ra Landing Page.
- Đảm bảo Interceptor `JwtAuthInterceptor` cho phép công khai truy cập cả `/` và `/courses` đối với khách.
- Đảm bảo toàn bộ tài nguyên tĩnh (CDN CSS, Font, Icons) tải mượt mà không lỗi console.

---

### Task 1: Tạo View Template Landing Page Độc Lập (`landing.html`)

**Files:**
- Create: `src/main/resources/templates/landing.html`
- Reference: `docs/reference/code.html`

**Interfaces:**
- Consumes: Mockup data hoặc dynamic links (`@{/login}`, `@{/register}`, `@{/courses}`)
- Produces: Giao diện Landing Page chuẩn HTML5 với Dark mode, Glassmorphism, DaisyUI & Tailwind CSS

- [x] **Step 1: Tạo khung HTML và cấu hình Head**
  Thiết lập meta charset UTF-8, Google Fonts (Plus Jakarta Sans), Tailwind CSS CDN, DaisyUI 4 CDN, Font Awesome 6, cấu hình màu sắc thương hiệu `brand` và keyframes hoạt ảnh (`blob`, `float`, `pulse-glow`).

- [x] **Step 2: Xây dựng Sticky Navbar & Background Blobs**
  Tạo hiệu ứng nền phát sáng dạ quang (`blur-3xl animate-blob`) cùng thanh điều hướng `glass-card` chứa Logo "Smart StudyHub RAG", menu dẫn tới các section (#features, #about, #pricing) và cụm nút CTA (Đăng nhập / Bắt đầu học).

- [x] **Step 3: Xây dựng Hero Section & AI Assistant Mockup Window**
  Tạo tiêu đề lớn với hiệu ứng chữ Gradient `text-gradient`, mô tả giải pháp RAG, nút kêu gọi hành động (CTA), và cửa sổ mô phỏng ứng dụng (`mockup-window`) thể hiện giao diện Trợ lý RAG / Phân tích học tập.

- [x] **Step 4: Xây dựng Section Tính năng (Features) & Giá trị cốt lõi**
  Trình bày 4 cột tính năng chính: (1) RAG Question Answering theo giáo trình, (2) Auto-Chunking tài liệu PDF, (3) AI Quiz Generator tạo trắc nghiệm tức thì, (4) Study Planner học vừa sức.

- [x] **Step 5: Xây dựng Call-To-Action Cuối trang & Footer**
  Thêm khối banner kêu gọi đăng ký tài khoản miễn phí và chân trang thông tin đồ án nhóm (Group 2 - HSF302).

---

### Task 2: Điều Chỉnh Điều Hướng & Controller (`HomeController` & `CourseController`)

**Files:**
- Modify: `src/main/java/com/group2/assignment/core/controller/HomeController.java`
- Modify: `src/main/java/com/group2/assignment/core/controller/CourseController.java`

**Interfaces:**
- Consumes: `HttpSession` (kiểm tra `currentUser`), `CourseService`
- Produces: Endpoint `GET /` (Landing hoặc redirect `/courses`), `GET /dashboard` (redirect `/courses`), `GET /courses` (hiển thị danh sách môn học)

- [x] **Step 1: Cập nhật `HomeController.java`**
  - Trong phương thức `index()` tại `@GetMapping("/")`: kiểm tra nếu `session.getAttribute("currentUser") != null` thì trả về `"redirect:/courses"`, ngược lại trả về view `"landing"`.
  - Thêm endpoint `@GetMapping("/dashboard")` thực hiện `return "redirect:/courses";`.

- [x] **Step 2: Chuyển logic hiển thị danh sách môn học sang `CourseController.java`**
  - Thêm phương thức `@GetMapping` trong `CourseController` để xử lý `GET /courses`.
  - Nhận các query params `@RequestParam(value = "keyword", required = false) String keyword`, `@RequestParam(value = "semester", required = false) String semester`, nạp dữ liệu môn học vào Model và trả về view `"index"`.

---

### Task 3: Cấu Hình Bảo Mật Tuyến Đường (`WebMvcConfig`) & Đồng Bộ Navigation Links

**Files:**
- Modify: `src/main/java/com/group2/assignment/auth/config/WebMvcConfig.java`
- Modify: `src/main/resources/templates/fragments/navbar.html`
- Modify: `src/main/resources/templates/fragments/sidebar.html`
- Modify: `src/main/resources/templates/courses/detail.html`

**Interfaces:**
- Consumes: Cấu hình interceptor và các liên kết Thymeleaf
- Produces: Trải nghiệm điều hướng nhất quán không bị lỗi 403/redirect loop

- [x] **Step 1: Cập nhật `WebMvcConfig.java`**
  Đảm bảo danh sách `excludePathPatterns` bao gồm:
  `"/"`, `"/courses"`, `"/courses/*"`, `"/login"`, `"/register"`, `"/logout"`.

- [x] **Step 2: Cập nhật liên kết trong `navbar.html` & `sidebar.html`**
  Đổi liên kết tab "Môn Học" từ `th:href="@{/}"` thành `th:href="@{/courses}"`.
  Kiểm tra link Logo: Nếu đã đăng nhập trỏ về `@{/courses}`, nếu chưa trỏ về `@{/}`.

- [x] **Step 3: Cập nhật breadcrumbs trong `detail.html`**
  Cập nhật nút "Trang Chủ" / "Môn Học" trỏ về `th:href="@{/courses}"`.

---

### Task 4: Kiểm Thử Tích Hợp & Xác Minh Trực Quan

**Files:**
- Test: Kiểm tra luồng trên trình duyệt & xác thực log hệ thống.
- Created Test File: `src/test/java/com/group2/assignment/core/HomeControllerTest.java`

- [x] **Step 1: Kiểm thử luồng khách vãng lai (Guest)**
  Mở trình duyệt ở chế độ ẩn danh, truy cập `http://localhost:8080/`.
  Kỳ vọng: Hiển thị giao diện Landing Page màu tối (Dark Mode) sang trọng với đầy đủ hiệu ứng Glassmorphism.

- [x] **Step 2: Kiểm thử các nút CTA trên Landing Page**
  Click "Đăng Nhập" -> Chuyển đến `/login`.
  Click "Bắt Đầu Miễn Phí" -> Chuyển đến `/register`.
  Click "Khám Phá Môn Học" -> Chuyển đến `/courses`.

- [x] **Step 3: Kiểm thử luồng người dùng đã đăng nhập**
  Đăng nhập bằng tài khoản sinh viên. Sau khi đăng nhập thành công, truy cập `http://localhost:8080/`.
  Kỳ vọng: Tự động redirect về `http://localhost:8080/courses` (Dashboard danh sách môn học).

---

### Task 5: Tích Hợp Section Bảng Giá (Pricing Tiers) Chuẩn Cấu Trúc code.html

**Files:**
- Modify: `src/main/resources/templates/landing.html`

- [x] **Step 1: Xây dựng Layout 3 Cột (Starter, Pro Nổi Bật, Nhóm & Đồ Án)**
  Thiết lập 3 gói cước học tập: Sinh Viên Cơ Bản (0đ/tháng), Pro Ôn Thi AI (49.000đ/tháng - ruy băng "Phổ Biến Nhất", gradient border & nút CTA nổi bật), và Nhóm & Đồ Án (199.000đ/kỳ cho nhóm CapStone/Lab).

- [x] **Step 2: Kích hoạt Liên Kết Bảng Giá (#pricing)**
  Cập nhật liên kết `#pricing` vào Header Sticky Navbar, Mobile Drawer Menu và Footer chân trang.

- [x] **Step 3: Kiểm Thử Render & Toàn Vẹn Hệ Thống**
  Chạy lại bộ test tự động qua Maven đảm bảo toàn bộ 22 tests vượt qua.
