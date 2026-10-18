# 04. PHÂN CHIA NHIỆM VỤ 6 THÀNH VIÊN (MEMBER TASKS)
**Dự án**: **Smart Multi-Course Study Hub & RAG AI**  
**Nhóm thực hiện**: Tùng (Leader), Bình, Nhật, Thắng, Ân, Trường  

Tài liệu này xác định rõ vai trò, đầu việc Fullstack (Frontend + Backend), sản phẩm bàn giao (Deliverables) và tiêu chuẩn hoàn thành cho từng bạn trong nhóm.

---

## 🎯 NGUYÊN TẮC: ĐỘC LẬP HOÀN TOÀN (MODULE OWNERSHIP)
1. **Không xé lẻ tính năng**: Mỗi bạn dev sở hữu trọn vẹn 1 Module lớn từ giao diện (Thymeleaf + DaisyUI), xử lý logic, lưu trữ dữ liệu đến hiển thị kết quả. Không ai phải chờ đợi hay dẫm chân lên code của nhau.
2. **Thực nghiệm AI cá nhân**: Cả 6 thành viên đều trực tiếp dùng AI trong IDE cho công việc của mình và ghi nhật ký vào `docs/ai-logs/ai-log-[tên-bạn].md`.
3. **Trường** không ôm code tính năng để tập trung toàn lực làm Tổng quản chất lượng: User Guide, Slide, QA và Demo.

---

## 📌 BẢNG TỔNG QUAN PHÂN CÔNG 6 THÀNH VIÊN

| Thành viên | Tên Module phụ trách | Bản chất chức năng | Phạm vi Fullstack thực hiện từ A - Z |
|:---|:---|:---|:---|
| **TÙNG (Leader)** 👑 | **Courses & Layout Module** | Quản lý Môn học (CRUD) & Master Layout | Setup nền móng dự án + Layout Glassmorphism + Toàn bộ tính năng CRUD Môn học |
| **BÌNH** 🌟 | **RAG Knowledge & Chatbot Module** | Upload PDF, Chunking & Chat Giáo Trình | Upload PDF theo môn + Apache PDFBox Chunking + Khung Chat RAG & Lịch sử chat RAG |
| **ÂN** | **Settings & Security Module** | Cài đặt & Bảo mật API Key | Form Settings ẩn/hiện key + Test Connection Ping + Lưu Session an toàn |
| **THẮNG** | **AI Study Planner Module** | Lập Kế Hoạch Học Tập Vừa Sức Bằng AI | Form chọn lịch rảnh, ngày thi + Phân bổ thời gian theo Chunks giáo trình + Timeline DaisyUI |
| **NHẬT** 🚀 | **AI Quiz Generator Module** | Tự Động Tạo Đề Thi Trắc Nghiệm Theo Chương | Bộ lọc chọn chương & độ khó + Sinh Multiple Choice từ Chunks + Giao diện thi & chấm điểm |
| **TRƯỜNG** | **Master Lead & QA/Presentation** | Tổng Quản Bàn Giao & Điều Phối | Tài liệu User Guide (Word/PDF) + Slide PowerPoint + Master QA & Demo |

---

## 🔍 CHI TIẾT ĐẦU VIỆC TỪNG THÀNH VIÊN

---

### 👑 1. TÙNG (LEADER): MODULE QUẢN LÝ MÔN HỌC & MASTER LAYOUT (COURSES MODULE)
* **Trách nhiệm Fullstack trọn gói**:
  1. **Nền móng & Master Layout dùng chung**:
     * Cấu hình dự án: `pom.xml`, `application.properties`, H2 Database, Tailwind CSS + DaisyUI.
     * Xây dựng `layout.html` bằng Thymeleaf Fragments (Navbar đổi theme Dark/Light, Sidebar menu trượt, Footer Glassmorphism).
  2. **Quản lý Môn học (CRUD Courses)**:
     * Giao diện (`/courses`): Form tạo môn học mới (Mã môn, Tên môn, Mô tả, Chọn màu đại diện/icon) + Lưới hiển thị các thẻ Card môn học đã tạo.
     * Chức năng Chỉnh sửa và Xóa môn học.
     * Thống kê số lượng: Môn học này hiện có bao nhiêu tài liệu, bao nhiêu ghi chú/kế hoạch.
  3. **Backend Spring Boot**:
     * Model `Course.java`, `CourseController.java`, `CourseService.java` (lưu trữ danh sách môn học trong H2 Database).
* **AI Log cá nhân**: Prompt AI sinh Master Layout Glassmorphism và Form CRUD Môn học bằng DaisyUI (lưu vào `docs/ai-logs/ai-log-tung.md`).
* **Vấn đáp dự kiến**: *"Trình bày cách bạn quản lý danh sách Môn học và binding dữ liệu trong Thymeleaf?"*

---

