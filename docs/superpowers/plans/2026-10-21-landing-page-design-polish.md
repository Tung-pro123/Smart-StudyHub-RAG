# Kế Hoạch Nâng Cấp Thiết Kế Landing Page (Design Polish to Production-Ready)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Hoàn thiện và tinh chỉnh toàn diện giao diện trang `landing.html` đạt chuẩn production-ready dựa trên phản hồi của Design Leader: tối ưu hóa khoảng trắng (Whitespace Rhythm), nâng cấp độ tương phản chuẩn WCAG (Contrast & Readability), tiết chế hiệu ứng Hero Glow, cải thiện dải logo đối tác, thêm vi tương tác (Micro-interactions) cho Navbar/Cards và thiết kế Mini UI Preview Cards cho mục Blog.

**Architecture:** Giữ vững kiến trúc Spring Boot 3 SSR với Thymeleaf và Tailwind CSS theo triết lý `/ponytail`. Không phát sinh thư viện JavaScript bên ngoài; toàn bộ vi tương tác, thẻ mô phỏng UI và hiệu ứng thị giác đều được xây dựng bằng Utility Classes của Tailwind CSS và CSS thuần tối ưu hóa trên GPU.

**Tech Stack:** HTML5, CSS3, Tailwind CSS (forms, container-queries), DaisyUI v4, FontAwesome 6, Thymeleaf, Spring Boot 3.

**Spec:** Đánh giá chi tiết (Critique) từ Design Leader và các quyết định thiết kế đã thống nhất qua `/grill-me`:
- Nâng padding các section lên `py-24 md:py-32` để tạo nhịp điệu khoảng trắng thoáng đãng.
- Tăng độ tương phản text phụ lên `text-gray-300` / `text-gray-200` đạt chuẩn WCAG AA trên nền tối.
- Tiết chế Hero Glow (`opacity-15 group-hover:opacity-30 blur-2xl`) chống lóa viền dashboard mockup.
- Phóng lớn và làm sáng dàn logo đối tác Social Proof (Trusted By).
- Thêm hiệu ứng gạch chân gradient động cho Navbar desktop và hiệu ứng nổi thẻ `hover:-translate-y-1.5` đồng nhất cho các Card.
- Chuyển đổi icon Blog thành 3 Mini UI Cards (RAG Code Preview, Bento Wireframe, Cluster Monitor).

## Global Constraints

- Giữ nguyên 100% logic định tuyến Spring Boot Thymeleaf (`@{/}`, `@{/courses}`, `@{/login}`, `@{/register}`, `th:if="${session.currentUser != null}"`).
- Giữ nguyên 100% các animation engine hiện có: LaserFlow WebGL2 full background, 3D Tilt, Glare, Floating Widgets, Counter Animation và Billing Toggle.
- Tuân thủ `/ponytail`: Không cài thêm npm dependencies hay CSS frameworks nặng nề; sử dụng các tiện ích sẵn có của Tailwind.
- Toàn bộ 22/22 tests trong test suite phải tiếp tục đạt `BUILD SUCCESS`.

## Review Focus

1. **Khả năng hiển thị chữ (Readability):** Các đoạn text phụ trong Bento Box, Pricing Cards và Testimonials không được chìm vào nền tối khi hiển thị trên các màn hình có độ sáng thấp.
2. **Không làm vỡ layout responsive:** Các section sau khi tăng padding dọc (`py-24 md:py-32`) phải giữ tỷ lệ cân đối trên cả điện thoại (sm/md) và máy tính để bàn (lg/xl).
3. **Hiệu ứng hover mượt mà (60 FPS):** Hiệu ứng `hover:-translate-y-1.5` trên các card và gạch chân trên navbar không gây giật lag hoặc layout shift.
4. **Tính toàn vẹn của Mockup:** Việc giảm Hero Glow phải làm nổi bật nội dung biểu đồ bên trong khung mà không làm mất đi cảm giác chiều sâu công nghệ.
5. **Thymeleaf Template Compilation:** Toàn bộ cú pháp HTML/Thymeleaf phải chuẩn xác, không làm gãy quá trình parse template của Spring Boot.

---

### Task 1: Nhịp Điệu Khoảng Trắng (Whitespace Rhythm) & Hệ Thống Typography

**Files:**
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: Cấu trúc HTML của các section chính trong `landing.html`.
- Produces: Nhịp điệu thị giác thông thoáng với padding dọc chuẩn mực và tiêu đề H2 nổi bật.

- [x] **Step 1: Đồng bộ padding dọc các Section chính**
  Cập nhật class padding của các section sau từ `py-16` / `py-20` lên `py-24 md:py-32`:
  - Section b (Hero): Tăng khoảng cách dưới `pb-28 md:pb-36` để tách biệt khỏi Social Proof.
  - Section c (Trusted By): `py-16 md:py-20`.
  - Section d (About Us - What Drives Us): `py-24 md:py-32`.
  - Section e (Features - Bento Box): `py-24 md:py-32`.
  - Section f (Interactive Analytics Preview): `py-24 md:py-32`.
  - Section g (Stats - Impact in Numbers): `py-24 md:py-32`.
  - Section h (Pricing Plans): `py-24 md:py-32`.
  - Section Testimonials: `py-24 md:py-32`.
  - Section Blog / Insights: `py-24 md:py-32`.
  - Section FAQ Accordion: `py-24 md:py-32`.
  - Section CTA Banner: `py-20 sm:py-28`.

