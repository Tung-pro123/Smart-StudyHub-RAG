# AI Quiz PDF Export & Squad Leaderboard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Triển khai tính năng xuất file PDF đề thi thực sự ở Backend cho gói PRO & SQUAD (bao gồm 2 chế độ: đề thi sạch và đề thi kèm đáp án giải thích chi tiết) và tính năng Bảng xếp hạng thi đua điểm số theo nhóm (Squad Leaderboard) cho gói SQUAD.

**Architecture:** 
1. Tầng Data/Domain: Bổ sung quan hệ `@ManyToOne` giữa `QuizAttempt` và `User`, bổ sung trường `squadLeaderUsername` trong `User` để gom nhóm các thành viên cùng nhóm học tập.
9: 2. Tầng Export PDF: Tích hợp thư viện `com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10` (lưu ý: OpenHTMLToPDF 1.0.10 yêu cầu Apache PDFBox 2.x do phụ thuộc các trường static của `PDType1Font` như `COURIER_BOLD_OBLIQUE`; do đó `pom.xml` hạ version `org.apache.pdfbox:pdfbox` từ `3.0.3` xuống `2.0.31` và refactor toàn bộ mã nguồn sử dụng PDFBox 3.x sang PDFBox 2.x gồm `PdfTextExtractor.java`, `PdfChunkingService.java`, `PdfChunkingServiceTest.java`, và `PdfTextExtractorTest.java` để tương thích hoàn toàn), sử dụng `SpringTemplateEngine` của Thymeleaf để render `pdf-template.html` thành luồng byte PDF trả về qua API export PDF.
10: 3. Tầng Bảng xếp hạng (Leaderboard): Truy vấn JPQL gom nhóm theo thành viên squad (`MAX(score)`, `COUNT(attempt)`), phân hạng Top 1, 2, 3 kèm huy chương trên `quiz/index.html`.
11: 4. Tầng Feature Gating: Kiểm soát 3 gói cước (`FREE`, `PRO`, `SQUAD`), chặn sinh đề 15 - 20 câu và chặn xuất PDF đối với gói `FREE`.
12: 
13: **Tech Stack:** Spring Boot 3.3.4, Java 21, Spring Data JPA, OpenHTMLToPDF 1.0.10, Apache PDFBox 2.0.31, Thymeleaf, TailwindCSS / DaisyUI, JUnit 5, Mockito.
14: 
15: **Spec:** `docs/tasks/task-2026-10-31-pricing-tiers.md` (Mục 5: NHẬT - AI QUIZ GENERATOR & BẢNG XẾP HẠNG).
16: 
17: ## Global Constraints
18: 
19: - **Dependency & PDFBox Compatibility**: Thêm `com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10` vào `pom.xml`. Do OpenHTMLToPDF 1.0.10 bắt buộc dùng PDFBox 2.x, `pom.xml` phải hạ version `org.apache.pdfbox:pdfbox` từ `3.0.3` xuống `2.0.31` (hoặc để OpenHTMLToPDF tự cung cấp `2.0.24`), và refactor toàn bộ các class và test đang dùng PDFBox 3.x sang PDFBox 2.x (`PdfTextExtractor.java` và `PdfChunkingService.java` sang `PDDocument.load(...)`; `PdfChunkingServiceTest.java` và `PdfTextExtractorTest.java` sang `PDType1Font.HELVETICA`). Không thêm các thư viện PDF cồng kềnh khác.
20: - **Java version**: Java 21 LTS (`maven-compiler-plugin`).
21: - **Convention**: Conventional Commits cho git commit (`feat(quiz): ...`, `test(quiz): ...`).
22: - **Character Encoding**: Tất cả template HTML và luồng PDF phải đảm bảo UTF-8 tiếng Việt chuẩn xác.
23: - **Backward Compatibility**: `QuizAttempt.user` có `nullable = true` để không làm hỏng dữ liệu hoặc test mock cũ.
24: 
25: ## Review Focus
26: 
27: 1. **User chưa đăng nhập hoặc session null khi export PDF**: Phải chặn truy cập trái phép và trả về mã lỗi 401/403 thay vì ném NullPointerException (500).
28: 2. **User gói FREE cố tình gọi API `/api/quiz/{id}/export-pdf` bằng URL trực tiếp**: Phải trả về `403 Forbidden`, không cho tải file.
29: 3. **Sinh viên SQUAD chưa có thành viên nào nộp bài**: Leaderboard phải hiển thị rỗng hoặc thông báo thân thiện, không crash giao diện.
30: 4. **Tham số `showAnswers` của API export PDF**: `false` chỉ in câu hỏi và 4 phương án; `true` in thêm bảng đáp án và giải thích ở trang riêng biệt (`page-break-before: always`).
31: 5. **Hiển thị giao diện nộp bài thi (`take.html`)**: Tự động điền `fullName` của `currentUser` thay vì chuỗi cứng "Sinh viên".
32: 6. **Hỗ trợ Font Tiếng Việt trong PDF (Task 2 - Font Unicode)**: Cấu hình OpenHTMLToPDF `PdfRendererBuilder` nạp font Unicode (`/fonts/Arial.ttf` từ classpath) cho cả trọng số Normal (400) và Bold (700) với font subsetting (`true`) và tên font family `"Arial"`, đồng thời cập nhật CSS `font-family: "Arial", "DejaVu Sans", sans-serif;` trong `pdf-template.html` đảm bảo tiếng Việt có dấu hiển thị hoàn hảo, không bị ô vuông `???` trên môi trường Linux/Docker.
33: 7. **Chuẩn hóa Tên file PDF Export (Task 3 - Clean Filename)**: Xử lý ký tự đặc thù tiếng Việt 'đ' và 'Đ' bằng cách chuyển đổi trước (`đ` -> `d`, `Đ` -> `D`) trước khi áp dụng `Normalizer.normalize(rawTitle, Normalizer.Form.NFD).replaceAll("\\p{M}", "")`, sau đó thay thế `[^a-zA-Z0-9.-]` bằng `_` và rút gọn dấu gạch dưới, fallback về `"de-thi"`, tránh tên file bị méo dạng `___thi_s__1.pdf`.
34: 8. **Toast Notification Flashcards (Task 4 - UI Script)**: Khắc phục triệt để lỗi JavaScript `showToast is not defined` trong `result.html` bằng cách định nghĩa hàm tự hủy `showFlashcardToast()` hiển thị DaisyUI Toast thông báo tính năng Flashcards Spaced Repetition đang hoàn thiện và tự động xóa khỏi DOM sau 3.5 giây.
35: 9. **Migration Dữ liệu cũ & Test Isolation (Task 1 & 5 - DB Constraint)**: Entity `QuizAttempt.user` bắt buộc cấu hình `@JoinColumn(name = "user_id", nullable = true)` để tương thích ngược hoàn toàn với dữ liệu seed và lượt nộp bài ẩn danh. Toàn bộ test class (`QuizSquadLeaderboardAndExportTest`, `QuizDoDVerificationTest`) bắt buộc khai báo `@Transactional` rollback tự động sau mỗi bài test và tuyệt đối loại bỏ các lệnh xóa phá hủy `deleteAll()` làm mất dữ liệu mẫu ban đầu.
36: 10. **Tương thích Toàn Diện Thư viện PDFBox 2.x (Task 1 - Critical Cross-Module Compatibility)**: `openhtmltopdf-pdfbox:1.0.10` yêu cầu Apache PDFBox 2.x (do phụ thuộc vào các trường font tĩnh trong `PDType1Font`, nếu giữ `pdfbox:3.0.3` sẽ văng `NoSuchFieldError`). Khắc phục triệt để bằng cách hạ `pdfbox` xuống `2.0.31` trong `pom.xml` và refactor toàn bộ mã nguồn sử dụng PDFBox 3.x trên toàn project:
    - Trong `PdfTextExtractor.java` (Quiz): chuyển `Loader.loadPDF(file/bytes)` sang `PDDocument.load(file/bytes)` và xóa `import org.apache.pdfbox.Loader;`.
    - Trong `PdfChunkingService.java` (RAG): chuyển `Loader.loadPDF(fileBytes)` sang `PDDocument.load(fileBytes)` và xóa `import org.apache.pdfbox.Loader;`.
    - Trong `PdfChunkingServiceTest.java` (RAG test) & `PdfTextExtractorTest.java` (Quiz test): xóa `import org.apache.pdfbox.pdmodel.font.Standard14Fonts;` và chuyển `new PDType1Font(Standard14Fonts.FontName.HELVETICA)` sang hằng số tĩnh `PDType1Font.HELVETICA`.
