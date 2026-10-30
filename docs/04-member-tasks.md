# 04. PHÂN CHIA NHIỆM VỤ CHI TIẾT 6 THÀNH VIÊN (MEMBER TASKS & IMPLEMENTATION GUIDE)
**Dự án**: **Smart Multi-Course Study Hub & RAG AI Platform**  
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

| Thành viên | Tên nhánh Git | Package Backend | Thư mục Giao diện | Nhiệm vụ chính | Trạng thái |
|:---|:---|:---|:---|:---|:---:|
| **TÙNG** (Lead) 👑 | `feature/core-courses` | `com.group2.assignment.core` | `templates/`, `fragments/` | Nền móng dự án, CRUD Môn học, Layout Glassmorphism | **Hoàn thành** ✅ |
| **BÌNH** 🌟 | `feature/rag-engine` | `com.group2.assignment.rag` | `templates/rag/` | Upload PDF, Cắt Chunks (PDFBox), RAG Chatbot giáo trình | **Hoàn thành** ✅ |
| **ÂN** 📊 | `feature/admin-dashboard` | `com.group2.assignment.settings` | `templates/admin/` | Admin Hub, Thống kê KPI, Quản lý Gói cước (Free/Pro), System AI Key | **Đang nâng cấp** ⏳ |
| **THẮNG** 📅 | `feature/study-planner` | `com.group2.assignment.planner` | `templates/planner/` | Form chọn lịch rảnh, AI Study Planner Timeline theo Chunks | *Đang làm* ⏳ |
| **NHẬT** 🚀 | `feature/quiz-generator` | `com.group2.assignment.quiz` | `templates/quiz/` | Bộ lọc độ khó, Sinh đề trắc nghiệm AI JSON, Chấm điểm tự động | **Hoàn thành** ✅ |
| **TRƯỜNG** 🛡️ | `feature/auth-jwt-security` | `com.group2.assignment.auth` | `templates/auth/` | Đăng ký, Đăng nhập, Bảo mật JWT, Google OAuth2, Slide & User Guide | **Hoàn thành** ✅ |

---

## 🔍 CHI TIẾT ĐẦU VIỆC TỪNG BẠN (CÓ HƯỚNG DẪN TỪNG BƯỚC & GỢI Ý PROMPT AI)

---

### 👑 1. LEADER TÙNG: MODULE CORE & QUẢN LÝ MÔN HỌC (COURSES)
* **Nhánh làm việc**: `feature/core-courses`
* **Package phụ trách**: `com.group2.assignment.core`
* **Thư mục View**: `src/main/resources/templates/` (`index.html`, `courses/`, `fragments/`)
* **Các file Java trọng tâm**:
  - `entity/Course.java`, `entity/Chunk.java` *(đã tạo sẵn tại core)*
  - `repository/CourseRepository.java`, `repository/ChunkRepository.java`
  - `service/CourseService.java`
  - `controller/HomeController.java`, `controller/CourseController.java`
* **Các URL Endpoints phụ trách**:
  - `GET /`: Trang chủ Dashboard hiển thị danh sách các khóa học + nút tắt nhanh sang các module.
  - `GET /courses/new`: Modal / Trang form tạo môn học mới.
  - `POST /courses`: Tiếp nhận dữ liệu form (Mã môn, Tên môn, Mô tả, Màu sắc/Icon) và lưu vào Database (kiểm tra hạn ngạch tối đa 3 môn đối với gói Free).
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
* **Package phụ trách**: `com.group2.assignment.rag`
* **Thư mục View**: `src/main/resources/templates/rag/` (`documents.html`, `chat.html`)
* **Các file Java cần tạo/code**:
  - `entity/Document.java`, `entity/ChatSession.java`, `entity/ChatMessage.java`
  - `repository/DocumentRepository.java`, `repository/ChatSessionRepository.java`, `repository/ChatMessageRepository.java`
  - `service/PdfChunkingService.java`: Dùng Apache PDFBox trích xuất văn bản từ PDF và cắt thành các `Chunk` (300-500 từ).
  - `service/RagEngineService.java`: Tìm kiếm Chunks liên quan trong môn, dựng System Prompt và gọi Gemini API để trả lời (dùng Centralized System Key).
  - `controller/DocumentController.java`: Upload và hiển thị tài liệu theo môn.
  - `controller/ChatController.java`: Giao diện chat và API gửi nhận tin nhắn (kiểm soát 50 câu hỏi/ngày cho gói Free).
