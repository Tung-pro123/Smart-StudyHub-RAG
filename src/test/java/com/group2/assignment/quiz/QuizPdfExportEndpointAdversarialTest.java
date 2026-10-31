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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class QuizPdfExportEndpointAdversarialTest {

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
    private com.group2.assignment.quiz.service.QuizPdfExportService quizPdfExportService;

    private User proUser;
    private User freeUser;
    private User adminUser;
    private User campusUser;
    private User squadUser;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();

        proUser = userRepository.save(User.builder()
                .username("adv_pro_" + ts)
                .email("adv_pro_" + ts + "@studyhub.vn")
                .fullName("Pro User Stress")
                .password("hash123")
                .subscriptionTier("PRO")
                .role("ROLE_STUDENT")
                .build());

        freeUser = userRepository.save(User.builder()
                .username("adv_free_" + ts)
                .email("adv_free_" + ts + "@studyhub.vn")
                .fullName("Free User Stress")
                .password("hash123")
                .subscriptionTier("FREE")
                .role("ROLE_STUDENT")
                .build());

        adminUser = userRepository.save(User.builder()
                .username("adv_admin_" + ts)
                .email("adv_admin_" + ts + "@studyhub.vn")
                .fullName("Admin User Stress")
                .password("hash123")
                .subscriptionTier("FREE")
                .role("ROLE_ADMIN")
                .build());

        campusUser = userRepository.save(User.builder()
                .username("adv_campus_" + ts)
                .email("adv_campus_" + ts + "@studyhub.vn")
                .fullName("Campus User Stress")
                .password("hash123")
                .subscriptionTier("CAMPUS")
                .role("ROLE_STUDENT")
                .build());

        squadUser = userRepository.save(User.builder()
                .username("adv_squad_" + ts)
                .email("adv_squad_" + ts + "@studyhub.vn")
                .fullName("Squad User Stress")
                .password("hash123")
                .subscriptionTier("SQUAD")
                .role("ROLE_STUDENT")
                .build());

        testCourse = courseRepository.save(Course.builder()
                .name("Adversarial Stress Test Course")
                .code("ADV_" + ts)
                .description("Course for adversarial testing of quiz export")
                .build());
    }

    private MockHttpSession sessionFor(User user) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, user);
        return session;
    }

    private Quiz createQuiz(String title, int questionCount) {
        Quiz quiz = quizRepository.save(Quiz.builder()
                .title(title)
                .course(testCourse)
                .difficulty("HARD")
                .totalQuestions(questionCount)
                .timeLimitMinutes(30)
                .build());

        for (int i = 1; i <= questionCount; i++) {
            quizQuestionRepository.save(QuizQuestion.builder()
                    .quiz(quiz)
                    .sortOrder(i)
                    .questionText("Stress Question #" + i + " for " + title)
                    .optionA("Lựa chọn A")
                    .optionB("Lựa chọn B")
                    .optionC("Lựa chọn C")
                    .optionD("Lựa chọn D")
                    .correctAnswer("A")
                    .explanation("Giải thích chi tiết cho câu hỏi " + i)
                    .build());
        }
        return quiz;
    }

    private String extractFilenameFromHeader(String contentDisposition) {
        assertNotNull(contentDisposition, "Content-Disposition header must not be null");
        Pattern pattern = Pattern.compile("filename=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(contentDisposition);
        assertTrue(matcher.find(), "Content-Disposition must contain filename=\"...\": " + contentDisposition);
        return matcher.group(1);
    }

    // =========================================================================
    // 1. Stress Testing: Complex Vietnamese Tone Marks & Diacritics Normalization
    // =========================================================================

    @Test
    @DisplayName("Adversarial: Toàn bộ 27 âm sắc tiếng Việt (ả, ã, ạ, ắ, ặ, ẳ, ẵ, ế, ề, ể, ễ, ệ, ố, ồ, ổ, ỗ, ộ, ứ, ừ, ử, ữ, ự, ý, ỳ, ỷ, ỹ, ỵ)")
    void testAll27VietnameseToneMarksNormalization() throws Exception {
        String complexToneTitle = "Đề thi thử ả ã ạ ắ ặ ẳ ẵ ế ề ể ễ ệ ố ồ ổ ỗ ộ ứ ừ ử ữ ự ý ỳ ỷ ỹ ỵ và Đ đ";
        Quiz quiz = createQuiz(complexToneTitle, 1);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/pdf")))
                .andReturn();

        String cd = result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION);
        String filename = extractFilenameFromHeader(cd);

        // Đảm bảo không còn bất kỳ ký tự non-ASCII nào trong filename
        assertFalse(filename.chars().anyMatch(c -> c > 127), "Filename must not contain non-ASCII chars: " + filename);
        // Đảm bảo đ và Đ được chuyển thành d và D
        assertFalse(filename.contains("đ") || filename.contains("Đ"), "Filename must not contain đ/Đ: " + filename);
        assertTrue(filename.startsWith("De_thi_thu_a_a_a_a_a_a_a_e_e_e_e_e_o_o_o_o_o_u_u_u_u_u_y_y_y_y_y_va_D_d"),
                "Filename should normalize all vowels and đ/Đ properly, but was: " + filename);
        assertTrue(filename.endsWith(".pdf"));
    }

    @Test
    @DisplayName("Adversarial: Vietnamese Uppercase Vowels with Diacritics (Ả, Ã, Ạ, Ắ, Ặ, Ẳ, Ẵ, Ế, Ề, Ể, Ễ, Ệ, Ố, Ồ, Ổ, Ỗ, Ộ, Ứ, Ừ, Ử, Ữ, Ự, Ý, Ỳ, Ỷ, Ỹ, Ỵ)")
    void testUppercaseVietnameseDiacriticsNormalization() throws Exception {
        String uppercaseToneTitle = "BÀI TẬP Ả Ã Ạ Ắ Ặ Ẳ Ẵ Ế Ề Ể Ễ Ệ Ố Ồ Ổ Ỗ Ộ Ứ Ừ Ử Ữ Ự Ý Ỳ Ỷ Ỹ Ỵ ĐỒ ÁN";
        Quiz quiz = createQuiz(uppercaseToneTitle, 1);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        String filename = extractFilenameFromHeader(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertFalse(filename.chars().anyMatch(c -> c > 127), "No non-ASCII allowed: " + filename);
        assertTrue(filename.startsWith("BAI_TAP_A_A_A_A_A_A_A_E_E_E_E_E_O_O_O_O_O_U_U_U_U_U_Y_Y_Y_Y_Y_DO_AN"));
        assertTrue(filename.endsWith(".pdf"));
    }

    // =========================================================================
    // 2. Adversarial Testing: Directory Traversal and Path Manipulation Injection
    // =========================================================================

    @ParameterizedTest(name = "Path traversal injection: {0}")
    @ValueSource(strings = {
            "../../../../etc/passwd",
            "..\\..\\windows\\system32\\cmd.exe",
            "/var/root/secret.conf",
            "C:\\boot.ini",
            "..",
            "....",
            "../../../malicious.pdf",
            "..\\..\\webapps\\ROOT\\shell.jsp"
    })
    @DisplayName("Adversarial: Ký tự Directory Traversal (../, ..\\, /) phải bị vô hiệu hóa hoàn toàn")
    void testPathTraversalInjectionSanitization(String maliciousTitle) throws Exception {
        Quiz quiz = createQuiz(maliciousTitle, 1);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        String cd = result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION);
        String filename = extractFilenameFromHeader(cd);

        // Tuyệt đối không chứa dấu gạch chéo xuôi hoặc ngược
        assertFalse(filename.contains("/"), "Filename must not contain forward slash: " + filename);
        assertFalse(filename.contains("\\"), "Filename must not contain backslash: " + filename);
        // Không chứa dấu ngoặc kép hoặc ký tự điều khiển
        assertFalse(filename.contains("\""), "Filename must not contain unescaped quotes: " + filename);
        assertTrue(filename.endsWith(".pdf"), "Filename must end with .pdf: " + filename);
    }

    // =========================================================================
    // 3. Adversarial Testing: Header Injection & Parameter Quoting Attacks
    // =========================================================================

    @ParameterizedTest(name = "Header injection attack: {0}")
    @ValueSource(strings = {
            "test\"; filename=\"pwned.exe\"; dummy=\"",
            "quiz\r\nSet-Cookie: sessionId=hacked\r\n\r\n",
            "quiz\nInjected-Header: evil",
            "quiz; param=value; dummy=\"test",
            "quiz<script>alert(1)</script>"
    })
    @DisplayName("Adversarial: Tấn công HTTP Response Splitting & Quoting Injection trong header")
    void testHeaderInjectionAttacksNeutralized(String injectionPayload) throws Exception {
        Quiz quiz = createQuiz(injectionPayload, 1);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        String cd = result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(cd);

        // Header Content-Disposition phải có đúng 1 cặp filename="..."
        int firstIndex = cd.indexOf("filename=\"");
        int lastIndex = cd.lastIndexOf("filename=\"");
        assertEquals(firstIndex, lastIndex, "Content-Disposition should have exactly one filename attribute: " + cd);

        // Không chứa ký tự xuống dòng CRLF
        assertFalse(cd.contains("\r"), "Header must not contain carriage return");
        assertFalse(cd.contains("\n"), "Header must not contain line feed");

        // Không chứa thẻ HTML script
        assertFalse(cd.contains("<script>"), "Header must not contain script tags");
    }

    // =========================================================================
    // 4. Stress Testing: Emojis, Unicode Symbols & Math Characters
    // =========================================================================

    @Test
    @DisplayName("Adversarial: Tiêu đề toàn bộ bằng Emoji (🎯🚀📚🔥💡) phải fallback về 'de-thi.pdf'")
    void testPureEmojiTitleFallsBackToDeThi() throws Exception {
        Quiz quiz = createQuiz("🎯🚀📚🔥💡🎉🌟", 1);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        String filename = extractFilenameFromHeader(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("de-thi.pdf", filename, "Pure emoji title should fallback to de-thi.pdf");
    }

    @Test
    @DisplayName("Adversarial: Tiêu đề kết hợp Emoji và Chữ (Đề thi 🎯 Toán RAG 🚀)")
    void testMixedEmojiTitleCleansed() throws Exception {
        Quiz quiz = createQuiz("Đề thi 🎯 Toán RAG 🚀 Cực Hay", 1);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        String filename = extractFilenameFromHeader(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("De_thi_Toan_RAG_Cuc_Hay.pdf", filename);
    }

    @Test
    @DisplayName("Adversarial: Tiêu đề chứa ký tự toán học, tiền tệ và biểu tượng đặc biệt ($100% * & # @ ~ ^ [ ])")
    void testMathAndSymbolsSanitization() throws Exception {
        Quiz quiz = createQuiz("Thi Thử: 100% (Toán + Lý * Hóa = #1) ~ ^ & @ $ [V.2]", 1);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        String filename = extractFilenameFromHeader(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("Thi_Thu_100_Toan_Ly_Hoa_1_V.2.pdf", filename);
    }

    // =========================================================================
    // 5. Stress Testing: Empty, Whitespace, Punctuation-Only & Long Titles
    // =========================================================================

    @ParameterizedTest(name = "Fallback candidate: \"{0}\"")
    @ValueSource(strings = {
            "     ",
            "\t\t\n\r",
            "____",
            "----",
            "__--__",
            "!@#$%^&*()",
            "   !!!   "
    })
    @DisplayName("Adversarial: Tiêu đề rỗng/chỉ chứa khoảng trắng/ký tự đặc biệt phải an toàn fallback về de-thi.pdf")
    void testBlankAndPunctuationOnlyTitlesFallbackSafely(String emptyCandidate) throws Exception {
        Quiz quiz = createQuiz(emptyCandidate, 1);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        String filename = extractFilenameFromHeader(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertTrue(filename.equals("de-thi.pdf") || filename.matches("^[a-zA-Z0-9.-]+\\.pdf$"),
                "Filename should be de-thi.pdf or valid safe alphanumeric: " + filename);
    }

    @Test
    @DisplayName("Stress: Tiêu đề cực dài (255 ký tự tối đa DB)")
    void testMaxDbLengthTitle255Chars() throws Exception {
        String longTitle = "Đề thi kiểm tra đánh giá năng lực tư duy toán học và kỹ thuật phần mềm nâng cao dành cho sinh viên xuất sắc năm 2026 ".repeat(3);
        if (longTitle.length() > 255) {
            longTitle = longTitle.substring(0, 255);
        }
        Quiz quiz = createQuiz(longTitle, 1);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        String filename = extractFilenameFromHeader(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertFalse(filename.chars().anyMatch(c -> c > 127), "No non-ASCII allowed");
        assertTrue(filename.endsWith(".pdf"));
        assertTrue(filename.length() > 50, "Filename should reflect long title");
    }

    // =========================================================================
    // 6. Security & Authorization Matrix Testing
    // =========================================================================

    @Test
    @DisplayName("Security: Chưa đăng nhập (Session = null) trả về 401 Unauthorized")
    void testUnauthenticatedReturns401() throws Exception {
        Quiz quiz = createQuiz("Quiz 401 Test", 1);
        mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: Gói FREE bị chặn 403 Forbidden")
    void testFreeTierForbidden403() throws Exception {
        Quiz quiz = createQuiz("Quiz 403 Test", 1);
        mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(freeUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security: Gói CAMPUS được phép xuất PDF thành công 200 OK")
    void testCampusTierAllowed200() throws Exception {
        Quiz quiz = createQuiz("Quiz Campus Test", 1);
        mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(campusUser)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/pdf")));
    }

    @Test
    @DisplayName("Security: Gói SQUAD được phép xuất PDF thành công 200 OK")
    void testSquadTierAllowed200() throws Exception {
        Quiz quiz = createQuiz("Quiz Squad Test", 1);
        mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(squadUser)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/pdf")));
    }

    @Test
    @DisplayName("Security: ROLE_ADMIN (dù subscriptionTier là FREE) vẫn được phép xuất PDF 200 OK")
    void testAdminWithFreeTierAllowed200() throws Exception {
        Quiz quiz = createQuiz("Quiz Admin Test", 1);
        mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(adminUser)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/pdf")));
    }

    // =========================================================================
    // 7. Error Handling & Edge Cases
    // =========================================================================

    @Test
    @DisplayName("Error Handling: Đề thi ID âm (-99) trả về 404 Not Found")
    void testNegativeQuizIdReturns404() throws Exception {
        mockMvc.perform(get("/api/quiz/-99/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Error Handling: Đề thi ID không tồn tại (999999999) trả về 404 Not Found")
    void testNonExistentQuizIdReturns404() throws Exception {
        mockMvc.perform(get("/api/quiz/999999999/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Edge Case: Đề thi không có câu hỏi nào (0 câu hỏi) vẫn kết xuất PDF hợp lệ")
    void testQuizWithZeroQuestionsExportsValidPdf() throws Exception {
        Quiz emptyQuiz = quizRepository.save(Quiz.builder()
                .title("Đề thi Trống 0 Câu Hỏi")
                .course(testCourse)
                .difficulty("EASY")
                .totalQuestions(0)
                .timeLimitMinutes(10)
                .build());

        MvcResult result = mockMvc.perform(get("/api/quiz/" + emptyQuiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        byte[] pdfBytes = result.getResponse().getContentAsByteArray();
        assertTrue(pdfBytes.length > 500, "PDF bytes should be non-empty");
        String magic = new String(pdfBytes, 0, Math.min(5, pdfBytes.length), StandardCharsets.ISO_8859_1);
        assertEquals("%PDF-", magic);
    }

    // =========================================================================
    // 8. PDF Content & Binary Integrity
    // =========================================================================

    @Test
    @DisplayName("PDF Binary: Xác thực magic header %PDF- và đuôi EOF trong file kết xuất")
    void testPdfBinaryFormatAndStructure() throws Exception {
        Quiz quiz = createQuiz("Đề Thi Xác Thực Định Dạng Binary", 2);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .param("showAnswers", "true")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        assertTrue(bytes.length > 1000, "Generated PDF with 2 questions must exceed 1000 bytes");

        // Magic header
        String magic = new String(bytes, 0, 5, StandardCharsets.ISO_8859_1);
        assertEquals("%PDF-", magic, "PDF header must be %PDF-");

        // EOF marker in last 200 bytes
        String tail = new String(bytes, Math.max(0, bytes.length - 200), Math.min(200, bytes.length), StandardCharsets.ISO_8859_1);
        assertTrue(tail.contains("%%EOF"), "PDF must conclude with %%EOF trailer marker");
    }

    @ParameterizedTest(name = "showAnswers param: \"{0}\" -> expectSuffix: \"{1}\"")
    @CsvSource({
            "true, _kem_dap_an.pdf",
            "false, .pdf",
            ", .pdf"
    })
    @DisplayName("PDF Filename: Hậu tố _kem_dap_an phụ thuộc chính xác vào tham số showAnswers")
    void testShowAnswersSuffixBehavior(String showAnswersParam, String expectedSuffix) throws Exception {
        Quiz quiz = createQuiz("Đề Kiểm Tra Cuối Kỳ", 1);

        var req = get("/api/quiz/" + quiz.getId() + "/export-pdf").session(sessionFor(proUser));
        if (showAnswersParam != null && !showAnswersParam.isBlank()) {
            req = req.param("showAnswers", showAnswersParam);
        }

        MvcResult result = mockMvc.perform(req)
                .andExpect(status().isOk())
                .andReturn();

        String filename = extractFilenameFromHeader(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertTrue(filename.endsWith(expectedSuffix),
                "Filename " + filename + " should end with expected suffix: " + expectedSuffix);
    }

    // =========================================================================
    // 9. Additional Adversarial & Stress Scenarios (Concurrency, Windows & RTL)
    // =========================================================================

    @ParameterizedTest(name = "Windows reserved device name: \"{0}\"")
    @ValueSource(strings = {"CON", "PRN", "AUX", "NUL", "COM1", "LPT1"})
    @DisplayName("Adversarial: Tiêu đề là tên thiết bị đặc biệt của hệ điều hành Windows")
    void testWindowsReservedDeviceNames(String reservedName) throws Exception {
        Quiz quiz = createQuiz(reservedName, 1);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        String filename = extractFilenameFromHeader(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(reservedName + ".pdf", filename);
    }

    @Test
    @DisplayName("Adversarial: Tiêu đề chứa ký tự RTL Override (\\u202E) và Zero-Width Space (\\u200B)")
    void testRtlAndZeroWidthCharactersSanitization() throws Exception {
        String trickyTitle = "Đề Thi \u202Efdp.exe\u200B Trắc Nghiệm";
        Quiz quiz = createQuiz(trickyTitle, 1);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        String filename = extractFilenameFromHeader(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        // Ký tự RTL (\u202E) và Zero-Width (\u200B) không thuộc [a-zA-Z0-9.-] nên bị thay thế thành _
        assertFalse(filename.contains("\u202E"), "Must not contain RTL override");
        assertFalse(filename.contains("\u200B"), "Must not contain zero-width space");
        assertEquals("De_Thi_fdp.exe_Trac_Nghiem.pdf", filename);
    }

    @Test
    @DisplayName("Adversarial: Tiêu đề chứa dấu ngoặc kép thông minh/Unicode curly quotes (“ ” ‘ ’)")
    void testCurlyQuotesSanitization() throws Exception {
        String curlyTitle = "“Đề Thi Học Kỳ I” - ‘Chuyên Đề RAG’";
        Quiz quiz = createQuiz(curlyTitle, 1);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        String filename = extractFilenameFromHeader(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("De_Thi_Hoc_Ky_I_-_Chuyen_De_RAG.pdf", filename);
    }

    @Test
    @DisplayName("Stress: Đề thi quy mô lớn 30 câu hỏi kết xuất mượt mà không quá tải")
    void testLargeQuiz30QuestionsExport() throws Exception {
        Quiz bigQuiz = createQuiz("Đề Thi Tổng Hợp Quy Mô Lớn 30 Câu Hỏi", 30);

        MvcResult result = mockMvc.perform(get("/api/quiz/" + bigQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "true")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andReturn();

        byte[] pdfBytes = result.getResponse().getContentAsByteArray();
        assertTrue(pdfBytes.length > 5000, "30-question PDF must be reasonably large (>5KB)");
        String magic = new String(pdfBytes, 0, 5, StandardCharsets.ISO_8859_1);
        assertEquals("%PDF-", magic);
    }

    @Test
    @DisplayName("Stress & Concurrency: 10 yêu cầu xuất PDF đồng thời xử lý song song không xung đột trạng thái")
    void testConcurrentPdfExportServiceExecution() throws Exception {
        Quiz concurrentQuiz = createQuiz("Đề Thi Kiểm Thử Đa Luồng Concurrency", 3);
        List<QuizQuestion> questions = quizQuestionRepository.findAllByQuizIdOrderBySortOrderAsc(concurrentQuiz.getId());

        int threadCount = 10;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicReference<Throwable> failureRef = new java.util.concurrent.atomic.AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    byte[] bytes = quizPdfExportService.exportQuizToPdf(concurrentQuiz, questions, true);
                    if (bytes != null && bytes.length > 500) {
                        String magic = new String(bytes, 0, 5, StandardCharsets.ISO_8859_1);
                        if ("%PDF-".equals(magic)) {
                            successCount.incrementAndGet();
                        }
                    }
                } catch (Throwable t) {
                    failureRef.compareAndSet(null, t);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(30, java.util.concurrent.TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All 10 concurrent requests should finish within 30 seconds");
        assertNull(failureRef.get(), "No thread should fail: " + failureRef.get());
        assertEquals(10, successCount.get(), "All 10 concurrent requests should succeed with valid PDF bytes");
    }

    @Test
    @DisplayName("Stress: Gửi liên tiếp 5 yêu cầu MockMvc export PDF không suy giảm hiệu năng")
    void testRepeatedSequentialMockMvcPdfExports() throws Exception {
        Quiz quiz = createQuiz("Đề Thi Lặp Lại 5 Lần", 2);

        for (int i = 0; i < 5; i++) {
            MvcResult res = mockMvc.perform(get("/api/quiz/" + quiz.getId() + "/export-pdf")
                            .session(sessionFor(proUser)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("application/pdf")))
                    .andReturn();
            byte[] bytes = res.getResponse().getContentAsByteArray();
            assertNotNull(bytes);
            assertTrue(bytes.length > 500);
        }
    }
}

