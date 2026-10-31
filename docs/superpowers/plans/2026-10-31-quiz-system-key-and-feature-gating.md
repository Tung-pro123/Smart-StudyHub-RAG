# Kế Hoạch Triển Khai: Centralized System Key & Feature Gating Cho Quiz

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Chuyển đổi module Quiz sang dùng Centralized System Key thông qua `SystemAiKeyService`, thiết lập Feature Gating bảo vệ tài nguyên (chặn biên dữ liệu & giới hạn 10 câu cho gói Free ở cả Backend + UI), bổ sung tính năng PRO (Xuất PDF & Flashcards Toast), chống Click Spam và đồng bộ hóa Modal thanh toán VietQR trên Landing Page.

**Architecture:** 
- Tái sử dụng `SystemAiKeyService` theo nguyên tắc DRY và Ponytail ladder (không sinh thêm `AiConfig` dư thừa).
- Áp dụng mô hình **3 lớp phòng thủ (Defense-in-Depth & Fail-Fast)** tại `QuizController.generateQuiz`: (1) Chặn lỗi ép kiểu `NumberFormatException`, (2) Validate miền giá trị biên `[1, 20]`, (3) Kiểm tra quyền gói cước `User.isPro()`.
- Chống Click Spam bằng **Idempotency** tại `/auth/upgrade-pro` kết hợp vô hiệu hóa nút submit ở tầng Client.
- Giao diện sử dụng DaisyUI và TailwindCSS, đồng bộ trải nghiệm người dùng từ Landing Page đến Dashboard.

**Tech Stack:** Java 17, Spring Boot 3, Spring Data JPA, Thymeleaf, TailwindCSS, DaisyUI, JUnit 5, MockMvc.

**Spec:** `docs/tasks/task-2026-10-30.md` (Mục 6: NHẬT - MODULE AI QUIZ GENERATOR & ĐỒNG BỘ LANDING PAGE).

## Global Constraints

- Không tạo thêm file cấu hình API Key mới; dùng duy nhất `SystemAiKeyService` và property `studyhub.ai.gemini-api-key`.
- Người dùng Free chỉ được tạo đề tối đa 10 câu hỏi; cố tình gửi giá trị âm, sai kiểu (`"abc"`), hoặc `> 10` câu ở request POST sẽ bị chặn và báo lỗi thân thiện qua Flash message.
- Endpoint `/auth/upgrade-pro` phải có cơ chế Idempotency: Nếu user đã là PRO, không thực hiện ghi đè lặp lại vào cơ sở dữ liệu.
- Tuân thủ quy ước commit Conventional Commits: `feat(quiz): ...`, `test(quiz): ...`.

## Review Focus

1. **Input rác hoặc sai kiểu**: Gửi `totalQuestions="abc"`, `"-5"`, `"0"`, hoặc `"99"` -> Hệ thống bắt lỗi nhẹ nhàng, không sập 500 hay WhiteLabel Error, chuyển hướng về kèm Flash error message.
2. **Bypass gói cước**: Người dùng Free cố tình gửi `totalQuestions=15` hoặc `20` -> Bị chặn ngay trước khi gọi AI, trả về thông điệp yêu cầu nâng cấp PRO.
3. **Click spam thanh toán**: Bấm nút "Đã Chuyển Khoản" nhiều lần -> UI khóa nút + hiển thị spinner sau click đầu tiên; Backend kiểm tra `isPro()` tránh duplicate updates.
4. **Trải nghiệm nút PRO**: 
   - User Free: Nút "Xuất PDF" và "Flashcards" ở trạng thái disabled kèm badge `PRO`.
   - User PRO: Nút "Xuất PDF" kích hoạt `window.print()`; nút "Flashcards" kích hoạt Toast DaisyUI với hiệu ứng mượt mà.
5. **Dọn dẹp code sạch sẽ**: Toàn bộ codebase không còn bất kỳ lời gọi nào truyền thừa tham số `apiKey` vào `QuizGeneratorService` hay `QuizService`.

---

### Task 1: Refactor Quiz Module sang Centralized System Key & Clean Up

**Files:**
- Modify: `src/main/java/com/group2/assignment/quiz/service/QuizGeneratorService.java`
- Modify: `src/main/java/com/group2/assignment/quiz/service/QuizService.java`
- Modify: `src/main/java/com/group2/assignment/quiz/controller/QuizController.java`
- Modify: `src/test/java/com/group2/assignment/quiz/service/QuizGeneratorServiceTest.java`