11. **Routing Đồng Bộ & Parameter Controller (Task 3 - Controller Routing & Parameter)**: Cấu hình `QuizController` hỗ trợ cả `/quiz` và `/api/quiz` (qua class-level `@RequestMapping({"/quiz", "/api/quiz"})` và method mapping `@GetMapping({"/api/quiz/{id}/export-pdf", "/{id}/export-pdf", "/quiz/{id}/export-pdf"})`) để đảm bảo các request tới `/api/quiz/{id}/export-pdf` không bị 404 Not Found. Method `submitQuiz` bổ sung tường minh tham số `HttpSession session`.
12. **Theo Dõi Tài Nguyên Font Trong Git (Task 2 - Font Git Tracking)**: File font `src/main/resources/fonts/Arial.ttf` bắt buộc phải được commit vào git trong Task 2 Step 6 để tránh lỗi thiếu file font gây NullPointerException trên môi trường CI/Docker.

---

### Task 1: Thêm Dependency OpenHTMLToPDF và Cấu hình Entity User & QuizAttempt

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/java/com/group2/assignment/auth/entity/User.java`
- Modify: `src/main/java/com/group2/assignment/quiz/entity/QuizAttempt.java`
- Modify: `src/main/java/com/group2/assignment/quiz/service/PdfTextExtractor.java`
- Modify: `src/main/java/com/group2/assignment/rag/service/PdfChunkingService.java`
- Modify: `src/test/java/com/group2/assignment/rag/PdfChunkingServiceTest.java`
- Modify: `src/test/java/com/group2/assignment/quiz/service/PdfTextExtractorTest.java`
- Test: `src/test/java/com/group2/assignment/quiz/entity/QuizAttemptUserMappingTest.java`

**Interfaces:**
- `User.java`: Thêm `private String squadLeaderUsername`, method `public String getEffectiveSquadLeader()`, method `public boolean isSquad()`.
- `QuizAttempt.java`: Thêm `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = true) private User user` (bắt buộc `nullable = true` để tương thích ngược với dữ liệu cũ và lượt thi ẩn danh).

- [ ] **Step 1: Cập nhật dependency trong `pom.xml` (Thêm OpenHTMLToPDF & Hạ version PDFBox xuống 2.x)**

Thư viện `com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10` yêu cầu Apache PDFBox 2.x do phụ thuộc vào các hằng số font tĩnh trong `PDType1Font` (như `COURIER_BOLD_OBLIQUE`). Trong `pom.xml`, dependency `org.apache.pdfbox:pdfbox` hiện đang ở version `3.0.3` dẫn đến xung đột classpath nghiêm trọng gây `java.lang.NoSuchFieldError` lúc runtime khi render PDF.

Do đó, `pom.xml` bắt buộc phải hạ version `org.apache.pdfbox:pdfbox` từ `3.0.3` xuống `2.0.31` (hoặc có thể bỏ khai báo tường minh để `openhtmltopdf-pdfbox` tự cung cấp version tương thích `2.0.24`) và thêm `openhtmltopdf-pdfbox:1.0.10`:

```xml
        <!-- Apache PDFBox: Hạ từ 3.0.3 xuống 2.0.31 để đảm bảo tương thích 100% với OpenHTMLToPDF 1.0.10 (tránh NoSuchFieldError) -->
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>2.0.31</version>
        </dependency>

        <!-- OpenHTMLToPDF: Xuất HTML/CSS sang PDF cho Module Quiz Đề thi -->
        <dependency>
            <groupId>com.openhtmltopdf</groupId>
            <artifactId>openhtmltopdf-pdfbox</artifactId>
            <version>1.0.10</version>
        </dependency>
```

- [ ] **Step 2: Viết failing test cho User squad method và QuizAttempt mapping**

Tạo `src/test/java/com/group2/assignment/quiz/entity/QuizAttemptUserMappingTest.java`:
```java
package com.group2.assignment.quiz.entity;

import com.group2.assignment.auth.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuizAttemptUserMappingTest {

    @Test
    @DisplayName("User: Kiểm tra xác định nhóm trưởng hiệu dụng getEffectiveSquadLeader")
    void testEffectiveSquadLeader() {
        User leader = User.builder().username("leader1").subscriptionTier("SQUAD").build();
        assertEquals("leader1", leader.getEffectiveSquadLeader());

        User member = User.builder().username("member1").subscriptionTier("SQUAD").squadLeaderUsername("leader1").build();
        assertEquals("leader1", member.getEffectiveSquadLeader());

        User freeUser = User.builder().username("free1").subscriptionTier("FREE").build();
        assertNull(freeUser.getEffectiveSquadLeader());
    }

    @Test
    @DisplayName("QuizAttempt: Gán User và Quiz hợp lệ")
    void testQuizAttemptUserAssociation() {
        User user = User.builder().id(10L).username("testuser").build();
        QuizAttempt attempt = QuizAttempt.builder()
                .id(1L)
                .user(user)
                .studentName("Test User")
                .totalScore(9.0)
                .correctCount(9)
                .totalCount(10)
                .build();

        assertNotNull(attempt.getUser());
        assertEquals(10L, attempt.getUser().getId());
    }
}
```

- [ ] **Step 3: Chạy test để xác nhận FAIL**

Run: `mvn test -Dtest=QuizAttemptUserMappingTest`
Expected: FAIL do chưa có trường và phương thức trên `User` và `QuizAttempt`.

- [ ] **Step 4: Cập nhật Entity `User.java` và `QuizAttempt.java`**

Trong `User.java`:
```java
    @Column(length = 50)
    private String squadLeaderUsername; // Username nhóm trưởng sở hữu gói Study Squad

    public boolean isSquad() {
        return "SQUAD".equalsIgnoreCase(this.subscriptionTier);
    }

    public String getEffectiveSquadLeader() {
        if (!isSquad()) {
            return null;
        }
        if (this.squadLeaderUsername != null && !this.squadLeaderUsername.isBlank()) {
            return this.squadLeaderUsername.trim();
        }
        return this.username;
    }
```

Trong `QuizAttempt.java`:
```java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @ToString.Exclude
    private com.group2.assignment.auth.entity.User user;
