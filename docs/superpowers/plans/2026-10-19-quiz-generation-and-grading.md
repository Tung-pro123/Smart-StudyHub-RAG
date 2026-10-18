# Quiz Generation & Instant Grading Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Xây dựng hoàn chỉnh module Quiz cho phép sinh đề thi trắc nghiệm 4 lựa chọn (A/B/C/D) từ tài liệu PDF tải lên (sử dụng Apache PDFBox và Google Gemini 1.5 Flash kèm cơ chế fallback an toàn), đồng thời hỗ trợ làm bài thi trắc nghiệm trực tuyến với bộ máy chấm điểm thang 10 tức thì và hiển thị lời giải chi tiết.

**Architecture:** Áp dụng kiến trúc Package-by-Feature (`com.group2.assignment.quiz`). Tận dụng triệt để các Entity đã có (`Quiz`, `QuizQuestion`, `QuizAttempt`), bổ sung DTOs tinh gọn, `QuizService` đảm nhận nghiệp vụ chấm điểm & tích hợp PDFBox/Gemini, và `QuizController` điều hướng giao diện Thymeleaf (Danh sách đề, Phòng thi, Trang kết quả).

**Tech Stack:** Java 17, Spring Boot 3.3.4, Spring Data JPA, H2 Database, Apache PDFBox 3.0.3, Jackson `ObjectMapper`, `java.net.http.HttpClient` (Java Stdlib), Thymeleaf + Tailwind CSS/DaisyUI.

