package com.group2.assignment.rag.service;

import com.group2.assignment.core.entity.Chunk;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.ChunkRepository;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.rag.dto.ChunkDto;
import com.group2.assignment.rag.entity.Document;
import com.group2.assignment.rag.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfChunkingService {

    public static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25 MB
    public static final int DEFAULT_MIN_WORDS = 300;
    public static final int DEFAULT_MAX_WORDS = 500;

    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final CourseRepository courseRepository;

    /**
     * Tiếp nhận file PDF, validate, lưu trữ, trích xuất text từng trang và cắt chunks
     */
    public Document processPdf(Long courseId, MultipartFile file) {
        validateFile(file);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học với ID: " + courseId));

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf";
        
        // 1. Tạo thư mục vật lý ./uploads/courses/{courseId}/
        Path uploadDirPath = Paths.get("uploads", "courses", String.valueOf(courseId));
        Path destinationPath;
        try {
            Files.createDirectories(uploadDirPath);
            String safeFileName = System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            destinationPath = uploadDirPath.resolve(safeFileName);
            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Lỗi khi lưu file PDF lên đĩa: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lưu file lên máy chủ: " + e.getMessage(), e);
        }

        // 2. Tạo bản ghi Document trạng thái CHUNKING
        Document document = Document.builder()
                .course(course)
                .fileName(originalFilename)
                .filePath(destinationPath.toAbsolutePath().toString())
                .fileType("application/pdf")
                .fileSize(file.getSize())
                .status("CHUNKING")
                .totalPages(0)
                .totalChunks(0)
                .build();
        document = documentRepository.save(document);

        // 3. Trích xuất text và cắt chunk với Apache PDFBox 3.0.3
        try {
            byte[] fileBytes = file.getBytes();
            List<Chunk> chunksToSave = new ArrayList<>();
            int totalPages;

            try (PDDocument pdDocument = Loader.loadPDF(fileBytes)) {
                totalPages = pdDocument.getNumberOfPages();
                if (totalPages == 0) {
                    throw new IllegalArgumentException("File PDF không chứa trang nào!");
                }

                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);

                int globalChunkIndex = 0;
                for (int page = 1; page <= totalPages; page++) {
                    stripper.setStartPage(page);
                    stripper.setEndPage(page);
                    String pageText = stripper.getText(pdDocument);

                    if (pageText == null || pageText.isBlank()) {
                        continue;
                    }

                    List<String> pageChunks = splitTextIntoChunks(pageText, DEFAULT_MIN_WORDS, DEFAULT_MAX_WORDS);
                    for (String chunkText : pageChunks) {
                        int wordCount = countWords(chunkText);
                        int tokenCount = estimateTokens(wordCount);

                        Chunk chunk = Chunk.builder()
                                .document(document)
                                .course(course)
                                .chunkIndex(globalChunkIndex++)
                                .content(chunkText)
                                .pageNumber(page)
                                .wordCount(wordCount)
                                .tokenCount(tokenCount)
                                .hasEmbedding(false)
                                .build();
                        chunksToSave.add(chunk);
                    }
                }
            }

            if (chunksToSave.isEmpty()) {
                throw new IllegalArgumentException("Không thể trích xuất văn bản từ PDF (file có thể là ảnh quét hoặc tài liệu rỗng)!");
            }

            // Lưu toàn bộ Chunks
            chunkRepository.saveAll(chunksToSave);

            // Cập nhật Document thành công
            document.setStatus("INDEXED");
            document.setTotalPages(totalPages);
            document.setTotalChunks(chunksToSave.size());
            document.setErrorMessage(null);
            return documentRepository.save(document);

        } catch (Exception e) {
            log.error("Lỗi khi xử lý chunking tài liệu PDF: {}", e.getMessage(), e);
            document.setStatus("ERROR");
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);
            throw new RuntimeException("Lỗi xử lý PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Validate định dạng PDF và dung lượng
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn một file PDF hợp lệ để tải lên!");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Dung lượng file vượt quá giới hạn cho phép (tối đa 25MB)!");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Chỉ chấp nhận tài liệu định dạng PDF (.pdf)!");
        }
    }

    /**
     * Thuật toán cắt chuỗi văn bản thông minh từ 300 - 500 từ theo ranh giới câu/đoạn
     */
    public List<String> splitTextIntoChunks(String text, int minWords, int maxWords) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        String cleanedText = text.trim();
        int totalWords = countWords(cleanedText);
        if (totalWords <= maxWords) {
            chunks.add(cleanedText);
            return chunks;
        }

        // Tách câu theo dấu ngắt câu hoặc xuống dòng
        String[] sentences = cleanedText.split("(?<=[.!?\\n])\\s+");
        StringBuilder currentChunk = new StringBuilder();
        int currentWordCount = 0;

        for (String sentence : sentences) {
            String trimmedSentence = sentence.trim();
            if (trimmedSentence.isEmpty()) {
                continue;
            }

            int sentenceWords = countWords(trimmedSentence);

            // Nếu câu hiện tại quá dài vượt maxWords, tự chia nhỏ câu
            if (sentenceWords > maxWords) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                    currentWordCount = 0;
                }
                chunks.addAll(splitLongSentence(trimmedSentence, maxWords));
                continue;
            }

            if (currentWordCount + sentenceWords > maxWords && currentWordCount >= minWords) {
                chunks.add(currentChunk.toString().trim());
                currentChunk.setLength(0);
                currentWordCount = 0;
            }

            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
            }
            currentChunk.append(trimmedSentence);
            currentWordCount += sentenceWords;
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private List<String> splitLongSentence(String sentence, int maxWords) {
        List<String> parts = new ArrayList<>();
        String[] words = sentence.split("\\s+");
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (String word : words) {
            if (count >= maxWords) {
                parts.add(sb.toString().trim());
                sb.setLength(0);
                count = 0;
            }
            if (sb.length() > 0) sb.append(" ");
            sb.append(word);
            count++;
        }
        if (sb.length() > 0) {
            parts.add(sb.toString().trim());
        }
        return parts;
    }

    public int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }

    public int estimateTokens(int wordCount) {
        return (int) Math.round(wordCount * 1.3);
    }

    /**
     * Xóa tài liệu và các chunk liên quan
     */
    @Transactional
    public void deleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu ID: " + documentId));

        // Xóa file vật lý
        if (document.getFilePath() != null) {
            try {
                Path path = Paths.get(document.getFilePath());
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.warn("Không thể xóa file vật lý {}: {}", document.getFilePath(), e.getMessage());
            }
        }

        chunkRepository.deleteAllByDocumentId(documentId);
        documentRepository.delete(document);
    }

    public List<Document> getDocumentsByCourse(Long courseId) {
        return documentRepository.findAllByCourseIdOrderByUploadedAtDesc(courseId);
    }

    public List<Chunk> getChunksByCourse(Long courseId) {
        return chunkRepository.findAllByCourseId(courseId);
    }

    public List<ChunkDto> getChunkDtosByCourse(Long courseId) {
        return chunkRepository.findAllByCourseId(courseId).stream()
                .map(chunk -> ChunkDto.builder()
                        .id(chunk.getId())
                        .chunkIndex(chunk.getChunkIndex())
                        .content(chunk.getContent())
                        .pageNumber(chunk.getPageNumber())
                        .wordCount(chunk.getWordCount())
                        .tokenCount(chunk.getTokenCount())
                        .documentId(chunk.getDocument() != null ? chunk.getDocument().getId() : null)
                        .documentFileName(chunk.getDocument() != null ? chunk.getDocument().getFileName() : "")
                        .build())
                .toList();
    }
}
