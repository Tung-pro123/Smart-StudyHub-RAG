# 06. HƯỚNG DẪN VIẾT PROMPT MẪU CHO TỪNG THÀNH VIÊN (PROMPT ENGINEERING GUIDE)
**Dự án**: **Smart Multi-Course Study Hub & RAG AI**  
**Nhóm thực hiện**: Tùng (Leader), Bình, Nhật, Thắng, Ân, Trường  

Tài liệu này cung cấp công thức câu lệnh chuẩn (Prompt Framework) và **bộ Prompts mẫu đo ni đóng giày cho đúng 6 thành viên**, giúp từng bạn dễ dàng copy, tùy biến và yêu cầu AI trong IDE sinh ra mã nguồn chính xác nhất cho module của mình.

---

## 📐 1. CÔNG THỨC PROMPT CHUẨN 5 THÀNH PHẦN (R-C-T-C-O)

Khi làm việc với các công cụ AI (Cursor, Copilot, ChatGPT, Gemini), một câu prompt rõ ràng luôn gồm 5 yếu tố:

```
[ROLE] + [CONTEXT] + [TASK] + [CONSTRAINTS] + [OUTPUT FORMAT]
```

1. **Role (Vai trò)**: Đặt vai trò chuyên gia cho AI (ví dụ: *"Bạn là Senior Spring Boot & Tailwind CSS Developer"*).
2. **Context (Bối cảnh)**: Nêu rõ dự án đang dùng: *Java 17, Spring Boot 3.3, Thymeleaf, Tailwind CSS, DaisyUI, H2 Database*.
3. **Task (Nhiệm vụ cụ thể)**: Yêu cầu rõ chức năng cần sinh ra.
4. **Constraints (Ràng buộc)**: Giới hạn những gì ĐƯỢC và KHÔNG ĐƯỢC làm (ví dụ: *"Chỉ dùng DaisyUI v4, không dùng JS ngoài, lưu key vào Session, không hardcode"*).
5. **Output Format (Định dạng)**: Yêu cầu trả về code sạch, có comment giải thích.

---

## 🎯 2. BỘ PROMPT MẪU CỤ THỂ CHO 6 THÀNH VIÊN

---

### 👑 1. DÀNH CHO TÙNG (LEADER - MODULE 1: COURSES & MASTER LAYOUT)
* **Prompt 1: Sinh Master Layout Glassmorphism bằng DaisyUI + Thymeleaf**:
  ```text
  Bạn là chuyên gia UI/UX. Tôi đang làm dự án bằng Spring Boot 3.3, Thymeleaf và DaisyUI v4.
  Hãy viết file templates/layout/main.html dùng cú pháp Thymeleaf Fragment th:fragment="main-layout(pageTitle, content)":
  1. Header có Logo "StudyHub AI", menu điều hướng nhanh, và nút đổi Theme (Light / Dark) dùng class theme-controller của DaisyUI.
  2. Sidebar dạng Drawer trượt đóng/mở được trên mobile.
  3. Áp dụng hiệu ứng kính mờ Glassmorphism (backdrop-blur-xl, nền mờ, viền mỏng).
  4. Phần giữa là thẻ <main> nhúng dynamic content từ trang con.
  Trả về code HTML chuẩn Thymeleaf không bị lỗi cú pháp.
  ```

* **Prompt 2: Tạo Entity JPA & Service CRUD Môn học**:
  ```text
  Tôi dùng Spring Data JPA và H2 Database. Hãy viết:
  1. Entity Course.java: gồm id (Long, GeneratedValue), code (HSF302), name, description, color (class Tailwind gradient), createdAt.
  2. CourseRepository.java kế thừa JpaRepository.
  3. CourseService.java: các hàm CRUD và hàm @PostConstruct initSampleData tự tạo 2 môn học mẫu HSF302 và MAS291 nếu database trống.
  ```

---

### 🌟 2. DÀNH CHO BÌNH (MODULE 2: RAG KNOWLEDGE & CHAT GIÁO TRÌNH)
* **Prompt 1: Viết hàm đọc file PDF bằng Apache PDFBox và cắt Chunks**:
  ```text
  Tôi đang dùng Spring Boot 3.3 và thư viện Apache PDFBox 3.0.3.
  Hãy viết một Service tên là PdfChunkingService.java:
  1. Nhận vào MultipartFile file PDF giáo trình và Long courseId.
  2. Dùng PDFTextStripper đọc toàn bộ nội dung văn bản trong file PDF.
  3. Viết thuật toán cắt nhỏ văn bản thành các đoạn Chunks từ 300 đến 500 từ, xử lý ngắt theo dấu chấm câu để không bị đứt đoạn câu văn.
  4. Trả về danh sách List<Chunk> chứa: chunkIndex, content, courseId, wordCount.
  ```