**Interfaces:**
- Consumes: `SystemAiKeyService.getApiKey(): String`
- Produces: 
  - `QuizGeneratorService.generateQuestions(String contextText, int count, String difficulty): List<QuizQuestion>`
  - `QuizService.createQuizFromPdf(Long courseId, MultipartFile file, String title, String chapterTopic, String difficulty, int totalQuestions, int timeLimitMinutes, Integer fromPage, Integer toPage): Quiz`
  - `QuizService.createQuizFromDocument(Long courseId, Long documentId, String title, String chapterTopic, String difficulty, int totalQuestions, int timeLimitMinutes, Integer fromPage, Integer toPage): Quiz`

- [ ] **Step 1: Quét toàn dự án kiểm tra mọi nơi đang gọi các hàm cũ có tham số apiKey**

Chạy lệnh kiểm tra trong PowerShell:
```powershell
Get-ChildItem -Recurse -Filter *.java | Select-String "generateQuestions|createQuizFrom"
```
Mục tiêu: Ghi nhận chính xác 3 file cần sửa (`QuizController.java`, `QuizService.java`, `QuizGeneratorService.java`) và file test `QuizGeneratorServiceTest.java`.

- [ ] **Step 2: Cập nhật QuizGeneratorServiceTest với chữ ký phương thức mới**

```java
// File: src/test/java/com/group2/assignment/quiz/service/QuizGeneratorServiceTest.java
package com.group2.assignment.quiz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.settings.service.SystemAiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizGeneratorServiceTest {

    @Mock
    private SystemAiKeyService systemAiKeyService;

    private QuizGeneratorService quizGeneratorService;

    @BeforeEach
    void setUp() {
        quizGeneratorService = new QuizGeneratorService(new ObjectMapper(), systemAiKeyService);
    }

    @Test
    @DisplayName("Fallback sinh câu hỏi mẫu khi System Key rỗng hoặc không cấu hình")
    void testGenerateQuestionsFallbackWhenNoKey() {
        when(systemAiKeyService.getApiKey()).thenReturn("");

        List<QuizQuestion> questions = quizGeneratorService.generateQuestions("Nội dung Spring Boot IoC Container", 5, "MEDIUM");

        assertThat(questions).isNotNull();
        assertThat(questions).hasSize(5);
        assertThat(questions.get(0).getQuestionText()).isNotEmpty();
    }
}
```

- [ ] **Step 3: Refactor QuizGeneratorService để inject SystemAiKeyService**

Trong `src/main/java/com/group2/assignment/quiz/service/QuizGeneratorService.java`:
- Thêm dependency `private final SystemAiKeyService systemAiKeyService;`
- Cập nhật constructor nhận `(ObjectMapper objectMapper, SystemAiKeyService systemAiKeyService)`.
- Cập nhật hàm `generateQuestions(String contextText, int count, String difficulty)`:
```java
    public List<QuizQuestion> generateQuestions(String contextText, int count, String difficulty) {
        String apiKey = systemAiKeyService.getApiKey();
        if (apiKey != null && !apiKey.trim().isBlank()) {
            try {
                return callGeminiAi(contextText, count, difficulty, apiKey.trim());
            } catch (Exception e) {
                log.warn("Gọi Gemini API thất bại ({}), tự động kích hoạt Smart Semantic Fallback", e.getMessage());
            }
        }
        return generateMockQuestions(contextText, count, difficulty);
    }
```

- [ ] **Step 4: Refactor QuizService và QuizController loại bỏ tham số apiKey**

Trong `src/main/java/com/group2/assignment/quiz/service/QuizService.java`:
- Xóa bỏ tham số `String apiKey` ở `createQuizFromPdf` và `createQuizFromDocument`.
- Trong thân hàm, gọi `quizGeneratorService.generateQuestions(text, totalQuestions, difficulty);`.

Trong `src/main/java/com/group2/assignment/quiz/controller/QuizController.java`:
- Xóa bỏ biến `String apiKey = systemAiKeyService.getApiKey();` trong `generateQuiz(...)`.
- Gọi hàm `quizService.createQuizFromDocument(...)` và `quizService.createQuizFromPdf(...)` với danh sách tham số đã giản lược.

