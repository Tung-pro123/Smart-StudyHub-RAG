package com.group2.assignment.rag;

import com.group2.assignment.core.entity.Chunk;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.ChunkRepository;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.rag.entity.Document;
import com.group2.assignment.rag.service.PdfChunkingService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PdfChunkingServiceTest {

    @Autowired
    private PdfChunkingService pdfChunkingService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    private Course sampleCourse;

    @BeforeEach
    void setUp() {
        sampleCourse = courseRepository.findByCodeIgnoreCase("HSF302")
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .code("HSF302")
                        .name("JPA and Spring Framework")
                        .semester("Fall 2026")
                        .credits(3)
                        .build()));
    }

    @Test
    @DisplayName("Cắt văn bản dài thành các đoạn Chunks thông minh không làm mất câu")
    void testSplitTextIntoChunks() {
        // Tạo đoạn văn bản dài gồm nhiều câu có độ dài > 500 từ
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 35; i++) {
            sb.append("Câu thứ ").append(i).append(": Kiến trúc Spring Framework cung cấp cơ chế Inversion of Control và Dependency Injection toàn diện cho ứng dụng Java doanh nghiệp. ");
        }
        String text = sb.toString();
        int totalWords = pdfChunkingService.countWords(text);
        assertTrue(totalWords > 500, "Tổng số từ kiểm thử phải > 500 từ");

        List<String> chunks = pdfChunkingService.splitTextIntoChunks(text, 100, 300);

        assertFalse(chunks.isEmpty(), "Danh sách chunks không được rỗng");
        assertTrue(chunks.size() >= 2, "Văn bản phải được chia thành từ 2 chunks trở lên");

        // Kiểm tra từng chunk không bị rỗng và câu không bị cắt ngang lửng lơ
        for (String chunk : chunks) {
            assertTrue(chunk.endsWith("."), "Mỗi chunk nên kết thúc bằng dấu chấm kết thúc câu");
            int words = pdfChunkingService.countWords(chunk);
            assertTrue(words <= 350, "Số từ trong mỗi chunk không được vượt quá ngưỡng tối đa cho phép");
        }
    }

    @Test
    @DisplayName("Từ chối file không phải định dạng PDF (.pdf)")
    void testValidateFileNonPdfRejected() {
        MockMultipartFile docxFile = new MockMultipartFile(
                "file", "curriculum.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "Nội dung file word".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                pdfChunkingService.validateFile(docxFile)
        );
        assertTrue(ex.getMessage().contains("PDF"), "Phải có thông báo lỗi yêu cầu định dạng PDF");
    }

    @Test
    @DisplayName("Từ chối file rỗng 0 bytes")
    void testValidateEmptyFileRejected() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                pdfChunkingService.validateFile(emptyFile)
        );
        assertTrue(ex.getMessage().contains("rỗng") || ex.getMessage().contains("hợp lệ"));
    }

    @Test
    @DisplayName("Từ chối file vượt quá 25MB")
    void testValidateOversizedFileRejected() {
        // Mock file có dung lượng lớn hơn 25MB
        byte[] largeBytes = new byte[100];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "giant.pdf", "application/pdf", largeBytes
        ) {
            @Override
            public long getSize() {
                return (26L * 1024 * 1024); // 26 MB
            }
        };

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                pdfChunkingService.validateFile(largeFile)
        );
        assertTrue(ex.getMessage().contains("25MB"), "Phải có thông báo lỗi vượt quá 25MB");
    }

    @Test
    @DisplayName("Xử lý file PDF chuẩn: trích xuất text từng trang, cắt chunks và cập nhật INDEXED")
    void testProcessPdfSuccess() throws Exception {
        // Tạo file PDF 2 trang mẫu
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page1 = new PDPage();
            doc.addPage(page1);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.showText("Page 1: Spring Boot Auto Configuration reduces boilerplate setup.");
                cs.endText();
            }

            PDPage page2 = new PDPage();
            doc.addPage(page2);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.showText("Page 2: Spring Data JPA simplifies database access layer.");
                cs.endText();
            }

            doc.save(baos);
        }

        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "spring_basics.pdf", "application/pdf", baos.toByteArray()
        );

        Document processedDoc = pdfChunkingService.processPdf(sampleCourse.getId(), pdfFile);

        assertNotNull(processedDoc.getId());
        assertEquals("INDEXED", processedDoc.getStatus());
        assertEquals(2, processedDoc.getTotalPages());
        assertTrue(processedDoc.getTotalChunks() >= 2);

        List<Chunk> chunks = chunkRepository.findAllByDocumentIdOrderByChunkIndexAsc(processedDoc.getId());
        assertEquals(processedDoc.getTotalChunks(), chunks.size());

        // Kiểm tra pageNumber của từng chunk tương ứng
        assertEquals(1, chunks.get(0).getPageNumber());
        assertTrue(chunks.get(0).getContent().contains("Spring Boot Auto Configuration"));

        assertEquals(2, chunks.get(1).getPageNumber());
        assertTrue(chunks.get(1).getContent().contains("Spring Data JPA"));

        // Dọn dẹp file vật lý sau khi test
        if (processedDoc.getFilePath() != null) {
            try {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(processedDoc.getFilePath()));
            } catch (Exception ignored) {}
        }
    }
}

