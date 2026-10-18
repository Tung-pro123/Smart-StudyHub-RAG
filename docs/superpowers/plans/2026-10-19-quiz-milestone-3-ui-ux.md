# Quiz Milestone 3: Web Controller & DaisyUI UI/UX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Hoàn thiện trải nghiệm người dùng (UI/UX) cho toàn bộ module Quiz theo chuẩn thi cử trực tuyến: Phòng thi Split-View chuyên nghiệp với ma trận câu hỏi & thanh tiến độ tương tác, Trang kết quả với bộ lọc Đúng/Sai tức thì và biểu đồ điểm số Radial Progress, cùng Trung tâm Quản lý đề thi với bộ lọc độ khó, bảng lịch sử thi gần đây và tính năng xóa đề thi an toàn.

**Architecture:** Bổ sung phương thức xóa đề thi `deleteQuiz(Long id)` và truy xuất lịch sử làm bài `getRecentAttempts(Long courseId)` trong `QuizService` & `QuizController`. Thiết kế lại 3 giao diện Thymeleaf (`index.html`, `take.html`, `result.html`) sử dụng hoàn toàn Tailwind CSS, DaisyUI v4 và FontAwesome có sẵn trong project.

**Tech Stack:** Java 17, Spring Boot 3.3.4, Spring Data JPA, Thymeleaf, Tailwind CSS, DaisyUI v4, FontAwesome 6.5.1, Vanilla JavaScript.