### 🌟 2. BÌNH: MODULE RAG CHATBOT - TÀI LIỆU, CHUNKING & CHAT GIÁO TRÌNH
* **Trách nhiệm Fullstack trọn gói**:
  1. **Quản lý Tài liệu theo Môn** (`/courses/{courseId}/documents`):
     * Form kéo-thả (Drag & Drop) upload file PDF giáo trình/slide bài giảng vào môn học tương ứng.
     * Bảng danh sách tài liệu PDF đã upload thuộc môn học đó.
  2. **Đọc Text & Cắt Chunks**:
     * Tích hợp thư viện **Apache PDFBox** trong `pom.xml`.
     * `PdfChunkingService.java`: Trích xuất text, cắt nhỏ thành các đoạn Chunks (300-500 từ) gắn với `courseId`.
     * **Chunk Visualizer**: Accordion hiển thị các đoạn đã cắt để sinh viên bấm xem chi tiết.
  3. **Khung Chat RAG & Lịch sử Hội thoại của Môn**:
     * Cửa sổ chat riêng theo môn học (`/courses/{courseId}/chat`), hiển thị bong bóng chat DaisyUI và **lịch sử chat của môn đó**.
     * **RAG Engine**: Khi sinh viên hỏi, tìm các chunks của môn học đó, nhúng vào System Prompt gửi sang Gemini API để AI trả lời bám sát 100% tài liệu giáo trình!
* **AI Log cá nhân**: Prompt AI viết thuật toán đọc PDF, cắt chunk và cơ chế RAG Context (lưu vào `docs/ai-logs/ai-log-binh.md`).
* **Vấn đáp dự kiến**: *"Giải thích toàn bộ luồng RAG từ lúc nhận file PDF giáo trình đến khi AI trả lời câu hỏi dựa trên nội dung file đó?"*

---

### 👤 3. ÂN: MODULE CÀI ĐẶT & BẢO MẬT API KEY (SETTINGS & SECURITY)
* **Trách nhiệm Fullstack trọn gói**:
  1. **Màn hình Cài đặt (`/settings`)**:
     * Form nhập API Key cá nhân có nút con mắt ẩn/hiện mật khẩu.
     * Dropdown chọn Provider: **Google Gemini** (khuyên dùng), **Groq**, hoặc **OpenAI**.
  2. **Tính năng Kiểm tra Kết nối (Test Connection)**:
     * Nút bấm gửi thử request ping nhỏ sang API để kiểm tra key sống hay chết, đo thời gian phản hồi (ms).
     * Hộp thông báo DaisyUI Alert: Báo xanh khi kết nối thành công, báo đỏ khi key sai/hết hạn.
  3. **Backend & Hướng dẫn**:
     * `SettingsController.java`, `ApiKeyService.java`: Lưu Key an toàn trong `HttpSession` (mỗi sinh viên dùng key riêng, không bị lộ ra ngoài).
     * Tích hợp một trang hướng dẫn nhỏ ngay trên giao diện: *"Cách lấy API Key Gemini miễn phí trong 1 phút"*.
* **AI Log cá nhân**: Prompt AI viết form bảo mật mật khẩu và logic kiểm tra kết nối API Key (lưu vào `docs/ai-logs/ai-log-an.md`).
* **Vấn đáp dự kiến**: *"Tại sao bạn lại chọn lưu API Key vào HttpSession và cơ chế Test Connection hoạt động ra sao?"*

---

### 👤 4. THẮNG: MODULE AI STUDY PLANNER - LẬP KẾ HOẠCH HỌC TẬP VỪA SỨC (STUDY PLANNER)
* **Trách nhiệm Fullstack trọn gói**:
  1. **Giao diện Study Planner (`/planner`)**:
     * **Cột bên trái (Input Form)**:
       * Dropdown chọn **Môn học** (lấy từ Module 1 của Tùng).
       * Nhập **Số giờ mỗi tuần** (ví dụ: 6 tiếng).
       * Chọn **Ngày rảnh trong tuần** (dạng các nút bấm: [Thứ 2], [Thứ 3], [Thứ 4], [Thứ 5], [Thứ 6], [Thứ 7], [Chủ nhật]).
       * Ô chọn **Ngày nghỉ** & **Ngày thi cuối kỳ**.
       * Ô nhập **Mục tiêu học tập** (ví dụ: *"Nắm chắc lý thuyết và làm được bài CRUD hoàn chỉnh"*).
       * Nút bấm: **[Tạo kế hoạch với Gemini]**.
     * **Cột bên phải (Output Timeline)**:
       * Hiển thị lộ trình học phân bổ theo ngày/tuần bằng **DaisyUI Timeline / Steps Component** (Tuần 1 học phần nào, Tuần 2 thực hành phần nào, Tuần cuối ôn thi).
  2. **Backend Spring Boot**:
     * `PlannerController.java`, `PlannerService.java`.
     * Logic: Nhận thông số thời gian rảnh + Lấy danh mục các bài học từ Chunks của môn học đó (từ Module của Bình) ➔ Đóng gói gửi sang Gemini API với System Prompt chuyên gia lập kế hoạch ➔ Trả về lộ trình Timeline theo cấu trúc JSON và render lên giao diện.