```

- [ ] **Step 5: Refactor toàn diện các Class & Test sử dụng PDFBox 2.x và Chạy test đảm bảo PASS 100%**

Do hạ version PDFBox từ 3.x xuống 2.0.31 trong `pom.xml`, các class và test trước đây phụ thuộc vào các API đặc thù của PDFBox 3.x (`org.apache.pdfbox.Loader` và `org.apache.pdfbox.pdmodel.font.Standard14Fonts`) sẽ bị lỗi biên dịch. Cụ thể có 4 file cần được refactor trên toàn dự án (bao gồm cả module Quiz và module RAG):

1. **`PdfTextExtractor.java` (Module Quiz)**:
   - *Nguyên nhân*: Class `Loader` không tồn tại trong PDFBox 2.x. Phương thức nạp PDF chuẩn của PDFBox 2.x là `PDDocument.load(...)`.
   - *Thực hiện*: Trong `src/main/java/com/group2/assignment/quiz/service/PdfTextExtractor.java`:
     - Xóa: `import org.apache.pdfbox.Loader;`
     - Tại dòng 30 (trong method `extractText(File file, ...)`): chuyển từ `Loader.loadPDF(file)` sang `PDDocument.load(file)`.
     - Tại dòng 36 (trong method `extractFromBytes(byte[] bytes, ...)`): chuyển từ `Loader.loadPDF(bytes)` sang `PDDocument.load(bytes)`.
   ```java
   // src/main/java/com/group2/assignment/quiz/service/PdfTextExtractor.java
   public String extractText(File file, Integer fromPage, Integer toPage) throws IOException {
       if (file == null || !file.exists() || file.length() == 0) {
           throw new IllegalArgumentException("File tài liệu trên hệ thống không tồn tại hoặc rỗng!");
       }
       try (PDDocument document = PDDocument.load(file)) {
           return extractFromDocument(document, fromPage, toPage);
       }
   }

   private String extractFromBytes(byte[] bytes, Integer fromPage, Integer toPage) throws IOException {
       try (PDDocument document = PDDocument.load(bytes)) {
           return extractFromDocument(document, fromPage, toPage);
       }
   }
   ```

2. **`PdfChunkingService.java` (Module RAG)**:
   - *Nguyên nhân*: Class `Loader` không tồn tại trong PDFBox 2.x (`cannot find symbol: class Loader`).
   - *Thực hiện*: Trong `src/main/java/com/group2/assignment/rag/service/PdfChunkingService.java`:
     - Xóa: `import org.apache.pdfbox.Loader;`
     - Tại dòng 101 (trong method `processPdf`): thay `Loader.loadPDF(fileBytes)` bằng `PDDocument.load(fileBytes)`. (Nếu có method nhận `File` cũng thay bằng `PDDocument.load(file)`).
   ```java
   // src/main/java/com/group2/assignment/rag/service/PdfChunkingService.java (dòng 95-102)
   // 3. Trích xuất text và cắt chunk với Apache PDFBox 2.0.31
   try {
       byte[] fileBytes = file.getBytes();
       List<Chunk> chunksToSave = new ArrayList<>();
       int totalPages;

       try (PDDocument pdDocument = PDDocument.load(fileBytes)) {
   ```

3. **`PdfChunkingServiceTest.java` (Test Module RAG)**:
   - *Nguyên nhân*: Trong PDFBox 2.x, `Standard14Fonts` là package-private và không có enum public `FontName` (`Standard14Fonts is not public in org.apache.pdfbox.pdmodel.font; cannot be accessed from outside package`). Font chuẩn được truy cập trực tiếp qua các hằng số tĩnh trên `PDType1Font`.
   - *Thực hiện*: Trong `src/test/java/com/group2/assignment/rag/PdfChunkingServiceTest.java`:
     - Xóa: `import org.apache.pdfbox.pdmodel.font.Standard14Fonts;`
     - Tại dòng 135 & dòng 144: thay `new PDType1Font(Standard14Fonts.FontName.HELVETICA)` bằng `PDType1Font.HELVETICA`.
   ```java
   // src/test/java/com/group2/assignment/rag/PdfChunkingServiceTest.java (dòng 133-147)
   try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
       cs.beginText();
       cs.setFont(PDType1Font.HELVETICA, 12);
       cs.showText("Page 1: Spring Boot Auto Configuration reduces boilerplate setup.");
       cs.endText();
   }

   PDPage page2 = new PDPage();
   doc.addPage(page2);
   try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
       cs.beginText();
       cs.setFont(PDType1Font.HELVETICA, 12);
       cs.showText("Page 2: Spring Data JPA simplifies database access layer.");
       cs.endText();
   }
   ```

4. **`PdfTextExtractorTest.java` (Test Module Quiz)**:
   - *Nguyên nhân*: Tương tự, `Standard14Fonts` không truy cập được trong PDFBox 2.x.
   - *Thực hiện*: Trong `src/test/java/com/group2/assignment/quiz/service/PdfTextExtractorTest.java`:
     - Xóa: `import org.apache.pdfbox.pdmodel.font.Standard14Fonts;`
     - Tại dòng 28 & dòng 37: thay `new PDType1Font(Standard14Fonts.FontName.HELVETICA)` bằng `PDType1Font.HELVETICA`.
   ```java
   // src/test/java/com/group2/assignment/quiz/service/PdfTextExtractorTest.java (dòng 26-40)
   try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
       cs.beginText();
       cs.setFont(PDType1Font.HELVETICA, 12);
       cs.showText("Page 1: Introduction to Spring Framework IoC Container");
       cs.endText();
   }

   PDPage page2 = new PDPage();
   doc.addPage(page2);
   try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
       cs.beginText();
       cs.setFont(PDType1Font.HELVETICA, 12);
       cs.showText("Page 2: Dependency Injection is the core pattern of Spring");
       cs.endText();
   }
   ```

**Xác minh Kiểm thử sau Task 1:**
Chạy kiểm thử các bài test liên quan và chạy toàn bộ test suite để xác nhận 100% không có lỗi biên dịch hoặc regression trên tất cả module (`auth`, `quiz`, `rag`):
- Run unit tests: `mvn test -Dtest=QuizAttemptUserMappingTest,PdfTextExtractorTest,PdfChunkingServiceTest`
  Expected: BUILD SUCCESS (Toàn bộ các unit test liên quan đều pass).
- Run full project test suite: `mvn test`
  Expected: BUILD SUCCESS (Tất cả test cases của các module `auth`, `quiz`, `rag` đều biên dịch và pass 100% cleanly, zero compilation errors, zero regressions).

- [ ] **Step 6: Commit**

```bash
git add pom.xml \
  src/main/java/com/group2/assignment/auth/entity/User.java \
  src/main/java/com/group2/assignment/quiz/entity/QuizAttempt.java \
  src/main/java/com/group2/assignment/quiz/service/PdfTextExtractor.java \
  src/main/java/com/group2/assignment/rag/service/PdfChunkingService.java \
  src/test/java/com/group2/assignment/rag/PdfChunkingServiceTest.java \
  src/test/java/com/group2/assignment/quiz/service/PdfTextExtractorTest.java \
  src/test/java/com/group2/assignment/quiz/entity/QuizAttemptUserMappingTest.java
