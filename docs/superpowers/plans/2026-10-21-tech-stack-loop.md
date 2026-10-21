# Kế Hoạch Chuyển Đổi Dải Băng Công Nghệ & Hiệu Ứng Infinite Tech Loop

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Chuyển đổi khu vực đối tác (Section c - Social Proof) từ các logo công ty chung chung (GitHub, Stripe, Slack...) về các công nghệ cốt lõi của dự án (Java 21, Spring Boot 3, Thymeleaf, Gemini AI, RAG Vector Search...) với hiệu ứng dải băng cuộn vô tận (Infinite Marquee Loop) dạng Pill Chips Glassmorphism, có Fade Mask 2 đầu và Pause on hover theo chuẩn `/ponytail`.

**Architecture:** 
- **Không dùng GSAP / React:** Dùng 100% CSS Keyframes GPU-accelerated (`transform: translate3d`) kết hợp CSS Mask Image (`linear-gradient(to right, transparent, black 10%, black 90%, transparent)`).
- **Seamless Loop:** Cấu trúc 2 flex track chạy liên tục kế tiếp nhau, đảm bảo hiệu ứng cuộn 60 FPS vĩnh viễn không bao giờ bị khựng hay giật khung hình.

**Tech Stack:** HTML5, CSS3, Tailwind CSS, FontAwesome 6, Thymeleaf, Spring Boot 3.

---

### File Structure Map

| Tệp tin | Trách nhiệm | Thao tác |
| :--- | :--- | :--- |
| `src/main/resources/templates/landing.html` | Cập nhật CSS Keyframes marquee, thay thế Section c bằng Tech Stack Loop và Fade Mask | **Modify** |
| `src/test/java/com/group2/assignment/core/HomeControllerTest.java` | Test suite kiểm tra view compilation và routing của trang landing page | **Test** |

---

## Tasks

### Task 1: Định Nghĩa CSS Keyframes Cho Infinite Marquee Loop & Fade Mask

**Files:**
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: Khối thẻ `<style>` trong `landing.html`.
- Produces: Class `.animate-marquee` và `.tech-loop-mask` chạy mượt mà trên GPU.

- [x] **Step 1: Thêm CSS rule cho dải băng cuộn vô tận**
  Trong thẻ `<style>` của `landing.html`:
  ```css
  /* Infinite Marquee Tech Loop */
  @keyframes marquee {
    0% { transform: translate3d(0, 0, 0); }
    100% { transform: translate3d(-50%, 0, 0); }
  }
  .animate-marquee {
    display: flex;
    width: max-content;
    animation: marquee 35s linear infinite;
  }
  .animate-marquee:hover {
    animation-play-state: paused;
  }
  .tech-loop-mask {
    mask-image: linear-gradient(to right, transparent, black 12%, black 88%, transparent);
    -webkit-mask-image: linear-gradient(to right, transparent, black 12%, black 88%, transparent);
  }
  ```

---

### Task 2: Tái Cấu Trúc Section c Sang "Powered By Modern Tech Stack & Enterprise AI"

**Files:**
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: Cấu trúc Section c (dòng 790 - 823 trong `landing.html`).
- Produces: Giao diện dải băng trượt vô tận hiển thị 10 công nghệ cốt lõi và AI tools dạng Pill Chips.

