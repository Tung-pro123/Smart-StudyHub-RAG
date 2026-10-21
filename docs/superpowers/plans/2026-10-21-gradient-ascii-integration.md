# Kế Hoạch Thay Thế LaserFlow Bằng <GradientASCII /> (Vanilla WebGL2 Engine)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (recommended) or superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Loại bỏ hoàn toàn hiệu ứng nền LaserFlow (xóa script, DOM và style), thay thế bằng component `<GradientASCII />` từ React Bits được chuyển đổi sang Vanilla WebGL2 module (`gradient-ascii.js`) phục vụ cho môi trường Spring Boot SSR / Thymeleaf theo chuẩn `/ponytail` (0 dependencies, 0 bundle build, 60 FPS mượt mà).

**Architecture:**
- **Vanilla WebGL2 Engine (`gradient-ascii.js`):** Chuyển đổi mã nguồn React component thành module Vanilla JS độc lập đóng gói trong IIFE với hàm toàn cục `window.initGradientASCII(container, options)`.
- **Shader Pipeline:** Sử dụng WebGL2 GLSL 300 es với Multiple Render Targets (MRT - Field & Aux), Dynamic Glyph Atlas sinh tự động qua Canvas 2D, Halo Downsample + Gaussian Blur cho hiệu ứng phát quang (bloom glow), và Composite pass với grain dithering.
- **Tối ưu hiệu năng & Tương tác:** Capping `dpr: 1.0`, `pointer-events: none` trên canvas để không cản trở tương tác của các nút bấm và mockup trên Hero Section, trong khi vẫn lắng nghe tọa độ chuột qua `window.addEventListener('pointermove')` để tạo hiệu ứng xáo trộn ký tự ASCII dưới con trỏ chuột.
- **Cleanup Triệt Để:** Xóa bỏ tệp `src/main/resources/static/js/laser-flow.js`, dọn dẹp class CSS `.laser-flow-*` và script khởi tạo `initLaserFlow` trong `landing.html`.

**Tech Stack:** Vanilla JavaScript (ES6+), WebGL2, GLSL 300 es, HTML5 Canvas 2D Atlas, CSS3, Thymeleaf, Spring Boot 3.

**Spec:** Quyết định thiết kế qua `/grill-me`:
- Shape: `'horizon'`
- Colors: Deep Space AI `['#312e81', '#6366f1', '#c084fc', '#38bdf8']`
- Characters: `' =+xX0S8#@'`
- Cell Size: `18`
- Density: `0.75`
- Gradient Opacity: `0.6`
- Glyph Opacity: `0.7`
- Glow: `0.2`
- DPR: `1.0` (chống lag, ổn định 60 FPS)

## Global Constraints
- Không thêm bất kỳ thư viện NPM / Build tool ngoài nào (100% Native WebGL2 & Thymeleaf).
- Không ảnh hưởng đến các thành phần khác trên Hero (Header, Badge, Title, CTA Buttons, 3D Tilt Mockup, Floating Widgets).
- Đảm bảo toàn bộ 4/4 tests trong `HomeControllerTest` vượt qua với `BUILD SUCCESS`.

## Review Focus
1. Canvas tracking chuột khi `pointer-events: none` (phải lắng nghe trên `window` và tính toán relative bounds chính xác).
2. Tương thích kích thước màn hình co giãn (`ResizeObserver` cập nhật FBOs và viewport không biến dạng aspect ratio).
3. Hỗ trợ tắt chuyển động khi bật `prefers-reduced-motion` (giữ nguyên dải màu tĩnh, không flicker).
4. Dọn sạch bộ nhớ khi destroy (delete textures, framebuffers, VAO, shader programs, loseContext).
5. Tốc độ tải trang: file script tách riêng, nạp defer/cuối trang không chặn render First Contentful Paint.

---

### File Structure Map

| Tệp tin | Trách nhiệm | Thao tác |
| :--- | :--- | :--- |
| `src/main/resources/static/js/laser-flow.js` | Tệp engine cũ cần loại bỏ | **Delete** |
| `src/main/resources/static/js/gradient-ascii.js` | Engine WebGL2 tính toán trường ký tự ASCII & quang phổ Gradient | **Create** |
| `src/main/resources/templates/landing.html` | Xóa CSS/DOM/Script của LaserFlow, tích hợp GradientASCII container & khởi tạo | **Modify** |
| `src/test/java/com/group2/assignment/core/HomeControllerTest.java` | Test suite kiểm tra view compilation và routing | **Test** |

---

## Tasks

### Task 1: Xóa Bỏ Hoàn Toàn Hiệu Ứng Cũ LaserFlow