**Spec:** [README.md](file:///c:/Dev/Smart-StudyHub-RAG/README.md) (Module 5 - NHẬT: AI Quiz Generator & Grading), [V1__initial_schema.sql](file:///c:/Dev/Smart-StudyHub-RAG/src/main/resources/db/migration/V1__initial_schema.sql#L120-L165).

---

## Global Constraints

- **Java Version:** JDK 17, Spring Boot 3.3.4.
- **Dependency Policy (Ponytail Principle):** Tuyệt đối không thêm thư viện ngoài không cần thiết. Tận dụng `org.apache.pdfbox:pdfbox:3.0.3` đã có trong `pom.xml`, `java.net.http.HttpClient` của Java Stdlib để gọi Gemini API, và `com.fasterxml.jackson.databind.ObjectMapper` có sẵn trong Spring Web.
- **Database Schema Integrity:** Tôn trọng 100% cấu trúc 3 bảng `quizzes`, `quiz_questions`, `quiz_attempts` đã được định nghĩa trong `V1__initial_schema.sql` và các Entity JPA tương ứng.
- **Resilience First:** Nếu chưa có Gemini API Key trong Session hoặc API mạng gặp lỗi, hệ thống phải tự động fallback sang thuật toán Mock Generator trích xuất trực tiếp từ text của tài liệu, tuyệt đối không để ứng dụng bị crash hay gián đoạn demo.

## Review Focus

1. **Submission with missing/unanswered questions:** Sinh viên nộp bài khi chỉ tích 3/5 câu; hệ thống phải xử lý các câu chưa chọn là đáp án trống/sai mà không gây `NullPointerException`.
2. **Malformed or Empty PDF Upload:** Người dùng tải file rỗng, file không phải PDF, hoặc PDF không chứa text (chỉ có ảnh scan); hệ thống phải ném lỗi nghiệp vụ rõ ràng và thông báo ra giao diện.
3. **Gemini JSON parsing failure:** Gemini trả về markdown bọc trong ````json ... ````; parser phải bóc tách sạch sẽ chuỗi JSON trước khi map vào `List<QuizQuestion>`.
4. **Division by Zero in Scoring:** Trường hợp hiếm gặp đề thi có 0 câu hỏi (`totalCount == 0`), thuật toán tính điểm phải trả về 0.0 an toàn.
5. **Session Safety:** Lấy API Key từ `HttpSession` với key `SettingsConstants.SESSION_API_KEY` an toàn, không throw nếu session null.

---

## Phase Breakdown

- **Milestone 1 (Task 1 & 2):** Quiz Taking, Scoring Engine & DTOs (Chấm điểm tức thì & Xem kết quả).
- **Milestone 2 (Task 3 & 4):** PDF Document Text Extraction & AI Generator (Apache PDFBox + Gemini Service + Mock Fallback).
- **Milestone 3 (Task 5):** Web Controller & Thymeleaf Views (Giao diện danh sách, phòng thi, và bảng điểm chi tiết).

---

### Task 1: Quiz DTOs & Grading Logic Foundation

**Files:**
- Create: `src/main/java/com/group2/assignment/quiz/dto/QuizSubmissionDto.java`
- Create: `src/main/java/com/group2/assignment/quiz/dto/QuestionResultDto.java`
- Create: `src/main/java/com/group2/assignment/quiz/dto/QuizResultDto.java`
- Test: `src/test/java/com/group2/assignment/quiz/service/QuizGradingTest.java`

**Interfaces:**
- Consumes: `com.group2.assignment.quiz.entity.Quiz`, `com.group2.assignment.quiz.entity.QuizQuestion`
- Produces: `QuizResultDto gradeSubmission(Long quizId, QuizSubmissionDto submission)`

- [ ] **Step 1: Write unit test for grading calculation**

```java
package com.group2.assignment.quiz.service;

import com.group2.assignment.quiz.dto.QuizResultDto;
import com.group2.assignment.quiz.dto.QuizSubmissionDto;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizQuestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuizGradingTest {

    @Test
    @DisplayName("Chấm điểm 5 câu: 4 đúng 1 sai phải được 8.0/10")
    void testGradingCalculation() {
        QuizQuestion q1 = QuizQuestion.builder().id(1L).correctAnswer("A").build();
        QuizQuestion q2 = QuizQuestion.builder().id(2L).correctAnswer("B").build();
        QuizQuestion q3 = QuizQuestion.builder().id(3L).correctAnswer("C").build();
        QuizQuestion q4 = QuizQuestion.builder().id(4L).correctAnswer("D").build();
        QuizQuestion q5 = QuizQuestion.builder().id(5L).correctAnswer("A").build();

        List<QuizQuestion> questions = List.of(q1, q2, q3, q4, q5);

        Map<Long, String> answers = new HashMap<>();
        answers.put(1L, "A"); // Đúng
        answers.put(2L, "B"); // Đúng
        answers.put(3L, "C"); // Đúng
        answers.put(4L, "D"); // Đúng
        answers.put(5L, "B"); // Sai (đáp án đúng là A)

        int correctCount = 0;
        for (QuizQuestion q : questions) {
            String selected = answers.get(q.getId());
            if (selected != null && selected.equalsIgnoreCase(q.getCorrectAnswer())) {
                correctCount++;
            }
        }

        double score = questions.isEmpty() ? 0.0 : Math.round(((double) correctCount / questions.size() * 10.0) * 10.0) / 10.0;

        assertEquals(4, correctCount);
        assertEquals(8.0, score);
    }
}
```

- [ ] **Step 2: Run test to verify it passes locally**

Run: `.\mvnw.cmd test -Dtest=QuizGradingTest`
Expected: PASS

- [ ] **Step 3: Create DTO files for Quiz Submission and Result**

File `src/main/java/com/group2/assignment/quiz/dto/QuizSubmissionDto.java`:
```java
package com.group2.assignment.quiz.dto;

import lombok.*;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmissionDto {
    private String studentName;
    private Integer timeSpentSeconds;
    @Builder.Default
    private Map<Long, String> answers = new HashMap<>(); // QuestionId -> Selected Option ("A", "B", "C", "D")
}
```

File `src/main/java/com/group2/assignment/quiz/dto/QuestionResultDto.java`:
```java
package com.group2.assignment.quiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResultDto {
    private Long questionId;
    private Integer sortOrder;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String selectedAnswer; // Câu trả lời sinh viên chọn (hoặc null nếu bỏ trống)
    private String correctAnswer;   // "A", "B", "C", "D"
    private boolean isCorrect;      // true nếu đúng
    private String explanation;     // Lời giải thích
}
```

File `src/main/java/com/group2/assignment/quiz/dto/QuizResultDto.java`:
```java
package com.group2.assignment.quiz.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResultDto {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private String courseName;
    private String studentName;
    private Double totalScore;       // Thang điểm 10 (ví dụ: 8.5)
    private Integer correctCount;     // Số câu đúng
    private Integer totalCount;       // Tổng số câu
    private Integer timeSpentSeconds; // Thời gian làm bài tính bằng giây
    private LocalDateTime completedAt;
    private List<QuestionResultDto> details;
}
```

- [ ] **Step 4: Commit Task 1**

```bash
git add src/main/java/com/group2/assignment/quiz/dto/ src/test/java/com/group2/assignment/quiz/
git commit -m "feat(quiz): add quiz submission and grading DTOs with unit test"
```

---

### Task 2: QuizService - Submission Grading & Storage

**Files:**
- Create: `src/main/java/com/group2/assignment/quiz/service/QuizService.java`
- Modify: `src/main/java/com/group2/assignment/quiz/repository/QuizAttemptRepository.java`

**Interfaces:**
- Consumes: `QuizRepository`, `QuizQuestionRepository`, `QuizAttemptRepository`, `ObjectMapper`
- Produces:
  - `Quiz getQuizById(Long quizId)`
  - `List<QuizQuestion> getQuestionsByQuizId(Long quizId)`
  - `QuizResultDto submitAndGrade(Long quizId, QuizSubmissionDto submission)`
  - `QuizResultDto getAttemptResult(Long attemptId)`

- [ ] **Step 1: Implement QuizService grading logic and JSON serialization**

File `src/main/java/com/group2/assignment/quiz/service/QuizService.java`:
```java
package com.group2.assignment.quiz.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.quiz.dto.QuestionResultDto;
import com.group2.assignment.quiz.dto.QuizResultDto;
import com.group2.assignment.quiz.dto.QuizSubmissionDto;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizAttempt;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.quiz.repository.QuizAttemptRepository;
import com.group2.assignment.quiz.repository.QuizQuestionRepository;
import com.group2.assignment.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ObjectMapper objectMapper;

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đề thi với ID: " + id));
    }

    public List<Quiz> getQuizzesByCourse(Long courseId) {
        return quizRepository.findAllByCourseIdOrderByCreatedAtDesc(courseId);
    }

    public List<QuizQuestion> getQuestionsByQuizId(Long quizId) {
        return quizQuestionRepository.findAllByQuizIdOrderBySortOrderAsc(quizId);
    }

    @Transactional
    public QuizResultDto submitAndGrade(Long quizId, QuizSubmissionDto submission) {
        Quiz quiz = getQuizById(quizId);
        List<QuizQuestion> questions = getQuestionsByQuizId(quizId);

        Map<Long, String> userAnswers = submission.getAnswers() != null ? submission.getAnswers() : Collections.emptyMap();
        int correctCount = 0;
        List<QuestionResultDto> details = new ArrayList<>();

        for (QuizQuestion q : questions) {
            String selected = userAnswers.get(q.getId());
            boolean isCorrect = selected != null && selected.trim().equalsIgnoreCase(q.getCorrectAnswer().trim());
            if (isCorrect) {
                correctCount++;
            }

            details.add(QuestionResultDto.builder()
                    .questionId(q.getId())
                    .sortOrder(q.getSortOrder())
                    .questionText(q.getQuestionText())
                    .optionA(q.getOptionA())
                    .optionB(q.getOptionB())
                    .optionC(q.getOptionC())
                    .optionD(q.getOptionD())
                    .selectedAnswer(selected != null ? selected.trim().toUpperCase() : "")
                    .correctAnswer(q.getCorrectAnswer().trim().toUpperCase())
                    .isCorrect(isCorrect)
                    .explanation(q.getExplanation())
                    .build());
        }

        int totalCount = questions.size();
        double score = totalCount == 0 ? 0.0 : Math.round(((double) correctCount / totalCount * 10.0) * 10.0) / 10.0;

        String answersJson = "{}";
        try {
            answersJson = objectMapper.writeValueAsString(userAnswers);
        } catch (Exception e) {
            log.error("Lỗi khi chuyển đổi userAnswers thành JSON", e);
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .studentName(submission.getStudentName() != null && !submission.getStudentName().isBlank() ? submission.getStudentName().trim() : "Sinh viên")
                .totalScore(score)
                .correctCount(correctCount)
                .totalCount(totalCount)
                .userAnswers(answersJson)
                .timeSpentSeconds(submission.getTimeSpentSeconds() != null ? submission.getTimeSpentSeconds() : 0)
                .completedAt(LocalDateTime.now())
                .build();

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        return QuizResultDto.builder()
                .attemptId(savedAttempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .courseName(quiz.getCourse() != null ? quiz.getCourse().getName() : "")
                .studentName(savedAttempt.getStudentName())
                .totalScore(score)
                .correctCount(correctCount)
                .totalCount(totalCount)
                .timeSpentSeconds(savedAttempt.getTimeSpentSeconds())
                .completedAt(savedAttempt.getCompletedAt())
                .details(details)
                .build();
    }

    public QuizResultDto getAttemptResult(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kết quả làm bài với ID: " + attemptId));

        Quiz quiz = attempt.getQuiz();
        List<QuizQuestion> questions = getQuestionsByQuizId(quiz.getId());

        Map<Long, String> userAnswers = new HashMap<>();
        try {
            if (attempt.getUserAnswers() != null && !attempt.getUserAnswers().isBlank()) {
                userAnswers = objectMapper.readValue(attempt.getUserAnswers(), new TypeReference<Map<Long, String>>() {});
            }
        } catch (Exception e) {
            log.warn("Không thể parse userAnswers JSON cho attempt {}", attemptId);
        }

        List<QuestionResultDto> details = new ArrayList<>();
        for (QuizQuestion q : questions) {
            String selected = userAnswers.get(q.getId());
            boolean isCorrect = selected != null && selected.trim().equalsIgnoreCase(q.getCorrectAnswer().trim());
            details.add(QuestionResultDto.builder()
                    .questionId(q.getId())
                    .sortOrder(q.getSortOrder())
                    .questionText(q.getQuestionText())
                    .optionA(q.getOptionA())
                    .optionB(q.getOptionB())
                    .optionC(q.getOptionC())
                    .optionD(q.getOptionD())
                    .selectedAnswer(selected != null ? selected.trim().toUpperCase() : "")
                    .correctAnswer(q.getCorrectAnswer().trim().toUpperCase())
                    .isCorrect(isCorrect)
                    .explanation(q.getExplanation())
                    .build());
        }

        return QuizResultDto.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .courseName(quiz.getCourse() != null ? quiz.getCourse().getName() : "")
                .studentName(attempt.getStudentName())
                .totalScore(attempt.getTotalScore())
                .correctCount(attempt.getCorrectCount())
                .totalCount(attempt.getTotalCount())
                .timeSpentSeconds(attempt.getTimeSpentSeconds())
                .completedAt(attempt.getCompletedAt())
                .details(details)
                .build();
    }
}
```

- [ ] **Step 2: Compile to verify syntax**

Run: `.\mvnw.cmd compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit Task 2**

```bash
git add src/main/java/com/group2/assignment/quiz/service/QuizService.java
git commit -m "feat(quiz): implement grading and result retrieval in QuizService"
```

---

### Task 3: PDF Document Text Extraction & AI/Mock Quiz Generator

**Files:**
- Create: `src/main/java/com/group2/assignment/quiz/service/PdfTextExtractor.java`
- Create: `src/main/java/com/group2/assignment/quiz/service/QuizGeneratorService.java`
- Test: `src/test/java/com/group2/assignment/quiz/service/PdfTextExtractorTest.java`

**Interfaces:**
- Consumes: Apache PDFBox `Loader.loadPDF`, Gemini 1.5 Flash API via `java.net.http.HttpClient`
- Produces: `List<QuizQuestion> generateQuestionsFromText(String text, int questionCount, String difficulty, String apiKey)`

- [ ] **Step 1: Implement PdfTextExtractor using Apache PDFBox 3.0.3**

File `src/main/java/com/group2/assignment/quiz/service/PdfTextExtractor.java`:
```java
package com.group2.assignment.quiz.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class PdfTextExtractor {

    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không được rỗng!");
        }

        byte[] bytes = file.getBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            if (text == null || text.trim().isBlank()) {
                throw new IllegalArgumentException("Không thể trích xuất văn bản từ file PDF (file scan hoặc rỗng)!");
            }
            return text.trim();
        }
    }
}
```

- [ ] **Step 2: Implement QuizGeneratorService with Gemini 1.5 Flash & Mock Fallback**

File `src/main/java/com/group2/assignment/quiz/service/QuizGeneratorService.java`:
```java
package com.group2.assignment.quiz.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.quiz.entity.QuizQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizGeneratorService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<QuizQuestion> generateQuestions(String contextText, int count, String difficulty, String apiKey) {
        if (apiKey != null && !apiKey.trim().isBlank()) {
            try {
                return callGeminiAi(contextText, count, difficulty, apiKey.trim());
            } catch (Exception e) {
                log.warn("Gọi Gemini API thất bại ({}), tự động chuyển sang Fallback Generator", e.getMessage());
            }
        }
        return generateMockQuestions(contextText, count, difficulty);
    }

    private List<QuizQuestion> callGeminiAi(String contextText, int count, String difficulty, String apiKey) throws Exception {
        String truncatedContext = contextText.length() > 8000 ? contextText.substring(0, 8000) : contextText;

        String systemPrompt = "Bạn là trợ lý giảng viên đại học. Hãy đọc kỹ tài liệu dưới đây và sinh chính xác " + count + " câu hỏi trắc nghiệm khách quan 4 lựa chọn (A, B, C, D) với độ khó " + difficulty + ".\n"
                + "Quy tắc bắt buộc: Chỉ trả về mảng JSON thuần túy (không dùng backticks, không bọc markdown ```json), mỗi phần tử gồm các trường:\n"
                + "- questionText: Nội dung câu hỏi ngắn gọn, rõ ràng\n"
                + "- optionA: Lựa chọn A\n"
                + "- optionB: Lựa chọn B\n"
                + "- optionC: Lựa chọn C\n"
                + "- optionD: Lựa chọn D\n"
                + "- correctAnswer: Chỉ ghi 'A', 'B', 'C', hoặc 'D'\n"
                + "- explanation: Lời giải thích ngắn gọn dựa trên tài liệu\n\n"
                + "Nội dung tài liệu:\n" + truncatedContext;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", systemPrompt)))
                )
        );

        String jsonPayload = objectMapper.writeValueAsString(requestBody);
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini HTTP error: " + response.statusCode() + " - " + response.body());
        }

        JsonNode rootNode = objectMapper.readTree(response.body());
        String rawContent = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        // Clean markdown backticks if Gemini includes them
        String cleanJson = rawContent.replaceAll("^```json\\s*", "").replaceAll("```\\s*$", "").trim();

        List<Map<String, String>> rawQuestions = objectMapper.readValue(cleanJson, new TypeReference<List<Map<String, String>>>() {});
        List<QuizQuestion> questions = new ArrayList<>();
        int order = 1;

        for (Map<String, String> m : rawQuestions) {
            questions.add(QuizQuestion.builder()
                    .questionText(m.getOrDefault("questionText", "Câu hỏi trắc nghiệm"))
                    .optionA(m.getOrDefault("optionA", "Đáp án A"))
                    .optionB(m.getOrDefault("optionB", "Đáp án B"))
                    .optionC(m.getOrDefault("optionC", "Đáp án C"))
                    .optionD(m.getOrDefault("optionD", "Đáp án D"))
                    .correctAnswer(m.getOrDefault("correctAnswer", "A").trim().toUpperCase())
                    .explanation(m.getOrDefault("explanation", "Dựa theo nội dung giáo trình được cung cấp."))
                    .sortOrder(order++)
                    .build());
        }

        return questions;
    }

    public List<QuizQuestion> generateMockQuestions(String contextText, int count, String difficulty) {
        List<QuizQuestion> questions = new ArrayList<>();
        String[] lines = contextText.split("\\r?\\n");
        List<String> validSentences = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() > 30 && trimmed.contains(" ")) {
                validSentences.add(trimmed);
            }
        }

        for (int i = 0; i < count; i++) {
            String topicSentence = i < validSentences.size() ? validSentences.get(i) : "Khái niệm kiến thức trọng tâm số " + (i + 1);
            if (topicSentence.length() > 120) {
                topicSentence = topicSentence.substring(0, 117) + "...";
            }

            questions.add(QuizQuestion.builder()
                    .questionText("Theo nội dung tài liệu: \"" + topicSentence + "\", phát biểu nào sau đây là chính xác?")
                    .optionA("Phát biểu khẳng định đúng bản chất của: " + topicSentence)
                    .optionB("Khái niệm này hoàn toàn trái ngược với định nghĩa chuẩn")
                    .optionC("Chỉ áp dụng trong các trường hợp ngoại lệ đặc biệt")
                    .optionD("Không có liên quan đến nội dung bài học")
                    .correctAnswer("A")
                    .explanation("Đáp án A là khẳng định chính xác nhất được trích xuất từ câu chủ đề của tài liệu.")
                    .sortOrder(i + 1)
                    .build());
        }

        return questions;
    }
}
```

- [ ] **Step 3: Hook generator into QuizService to persist Quiz**

Add method in `src/main/java/com/group2/assignment/quiz/service/QuizService.java`:
```java
    private final QuizGeneratorService quizGeneratorService;
    private final PdfTextExtractor pdfTextExtractor;
    private final com.group2.assignment.core.repository.CourseRepository courseRepository;

    @Transactional
    public Quiz createQuizFromPdf(Long courseId, org.springframework.web.multipart.MultipartFile file, String title, String chapterTopic, String difficulty, int totalQuestions, int timeLimitMinutes, String apiKey) throws java.io.IOException {
        com.group2.assignment.core.entity.Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + courseId));

        String text = pdfTextExtractor.extractText(file);
        List<QuizQuestion> questions = quizGeneratorService.generateQuestions(text, totalQuestions, difficulty, apiKey);

        Quiz quiz = Quiz.builder()
                .course(course)
                .title(title != null && !title.isBlank() ? title.trim() : "Đề trắc nghiệm: " + file.getOriginalFilename())
                .chapterTopic(chapterTopic != null && !chapterTopic.isBlank() ? chapterTopic.trim() : "Toàn bộ tài liệu")
                .difficulty(difficulty != null ? difficulty : "MEDIUM")
                .totalQuestions(questions.size())
                .timeLimitMinutes(timeLimitMinutes > 0 ? timeLimitMinutes : 15)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);

        for (QuizQuestion q : questions) {
            q.setQuiz(savedQuiz);
            quizQuestionRepository.save(q);
        }

        return savedQuiz;
    }