* **Prompt 2: Cơ chế RAG Context Injection gọi Gemini API trả lời câu hỏi**:
  ```text
  Hãy viết RagEngineService.java trong Spring Boot:
  1. Nhận vào câu hỏi của sinh viên và danh sách các Chunks của môn học đó.
  2. Tìm kiếm 3 đoạn Chunk có nội dung liên quan nhất tới câu hỏi.
  3. Đóng gói System Prompt: "Bạn là Trợ lý Giảng viên môn học. Dưới đây là các trích đoạn từ giáo trình chính thức: [TRÍCH ĐOẠN CHUNKS]. Hãy dựa vào tài liệu trên để trả lời câu hỏi của sinh viên. Nếu trong tài liệu không có, hãy thông báo rõ ràng."
  4. Dùng Spring 6 RestClient gửi HTTP POST sang Google Gemini API (gemini-1.5-flash) và trả về câu trả lời Markdown.
  ```

---

### 👤 3. DÀNH CHO ÂN (MODULE 3: CÀI ĐẶT & BẢO MẬT API KEY)
* **Prompt 1: Giao diện Form Cài Đặt API Key bảo mật bằng DaisyUI**:
  ```text
  Hãy tạo trang templates/settings.html dùng DaisyUI và Tailwind:
  1. Form nhập API Key cá nhân có nút icon mắt để bấm ẩn/hiện mật khẩu bằng JavaScript thuần.
  2. Dropdown chọn Provider: Google Gemini (gemini-1.5-flash) hoặc Groq (llama-3.3-70b).
  3. Nút "Test Connection" gửi AJAX/Fetch để kiểm tra key sống/chết.
  4. Hộp Alert DaisyUI đổi màu xanh khi kết nối thành công, màu đỏ khi key sai.
  5. Có một khung nhỏ hướng dẫn "3 bước lấy Google Gemini API Key miễn phí trong 1 phút".
  ```

* **Prompt 2: Controller lưu API Key an toàn trong HttpSession**:
  ```text
  Hãy viết SettingsController.java và ApiKeyService.java trong Spring Boot 3.3:
  1. Lưu API Key vào HttpSession để mỗi người dùng có key riêng và không bị lộ key ra file code hay database.
  2. Viết endpoint POST /settings/test-connection: gửi một request Ping nhỏ sang Gemini API, đo thời gian phản hồi (latency tính bằng ms) và trả về JSON {success: true, latencyMs: 250}.
  ```

---

### 👤 4. DÀNH CHO THẮNG (MODULE 4: AI STUDY PLANNER - LỊCH HỌC VỪA SỨC)
* **Prompt 1: Giao diện Form Study Planner & Timeline DaisyUI**:
  ```text
  Tôi muốn tạo trang templates/planner.html chia làm 2 cột bằng Tailwind + DaisyUI:
  - Cột trái: Form nhập thông tin gồm Dropdown chọn Môn học, ô nhập số giờ mỗi tuần (mặc định 6h), các nút bấm chọn Ngày rảnh trong tuần (Thứ 2, Thứ 3, ..., Chủ nhật - bấm vào đổi màu active), ô chọn Ngày thi, và ô nhập Mục tiêu học tập. Nút bấm: "Tạo kế hoạch với Gemini".
  - Cột phải: Khung hiển thị Lộ trình học tập Timeline bằng component DaisyUI Steps / Timeline hiển thị chi tiết theo từng tuần và từng ngày.
  ```

* **Prompt 2: Backend Prompt cho Gemini phân bổ lịch học theo Chunks giáo trình**:
  ```text
  Hãy viết PlannerService.java trong Spring Boot:
  Nhận vào: courseId, hoursPerWeek, selectedDays (List<String>), examDate, userGoal, và danh sách các bài học/chunks của môn đó.
  Hãy viết prompt gửi sang Gemini API yêu cầu AI đóng vai Chuyên gia Lập kế hoạch giáo dục:
  "Dựa trên các bài học sau của môn: [DANH SÁCH BÀI HỌC], sinh viên rảnh vào [CÁC NGÀY], mỗi tuần học [SỐ GIỜ], ngày thi là [NGÀY THI]. Hãy phân bổ lịch học vừa sức theo từng tuần thành định dạng JSON gồm: weekNumber, dayOfWeek, taskTitle, durationMinutes, isReviewDay."
  Bóc tách JSON và render ra Model cho Thymeleaf.
  ```