- [ ] **Step 5: Chạy lại test và verify kết quả**

Run: `mvn test -Dtest=QuizGeneratorServiceTest`
Expected: `BUILD SUCCESS` (Tests pass).

- [ ] **Step 6: Commit thay đổi Task 1**

```bash
git add src/main/java/com/group2/assignment/quiz/ src/test/java/com/group2/assignment/quiz/
git commit -m "feat(quiz): migrate quiz generator to centralized system key"
```

---

### Task 2: Kiểm soát hạn ngạch gói cước (Feature Gating) & Xử lý biên dữ liệu

**Files:**
- Modify: `src/main/java/com/group2/assignment/quiz/controller/QuizController.java`
- Modify: `src/main/resources/templates/quiz/index.html`
- Modify: `src/main/resources/templates/quiz/result.html`
- Create: `src/test/java/com/group2/assignment/quiz/QuizFeatureGatingTest.java`

**Interfaces:**
- Consumes:
  - `User.isPro(): boolean`
  - `session.getAttribute(AuthController.SESSION_USER): User`
- Produces:
  - 3 lớp Validation tại `POST /quiz/generate`:
    1. Parse an toàn số lượng câu hỏi (bắt `NumberFormatException`).
    2. Chặn miền giá trị không hợp lệ (`<= 0` hoặc `> 20`).
    3. Chặn người dùng Free yêu cầu `> 10` câu hỏi.
  - Model attributes: `isPro`, `isFreeTier` truyền vào view `quiz/index` và `quiz/result`.

- [ ] **Step 1: Viết Integration Test kiểm tra toàn diện các trường hợp biên**

Tạo file `src/test/java/com/group2/assignment/quiz/QuizFeatureGatingTest.java`:

```java
package com.group2.assignment.quiz;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuizFeatureGatingTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CourseRepository courseRepository;

    @MockBean
    private QuizService quizService;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        testCourse = courseRepository.findByCodeIgnoreCase("HSF302")
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .code("HSF302")
                        .name("Spring Framework")
                        .build()));
    }

    @Test
    @DisplayName("Gửi giá trị số lượng câu hỏi sai định dạng chuỗi (abc) -> Bắt lỗi nhẹ nhàng và báo lỗi Flash")
    void testInvalidNumberFormat() throws Exception {
        User user = User.builder().id(100L).username("student1").subscriptionTier("FREE").build();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, user);

        MockMultipartFile file = new MockMultipartFile("pdfFile", "test.pdf", "application/pdf", "dummy content".getBytes());

        mvc.perform(multipart("/quiz/generate")
                        .file(file)
                        .param("courseId", testCourse.getId().toString())
                        .param("totalQuestions", "abc")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz?courseId=" + testCourse.getId()))
                .andExpect(flash().attribute("errorMessage", "Số lượng câu hỏi không hợp lệ!"));

        verify(quizService, never()).createQuizFromPdf(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Gửi số lượng câu hỏi <= 0 hoặc > 20 -> Báo lỗi miền giá trị")
    void testOutOfBoundsQuestionCount() throws Exception {
        User user = User.builder().id(100L).username("student1").subscriptionTier("FREE").build();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, user);

        MockMultipartFile file = new MockMultipartFile("pdfFile", "test.pdf", "application/pdf", "dummy content".getBytes());

        mvc.perform(multipart("/quiz/generate")
                        .file(file)
                        .param("courseId", testCourse.getId().toString())
                        .param("totalQuestions", "-5")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Số lượng câu hỏi phải từ 1 đến 20 câu!"));
    }

    @Test
    @DisplayName("Gói Free yêu cầu 15 câu hỏi -> Chặn lại và yêu cầu nâng cấp PRO")
    void testFreeUserExceedsLimit() throws Exception {
        User freeUser = User.builder().id(101L).username("free_student").role("ROLE_STUDENT").subscriptionTier("FREE").build();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, freeUser);

        MockMultipartFile file = new MockMultipartFile("pdfFile", "test.pdf", "application/pdf", "dummy content".getBytes());

        mvc.perform(multipart("/quiz/generate")
                        .file(file)
                        .param("courseId", testCourse.getId().toString())
                        .param("totalQuestions", "15")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));

        verify(quizService, never()).createQuizFromPdf(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Gói Free yêu cầu 10 câu hỏi -> Được phép tạo đề")
    void testFreeUserWithinLimit() throws Exception {
        User freeUser = User.builder().id(102L).username("free_student_ok").role("ROLE_STUDENT").subscriptionTier("FREE").build();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, freeUser);

        MockMultipartFile file = new MockMultipartFile("pdfFile", "test.pdf", "application/pdf", "dummy content".getBytes());

        mvc.perform(multipart("/quiz/generate")
                        .file(file)
                        .param("courseId", testCourse.getId().toString())
                        .param("totalQuestions", "10")
                        .session(session))
                .andExpect(status().is3xxRedirection());
    }
}
```