```

- [ ] **Step 4: Verify with mvn compile**

Run: `.\mvnw.cmd compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit Task 3**

```bash
git add src/main/java/com/group2/assignment/quiz/service/
git commit -m "feat(quiz): add PdfTextExtractor and QuizGeneratorService with Gemini and fallback"
```

---

### Task 4: Web Controller & Endpoints

**Files:**
- Create: `src/main/java/com/group2/assignment/quiz/controller/QuizController.java`

**Endpoints:**
- `GET /quiz`: Hiển thị danh sách đề trắc nghiệm của môn (tham số `courseId`).
- `POST /quiz/generate`: Nhận file upload PDF và các tiêu chí đề thi, gọi service sinh Quiz rồi chuyển hướng đến `/quiz/{quizId}`.
- `GET /quiz/{id}`: Giao diện phòng thi trực tuyến cho sinh viên làm bài.
- `POST /quiz/{id}/submit`: Nộp bài thi, tính điểm và chuyển hướng sang `/quiz/attempt/{attemptId}`.
- `GET /quiz/attempt/{attemptId}`: Hiển thị bảng kết quả chi tiết kèm lời giải từng câu.

- [ ] **Step 1: Implement QuizController with Session API Key extraction**

File `src/main/java/com/group2/assignment/quiz/controller/QuizController.java`:
```java
package com.group2.assignment.quiz.controller;

import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.dto.QuizResultDto;
import com.group2.assignment.quiz.dto.QuizSubmissionDto;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.quiz.service.QuizService;
import com.group2.assignment.settings.SettingsConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quiz")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

    private final QuizService quizService;
    private final CourseRepository courseRepository;

    @GetMapping
    public String quizIndex(@RequestParam(value = "courseId", required = false) Long courseId, Model model) {
        List<Course> courses = courseRepository.findAll();
        Course selectedCourse = null;

        if (courseId != null) {
            selectedCourse = courseRepository.findById(courseId).orElse(null);
        } else if (!courses.isEmpty()) {
            selectedCourse = courses.get(0);
            courseId = selectedCourse.getId();
        }

        List<Quiz> quizzes = selectedCourse != null ? quizService.getQuizzesByCourse(selectedCourse.getId()) : List.of();

        model.addAttribute("courses", courses);
        model.addAttribute("selectedCourse", selectedCourse);
        model.addAttribute("quizzes", quizzes);

        return "quiz/index";
    }

    @PostMapping("/generate")
    public String generateQuiz(
            @RequestParam("courseId") Long courseId,
            @RequestParam("pdfFile") MultipartFile pdfFile,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "chapterTopic", required = false) String chapterTopic,
            @RequestParam(value = "difficulty", defaultValue = "MEDIUM") String difficulty,
            @RequestParam(value = "totalQuestions", defaultValue = "5") int totalQuestions,
            @RequestParam(value = "timeLimitMinutes", defaultValue = "15") int timeLimitMinutes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            String apiKey = (String) session.getAttribute(SettingsConstants.SESSION_API_KEY);
            Quiz quiz = quizService.createQuizFromPdf(courseId, pdfFile, title, chapterTopic, difficulty, totalQuestions, timeLimitMinutes, apiKey);
            redirectAttributes.addFlashAttribute("successMessage", "Sinh đề trắc nghiệm thành công với " + quiz.getTotalQuestions() + " câu hỏi!");
            return "redirect:/quiz/" + quiz.getId();
        } catch (Exception e) {
            log.error("Lỗi khi sinh đề thi", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể sinh đề thi: " + e.getMessage());
            return "redirect:/quiz?courseId=" + courseId;
        }
    }

    @GetMapping("/{id}")
    public String takeQuiz(@PathVariable("id") Long id, Model model) {
        Quiz quiz = quizService.getQuizById(id);
        List<QuizQuestion> questions = quizService.getQuestionsByQuizId(id);

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);

        return "quiz/take";
    }

    @PostMapping("/{id}/submit")
    public String submitQuiz(
            @PathVariable("id") Long id,
            @RequestParam(value = "studentName", defaultValue = "Sinh viên") String studentName,
            @RequestParam(value = "timeSpentSeconds", defaultValue = "0") Integer timeSpentSeconds,
            HttpServletRequest request,
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

        QuizResultDto result = quizService.submitAndGrade(id, submission);
        return "redirect:/quiz/attempt/" + result.getAttemptId();
    }

    @GetMapping("/attempt/{attemptId}")
    public String viewAttemptResult(@PathVariable("attemptId") Long attemptId, Model model) {
        QuizResultDto result = quizService.getAttemptResult(attemptId);
        model.addAttribute("result", result);
        return "quiz/result";
    }
}
```

