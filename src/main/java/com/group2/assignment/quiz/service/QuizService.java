package com.group2.assignment.quiz.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseRepository courseRepository;
    private final QuizGeneratorService quizGeneratorService;
    private final PdfTextExtractor pdfTextExtractor;
    private final ObjectMapper objectMapper;
    private final com.group2.assignment.rag.repository.DocumentRepository documentRepository;

    public QuizService(
            QuizRepository quizRepository,
            QuizQuestionRepository quizQuestionRepository,
            QuizAttemptRepository quizAttemptRepository,
            CourseRepository courseRepository,
            QuizGeneratorService quizGeneratorService,
            PdfTextExtractor pdfTextExtractor,
            ObjectMapper objectMapper,
            com.group2.assignment.rag.repository.DocumentRepository documentRepository) {
        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.courseRepository = courseRepository;
        this.quizGeneratorService = quizGeneratorService;
        this.pdfTextExtractor = pdfTextExtractor;
        this.objectMapper = objectMapper;
        this.documentRepository = documentRepository;
    }

    @jakarta.annotation.PostConstruct
    public void initSampleQuiz() {
        try {
            if (quizRepository.count() == 0) {
                List<Course> courses = courseRepository.findAll();
                if (courses.isEmpty()) return;
                Course hsf = courses.get(0);

            Quiz sampleQuiz = Quiz.builder()
                    .course(hsf)
                    .title("Đề Ôn Tập: Kiến Trúc Spring Boot 3 & JPA Nền Tảng")
                    .chapterTopic("Chương 1: IoC Container & Dependency Injection")
                    .difficulty("MEDIUM")
                    .totalQuestions(5)
                    .timeLimitMinutes(15)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Quiz saved = quizRepository.save(sampleQuiz);

            List<QuizQuestion> questions = List.of(
                    QuizQuestion.builder()
                            .quiz(saved)
                            .sortOrder(1)
                            .questionText("Trong Spring Framework, Annotation nào được sử dụng để đánh dấu một Class là Spring Bean thuộc tầng xử lý nghiệp vụ (Business Logic)?")
                            .optionA("@Repository")
                            .optionB("@Service")
                            .optionC("@Controller")
                            .optionD("@Configuration")
                            .correctAnswer("B")
                            .explanation("@Service là annotation chuyên biệt trong Spring stereotyping để chỉ định lớp đảm nhận nghiệp vụ (Business Service).")
                            .build(),
                    QuizQuestion.builder()
                            .quiz(saved)
                            .sortOrder(2)
                            .questionText("Annotation nào của Jakarta Persistence được dùng để chỉ định khóa chính (Primary Key) của một Entity?")
                            .optionA("@Id")
                            .optionB("@Key")
                            .optionC("@GeneratedValue")
                            .optionD("@Column")
                            .correctAnswer("A")
                            .explanation("@Id đánh dấu trường dữ liệu làm khóa chính cho thực thể trong chuẩn JPA.")
                            .build(),
                    QuizQuestion.builder()
                            .quiz(saved)
                            .sortOrder(3)
                            .questionText("Lợi ích chính của việc sử dụng Dependency Injection (DI) trong Spring Boot là gì?")
                            .optionA("Giúp ứng dụng chạy nhanh hơn 100 lần")
                            .optionB("Giảm sự phụ thuộc chặt chẽ (Loose Coupling) giữa các thành phần và dễ dàng viết Unit Test")
                            .optionC("Tự động tạo cơ sở dữ liệu trên đám mây")
                            .optionD("Bắt buộc mọi phương thức phải là static")
                            .correctAnswer("B")
                            .explanation("DI đảo ngược quyền điều khiển việc khởi tạo đối tượng, giúp các class lỏng lẻo (loose coupling) và dễ mock khi test.")
                            .build(),
                    QuizQuestion.builder()
                            .quiz(saved)
                            .sortOrder(4)
                            .questionText("Trong Spring Data JPA, interface nào cung cấp sẵn các phương thức CRUD cơ bản và phân trang (Paging/Sorting)?")
                            .optionA("CrudRepository")
                            .optionB("JpaRepository")
                            .optionC("PagingAndSortingRepository")
                            .optionD("Tất cả các đáp án trên đều đúng")
                            .correctAnswer("D")
                            .explanation("JpaRepository kế thừa từ PagingAndSortingRepository, và PagingAndSortingRepository kế thừa từ CrudRepository, do đó cả 3 đều cung cấp các năng lực này.")
                            .build(),
                    QuizQuestion.builder()
                            .quiz(saved)
                            .sortOrder(5)
                            .questionText("Trong kiến trúc RESTful Web Service, HTTP Method nào thường được sử dụng để gửi dữ liệu bài thi lên server để chấm điểm?")
                            .optionA("GET")
                            .optionB("POST")
                            .optionC("PUT")
                            .optionD("DELETE")
                            .correctAnswer("B")
                            .explanation("POST được thiết kế để gửi payload dữ liệu lên server xử lý và tạo mới tài nguyên (ở đây là QuizAttempt).")
                            .build()
            );

            quizQuestionRepository.saveAll(questions);
        }
        } catch (Exception e) {
            log.warn("Không thể khởi tạo dữ liệu mẫu bài thi: {}", e.getMessage());
        }
    }

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
    public void deleteQuiz(Long id) {
        quizQuestionRepository.deleteAllByQuizId(id);
        quizAttemptRepository.deleteAllByQuizId(id);
        quizRepository.deleteById(id);
    }

    public List<QuizAttempt> getRecentAttemptsByCourse(Long courseId) {
        return quizAttemptRepository.findRecentAttemptsByCourseId(courseId);
    }

    @Transactional
    public Quiz createQuizFromPdf(Long courseId, MultipartFile file, String title, String chapterTopic, String difficulty, int totalQuestions, int timeLimitMinutes, Integer fromPage, Integer toPage, String apiKey) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + courseId));

        String text = pdfTextExtractor.extractText(file, fromPage, toPage);
        List<QuizQuestion> questions = quizGeneratorService.generateQuestions(text, totalQuestions, difficulty, apiKey);

        String defaultTitle = (title != null && !title.isBlank()) ? title.trim() : "Đề trắc nghiệm: " + file.getOriginalFilename();
        if (fromPage != null && toPage != null) {
            defaultTitle += " (Trang " + fromPage + "-" + toPage + ")";
        }

        Quiz quiz = Quiz.builder()
                .course(course)
                .title(defaultTitle)
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

    @Transactional
    public Quiz createQuizFromDocument(Long courseId, Long documentId, String title, String chapterTopic, String difficulty, int totalQuestions, int timeLimitMinutes, Integer fromPage, Integer toPage, String apiKey) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + courseId));

        com.group2.assignment.rag.entity.Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu ID: " + documentId));

        if (doc.getFilePath() == null || doc.getFilePath().isBlank()) {
            throw new IllegalArgumentException("Tài liệu '" + doc.getFileName() + "' chưa có file vật lý trên server!");
        }

        java.io.File physicalFile = new java.io.File(doc.getFilePath());
        if (!physicalFile.exists()) {
            throw new IllegalArgumentException("File vật lý của tài liệu không tồn tại trên đĩa: " + doc.getFilePath());
        }

        String text = pdfTextExtractor.extractText(physicalFile, fromPage, toPage);
        List<QuizQuestion> questions = quizGeneratorService.generateQuestions(text, totalQuestions, difficulty, apiKey);

        String defaultTitle = (title != null && !title.isBlank()) ? title.trim() : "Đề thi từ tài liệu: " + doc.getFileName();
        if (fromPage != null && toPage != null) {
            defaultTitle += " (Trang " + fromPage + "-" + toPage + ")";
        }

        Quiz quiz = Quiz.builder()
                .course(course)
                .title(defaultTitle)
                .chapterTopic(chapterTopic != null && !chapterTopic.isBlank() ? chapterTopic.trim() : doc.getFileName())
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