- [ ] **Step 2: Chạy test để xác nhận fail trước khi cài đặt**

Run: `mvn test -Dtest=QuizFeatureGatingTest`
Expected: FAIL.

- [ ] **Step 3: Triển khai 3 lớp phòng thủ trong QuizController.generateQuiz**

Cập nhật `QuizController.java`:
```java
    @PostMapping("/generate")
    public String generateQuiz(
            @RequestParam("courseId") Long courseId,
            @RequestParam(value = "documentId", required = false) Long documentId,
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "chapterTopic", required = false) String chapterTopic,
            @RequestParam(value = "difficulty", defaultValue = "MEDIUM") String difficulty,
            @RequestParam(value = "totalQuestions", defaultValue = "5") String totalQuestionsStr,
            @RequestParam(value = "timeLimitMinutes", defaultValue = "15") int timeLimitMinutes,
            @RequestParam(value = "fromPage", required = false) Integer fromPage,
            @RequestParam(value = "toPage", required = false) Integer toPage,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Lớp 1: Bắt lỗi parse kiểu dữ liệu
        int totalQuestions;
        try {
            totalQuestions = Integer.parseInt(totalQuestionsStr != null ? totalQuestionsStr.trim() : "5");
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số lượng câu hỏi không hợp lệ!");
            return "redirect:/quiz?courseId=" + courseId;
        }

        // Lớp 2: Kiểm tra biên giá trị
        if (totalQuestions <= 0 || totalQuestions > 20) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số lượng câu hỏi phải từ 1 đến 20 câu!");
            return "redirect:/quiz?courseId=" + courseId;
        }

        // Lớp 3: Kiểm tra quyền hạn gói cước
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        boolean isPro = currentUser != null && currentUser.isPro();
        if (!isPro && totalQuestions > 10) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Gói Free chỉ hỗ trợ tối đa 10 câu hỏi/đề thi. Vui lòng nâng cấp gói PRO để sinh đề 15 - 20 câu!");
            return "redirect:/quiz?courseId=" + courseId;
        }

        // Kiểm tra tài liệu đầu vào
        if ((documentId == null || documentId <= 0) && (pdfFile == null || pdfFile.isEmpty())) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Môn học này chưa có tài liệu hoặc bạn chưa chọn tài liệu giáo trình! Vui lòng tải tài liệu lên Kho tài liệu của môn học trước.");
            return "redirect:/quiz?courseId=" + courseId;
        }

        try {
            Quiz quiz;
            if (documentId != null && documentId > 0) {
                quiz = quizService.createQuizFromDocument(courseId, documentId, title, chapterTopic, difficulty, totalQuestions, timeLimitMinutes, fromPage, toPage);
            } else {
                quiz = quizService.createQuizFromPdf(courseId, pdfFile, title, chapterTopic, difficulty, totalQuestions, timeLimitMinutes, fromPage, toPage);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Sinh đề trắc nghiệm thành công với " + quiz.getTotalQuestions() + " câu hỏi!");
            return "redirect:/quiz/" + quiz.getId();
        } catch (Exception e) {
            log.error("Lỗi khi sinh đề thi", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể sinh đề thi: " + e.getMessage());
            return "redirect:/quiz?courseId=" + courseId;
        }
    }
```
Và trong `quizIndex`, truyền `isPro` và `isFreeTier`:
```java
User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
boolean isPro = currentUser != null && currentUser.isPro();
model.addAttribute("isPro", isPro);
model.addAttribute("isFreeTier", !isPro);
```

- [ ] **Step 4: Cập nhật giao diện Quiz Index & Card đề thi**

