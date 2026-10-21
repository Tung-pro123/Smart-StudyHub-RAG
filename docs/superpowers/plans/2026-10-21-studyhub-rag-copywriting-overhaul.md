# Smart Multi-Course Study Hub & RAG AI Landing Page Overhaul Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (recommended) or superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Thay đổi toàn bộ nội dung (Copywriting) và trình bày tính năng (Components/Visuals) của trang `landing.html` để phản ánh đúng 100% đề tài thực tế của đồ án: **"Smart Multi-Course Study Hub & RAG AI"** (dành cho sinh viên đại học, giải quyết quá tải slide/giáo trình PDF, RAG trợ lý học tập 100% không ảo giác có trích dẫn số trang, AI Study Planner lập lịch học vừa sức, AI Quiz Generator sinh trắc nghiệm A/B/C/D từ PDF, $0 vận hành với H2 DB nhúng + Gemini API).

**Architecture:**
- **Song ngữ AI SaaS hiện đại:** Tiêu đề / Tagline tiếng Anh sắc bén phong cách Linear/Perplexity kết hợp giải thích chi tiết tiếng Việt thực tế chuẩn môn học FPTU (HSF302, MAS291, PRJ301, SWE201).
- **Interactive RAG Mockup:** Biến 3D Tilt Card cũ thành môi trường học tập thực tế chia 2 cột: Bên trái là PDF giáo trình có highlight trích đoạn & metadata số trang; Bên phải là Khung Chat AI với Citation Badges ("Nguồn: HSF302_Syllabus.pdf - Trang 14") cùng 3 nút Prompt Pills tương tác chuyển đổi kịch bản hỏi đáp mượt mà. 4 Widget nổi chuyển thành các chỉ số kỹ thuật RAG (768-dim Vectors, Zero Hallucination, $0 Server Cost, AI Quiz Ready).
- **RAG Pipeline 3 Bước (Thay thế About Us cũ):** Đưa sơ đồ 3 bước chuẩn của đồ án lên ngay sau Tech Loop (1. Ingest & Embed Apache PDFBox -> 2. Vector Retrieve H2 Database -> 3. Generate Gemini 1.5 Flash kèm Citations) để giảng viên thấy ngay kiến trúc kỹ thuật.
- **Bento Box & Data Overhaul:** Cập nhật Bento 4 ô tính năng cốt lõi, Impact in Numbers (99.9% Grounded, <1.5s Truy xuất, 10x Tốc độ ôn thi, $0 Chi phí), Bảng gói học tập (Free Sinh Viên, Pro Ôn Thi Tốc Hành, Campus / Khoa), Testimonials sinh viên và Blog chia sẻ kỹ thuật RAG.

**Tech Stack:** Java 17, Spring Boot 3, Thymeleaf SSR, Tailwind CSS, DaisyUI, Font Awesome 6, Vanilla JS.

**Spec:** Thống nhất qua `/grill-me`:
1. Ngôn ngữ: Song ngữ kết hợp (Headline English + Body/Subtitles Vietnamese).
2. Hero Mockup: Interactive RAG Mockup (Trái: PDF Viewer có highlight; Phải: Chat RAG có Citations; kèm 3 nút Prompt Pills).
3. Kiến trúc RAG: Thay thế 'About Us' bằng 'How It Works: 3-Step RAG Pipeline'.
4. Blog Section: Chuyển thành 'Study Guides & Tech Insights' (RAG Hallucination, H2 DB Vector, AI Quiz).

## Global Constraints
- Tuân thủ `/ponytail`: 100% sử dụng Tailwind CSS và Vanilla JS native, không cài thêm thư viện npm/bundle bên ngoài.
- Giữ nguyên và bảo toàn hoạt động của các component WebGL2 & Canvas đã tích hợp trước đó: `GradientASCII` (`#hero-ascii-container`), `SpecularButton` (`#hero-specular-btn`), và Infinite Marquee Tech Loop (Java 17, Gemini 1.5 Flash, Spring Boot 3).
- Toàn bộ đường dẫn Thymeleaf (`th:href="@{/courses}"`, `th:href="@{/login}"`, `th:href="@{/register}"`) và session check (`th:if="${session.currentUser != null}"`) phải giữ nguyên tính hợp lệ.
- Đảm bảo 4/4 unit tests trong `HomeControllerTest` vượt qua `BUILD SUCCESS`.