git commit -m "feat(quiz): add openhtmltopdf, downgrade pdfbox to 2.0.31 with complete codebase refactoring, and map user to quiz attempt"
```

---

### Task 2: Xây Dựng Service Xuất PDF (`QuizPdfExportService`) & Template HTML

**Files:**
- Asset/Track: `src/main/resources/fonts/Arial.ttf`
- Create: `src/main/resources/templates/quiz/pdf-template.html`
- Create: `src/main/java/com/group2/assignment/quiz/service/QuizPdfExportService.java`
- Test: `src/test/java/com/group2/assignment/quiz/service/QuizPdfExportServiceTest.java`

**Interfaces:**
- `QuizPdfExportService`:
  - `byte[] exportQuizToPdf(Quiz quiz, List<QuizQuestion> questions, boolean showAnswers)`: Nhận quiz, câu hỏi và flag `showAnswers`, trả về mảng byte PDF.

- [ ] **Step 1: Tạo template in ấn `pdf-template.html`**

Tạo `src/main/resources/templates/quiz/pdf-template.html` định dạng HTML sạch, CSS inline `@page` với lề in ấn tiêu chuẩn, hỗ trợ `showAnswers`:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title th:text="${quiz.title}">Đề Thi Trắc Nghiệm</title>
    <style>
        @page {
            size: A4;
            margin: 20mm;
            @bottom-center {
                content: "Trang " counter(page) " / " counter(pages);
                font-family: "Arial", "DejaVu Sans", sans-serif;
                font-size: 9pt;
                color: #666666;
            }
        }
        body {
            font-family: "Arial", "DejaVu Sans", sans-serif;
            color: #1a1a1a;
            line-height: 1.5;
            font-size: 11pt;
            margin: 0;
            padding: 0;
        }
        .header {
            text-align: center;
            border-bottom: 2px solid #2563eb;
            padding-bottom: 15px;
            margin-bottom: 20px;
        }
        .header h1 {
            margin: 0 0 6px 0;
            font-size: 18pt;
            color: #1e3a8a;
            text-transform: uppercase;
        }
        .meta-info {
            font-size: 10pt;
            color: #4b5563;
        }
        .student-box {
            border: 1px dashed #9ca3af;
            padding: 10px 15px;
            margin-bottom: 25px;
            font-size: 10.5pt;
        }
        .question-item {
            margin-bottom: 16px;
            page-break-inside: avoid;
        }
        .question-title {
            font-weight: bold;
            color: #111827;
            margin-bottom: 6px;
        }
        .options-grid {
            margin-left: 15px;
        }
        .option-item {
            margin-bottom: 4px;
        }
        .answers-page {
            page-break-before: always;
        }
        .answer-badge {
            font-weight: bold;
            color: #16a34a;
        }
        .explanation {
            font-size: 9.5pt;
            color: #4b5563;
            font-style: italic;
            margin-top: 2px;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1 th:text="${quiz.title}">ĐỀ THI TRẮC NGHIỆM AI</h1>
        <div class="meta-info">
            <strong>Môn học:</strong> <span th:text="${quiz.course != null ? quiz.course.name : 'Chung'}"></span> |
            <strong>Độ khó:</strong> <span th:text="${quiz.difficulty}">MEDIUM</span> |
            <strong>Số câu:</strong> <span th:text="${#lists.size(questions)}">10</span> câu |
            <strong>Thời gian:</strong> <span th:text="${quiz.timeLimitMinutes != null ? quiz.timeLimitMinutes : 15}">15</span> phút
        </div>
    </div>

    <div class="student-box">
        Họ và tên thí sinh: ................................................................ Lớp: ............................ Ngày thi: ......................
    </div>

    <div class="questions">
        <div th:each="q, stat : ${questions}" class="question-item">
            <div class="question-title">
                Câu <span th:text="${stat.count}"></span>: <span th:text="${q.questionText}">Nội dung câu hỏi</span>
            </div>
            <div class="options-grid">
                <div class="option-item">A. <span th:text="${q.optionA}">Lựa chọn A</span></div>
                <div class="option-item">B. <span th:text="${q.optionB}">Lựa chọn B</span></div>
                <div class="option-item">C. <span th:text="${q.optionC}">Lựa chọn C</span></div>
                <div class="option-item">D. <span th:text="${q.optionD}">Lựa chọn D</span></div>
            </div>
        </div>
    </div>

    <!-- Trang Đáp Án (Chỉ hiển thị khi showAnswers = true) -->
    <div th:if="${showAnswers}" class="answers-page">
        <div class="header">
            <h1 style="color: #16a34a;">ĐÁP ÁN VÀ LỜI GIẢI CHI TIẾT</h1>
            <div class="meta-info" th:text="${quiz.title}">Đề thi trắc nghiệm</div>
        </div>

        <div th:each="q, stat : ${questions}" class="question-item" style="border-bottom: 1px dotted #e5e7eb; padding-bottom: 8px;">
            <div>
                <strong>Câu <span th:text="${stat.count}"></span>:</strong>
                Đáp án: <span class="answer-badge" th:text="${q.correctAnswer}">A</span>
            </div>
            <div th:if="${q.explanation != null and !q.explanation.isBlank()}" class="explanation">
                Giải thích: <span th:text="${q.explanation}">Giải thích chi tiết vì sao chọn đáp án này.</span>
            </div>
        </div>
    </div>
</body>
</html>
```

- [ ] **Step 2: Viết test cho `QuizPdfExportService`**

Tạo `src/test/java/com/group2/assignment/quiz/service/QuizPdfExportServiceTest.java`:
```java
package com.group2.assignment.quiz.service;

import com.group2.assignment.core.entity.Course;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizQuestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class QuizPdfExportServiceTest {

    @Autowired
    private QuizPdfExportService pdfExportService;

    @Test
    @DisplayName("Xuất PDF đề thi không kèm đáp án (showAnswers = false)")
    void testExportPdfWithoutAnswers() {
        Course course = Course.builder().name("Lập Trình Web Java").code("CS101").build();
        Quiz quiz = Quiz.builder()
                .id(1L)
                .title("Đề kiểm tra chương 1")
                .difficulty("EASY")
                .timeLimitMinutes(15)
                .course(course)
                .build();

        QuizQuestion q1 = QuizQuestion.builder()
                .questionText("Spring Boot dùng Annotation nào để cấu hình?")
                .optionA("@SpringBootApplication")
                .optionB("@ConfigurationOnly")
                .optionC("@MainClass")
                .optionD("@JavaApp")
                .correctAnswer("A")
                .explanation("Annotation @SpringBootApplication kích hoạt auto-configuration.")
                .build();

        byte[] pdfBytes = pdfExportService.exportQuizToPdf(quiz, List.of(q1), false);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "File PDF phải có dung lượng hợp lệ");
        // Kiểm tra magic header PDF %PDF-
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
    }

    @Test
    @DisplayName("Xuất PDF đề thi có kèm đáp án và lời giải (showAnswers = true)")
    void testExportPdfWithAnswers() {
        Course course = Course.builder().name("Hệ Quản Trị CSDL").code("DB201").build();
        Quiz quiz = Quiz.builder()
                .id(2L)
                .title("Đề ôn tập SQL")
                .difficulty("HARD")
                .timeLimitMinutes(30)
                .course(course)
                .build();

        QuizQuestion q1 = QuizQuestion.builder()
                .questionText("Mệnh đề GROUP BY trong SQL dùng để làm gì?")
                .optionA("Sắp xếp thứ tự")
                .optionB("Gom nhóm dữ liệu theo các cột chỉ định")
                .optionC("Lọc bản ghi trước khi nhóm")
                .optionD("Xóa dữ liệu trùng")
                .correctAnswer("B")
                .explanation("GROUP BY nhóm các dòng có cùng giá trị lại để tổng hợp.")
                .build();

        byte[] pdfBytesWithAnswers = pdfExportService.exportQuizToPdf(quiz, List.of(q1), true);

        assertNotNull(pdfBytesWithAnswers);
        assertTrue(pdfBytesWithAnswers.length > 500);
    }
}
```

- [ ] **Step 3: Chạy test để xác nhận FAIL**

Run: `mvn test -Dtest=QuizPdfExportServiceTest`
Expected: FAIL do chưa tạo class `QuizPdfExportService`.

- [ ] **Step 4: Viết `QuizPdfExportService.java`**