* **Các URL Endpoints phụ trách**:
  - `GET /courses/{courseId}/documents`: Bảng danh sách tài liệu PDF đã upload của môn học đó + Accordion xem trước các đoạn Chunks đã cắt.
  - `POST /courses/{courseId}/documents/upload`: Nhận file `MultipartFile file`, lưu vào thư mục `uploads/`, gọi `PdfChunkingService` cắt chunks và lưu vào `ChunkRepository`.
  - `GET /courses/{courseId}/chat`: Màn hình chat trực quan với trợ lý môn học.
  - `POST /courses/{courseId}/chat/send`: Nhận câu hỏi từ form/AJAX, gọi `RagEngineService` ghép với Chunks, gọi Gemini và trả về câu trả lời kèm thẻ Citations số trang.
* **Các bước triển khai**:
  1. Thêm dependency `pdfbox` vào `pom.xml`.
  2. Tạo `PdfChunkingService.java` dùng `Loader.loadPDF()` và `PDFTextStripper` để lấy text, chia đoạn theo độ dài 300-500 từ kèm số trang.
  3. Tạo `DocumentController.java` xử lý upload file và redirect về trang xem danh sách tài liệu.
  4. Tạo giao diện `chat.html` với DaisyUI Chat Bubble (`chat-start`, `chat-end`) để sinh viên hỏi đáp kiến thức giáo trình.
* **Gợi ý Prompt AI cho Bình**:
  > *"Viết service trong Spring Boot 3 sử dụng Apache PDFBox để đọc file PDF tải lên, tách thành từng đoạn chunk khoảng 300 từ và lưu vào danh sách Entity Chunk có gắn với Course tương ứng."*
* **Câu hỏi vấn đáp dự kiến**: *"Em hãy giải thích cách thuật toán RAG lấy ngữ cảnh từ các Chunks PDF để giúp Gemini trả lời chính xác tài liệu mà không bị bịa đặt?"*

---

### 📊 3. ÂN: MODULE ADMIN DASHBOARD, SYSTEM ANALYTICS & QUẢN TRỊ GÓI CƯỚC (ADMIN HUB)
* **Nhánh làm việc**: `feature/admin-dashboard`
* **Package phụ trách**: `com.group2.assignment.settings` (hoặc `com.group2.assignment.admin`)
* **Thư mục View**: `src/main/resources/templates/admin/` (`dashboard.html`, `users.html`, `settings.html`)
* **Các file Java cần tạo/code**:
  - `dto/AdminStatsDto.java`: Chứa các chỉ số thống kê KPI (`totalUsers`, `freeUsersCount`, `proUsersCount`, `totalCourses`, `totalChunks`, `todayRagQueries`, `geminiPingLatencyMs`).
  - `dto/UpdateTierRequestDto.java`: Nhận ID người dùng và hạng gói cước mới (`FREE`, `PRO`, `CAMPUS`).
  - `service/AdminAnalyticsService.java`: Truy vấn tổng hợp số liệu thống kê thời gian thực từ các Repository.
  - `service/SubscriptionQuotaService.java`: Logic kiểm tra giới hạn (3 môn, 50 câu RAG/ngày, reset lúc 00:00, khóa/mở Planner).
  - `service/SystemApiKeyService.java`: Quản lý Centralized System Key (đọc cấu hình server, Ping Test đo latency ms).
  - `controller/AdminController.java`: Màn hình thống kê, quản lý người dùng và cấu hình hệ thống.
* **Các URL Endpoints phụ trách (Chỉ dành riêng cho `ROLE_ADMIN`)**:
  - `GET /admin/dashboard`: Bảng điều khiển KPI trực quan (Tổng sinh viên, Tỷ lệ Free/Pro, Tổng môn học, Lượt truy vấn RAG trong ngày).
  - `GET /admin/users`: Bảng danh sách sinh viên, hiển thị chi tiết số câu hỏi đã dùng trong ngày và gói cước hiện tại.
  - `POST /admin/users/{id}/upgrade-pro`: Nút 1-Click nâng cấp tài khoản sinh viên từ `FREE` lên `PRO`.
  - `POST /admin/users/{id}/downgrade-free`: Nút 1-Click hạ tài khoản sinh viên về `FREE`.
  - `GET /admin/settings`: Màn hình cấu hình System API Key tập trung của hệ thống.
  - `POST /admin/settings/test-ping`: Gửi request probe kiểm tra kết nối tới Google Gemini API và đo thời gian phản hồi ms.
