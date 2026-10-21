package com.group2.assignment.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.interceptor.JwtAuthInterceptor;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.rag.controller.ChatController;
import com.group2.assignment.rag.controller.DocumentController;
import com.group2.assignment.rag.dto.ChatRequestDto;
import com.group2.assignment.rag.entity.Document;
import com.group2.assignment.rag.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class DocumentControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private DocumentController documentController;

    @Autowired
    private ChatController chatController;

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Course sampleCourse;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(documentController, chatController)
                .addInterceptors(jwtAuthInterceptor)
                .build();

        sampleCourse = courseRepository.findByCodeIgnoreCase("HSF302")
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .code("HSF302")
                        .name("JPA and Spring Framework")
                        .build()));

        mockUser = User.builder()
                .id(999L)
                .username("binh_tester")
                .fullName("Nguyễn Văn Bình")
                .role("STUDENT")
                .build();
    }

    @Test
    @DisplayName("GET /courses/{courseId}/documents trả về giao diện rag/documents kèm Course và danh sách tài liệu")
    void testShowDocumentsPageSuccess() throws Exception {
        mockMvc.perform(get("/courses/" + sampleCourse.getId() + "/documents")
                        .sessionAttr(AuthController.SESSION_USER, mockUser))
                .andExpect(status().isOk())
                .andExpect(view().name("rag/documents"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("documents"))
                .andExpect(model().attributeExists("chunks"));
    }

    @Test
    @DisplayName("GET /courses/{courseId}/documents chưa đăng nhập bị chuyển hướng về /login")
    void testShowDocumentsPageUnauthorized() throws Exception {
        mockMvc.perform(get("/courses/" + sampleCourse.getId() + "/documents"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=unauthorized"));
    }

    @Test
    @DisplayName("POST /courses/{courseId}/documents/{docId}/delete xóa tài liệu và chuyển hướng kèm flash message")
    void testDeleteDocumentSuccess() throws Exception {
        Document doc = documentRepository.save(Document.builder()
                .course(sampleCourse)
                .fileName("test_delete.pdf")
                .status("INDEXED")
                .build());

        mockMvc.perform(post("/courses/" + sampleCourse.getId() + "/documents/" + doc.getId() + "/delete")
                        .sessionAttr(AuthController.SESSION_USER, mockUser))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/" + sampleCourse.getId() + "/documents"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("GET /courses/{courseId}/chat hiển thị giao diện RAG Chatbot")
    void testShowChatPageSuccess() throws Exception {
        mockMvc.perform(get("/courses/" + sampleCourse.getId() + "/chat")
                        .sessionAttr(AuthController.SESSION_USER, mockUser))
                .andExpect(status().isOk())
                .andExpect(view().name("rag/chat"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("sessions"))
                .andExpect(model().attributeExists("hasApiKey"));
    }

    @Test
    @DisplayName("POST /courses/{courseId}/chat/send dạng JSON API trả về phản hồi ChatResponseDto")
    void testSendChatMessageJsonApi() throws Exception {
        ChatRequestDto requestDto = ChatRequestDto.builder()
                .question("Dependency Injection hoạt động như thế nào?")
                .build();

        mockMvc.perform(post("/courses/" + sampleCourse.getId() + "/chat/send")
                        .sessionAttr(AuthController.SESSION_USER, mockUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").exists())
                .andExpect(jsonPath("$.sessionId").exists());
    }
}
