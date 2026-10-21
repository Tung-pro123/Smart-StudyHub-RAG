# Kế Hoạch Sửa Lỗi Pricing (Billing Pill, Ribbon Cutoff) & Tích Hợp PixelCard Cho Thẻ Pro

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Khắc phục triệt để 2 lỗi giao diện ở mục Pricing (Thanh gạt chu kỳ thanh toán tràn chữ/lệch viền & Ribbon "Most Popular" bị cắt nửa) và tích hợp hiệu ứng `PixelCard` (chuyển đổi từ React Bits sang Vanilla JS Canvas2D) cho thẻ **Pro**, đảm bảo tuân thủ triết lý `/ponytail` (không thêm thư viện ngoài, tối ưu hiệu năng và giữ nguyên 22/22 bài test xanh sạch).

**Architecture:** 
- **Billing Pill Slider:** Chuyển từ hardcode CSS `50%` sang dynamic DOM geometry (`offsetLeft` & `offsetWidth` của nút được chọn) kết hợp CSS transition mượt mà.
- **Pro Card Ribbon:** Tách lớp hiển thị thẻ Pro thành `overflow: visible` (cho ribbon `-top-3.5`) và lớp container nền `overflow: hidden` (cho các hiệu ứng ánh sáng/canvas).
- **PixelCard Engine:** Chuyển đổi mã nguồn React sang Vanilla JS Canvas2D (`src/main/resources/static/js/pixel-card.js`), tự quản lý vòng lặp vẽ pixel, phát hiện tương tác chuột và tự ngắt bằng `IntersectionObserver` khi cuộn ra ngoài màn hình.

**Tech Stack:** HTML5, CSS3, Tailwind CSS, Canvas 2D, JavaScript ES6+, Thymeleaf, Spring Boot 3.

---

### File Structure Map

| Tệp tin | Trách nhiệm | Thao tác |
| :--- | :--- | :--- |
| `src/main/resources/static/js/pixel-card.js` | Engine vẽ hiệu ứng PixelCard bằng Canvas2D thuần, hỗ trợ pointer tracking, fade mask và pulse effect | **Create** |
| `src/main/resources/templates/landing.html` | Cập nhật cấu trúc thanh gạt Billing Pill, tách lớp thẻ Pro để ribbon không bị cắt, mount canvas PixelCard và nhúng script | **Modify** |
| `src/test/java/com/group2/assignment/core/HomeControllerTest.java` | Test suite kiểm tra view compilation và routing của trang landing page | **Test** |

---

## Tasks

### Task 1: Sửa Lỗi Thanh Gạt Chu Kỳ Thanh Toán (Billing Pill Slider & Text Overflow)

**Files:**
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: Cấu trúc HTML của `#billing-pill-indicator`, `#billing-btn-monthly`, `#billing-btn-yearly`.
- Produces: Thanh trượt indicator ôm khít từng nút, trượt mượt mà theo kích thước thực tế mà không làm chữ bị tràn hay đè lên nhau.

- [x] **Step 1: Cập nhật CSS & HTML thanh gạt trong `landing.html`**
  - Xóa class tính cứng `w-[calc(50%-6px)]` và `left-1.5` trên `#billing-pill-indicator`.
  - Thiết lập thuộc tính khởi tạo cho `#billing-pill-indicator`:
    ```html
    <div id="billing-pill-indicator" class="absolute top-1.5 bottom-1.5 rounded-full btn-gradient shadow-md shadow-violet-500/40 transition-all duration-300 ease-out pointer-events-none z-0" style="left: 6px; width: 0;"></div>
    ```
  - Tinh chỉnh padding và hiển thị của 2 nút bấm:
    - Monthly: `px-5 sm:px-6 py-2.5 rounded-full text-xs sm:text-sm font-bold text-white transition-colors duration-200 flex items-center justify-center gap-1.5`
    - Yearly: `px-5 sm:px-6 py-2.5 rounded-full text-xs sm:text-sm font-bold text-gray-400 hover:text-gray-200 transition-colors duration-200 flex items-center justify-center gap-2`

- [x] **Step 2: Cập nhật JavaScript tính toán vị trí động (Dynamic DOM Offset)**
  - Trong block script của `landing.html`, xây dựng hàm `updatePill(btn)`:
    ```javascript
    const updatePill = (btn) => {
      if (!btn || !pillIndicator) return;
      pillIndicator.style.left = `${btn.offsetLeft}px`;
      pillIndicator.style.width = `${btn.offsetWidth}px`;
    };
    ```
  - Gọi `updatePill(btnMonthly)` khi khởi tạo trang (trong `DOMContentLoaded` hoặc sau khi DOM sẵn sàng).
  - Cập nhật trong `setBillingCycle`:
    - Khi chọn `yearly`: gọi `updatePill(btnYearly)`.
    - Khi chọn `monthly`: gọi `updatePill(btnMonthly)`.
  - Thêm lắng nghe sự kiện `window.addEventListener('resize', () => updatePill(currentBilling === 'yearly' ? btnYearly : btnMonthly))` để giữ pill luôn chuẩn xác trên mọi kích thước màn hình.

---

