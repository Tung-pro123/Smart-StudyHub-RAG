# 🎓 Smart Multi-Course Study Hub & RAG AI Platform
> **Môn học**: HSF302 - JPA and Spring Framework | **FPT University**  
> **Học kỳ**: Fall 2026 | **Nhóm phát triển**: 6 thành viên (Leader: Tùng)

[![Java](https://img.shields.io/badge/Java-17-orange.svg?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Data JPA](https://img.shields.io/badge/Spring_Data-JPA-blue.svg?style=for-the-badge&logo=hibernate)](https://spring.io/projects/spring-data-jpa)
[![H2 Database](https://img.shields.io/badge/H2-File--based_DB-blueviolet.svg?style=for-the-badge&logo=database)](http://www.h2database.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-darkgreen.svg?style=for-the-badge&logo=thymeleaf)](https://www.thymeleaf.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-DaisyUI_v4-06B6D4.svg?style=for-the-badge&logo=tailwindcss)](https://daisyui.com/)
[![Google Gemini](https://img.shields.io/badge/AI_Engine-Gemini_1.5_Flash-blue.svg?style=for-the-badge&logo=google)](https://ai.google.dev/)

---

## 📖 GIỚI THIỆU ĐỀ TÀI (PROJECT OVERVIEW)

**Smart Multi-Course Study Hub & RAG AI** là nền tảng quản lý và hỗ trợ học tập thông minh đa môn học dành cho sinh viên đại học. Hệ thống giải quyết trọn vẹn tình trạng sinh viên bị quá tải bài giảng slide và mất phương hướng khi ôn thi bằng cách kết hợp:

1. **Quản lý đa môn học tập trung**: Không gian học tập riêng biệt cho từng môn (HSF302, MAS291, PRJ301...).
2. **Bộ máy RAG Giáo trình (Retrieval-Augmented Generation)**: Tự động trích xuất text từ file PDF giáo trình bằng **Apache PDFBox**, chia nhỏ thành các đoạn Chunks và sinh vector nhúng 768 chiều. Trợ lý AI trả lời thắc mắc bám sát 100% tài liệu kèm **trích dẫn số trang (Citations)**, triệt tiêu hiện tượng ảo giác (hallucination).
3. **AI Study Planner**: Tự động phân tích khối lượng giáo trình kết hợp với lịch rảnh của sinh viên để lập thời khóa biểu học tập vừa sức.
4. **AI Quiz Generator**: Tự động sinh đề thi trắc nghiệm A/B/C/D theo từng chương và độ khó, chấm điểm tức thì và giải thích chi tiết.
5. **Chi phí vận hành $0**: Sử dụng H2 Database nhúng sẵn bằng Java (lưu bền vững ra file đĩa) và Gemini API miễn phí, không tốn tài nguyên máy chủ.

---

## 🏛️ KIẾN TRÚC HỆ THỐNG (MODULAR FEATURE ARCHITECTURE)

Dự án áp dụng mô hình kiến trúc **Package-by-Feature (Vertical Slice Architecture)** chuẩn mực doanh nghiệp (tương tự đồ án SWP/Capstone), giúp 6 thành viên trong nhóm làm việc độc lập 100%, gần như không bao giờ bị xung đột mã nguồn (Git Conflict):

```
com.group2.assignment/
│
├── 👑 core/                           <-- Module 1: TÙNG (Leader)
│   ├── entity/                        Course.java, Chunk.java (Dữ liệu nền tảng dùng chung)
│   ├── repository/                    CourseRepository.java, ChunkRepository.java
│   ├── service/                       CourseService.java (Khởi tạo dữ liệu môn mẫu)
│   └── controller/                    HomeController.java (Trang chủ /)
│
├── 🌟 rag/                            <-- Module 2: BÌNH
│   ├── entity/                        Document.java, ChatSession.java, ChatMessage.java
│   ├── repository/                    DocumentRepository.java, ChatSessionRepository.java, ChatMessageRepository.java
│   ├── service/                       PdfChunkingService, RagEngineService
│   └── controller/                    RagChatController
│
├── 📅 planner/                        <-- Module 4: THẮNG
│   ├── entity/                        StudyPlan.java, StudyTask.java
│   ├── repository/                    StudyPlanRepository.java, StudyTaskRepository.java
│   ├── service/                       PlannerService (Thuật toán phân bổ lịch học)
│   └── controller/                    PlannerController
│
├── 🚀 quiz/                           <-- Module 5: NHẬT
│   ├── entity/                        Quiz.java, QuizQuestion.java, QuizAttempt.java
│   ├── repository/                    QuizRepository.java, QuizQuestionRepository.java, QuizAttemptRepository.java
│   ├── service/                       QuizService (Sinh đề trắc nghiệm & chấm điểm)
│   └── controller/                    QuizController
│
└── 🔑 settings/                       <-- Module 3: ÂN
    ├── SettingsConstants.java         (Cấu hình Session Key)
    ├── service/                       ApiKeyService (Test Ping & lưu Session)
    └── controller/                    SettingsController
```

---

## 👥 PHÂN CÔNG NHIỆM VỤ 6 THÀNH VIÊN (TEAM ASSIGNMENTS)

| Thành viên | Vai trò & Module phụ trách | Trách nhiệm Fullstack chính |
|:---|:---|:---|
| **TÙNG (Leader)** 👑 | **Core & Master Layout Module** | Thiết kế nền móng dự án, Master Layout Glassmorphism, CRUD Môn học, Quản lý CSDL dùng chung. |
| **BÌNH** 🌟 | **RAG Knowledge & Chatbot Module** | Upload PDF giáo trình theo môn, Apache PDFBox Chunking, Khung Chat RAG riêng của môn, lưu lịch sử chat. |
| **ÂN** | **Settings & Security Module** | Giao diện cài đặt API Key, Test Connection Ping, lưu an toàn trong `HttpSession` (không lộ vào DB/Git). |
| **THẮNG** | **AI Study Planner Module** | Form nhập lịch rảnh & ngày thi, phân bổ thời gian học theo Chunks giáo trình, Timeline học DaisyUI. |
| **NHẬT** 🚀 | **AI Quiz Generator Module** | Bộ lọc chọn chương & độ khó, sinh trắc nghiệm A/B/C/D từ Chunks, giao diện thi, chấm điểm và lưu kết quả. |
| **TRƯỜNG** | **Master Lead, QA & Presentation** | Chủ biên tài liệu User Guide Word/PDF, Thiết kế bộ Slide thuyết trình 15 trang, Master QA kiểm thử và điều phối Demo. |

---

## 🛠️ CÔNG NGHỆ SỬ DỤNG (TECH STACK)

* **Backend**: Java 17, Spring Boot 3.3.4 (Spring Web, Spring Data JPA, Hibernate 6.5+).
* **Database**: H2 Database (File-based: `./data/studyhubdb`). Tích hợp sẵn Web Console tại `/h2-console`.
* **Database Migration**: Hỗ trợ chuẩn Flyway Migration với script [`V1__initial_schema.sql`](src/main/resources/db/migration/V1__initial_schema.sql).
* **Frontend**: Thymeleaf Server-Side Rendering (SSR), Tailwind CSS (Play CDN), DaisyUI v4, FontAwesome 6, Google Fonts (Plus Jakarta Sans).
* **Tài liệu PDF**: Apache PDFBox 3.0.3 (Trích xuất văn bản tài liệu).
* **Trí tuệ nhân tạo (AI)**: Google Gemini API (`gemini-1.5-flash` và model nhúng vector `text-embedding-004`).

---

## ⚡ HƯỚNG DẪN CÀI ĐẶT & CHẠY DỰ ÁN (QUICK START)

### 1. Yêu cầu môi trường
* **Java Development Kit (JDK)**: Phiên bản 17 trở lên.
* **Apache Maven**: Phiên bản 3.8+ (hoặc dùng Maven nhúng sẵn trong IntelliJ IDEA).
* **IDE khuyên dùng**: IntelliJ IDEA (Ultimate hoặc Community).

### 2. Khởi chạy ứng dụng
1. Clone dự án về máy:
   ```bash
   git clone <URL_REPO_CUA_BAN>
   cd Assignment
   ```
2. Chạy ứng dụng bằng Maven:
   ```bash
   mvn spring-boot:run
   ```
   *(Hoặc mở dự án trong IntelliJ IDEA, tìm đến file `AssignmentApplication.java` và bấm nút **Run** ▶️)*.

3. Mở trình duyệt web và trải nghiệm:
   * **Giao diện chính**: [http://localhost:8080](http://localhost:8080)
   * **H2 Database Web Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
     * JDBC URL: `jdbc:h2:file:./data/studyhubdb`
     * User: `sa` | Password: *(để trống)*

---

## 📚 TÀI LIỆU QUY CHUẨN NỘI BỘ (DOCUMENTATION)

Toàn bộ quy trình và đặc tả kỹ thuật chi tiết được lưu trong thư mục [`docs/`](docs/):
* [`01-project-context.md`](docs/01-project-context.md): Bối cảnh môn học HSF302 và định hướng Project cuối kỳ.
* [`02-git-workflow.md`](docs/02-git-workflow.md): Quy chuẩn phân nhánh Git (`feature/`), chuẩn Commit Message và Merge PR.
* [`03-team-rules.md`](docs/03-team-rules.md): Quy định deadline 21h00, văn hóa báo blocker 4 tiếng, chấm điểm công bằng.
* [`04-member-tasks.md`](docs/04-member-tasks.md): Chi tiết đầu việc Fullstack của 6 thành viên: Tùng, Bình, Ân, Thắng, Nhật, Trường.
* [`05-features-and-architecture.md`](docs/05-features-and-architecture.md): Sơ đồ luồng dữ liệu RAG, Planner và Quiz Generator.
* [`06-prompt-engineering-guide.md`](docs/06-prompt-engineering-guide.md): Bộ Prompt mẫu hướng dẫn 6 bạn dùng AI hiệu quả trong IDE.
* [`07-tech-stack.md`](docs/07-tech-stack.md): Tổng hợp toàn bộ công nghệ Stack và lý do lựa chọn $0 chi phí.
* [`08-tailwind-configuration-guide.md`](docs/08-tailwind-configuration-guide.md): Cẩm nang 3 cách cấu hình Tailwind CSS phục vụ vấn đáp.
* [`09-database-schema-design.md`](docs/09-database-schema-design.md): Đặc tả 10 bảng dữ liệu, ràng buộc khóa ngoại Cascade và Flyway.

---

## 📄 BẢN QUYỀN & GIẢNG VIÊN HƯỚNG DẪN
Dự án được thực hiện phục vụ học phần **HSF302 - JPA and Spring Framework** tại **Đại học FPT**.  
© Fall 2026 - Nhóm sinh viên thực hiện: Tùng (Leader), Bình, Nhật, Thắng, Ân, Trường.

<!-- Test PR verification bot -->
