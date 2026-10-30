package com.group2.assignment.rag;

import com.group2.assignment.core.entity.Chunk;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.ChunkRepository;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.rag.dto.ChatResponseDto;
import com.group2.assignment.rag.dto.CitationDto;
import com.group2.assignment.rag.entity.Document;
import com.group2.assignment.rag.repository.DocumentRepository;
import com.group2.assignment.rag.service.RagEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RagEngineServiceTest {

    @Autowired
    private RagEngineService ragEngineService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    private Course sampleCourse;
    private Document sampleDoc;

    @BeforeEach
    void setUp() {
        sampleCourse = courseRepository.findByCodeIgnoreCase("HSF302")
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .code("HSF302")
                        .name("JPA and Spring Framework")
                        .build()));

        sampleDoc = documentRepository.save(Document.builder()
                .course(sampleCourse)
                .fileName("Spring_In_Action.pdf")
                .status("INDEXED")
                .build());

        // Tạo 3 đoạn chunks kiến thức khác nhau
        chunkRepository.save(Chunk.builder()
                .course(sampleCourse)
                .document(sampleDoc)
                .chunkIndex(0)
                .pageNumber(5)
                .content("Inversion of Control và Dependency Injection là hai nguyên lý cốt lõi của Spring Framework để quản lý Bean lifecycle.")
                .wordCount(20)
                .build());

        chunkRepository.save(Chunk.builder()
                .course(sampleCourse)
                .document(sampleDoc)
                .chunkIndex(1)
                .pageNumber(18)
                .content("Spring Data JPA đơn giản hóa việc tương tác cơ sở dữ liệu với JpaRepository tự động sinh câu lệnh HQL và SQL.")
                .wordCount(22)
                .build());

        chunkRepository.save(Chunk.builder()
                .course(sampleCourse)
                .document(sampleDoc)
                .chunkIndex(2)
                .pageNumber(35)
                .content("Deploy ứng dụng với Docker container hóa và Dockerfile tối ưu kích thước image đa tầng.")
                .wordCount(15)
                .build());
    }

    @Test
    @DisplayName("Thuật toán xếp hạng ngữ nghĩa chọn đúng top Chunks liên quan đến câu hỏi")
    void testFindRelevantChunks() {
        // Query liên quan đến Spring Data JPA
        List<Chunk> results = ragEngineService.findRelevantChunks(sampleCourse.getId(), "Spring Data JPA cơ sở dữ liệu", 2);

        assertFalse(results.isEmpty());
        // Chunk về Spring Data JPA (chunkIndex = 1) phải đứng đầu danh sách vì điểm liên quan cao nhất
        assertEquals(1, results.get(0).getChunkIndex());
        assertTrue(results.get(0).getContent().contains("Spring Data JPA"));
    }

    @Test
    @DisplayName("Tính điểm khớp chính xác toàn bộ cụm từ khóa cao hơn khớp từng phần")
    void testCalculateRelevanceScore() {
        Chunk chunk = Chunk.builder()
                .content("Spring Data JPA đơn giản hóa việc thao tác dữ liệu")
                .build();

        Set<String> keywords = Set.of("spring", "data", "jpa");
        double exactScore = ragEngineService.calculateRelevanceScore(chunk, "spring data jpa", keywords);
        double partialScore = ragEngineService.calculateRelevanceScore(chunk, "khác biệt", keywords);

        assertTrue(exactScore > partialScore, "Khớp chính xác và khớp từ khóa phải có điểm cao hơn đáng kể");
    }

    @Test
    @DisplayName("Trích xuất từ khóa ngữ nghĩa và loại bỏ stop words Tiếng Việt và Tiếng Anh")
    void testExtractKeywords() {
        String query = "Hãy cho biết giải thích về Dependency Injection là gì trong Spring?";
        Set<String> keywords = ragEngineService.extractKeywords(query);

        assertFalse(keywords.contains("là"));
        assertFalse(keywords.contains("trong"));
        assertFalse(keywords.contains("về"));
        assertTrue(keywords.contains("dependency"));
        assertTrue(keywords.contains("injection"));
        assertTrue(keywords.contains("spring"));
    }

    @Test
    @DisplayName("Dựng System Prompt tiêm ngữ cảnh RAG Chunks chuẩn xác")
    void testBuildSystemPrompt() {
        List<Chunk> chunks = chunkRepository.findAllByCourseId(sampleCourse.getId());
        String question = "Dependency Injection là gì?";

        String prompt = ragEngineService.buildSystemPrompt(chunks, question);

        assertNotNull(prompt);
        assertTrue(prompt.contains("Bạn là Trợ lý Giảng viên AI chuyên nghiệp"));
        assertTrue(prompt.contains("Spring_In_Action.pdf"));
        assertTrue(prompt.contains("Trang: 5"));
        assertTrue(prompt.contains(question));
        assertTrue(prompt.contains("Quy tắc:"));
    }

    @Test
    @DisplayName("Dựng danh sách Citations đầy đủ thông tin trang và tên file")
    void testBuildCitations() {
        List<Chunk> chunks = chunkRepository.findAllByCourseId(sampleCourse.getId());
        List<CitationDto> citations = ragEngineService.buildCitations(chunks);

        assertEquals(chunks.size(), citations.size());
        assertEquals("Spring_In_Action.pdf", citations.get(0).getDocumentFileName());
        assertEquals(5, citations.get(0).getPageNumber());
        assertNotNull(citations.get(0).getSnippet());
    }

    @Test
    @DisplayName("RAG Chatbot kích hoạt Fallback hướng dẫn người dùng khi chưa cài API Key")
    void testChatFallbackWhenNoApiKey() {
        MockHttpSession emptySession = new MockHttpSession();
        emptySession.setAttribute("GEMINI_API_KEY", "ignored-personal-key");

        ChatResponseDto response = ragEngineService.chatWithCourse(
                sampleCourse.getId(),
                null,
                "Làm thế nào để cấu hình Spring Boot?",
                emptySession
        );

        assertNotNull(response);
        assertNotNull(response.getSessionId());
        assertFalse(response.isSuccess());
        assertTrue(response.getAnswer().contains("Chưa cấu hình Google Gemini API Key"));
        assertTrue(response.getAnswer().contains("quản trị viên"));
        assertFalse(response.getCitations().isEmpty());
    }
}