### Task 2: Sửa Lỗi Ribbon "Most Popular" Bị Cắt Nửa Trên Thẻ Pro

**Files:**
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: Thẻ `div` chứa gói Pro (`data-plan="pro"`), ribbon `Most Popular`, lớp `.spotlight-overlay` và `.spotlight-border`.
- Produces: Ribbon `Most Popular` nổi trọn vẹn ở đỉnh trên `-top-3.5` không bị che, trong khi nền ánh sáng vẫn bo góc chuẩn xác.

- [x] **Step 1: Cập nhật CSS cho `.spotlight-card.spotlight-pro`**
  - Đảm bảo thẻ Pro có `overflow: visible !important;` để không cắt các phần tử nhô ra ngoài biên tọa độ.
  - Thêm rule CSS:
    ```css
    .spotlight-card.spotlight-pro {
      overflow: visible;
    }
    ```

- [x] **Step 2: Tái cấu trúc cấu trúc HTML của thẻ Pro trong `landing.html`**
  - Giữ ribbon nằm ngoài lớp cắt:
    ```html
    <!-- Popular Ribbon (Full view, no clipping) -->
    <div class="absolute -top-3.5 left-1/2 -translate-x-1/2 px-4 py-1 rounded-full btn-gradient text-[11px] font-extrabold uppercase tracking-wider text-white shadow-lg shadow-violet-500/40 border border-violet-400/30 z-30 pointer-events-none">
      Most Popular
    </div>
    ```
  - Tạo container con bo góc bọc toàn bộ hiệu ứng nền và canvas:
    ```html
    <!-- Rounded Background & Effects Container (Overflow Hidden) -->
    <div class="absolute inset-0 rounded-3xl overflow-hidden pointer-events-none z-0">
      <div class="spotlight-overlay"></div>
      <div class="spotlight-border"></div>
      <canvas id="pro-pixel-canvas" class="pixel-card-canvas absolute inset-0 w-full h-full pointer-events-none"></canvas>
    </div>
    ```
  - Giữ `.spotlight-content` ở lớp `relative z-10 flex flex-col justify-between h-full` để nội dung chữ và nút bấm luôn nằm phía trên.

---

### Task 3: Chuyển Đổi & Tích Hợp Hiệu Ứng PixelCard Cho Thẻ Pro

**Files:**
- Create: `src/main/resources/static/js/pixel-card.js`
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: Mã nguồn React Bits `PixelCard`.
- Produces: Thư viện Vanilla JS thuần nhẹ, hiệu năng cao, render các hạt pixel đa sắc lấp lánh và sóng tương tác chuột cho thẻ Pro.

- [x] **Step 1: Viết script `src/main/resources/static/js/pixel-card.js`**
  - Triển khai logic Canvas2D từ React Bits sang hàm `window.initPixelCard(options)`:
    - Bảng màu mặc định: `['#e0e7ff', '#c084fc', '#ec4899', '#818cf8']`.
    - Glow: `#a855f7` (Cyber Violet).
    - `gap: 6`, `pixelSize: 2`, `speed: 35`, `fade: 0.65`, `trigger: 'hover'`.
    - Tự động bắt sự kiện `pointerenter`, `pointermove`, `pointerleave`, `pointerdown` (click pulse).
    - Tích hợp `IntersectionObserver` tự ngắt `requestAnimationFrame` khi thẻ không nằm trong viewport để tiết kiệm GPU tối đa theo triết lý `/ponytail`.
    - Tích hợp kiểm tra `prefers-reduced-motion`.

- [x] **Step 2: Nhúng script và khởi tạo PixelCard trong `landing.html`**
  - Thêm thẻ `<script th:src="@{/js/pixel-card.js}"></script>` trước khi đóng thẻ `</body>`.
  - Thêm đoạn khởi tạo trong `DOMContentLoaded`:
    ```javascript
    if (typeof window.initPixelCard === 'function') {
      window.initPixelCard({
        container: '[data-plan="pro"]',
        canvas: '#pro-pixel-canvas',
        colors: ['#e0e7ff', '#c084fc', '#ec4899', '#818cf8'],
        glowColor: '#a855f7',
        gap: 6,
        pixelSize: 2,
        speed: 35,
        fade: 0.65,
        spotlight: 0.5,
        glow: 0.45,
        trigger: 'hover',
        clickPulse: true
      });
    }
    ```

---

### Task 4: Kiểm Thử Tự Động & Xác Minh Toàn Vẹn Hệ Thống

**Files:**
- Test: `src/test/java/com/group2/assignment/core/HomeControllerTest.java`

- [x] **Step 1: Chạy kiểm thử tự động `HomeControllerTest`**
  ```powershell
  .\mvnw.cmd test -Dtest=HomeControllerTest
  ```
  Xác minh view `landing` biên dịch không có lỗi và cả 4 bài test điều hướng đều đạt `BUILD SUCCESS`.

- [x] **Step 2: Chạy toàn bộ Test Suite của dự án**
  ```powershell
  .\mvnw.cmd test
  ```
  Xác minh toàn bộ 22/22 tests của dự án đều đạt `BUILD SUCCESS`.