* **Các bước triển khai**:
  1. Xây dựng `AdminStatsDto` và viết phương thức tính toán tổng hợp trong `AdminAnalyticsService`.
  2. Tạo giao diện `dashboard.html` bằng Tailwind CSS & DaisyUI với 4 thẻ KPI Stat Card và thanh tiến trình Radial Progress thể hiện tỷ lệ % người dùng Pro.
  3. Xây dựng bảng sinh viên trong `users.html` với nút bấm chuyển đổi nhanh gói cước (kèm thông báo alert thành công).
  4. Cấu hình phân quyền để `JwtAuthInterceptor` chỉ cho phép người dùng có `role = "ROLE_ADMIN"` truy cập các URL `/admin/**`.
* **Gợi ý Prompt AI cho Ân**:
  > *"Viết AdminController trong Spring Boot 3 chỉ cho phép ROLE_ADMIN truy cập, hiển thị thống kê tổng số User (chia theo Free/Pro), tổng Course, tổng câu hỏi RAG hôm nay và cung cấp API POST để nâng cấp gói cước cho sinh viên từ FREE lên PRO."*
* **Câu hỏi vấn đáp dự kiến của Giảng viên**:
  - *"Em hãy giải thích cách phân quyền bảo vệ tuyến đường `/admin/**` để đảm bảo sinh viên thông thường không thể tự ý vào trang quản trị?"*
  - *"Hệ thống kiểm soát hạn ngạch (Quota Engine) của gói Free và Pro hoạt động như thế nào khi sinh viên gửi câu hỏi thứ 51 trong ngày?"*

---

### 👤 4. THẮNG: MODULE AI STUDY PLANNER - LẬP KẾ HOẠCH HỌC TẬP VỪA SỨC
* **Nhánh làm việc**: `feature/study-planner`
* **Package phụ trách**: `com.group2.assignment.planner`
* **Thư mục View**: `src/main/resources/templates/planner/` (`index.html`, `view.html`)
* **Các file Java cần tạo/code**:
  - `entity/StudyPlan.java`, `entity/StudyTask.java`
  - `repository/StudyPlanRepository.java`, `repository/StudyTaskRepository.java`
  - `dto/StudyPlanRequestDto.java`: Nhận dữ liệu form (môn học, số giờ rảnh/tuần, danh sách thứ rảnh, ngày thi, mục tiêu điểm số).
  - `service/StudyPlannerService.java`: Thu thập danh mục Chunks của môn học, đóng gói System Prompt gửi Gemini sinh Timeline học tập theo chuẩn JSON, parse vào `StudyPlan`.
  - `controller/PlannerController.java`: Xử lý form lập kế hoạch và hiển thị lộ trình Timeline (kiểm tra quyền truy cập Gói Pro).
* **Các URL Endpoints phụ trách**:
  - `GET /planner`: Giao diện Form lập kế hoạch (Cột trái) và Lịch sử các kế hoạch đã tạo (Cột phải). Khóa hiển thị nếu là gói Free.
  - `POST /planner/generate`: Tiếp nhận lịch rảnh của sinh viên ➔ Lấy System API Key ➔ Đọc Chunks giáo trình của môn đó ➔ Gọi AI phân bổ kiến thức ➔ Lưu và chuyển hướng đến trang xem Timeline.
  - `GET /planner/{planId}`: Hiển thị lộ trình phân bổ theo ngày/tuần bằng **DaisyUI Timeline / Steps Component**.
  - `POST /planner/tasks/{taskId}/toggle`: Đánh dấu đã hoàn thành nhiệm vụ ôn tập đó (Checkmark).
* **Các bước triển khai**:
  1. Tạo giao diện form chọn thứ trong tuần với các checkbox/button đẹp mắt bằng DaisyUI.
  2. Viết System Prompt cho Gemini yêu cầu trả về cấu trúc JSON gồm: `weekNumber`, `topicName`, `estimatedHours`, `actionItems`.
  3. Dùng Jackson `ObjectMapper` parse JSON của Gemini và lưu vào `StudyPlanRepository` & `StudyTaskRepository`.
  4. Hiển thị ra giao diện dưới dạng Timeline trực quan.