Tạo `src/main/java/com/group2/assignment/quiz/service/QuizPdfExportService.java`:
```java
package com.group2.assignment.quiz.service;

import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizPdfExportService {

    private final SpringTemplateEngine templateEngine;

    public byte[] exportQuizToPdf(Quiz quiz, List<QuizQuestion> questions, boolean showAnswers) {
        try {
            Context context = new Context();
            context.setVariable("quiz", quiz);
            context.setVariable("questions", questions);
            context.setVariable("showAnswers", showAnswers);

            String htmlContent = templateEngine.process("quiz/pdf-template", context);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();

                // Cấu hình nạp Unicode font Tiếng Việt từ classpath cho cả Normal (400) và Bold (700) với font subsetting
                builder.useFont(() -> getClass().getResourceAsStream("/fonts/Arial.ttf"), "Arial", 400, PdfRendererBuilder.FontStyle.NORMAL, true);
                builder.useFont(() -> getClass().getResourceAsStream("/fonts/Arial.ttf"), "Arial", 700, PdfRendererBuilder.FontStyle.NORMAL, true);

                builder.withHtmlContent(htmlContent, null);
                builder.toStream(outputStream);
                builder.run();
                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            log.error("Lỗi khi kết xuất file PDF đề thi quizId={}", quiz != null ? quiz.getId() : "null", e);
            throw new RuntimeException("Không thể tạo file PDF đề thi: " + e.getMessage(), e);
        }
    }
}
```

- [ ] **Step 5: Chạy test để đảm bảo PASS**

Run: `mvn test -Dtest=QuizPdfExportServiceTest`
Expected: BUILD SUCCESS (2/2 tests passed).

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/fonts/Arial.ttf src/main/resources/templates/quiz/pdf-template.html src/main/java/com/group2/assignment/quiz/service/QuizPdfExportService.java src/test/java/com/group2/assignment/quiz/service/QuizPdfExportServiceTest.java
git commit -m "feat(quiz): implement QuizPdfExportService using openhtmltopdf with unicode font support"
```

---

### Task 3: Cập Nhật Repository, Service & Controller Cho Bảng Xếp Hạng Squad & API Xuất PDF

**Files:**
- Modify: `src/main/java/com/group2/assignment/quiz/repository/QuizAttemptRepository.java`
- Modify: `src/main/java/com/group2/assignment/quiz/service/QuizService.java`
- Modify: `src/main/java/com/group2/assignment/quiz/controller/QuizController.java`
- Test: `src/test/java/com/group2/assignment/quiz/QuizSquadLeaderboardAndExportTest.java`

**Interfaces:**
- `QuizAttemptRepository`:
  - `findSquadLeaderboard(String leaderUsername, Long courseId)`
- `QuizService`:
  - `submitAndGrade(Long quizId, QuizSubmissionDto submission, User currentUser)`
  - `getSquadLeaderboard(String leaderUsername, Long courseId)`: trả về danh sách `SquadMemberScoreDto`
- `QuizController`:
  - `GET /api/quiz/{id}/export-pdf?showAnswers={true|false}`: Xuất PDF có phân quyền (Free -> 403, Pro/Squad -> file PDF).
  - `GET /quiz`: Truyền `squadLeaderboard` nếu user là SQUAD.
  - `POST /quiz/{id}/submit`: Lưu `currentUser` vào attempt.

- [ ] **Step 1: Viết failing test cho Squad Leaderboard & Export PDF API**

Tạo `src/test/java/com/group2/assignment/quiz/QuizSquadLeaderboardAndExportTest.java`:
```java
package com.group2.assignment.quiz;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizAttempt;
import com.group2.assignment.quiz.repository.QuizAttemptRepository;
import com.group2.assignment.quiz.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuizSquadLeaderboardAndExportTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    private User freeUser;
    private User proUser;
    private User squadLeader;
    private User squadMember;
    private Course course;
    private Quiz testQuiz;

    @BeforeEach
    void setUp() {
        // Tuyệt đối không dùng deleteAll() để tránh xóa mất dữ liệu mẫu ban đầu (initSampleQuiz)
        // Dữ liệu tạo trong test được tự động rollback sau mỗi method nhờ @Transactional

        freeUser = userRepository.save(User.builder()
                .username("free_student_" + System.currentTimeMillis())
                .email("free_" + System.currentTimeMillis() + "@gmail.com")
                .fullName("Free Student")
                .password("hash")
                .subscriptionTier("FREE")
                .role("ROLE_STUDENT")
                .build());

        proUser = userRepository.save(User.builder()
                .username("pro_student_" + System.currentTimeMillis())
                .email("pro_" + System.currentTimeMillis() + "@gmail.com")
                .fullName("Pro Student")
                .password("hash")
                .subscriptionTier("PRO")
                .role("ROLE_STUDENT")
                .build());

        squadLeader = userRepository.save(User.builder()
                .username("squad_leader_" + System.currentTimeMillis())
                .email("leader_" + System.currentTimeMillis() + "@gmail.com")
                .fullName("Trưởng Nhóm Squad")
                .password("hash")
                .subscriptionTier("SQUAD")
                .role("ROLE_STUDENT")
                .build());

        squadMember = userRepository.save(User.builder()
                .username("squad_member_" + System.currentTimeMillis())
                .email("member_" + System.currentTimeMillis() + "@gmail.com")
                .fullName("Thành Viên Squad")
                .password("hash")
                .subscriptionTier("SQUAD")
                .squadLeaderUsername(squadLeader.getUsername())
                .role("ROLE_STUDENT")
                .build());

        course = courseRepository.save(Course.builder()
                .name("Kỹ Thuật Phần Mềm Nâng Cao")
                .code("SWE301")
                .description("Môn học kiểm thử")
                .build());

        testQuiz = quizRepository.save(Quiz.builder()
                .title("Đề thi kiểm thử Squad & PDF")
                .course(course)
                .difficulty("MEDIUM")
                .totalQuestions(5)
                .timeLimitMinutes(15)
                .build());
    }

    private MockHttpSession sessionFor(User user) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, user);
        return session;
    }

    @Test
    @DisplayName("API Export PDF: Gói Free bị chặn 403 Forbidden")
    void testExportPdfForbiddenForFree() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .session(sessionFor(freeUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API Export PDF: Gói Pro tải file PDF thành công")
    void testExportPdfSuccessForPro() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "false")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=")));
    }

    @Test
    @DisplayName("API Export PDF: Tên file tiếng Việt có chữ Đ/đ được chuẩn hóa thành D/d sạch sẽ")
    void testExportPdfFilenameNormalizedVietnamese() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "false")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("filename=\"De_thi_kiem_thu_Squad_PDF.pdf\"")));
    }

    @Test
    @DisplayName("Bảng xếp hạng Squad: Hiển thị điểm số cao nhất của các thành viên trong nhóm")
    void testSquadLeaderboardDisplayed() throws Exception {
        // Tạo 2 attempts: Leader điểm 8.0, Member điểm 10.0
        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(testQuiz)
                .user(squadLeader)
                .studentName(squadLeader.getFullName())
                .totalScore(8.0)
                .correctCount(4)
                .totalCount(5)
                .build());

        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(testQuiz)
                .user(squadMember)
                .studentName(squadMember.getFullName())
                .totalScore(10.0)
                .correctCount(5)
                .totalCount(5)
                .build());

        mockMvc.perform(get("/quiz")
                        .param("courseId", String.valueOf(course.getId()))
                        .session(sessionFor(squadMember)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("squadLeaderboard"))
                .andExpect(model().attribute("squadLeaderboard", hasSize(2)));
    }
}
```

- [ ] **Step 2: Chạy test để xác nhận FAIL**

Run: `mvn test -Dtest=QuizSquadLeaderboardAndExportTest`
Expected: FAIL do chưa có endpoint `/api/quiz/{id}/export-pdf` và chưa có method Leaderboard.

- [ ] **Step 3: Bổ sung DTO và Query trong `QuizAttemptRepository`**

Tạo DTO `src/main/java/com/group2/assignment/quiz/dto/SquadLeaderboardDto.java`:
```java
package com.group2.assignment.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SquadLeaderboardDto {
    private String username;
    private String fullName;
    private String avatar;
    private Double maxScore;
    private Long attemptCount;
}
```

Cập nhật `QuizAttemptRepository.java`:
```java
    @Query("SELECT u.username, u.fullName, u.avatar, MAX(a.totalScore), COUNT(a.id) " +
           "FROM QuizAttempt a JOIN a.user u " +
           "WHERE (u.squadLeaderUsername = :leaderUsername OR u.username = :leaderUsername) " +
           "AND (:courseId IS NULL OR a.quiz.course.id = :courseId) " +
           "GROUP BY u.id, u.username, u.fullName, u.avatar " +
           "ORDER BY MAX(a.totalScore) DESC, COUNT(a.id) DESC, u.username ASC")
    List<Object[]> findSquadLeaderboardRaw(@Param("leaderUsername") String leaderUsername, @Param("courseId") Long courseId);