## Review Focus
1. Tương tác Prompt Pills trong Hero Mockup mượt mà, đổi câu hỏi - câu trả lời và số trang trích dẫn ngay lập tức mà không reload trang.
2. Hiển thị chuẩn trên mọi độ phân giải từ Mobile (`xs`, `sm`) đến Desktop (`lg`), không tràn khung ngang.
3. Không làm gãy bố cục 3D Tilt (`#hero-tilt-container`) và hiệu ứng đèn rọi chuột (`glare-overlay`).
4. Toàn bộ text tiếng Việt có dấu hiển thị chuẩn xác với font `Plus Jakarta Sans`.
5. Đảm bảo các chỉ số và tên môn học (HSF302, MAS291, PRJ301) thể hiện chân thực bối cảnh sinh viên đại học.

---

### File Structure Map

| Tệp tin | Trách nhiệm | Thao tác |
| :--- | :--- | :--- |
| `src/main/resources/templates/landing.html` | Cập nhật toàn bộ copywriting, interactive RAG mockup, bento cards, pipeline, numbers, pricing, testimonials | **Modify** |
| `src/test/java/com/group2/assignment/core/HomeControllerTest.java` | Test suite kiểm tra view compilation và routing | **Test** |

---

## Tasks

### Task 1: Cập Nhật Hero Section & Xây Dựng Interactive RAG Chat Mockup

**Files:**
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: `#hero-ascii-container`, `#hero-specular-btn`, `#hero-tilt-card`.
- Produces: Hero Section mới với Headline RAG ("Unlock the Hidden Knowledge in Your Courseware"), Subheading tiếng Việt giải thích RAG & Triệt tiêu ảo giác, 4 RAG Status Floating Widgets, và Fake RAG Chat UI 2 cột (PDF Reader + Chat AI có Citation Badge + 3 Prompt Pills).

- [x] **Step 1: Cập nhật Tag, Headline, Subheadline và CTA**
  - Tag: `Smart Multi-Course Study Hub & RAG AI v2.0`
  - Heading: `Unlock the Hidden Knowledge in Your <span class="text-gradient">Courseware.</span>`
  - Subheadline: `Hệ thống trợ lý học tập thông minh đa môn học. Tự động trích xuất slide & giáo trình PDF, giải đáp thắc mắc 100% bám sát tài liệu với số trang trích dẫn (Citations) — triệt tiêu hoàn toàn ảo giác (zero hallucinations).`
  - Buttons: "Bắt đầu học miễn phí" (Register) & "Khám phá môn học" (Explore Courses với SpecularButton).

- [x] **Step 2: Cập nhật 4 Floating Widgets quanh 3D Tilt Mockup thành chỉ số RAG**
  - Widget 1 (Top-Left): `768-dim Vector Embeddings` (Apache PDFBox + Neural Chunks).
  - Widget 2 (Top-Right): `100% Grounded Citations` (Trích dẫn số trang chính xác).
  - Widget 3 (Bottom-Left): `0.4s Semantic Retrieval` (H2 Database Cosine Search).
  - Widget 4 (Bottom-Right): `AI Quiz Generator Ready` (Sinh đề A/B/C/D tự động).

- [x] **Step 3: Thay thế nội dung bên trong Mockup Window thành Interactive RAG Mockup**
  - Thanh tiêu đề: `studyhub.ai/course/HSF302/rag-assistant` + Status: `PDF Indexed (48 Pages)`.
  - Cột trái: Mockup trang tài liệu `HSF302_Syllabus_SoftSkills.pdf` (Trang 14) có highlight dòng chữ vàng về "Kỹ năng phản biện và lắng nghe tích cực".
  - Cột phải: Chat box sinh viên hỏi: "Trình bày 5 bước lắng nghe tích cực theo giáo trình?", AI phản hồi với nội dung trích từ giáo trình kèm Citation Badge `[Nguồn: HSF302_Syllabus.pdf - Trang 14]`.
  - Bên dưới: 3 nút Prompt Pills tương tác:
    - Pill 1: "Lắng nghe tích cực (HSF302)"
    - Pill 2: "Cấu trúc JSP/Servlet (PRJ301)"
    - Pill 3: "Phân phối xác suất (MAS291)"
  - Script nhỏ inline chuyển đổi nội dung PDF highlight và Chat message khi click Pill.

---

### Task 2: Chuyển Đổi Section 'About Us' Thành 'How It Works: 3-Step RAG Pipeline'

**Files:**
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: Thẻ `<section id="about">`.
- Produces: Section RAG Architecture Pipeline 3 bước với visual data flow card.