- [x] **Step 1: Cập nhật tiêu đề và dải băng cuộn vô tận trong `landing.html`**
  - Tiêu đề:
    ```html
    <p class="text-xs uppercase tracking-widest text-gray-400 font-semibold mb-8">
      Powered by <span class="text-white font-bold">Modern Tech Stack</span> &amp; <span class="text-transparent bg-clip-text bg-gradient-to-r from-violet-400 via-pink-400 to-indigo-400 font-extrabold">Enterprise AI</span>
    </p>
    ```
  - Danh sách 10 công nghệ cốt lõi (nhân bản 2 lần trong 1 flex track để loop liền mạch):
    1. **Java 21**: `<i class="fa-brands fa-java text-orange-400 text-lg"></i> <span>Java 21</span>`
    2. **Spring Boot 3**: `<i class="fa-solid fa-leaf text-emerald-400 text-lg"></i> <span>Spring Boot 3</span>`
    3. **Thymeleaf**: `<i class="fa-solid fa-feather-pointed text-green-400 text-lg"></i> <span>Thymeleaf SSR</span>`
    4. **Gemini 1.5 Pro**: `<i class="fa-solid fa-wand-magic-sparkles text-pink-400 text-lg"></i> <span>Gemini 1.5 Pro</span>`
    5. **RAG Vector AI**: `<i class="fa-solid fa-brain text-violet-400 text-lg"></i> <span>RAG Vector Search</span>`
    6. **PostgreSQL / H2**: `<i class="fa-solid fa-database text-blue-400 text-lg"></i> <span>PostgreSQL &amp; H2</span>`
    7. **Tailwind CSS**: `<i class="fa-brands fa-css3-alt text-cyan-400 text-lg"></i> <span>Tailwind CSS</span>`
    8. **Docker Engine**: `<i class="fa-brands fa-docker text-sky-400 text-lg"></i> <span>Docker Container</span>`
    9. **Semantic Cache**: `<i class="fa-solid fa-bolt text-amber-400 text-lg"></i> <span>Semantic Cache</span>`
    10. **Neural Embeddings**: `<i class="fa-solid fa-network-wired text-indigo-400 text-lg"></i> <span>Neural Embeddings</span>`
  - Mỗi chip bọc trong:
    ```html
    <div class="flex items-center gap-2.5 px-5 py-2.5 rounded-full glass-card border border-white/10 hover:border-violet-500/50 hover:bg-white/5 hover:scale-105 transition-all duration-300 text-sm font-semibold text-gray-300 hover:text-white cursor-pointer whitespace-nowrap shadow-lg shadow-black/20">
    ```

---

### Task 3: Kiểm Thử Tự Động & Xác Minh Toàn Vẹn Hệ Thống

**Files:**
- Test: `src/test/java/com/group2/assignment/core/HomeControllerTest.java`

- [x] **Step 1: Chạy kiểm thử tự động `HomeControllerTest`**
  ```powershell
  .\mvnw.cmd test -Dtest=HomeControllerTest
  ```
  Xác minh view `landing` biên dịch chuẩn xác không lỗi cú pháp HTML/Thymeleaf.

- [x] **Step 2: Chạy toàn bộ Test Suite của dự án**
  ```powershell
  .\mvnw.cmd test
  ```
  Xác minh toàn bộ 22/22 unit và integration tests đều đạt `BUILD SUCCESS`.

---

### Task 4: Tinh Chỉnh Giao Diện Không Viền (Borderless) & Loop Pause on Hover

**Files:**
- Modify: `src/main/resources/templates/landing.html`

- [x] **Step 1: Tháo bỏ khung viền Pill Chips & Tăng khoảng cách thoáng đãng**
  - Chuyển từ container pill `glass-card border border-white/10 px-5 py-2.5 rounded-full` sang dạng Borderless Logo phẳng.
  - Tăng khoảng cách `gap` từ `gap-4 sm:gap-6` lên `gap-12 sm:gap-16`.
  - Icon kích thước lớn `text-2xl sm:text-3xl`, text `text-lg sm:text-xl font-bold text-gray-300 hover:text-white`.
  - Icon giữ màu nhận diện thương hiệu, phóng to nhẹ `group-hover:scale-110` với `transition-transform duration-300`.
- [x] **Step 2: Tối ưu hiệu năng GPU & Tạm dừng dải băng khi Hover**
  - Giữ nguyên `.animate-marquee:hover { animation-play-state: paused; }`.
  - Thêm `will-change: transform;` và tích hợp vào `@media (prefers-reduced-motion: reduce)` để tối ưu Render Compositing và đạt chuẩn Accessibility WCAG.
- [x] **Step 3: Xác minh kiểm thử**
  - `HomeControllerTest`: 4/4 PASSED, `BUILD SUCCESS`.