---

### 🚀 5. DÀNH CHO NHẬT (MODULE 5: AI QUIZ GENERATOR - TRẮC NGHIỆM THEO CHƯƠNG)
* **Prompt 1: Prompt yêu cầu Gemini đọc Chunks sinh Đề thi trắc nghiệm JSON**:
  ```text
  Tôi muốn viết QuizService.java trong Spring Boot 3:
  Đọc các đoạn Chunks của một chương tài liệu và gửi prompt sang Google Gemini API:
  "Dựa trên nội dung tài liệu sau: [NỘI DUNG CHUNKS], hãy tạo 5 câu hỏi trắc nghiệm khách quan với độ khó: [DỄ / TRUNG BÌNH / KHÓ].
  Yêu cầu trả về duy nhất 1 JSON Array, mỗi phần tử gồm:
  - id: số thứ tự
  - question: câu hỏi
  - options: [đáp án A, B, C, D]
  - correctAnswer: vị trí đáp án đúng (0, 1, 2 hoặc 3)
  - explanation: trích dẫn giải thích ngắn từ tài liệu vì sao đáp án đó đúng."
  ```

* **Prompt 2: Giao diện Làm bài thi trắc nghiệm & Chấm điểm tức thì bằng DaisyUI**:
  ```text
  Hãy tạo view templates/quiz.html dùng DaisyUI:
  1. Khung hiển thị lần lượt các câu hỏi trắc nghiệm, mỗi đáp án A/B/C/D dùng thẻ radio input của DaisyUI.
  2. Nút "Nộp Bài": Khi bấm nộp, JavaScript so sánh đáp án người dùng chọn với correctAnswer:
     - Câu đúng: Viền xanh lá (border-success), hiện icon check.
     - Câu sai: Viền đỏ (border-error), hiện icon xmark và tô màu đáp án đúng.
     - Mở khung giải thích trích dẫn: "Đáp án đúng vì theo tài liệu...".
     - Hiển thị tổng điểm: "Bạn đạt 8/10 điểm!".
  ```

---

### 👑 6. DÀNH CHO TRƯỜNG (MODULE 6: MASTER LEAD - USER GUIDE & SLIDE)
* **Prompt 1: Tối ưu dàn ý User Guide và kịch bản thuyết trình**:
  ```text
  Bạn là Chuyên gia Kỹ thuật Phần mềm (Technical Writer).
  Dự án của chúng tôi là Smart Multi-Course Study Hub & RAG AI gồm 5 module: Quản lý môn học, RAG Chatbot giáo trình, Quản lý API Key, AI Study Planner và AI Quiz Generator.
  Hãy soạn cho tôi:
  1. Dàn ý chi tiết cho cuốn tài liệu User Guide Word/PDF (Mục lục, Hướng dẫn cài đặt JDK 17, Hướng dẫn sử dụng từng module, Báo cáo thực nghiệm AI).
  2. Dàn ý bộ Slide PowerPoint 14 trang chuẩn cấu trúc báo cáo đại học môn HSF302.
  3. Kịch bản MC (Rehearsal Script) phân chia lượt nói và demo cho 6 bạn: Tùng, Bình, Nhật, Thắng, Ân, Trường.
  ```

---

## 💡 3. BÍ QUYẾT KHI COPY PROMPT VÀO IDE
1. Thay thế các phần trong dấu `[NGOẶC VUÔNG]` bằng thông tin thực tế của bạn.
2. Nếu AI sinh ra code dùng thư viện cũ bị lỗi, hãy nhắc AI ngay: *"Tôi đang dùng Spring Boot 3.3 và Java 17, hãy viết lại bằng thư viện mới nhất."*
3. Mỗi bạn khi nhận được kết quả tốt từ AI, nhớ chụp màn hình lại và lưu vào `docs/ai-logs/ai-log-[tên-bạn].md` để bạn Trường tổng hợp nhé!