**Files:**
- Delete: `src/main/resources/static/js/laser-flow.js`
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: Khối CSS `.laser-flow-*`, thẻ `#hero-laser-container`, và thẻ `<script th:src="@{/js/laser-flow.js}">`.
- Produces: Mã nguồn sạch sẽ, không còn tàn dư của LaserFlow.

- [x] **Step 1: Xóa tệp `src/main/resources/static/js/laser-flow.js`**
  Xóa tệp khỏi thư mục static script.

- [x] **Step 2: Dọn dẹp CSS và script LaserFlow trong `landing.html`**
  - Xóa CSS `.laser-flow-container` và `.laser-flow-canvas` trong `<style>`.
  - Xóa thẻ `<script th:src="@{/js/laser-flow.js}"></script>` và khối lệnh `initLaserFlow` ở cuối file.

---

### Task 2: Xây Dựng Engine Vanilla WebGL2 `gradient-ascii.js`

**Files:**
- Create: `src/main/resources/static/js/gradient-ascii.js`

**Interfaces:**
- Consumes: Phần tử DOM container (selector hoặc HTMLElement) và object cấu hình `options`.
- Produces: Hàm toàn cục `window.initGradientASCII(container, options)` trả về `{ destroy(), updateSettings() }`.

- [x] **Step 1: Viết module Vanilla JS chuyển đổi từ React component**
  - Chuyển đổi toàn bộ VERTEX/FIELD/HALO/BLUR/COMPOSITE shaders sang GLSL 300 es.
  - Xây dựng Dynamic Font Atlas bằng Canvas 2D (`buildAtlas`).
  - Thiết lập Multiple Render Targets (MRT) cho field target (`outColor` và `outAux`).
  - Xây dựng 2 FBOs cho 2-pass horizontal & vertical Gaussian Blur tạo bloom phát quang.
  - Tối ưu tracking con trỏ chuột trên `window` và ngắt nhịp khi ẩn tab/khuất màn hình qua `IntersectionObserver`.

---

### Task 3: Tích Hợp GradientASCII Vào Hero Section Trong `landing.html`

**Files:**
- Modify: `src/main/resources/templates/landing.html`

- [x] **Step 1: Thêm CSS cho `.gradient-ascii-container` và `.gradient-ascii-canvas`**
  ```css
  /* GradientASCII Background Container & Canvas */
  .gradient-ascii-container {
    position: absolute;
    width: 100%;
    height: 100%;
    overflow: hidden;
    pointer-events: none;
  }
  .gradient-ascii-canvas {
    position: absolute;
    inset: 0;
    display: block;
    width: 100%;
    height: 100%;
    pointer-events: none;
  }
  ```

- [x] **Step 2: Thay thế DOM container ở Hero Section**
  Thay thế:
  ```html
  <div id="hero-laser-container" class="laser-flow-container pointer-events-none absolute -top-16 left-1/2 -translate-x-1/2 w-screen h-[calc(100%+8rem)] z-0 overflow-hidden"></div>
  ```
  Thành:
  ```html
  <div id="hero-ascii-container" class="gradient-ascii-container pointer-events-none absolute -top-16 left-1/2 -translate-x-1/2 w-screen h-[calc(100%+8rem)] z-0 overflow-hidden"></div>
  ```

- [x] **Step 3: Nhúng thẻ `<script>` và khởi tạo `initGradientASCII`**
  ```html
  <!-- GradientASCII WebGL2 Script & Initialization -->
  <script th:src="@{/js/gradient-ascii.js}"></script>
  <script>
    document.addEventListener('DOMContentLoaded', () => {
      if (typeof window.initGradientASCII === 'function') {
        window.initGradientASCII('#hero-ascii-container', {
          shape: 'horizon',
          colors: ['#312e81', '#6366f1', '#c084fc', '#38bdf8'],
          characters: ' =+xX0S8#@',
          cellSize: 18,
          density: 0.75,
          gradientOpacity: 0.6,
          glyphOpacity: 0.7,
          glow: 0.2,
          softness: 0.5,
          scale: 1.0,
          flow: 0.5,
          speed: 0.8,
          grain: 0.06,
          mouseInteraction: true,
          clickEffect: 'wave',
          dpr: 1.0
        });
      }
    });
  </script>
  ```

---

### Task 4: Kiểm Thử Tự Động & Xác Minh Toàn Vẹn Hệ Thống

**Files:**
- Test: `src/test/java/com/group2/assignment/core/HomeControllerTest.java`

- [x] **Step 1: Chạy kiểm thử tự động `HomeControllerTest`**
  ```powershell
  .\mvnw.cmd test -Dtest=HomeControllerTest
  ```
  Xác minh view `landing` biên dịch chuẩn xác không còn tàn dư LaserFlow và tích hợp thành công GradientASCII (4/4 tests PASS, BUILD SUCCESS).