**Spec:** [README.md](file:///c:/Dev/Smart-StudyHub-RAG/README.md), [head.html](file:///c:/Dev/Smart-StudyHub-RAG/src/main/resources/templates/fragments/head.html), [QuizAttemptRepository.java](file:///c:/Dev/Smart-StudyHub-RAG/src/main/java/com/group2/assignment/quiz/repository/QuizAttemptRepository.java).

---

## Global Constraints

- **Dependency Policy (Ponytail Principle):** Tuyệt đối không thêm thư viện frontend ngoài (không dùng npm, react hay vue). Sử dụng 100% Vanilla JS nhẹ nhàng và DaisyUI v4 + Tailwind CDN đã tích hợp sẵn trong `head.html`.
- **Responsive First:** Mọi giao diện phải hiển thị hoàn hảo trên cả Mobile (cột đơn) và Desktop (Split-View 2 cột).
- **Zero Glitch Navigation:** Chuyển đổi giữa các câu hỏi, lọc câu đúng/sai phải chạy mượt mà ngay trên Client (DOM manipulation) không gây reload trang không cần thiết.

## Review Focus

1. **Unanswered Question Prompt:** Thí sinh bấm nộp bài khi vẫn còn câu chưa chọn; hệ thống phải hiện Modal cảnh báo số câu chưa làm trước khi submit.
2. **Matrix Question Sync:** Khi thí sinh click chọn radio A/B/C/D ở bất kỳ câu nào, nút số câu tương ứng (1, 2, 3...) ở sidebar bên phải phải lập tức đổi màu (từ xám sang tím/xanh) để thí sinh biết câu đó đã hoàn thành.
3. **Empty Filter State in Result View:** Khi thí sinh làm đúng 100% (0 câu sai) và bấm lọc "Chỉ xem câu SAI", giao diện phải hiển thị empty state chúc mừng rực rỡ thay vì trang trắng.
4. **Delete Cascade Safety:** Khi xóa đề thi, toàn bộ câu hỏi (`QuizQuestion`) và lịch sử bài thi (`QuizAttempt`) phải được xóa sạch sẽ theo quan hệ `ON DELETE CASCADE` của CSDL mà không gây lỗi khóa ngoại.
5. **Timer Expiration Auto-Submit:** Khi đồng hồ đếm ngược về 00:00, form phải tự động nộp bài ngay cả khi thí sinh không bấm nút.

---

## Phase Breakdown

- **Task 1: Backend Service & Endpoints for Deletion & Recent Attempts**
  - Thêm phương thức xóa đề thi và lấy lịch sử thi của môn trong `QuizService.java`.
  - Bổ sung endpoint `POST /quiz/{id}/delete` và truyền `recentAttempts` vào model trong `QuizController.java`.
  - Unit test kiểm chứng xóa đề thi và truy vấn lịch sử.
- **Task 2: Exam Room (`take.html`) Split-View & Question Matrix Palette**
  - Thiết kế bố cục 2 cột (Left: Câu hỏi dạng card lớn, Right: Sticky Sidebar ma trận câu hỏi 1, 2, 3...).
  - JS đồng bộ trạng thái tick radio $\leftrightarrow$ màu nút số câu & thanh tiến độ % hoàn thành.
  - Modal cảnh báo nếu còn câu chưa làm trước khi nộp.
- **Task 3: Interactive Result Review (`result.html`) with Quick Filters**
  - DaisyUI Radial Score Progress với màu sắc động (Xanh/Vàng/Đỏ).
  - Client-side Filter Tabs ("Tất cả", "Chỉ câu SAI ❌", "Chỉ câu ĐÚNG ✅").
  - Callout box giải thích chi tiết với icon bóng đèn.
- **Task 4: Quiz Hub (`index.html`) with Difficulty Filters & Recent Attempts Widget**
  - Bộ lọc độ khó (Tất cả, EASY, MEDIUM, HARD).
  - Bảng lịch sử bài làm gần đây (Ngày thi, Điểm số, Nút xem lại kết quả).
  - Modal xác nhận xóa đề thi an toàn.

---

### Task 1: Backend Service & Endpoints for Deletion & Recent Attempts

**Files:**
- Modify: `src/main/java/com/group2/assignment/quiz/repository/QuizAttemptRepository.java`
- Modify: `src/main/java/com/group2/assignment/quiz/service/QuizService.java`
- Modify: `src/main/java/com/group2/assignment/quiz/controller/QuizController.java`
- Test: `src/test/java/com/group2/assignment/quiz/service/QuizServiceTest.java`

**Interfaces:**
- Consumes: `QuizRepository`, `QuizAttemptRepository`
- Produces:
  - `void deleteQuiz(Long quizId)`
  - `List<QuizAttempt> getRecentAttemptsByCourse(Long courseId)`
  - Endpoint `POST /quiz/{id}/delete`

- [ ] **Step 1: Add query method in `QuizAttemptRepository.java`**

```java
// Bổ sung method lấy danh sách attempt theo courseId:
@Query("SELECT a FROM QuizAttempt a JOIN a.quiz q WHERE q.course.id = :courseId ORDER BY a.completedAt DESC")
List<QuizAttempt> findRecentAttemptsByCourseId(@Param("courseId") Long courseId);
```

- [ ] **Step 2: Add `deleteQuiz` and `getRecentAttemptsByCourse` in `QuizService.java`**

```java
    @Transactional
    public void deleteQuiz(Long id) {
        quizQuestionRepository.deleteAllByQuizId(id);
        quizAttemptRepository.deleteAllByQuizId(id);
        quizRepository.deleteById(id);
    }

    public List<QuizAttempt> getRecentAttemptsByCourse(Long courseId) {
        return quizAttemptRepository.findRecentAttemptsByCourseId(courseId);
    }
```

- [ ] **Step 3: Add `deleteQuiz` endpoint in `QuizController.java`**

```java
    @PostMapping("/{id}/delete")
    public String deleteQuiz(@PathVariable("id") Long id, @RequestParam("courseId") Long courseId, RedirectAttributes redirectAttributes) {
        try {
            quizService.deleteQuiz(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đề thi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa đề thi: " + e.getMessage());
        }
        return "redirect:/quiz?courseId=" + courseId;
    }
```

- [ ] **Step 4: Verify with `mvn compile`**

Run: `.\mvnw.cmd compile`
Expected: BUILD SUCCESS

---

### Task 2: Redesign Exam Room (`take.html`) Split-View & Question Matrix

**Files:**
- Modify: `src/main/resources/templates/quiz/take.html`

- [ ] **Step 1: Implement 2-Column Split-View layout (Questions + Sticky Matrix Sidebar)**
- [ ] **Step 2: Add JS Event Listeners on Radio inputs to update Matrix Numbers (1, 2, 3...) & Progress Bar**
- [ ] **Step 3: Add Modal confirmation checking for unanswered questions before submitting**

---

### Task 3: Interactive Result Review (`result.html`) with Filters

**Files:**
- Modify: `src/main/resources/templates/quiz/result.html`

- [ ] **Step 1: Add DaisyUI Radial Progress with dynamic CSS variables**
- [ ] **Step 2: Add Client-side Filter Tabs ("Tất cả", "Chỉ câu SAI", "Chỉ câu ĐÚNG")**
- [ ] **Step 3: Add Empty State for 100% score ("Không có câu sai nào!")**

---

### Task 4: Quiz Hub (`index.html`) with History Widget & Delete Modal

**Files:**
- Modify: `src/main/resources/templates/quiz/index.html`

- [ ] **Step 1: Add Difficulty Filter Pills/Tabs (ALL, EASY, MEDIUM, HARD)**
- [ ] **Step 2: Add Recent Attempts History Table widget with direct link to `/quiz/attempt/{id}`**
- [ ] **Step 3: Add Delete Quiz button with modal confirmation dialog**
- [ ] **Step 4: Run full verification `./mvnw.cmd test`**

---
