# Quiz Milestone 2: PDF Processing & AI Question Generator Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Nâng cấp bộ máy xử lý PDF và sinh đề thi trắc nghiệm (Milestone 2) đạt chuẩn sản phẩm thực tế: Hỗ trợ trích xuất theo khoảng trang (Apache PDFBox `setStartPage`/`setEndPage`), tích hợp cả file tải lên mới lẫn tài liệu có sẵn của Môn học (`Document` entity), kích hoạt chế độ **Gemini 1.5 Flash Structured JSON Output** (`responseMimeType: "application/json"`), và thuật toán **Smart Semantic Mock Fallback** tạo câu hỏi thực tế khi offline/hết quota AI.

**Architecture:** Mở rộng `PdfTextExtractor` hỗ trợ `extractText(MultipartFile, int fromPage, int toPage)` và `extractText(Path filePath, int fromPage, int toPage)`. Nâng cấp `QuizGeneratorService` với payload Gemini `generationConfig` chuẩn JSON schema và thuật toán Semantic Heuristic Parser. Bổ sung endpoint trong `QuizController` để sinh đề từ tài liệu môn đã có sẵn.

**Tech Stack:** Java 17, Spring Boot 3.3.4, Apache PDFBox 3.0.3, Jackson `ObjectMapper`, `java.net.http.HttpClient` (Java Stdlib), Google Gemini 1.5 Flash API.