```

- [ ] **Step 4: Cập nhật `QuizService.java`**

Bổ sung phương thức `getSquadLeaderboard` (trích xuất đầy đủ trường `avatar = (String) row[2]`):
```java
    public List<SquadLeaderboardDto> getSquadLeaderboard(String leaderUsername, Long courseId) {
        if (leaderUsername == null || leaderUsername.isBlank()) {
            return List.of();
        }
        List<Object[]> rawList = quizAttemptRepository.findSquadLeaderboardRaw(leaderUsername.trim(), courseId);
        List<SquadLeaderboardDto> dtoList = new ArrayList<>();
        for (Object[] row : rawList) {
            String username = (String) row[0];
            String fullName = (String) row[1];
            String avatar = (String) row[2];
            Double maxScore = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            Long attemptCount = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            dtoList.add(SquadLeaderboardDto.builder()
                    .username(username)
                    .fullName(fullName)
                    .avatar(avatar)
                    .maxScore(maxScore)
                    .attemptCount(attemptCount)
                    .build());
        }
        return dtoList;
    }
```
Và trong `QuizService.java`, bổ sung overload `submitAndGrade` có tham số `User currentUser` để gán `attempt.setUser(currentUser)`. Giữ nguyên overload cũ không có tham số User ủy quyền sang overload mới để tương thích ngược 100%:
```java
    @Transactional
    public QuizResultDto submitAndGrade(Long quizId, QuizSubmissionDto submission) {
        return submitAndGrade(quizId, submission, null);
    }

    @Transactional
    public QuizResultDto submitAndGrade(Long quizId, QuizSubmissionDto submission, User currentUser) {
        // Logic chấm điểm giữ nguyên như hiện tại...
        // Sau khi khởi tạo attempt từ submission:
        // attempt.setUser(currentUser);
        // quizAttemptRepository.save(attempt);
        // return QuizResultDto...
    }
```

- [ ] **Step 5: Cập nhật `QuizController.java`**

Trong `QuizController.java`:
1. Inject `QuizPdfExportService`.
2. Cấu hình class-level routing: Cập nhật annotation class để hỗ trợ cả route web `/quiz` và API `/api/quiz`:
```java
@Controller
@RequestMapping({"/quiz", "/api/quiz"})
@Slf4j
public class QuizController { ... }
```
3. Trong `quizIndex`:
```java
        List<SquadLeaderboardDto> squadLeaderboard = List.of();
        if (currentUser != null && currentUser.isSquad()) {
            String effectiveLeader = currentUser.getEffectiveSquadLeader();
            squadLeaderboard = quizService.getSquadLeaderboard(effectiveLeader, selectedCourse != null ? selectedCourse.getId() : null);
        }
        model.addAttribute("squadLeaderboard", squadLeaderboard);
        model.addAttribute("isSquad", currentUser != null && currentUser.isSquad());
```
4. Trong `submitQuiz`: Bổ sung tham số `HttpSession session` vào signature phương thức để lấy `currentUser`:
```java
    @PostMapping("/{id}/submit")
    public String submitQuiz(
            @PathVariable("id") Long id,
            @RequestParam(value = "studentName", defaultValue = "Sinh viên") String studentName,
            @RequestParam(value = "timeSpentSeconds", defaultValue = "0") Integer timeSpentSeconds,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Map<Long, String> answers = new HashMap<>();
        List<QuizQuestion> questions = quizService.getQuestionsByQuizId(id);

        for (QuizQuestion q : questions) {
            String ans = request.getParameter("answer_" + q.getId());
            if (ans != null && !ans.isBlank()) {
                answers.put(q.getId(), ans.trim().toUpperCase());
            }
        }

        QuizSubmissionDto submission = QuizSubmissionDto.builder()
                .studentName(studentName)
                .timeSpentSeconds(timeSpentSeconds)
                .answers(answers)
                .build();

        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        QuizResultDto result = quizService.submitAndGrade(id, submission, currentUser);
        return "redirect:/quiz/attempt/" + result.getAttemptId();
    }
