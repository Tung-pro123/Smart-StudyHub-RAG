# Kế Hoạch Cập Nhật Landing Page v2 (Đồng Bộ Đầy Đủ Hiệu Ứng 3D Tilt, Glare, Particle Burst & Billing Toggle)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Đồng bộ hóa 100% mã nguồn, hiệu ứng tương tác cao cấp (3D Tilt, Specular Glare, Particle Burst, Animated Counters, Billing Switcher) từ file `docs/reference/code.html` mới vào `landing.html`, tích hợp chuẩn xác thương hiệu StudyHub AI và liên kết động Spring Boot Thymeleaf.

**Architecture:** Giữ nguyên mô hình Single-Page Landing View của Spring MVC. Nâng cấp View Layer (`landing.html`) với toàn bộ animation engine (Tailwind container-queries, requestAnimationFrame lerp 3D, IntersectionObserver, Web Audio/Canvas micro-particles) và gắn Thymeleaf dynamic routing.

**Tech Stack:** Spring Boot 3, Thymeleaf, Tailwind CSS (forms, container-queries), DaisyUI v4, FontAwesome 6, Google Fonts (Plus Jakarta Sans & Inter), Vanilla JS Animation Engine.

**Spec:** File tham khảo mới `docs/reference/code.html` (1.923 dòng) và các quyết định thiết kế từ lượt phỏng vấn `/grill-me`:
- Giữ nguyên 100% cấu trúc HTML, CSS Keyframes, hiệu ứng JavaScript và nội dung từ `code.html`.
- Tích hợp nhận diện thương hiệu StudyHub AI (HSF302) và dynamic routing Spring Boot Thymeleaf.
- Tất cả các nút chọn gói giá (Starter, Pro, Enterprise) đều trỏ đến trang Đăng ký (`@{/register}`).
- Header Navbar hiển thị nút "Vào Dashboard" (`@{/courses}`) khi đã đăng nhập (`session.currentUser != null`).

## Global Constraints

- Giữ nguyên 100% cấu trúc HTML, CSS Keyframes, và Script tương tác từ `docs/reference/code.html`.
- Giữ nguyên số tiền và chu kỳ gói cước ($19, $49, $199 kèm toggle Monthly/Yearly với data-monthly và data-yearly).
- Gắn thẻ `xmlns:th="http://www.thymeleaf.org"` và các liên kết động Thymeleaf (`@{/}`, `@{/courses}`, `@{/login}`, `@{/register}`).
- Kiểm tra toàn bộ test suite đảm bảo không phát sinh lỗi biên dịch hay hồi quy.

---

### Task 1: Nâng Cấp Template `landing.html` Từ `code.html` Mới

**Files:**
- Modify: `src/main/resources/templates/landing.html`
- Source: `docs/reference/code.html`

- [x] **Step 1: Chuẩn bị nội dung mã nguồn tích hợp Thymeleaf**
  Đọc file `docs/reference/code.html`, thêm namespace `xmlns:th="http://www.thymeleaf.org"`, gắn brand "StudyHub AI", cập nhật các nút CTA và menu đăng nhập/dashboard dựa trên `session.currentUser`.

- [x] **Step 2: Ghi đè vào `src/main/resources/templates/landing.html`**
  Cập nhật file với đầy đủ 1.948 dòng mã nguồn hoàn chỉnh không thiếu sót bất kỳ hiệu ứng nào.

---

### Task 2: Kiểm Thử Tự Động & Xác Minh Toàn Vẹn

**Files:**
- Test: `src/test/java/com/group2/assignment/core/HomeControllerTest.java`

- [x] **Step 1: Chạy kiểm thử HomeControllerTest**
  Chạy lệnh Maven xác nhận template render hợp lệ và 4 bài test điều hướng đều PASS.

- [x] **Step 2: Chạy toàn bộ test suite của dự án**
  Xác minh toàn bộ 22/22 unit và integration tests đều đạt `BUILD SUCCESS`.
