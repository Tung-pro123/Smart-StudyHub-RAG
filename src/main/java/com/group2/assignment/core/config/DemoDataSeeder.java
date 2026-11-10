package com.group2.assignment.core.config;

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
import com.group2.assignment.rag.entity.Document;
import com.group2.assignment.rag.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final DocumentRepository documentRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Override
    public void run(String... args) {
        log.info("Bắt đầu khởi tạo & liên kết dữ liệu môn học demo theo từng tài khoản (Data Isolation)...");

        User student = userRepository.findByUsernameIgnoreCase("student").orElse(null);
        User proStudent = userRepository.findByUsernameIgnoreCase("pro_student").orElse(null);
        User squadLeader = userRepository.findByUsernameIgnoreCase("squad_leader").orElse(null);
        User squadMember1 = userRepository.findByUsernameIgnoreCase("squad_member1").orElse(null);

        // 1. Gán môn HSF302 và MAS291 cho student (Gói Free: 2 môn, tạo thêm 1 môn nữa là đạt hạn mức tối đa 3 môn)
        Course hsf302 = null;
        if (student != null) {
            hsf302 = assignOrCreateCourse("HSF302", "JPA and Spring Framework",
                    "Lập trình ứng dụng web nâng cao với Spring Boot 3, Spring Data JPA, Thymeleaf và RAG AI.",
                    "from-indigo-600 to-purple-600", "fa-laptop-code", "Fall 2026", 3, student);

            assignOrCreateCourse("MAS291", "Probability and Statistics",
                    "Xác suất thống kê ứng dụng trong khoa học dữ liệu, kiểm định giả thuyết và machine learning.",
                    "from-emerald-600 to-teal-600", "fa-chart-pie", "Fall 2026", 3, student);
        }

        // 2. Tạo 4 môn học cho pro_student (Gói Pro: không giới hạn môn học)
        if (proStudent != null) {
            assignOrCreateCourse("FER202", "Front-End Web Development with React",
                    "Xây dựng giao diện Single Page Application hiện đại với ReactJS, Tailwind CSS và State Management.",
                    "from-blue-600 to-cyan-600", "fa-brands fa-react", "Fall 2026", 3, proStudent);

            assignOrCreateCourse("SWE201", "Introduction to Software Engineering",
                    "Quy trình công nghệ phần mềm chuyên nghiệp, mô hình Waterfall, Agile/Scrum và kiểm thử.",
                    "from-violet-600 to-fuchsia-600", "fa-layer-group", "Fall 2026", 3, proStudent);

            assignOrCreateCourse("DBI202", "Introduction to Databases",
                    "Thiết kế cơ sở dữ liệu quan hệ, chuẩn hóa dữ liệu 3NF, BCNF và tối ưu hóa truy vấn SQL nâng cao.",
                    "from-amber-500 to-orange-600", "fa-database", "Fall 2026", 3, proStudent);

            assignOrCreateCourse("ITE302c", "Ethics in Information Technology",
                    "Đạo đức nghề nghiệp và pháp lý trong công nghệ thông tin, an toàn thông tin và AI ethics.",
                    "from-teal-600 to-cyan-700", "fa-scale-balanced", "Fall 2026", 3, proStudent);
        }

        // 3. Tạo 2 môn học cho squad_leader (Gói Study Squad) — đều là môn Nhóm dùng chung (squadShared=true)
        Course exe101 = null;
        Course lab211 = null;
        if (squadLeader != null) {
            exe101 = assignOrCreateCourse("EXE101", "Experiential Entrepreneurship",
                    "Dự án khởi nghiệp đổi mới sáng tạo, nghiên cứu thị trường và pitching dự án nhóm.",
                    "from-rose-500 to-pink-600", "fa-lightbulb", "Fall 2026", 3, squadLeader, true);

            lab211 = assignOrCreateCourse("LAB211", "OOP Java Programming Lab",
                    "Thực hành lập trình hướng đối tượng Java thuần, thiết kế mẫu Design Pattern và tối ưu mã nguồn.",
                    "from-sky-600 to-indigo-700", "fa-diagram-project", "Fall 2026", 3, squadLeader, true);
        }

        // 4. Seed tài liệu PDF giáo trình mẫu cho HSF302, EXE101, LAB211
        seedDocumentForCourse(hsf302, "Slide_HSF302_Spring_JPA.pdf", "HSF302 - JPA and Spring Boot Architecture Guide");
        seedDocumentForCourse(exe101, "Slide_EXE101_Startup_Project.pdf", "EXE101 - Experiential Entrepreneurship Guide");
        seedDocumentForCourse(lab211, "Slide_LAB211_OOP_Java.pdf", "LAB211 - OOP Java Design Patterns");

        // 5. Seed dữ liệu đề thi và lượt thi Squad Leaderboard cho EXE101
        seedSquadQuizAndAttempts(exe101, squadLeader, squadMember1);

        log.info("Hoàn tất thiết lập môn học và dữ liệu demo cho tất cả các gói cước!");
    }

    private Course assignOrCreateCourse(String code, String name, String desc, String color, String icon,
                                        String semester, int credits, User owner) {
        return assignOrCreateCourse(code, name, desc, color, icon, semester, credits, owner, false);
    }

    private Course assignOrCreateCourse(String code, String name, String desc, String color, String icon,
                                        String semester, int credits, User owner, boolean squadShared) {
        Course existing = null;
        if (owner != null && owner.getId() != null) {
            existing = courseRepository.findByCodeIgnoreCaseAndUserId(code, owner.getId()).orElse(null);
        }
        if (existing == null) {
            existing = courseRepository.findFirstByCodeIgnoreCase(code).orElse(null);
        }
        if (existing != null) {
            if (existing.getUser() == null) {
                existing.setUser(owner);
                existing.setSquadShared(squadShared);
                return courseRepository.save(existing);
            }
            // Nếu đã tồn tại, cập nhật squadShared nếu cần
            if (existing.getSquadShared() == null || existing.getSquadShared() != squadShared) {
                existing.setSquadShared(squadShared);
                courseRepository.save(existing);
            }
            return existing;
        } else {
            return courseRepository.save(Course.builder()
                    .code(code)
                    .name(name)
                    .description(desc)
                    .color(color)
                    .icon(icon)
                    .semester(semester)
                    .credits(credits)
                    .user(owner)
                    .squadShared(squadShared)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    private void seedDocumentForCourse(Course course, String fileName, String titleText) {
        if (course == null) return;
        List<Document> existingDocs = documentRepository.findAllByCourseIdOrderByUploadedAtDesc(course.getId());
        if (!existingDocs.isEmpty()) return;

        Path targetPath = Paths.get("uploads", "courses", String.valueOf(course.getId()), fileName);
        createSamplePdfIfNotExists(targetPath, titleText);

        long fileSize = 1024L;
        try {
            if (Files.exists(targetPath)) {
                fileSize = Files.size(targetPath);
            }
        } catch (Exception ignored) {}

        documentRepository.save(Document.builder()
                .course(course)
                .uploadedBy(course != null ? course.getUser() : null)
                .fileName(fileName)
                .filePath(targetPath.toString())
                .fileType("application/pdf")
                .fileSize(fileSize)
                .totalPages(5)
                .totalChunks(10)
                .status("INDEXED")
                .uploadedAt(LocalDateTime.now())
                .build());
        log.info("Đã tạo tài liệu mẫu '{}' cho môn {}", fileName, course.getCode());
    }

    private void createSamplePdfIfNotExists(Path targetPath, String contentTitle) {
        try {
            if (Files.exists(targetPath)) {
                return;
            }
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }

            // Ưu tiên copy nếu có file PDF mẫu sẵn trên server
            Path existingPdf = Paths.get("uploads", "courses", "1", "1793409806086_Clean_code_in_Java.pdf");
            if (Files.exists(existingPdf)) {
                Files.copy(existingPdf, targetPath, StandardCopyOption.REPLACE_EXISTING);
                return;
            }

            // Hoặc sinh file PDF 2 trang chuẩn bằng Apache PDFBox
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    cs.newLineAtOffset(50, 700);
                    cs.showText(contentTitle);
                    cs.newLineAtOffset(0, -25);
                    cs.setFont(PDType1Font.HELVETICA, 10);
                    cs.showText("Sample Course Slide document for StudyHub AI Quiz Generator.");
                    cs.endText();
                }
                document.save(targetPath.toFile());
            }
        } catch (Exception e) {
            log.warn("Không thể tạo file PDF mẫu {}: {}", targetPath, e.getMessage());
        }
    }

    private void seedSquadQuizAndAttempts(Course course, User squadLeader, User squadMember1) {
        if (course == null || squadLeader == null || squadMember1 == null) return;

        List<Quiz> existingQuizzes = quizRepository.findAllByCourseIdOrderByCreatedAtDesc(course.getId());
        Quiz squadQuiz;
        if (existingQuizzes.isEmpty()) {
            squadQuiz = quizRepository.save(Quiz.builder()
                    .course(course)
                    .createdBy(squadLeader)
                    .title("Đề Thi Squad: Dự Án Khởi Nghiệp Đổi Mới Sáng Tạo EXE101")
                    .chapterTopic("Chương 1 & 2: Nghiên cứu thị trường và Lean Canvas")
                    .difficulty("MEDIUM")
                    .totalQuestions(5)
                    .timeLimitMinutes(15)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build());

            List<QuizQuestion> questions = List.of(
                    QuizQuestion.builder().quiz(squadQuiz).sortOrder(1)
                            .questionText("Trong mô hình Lean Canvas, yếu tố nào xác định lợi thế cạnh tranh khó sao chép?")
                            .optionA("Unfair Advantage").optionB("Unique Value Proposition").optionC("Cost Structure").optionD("Key Metrics")
                            .correctAnswer("A").explanation("Unfair Advantage là lợi thế cạnh tranh độc nhất mà đối thủ không thể dễ dàng mua hoặc sao chép.").build(),
                    QuizQuestion.builder().quiz(squadQuiz).sortOrder(2)
                            .questionText("Mục đích chính của sản phẩm khả dụng tối thiểu (MVP) là gì?")
                            .optionA("Bán kiếm lợi nhuận ngay").optionB("Kiểm chứng giả định thị trường với chi phí tối thiểu").optionC("Hoàn thiện toàn bộ tính năng").optionD("Thu hút vốn đầu tư mạo hiểm")
                            .correctAnswer("B").explanation("MVP giúp startup kiểm chứng nhanh các giả định kinh doanh với nguồn lực và chi phí tối thiểu.").build(),
                    QuizQuestion.builder().quiz(squadQuiz).sortOrder(3)
                            .questionText("Khách hàng mục tiêu trong giai đoạn đầu của startup được gọi là gì?")
                            .optionA("Late Majority").optionB("Laggards").optionC("Early Adopters").optionD("General Public")
                            .correctAnswer("C").explanation("Early Adopters là nhóm khách hàng tiên phong, sẵn sàng chấp nhận rủi ro dùng thử sản phẩm mới.").build(),
                    QuizQuestion.builder().quiz(squadQuiz).sortOrder(4)
                            .questionText("Chỉ số CAC trong khởi nghiệp đo lường điều gì?")
                            .optionA("Customer Acquisition Cost").optionB("Company Asset Capital").optionC("Cash And Capital").optionD("Current Annual Cost")
                            .correctAnswer("A").explanation("CAC là chi phí để có được một khách hàng mới (Customer Acquisition Cost).").build(),
                    QuizQuestion.builder().quiz(squadQuiz).sortOrder(5)
                            .questionText("Khái niệm 'Pivot' trong Lean Startup có nghĩa là gì?")
                            .optionA("Đóng cửa công ty").optionB("Chuyển hướng chiến lược kinh doanh khi hướng đi cũ không hiệu quả").optionC("Chia tách cổ phần").optionD("Sa thải nhân sự")
                            .correctAnswer("B").explanation("Pivot là quyết định thay đổi hướng đi chiến lược sản phẩm dựa trên phản hồi thực tế của khách hàng.").build()
            );
            quizQuestionRepository.saveAll(questions);
            log.info("Đã tạo đề thi squad mẫu cho môn {}", course.getCode());
        } else {
            squadQuiz = existingQuizzes.get(0);
        }

        if (quizAttemptRepository.countByQuizId(squadQuiz.getId()) == 0) {
            // Member đạt 10.0 điểm
            quizAttemptRepository.save(QuizAttempt.builder()
                    .quiz(squadQuiz)
                    .user(squadMember1)
                    .studentName(squadMember1.getFullName())
                    .totalScore(10.0)
                    .correctCount(5)
                    .totalCount(5)
                    .userAnswers("{\"1\":\"A\",\"2\":\"B\",\"3\":\"C\",\"4\":\"A\",\"5\":\"B\"}")
                    .timeSpentSeconds(180)
                    .completedAt(LocalDateTime.now().minusMinutes(20))
                    .build());

            // Leader đạt 8.0 điểm
            quizAttemptRepository.save(QuizAttempt.builder()
                    .quiz(squadQuiz)
                    .user(squadLeader)
                    .studentName(squadLeader.getFullName())
                    .totalScore(8.0)
                    .correctCount(4)
                    .totalCount(5)
                    .userAnswers("{\"1\":\"A\",\"2\":\"B\",\"3\":\"C\",\"4\":\"A\",\"5\":\"A\"}")
                    .timeSpentSeconds(240)
                    .completedAt(LocalDateTime.now().minusHours(1))
                    .build());

            log.info("Đã khởi tạo 2 lượt thi đua điểm số cho squad_member1 (10.0đ) và squad_leader (8.0đ)!");
        }
    }
}