Trong `src/main/resources/templates/quiz/index.html`:
1. Trong Modal tạo đề:
   - Các option `15` và `20` được đặt `th:disabled="${isFreeTier}"`.
   - Hiển thị thông báo nhỏ nếu là Free user: `* Gói Free giới hạn tối đa 10 câu hỏi/lần tạo.`
2. Trên từng thẻ Card đề thi:
   - Nút "Xuất PDF":
     - Nếu `isPro`: `<button onclick="window.print()" class="btn btn-xs btn-outline btn-primary rounded-lg font-bold gap-1"><i class="fa-solid fa-file-pdf"></i> Xuất PDF</button>`
     - Nếu `isFreeTier`: `<div class="indicator"><span class="indicator-item badge badge-secondary badge-xs">PRO</span><button disabled class="btn btn-xs btn-disabled opacity-50 rounded-lg gap-1"><i class="fa-solid fa-file-pdf"></i> Xuất PDF</button></div>`
   - Nút "Flashcards":
     - Nếu `isPro`: `<button onclick="showToast('Tính năng Flashcards Spaced Repetition đang được hoàn thiện!')" class="btn btn-xs btn-outline btn-warning rounded-lg font-bold gap-1"><i class="fa-solid fa-layer-group"></i> Flashcards</button>`
     - Nếu `isFreeTier`: `<div class="indicator"><span class="indicator-item badge badge-secondary badge-xs">PRO</span><button disabled class="btn btn-xs btn-disabled opacity-50 rounded-lg gap-1"><i class="fa-solid fa-layer-group"></i> Flashcards</button></div>`
3. Thêm Toast UI và Script chuẩn ở cuối `quiz/index.html` và `quiz/result.html`:
```html
<div id="app-toast" class="toast toast-end toast-bottom z-50 hidden">
    <div class="alert alert-info shadow-lg text-white font-semibold text-xs py-2 px-4 rounded-2xl">
        <span id="app-toast-msg">Thông báo</span>
    </div>
</div>
<script>
function showToast(msg) {
    const toast = document.getElementById('app-toast');
    const toastMsg = document.getElementById('app-toast-msg');
    if (toast && toastMsg) {
        toastMsg.innerText = msg;
        toast.classList.remove('hidden');
        setTimeout(() => toast.classList.add('hidden'), 3500);
    }
}
</script>
```

- [ ] **Step 5: Cập nhật giao diện trang Kết quả thi (quiz/result.html)**

Trong `src/main/resources/templates/quiz/result.html`:
Tương tự, bổ sung cụm nút "Xuất kết quả PDF" và "Ôn tập Flashcards" với trạng thái check `isPro`.

- [ ] **Step 6: Chạy lại test và verify kết quả**

Run: `mvn test -Dtest=QuizFeatureGatingTest`
Expected: `BUILD SUCCESS` (4/4 tests pass).

- [ ] **Step 7: Commit thay đổi Task 2**

```bash
git add src/main/java/com/group2/assignment/quiz/ src/main/resources/templates/quiz/ src/test/java/com/group2/assignment/quiz/
git commit -m "feat(quiz): enforce 3-layer validation for question limits and add pro features"
```

---

### Task 3: Chống Click Spam, Đồng bộ Landing Page & Modal VietQR

**Files:**
- Modify: `src/main/java/com/group2/assignment/auth/controller/AuthController.java`
- Modify: `src/main/resources/templates/landing.html`
- Modify: `src/test/java/com/group2/assignment/auth/AuthControllerTest.java` (hoặc tạo test case nâng cấp)

**Interfaces:**
- Consumes: `AuthController.upgradePro`
- Produces: 
  - Idempotency check: Trả về ngay nếu user đã là PRO.
  - UI Modal VietQR có cơ chế disable nút & hiển thị spinner ngay sau lần bấm đầu tiên.
  - Đồng bộ Pricing bullet points của Landing Page.

- [ ] **Step 1: Áp dụng Idempotency trong AuthController.upgradePro**