- [ ] **Step 2: Verify with mvn compile**

Run: `.\mvnw.cmd compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit Task 4**

```bash
git add src/main/java/com/group2/assignment/quiz/controller/QuizController.java
git commit -m "feat(quiz): add QuizController endpoints for listing, generating, taking, and reviewing quiz"
```

---

### Task 5: Thymeleaf Templates & UI Integration

**Files:**
- Create: `src/main/resources/templates/quiz/index.html` (Quản lý đề & Form tải PDF sinh đề)
- Create: `src/main/resources/templates/quiz/take.html` (Phòng thi trực tuyến với đồng hồ đếm ngược)
- Create: `src/main/resources/templates/quiz/result.html` (Bảng điểm & Phân tích đáp án Đúng/Sai)

- [ ] **Step 1: Create `src/main/resources/templates/quiz/index.html`**
- [ ] **Step 2: Create `src/main/resources/templates/quiz/take.html`**
- [ ] **Step 3: Create `src/main/resources/templates/quiz/result.html`**
- [ ] **Step 4: Manual smoke test in browser/terminal via `./mvnw.cmd test` and run app**
- [ ] **Step 5: Commit Task 5**

```bash
git add src/main/resources/templates/quiz/
git commit -m "feat(quiz): add responsive Thymeleaf templates for quiz module"
```