- [x] **Step 1: Viết lại cấu trúc Section thành RAG Pipeline**
  - Badge: `UNDER THE HOOD ARCHITECTURE`
  - Heading: `How Our <span class="text-gradient">RAG Pipeline</span> Works`
  - Subheading: `Quy trình 3 bước khép kín biến slide bài giảng và giáo trình đồ sộ thành kho tri thức sống động, phục vụ truy vấn thời gian thực với chi phí máy chủ $0.`

- [x] **Step 2: 3 Cards cho 3 bước RAG chuẩn mực**
  - **Bước 1: Ingest & Embed (Nạp & Nhúng)**
    - Icon: `fa-file-pdf` & `fa-network-wired` (Indigo)
    - Nội dung: Tải lên tài liệu PDF môn học, Apache PDFBox tự động trích xuất text, chia nhỏ thành các đoạn Chunks thông minh (500 tokens, 10% overlap) và sinh vector nhúng 768 chiều.
  - **Bước 2: Vector Retrieve (Truy xuất Ngữ nghĩa)**
    - Icon: `fa-database` & `fa-magnifying-glass-chart` (Violet)
    - Nội dung: Khi sinh viên đặt câu hỏi, hệ thống vector hóa truy vấn và tính toán Cosine Similarity trực tiếp trên H2 Database nhúng để lọc ra top 3 đoạn văn bản liên quan nhất trong < 0.5s.
  - **Bước 3: Grounded Generate (Sinh Câu Trả Lời & Citations)**
    - Icon: `fa-wand-magic-sparkles` & `fa-quote-left` (Pink)
    - Nội dung: Gemini 1.5 Flash nhận câu hỏi cùng các đoạn ngữ cảnh trích xuất, tổng hợp câu trả lời mạch lạc và đính kèm số trang trích dẫn chính xác, cam kết không ảo giác.

---

### Task 3: Chuyển Đổi Bento Box Features Thành 4 Tính Năng Đồ Án Cốt Lõi

**Files:**
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: `<section id="features">`.
- Produces: Bento Grid 4 ô bám sát 100% tính năng của StudyHub.

- [x] **Step 1: Cập nhật tiêu đề Bento Features**
  - Badge: `CORE CAPABILITIES`
  - Heading: `Designed for University Students & <span class="text-gradient">Exam Preparation</span>`
  - Subtitle: `Không còn cảnh lạc lối giữa hàng trăm trang slide trước tuần thi Final. Tất cả công cụ học tập thông minh quy tụ trong một nền tảng duy nhất.`

- [x] **Step 2: Bento Card 1 (Ô lớn 2 cột): Semantic Search with Zero Hallucination**
  - Title: `Semantic Search, Not Just Keywords`
  - Mô tả: Tìm kiếm theo ý nghĩa ngữ cảnh thay vì khớp từng từ khóa thô sơ. Hệ thống hiểu bản chất câu hỏi ngay cả khi bạn dùng từ đồng nghĩa hoặc câu hỏi khái quát, luôn đính kèm Citation nguồn trang.
  - Mini Component: Visual Search Radar Card với mock prompt "So sánh GET và POST trong Servlet" -> Kết quả trích xuất 99.8% độ tin cậy.

- [x] **Step 3: Bento Card 2 (1 cột): Centralized Multi-Course Hub**
  - Title: `Quản Lý Đa Môn Học Tập Trung`
  - Mô tả: Không gian học tập riêng biệt cho từng môn: HSF302, MAS291, PRJ301, SWE201. Mỗi môn sở hữu kho giáo trình, vector database và lịch sử hỏi đáp độc lập.
  - Footer tag: `Separated Course Spaces`.

- [x] **Step 4: Bento Card 3 (1 cột): $0 Architecture & Local Privacy**
  - Title: `Kiến Trúc Chi Phí $0 & Bảo Mật`
  - Mô tả: Vận hành hoàn toàn bằng H2 Database nhúng Java lưu bền vững ra file đĩa và Gemini API miễn phí, không tốn tài nguyên server đám mây, dữ liệu lưu trữ an toàn.
  - Footer tag: `Embedded H2 DB • Zero Cost`.