```
5. Thêm endpoint xuất PDF hỗ trợ cả hai đường dẫn `@GetMapping({"/api/quiz/{id}/export-pdf", "/{id}/export-pdf", "/quiz/{id}/export-pdf"})` (tránh 404 khi gọi `/api/quiz/{id}/export-pdf`):
```java
    @GetMapping({"/api/quiz/{id}/export-pdf", "/{id}/export-pdf", "/quiz/{id}/export-pdf"})
    public ResponseEntity<byte[]> exportQuizPdf(
            @PathVariable("id") Long id,
            @RequestParam(value = "showAnswers", defaultValue = "false") boolean showAnswers,
            HttpSession session) {

        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!currentUser.isPro()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Quiz quiz = quizService.getQuizById(id);
        List<QuizQuestion> questions = quizService.getQuestionsByQuizId(id);

        byte[] pdfBytes = quizPdfExportService.exportQuizToPdf(quiz, questions, showAnswers);

        String rawTitle = quiz.getTitle() != null ? quiz.getTitle() : "de-thi";
        String normalized = rawTitle.replace('đ', 'd').replace('Đ', 'D');
        normalized = java.text.Normalizer.normalize(normalized, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String cleanTitle = normalized.replaceAll("[^a-zA-Z0-9.-]", "_").replaceAll("_+", "_").replaceAll("^_|_$", "");
        if (cleanTitle.isBlank()) {
            cleanTitle = "de-thi";
        }
        String filename = cleanTitle + (showAnswers ? "_kem_dap_an" : "") + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }
```

- [ ] **Step 6: Chạy test để đảm bảo PASS**

Run: `mvn test -Dtest=QuizSquadLeaderboardAndExportTest`
Expected: BUILD SUCCESS (3/3 tests passed).

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/group2/assignment/quiz/dto/SquadLeaderboardDto.java src/main/java/com/group2/assignment/quiz/repository/QuizAttemptRepository.java src/main/java/com/group2/assignment/quiz/service/QuizService.java src/main/java/com/group2/assignment/quiz/controller/QuizController.java src/test/java/com/group2/assignment/quiz/QuizSquadLeaderboardAndExportTest.java
git commit -m "feat(quiz): implement squad leaderboard query and export-pdf endpoint"
```

---

### Task 4: Cập Nhật Giao Diện UI (`index.html`, `take.html`, `result.html`)

**Files:**
- Modify: `src/main/resources/templates/quiz/index.html`
- Modify: `src/main/resources/templates/quiz/take.html`
- Modify: `src/main/resources/templates/quiz/result.html`
- Test: `src/test/java/com/group2/assignment/quiz/QuizFeatureGatingTest.java`

**Interfaces:**
- `index.html`: Hiển thị Widget "🏆 Bảng Xếp Hạng Squad" vinh danh Top 1 🥇, Top 2 🥈, Top 3 🥉 khi `isSquad == true`.
- `take.html`: Tự động điền `studentName` từ `session.currentUser.fullName`.
- `result.html`:
  - 2 Nút bấm Tải PDF cho Pro/Squad: `Tải Đề Thi` và `Tải Đề + Đáp Án`.
  - Nút disabled kèm nhãn `PRO VIP` cho Free.
  - Khắc phục triệt để lỗi JavaScript `showToast is not defined` bằng cách thay thế lệnh gọi bằng `showFlashcardToast()` và nhúng hàm tự hủy thông báo DaisyUI Toast sau 3.5 giây.

- [ ] **Step 1: Cập nhật `quiz/take.html`**

Cập nhật ô nhập tên thí sinh trong `take.html`:
```html
<input type="text" name="studentName"
       th:value="${session.currentUser != null ? session.currentUser.fullName : 'Sinh viên'}"
       required class="input input-sm input-bordered rounded-xl text-xs font-semibold w-56 mt-0.5" />
```

- [ ] **Step 2: Cập nhật `quiz/result.html`**

Thay thế nút in PDF bằng 2 nút tải file PDF thực thụ, và sửa triệt để lỗi `showToast is not defined` khi bấm nút Flashcards bằng cách định nghĩa hàm `showFlashcardToast()` tự hủy:
```html
<!-- Dành cho PRO & SQUAD: 2 Nút Tải PDF -->
<th:block th:if="${isPro}">
    <div class="dropdown dropdown-bottom dropdown-end">
        <label tabindex="0" class="btn btn-sm bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white font-bold rounded-xl border-none shadow-md gap-2">
            <i class="fa-solid fa-file-pdf"></i>
            <span>Tải Đề Thi PDF</span>
            <i class="fa-solid fa-chevron-down text-[10px]"></i>
        </label>
        <ul tabindex="0" class="dropdown-content z-[1] menu p-2 shadow-xl bg-slate-900 border border-slate-700 text-white rounded-2xl w-56 mt-2 text-xs">
            <li>
                <a th:href="@{'/api/quiz/' + ${result.quizId} + '/export-pdf?showAnswers=false'}" class="py-2.5 hover:bg-slate-800 rounded-xl flex items-center gap-2">
                    <i class="fa-solid fa-file-lines text-indigo-400"></i>
                    <span>Tải Đề Thi (Không đáp án)</span>
                </a>
            </li>
            <li>
                <a th:href="@{'/api/quiz/' + ${result.quizId} + '/export-pdf?showAnswers=true'}" class="py-2.5 hover:bg-slate-800 rounded-xl flex items-center gap-2">
                    <i class="fa-solid fa-file-circle-check text-emerald-400"></i>
                    <span>Tải Đề Kèm Đáp Án &amp; Lời Giải</span>
                </a>
            </li>
        </ul>
    </div>

    <!-- Nút Flashcards Spaced Repetition cho PRO/SQUAD -->
    <button type="button" onclick="showFlashcardToast()" class="btn btn-sm bg-purple-50 hover:bg-purple-100 text-purple-700 font-bold rounded-xl border border-purple-200 gap-2">
        <i class="fa-solid fa-layer-group text-purple-600"></i>
        <span>Flashcards Spaced Repetition</span>
    </button>
</th:block>

<!-- Dành cho FREE: Disabled kèm badge PRO VIP -->
<th:block th:unless="${isPro}">
    <div class="relative group">
        <button type="button" disabled class="btn btn-sm btn-disabled opacity-50 cursor-not-allowed rounded-xl gap-2">
            <i class="fa-solid fa-file-pdf"></i>
            <span>Tải Đề Thi PDF</span>
        </button>
        <span class="badge badge-secondary badge-xs absolute -top-2 -right-2 font-bold shadow-md">PRO VIP</span>
    </div>

    <div class="relative group">
        <button type="button" disabled class="btn btn-sm btn-disabled opacity-50 cursor-not-allowed rounded-xl gap-2">
            <i class="fa-solid fa-layer-group"></i>
            <span>Flashcards</span>
        </button>
        <span class="badge badge-secondary badge-xs absolute -top-2 -right-2 font-bold shadow-md">PRO VIP</span>
    </div>
</th:block>

<script>
    function showFlashcardToast() {
        const toast = document.createElement('div');
        toast.className = "toast toast-end z-50 fixed bottom-5 right-5";
        toast.innerHTML = `<div class="alert alert-info shadow-lg text-white font-semibold"><span><i class="fa-solid fa-circle-info mr-2"></i>Tính năng Flashcards Spaced Repetition đang được hoàn thiện. Vui lòng quay lại sau!</span></div>`;
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 3500);
    }
</script>
```

- [ ] **Step 3: Cập nhật `quiz/index.html`**

Bổ sung Widget Bảng Xếp Hạng Squad ngay phía trên Lịch Sử Thi:
```html
    <!-- Widget BẢNG XẾP HẠNG SQUAD (Chỉ hiển thị cho Gói SQUAD) -->
    <div th:if="${isSquad}" class="space-y-4 pt-6 border-t border-purple-200/60">
        <div class="flex items-center justify-between">
            <div class="flex items-center gap-2.5">
                <span class="w-8 h-8 rounded-xl bg-gradient-to-tr from-amber-400 to-yellow-600 flex items-center justify-center text-white text-sm shadow-md">
                    <i class="fa-solid fa-trophy"></i>
                </span>
                <div>
                    <h2 class="text-lg font-black text-slate-800">Bảng Xếp Hạng Thi Đua Nhóm (Study Squad)</h2>
                    <p class="text-xs text-slate-500">Thi đua điểm số cao nhất giữa 5 thành viên trong nhóm học tập</p>
                </div>
            </div>
            <span class="badge badge-primary font-bold text-xs gap-1.5 py-3 px-3">
                <i class="fa-solid fa-users"></i> SQUAD LEADERBOARD
            </span>
        </div>

        <div class="bg-white rounded-3xl border border-purple-100 shadow-sm overflow-hidden">
            <div th:if="${#lists.isEmpty(squadLeaderboard)}" class="p-8 text-center text-slate-400 text-xs">
                Chưa có thành viên nào trong nhóm làm bài thi môn này. Hãy là người đầu tiên bứt phá điểm số!
            </div>
            <div th:unless="${#lists.isEmpty(squadLeaderboard)}" class="overflow-x-auto">
                <table class="table text-xs w-full">
                    <thead class="bg-purple-50/70 text-purple-900 uppercase font-black tracking-wider">
                        <tr>
                            <th class="w-16 text-center">Hạng</th>
                            <th>Thành viên</th>
                            <th class="text-center">Số lần làm bài</th>
                            <th class="text-right">Điểm cao nhất</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr th:each="m, stat : ${squadLeaderboard}" class="hover:bg-purple-50/30 transition-colors"
                            th:classappend="${stat.count == 1 ? 'bg-amber-50/40' : (stat.count == 2 ? 'bg-slate-50/60' : (stat.count == 3 ? 'bg-orange-50/30' : ''))}">
                            <td class="text-center font-black text-base">
                                <span th:if="${stat.count == 1}">🥇</span>
                                <span th:if="${stat.count == 2}">🥈</span>
                                <span th:if="${stat.count == 3}">🥉</span>
                                <span th:if="${stat.count > 3}" class="text-slate-400 font-bold text-xs" th:text="${stat.count}">4</span>
                            </td>
                            <td>
                                <div class="flex items-center gap-3">
                                    <img th:src="${m.avatar}" class="w-8 h-8 rounded-full border border-purple-200" alt="avatar" />
                                    <div>
                                        <div class="font-bold text-slate-800 text-sm" th:text="${m.fullName}">Họ tên</div>
                                        <div class="text-[11px] text-slate-400 font-mono" th:text="'@' + ${m.username}">@username</div>
                                    </div>
                                </div>
                            </td>
                            <td class="text-center font-bold text-slate-600" th:text="${m.attemptCount} + ' lượt'">3 lượt</td>
                            <td class="text-right">
                                <span class="badge font-black text-sm px-3 py-3"
                                      th:classappend="${m.maxScore >= 8.0 ? 'badge-success text-white' : (m.maxScore >= 5.0 ? 'badge-warning text-white' : 'badge-error text-white')}"
                                      th:text="${m.maxScore} + ' / 10'">10.0 / 10</span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
```

- [ ] **Step 4: Chạy kiểm thử toàn bộ `mvn test`**

Run: `mvn test`
Expected: BUILD SUCCESS (Toàn bộ test suite vượt qua 100%).

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/templates/quiz/index.html src/main/resources/templates/quiz/take.html src/main/resources/templates/quiz/result.html
git commit -m "feat(quiz): render squad leaderboard widget and real pdf export buttons in templates"
```

---

### Task 5: Kiểm Thử Toàn Diện & Verification (DoD)

**Files:**
- Test: `src/test/java/com/group2/assignment/quiz/QuizDoDVerificationTest.java`

- [ ] **Step 1: Viết test kiểm tra nghiêm ngặt theo DoD**

Tạo `src/test/java/com/group2/assignment/quiz/QuizDoDVerificationTest.java` (sử dụng `@Transactional` để đảm bảo test isolation, tuyệt đối không dùng `deleteAll()`):
```java
package com.group2.assignment.quiz;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizAttempt;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.quiz.repository.QuizAttemptRepository;
import com.group2.assignment.quiz.repository.QuizQuestionRepository;
import com.group2.assignment.quiz.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Mandate rollback on all test methods, avoiding destructive deleteAll()
class QuizDoDVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    private User freeUser;
    private User proUser;
    private User squadLeader;
    private User squadMember;
    private Course testCourse;
    private Quiz testQuiz;

    @BeforeEach
    void setUp() {
        // Test isolation: All entities created in this method are rolled back automatically by @Transactional
        // Không dùng deleteAll() để bảo tồn dữ liệu seed của ứng dụng
        long timestamp = System.currentTimeMillis();

        testCourse = courseRepository.save(Course.builder()
                .code("DOD_" + timestamp)
                .name("DoD Verification Course")
                .description("Course for DoD integration tests")
                .build());

        freeUser = userRepository.save(User.builder()
                .username("dod_free_" + timestamp)
                .email("dod_free_" + timestamp + "@studyhub.vn")
                .fullName("DoD Free Student")
                .password("hash")
                .subscriptionTier("FREE")
                .role("ROLE_STUDENT")
                .build());

        proUser = userRepository.save(User.builder()
                .username("dod_pro_" + timestamp)
                .email("dod_pro_" + timestamp + "@studyhub.vn")
                .fullName("DoD Pro Student")
                .password("hash")
                .subscriptionTier("PRO")
                .role("ROLE_STUDENT")
                .build());

        squadLeader = userRepository.save(User.builder()
                .username("dod_leader_" + timestamp)
                .email("dod_leader_" + timestamp + "@studyhub.vn")
                .fullName("DoD Squad Leader")
                .password("hash")
                .subscriptionTier("SQUAD")
                .role("ROLE_STUDENT")
                .build());

        squadMember = userRepository.save(User.builder()
                .username("dod_member_" + timestamp)
                .email("dod_member_" + timestamp + "@studyhub.vn")
                .fullName("DoD Squad Member")
                .password("hash")
                .subscriptionTier("SQUAD")
                .squadLeaderUsername(squadLeader.getUsername())
                .role("ROLE_STUDENT")
                .build());

        testQuiz = quizRepository.save(Quiz.builder()
                .title("Đề thi Đánh Giá Năng Lực 2026")
                .course(testCourse)
                .difficulty("MEDIUM")
                .totalQuestions(1)
                .timeLimitMinutes(15)
                .build());

        quizQuestionRepository.save(QuizQuestion.builder()
                .quiz(testQuiz)
                .questionText("Hệ thống StudyHub sử dụng công nghệ gì cho RAG?")
                .optionA("Spring AI")
                .optionB("LangChain")
                .optionC("LlamaIndex")
                .optionD("Semantic Kernel")
                .correctAnswer("A")
                .explanation("StudyHub tích hợp Spring AI với PGvector.")
                .sortOrder(1)
                .build());
    }

    private MockHttpSession sessionFor(User user) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, user);
        return session;
    }

    @Test
    @DisplayName("DoD 1: Chưa đăng nhập gọi /api/quiz/{id}/export-pdf trả về 401 Unauthorized")
    void testUnauthenticatedExportPdfReturns401() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DoD 2: User FREE gọi /api/quiz/{id}/export-pdf bị chặn 403 Forbidden")
    void testFreeUserExportPdfReturns403() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .session(sessionFor(freeUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DoD 3: User PRO xuất PDF đề thi sạch (showAnswers=false) nhận file PDF hợp lệ với tên file khử dấu Đ/đ")
    void testProUserExportPdfClean() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "false")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("Content-Disposition", containsString("filename=\"De_thi_Danh_Gia_Nang_Luc_2026.pdf\"")));
    }

    @Test
    @DisplayName("DoD 4: User PRO xuất PDF kèm đáp án (showAnswers=true) có hậu tố _kem_dap_an.pdf")
    void testProUserExportPdfWithAnswers() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "true")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("Content-Disposition", containsString("filename=\"De_thi_Danh_Gia_Nang_Luc_2026_kem_dap_an.pdf\"")));
    }

    @Test
    @DisplayName("DoD 5: Bảng xếp hạng Squad: Hiển thị bảng xếp hạng khi thành viên Squad xem trang quiz")
    void testSquadLeaderboardOnQuizIndex() throws Exception {
        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(testQuiz)
                .user(squadLeader)
                .studentName(squadLeader.getFullName())
                .totalScore(9.0)
                .correctCount(1)
                .totalCount(1)
                .build());

        mockMvc.perform(get("/quiz")
                        .param("courseId", testCourse.getId().toString())
                        .session(sessionFor(squadMember)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("squadLeaderboard"))
                .andExpect(model().attribute("isSquad", true));
    }

    @Test
    @DisplayName("DoD 6: Tương thích ngược: QuizAttempt lưu thành công với user = null")
    void testQuizAttemptNullableUserCompatibility() {
        QuizAttempt anonymousAttempt = QuizAttempt.builder()
                .quiz(testQuiz)
                .user(null) // Backward compatibility verify: nullable = true
                .studentName("Khách Vãng Lai")
                .totalScore(7.5)
                .correctCount(1)
                .totalCount(1)
                .build();

        QuizAttempt saved = quizAttemptRepository.save(anonymousAttempt);
        assertNotNull(saved.getId());
        assertNull(saved.getUser());
    }
}
```

- [ ] **Step 2: Chạy test DoD**

Run: `mvn test -Dtest=QuizDoDVerificationTest`
Expected: PASS.

- [ ] **Step 3: Chạy full test suite và kiểm tra code cleanliness**

Run: `mvn clean test`
Expected: BUILD SUCCESS (tất cả các bài test trong dự án pass).

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/group2/assignment/quiz/QuizDoDVerificationTest.java
git commit -m "test(quiz): add comprehensive DoD verification tests for free vs pro and squad"
```

