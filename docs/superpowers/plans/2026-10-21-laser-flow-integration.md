# Kế Hoạch Tích Hợp LaserFlow WebGL2 Vào Hero Section (Smart StudyHub AI)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Chuyển đổi component React `<LaserFlow />` từ React Bits sang thư viện Vanilla JS WebGL2 độc lập (`laser-flow.js`) và tích hợp trực tiếp vào Hero Dashboard Mockup (`#hero-tilt-card`) trên `landing.html` với hiệu ứng tia laser và bụi sáng sống động.

**Architecture:** Giữ vững kiến trúc Spring Boot 3 + Thymeleaf với tiêu chuẩn tối giản `/ponytail` (không kéo Node.js/React build pipeline). Đóng gói toàn bộ WebGL2 GLSL shaders, color tint engine, và RAF animation loop vào một thư viện tĩnh `src/main/resources/static/js/laser-flow.js`, được nạp tự động qua Spring Boot static resource handler.

**Tech Stack:** WebGL2 (GLSL 300 es), Vanilla JavaScript ES6+, CSS3, Spring Boot 3, Thymeleaf.

**Spec:** Đặc tả kỹ thuật React Bits `<LaserFlow />` và kết quả phỏng vấn `/grill-me`:
- Chuyển đổi sang Vanilla JS WebGL2 thuần (Zero dependencies).
- Bề mặt mục tiêu chiếu sáng (Target Surface): `#hero-tilt-card`.
- Bảng màu: Brand Violet/Indigo `#6366f1`.
- Tham số hiệu ứng: `beamPosition: 0.5`, `intensity: 1.8`, `flare: 1.5`, `spread: 1.5`, `spill: 1.0`, `fog: 0.2`, `dust: 1.6`, `mouseInteraction: true`.

## Global Constraints

- Không thêm bất kỳ thư viện npm hay React runtime nào vào dự án.
- Đảm bảo `pointer-events: none` trên canvas để không làm cản trở tương tác 3D Tilt và các Floating Widgets của Mockup.
- Tuân thủ cấu hình bảo mật `WebMvcConfig.java` (đường dẫn `/js/**` đã được mở cho khách vãng lai).
- Chạy kiểm thử tự động xác nhận toàn bộ 22/22 unit và integration tests đều đạt `BUILD SUCCESS`.

---

### Task 1: Xây Dựng Thư Viện Vanilla WebGL2 `laser-flow.js`

**Files:**
- Create: `src/main/resources/static/js/laser-flow.js`

**Interfaces:**
- Produces: `window.initLaserFlow(options)` trả về object `{ destroy, setPaused, updateSettings }`.

- [x] **Step 1: Khởi tạo thư mục và file `laser-flow.js`**
  Định nghĩa Vertex Shader (`#version 300 es`) và Fragment Shader (`#version 300 es`) nguyên bản từ React Bits, chứa các hàm toán học SDF `roundBox`, `spill`, `easeLight`, `noise`, `fbm`.

- [x] **Step 2: Hiện thực thuật toán chuyển đổi màu Tint HSL**
  Định nghĩa ma trận `TINTS`, các hàm `toHsv`, `fromHsv`, `buildTints` để tự động biến đổi màu `#6366f1` thành 16 phổ màu quang học hài hòa.

- [x] **Step 3: Hiện thực WebGL2 Lifecycle & Rendering Loop**
  Xây dựng hàm `initLaserFlow(options)` quản lý WebGL context, compiling, buffer quad, đo đạc tọa độ `surface` theo thời gian thực (tương thích với 3D tilt và resize), và vòng lặp RAF tự ngắt khi cuộn khỏi màn hình (IntersectionObserver).

---

### Task 2: Tích Hợp LaserFlow Vào Template `landing.html`

**Files:**
- Modify: `src/main/resources/templates/landing.html`

- [x] **Step 1: Bổ sung CSS cho LaserFlow container & canvas**
  Thêm lớp `.laser-flow-container` và `.laser-flow-canvas` với `position: absolute`, `pointer-events: none`, và `z-index` phù hợp.

- [x] **Step 2: Thêm thẻ chứa `#hero-laser-container` trên Hero Mockup**
  Đặt thẻ container bao phủ phía trên mockup `#hero-tilt-container` để tia sáng có thể rót từ đỉnh trang xuống chạm mép trên của `#hero-tilt-card`.

- [x] **Step 3: Nhúng script `th:src="@{/js/laser-flow.js}"` và kích hoạt hiệu ứng**
  Gọi `initLaserFlow` trong sự kiện `DOMContentLoaded` với các tham số đã thống nhất qua `/grill-me`.

---

### Task 3: Kiểm Thử Tự Động & Xác Minh Tích Hợp

**Files:**
- Test: `src/test/java/com/group2/assignment/core/HomeControllerTest.java`

- [x] **Step 1: Chạy kiểm thử `HomeControllerTest`**
  Xác minh view `landing` render thành công và không lỗi cú pháp Thymeleaf với asset static mới.

- [x] **Step 2: Chạy toàn bộ Test Suite dự án**
  Chạy `mvnw test` xác nhận 22/22 tests đều PASS.