* **AI Log cá nhân**: Prompt AI sinh giao diện Form chọn ngày rảnh và thuật toán phân bổ khối lượng kiến thức theo thời gian (lưu vào `docs/ai-logs/ai-log-thang.md`).
* **Vấn đáp dự kiến**: *"Cách bạn kết hợp thông tin Chunks của giáo trình với lịch rảnh của sinh viên để AI sinh ra Timeline học tập vừa sức?"*

---

### 🚀 5. NHẬT: MODULE TỰ ĐỘNG TẠO ĐỀ THI TRẮC NGHIỆM THEO CHƯƠNG (AI QUIZ GENERATOR)
* **Trách nhiệm Fullstack trọn gói**:
  1. **Bộ lọc Cá nhân hóa Đề thi (`/quiz`)**:
     * Dropdown chọn Môn học & Chọn Tài liệu / Chương muốn kiểm tra kiến thức.
     * Chọn **Độ khó**: Dễ (Nhận biết) / Trung bình (Thông hiểu) / Khó (Vận dụng code).
     * Chọn số lượng câu hỏi: 5 câu, 10 câu.
     * Nút bấm: **[Tạo Đề Thi Bằng AI]**.
  2. **Bộ máy Sinh Đề Trắc Nghiệm (Quiz Engine)**:
     * Backend gọi Gemini API đọc các Chunks trong chương đã chọn và sinh ra danh sách câu trắc nghiệm (Multiple Choice: A, B, C, D) kèm đáp án đúng và lời giải thích trích từ giáo trình.
  3. **Giao diện Làm Bài Thi & Chấm Điểm Tự Động**:
     * Hiển thị từng câu hỏi với radio button chọn đáp án A/B/C/D.
     * Nút **[Nộp Bài]**:
       * Hệ thống tự động chấm điểm và hiển thị kết quả (ví dụ: *Bạn đạt 8/10 điểm!*).
       * Đổi màu xanh lá cho câu đúng, màu đỏ cho câu sai.
       * Mở khung giải thích trích dẫn: *"Đáp án C đúng vì theo giáo trình trang 15..."*.
  4. **Backend**: `QuizController.java`, `QuizService.java` xử lý sinh đề và chấm điểm.
* **AI Log cá nhân**: Prompt AI sinh đề trắc nghiệm format JSON từ tài liệu và giao diện chấm điểm DaisyUI (lưu vào `docs/ai-logs/ai-log-nhat.md`).
* **Vấn đáp dự kiến**: *"Làm thế nào để đảm bảo các câu hỏi trắc nghiệm do AI sinh ra bám sát 100% tài liệu mà không bị bịa đặt đáp án (hallucination)?"*

---

### 👑 6. TRƯỜNG: MASTER LEAD (USER GUIDE, SLIDE POWERPOINT & DEMO)
* **Trách nhiệm làm từ A - Z**:
  1. **Chủ biên cuốn Tài liệu User Guide (Word/PDF)**:
     * Gom tài liệu và ảnh chụp code/prompts từ cả 5 module trên (của Tùng, Bình, Ân, Thắng, Nhật).
     * Soạn thảo tài liệu hoàn chỉnh: Bối cảnh, Cài đặt từ A-Z, Hướng dẫn 5 Module, Báo cáo thực nghiệm AI trong IDE.
  2. **Thiết kế bộ Slide PowerPoint (12 - 15 trang)**: Chuẩn phong cách thiết kế hiện đại, hình ảnh trực quan luồng hệ thống Đa Môn Học.
  3. **Đảm nhận vai trò Master QA Tester**:
     * Chạy thử toàn bộ luồng: *Tạo môn (Tùng) ➔ Cài key (Ân) ➔ Upload PDF & Chat RAG (Bình) ➔ Lập kế hoạch Study Planner (Thắng) ➔ Thi thử trắc nghiệm (Nhật)*.
     * Bắt lỗi phát sinh và yêu cầu các bạn dev fix trước ngày nộp.
  4. **Kịch bản Thuyết trình & Điều phối Demo**:
     * Chuẩn bị sẵn 2 file PDF bài giảng mẫu để demo toàn bộ chu trình trên lớp.
     * Làm MC mở đầu và kết luận buổi bảo vệ trước giảng viên.
* **AI Log cá nhân**: Dùng AI tối ưu dàn ý User Guide và Slide deck (lưu vào `docs/ai-logs/ai-log-truong.md`).
* **Vấn đáp dự kiến**: *"Điều phối mở đầu buổi thuyết trình, giới thiệu tổng quan hệ thống và kết luận định hướng mở rộng cho Project cuối kỳ."*