- [x] **Step 5: Bento Card 4 (Ô lớn 2 cột): AI Study Planner & AI Quiz Generator**
  - Title: `AI Study Planner & Instant Quiz Generator`
  - Mô tả: Tự động phân tích dung lượng slide kết hợp lịch rảnh để lập thời khóa biểu học tập vừa sức; đồng thời tự động sinh đề thi trắc nghiệm A/B/C/D theo từng chương kèm giải thích chi tiết.
  - Code/Terminal visual: JSON preview của câu hỏi Quiz do AI sinh tự động từ giáo trình.

---

### Task 4: Chuyển Đổi Numbers, Pricing, Testimonials, Blog & CTA

**Files:**
- Modify: `src/main/resources/templates/landing.html`

**Interfaces:**
- Consumes: Sections `#impact`, `#pricing`, `#testimonials`, `#blog`, CTA section, footer.
- Produces: Toàn bộ nội dung học tập, bảng giá gói học, đánh giá của sinh viên các môn học.

- [x] **Step 1: Impact in Numbers**
  - Số 1: `99.9%` - Không Ảo Giác (Grounded Citations)
  - Số 2: `< 1.2s` - Tốc Độ Phản Hồi RAG
  - Số 3: `10x` - Tốc Độ Ôn Thi & Tóm Tắt Slide
  - Số 4: `$0` - Chi Phí Vận Hành Server

- [x] **Step 2: Pricing Table (Gói Học Tập Sinh Viên)**
  - Gói 1: `Sinh Viên Tự Học` ($0/tháng - 3 Môn học, RAG 100 câu hỏi/ngày, Sinh đề trắc nghiệm cơ bản).
  - Gói 2: `Ôn Thi Cấp Tốc (Pro)` (49k VNĐ / tháng - Không giới hạn môn học, RAG Gemini 1.5 Flash tốc độ cao, AI Study Planner tự động, Không giới hạn Quiz).
  - Gói 3: `Lớp Học & Giảng Viên (Campus)` (Liên hệ - Chia sẻ kho giáo trình theo lớp, Dashboard thống kê tiến độ học tập sinh viên).

- [x] **Step 3: Testimonials (Đánh Giá Sinh Viên Thật)**
  - Review 1: Sinh viên Kỹ thuật Phần mềm (SE): *"Tài liệu PRJ301 và SWE201 có quá nhiều khái niệm, nhờ RAG AI trích dẫn đúng số trang mà mình hoàn thành bài ôn tập trong 1 tiếng thay vì mất cả đêm lật slide."*
  - Review 2: Sinh viên ngành Kinh tế / Quản trị: *"Học HSF302 rất nhiều case study lý thuyết dài. Tính năng Quiz Generator sinh đề trắc nghiệm sát sạt đề thi thật giúp mình đạt điểm A môn này."*
  - Review 3: Sinh viên ngành Trí tuệ Nhân tạo: *"Kiến trúc kết hợp H2 Database và Gemini nhúng cực kỳ thông minh. Hệ thống chạy êm ru trên máy cá nhân mà không tốn một đồng chi phí hosting."*

- [x] **Step 4: Study Guides & Tech Insights (Blog)**
  - Bài 1: `Cách RAG Triệt Tiêu Hoàn Toàn Ảo Giác Khi Ôn Thi Slide 300 Trang`
  - Bài 2: `Tối Ưu Hóa Vector Search 768 Chiều Trên H2 Database Nhúng Chi Phí $0`
  - Bài 3: `Ứng Dụng Thuật Toán Spaced Repetition Và AI Lập Kế Hoạch Học Tập Vừa Sức`

- [x] **Step 5: Call To Action & Footer**
  - Headline: `Ready to Turn Your Heavy Slides into an AI Tutor?`
  - Subheadline: `Tham gia cùng hàng trăm sinh viên học tập thông minh hơn với StudyHub RAG AI ngay hôm nay.`
  - Footer: Thông tin đồ án, nhóm sinh viên phát triển Group 2, các liên kết môn học, GitHub repository.

---

### Task 5: Kiểm Thử, Xác Minh & Hoàn Thiện

**Files:**
- Test: `src/test/java/com/group2/assignment/core/HomeControllerTest.java`

- [x] **Step 1: Chạy test suite Maven**
  Run: `.\mvnw.cmd test -Dtest=HomeControllerTest`
  Expected: `BUILD SUCCESS` (4 tests run, 0 failures, 0 errors).

- [x] **Step 2: Kiểm tra trực quan tương tác trang**
  Xác minh các nút prompt pills trên Hero RAG mockup, switch Monthly/Yearly trên pricing, và các hiệu ứng WebGL2 không bị ảnh hưởng.