* **Gợi ý Prompt AI cho Thắng**:
  > *"Hãy viết Prompt cho Google Gemini đóng vai trò Chuyên gia Giáo dục: Nhận vào danh sách các chương học và lịch rảnh của sinh viên. Yêu cầu AI sinh ra lộ trình ôn tập vừa sức chia theo từng tuần dưới dạng chuỗi JSON chuẩn."*
* **Câu hỏi vấn đáp dự kiến**: *"Cách bạn kết hợp thông tin Chunks của giáo trình với lịch rảnh của sinh viên để AI sinh ra Timeline học tập vừa sức mà không bị quá tải?"*

---

### 🚀 5. NHẬT: MODULE AI QUIZ GENERATOR - SINH ĐỀ TRẮC NGHIỆM VÀ CHẤM ĐIỂM
* **Nhánh làm việc**: `feature/quiz-generator`
* **Package phụ trách**: `com.group2.assignment.quiz`
* **Thư mục View**: `src/main/resources/templates/quiz/` (`index.html`, `take.html`, `result.html`)
* **Các file Java cần tạo/code**:
  - `entity/Quiz.java`, `entity/QuizQuestion.java`, `entity/QuizAttempt.java`
  - `repository/QuizRepository.java`, `repository/QuizQuestionRepository.java`, `repository/QuizAttemptRepository.java`
  - `dto/QuizGenerateRequestDto.java`: Nhận môn học, độ khó (Dễ/Trung bình/Khó), số lượng câu hỏi (5 hoặc 10 câu).
  - `service/QuizService.java`: Gửi Chunks tài liệu môn học sang Gemini với yêu cầu sinh câu hỏi trắc nghiệm 4 đáp án (A, B, C, D) + Đáp án đúng + Trích dẫn giải thích.
  - `controller/QuizController.java`: Màn hình cấu hình sinh đề, làm bài thi và nộp bài chấm điểm.
* **Các URL Endpoints phụ trách**:
  - `GET /quiz`: Form chọn môn, chọn độ khó, chọn số lượng câu + Danh sách các đề thi đã tạo trước đó.
  - `POST /quiz/generate`: Gọi AI sinh đề thi ➔ Lưu `Quiz` và các `QuizQuestion` vào database ➔ Chuyển hướng sang trang làm bài thi.
  - `GET /quiz/{quizId}/take`: Màn hình làm bài thi trắc nghiệm trực quan với radio button cho từng phương án A, B, C, D kèm đồng hồ đếm ngược.
  - `POST /quiz/{quizId}/submit`: Tiếp nhận đáp án của sinh viên, tự động so sánh với `correctAnswer`, tính điểm và lưu `QuizAttempt`.
  - `GET /quiz/attempts/{attemptId}`: Màn hình kết quả: Đổi màu xanh lá cho câu đúng, màu đỏ cho câu sai và mở accordion giải thích trích dẫn.
* **Các bước triển khai**:
  1. Xây dựng form sinh đề với dropdown chọn môn và selector độ khó.
  2. Viết System Prompt cho Gemini yêu cầu trả về mảng JSON câu hỏi.
  3. Xây dựng giao diện làm bài thi và thuật toán chấm điểm tự động trong `QuizService.java`.
* **Gợi ý Prompt AI cho Nhật**:
  > *"Viết System Prompt yêu cầu Gemini tạo ra 5 câu trắc nghiệm khách quan 4 phương án từ đoạn tài liệu văn bản cung cấp. Bắt buộc format đầu ra là JSON thuần túy không chứa markdown backticks, gồm các trường: question, optionA, optionB, optionC, optionD, correctAnswer, explanation."*
* **Câu hỏi vấn đáp dự kiến**: *"Làm sao em ép được Gemini chỉ trả lời bằng JSON thuần túy để Spring Boot parse được mà không bị lỗi cú pháp?"*

---