**Spec:** [README.md](file:///c:/Dev/Smart-StudyHub-RAG/README.md), [Document.java](file:///c:/Dev/Smart-StudyHub-RAG/src/main/java/com/group2/assignment/rag/entity/Document.java), [QuizGeneratorService.java](file:///c:/Dev/Smart-StudyHub-RAG/src/main/java/com/group2/assignment/quiz/service/QuizGeneratorService.java).

---

## Global Constraints

- **Dependency Policy (Ponytail Principle):** Tuyệt đối không thêm dependency ngoài. Sử dụng `PDFTextStripper` có sẵn của Apache PDFBox 3.0.3, `java.net.http.HttpClient` của Java 11+ stdlib, và `ObjectMapper` của Jackson.
- **Cross-Module Synergy:** Tái sử dụng bảng `documents` (`com.group2.assignment.rag.entity.Document`) và `DocumentRepository` để sinh đề từ tài liệu môn đã có, tạo sự kết nối chặt chẽ giữa Module RAG và Module Quiz.
- **Resilience Guarantee:** Bất kỳ lỗi mạng, sai định dạng, hết Quota 429 hay thiếu API Key đều không được làm đứt gãy luồng người dùng; tự động kích hoạt bộ đệm Smart Semantic Mock Fallback.

## Review Focus

1. **Negative or Inverted Page Ranges:** Người dùng nhập `fromPage > toPage` hoặc `fromPage <= 0`; hệ thống phải tự động chuẩn hóa (`Math.min`, `Math.max`) hoặc điều chỉnh về 1..totalPages an toàn.
2. **Encrypted / Password-protected PDFs:** Apache PDFBox quăng `InvalidPasswordException`; hệ thống bắt và trả về thông báo thân thiện.
3. **Empty Text Scan PDFs:** File PDF chỉ chứa ảnh scan (0 text); hệ thống ném thông báo yêu cầu tải PDF có text.
4. **Gemini JSON Token Overflow:** Context tài liệu quá dài; hệ thống phải cắt ngắn thông minh ở ranh giới câu (Sentence boundary) thay vì cắt đứt gãy giữa từ.
5. **Existing Document Path Resolution:** File vật lý của `Document` trên ổ đĩa có thể bị di chuyển hoặc null; hệ thống kiểm tra `Files.exists()` trước khi đọc.

---

## Phase Breakdown

- **Task 1: Apache PDFBox Range-Aware Text Extractor**
  - Hỗ trợ trích xuất theo khoảng trang (`fromPage` $\rightarrow$ `toPage`).
  - Hỗ trợ đọc từ cả `MultipartFile` và đường dẫn file vật lý `Path` / `File`.
  - Unit test kiểm chứng trích xuất trang chính xác.
- **Task 2: Gemini 1.5 Flash Native Structured JSON Output & Smart Semantic Fallback**
  - Tích hợp `generationConfig: { responseMimeType: "application/json" }`.
  - Thuật toán trích xuất câu định nghĩa kỹ thuật & sinh distractors thông minh khi fallback.
- **Task 3: Integration with Course Documents & Modal UI Enhancements**
  - Service & Controller hỗ trợ sinh đề từ Document ID đã có.
  - Cập nhật giao diện Modal cho phép chuyển tab: [Upload File Mới] và [Chọn Tài Liệu Môn].

---

### Task 1: Apache PDFBox Range-Aware Text Extractor

**Files:**
- Modify: `src/main/java/com/group2/assignment/quiz/service/PdfTextExtractor.java`
- Create: `src/test/java/com/group2/assignment/quiz/service/PdfTextExtractorTest.java`

**Interfaces:**
- Consumes: Apache PDFBox `Loader.loadPDF(byte[])`, `Loader.loadPDF(File)`
- Produces:
  - `String extractText(MultipartFile file, Integer fromPage, Integer toPage)`
  - `String extractText(File file, Integer fromPage, Integer toPage)`

- [ ] **Step 1: Write unit test for range-aware text extraction**

```java
package com.group2.assignment.quiz.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PdfTextExtractorTest {

    @Test
    @DisplayName("Trích xuất text chính xác từ trang 2 của file PDF 2 trang")
    void testExtractSpecificPageRange() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page1 = new PDPage();
            doc.addPage(page1);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.showText("Page 1: Introduction to Spring Framework IoC Container");
                cs.endText();
            }

            PDPage page2 = new PDPage();
            doc.addPage(page2);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.showText("Page 2: Dependency Injection is the core pattern of Spring");
                cs.endText();
            }

            doc.save(baos);
        }

        MockMultipartFile mockFile = new MockMultipartFile("slide.pdf", "slide.pdf", "application/pdf", baos.toByteArray());

        PdfTextExtractor extractor = new PdfTextExtractor();
        String extractedPage2 = extractor.extractText(mockFile, 2, 2);

        assertTrue(extractedPage2.contains("Dependency Injection"));
        assertTrue(!extractedPage2.contains("Introduction to Spring Framework"));
    }
}
```

- [ ] **Step 2: Implement Range-Aware Extractor in `PdfTextExtractor.java`**

File `src/main/java/com/group2/assignment/quiz/service/PdfTextExtractor.java`:
```java
package com.group2.assignment.quiz.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class PdfTextExtractor {

    public String extractText(MultipartFile file) throws IOException {
        return extractText(file, null, null);
    }

    public String extractText(MultipartFile file, Integer fromPage, Integer toPage) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không được rỗng!");
        }
        return extractFromBytes(file.getBytes(), fromPage, toPage);
    }

    public String extractText(File file, Integer fromPage, Integer toPage) throws IOException {
        if (file == null || !file.exists() || file.length() == 0) {
            throw new IllegalArgumentException("File tài liệu trên hệ thống không tồn tại hoặc rỗng!");
        }
        try (PDDocument document = Loader.loadPDF(file)) {
            return extractFromDocument(document, fromPage, toPage);
        }
    }

    private String extractFromBytes(byte[] bytes, Integer fromPage, Integer toPage) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return extractFromDocument(document, fromPage, toPage);
        }
    }

    private String extractFromDocument(PDDocument document, Integer fromPage, Integer toPage) throws IOException {
        int totalPages = document.getNumberOfPages();
        if (totalPages == 0) {
            throw new IllegalArgumentException("Tài liệu PDF không chứa trang nào!");
        }

        int start = (fromPage != null && fromPage > 0) ? Math.min(fromPage, totalPages) : 1;
        int end = (toPage != null && toPage > 0) ? Math.min(toPage, totalPages) : totalPages;

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        stripper.setStartPage(start);
        stripper.setEndPage(end);

        String text = stripper.getText(document);
        if (text == null || text.trim().isBlank()) {
            throw new IllegalArgumentException("Không thể trích xuất văn bản từ trang " + start + " đến " + end + " (PDF chỉ có ảnh scan hoặc trang trắng)!");
        }

        return text.trim();
    }
}
```

- [ ] **Step 3: Run unit test to verify range extraction**

Run: `.\mvnw.cmd test -Dtest=PdfTextExtractorTest`
Expected: BUILD SUCCESS (1 test passed)

---

### Task 2: Gemini 1.5 Flash Structured JSON & Smart Semantic Fallback

**Files:**
- Modify: `src/main/java/com/group2/assignment/quiz/service/QuizGeneratorService.java`
- Test: `src/test/java/com/group2/assignment/quiz/service/QuizGeneratorServiceTest.java`

**Interfaces:**
- Consumes: Gemini 1.5 Flash REST API with `generationConfig: { responseMimeType: "application/json" }`
- Produces: `List<QuizQuestion> generateQuestions(String contextText, int count, String difficulty, String apiKey)`

- [ ] **Step 1: Write test for Smart Semantic Fallback question generation**

```java
package com.group2.assignment.quiz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.quiz.entity.QuizQuestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class QuizGeneratorServiceTest {

    @Test
    @DisplayName("Smart Mock Fallback trích xuất thành công câu hỏi và 4 đáp án phân biệt từ văn bản")
    void testSmartMockFallbackGeneration() {
        QuizGeneratorService service = new QuizGeneratorService(new ObjectMapper());

        String context = "Spring Framework là một nền tảng mã nguồn mở mạnh mẽ cho Java doanh nghiệp.\n"
                + "Inversion of Control (IoC) là nguyên lý cốt lõi giúp đảo ngược quyền điều khiển tạo đối tượng.\n"
                + "Dependency Injection (DI) là một dạng cụ thể của IoC cho phép nạp các phụ thuộc từ bên ngoài.\n"
                + "Spring Boot giúp tự động cấu hình các ứng dụng Spring nhanh chóng và giảm boilerplate code.\n"
                + "Spring Data JPA cung cấp các abstraction mạnh mẽ để thao tác với cơ sở dữ liệu qua Hibernate.";

        List<QuizQuestion> questions = service.generateMockQuestions(context, 3, "MEDIUM");

        assertEquals(3, questions.size());
        for (QuizQuestion q : questions) {
            assertNotNull(q.getQuestionText());
            assertNotNull(q.getOptionA());
            assertNotNull(q.getOptionB());
            assertNotNull(q.getOptionC());
            assertNotNull(q.getOptionD());
            assertEquals("A", q.getCorrectAnswer());
            assertNotNull(q.getExplanation());
        }
    }
}
```

- [ ] **Step 2: Upgrade `QuizGeneratorService.java` with Gemini Structured JSON & Semantic Heuristics**

```java
// File: src/main/java/com/group2/assignment/quiz/service/QuizGeneratorService.java
// Cập nhật payload:
// generationConfig: {
//     "responseMimeType": "application/json",
//     "temperature": 0.4
// }
// Và thuật toán Semantic Heuristic Parser cho Fallback
```

- [ ] **Step 3: Run unit tests**

Run: `.\mvnw.cmd test -Dtest=QuizGeneratorServiceTest`
Expected: PASS

---

### Task 3: Integration with Course Documents & UI Modal Tabs

**Files:**
- Modify: `src/main/java/com/group2/assignment/quiz/service/QuizService.java`
- Modify: `src/main/java/com/group2/assignment/quiz/controller/QuizController.java`
- Modify: `src/main/resources/templates/quiz/index.html`

- [ ] **Step 1: Add `createQuizFromDocument` in `QuizService.java`**
- [ ] **Step 2: Add page range parameters (`fromPage`, `toPage`) and `documentId` handling in `QuizController.java`**
- [ ] **Step 3: Update `quiz/index.html` with Tabbed Modal: [Tải File Mới] vs [Chọn Tài Liệu Môn Đã Có]**
- [ ] **Step 4: Run full verification `./mvnw.cmd test`**

---