- [x] **Step 2: Nâng cấp sức nặng Typography cho các Tiêu đề H2**
  Đồng bộ kích thước font chữ cho tất cả các thẻ `<h2>` của từng section sang:
  `text-4xl sm:text-5xl lg:text-6xl font-extrabold text-white tracking-tight leading-tight mt-3 mb-4`
  Đồng thời các thẻ badge tiêu đề nhỏ (`badge-outline`) tăng padding nhẹ `px-3.5 py-1.5` cho cân đối.

- [x] **Step 3: Phóng lớn nội dung trích dẫn Testimonials**
  Trong mục Testimonials, nâng cấp kích thước đoạn quote từ `text-sm text-gray-400` lên `text-base sm:text-lg text-gray-200 font-medium leading-relaxed mb-6 italic`.

---

### Task 2: Độ Tương Phản Chuẩn WCAG & Tinh Chỉnh Hero Glow / Trusted By

**Files:**
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: Các class màu text và hiệu ứng glow trong `landing.html`.
- Produces: Độ tương phản cao dễ đọc (WCAG AA > 7:1) và dàn logo Social Proof sắc nét.

- [x] **Step 1: Nâng cấp độ tương phản của Text mô tả phụ (Sub-text)**
  Tìm và thay thế các lớp màu chữ phụ bị mờ:
  - Thay `text-gray-500` và `text-gray-400` trong mô tả các card (Bento Features, Pricing feature lists, Blog excerpt) sang `text-gray-300` hoặc `text-gray-200`.
  - Đảm bảo các tiêu đề tính năng và con số thống kê dùng `text-white font-bold`.

- [x] **Step 2: Tiết chế hiệu ứng Hero Mockup Glow chống lóa**
  Tại thẻ gradient viền phát sáng của Dashboard Mockup (`#hero-tilt-card`):
  Thay thế:
  ```html
  <div class="absolute -inset-1.5 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 rounded-3xl blur-xl opacity-30 group-hover:opacity-60 transition duration-1000 group-hover:duration-200 pointer-events-none"></div>
  ```
  Thành:
  ```html
  <div class="absolute -inset-1.5 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 rounded-3xl blur-2xl opacity-15 group-hover:opacity-30 transition duration-1000 group-hover:duration-300 pointer-events-none"></div>
  ```

- [x] **Step 3: Phóng lớn và làm sáng dàn Logo đối tác (Trusted By)**
  Nâng cấp khối Social Proof:
  - Tăng khoảng cách `gap-10 sm:gap-16`.
  - Tăng kích thước icon và nhãn chữ: `<i class="fa-brands ... text-3xl"></i> <span class="text-lg font-bold">`.
  - Đổi màu từ `opacity-50 grayscale` thành `opacity-75 grayscale hover:grayscale-0 hover:opacity-100 transition-all duration-300 text-gray-400 hover:text-white`.

---

### Task 3: Vi Tương Tác (Navbar / Cards) & Thiết Kế Mini UI Blog Cards

**Files:**
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: Cấu trúc Navbar desktop, thẻ Card CSS và khối Blog thumbnail.
- Produces: Trải nghiệm tương tác tinh tế, sang trọng và hình thu nhỏ Blog chân thực.

- [x] **Step 1: Bổ sung hiệu ứng Hover Underline cho Navbar Desktop Links**
  Thêm hiệu ứng gạch chân gradient động vào các link trên header menu (`Features`, `About Us`, `Pricing`, `Testimonials`, `FAQ`, `Courses`):
  ```html
  class="relative text-sm font-medium text-gray-300 hover:text-white transition-colors duration-200 py-1 after:absolute after:bottom-0 after:left-0 after:w-0 after:h-0.5 after:bg-gradient-to-r after:from-indigo-500 after:via-violet-500 after:to-pink-500 hover:after:w-full after:transition-all after:duration-300"
  ```

- [x] **Step 2: Đồng nhất độ bo góc & Vi tương tác nổi thẻ (Card Elevation)**
  Áp dụng hiệu ứng nâng nổi đồng nhất cho các Card tính năng và Card bài viết:
  `hover:-translate-y-1.5 hover:shadow-2xl hover:shadow-indigo-500/10 hover:border-violet-500/30 transition-all duration-300 ease-out`
  Đồng thời kiểm tra tính đồng nhất của góc bo thẻ (`rounded-2xl` cho thẻ con, `rounded-3xl` cho thẻ lớn).

