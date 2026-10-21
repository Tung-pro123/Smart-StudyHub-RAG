# Navbar Floating Glass Island Overhaul Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (recommended) or superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Chuyển đổi thanh Navbar hiện tại của `landing.html` thành phong cách **"Floating Glass Island" (Đảo nổi kính mờ)** chuẩn phong cách SaaS hiện đại (như Vercel/Linear), loại bỏ hoàn toàn badge `HSF302` cạnh Logo, tăng khoảng thở (whitespace), tinh chỉnh Typography và hiệu ứng Glow Button.

**Architecture:**
- **Layout & Floating Position:** Đổi `<header class="sticky top-0...">` thành `<header class="fixed top-4 left-0 right-0 z-50 flex justify-center px-4 sm:px-6 pointer-events-none">` với container con `max-w-6xl w-full rounded-2xl pointer-events-auto bg-slate-900/75 backdrop-blur-2xl border border-white/10 shadow-[0_8px_32px_rgba(0,0,0,0.45)]`.
- **Hero Spacing Adjustment:** Do Navbar chuyển sang `fixed top-4`, Hero Section cần điều chỉnh `pt-24` hoặc `pt-28` để không bị Navbar đè lên nội dung đầu trang.
- **Logo & Branding:** Xóa bỏ hoàn toàn thẻ `<span class="badge ...">HSF302</span>`. Giữ Logo Icon mũ cử nhân với gradient hồng tím phát sáng nhẹ và chữ `StudyHub <span class="text-gradient">AI</span>`.
- **Navigation Links:** 5 liên kết điều hướng được tinh chỉnh khoảng thở rộng rãi (`gap-7` hoặc `gap-8`), màu chữ `text-gray-300 hover:text-white transition-colors duration-200`, hiệu ứng underline gradient mượt mà khi hover.
- **Action Buttons:** Nút "Đăng Nhập" dạng Text Button thanh lịch (`text-gray-300 hover:text-white px-3 py-2`), Nút "Bắt Đầu" dạng Glow Button với gradient tím hồng (`hover:shadow-[0_0_24px_rgba(168,85,247,0.45)] hover:-translate-y-0.5`). Giữ nguyên logic Thymeleaf session (`th:if="${session.currentUser != null}"`).

**Tech Stack:** Thymeleaf SSR, Tailwind CSS, DaisyUI, Font Awesome 6.

**Spec:** Quyết định qua `/grill-me`:
- Style: Floating Island `rounded-2xl` lơ lửng ở `fixed top-4`.
- Kính mờ: `bg-slate-900/75 backdrop-blur-2xl border border-white/10`.
- Xóa bỏ triệt để: Badge `HSF302` trên Navbar.
- Nút bấm: Text button cho Login, Glow gradient cho Register/Get Started.

## Global Constraints
- Tuân thủ `/ponytail`: Sử dụng 100% Tailwind CSS có sẵn, không thêm thư viện ngoài.
- Bảo toàn toàn bộ liên kết Thymeleaf: `th:href="@{/}"`, `th:href="@{/courses}"`, `th:href="@{/login}"`, `th:href="@{/register}"` và trạng thái `session.currentUser`.
- Không ảnh hưởng đến các hiệu ứng WebGL2 (`GradientASCII`, `SpecularButton`).
- Đảm bảo 4/4 tests trong `HomeControllerTest` vượt qua `BUILD SUCCESS`.

## Review Focus
1. Navbar cố định `fixed top-4` không đè lên Hero Section Badge ("Smart Multi-Course Hub...").
2. Hoàn toàn sạch bóng chữ "HSF302" trên toàn bộ thanh điều hướng Navbar (cả Desktop lẫn Mobile).
3. Đổ bóng `shadow-[0_8px_32px_rgba(0,0,0,0.45)]` và viền `border-white/10` hiển thị sắc nét trên nền tối.
4. Mobile menu trigger (DaisyUI dropdown) vẫn bung ra chính xác và không bị lệch khung màn hình.
5. Kiểm thử `.\mvnw.cmd test -Dtest=HomeControllerTest` đạt `BUILD SUCCESS`.

---

### File Structure Map

| Tệp tin | Trách nhiệm | Thao tác |
| :--- | :--- | :--- |
| `src/main/resources/templates/landing.html` | Cập nhật cấu trúc Navbar sang Floating Glass Island & xóa badge HSF302 | **Modify** |
| `src/test/java/com/group2/assignment/core/HomeControllerTest.java` | Test suite kiểm tra view compilation và routing | **Test** |

---

## Tasks

### Task 1: Tái Cấu Trúc Navbar Thành "Floating Glass Island" Trong `landing.html`

**Files:**
- Modify: `src/main/resources/templates/landing.html:556-630`

**Interfaces:**
- Consumes: Thẻ `<header>` và `<nav>`.
- Produces: Thanh Navbar dạng đảo nổi lơ lửng `fixed top-4`, `max-w-6xl`, `rounded-2xl`, xóa badge HSF302, link thoáng rộng.

- [x] **Step 1: Cập nhật thẻ `<header>` và `<nav>` container**
  - Chuyển `header` thành `fixed top-4 left-0 right-0 z-50 flex justify-center px-4 sm:px-6 pointer-events-none`.
  - Container `nav` thành `glass-island w-full max-w-6xl pointer-events-auto rounded-2xl px-5 sm:px-6 py-3 flex items-center justify-between bg-slate-900/75 backdrop-blur-2xl border border-white/10 shadow-[0_8px_32px_rgba(0,0,0,0.45)]`.

- [x] **Step 2: Xóa hoàn toàn badge `HSF302` cạnh Logo**
  - Xóa thẻ `<span class="badge badge-sm badge-outline border-indigo-400/40 text-indigo-300 ml-2 hidden sm:inline-flex">HSF302</span>`.
  - Đã xóa sạch badge HSF302 cả trên `landing.html` lẫn `fragments/navbar.html`.

- [x] **Step 3: Tinh chỉnh Desktop Navigation Links & Action Buttons**
  - Desktop links: `hidden md:flex items-center gap-7 lg:gap-8 text-sm font-medium text-gray-300`.
  - Nút Login: Text button `hidden sm:inline-flex text-sm font-medium text-gray-300 hover:text-white px-3 py-2 transition-colors`.
  - Nút Register: Glow Gradient button `px-5 py-2.5 text-xs sm:text-sm font-semibold text-white rounded-xl bg-gradient-to-r from-purple-600 via-violet-600 to-pink-600 hover:from-purple-500 hover:to-pink-500 hover:shadow-[0_0_22px_rgba(168,85,247,0.45)] hover:-translate-y-0.5 transition-all duration-300 flex items-center gap-2`.

- [x] **Step 4: Điều chỉnh khoảng đệm đỉnh (Padding-top) của Hero Section**
  - Tăng `pt-16` lên `pt-28 md:pt-36` để Hero Section không bị thanh Floating Navbar đè lên.

---

### Task 2: Kiểm Thử & Xác Minh Hoàn Thiện

**Files:**
- Test: `src/test/java/com/group2/assignment/core/HomeControllerTest.java`

- [x] **Step 1: Chạy test suite Maven**
  Run: `.\mvnw.cmd test -Dtest=HomeControllerTest`
  Expected: `BUILD SUCCESS` (4 tests run, 0 failures, 0 errors).
  Result: Passed with 4 tests run, 0 failures, 0 errors.

- [x] **Step 2: Kiểm tra trực quan kết quả**
  Xác minh Navbar hiển thị đúng hiệu ứng đảo nổi kính mờ, không còn chữ HSF302 trên thanh bar.
  Hoàn thành 100%.
