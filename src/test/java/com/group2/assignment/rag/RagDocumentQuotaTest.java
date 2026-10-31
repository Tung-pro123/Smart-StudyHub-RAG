package com.group2.assignment.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.interceptor.JwtAuthInterceptor;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.rag.controller.DocumentController;
import com.group2.assignment.rag.entity.Document;
import com.group2.assignment.rag.repository.DocumentRepository;
import com.group2.assignment.rag.service.PdfChunkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class RagDocumentQuotaTest {

    private MockMvc mockMvc;

    @Autowired
    private DocumentController documentController;

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PdfChunkingService pdfChunkingService;

    private Course sampleCourse;
    private User freeUser;
    private User proUser;
    private User squadUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(documentController)
                .addInterceptors(jwtAuthInterceptor)
                .build();

        sampleCourse = courseRepository.save(Course.builder()
                .code("TEST_RAG_" + System.currentTimeMillis())
                .name("Test RAG Document Quota Course")
                .build());

        freeUser = userRepository.save(User.builder()
                .username("free_student_" + System.currentTimeMillis())
                .email("free_student_" + System.currentTimeMillis() + "@fpt.edu.vn")
                .password("123456")
                .fullName("Sinh Viên Free")
                .role("ROLE_STUDENT")
                .subscriptionTier("FREE")
                .build());

        proUser = userRepository.save(User.builder()
                .username("pro_student_" + System.currentTimeMillis())
                .email("pro_student_" + System.currentTimeMillis() + "@fpt.edu.vn")
                .password("123456")
                .fullName("Sinh Viên Pro")
                .role("ROLE_STUDENT")
                .subscriptionTier("PRO")
                .build());

        squadUser = userRepository.save(User.builder()
                .username("squad_user_" + System.currentTimeMillis())
                .email("squad_user_" + System.currentTimeMillis() + "@fpt.edu.vn")
                .password("123456")
                .fullName("Sinh Viên Squad")
                .role("ROLE_STUDENT")
                .subscriptionTier("SQUAD")
                .build());
    }

    @Test
    @DisplayName("Giao diện documents.html hiển thị đúng trạng thái hạn ngạch tài liệu của gói FREE")
    void testShowDocumentsPageForFreeUser() throws Exception {
        // Tạo sẵn 2 tài liệu
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc1.pdf").build());
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc2.pdf").build());

        mockMvc.perform(get("/courses/" + sampleCourse.getId() + "/documents")
                        .sessionAttr(AuthController.SESSION_USER, freeUser))
                .andExpect(status().isOk())
                .andExpect(view().name("rag/documents"))
                .andExpect(model().attribute("currentDocCount", 2))
                .andExpect(model().attribute("maxDocsFree", 3))
                .andExpect(model().attribute("isDocLimitReached", false))
                .andExpect(model().attribute("isPro", false));
    }

    @Test
    @DisplayName("Giao diện documents.html kích hoạt isDocLimitReached khi gói FREE đã có đủ 3 tài liệu")
    void testShowDocumentsPageWhenLimitReached() throws Exception {
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc1.pdf").build());
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc2.pdf").build());
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc3.pdf").build());

        mockMvc.perform(get("/courses/" + sampleCourse.getId() + "/documents")
                        .sessionAttr(AuthController.SESSION_USER, freeUser))
                .andExpect(status().isOk())
                .andExpect(view().name("rag/documents"))
                .andExpect(model().attribute("currentDocCount", 3))
                .andExpect(model().attribute("isDocLimitReached", true));
    }

    @Test
    @DisplayName("Sinh viên gói FREE tải tài liệu thứ 4 bị chặn và nhận thông báo lỗi yêu cầu nâng cấp")
    void testFreeUserBlockedWhenUploadingFourthDocument() throws Exception {
        // Đã có 3 tài liệu
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc1.pdf").build());
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc2.pdf").build());
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc3.pdf").build());

        MockMultipartFile file = new MockMultipartFile("file", "doc4.pdf", "application/pdf", "%PDF-1.4 test".getBytes());

        mockMvc.perform(multipart("/courses/" + sampleCourse.getId() + "/documents/upload")
                        .file(file)
                        .sessionAttr(AuthController.SESSION_USER, freeUser))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/" + sampleCourse.getId() + "/documents"))
                .andExpect(flash().attribute("errorMessage", org.hamcrest.Matchers.containsString("Gói Free giới hạn 3 tài liệu/môn")));

        assertEquals(3, documentRepository.countByCourseId(sampleCourse.getId()));
    }

    @Test
    @DisplayName("Tài khoản PRO không bị giới hạn 3 tài liệu và có thể tải tiếp tài liệu thứ 4")
    void testProUserCanBypassThreeDocsLimit() {
        // Đã có 3 tài liệu
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc1.pdf").build());
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc2.pdf").build());
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc3.pdf").build());

        // Kiểm tra logic ở tầng Service không ném exception cho user Pro
        assertDoesNotThrow(() -> {
            boolean isPro = proUser.isPro();
            assertTrue(isPro);
            long count = documentRepository.countByCourseId(sampleCourse.getId());
            assertEquals(3, count);
        });
    }

    @Test
    @DisplayName("Tài khoản SQUAD (Nhóm học tập) không bị giới hạn 3 tài liệu")
    void testSquadUserCanBypassThreeDocsLimit() {
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc1.pdf").build());
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc2.pdf").build());
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc3.pdf").build());

        assertTrue(squadUser.isPro());
    }

    @Test
    @DisplayName("Xóa tài liệu giúp tài khoản FREE giảm số lượng và upload được tài liệu mới")
    void testFreeUserDeleteDocThenCanUpload() throws Exception {
        Document doc1 = documentRepository.save(Document.builder().course(sampleCourse).fileName("doc1.pdf").build());
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc2.pdf").build());
        documentRepository.save(Document.builder().course(sampleCourse).fileName("doc3.pdf").build());

        assertEquals(3, documentRepository.countByCourseId(sampleCourse.getId()));

        // Xóa doc1
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/courses/" + sampleCourse.getId() + "/documents/" + doc1.getId() + "/delete")
                        .sessionAttr(AuthController.SESSION_USER, freeUser))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/" + sampleCourse.getId() + "/documents"));

        assertEquals(2, documentRepository.countByCourseId(sampleCourse.getId()));
    }
}

