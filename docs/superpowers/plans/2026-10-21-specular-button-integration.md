# Kế Hoạch Tích Hợp <SpecularButton /> (Vanilla WebGL2 Edition)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Chuyển đổi component `<SpecularButton />` từ React Bits (React + WebGL2) sang thư viện thuần Vanilla JavaScript & CSS theo chuẩn `/ponytail` (0 dependencies, 0 bundle build), tích hợp vào nút Hero Secondary CTA **"Explore Courses"** trên trang `landing.html` với màu ánh sáng tím ánh bạc (`#c4b5fd`) hòa quyện vào phong cách Deep Space SaaS.

**Architecture:**
- **Vanilla WebGL2 Engine (`specular-button.js`):** Độc lập hoàn toàn, không phụ thuộc React hay external runtime. Biên dịch shader GLSL 300 es, tính toán phản xạ quang học Fresnel và đường cong bo tròn theo vị trí chuột qua `requestAnimationFrame`.
- **Zero-CPU Idle Sleep:** Chỉ kích hoạt render khi chuột nằm trong phạm vi tương tác (`proximity = 250px`). Khi chuột rời đi và hiệu ứng ổn định (settled), tự động tắt `requestAnimationFrame` về 0% tải CPU/GPU.
- **Graceful Degradation:** Tự động fallback về viền CSS thông thường nếu trình duyệt không hỗ trợ WebGL2 hoặc kích hoạt chế độ tiết kiệm chuyển động `prefers-reduced-motion`.

**Tech Stack:** Vanilla JavaScript (ES6+), WebGL2, GLSL 300 es, CSS3, Thymeleaf, Spring Boot 3.

---

### File Structure Map

| Tệp tin | Trách nhiệm | Thao tác |
| :--- | :--- | :--- |
| `src/main/resources/static/js/specular-button.js` | Engine WebGL2 tính toán vệt sáng phản quang dọc viền bo tròn theo chuột | **Create** |
| `src/main/resources/templates/landing.html` | Bổ sung CSS của SpecularButton, cập nhật nút "Explore Courses" và khởi tạo JS | **Modify** |
| `src/test/java/com/group2/assignment/core/HomeControllerTest.java` | Test suite kiểm tra view compilation và template routing | **Test** |

---

## Tasks

### Task 1: Xây Dựng Engine Vanilla WebGL2 `specular-button.js`

**Files:**
- Create: `src/main/resources/static/js/specular-button.js`

**Interfaces:**
- Consumes: Phần tử DOM `<button>` hoặc `<a>` có chứa `<canvas class="specular-button__light">`.
- Produces: Hàm toàn cục `window.initSpecularButton(target, options)` trả về `{ destroy(), updateSettings() }`.

- [x] **Step 1: Viết module Vanilla JS chuyển đổi từ React component**
  - Chuyển toàn bộ VERTEX/FRAGMENT shaders, uniform mapping, math helpers (`roundedBox`, `loopPosition`, `lobe`, `rimPoint`).
  - Thiết lập logic tracking con trỏ chuột (`pointermove`, `pointerdown`), đo đạc kích thước tự động qua `ResizeObserver`.
  - Tối ưu hiệu năng: Cắt vòng lặp render bằng `IntersectionObserver` khi nút khuất khỏi màn hình hoặc khi chuột nằm ngoài `proximity` (250px).

---

### Task 2: Định Nghĩa CSS Cho SpecularButton Trong `landing.html`

**Files:**
- Modify: `src/main/resources/templates/landing.html`

- [x] **Step 1: Thêm CSS rule cho `.specular-button` trong thẻ `<style>`**
  - Định nghĩa biến màu CSS: `--sb-radius: 12px`, `--sb-tint: #ffffff`, `--sb-tint-opacity: 0.05`, `--sb-blur: 16px`.
  - Thiết lập `isolation: isolate`, `-webkit-backdrop-filter: blur()`, và kích thước canvas mở rộng `inset: -24px`.
  - Định nghĩa trạng thái `:active` scale 0.98 mượt mà.

---

### Task 3: Cập Nhật Nút Hero Secondary CTA "Explore Courses" & Kích Hoạt Script

**Files:**
- Modify: `src/main/resources/templates/landing.html`

- [x] **Step 1: Gắn cấu trúc DOM SpecularButton vào nút "Explore Courses"**
  ```html
  <a th:href="@{/courses}" id="hero-specular-btn" class="specular-button w-full sm:w-auto px-8 py-3.5 font-semibold text-base text-gray-200 hover:text-white flex items-center justify-center gap-2">
    <canvas class="specular-button__light" aria-hidden="true"></canvas>
    <span class="specular-button__label flex items-center gap-2">
      <i class="fa-solid fa-graduation-cap text-violet-400"></i>
      <span>Explore Courses</span>
    </span>
  </a>
  ```

- [x] **Step 2: Nhúng thẻ `<script>` và gọi `initSpecularButton`**
  - Nhúng `<script th:src="@{/js/specular-button.js}"></script>` trước thẻ đóng `</body>`.
  - Cấu hình màu sắc đã thống nhất qua `/grill-me`:
    - `lineColor: '#c4b5fd'` (Violet ánh bạc)
    - `baseColor: '#334155'`
    - `intensity: 1.15`
    - `glow: 1.2`
    - `proximity: 250`
    - `radius: 12`

---

### Task 4: Kiểm Thử Tự Động & Xác Minh Toàn Vẹn Hệ Thống

**Files:**
- Test: `src/test/java/com/group2/assignment/core/HomeControllerTest.java`

- [x] **Step 1: Chạy kiểm thử tự động `HomeControllerTest`**
  ```powershell
  .\mvnw.cmd test -Dtest=HomeControllerTest
  ```
  Xác minh view `landing` biên dịch chuẩn xác không lỗi cú pháp HTML/Thymeleaf (4/4 tests PASS, BUILD SUCCESS).
