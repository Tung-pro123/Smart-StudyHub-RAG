package com.group2.assignment.quiz;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.quiz.repository.QuizQuestionRepository;
import com.group2.assignment.quiz.repository.QuizRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuizControllerExportConcurrencyAndBoundaryTest {

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

    private User freeUser;
    private User proUser;
    private User squadUser;
    private User campusUser;
    private User adminUser;

    private Course course;
    private Quiz standardQuiz;
    private List<Quiz> quizzesToClean = new ArrayList<>();
    private List<User> usersToClean = new ArrayList<>();
    private List<Course> coursesToClean = new ArrayList<>();

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();

        freeUser = userRepository.save(User.builder()
                .username("stress_free_" + timestamp)
                .email("free_" + timestamp + "@gmail.com")
                .fullName("Sinh Vien Free Tier")
                .password("hash123")
                .subscriptionTier("FREE")
                .role("ROLE_STUDENT")
                .build());
        usersToClean.add(freeUser);

        proUser = userRepository.save(User.builder()
                .username("stress_pro_" + timestamp)
                .email("pro_" + timestamp + "@gmail.com")
                .fullName("Sinh Vien Pro Tier")
                .password("hash123")
                .subscriptionTier("PRO")
                .role("ROLE_STUDENT")
                .build());
        usersToClean.add(proUser);

        squadUser = userRepository.save(User.builder()
                .username("stress_squad_" + timestamp)
                .email("squad_" + timestamp + "@gmail.com")
                .fullName("Sinh Vien Squad Tier")
                .password("hash123")
                .subscriptionTier("SQUAD")
                .role("ROLE_STUDENT")
                .build());
        usersToClean.add(squadUser);

        campusUser = userRepository.save(User.builder()
                .username("stress_campus_" + timestamp)
                .email("campus_" + timestamp + "@gmail.com")
                .fullName("Sinh Vien Campus Tier")
                .password("hash123")
                .subscriptionTier("CAMPUS")
                .role("ROLE_STUDENT")
                .build());
        usersToClean.add(campusUser);

        adminUser = userRepository.save(User.builder()
                .username("stress_admin_" + timestamp)
                .email("admin_" + timestamp + "@gmail.com")
                .fullName("Quan Tri Vien")
                .password("hash123")
                .subscriptionTier("FREE")
                .role("ROLE_ADMIN")
                .build());
        usersToClean.add(adminUser);

        course = courseRepository.save(Course.builder()
                .name("Kien Truc Microservices & He Thong Phan Tan")
                .code("DIST_SYS_" + timestamp)
                .description("Khoa hoc kiem thu dong thoi va bien do")
                .build());
        coursesToClean.add(course);

        standardQuiz = quizRepository.save(Quiz.builder()
                .title("De Thi Danh Gia Nang Luc He Thong Phan Tan")
                .course(course)
                .difficulty("HARD")
                .totalQuestions(3)
                .timeLimitMinutes(25)
                .build());
        quizzesToClean.add(standardQuiz);

        quizQuestionRepository.saveAll(List.of(
                QuizQuestion.builder()
                        .quiz(standardQuiz)
                        .sortOrder(1)
                        .questionText("CAP Theorem khang dinh he thong phan tan chi co the thoa man toi da may tinh chat dong thoi?")
                        .optionA("1 tinh chat")
                        .optionB("2 tinh chat")
                        .optionC("3 tinh chat")
                        .optionD("Khong co tinh chat nao")
                        .correctAnswer("B")
                        .explanation("CAP quy dinh chi chon duoc toi da 2 trong 3 yeu to: Consistency, Availability, Partition tolerance.")
                        .build(),
                QuizQuestion.builder()
                        .quiz(standardQuiz)
                        .sortOrder(2)
                        .questionText("Giai thuat nao pho bien de dat su dong thuan (Consensus) trong he thong phan tan?")
                        .optionA("Raft va Paxos")
                        .optionB("Bubble Sort")
                        .optionC("Binary Search")
                        .optionD("Dijkstra")
                        .correctAnswer("A")
                        .explanation("Raft va Paxos la hai giao thuc dong thuan pho bien nhat.")
                        .build(),
                QuizQuestion.builder()
                        .quiz(standardQuiz)
                        .sortOrder(3)
                        .questionText("De tranh race condition khi xuat PDF hang loat can dam bao dieu gi?")
                        .optionA("Thread safety va state isolation giua cac phien")
                        .optionB("Dung bien static toan cuc khong dong bo")
                        .optionC("Tat ca cac luong dung chung mot session")
                        .optionD("Khong can kiem tra session")
                        .correctAnswer("A")
                        .explanation("Moi request phai su dung Session va State doc lap, khong dung bien mutable toan cuc.")
                        .build()
        ));
    }

    @AfterEach
    void tearDown() {
        for (Quiz q : quizzesToClean) {
            try {
                quizQuestionRepository.deleteAllByQuizId(q.getId());
                quizRepository.deleteById(q.getId());
            } catch (Exception ignored) {
            }
        }
        for (Course c : coursesToClean) {
            try {
                courseRepository.deleteById(c.getId());
            } catch (Exception ignored) {
            }
        }
        for (User u : usersToClean) {
            try {
                userRepository.deleteById(u.getId());
            } catch (Exception ignored) {
            }
        }
    }

    private MockHttpSession sessionFor(User user) {
        MockHttpSession session = new MockHttpSession();
        if (user != null) {
            session.setAttribute(AuthController.SESSION_USER, user);
        }
        return session;
    }

    // =========================================================================
    // 1. HIGH CONCURRENCY MULTI-THREADED TESTS
    // =========================================================================

    @Test
    @DisplayName("CONCURRENCY-1: 30 luồng đồng thời gọi API xuất PDF từ PRO user không bị race condition")
    void testHighConcurrencyProTier30Threads() throws Exception {
        int threadCount = 30;
        ExecutorService executor = Executors.newFixedThreadPool(15);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final boolean showAnswers = (i % 2 == 0);
            executor.submit(() -> {
                try {
                    startGate.await();

                    MvcResult result = mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                                    .param("showAnswers", String.valueOf(showAnswers))
                                    .session(sessionFor(proUser)))
                            .andExpect(status().isOk())
                            .andExpect(header().string("Content-Type", containsString("application/pdf")))
                            .andReturn();

                    byte[] content = result.getResponse().getContentAsByteArray();
                    assertNotNull(content);
                    assertTrue(content.length > 500);

                    // Check PDF magic header
                    assertEquals('%', (char) content[0]);
                    assertEquals('P', (char) content[1]);
                    assertEquals('D', (char) content[2]);
                    assertEquals('F', (char) content[3]);

                    // Verify PDF structure with PDFBox
                    try (PDDocument doc = PDDocument.load(content)) {
                        assertTrue(doc.getNumberOfPages() >= 1);
                        PDFTextStripper stripper = new PDFTextStripper();
                        String text = stripper.getText(doc);
                        assertTrue(text.contains("De Thi Danh Gia Nang Luc"));
                        if (showAnswers) {
                            assertTrue(text.contains("ĐÁP ÁN VÀ LỜI GIẢI CHI TIẾT"));
                        }
                    }

                    successCount.incrementAndGet();
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();
        boolean completed = endGate.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Toan bo 30 luong phai hoan thanh trong 60 giay");
        if (!errors.isEmpty()) {
            fail("Phat hien loi trong 30 luong dong thoi: " + errors.get(0).getMessage());
        }
        assertEquals(threadCount, successCount.get(), "Tat ca 30 request PRO deu phai thanh cong 200 OK");
    }

    @Test
    @DisplayName("CONCURRENCY-2: 40 luồng hỗn hợp các gói (10 Pro, 10 Squad, 10 Free, 10 Chưa login) - Tuyệt đối không rò rỉ Session")
    void testConcurrentMixedTierSessionsZeroLeakage() throws Exception {
        int countPerTier = 10;
        int totalThreads = countPerTier * 4; // 40 threads
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(totalThreads);

        AtomicInteger pro200Count = new AtomicInteger(0);
        AtomicInteger squad200Count = new AtomicInteger(0);
        AtomicInteger free403Count = new AtomicInteger(0);
        AtomicInteger unauth401Count = new AtomicInteger(0);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        // Create tasks list
        List<Runnable> tasks = new ArrayList<>();

        // 10 PRO tasks
        for (int i = 0; i < countPerTier; i++) {
            tasks.add(() -> {
                try {
                    startGate.await();
                    MvcResult res = mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                                    .session(sessionFor(proUser)))
                            .andReturn();
                    int st = res.getResponse().getStatus();
                    if (st == 200) {
                        pro200Count.incrementAndGet();
                    } else {
                        errors.add(new AssertionError("PRO mong doi 200 nhung nhan: " + st));
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    endGate.countDown();
                }
            });
        }

        // 10 SQUAD tasks
        for (int i = 0; i < countPerTier; i++) {
            tasks.add(() -> {
                try {
                    startGate.await();
                    MvcResult res = mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                                    .session(sessionFor(squadUser)))
                            .andReturn();
                    int st = res.getResponse().getStatus();
                    if (st == 200) {
                        squad200Count.incrementAndGet();
                    } else {
                        errors.add(new AssertionError("SQUAD mong doi 200 nhung nhan: " + st));
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    endGate.countDown();
                }
            });
        }

        // 10 FREE tasks
        for (int i = 0; i < countPerTier; i++) {
            tasks.add(() -> {
                try {
                    startGate.await();
                    MvcResult res = mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                                    .session(sessionFor(freeUser)))
                            .andReturn();
                    int st = res.getResponse().getStatus();
                    if (st == 403) {
                        free403Count.incrementAndGet();
                    } else {
                        errors.add(new AssertionError("FREE mong doi 403 nhung nhan: " + st));
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    endGate.countDown();
                }
            });
        }

        // 10 UNAUTH tasks (no session attribute)
        for (int i = 0; i < countPerTier; i++) {
            tasks.add(() -> {
                try {
                    startGate.await();
                    MvcResult res = mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf"))
                            .andReturn();
                    int st = res.getResponse().getStatus();
                    if (st == 401) {
                        unauth401Count.incrementAndGet();
                    } else {
                        errors.add(new AssertionError("UNAUTH mong doi 401 nhung nhan: " + st));
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    endGate.countDown();
                }
            });
        }

        // Shuffle tasks to maximize concurrency interleaving
        Collections.shuffle(tasks);
        tasks.forEach(executor::submit);

        // Unleash all 40 threads simultaneously
        startGate.countDown();
        boolean completed = endGate.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Tat ca 40 luong hon hop phai hoan tat trong 60 giay");
        if (!errors.isEmpty()) {
            fail("Phat hien loi cheo session giua cac goi c uoc: " + errors.get(0).getMessage());
        }

        assertEquals(10, pro200Count.get(), "Chinh xac 10 PRO tra ve 200");
        assertEquals(10, squad200Count.get(), "Chinh xac 10 SQUAD tra ve 200");
        assertEquals(10, free403Count.get(), "Chinh xac 10 FREE bi chan 403");
        assertEquals(10, unauth401Count.get(), "Chinh xac 10 chua dang nhap bi chan 401");
    }

    // =========================================================================
    // 2. BOUNDARY VALUES TESTING
    // =========================================================================

    @Test
    @DisplayName("BOUNDARY-1: Quiz title là chuỗi rỗng \"\" -> Xuất PDF thành công với tên file mặc định de-thi.pdf")
    void testBoundaryEmptyStringQuizTitle() throws Exception {
        Quiz emptyTitleQuiz = quizRepository.save(Quiz.builder()
                .title("")
                .course(course)
                .difficulty("EASY")
                .totalQuestions(1)
                .timeLimitMinutes(10)
                .build());
        quizzesToClean.add(emptyTitleQuiz);

        quizQuestionRepository.save(QuizQuestion.builder()
                .quiz(emptyTitleQuiz)
                .sortOrder(1)
                .questionText("Cau hoi kiem thu quiz title rong?")
                .optionA("Dap an A")
                .optionB("Dap an B")
                .optionC("Dap an C")
                .optionD("Dap an D")
                .correctAnswer("A")
                .explanation("Giai thich")
                .build());

        // Without answers
        mockMvc.perform(get("/api/quiz/" + emptyTitleQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "false")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"de-thi.pdf\""));

        // With answers
        mockMvc.perform(get("/api/quiz/" + emptyTitleQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "true")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"de-thi_kem_dap_an.pdf\""));
    }

    @Test
    @DisplayName("BOUNDARY-2: Quiz title là khoảng trắng blank -> Chuẩn hóa về de-thi.pdf")
    void testBoundaryBlankAndWhitespaceQuizTitle() throws Exception {
        Quiz blankTitleQuiz = quizRepository.save(Quiz.builder()
                .title("     ")
                .course(course)
                .difficulty("MEDIUM")
                .totalQuestions(1)
                .timeLimitMinutes(15)
                .build());
        quizzesToClean.add(blankTitleQuiz);

        mockMvc.perform(get("/api/quiz/" + blankTitleQuiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"de-thi.pdf\""));
    }

    @Test
    @DisplayName("BOUNDARY-3: Quiz title thuần ký tự đặc biệt nguy hiểm -> Chuẩn hóa an toàn thành de-thi.pdf")
    void testBoundarySpecialCharactersOnlyQuizTitle() throws Exception {
        Quiz specialCharsQuiz = quizRepository.save(Quiz.builder()
                .title("!@#$%^&*()_+={}[]|:;\"'<>?~`")
                .course(course)
                .difficulty("HARD")
                .totalQuestions(1)
                .timeLimitMinutes(15)
                .build());
        quizzesToClean.add(specialCharsQuiz);

        mockMvc.perform(get("/api/quiz/" + specialCharsQuiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"de-thi.pdf\""));
    }

    @Test
    @DisplayName("BOUNDARY-4: Quiz title tiếng Việt phức tạp chứa chữ Đ/đ hoa thường, dấu gạch nối và dấu thanh")
    void testBoundaryComplexVietnameseTitle() throws Exception {
        Quiz viQuiz = quizRepository.save(Quiz.builder()
                .title("Đề Thi Đánh Giá Kỹ Năng Đồ Họa & Điện Toán Đám Mây - Học Kỳ 1 (2026)")
                .course(course)
                .difficulty("MEDIUM")
                .totalQuestions(1)
                .timeLimitMinutes(20)
                .build());
        quizzesToClean.add(viQuiz);

        mockMvc.perform(get("/api/quiz/" + viQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "true")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"De_Thi_Danh_Gia_Ky_Nang_Do_Hoa_Dien_Toan_Dam_May_-_Hoc_Ky_1_2026_kem_dap_an.pdf\""));
    }

    @Test
    @DisplayName("BOUNDARY-5: Đề thi không có câu hỏi nào (0 questions) -> Không bị lỗi 500, trả về PDF rỗng hợp lệ")
    void testBoundaryZeroQuestionsQuizReturnsValidPdf() throws Exception {
        Quiz emptyQuiz = quizRepository.save(Quiz.builder()
                .title("De Thi Chua Co Cau Hoi Nao")
                .course(course)
                .difficulty("EASY")
                .totalQuestions(0)
                .timeLimitMinutes(10)
                .build());
        quizzesToClean.add(emptyQuiz);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + emptyQuiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertNotNull(content);
        assertTrue(content.length > 500, "PDF sinh ra van phai hop le");
        try (PDDocument doc = PDDocument.load(content)) {
            assertTrue(doc.getNumberOfPages() >= 1);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            assertTrue(text.contains("De Thi Chua Co Cau Hoi Nao"));
        }
    }

    @Test
    @DisplayName("BOUNDARY-6: Đề thi quy mô lớn 50 câu hỏi kèm đáp án -> PDF nhiều trang hoàn chỉnh")
    void testBoundaryLargeQuiz50Questions() throws Exception {
        Quiz bigQuiz = quizRepository.save(Quiz.builder()
                .title("De Thi Thu Quy Mo Lon 50 Cau Hoi")
                .course(course)
                .difficulty("HARD")
                .totalQuestions(50)
                .timeLimitMinutes(60)
                .build());
        quizzesToClean.add(bigQuiz);

        List<QuizQuestion> questions = new ArrayList<>(50);
        for (int i = 1; i <= 50; i++) {
            questions.add(QuizQuestion.builder()
                    .quiz(bigQuiz)
                    .sortOrder(i)
                    .questionText("Noi dung cau hoi so " + i + ": Danh gia nang luc lap trinh?")
                    .optionA("Dap an A cau " + i)
                    .optionB("Dap an B cau " + i)
                    .optionC("Dap an C cau " + i)
                    .optionD("Dap an D cau " + i)
                    .correctAnswer("A")
                    .explanation("Loi giai thich chi tiet cho cau hoi " + i + ".")
                    .build());
        }
        quizQuestionRepository.saveAll(questions);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + bigQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "true")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        byte[] pdf = result.getResponse().getContentAsByteArray();
        assertNotNull(pdf);
        assertTrue(pdf.length > 10000, "PDF 50 cau kem dap an phai co dung luong > 10KB");

        try (PDDocument doc = PDDocument.load(pdf)) {
            assertTrue(doc.getNumberOfPages() >= 3, "De thi 50 cau phai co it nhat 3 trang");
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            assertTrue(text.contains("De Thi Thu Quy Mo Lon 50 Cau Hoi"));
            assertTrue(text.contains("50"));
            assertTrue(text.contains("Loi giai thich chi tiet cho cau hoi 50."));
        }
    }

    @Test
    @DisplayName("BOUNDARY-7: ID đề thi không hợp lệ: số âm, số 0, hoặc ID không tồn tại -> Trả về 404 Not Found")
    void testBoundaryNegativeAndInvalidQuizIds() throws Exception {
        // ID âm -1
        mockMvc.perform(get("/api/quiz/-1/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isNotFound());

        // ID âm cực lớn -999999
        mockMvc.perform(get("/api/quiz/-999999/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isNotFound());

        // ID = 0
        mockMvc.perform(get("/api/quiz/0/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isNotFound());

        // ID dương cực lớn không tồn tại
        mockMvc.perform(get("/api/quiz/999999999/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("BOUNDARY-8: ID đề thi không phải kiểu số (chuỗi ký tự abc) -> Trả về 400 Bad Request")
    void testBoundaryNonNumericQuizIdReturns400() throws Exception {
        mockMvc.perform(get("/api/quiz/not-a-number/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // 3. AUTHENTICATION & TIER MATRIX TESTING
    // =========================================================================

    @Test
    @DisplayName("AUTH-1: Session rỗng hoặc chưa set thuộc tính SESSION_USER -> Trả về 401 Unauthorized")
    void testEmptySessionReturns401() throws Exception {
        MockHttpSession emptySession = new MockHttpSession();
        mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .session(emptySession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-2: Gói CAMPUS được phép xuất PDF thành công (CAMPUS là gói Pro cấp trường)")
    void testCampusTierReturns200() throws Exception {
        mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .session(sessionFor(campusUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")));
    }

    @Test
    @DisplayName("AUTH-3: User có role ROLE_ADMIN (dù subscriptionTier là FREE) -> Vẫn được phép xuất PDF thành công")
    void testAdminWithFreeTierReturns200() throws Exception {
        mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .session(sessionFor(adminUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")));
    }

    @Test
    @DisplayName("AUTH-4: Cả 2 đường dẫn route alias (/api/quiz và /quiz) đều hỗ trợ xuất PDF đồng nhất")
    void testRouteAliasesParity() throws Exception {
        // Route 1: /api/quiz/{id}/export-pdf
        mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")));

        // Route 2: /quiz/{id}/export-pdf
        mockMvc.perform(get("/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")));
    }
}