Trong `src/main/java/com/group2/assignment/auth/controller/AuthController.java`:
```java
    @PostMapping("/auth/upgrade-pro")
    public String upgradePro(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(SESSION_USER);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để nâng cấp gói tài khoản!");
            return "redirect:/login";
        }

        // CHỐNG CLICK SPAM (IDEMPOTENCY): Nếu đã là PRO, không cập nhật lặp lại
        if (currentUser.isPro()) {
            redirectAttributes.addFlashAttribute("successMessage", "Tài khoản của bạn hiện đã là gói PRO!");
            return "redirect:/courses";
        }

        User updated = authService.upgradeSubscriptionTier(currentUser.getUsername(), "PRO");
        session.setAttribute(SESSION_USER, updated);
        redirectAttributes.addFlashAttribute("successMessage",
                "🎉 Chúc mừng! Bạn đã nâng cấp thành công lên Gói PRO (Không giới hạn môn học, AI Study Planner & RAG không giới hạn)!");
        return "redirect:/courses";
    }
```

- [ ] **Step 2: Thêm Modal VietQR và chống double submit ở giao diện landing.html**

Trong `src/main/resources/templates/landing.html`:
1. Cập nhật nút CTA trong Bảng giá:
```html
<button type="button" onclick="document.getElementById('modal-upgrade-pro').showModal()" 
        class="w-full btn-gradient py-3.5 rounded-xl font-bold text-sm text-white shadow-lg shadow-indigo-500/30 text-center block cursor-pointer">
    <i class="fa-solid fa-bolt mr-1"></i> Nâng Cấp Gói Pro
</button>
```
2. Thêm Modal VietQR có form submit kèm script chống click spam:
```html
<dialog id="modal-upgrade-pro" class="modal modal-bottom sm:modal-middle">
    <div class="modal-box bg-slate-900 border border-slate-700 text-white rounded-3xl p-6 sm:p-8 max-w-md">
        <div class="flex justify-between items-center mb-4">
            <div class="flex items-center gap-2">
                <span class="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-500 to-pink-500 flex items-center justify-center text-white text-sm">
                    <i class="fa-solid fa-crown"></i>
                </span>
                <h3 class="font-extrabold text-lg text-white">Nâng Cấp StudyHub PRO</h3>
            </div>
            <form method="dialog">
                <button class="btn btn-sm btn-circle btn-ghost text-slate-400 hover:text-white">✕</button>
            </form>
        </div>

        <p class="text-xs text-slate-300 text-center mb-4 leading-relaxed">
            Quét mã VietQR chuyển khoản nhanh để mở khóa AI Study Planner, tạo đề 20 câu &amp; xuất PDF!
        </p>

        <div class="bg-white p-4 rounded-2xl flex flex-col items-center shadow-inner mb-4">
            <img src="https://api.vietqr.io/image/970422-0123456789-qH8t28g.jpg?accountName=STUDYHUB%20AI&amount=49000&addInfo=PRO%20STUDYHUB" 
                 alt="VietQR Code" class="w-48 h-48 object-contain rounded-lg" />
            <span class="text-[11px] text-slate-600 font-bold mt-2">Hỗ trợ 40+ ứng dụng Ngân hàng &amp; Ví MoMo</span>
        </div>

        <div class="bg-slate-800/80 rounded-2xl p-4 text-xs space-y-2 border border-slate-700">
            <div class="flex justify-between">
                <span class="text-slate-400">Ngân hàng:</span>
                <span class="font-bold text-white">MB Bank (Quân Đội)</span>
            </div>
            <div class="flex justify-between">
                <span class="text-slate-400">Số tài khoản:</span>
                <span class="font-mono font-bold text-indigo-400">0123456789</span>
            </div>
            <div class="flex justify-between">
                <span class="text-slate-400">Chủ tài khoản:</span>
                <span class="font-bold text-white">STUDYHUB AI TEAM</span>
            </div>
            <div class="flex justify-between">
                <span class="text-slate-400">Số tiền:</span>
                <span class="font-bold text-emerald-400">49.000 VNĐ / tháng</span>
            </div>
            <div class="flex justify-between border-t border-slate-700/60 pt-2">
                <span class="text-slate-400">Cú pháp CK:</span>
                <span class="font-mono font-bold text-amber-300">
                    <span th:if="${session.currentUser != null}" th:text="${session.currentUser.username + ' PRO'}">STUDENT_PRO</span>
                    <span th:unless="${session.currentUser != null}">[Tên_Đăng_Nhập] PRO</span>
                </span>
            </div>
        </div>

        <div class="modal-action flex items-center justify-between mt-5">
            <form method="dialog">
                <button class="btn btn-sm btn-ghost text-slate-400">Đóng</button>
            </form>
            <div th:if="${session.currentUser != null}">
                <form th:action="@{/auth/upgrade-pro}" method="post" 
                      onsubmit="const btn = this.querySelector('button[type=submit]'); btn.disabled = true; btn.innerHTML = '<span class=\'loading loading-spinner loading-xs\'></span> Đang xử lý...';">
                    <button type="submit" class="btn btn-sm bg-gradient-to-r from-emerald-500 to-teal-500 text-white font-bold border-none rounded-xl gap-1.5 shadow-md">
                        <i class="fa-solid fa-check"></i> Đã Chuyển Khoản (Xác Nhận)
                    </button>
                </form>
            </div>
            <div th:unless="${session.currentUser != null}">
                <a th:href="@{/register}" class="btn btn-sm bg-gradient-to-r from-indigo-500 to-pink-500 text-white font-bold border-none rounded-xl gap-1.5 shadow-md">
                    <i class="fa-solid fa-user-plus"></i> Đăng Ký Để Mua PRO
                </a>
            </div>
        </div>
    </div>
</dialog>
```