### 🛡️ 6. TRƯỜNG: MODULE XÁC THỰC, JWT SECURITY & CÁ NHÂN HÓA NGƯỜI DÙNG (AUTH & JWT MODULE)
* **Nhánh làm việc**: `feature/auth-jwt-security`
* **Package phụ trách**: `com.group2.assignment.auth`
* **Thư mục View**: `src/main/resources/templates/auth/` (`login.html`, `register.html`, `profile.html`)
* **Các file Java trọng tâm**:
  - `entity/User.java`: Bổ sung `subscriptionTier`, `dailyRagCount`, `lastRagResetDate`.
  - `repository/UserRepository.java`: Tìm kiếm người dùng theo username/email và kiểm tra trùng lặp.
  - `dto/LoginRequestDto.java`, `dto/RegisterRequestDto.java`: Nhận dữ liệu form đăng nhập / đăng ký.
  - `service/JwtService.java`: Bộ máy sinh và xác thực JSON Web Token chuẩn thư viện JJWT 0.12.x (`generateToken`, `validateToken`, `extractUsername`).
  - `service/AuthService.java`: Băm mật khẩu bằng thuật toán an toàn BCrypt, sinh JWT token đăng nhập và nạp sẵn tài khoản demo (`admin` / `123456`, `student` / `123456`, `pro_student` / `123456`).
  - `controller/AuthController.java`: Điều hướng đăng ký, đăng nhập (lưu token vào Cookie `STUDYHUB_JWT` và Session), đăng xuất và xem hồ sơ cá nhân.
* **Các URL Endpoints phụ trách**:
  - `GET /login`: Giao diện đăng nhập Glassmorphism (có nút con mắt toggle mật khẩu và hộp gợi ý tài khoản demo).
  - `POST /login`: Xác thực thông tin, tạo JWT Token, lưu vào HttpOnly Cookie và chuyển hướng về trang chủ hoặc `authRedirectUrl`.
  - `GET /register`: Form đăng ký tài khoản sinh viên mới (gán mặc định `subscriptionTier = "FREE"`).
  - `POST /register`: Mã hóa mật khẩu BCrypt, lưu tài khoản vào cơ sở dữ liệu và chuyển hướng sang `/login` với thông báo xanh.
  - `GET /profile`: Trang hồ sơ cá nhân hiển thị ảnh đại diện, email, quyền hạn (`ROLE_STUDENT` / `ROLE_ADMIN`), gói cước hiện tại (`FREE` / `PRO`) và trạng thái JWT Token.
  - `GET /logout`: Xóa Cookie JWT, hủy Session an toàn và đưa người dùng về trang đăng nhập.
* **Các bước triển khai**:
  1. Bổ sung các trường gói cước trong `User.java`.
  2. Nạp sẵn 3 tài khoản demo: Admin, Free Student, Pro Student.
  3. Hoàn thiện các method xử lý `login()` và `register()` trong `AuthService.java`.
  4. Tùy biến giao diện `login.html`, `register.html`, `profile.html` bằng Tailwind CSS và DaisyUI.
  5. Cùng Leader Tùng tổng hợp tài liệu User Guide Word/PDF và hoàn thiện bộ Slide PowerPoint (12 - 15 trang).
* **Gợi ý Prompt AI cho Trường**:
  > *"Viết AuthService trong Spring Boot 3 sử dụng BCryptPasswordEncoder để băm mật khẩu khi đăng ký tài khoản sinh viên và sử dụng thư viện JJWT 0.12.x để sinh JWT Token khi đăng nhập thành công. Yêu cầu kiểm tra trùng username/email và ném ngoại lệ rõ ràng."*
* **Câu hỏi vấn đáp dự kiến của Giảng viên**:
  - *"Em hãy giải thích cơ chế hoạt động của JSON Web Token (JWT) gồm mấy phần và tại sao lại dùng JWT để cá nhân hóa phiên đăng nhập thay vì chỉ dùng Session truyền thống?"*
  - *"Thuật toán băm BCrypt hoạt động ra sao để chống lại các cuộc tấn công Brute-force hoặc Rainbow Table?"*

---

## 🚀 QUY TRÌNH BẮT ĐẦU CHO CÁC BẠN (HÃY GỬI ĐOẠN NÀY VÀO NHÓM ZALO)
```text
Chào các bạn! Leader Tùng đã cập nhật toàn bộ kế hoạch dự án Smart StudyHub RAG:
1. Hệ thống đã tích hợp gói cước SaaS (Free $0 vs Pro $2) và cung cấp sẵn Key AI tập trung cho toàn trường.
2. Bạn Ân phụ trách Module Admin Hub, Thống kê KPI hệ thống và Quản lý Gói cước sinh viên.
3. Mọi người mở file docs/04-member-tasks.md và docs/03-business-rules.md để đọc chi tiết nhé!
4. Nhớ tạo nhánh theo chuẩn: feature/<tên-nhánh-của-bạn> và mở PR bằng tiếng Việt để Bot tự động squash-merge.
```