- [x] **Step 3: Chuyển đổi 3 Blog Thumbnails sang Mini UI Cards bằng CSS thuần**
  - **Bài 1 (AI & Tech):** Mini Code Terminal hiển thị truy vấn RAG Vector Search:
    ```html
    <div class="relative h-48 bg-slate-950/90 border-b border-white/10 p-5 font-mono text-xs flex flex-col justify-center overflow-hidden group-hover:bg-slate-900/90 transition-colors">
      <div class="flex items-center gap-1.5 mb-3 opacity-70">
        <span class="w-2.5 h-2.5 rounded-full bg-rose-500"></span>
        <span class="w-2.5 h-2.5 rounded-full bg-amber-500"></span>
        <span class="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
        <span class="ml-2 text-[10px] text-gray-400 font-sans">rag_pipeline.py</span>
      </div>
      <p class="text-violet-400"><span class="text-pink-400">await</span> vectorStore.<span class="text-cyan-300">search</span>(query)</p>
      <p class="text-emerald-400 mt-2 flex items-center gap-1.5 text-[11px]">
        <i class="fa-solid fa-check text-[10px]"></i> <span>Top match cosine: 0.984</span>
      </p>
    </div>
    ```
  - **Bài 2 (Design Systems):** Mini Bento Wireframe Layout:
    ```html
    <div class="relative h-48 bg-slate-950/90 border-b border-white/10 p-4 flex items-center justify-center overflow-hidden group-hover:bg-slate-900/90 transition-colors">
      <div class="grid grid-cols-3 gap-2 w-full max-w-[240px] opacity-80 group-hover:opacity-100 group-hover:scale-105 transition-all">
        <div class="col-span-2 h-14 rounded-xl bg-gradient-to-r from-indigo-500/20 to-purple-500/20 border border-indigo-400/30 flex items-center px-3">
          <span class="w-2 h-2 rounded-full bg-indigo-400 mr-2 animate-ping"></span>
          <span class="text-[10px] text-indigo-200 font-bold">Bento Col 2x</span>
        </div>
        <div class="col-span-1 h-14 rounded-xl bg-pink-500/20 border border-pink-400/30 flex items-center justify-center">
          <i class="fa-solid fa-shapes text-pink-300 text-xs"></i>
        </div>
        <div class="col-span-1 h-12 rounded-xl bg-cyan-500/20 border border-cyan-400/30 flex items-center justify-center">
          <i class="fa-solid fa-chart-simple text-cyan-300 text-xs"></i>
        </div>
        <div class="col-span-2 h-12 rounded-xl bg-violet-500/20 border border-violet-400/30 flex items-center px-3 justify-between">
          <span class="text-[10px] text-violet-200">Fluid Grid</span>
          <i class="fa-solid fa-check text-violet-400 text-[10px]"></i>
        </div>
      </div>
    </div>
    ```
  - **Bài 3 (Cloud Architecture):** Mini Multi-Region Sync Node Diagram:
    ```html
    <div class="relative h-48 bg-slate-950/90 border-b border-white/10 p-4 flex items-center justify-center overflow-hidden group-hover:bg-slate-900/90 transition-colors">
      <div class="flex items-center gap-3 w-full max-w-[240px] justify-between opacity-80 group-hover:opacity-100 group-hover:scale-105 transition-all">
        <div class="flex flex-col items-center gap-1.5 p-2 rounded-xl bg-slate-900 border border-emerald-500/30">
          <i class="fa-solid fa-server text-emerald-400 text-sm"></i>
          <span class="text-[9px] text-gray-300 font-bold">SG (Asia)</span>
          <span class="badge badge-xs bg-emerald-500/20 text-emerald-300 border-none text-[8px]">9ms</span>
        </div>
        <div class="flex-1 flex flex-col items-center">
          <span class="text-[8px] text-cyan-400 font-mono mb-1">Raft Sync</span>
          <div class="w-full h-0.5 bg-gradient-to-r from-emerald-500 via-cyan-400 to-indigo-500 relative">
            <span class="absolute -top-1 left-1/2 -translate-x-1/2 w-2 h-2 rounded-full bg-cyan-300 animate-pulse"></span>
          </div>
          <span class="text-[8px] text-gray-400 mt-1">Zero Loss</span>
        </div>
        <div class="flex flex-col items-center gap-1.5 p-2 rounded-xl bg-slate-900 border border-indigo-500/30">
          <i class="fa-solid fa-server text-indigo-400 text-sm"></i>
          <span class="text-[9px] text-gray-300 font-bold">FRA (EU)</span>
          <span class="badge badge-xs bg-indigo-500/20 text-indigo-300 border-none text-[8px]">14ms</span>
        </div>
      </div>
    </div>
    ```

---

### Task 4: Kiểm Thử Tự Động & Xác Minh Toàn Vẹn

**Files:**
- Test: `src/test/java/com/group2/assignment/core/HomeControllerTest.java`

- [x] **Step 1: Chạy kiểm thử tự động `HomeControllerTest`**
  Chạy lệnh:
  ```powershell
  .\mvnw.cmd test -Dtest=HomeControllerTest
  ```
  Xác minh view `landing` compile thành công và 4 bài test điều hướng đều đạt `BUILD SUCCESS`.

- [x] **Step 2: Chạy toàn bộ Test Suite của dự án**
  Chạy lệnh:
  ```powershell
  .\mvnw.cmd test
  ```
  Xác minh toàn bộ 22/22 unit và integration tests đều đạt `BUILD SUCCESS`.