- [ ] **Step 3: Đồng bộ bullet points của Pricing section trong landing.html**

Kiểm tra và cập nhật các dòng miêu tả quyền lợi:
- Gói Free: "Tối đa 10 câu hỏi trắc nghiệm/đề", "Không có Xuất PDF & Flashcards".
- Gói Pro: "Sinh đề 15 - 20 câu theo chương", "Xuất đề thi PDF & Flashcards Spaced Repetition".

- [ ] **Step 4: Commit thay đổi Task 3**

```bash
git add src/main/java/com/group2/assignment/auth/ src/main/resources/templates/landing.html
git commit -m "feat(auth): add idempotency to upgrade-pro and implement vietqr anti-spam modal"
```

---

### Task 4: Kiểm thử toàn diện & Verification (Definition of Done)

**Files:**
- Toàn bộ codebase liên quan đến Quiz, Auth và Settings

- [ ] **Step 1: Chạy toàn bộ Test Suite**

Run: `mvn clean test`
Expected: `BUILD SUCCESS`, 0 failures, 0 errors.

- [ ] **Step 2: Chạy kiểm thử thủ công qua UI (End-to-End Walkthrough)**

1. Khởi động ứng dụng bằng `run.bat` hoặc lệnh:
   ```powershell
   ./mvnw.cmd spring-boot:run
   ```
2. Đăng nhập tài khoản Free (`student`):
   - Mở `/quiz`, mở modal tạo đề. Thấy tùy chọn 15 và 20 câu bị disabled kèm nhãn `(PRO)`.
   - Tạo đề 5 hoặc 10 câu -> Sinh đề thành công.
   - Thấy nút "Xuất PDF" và "Flashcards" có badge `PRO` và bị disabled.
3. Thử test biên với Postman / Curl:
   - Gửi POST `/quiz/generate` với `totalQuestions="abc"` -> Redirect về kèm Flash `Số lượng câu hỏi không hợp lệ!`.
   - Gửi `totalQuestions=15` với session Free -> Redirect về kèm Flash `Gói Free chỉ hỗ trợ tối đa 10 câu hỏi/đề thi...`.
4. Vào Landing Page:
   - Bấm "Nâng Cấp Gói Pro" -> Modal VietQR bật lên sắc nét, cú pháp ghi rõ tên user hiện tại.
   - Bấm xác nhận nâng cấp -> Nút chuyển thành spinner, tài khoản được nâng lên PRO. Bấm lần nữa không gây lỗi.
5. Kiểm tra lại tài khoản PRO:
   - Vào `/quiz`, tạo đề 15 hoặc 20 câu -> Tạo thành công.
   - Bấm nút "Xuất PDF" -> Kích hoạt hộp thoại in `window.print()`.
   - Bấm nút "Flashcards" -> Toast thông báo hiển thị mượt mà trong 3.5 giây rồi ẩn.

- [ ] **Step 3: Commit hoàn tất toàn bộ Milestone**

```bash
git commit --allow-empty -m "chore(release): complete quiz centralized key, feature gating, and landing page sync"
```
