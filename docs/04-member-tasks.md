# 04. PHÂN CHIA NHIỆM VỤ CHI TIẾT 6 THÀNH VIÊN (MEMBER TASKS & IMPLEMENTATION GUIDE)
**Dự án**: **Smart Multi-Course Study Hub & RAG AI**  
**Repository**: [https://github.com/Tung-pro123/Smart-StudyHub-RAG](https://github.com/Tung-pro123/Smart-StudyHub-RAG)  
**Nhóm thực hiện**: Tùng (Leader), Bình, Nhật, Thắng, Ân, Trường  

Tài liệu này là **kim chỉ nam thực thi từ A - Z** cho từng bạn trong nhóm. Nhờ kiến trúc **Package-by-Feature**, mỗi bạn sở hữu độc lập 1 package và 1 thư mục giao diện, loại bỏ hoàn toàn khả năng conflict code khi làm việc cùng nhau!

---

## 🎯 NGUYÊN TẮC BẤT DI BẤT DỊCH (TEAM CODE OF CONDUCT)
1. **Độc lập tính năng (Feature Ownership)**: Bạn chỉ code trong package của mình và thư mục view của mình. Không tự ý sửa file của thành viên khác.
2. **Nhánh Git chuẩn**: Luôn tạo nhánh từ `main`: `feature/<tên-module>`, mở PR vào `main` với title 100% Tiếng Việt để Bot tự động duyệt và squash-merge.
3. **Thực nghiệm AI cá nhân**: Trong lúc làm, hãy lưu lại câu hỏi (Prompt) và chụp 2-3 ảnh màn hình chat với AI, copy [template.md](file:///d:/Ki5_Fall_2026/HSF302/Assignment/Assignment/docs/ai-logs/template.md) thành `docs/ai-logs/ai-log-[tên-bạn].md` để lưu lại (tiêu chí chấm điểm quan trọng của môn).

---

## 📌 BẢNG TỔNG QUAN PHÂN CÔNG VÀ RANH GIỚI MÃ NGUỒN

| Thành viên | Tên nhánh Git | Package Backend | Thư mục Giao diện | Nhiệm vụ chính |
|:---|:---|:---|:---|:---|
| **TÙNG** (Lead) 👑 | `feature/core-courses` | `com.tung.assignment.core` | `templates/`, `fragments/` | Nền móng dự án, CRUD Môn học, Layout Glassmorphism |
| **BÌNH** 🌟 | `feature/rag-engine` | `com.tung.assignment.rag` | `templates/rag/` | Upload PDF, Cắt Chunks (PDFBox), RAG Chatbot giáo trình |
| **ÂN** | `feature/settings-security`| `com.tung.assignment.settings` | `templates/settings/` | Cài đặt API Key, Nút Test Ping kết nối, Lưu Session |
| **THẮNG** | `feature/study-planner` | `com.tung.assignment.planner` | `templates/planner/` | Form chọn lịch rảnh, AI Study Planner Timeline theo Chunks |
| **NHẬT** 🚀 | `feature/quiz-generator` | `com.tung.assignment.quiz` | `templates/quiz/` | Bộ lọc độ khó, Sinh đề trắc nghiệm AI JSON, Chấm điểm tự động |
| **TRƯỜNG** | `docs/user-guide-and-slides` | `docs/` | Toàn bộ hệ thống | Tổng quản QA, Biên soạn User Guide, Thiết kế Slide & Demo |

---

## 🔍 CHI TIẾT ĐẦU VIỆC TỪNG BẠN (CÓ HƯỚNG DẪN TỪNG BƯỚC & GỢI Ý PROMPT AI)

---

### 👑 1. LEADER TÙNG: MODULE CORE & QUẢN LÝ MÔN HỌC (COURSES)
* **Nhánh làm việc**: `feature/core-courses`
* **Package phụ trách**: `com.tung.assignment.core`
* **Thư mục View**: `src/main/resources/templates/` (`index.html`, `courses/`, `fragments/`)
* **Các file Java trọng tâm**:
  - `entity/Course.java`, `entity/Chunk.java` *(đã tạo sẵn tại core)*
  - `repository/CourseRepository.java`, `repository/ChunkRepository.java`
  - `service/CourseService.java`
  - `controller/HomeController.java`, `controller/CourseController.java`
* **Các URL Endpoints phụ trách**:
  - `GET /`: Trang chủ Dashboard hiển thị danh sách các khóa học + nút tắt nhanh sang các module.
  - `GET /courses/new`: Modal / Trang form tạo môn học mới.
  - `POST /courses`: Tiếp nhận dữ liệu form (Mã môn, Tên môn, Mô tả, Màu sắc/Icon) và lưu vào Database.
  - `GET /courses/{id}/edit`: Form chỉnh sửa thông tin môn học.
  - `POST /courses/{id}/delete`: Xóa môn học (tự động xóa tài liệu và chunks liên quan).
* **Các bước triển khai**:
  1. Hoàn thiện phương thức `findAll()`, `save()`, `deleteById()` trong `CourseService.java`.
  2. Viết `CourseController.java` để đẩy danh sách môn học ra file `index.html` qua biến Thymeleaf `courses`.
  3. Xây dựng form thêm môn học bằng DaisyUI Modal hoặc form card glassmorphism.
* **Gợi ý Prompt AI cho Tùng**:
  > *"Viết CourseController trong Spring Boot 3 xử lý CRUD cho Course (hỗ trợ hiển thị danh sách, thêm mới, sửa, xóa). Sử dụng Thymeleaf Model và RedirectAttributes để gửi thông báo alert thành công."*

---

### 🌟 2. BÌNH: MODULE RAG CHATBOT - TÀI LIỆU, CHUNKING & CHAT GIÁO TRÌNH
* **Nhánh làm việc**: `feature/rag-engine`
* **Package phụ trách**: `com.tung.assignment.rag`
* **Thư mục View**: `src/main/resources/templates/rag/` (`documents.html`, `chat.html`)
* **Các file Java cần tạo/code**:
  - `entity/Document.java`, `entity/ChatSession.java`, `entity/ChatMessage.java` *(đã tạo khung sẵn)*
  - `repository/DocumentRepository.java`, `repository/ChatSessionRepository.java`, `repository/ChatMessageRepository.java`
  - `service/PdfChunkingService.java`: Dùng Apache PDFBox trích xuất văn bản từ PDF và cắt thành các `Chunk` (300-500 từ).
  - `service/RagChatService.java`: Tìm kiếm Chunks liên quan trong môn, dựng System Prompt và gọi Gemini API để trả lời.
  - `controller/DocumentController.java`: Upload và hiển thị tài liệu theo môn.
  - `controller/ChatController.java`: Giao diện chat và API gửi nhận tin nhắn.
* **Các URL Endpoints phụ trách**:
  - `GET /courses/{courseId}/documents`: Bảng danh sách tài liệu PDF đã upload của môn học đó + Accordion xem trước các đoạn Chunks đã cắt.
  - `POST /courses/{courseId}/documents/upload`: Nhận file `MultipartFile file`, lưu vào thư mục `uploads/`, gọi `PdfChunkingService` cắt chunks và lưu vào `ChunkRepository`.
  - `GET /courses/{courseId}/chat`: Màn hình chat trực quan với trợ lý môn học.
  - `POST /courses/{courseId}/chat/send`: Nhận câu hỏi từ form/AJAX, gọi `RagChatService` ghép với Chunks, gọi Gemini và trả về câu trả lời + lưu vào `ChatMessage`.
* **Các bước triển khai**:
  1. Thêm dependency `pdfbox` vào `pom.xml` (nếu chưa có).
  2. Tạo `PdfChunkingService.java` dùng `Loader.loadPDF()` và `PDFTextStripper` để lấy text, sau đó chia đoạn theo độ dài 1000-1500 ký tự.
  3. Tạo `DocumentController.java` xử lý upload file và redirect về trang xem danh sách tài liệu.
  4. Tạo giao diện `chat.html` với DaisyUI Chat Bubble (`chat-start`, `chat-end`) để sinh viên hỏi đáp kiến thức giáo trình.
* **Gợi ý Prompt AI cho Bình**:
  > *"Viết service trong Spring Boot 3 sử dụng Apache PDFBox để đọc file PDF tải lên, tách thành từng đoạn chunk khoảng 300 từ và lưu vào danh sách Entity Chunk có gắn với Course tương ứng."*
* **Câu hỏi vấn đáp dự kiến**: *"Em hãy giải thích cách thuật toán RAG lấy ngữ cảnh từ các Chunks PDF để giúp Gemini trả lời chính xác tài liệu mà không bị bịa đặt?"*

---

### 👤 3. ÂN: MODULE CÀI ĐẶT & BẢO MẬT API KEY (SETTINGS & SECURITY)
* **Nhánh làm việc**: `feature/settings-security`
* **Package phụ trách**: `com.tung.assignment.settings`
* **Thư mục View**: `src/main/resources/templates/settings/` (`index.html`)
* **Các file Java cần tạo/code**:
  - `SettingsConstants.java` *(đã tạo sẵn chứa hằng số key session)*
  - `dto/ApiKeyDto.java`: Chứa thuộc tính `apiKey`, `provider` (Gemini, Groq, OpenAI).
  - `dto/PingResponseDto.java`: Chứa `boolean success`, `long latencyMs`, `String message`.
  - `service/ApiKeyService.java`: Phương thức kiểm tra kết nối (Ping Test) sang Google Gemini API hoặc Groq.
  - `controller/SettingsController.java`: Màn hình cấu hình và các actions lưu session.
* **Các URL Endpoints phụ trách**:
  - `GET /settings`: Trang cấu hình hiển thị form nhập API Key, dropdown chọn Provider và hướng dẫn lấy key miễn phí trong 1 phút.
  - `POST /settings/save`: Lưu API Key vào `HttpSession` (`session.setAttribute(SettingsConstants.SESSION_API_KEY, key)`), báo thông báo xanh.
  - `POST /settings/test-ping`: Gửi một câu chào thử nghiệm nhỏ sang API Gemini, đo thời gian phản hồi mili-giây (`latencyMs`) và trả về kết quả Ping (Thành công / Thất bại).
* **Các bước triển khai**:
  1. Tạo file `templates/settings/index.html` với input password có nút con mắt toggle ẩn/hiện key bằng Javascript nhỏ.
  2. Viết `ApiKeyService.java` sử dụng `RestClient` của Spring Boot 3 để gửi POST request sang endpoint `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={apiKey}`.
  3. Bắt lỗi HTTP 400/403 để báo lỗi thân thiện nếu người dùng dán sai key hoặc key hết hạn.
* **Gợi ý Prompt AI cho Ân**:
  > *"Viết Spring Boot 3 Service kiểm tra API Key của Google Gemini còn sống hay không bằng cách gửi request ping nhỏ bằng RestClient, đo thời gian phản hồi ms và trả về trạng thái boolean success."*
* **Câu hỏi vấn đáp dự kiến**: *"Tại sao em lại lưu API Key vào HttpSession thay vì lưu cứng vào database hay file application.properties?"*

---

### 👤 4. THẮNG: MODULE AI STUDY PLANNER - LẬP KẾ HOẠCH HỌC TẬP VỪA SỨC
* **Nhánh làm việc**: `feature/study-planner`
* **Package phụ trách**: `com.tung.assignment.planner`
* **Thư mục View**: `src/main/resources/templates/planner/` (`index.html`, `view.html`)
* **Các file Java cần tạo/code**:
  - `entity/StudyPlan.java`, `entity/StudyTask.java` *(đã tạo khung sẵn)*
  - `repository/StudyPlanRepository.java`, `repository/StudyTaskRepository.java`
  - `dto/StudyPlanRequestDto.java`: Nhận dữ liệu form (môn học, số giờ rảnh/tuần, danh sách thứ rảnh, ngày thi, mục tiêu điểm số).
  - `service/StudyPlannerService.java`: Thu thập danh mục Chunks của môn học, đóng gói System Prompt gửi Gemini sinh Timeline học tập theo chuẩn JSON, parse vào `StudyPlan`.
  - `controller/PlannerController.java`: Xử lý form lập kế hoạch và hiển thị lộ trình Timeline.
* **Các URL Endpoints phụ trách**:
  - `GET /planner`: Giao diện Form lập kế hoạch (Cột trái) và Lịch sử các kế hoạch đã tạo (Cột phải).
  - `POST /planner/generate`: Tiếp nhận lịch rảnh của sinh viên ➔ Lấy API Key từ Session ➔ Đọc Chunks giáo trình của môn đó ➔ Gọi AI phân bổ kiến thức ➔ Lưu và chuyển hướng đến trang xem Timeline.
  - `GET /planner/{planId}`: Hiển thị lộ trình phân bổ theo ngày/tuần bằng **DaisyUI Timeline / Steps Component** (Tuần 1 học phần nào, Tuần 2 ôn tập phần nào, Tuần cuối làm đề).
  - `POST /planner/tasks/{taskId}/toggle`: Đánh dấu đã hoàn thành nhiệm vụ ôn tập đó (Checkmark).
* **Các bước triển khai**:
  1. Tạo giao diện form chọn thứ trong tuần với các checkbox/button đẹp mắt bằng DaisyUI.
  2. Viết System Prompt cho Gemini yêu cầu trả về cấu trúc JSON gồm: `weekNumber`, `topicName`, `estimatedHours`, `actionItems`.
  3. Dùng Jackson `ObjectMapper` parse JSON của Gemini và lưu vào `StudyPlanRepository` & `StudyTaskRepository`.
  4. Hiển thị ra giao diện dưới dạng Timeline trực quan.
* **Gợi ý Prompt AI cho Thắng**:
  > *"Hãy viết Prompt cho Google Gemini đóng vai trò Chuyên gia Giáo dục: Nhận vào danh sách các chương học và lịch rảnh của sinh viên (ví dụ: rảnh Thứ 3, Thứ 5, 2 tiếng/ngày, thi ngày 20/12). Yêu cầu AI sinh ra lộ trình ôn tập vừa sức chia theo từng tuần dưới dạng chuỗi JSON chuẩn."*
* **Câu hỏi vấn đáp dự kiến**: *"Cách bạn kết hợp thông tin Chunks của giáo trình với lịch rảnh của sinh viên để AI sinh ra Timeline học tập vừa sức mà không bị quá tải?"*

---

### 🚀 5. NHẬT: MODULE AI QUIZ GENERATOR - SINH ĐỀ TRẮC NGHIỆM VÀ CHẤM ĐIỂM
* **Nhánh làm việc**: `feature/quiz-generator`
* **Package phụ trách**: `com.tung.assignment.quiz`
* **Thư mục View**: `src/main/resources/templates/quiz/` (`index.html`, `take.html`, `result.html`)
* **Các file Java cần tạo/code**:
  - `entity/Quiz.java`, `entity/QuizQuestion.java`, `entity/QuizAttempt.java` *(đã tạo khung sẵn)*
  - `repository/QuizRepository.java`, `repository/QuizQuestionRepository.java`, `repository/QuizAttemptRepository.java`
  - `dto/QuizGenerateRequestDto.java`: Nhận môn học, độ khó (Dễ/Trung bình/Khó), số lượng câu hỏi (5 hoặc 10 câu).
  - `service/QuizService.java`: Gửi Chunks tài liệu môn học sang Gemini với yêu cầu sinh câu hỏi trắc nghiệm 4 đáp án (A, B, C, D) + Đáp án đúng + Trích dẫn giải thích.
  - `controller/QuizController.java`: Màn hình cấu hình sinh đề, làm bài thi và nộp bài chấm điểm.
* **Các URL Endpoints phụ trách**:
  - `GET /quiz`: Form chọn môn, chọn độ khó, chọn số lượng câu + Danh sách các đề thi đã tạo trước đó.
  - `POST /quiz/generate`: Gọi AI sinh đề thi ➔ Lưu `Quiz` và các `QuizQuestion` vào database ➔ Chuyển hướng sang trang làm bài thi.
  - `GET /quiz/{quizId}/take`: Màn hình làm bài thi trắc nghiệm trực quan với radio button cho từng phương án A, B, C, D.
  - `POST /quiz/{quizId}/submit`: Tiếp nhận đáp án của sinh viên, tự động so sánh với `correctAnswer`, tính điểm (ví dụ: 8/10) và lưu `QuizAttempt`.
  - `GET /quiz/attempts/{attemptId}`: Màn hình kết quả: Đổi màu xanh lá cho câu đúng, màu đỏ cho câu sai và mở accordion giải thích trích dẫn: *"Đáp án C đúng vì theo tài liệu trang 12..."*.
* **Các bước triển khai**:
  1. Xây dựng form sinh đề với dropdown chọn môn và selector độ khó.
  2. Viết System Prompt cho Gemini yêu cầu trả về mảng JSON câu hỏi: `[{"question": "...", "optionA": "...", "optionB": "...", "optionC": "...", "optionD": "...", "correctAnswer": "A", "explanation": "..."}]`.
  3. Xây dựng giao diện làm bài thi và thuật toán chấm điểm tự động trong `QuizService.java`.
* **Gợi ý Prompt AI cho Nhật**:
  > *"Viết System Prompt yêu cầu Gemini tạo ra 5 câu trắc nghiệm khách quan 4 phương án từ đoạn tài liệu văn bản cung cấp. Bắt buộc format đầu ra là JSON thuần túy không chứa markdown backticks, gồm các trường: question, optionA, optionB, optionC, optionD, correctAnswer, explanation."*
* **Câu hỏi vấn đáp dự kiến**: *"Làm sao em ép được Gemini chỉ trả lời bằng JSON thuần túy để Spring Boot parse được mà không bị lỗi cú pháp?"*

---

### 👑 6. TRƯỜNG: MASTER QA LEAD & USER GUIDE / POWERPOINT PRESENTATION
* **Nhánh làm việc**: `docs/user-guide-and-slides`
* **Thư mục phụ trách**: `docs/`, `src/main/resources/data/samples/`
* **Sản phẩm bàn giao cụ thể**:
  1. **Cuốn Tài liệu Báo cáo User Guide (Word / PDF)**:
     - Soạn thảo tài liệu hoàn chỉnh gồm: Giới thiệu hệ thống, Hướng dẫn cài đặt chạy từ A - Z, Chi tiết 5 Module nghiệp vụ (có hình ảnh minh họa giao diện sắc nét), Báo cáo thực nghiệm AI của 6 bạn.
  2. **Bộ Slide Thuyết trình PowerPoint (12 - 15 slides)**:
     - Thiết kế chuyên nghiệp, có logo, màu sắc đồng bộ, thể hiện luồng Đa Môn Học (Multi-Course) và các tính năng RAG, Study Planner, Quiz.
  3. **Bộ dữ liệu mẫu (Sample Dataset) để Demo trên lớp**:
     - Chuẩn bị sẵn 2 file PDF môn học thực tế (ví dụ: *Slide bài giảng JPA Spring Boot* và *Tài liệu Lập trình Java Cơ bản*).
     - Test trước toàn bộ kịch bản demo: Tạo môn ➔ Cài key ➔ Upload PDF ➔ Chat RAG ➔ Lập Study Plan ➔ Làm Quiz chấm điểm.
  4. **Kịch bản Thuyết trình (Presentation Script) & Điều phối bảo vệ**:
     - Viết lời mở đầu giới thiệu thành viên, phân công nhiệm vụ và kết luận trả lời câu hỏi của Giảng viên.
* **Gợi ý Prompt AI cho Trường**:
  > *"Hãy đóng vai trò chuyên gia viết tài liệu kỹ thuật phần mềm: Dựa trên kiến trúc Spring Boot 3 và 5 module chức năng của hệ thống Smart StudyHub RAG, hãy lập dàn ý chi tiết 15 slide thuyết trình bảo vệ đồ án trước hội đồng giảng viên."*
* **Câu hỏi vấn đáp dự kiến**: *"Em hãy điều phối trình bày tổng quan hệ thống và giải thích các bước QA kiểm thử để đảm bảo sản phẩm không bị crash khi demo?"*

---

## 🚀 QUY TRÌNH BẮT ĐẦU CHO CÁC BẠN (HÃY GỬI ĐOẠN NÀY VÀO NHÓM ZALO)
```text
Chào các bạn! Leader Tùng đã chuẩn bị xong 100% khung sườn dự án Smart StudyHub RAG.
Mọi người vào làm theo các bước sau nhé:
1. Clone repo: git clone https://github.com/Tung-pro123/Smart-StudyHub-RAG.git
2. Mở file docs/04-member-tasks.md để đọc chi tiết nhiệm vụ và hướng dẫn code của mình.
3. Tạo nhánh theo tên quy định từ main: git checkout -b feature/<tên-nhánh-của-bạn>
4. Trong quá trình dùng AI, copy file docs/ai-logs/template.md thành docs/ai-logs/ai-log-[tên-bạn].md để lưu lại nhé!
5. Khi hoàn thành, mở PR vào main với tiêu đề tiếng Việt để bot tự động merge!
```
