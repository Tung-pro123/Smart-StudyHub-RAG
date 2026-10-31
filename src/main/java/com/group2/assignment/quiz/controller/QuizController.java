package com.group2.assignment.quiz.controller;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.dto.QuizResultDto;
import com.group2.assignment.quiz.dto.QuizSubmissionDto;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.quiz.service.QuizService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
@Slf4j
public class QuizController {

    private final QuizService quizService;
    private final CourseRepository courseRepository;
    private final com.group2.assignment.rag.repository.DocumentRepository documentRepository;

    public QuizController(
            QuizService quizService,
            CourseRepository courseRepository,
            com.group2.assignment.rag.repository.DocumentRepository documentRepository) {
        this.quizService = quizService;
        this.courseRepository = courseRepository;
        this.documentRepository = documentRepository;
    }

    @GetMapping
    public String quizIndex(
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "difficulty", required = false, defaultValue = "ALL") String difficulty,
            HttpSession session,
            Model model) {
        List<Course> courses = courseRepository.findAll();
        Course selectedCourse = null;

        if (courseId != null) {
            selectedCourse = courseRepository.findById(courseId).orElse(null);
        } else if (!courses.isEmpty()) {
            selectedCourse = courses.get(0);
        }

        List<Quiz> quizzes = selectedCourse != null ? quizService.getQuizzesByCourse(selectedCourse.getId()) : List.of();
        if (difficulty != null && !difficulty.equalsIgnoreCase("ALL")) {
            quizzes = quizzes.stream()
                    .filter(q -> q.getDifficulty() != null && q.getDifficulty().equalsIgnoreCase(difficulty))
                    .toList();
        }

        List<com.group2.assignment.rag.entity.Document> documents = selectedCourse != null
                ? documentRepository.findAllByCourseIdOrderByUploadedAtDesc(selectedCourse.getId())
                : List.of();

        List<com.group2.assignment.quiz.entity.QuizAttempt> recentAttempts = selectedCourse != null
                ? quizService.getRecentAttemptsByCourse(selectedCourse.getId())
                : List.of();

        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        boolean isPro = currentUser != null && currentUser.isPro();

        model.addAttribute("courses", courses);
        model.addAttribute("selectedCourse", selectedCourse);
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("documents", documents);
        model.addAttribute("recentAttempts", recentAttempts);
        model.addAttribute("selectedDifficulty", difficulty != null ? difficulty.toUpperCase() : "ALL");
        model.addAttribute("isPro", isPro);
        model.addAttribute("isFreeTier", !isPro);

        return "quiz/index";
    }

    @PostMapping("/{id}/delete")
    public String deleteQuiz(
            @PathVariable("id") Long id,
            @RequestParam("courseId") Long courseId,
            RedirectAttributes redirectAttributes) {
        try {
            quizService.deleteQuiz(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đề thi thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi xóa đề thi ID " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa đề thi: " + e.getMessage());
        }
        return "redirect:/quiz?courseId=" + courseId;
    }

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

        // Lớp 1: Bắt lỗi parse kiểu dữ liệu số lượng câu hỏi
        int totalQuestions;
        try {
            totalQuestions = Integer.parseInt(totalQuestionsStr != null ? totalQuestionsStr.trim() : "5");
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số lượng câu hỏi không hợp lệ!");
            return "redirect:/quiz?courseId=" + courseId;
        }

        // Lớp 2: Kiểm tra biên giá trị (từ 1 đến 20 câu)
        if (totalQuestions <= 0 || totalQuestions > 20) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số lượng câu hỏi phải từ 1 đến 20 câu!");
            return "redirect:/quiz?courseId=" + courseId;
        }

        // Lớp 3: Kiểm tra quyền hạn gói cước (Gói Free tối đa 10 câu)
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

    @GetMapping("/{id}")
    public String takeQuiz(@PathVariable("id") Long id, HttpSession session, Model model) {
        Quiz quiz = quizService.getQuizById(id);
        List<QuizQuestion> questions = quizService.getQuestionsByQuizId(id);

        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        boolean isPro = currentUser != null && currentUser.isPro();

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("isPro", isPro);
        model.addAttribute("isFreeTier", !isPro);

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
    public String viewAttemptResult(@PathVariable("attemptId") Long attemptId, HttpSession session, Model model) {
        QuizResultDto result = quizService.getAttemptResult(attemptId);

        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        boolean isPro = currentUser != null && currentUser.isPro();

        model.addAttribute("result", result);
        model.addAttribute("isPro", isPro);
        model.addAttribute("isFreeTier", !isPro);
        return "quiz/result";
    }
}
