package com.group2.assignment.rag.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.core.entity.Chunk;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.ChunkRepository;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.rag.dto.ChatResponseDto;
import com.group2.assignment.rag.dto.CitationDto;
import com.group2.assignment.rag.entity.ChatMessage;
import com.group2.assignment.rag.entity.ChatSession;
import com.group2.assignment.rag.repository.ChatMessageRepository;
import com.group2.assignment.rag.repository.ChatSessionRepository;
import com.group2.assignment.settings.SettingsConstants;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagEngineService {

    private final ChunkRepository chunkRepository;
    private final CourseRepository courseRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    /**
     * Tìm kiếm top K Chunks liên quan nhất với câu hỏi dựa trên thuật toán Term Overlap & TF-IDF
     */
    public List<Chunk> findRelevantChunks(Long courseId, String query, int topK) {
        List<Chunk> allChunks = chunkRepository.findAllByCourseId(courseId);
        if (allChunks.isEmpty()) {
            return Collections.emptyList();
        }
        if (query == null || query.isBlank()) {
            return allChunks.stream().limit(topK).toList();
        }

        String lowerQuery = query.toLowerCase().trim();
        Set<String> queryKeywords = extractKeywords(lowerQuery);

        List<ScoredChunk> scoredChunks = new ArrayList<>();
        for (Chunk chunk : allChunks) {
            double score = calculateRelevanceScore(chunk, lowerQuery, queryKeywords);
            scoredChunks.add(new ScoredChunk(chunk, score));
        }

        // Sắp xếp giảm dần theo điểm độ liên quan
        scoredChunks.sort((a, b) -> Double.compare(b.score, a.score));

        // Lấy topK các chunk có điểm số liên quan
        List<Chunk> matched = scoredChunks.stream()
                .filter(sc -> sc.score > 0.0)
                .limit(topK)
                .map(sc -> sc.chunk)
                .collect(Collectors.toList());

        // Nếu không có chunk nào khớp từ khóa, lấy topK chunk đầu tiên của môn học
        if (matched.isEmpty()) {
            return allChunks.stream().limit(topK).toList();
        }

        return matched;
    }

    /**
     * Thuật toán tính điểm liên quan giữa câu hỏi và đoạn Chunk
     */
    public double calculateRelevanceScore(Chunk chunk, String fullQuery, Set<String> queryKeywords) {
        if (chunk == null || chunk.getContent() == null) {
            return 0.0;
        }

        String content = chunk.getContent().toLowerCase();
        double score = 0.0;

        // 1. Khớp chính xác toàn bộ cụm câu hỏi (+10 điểm)
        if (content.contains(fullQuery)) {
            score += 10.0;
        }

        // 2. Khớp từng từ khóa ngữ nghĩa
        int matchedKeywords = 0;
        for (String keyword : queryKeywords) {
            if (content.contains(keyword)) {
                matchedKeywords++;
                int occurrences = countOccurrences(content, keyword);
                score += 1.0 + Math.log(1.0 + occurrences);
            }
        }

        // 3. Tỷ lệ phủ sóng từ khóa trong chunk
        if (!queryKeywords.isEmpty()) {
            double coverage = (double) matchedKeywords / queryKeywords.size();
            score += coverage * 3.0;
        }

        return score;
    }

    /**
     * Trích xuất các từ khóa ngữ nghĩa (loại bỏ stop words)
     */
    public Set<String> extractKeywords(String query) {
        Set<String> stopWords = Set.of(
                "và", "là", "của", "cho", "trong", "với", "được", "các", "có", "những",
                "này", "để", "thì", "về", "như", "một", "ở", "gì", "nào", "sao", "thế",
                "ai", "đâu", "hãy", "biết", "giải", "thích",
                "the", "a", "an", "is", "are", "in", "on", "at", "for", "of", "and", "or", "to", "with", "what", "how"
        );

        String clean = query.replaceAll("[^a-zA-Z0-9a-zA-ZÀ-ỹ\\s]", " ").toLowerCase();
        String[] tokens = clean.split("\\s+");
        Set<String> keywords = new HashSet<>();
        for (String token : tokens) {
            if (token.length() >= 2 && !stopWords.contains(token)) {
                keywords.add(token);
            }
        }
        return keywords;
    }

    private int countOccurrences(String text, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    /**
     * Đóng gói System Prompt tiêm ngữ cảnh RAG Chunks
     */
    public String buildSystemPrompt(List<Chunk> chunks, String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là Trợ lý Giảng viên AI chuyên nghiệp của môn học. Dưới đây là các trích đoạn từ giáo trình chính thức của môn học:\n\n");

        if (chunks == null || chunks.isEmpty()) {
            sb.append("[Hiện tại môn học chưa có tài liệu giáo trình nào được tải lên]\n\n");
        } else {
            for (int i = 0; i < chunks.size(); i++) {
                Chunk c = chunks.get(i);
                String docName = (c.getDocument() != null && c.getDocument().getFileName() != null)
                        ? c.getDocument().getFileName() : "Tài liệu học tập";
                sb.append(String.format("--- TRÍCH ĐOẠN #%d (Tài liệu: %s, Trang: %d) ---\n", i + 1, docName, c.getPageNumber()));
                sb.append(c.getContent().trim()).append("\n\n");
            }
        }

        sb.append("HÃY DỰA VÀO CÁC TRÍCH ĐOẠN TRÊN ĐỂ TRẢ LỜI CÂU HỎI: ").append(question).append("\n\n");
        sb.append("Quy tắc:\n");
        sb.append("1. Trả lời chi tiết, chính xác, định dạng Markdown đẹp mắt (dùng bold, code block, gạch đầu dòng).\n");
        sb.append("2. Cuối câu trả lời, hãy liệt kê rõ ràng mục 'Trích dẫn tài liệu' ghi rõ trang và tên file.\n");
        sb.append("3. Nếu tài liệu không chứa thông tin liên quan, hãy thông báo lịch sự rằng giáo trình hiện tại không đề cập đến nội dung này.\n");

        return sb.toString();
    }

    /**
     * Dựng danh sách Citation DTO từ các Chunks
     */
    public List<CitationDto> buildCitations(List<Chunk> chunks) {
        List<CitationDto> citations = new ArrayList<>();
        if (chunks == null) return citations;

        for (Chunk chunk : chunks) {
            String snippet = chunk.getContent();
            if (snippet.length() > 220) {
                snippet = snippet.substring(0, 220) + "...";
            }
            citations.add(CitationDto.builder()
                    .chunkIndex(chunk.getChunkIndex())
                    .pageNumber(chunk.getPageNumber())
                    .documentFileName(chunk.getDocument() != null ? chunk.getDocument().getFileName() : "Tài liệu môn học")
                    .snippet(snippet)
                    .build());
        }
        return citations;
    }

    /**
     * Gửi yêu cầu RAG Chatbot giáo trình
     */
    @Transactional
    public ChatResponseDto chatWithCourse(Long courseId, Long sessionId, String question, HttpSession session) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học với ID: " + courseId));

        // 1. Quản lý ChatSession
        ChatSession chatSession;
        if (sessionId != null) {
            chatSession = chatSessionRepository.findById(sessionId).orElse(null);
        } else {
            chatSession = null;
        }

        if (chatSession == null) {
            chatSession = ChatSession.builder()
                    .course(course)
                    .title(generateTitle(question))
                    .build();
            chatSession = chatSessionRepository.save(chatSession);
        }

        // 2. Lấy API Key từ Session hoặc Biến môi trường
        String apiKey = null;
        if (session != null) {
            apiKey = (String) session.getAttribute(SettingsConstants.SESSION_API_KEY);
        }
        if (apiKey == null || apiKey.trim().isBlank()) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }

        // 3. Tìm kiếm Chunks liên quan
        List<Chunk> relevantChunks = findRelevantChunks(courseId, question, 4);
        List<CitationDto> citations = buildCitations(relevantChunks);
        String ragContextText = buildSystemPrompt(relevantChunks, question);

        // 4. Nếu chưa có API Key -> Fallback hướng dẫn người dùng
        if (apiKey == null || apiKey.trim().isBlank()) {
            String fallbackAnswer = "⚠️ **Chưa cấu hình Google Gemini API Key!**\n\n" +
                    "Bạn vui lòng truy cập trang [Cài đặt API Key](/settings) để thiết lập Gemini API Key trước khi sử dụng tính năng Trợ lý AI giáo trình.\n\n" +
                    "> **Gợi ý trích xuất từ tài liệu:** Tìm thấy " + relevantChunks.size() + " đoạn văn bản có khả năng liên quan đến câu hỏi của bạn.";

            // Lưu tin nhắn USER và thông báo cảnh báo
            saveMessages(chatSession, question, fallbackAnswer, ragContextText);

            return ChatResponseDto.builder()
                    .answer(fallbackAnswer)
                    .sessionId(chatSession.getId())
                    .sessionTitle(chatSession.getTitle())
                    .citations(citations)
                    .createdAt(LocalDateTime.now())
                    .success(false)
                    .errorMessage("Chưa cấu hình Google Gemini API Key")
                    .build();
        }

        // 5. Gọi Gemini API 1.5 Flash bằng Spring 6 RestClient
        try {
            String aiAnswer = callGeminiApi(apiKey.trim(), ragContextText);

            // Lưu tin nhắn của Sinh viên và AI
            saveMessages(chatSession, question, aiAnswer, ragContextText);

            return ChatResponseDto.builder()
                    .answer(aiAnswer)
                    .sessionId(chatSession.getId())
                    .sessionTitle(chatSession.getTitle())
                    .citations(citations)
                    .createdAt(LocalDateTime.now())
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi gọi Gemini API cho RAG Chat: {}", e.getMessage(), e);
            String errorAnswer = "❌ **Đã xảy ra lỗi khi trao đổi với AI Gemini:**\n\n" +
                    "Chi tiết: " + e.getMessage() + "\n\n" +
                    "Vui lòng kiểm tra lại kết nối mạng hoặc thử lại với API Key hợp lệ tại trang `/settings`.";

            saveMessages(chatSession, question, errorAnswer, ragContextText);

            return ChatResponseDto.builder()
                    .answer(errorAnswer)
                    .sessionId(chatSession.getId())
                    .sessionTitle(chatSession.getTitle())
                    .citations(citations)
                    .createdAt(LocalDateTime.now())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private String callGeminiApi(String apiKey, String prompt) throws Exception {
        RestClient restClient = RestClient.builder().build();

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        String url = GEMINI_API_URL + apiKey;

        String responseJson = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && !candidates.isEmpty()) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray() && !parts.isEmpty()) {
                return parts.get(0).path("text").asText();
            }
        }
        throw new RuntimeException("Phản hồi từ Gemini không chứa nội dung hợp lệ!");
    }

    private void saveMessages(ChatSession chatSession, String question, String answer, String ragContext) {
        ChatMessage userMsg = ChatMessage.builder()
                .session(chatSession)
                .sender("USER")
                .content(question)
                .tokenCount((int) Math.round(question.split("\\s+").length * 1.3))
                .build();
        chatMessageRepository.save(userMsg);

        ChatMessage aiMsg = ChatMessage.builder()
                .session(chatSession)
                .sender("AI")
                .content(answer)
                .ragContext(ragContext)
                .tokenCount((int) Math.round(answer.split("\\s+").length * 1.3))
                .build();
        chatMessageRepository.save(aiMsg);
    }

    private String generateTitle(String question) {
        if (question == null || question.isBlank()) {
            return "Cuộc trò chuyện mới";
        }
        String cleaned = question.trim().replaceAll("\\s+", " ");
        if (cleaned.length() > 40) {
            return cleaned.substring(0, 37) + "...";
        }
        return cleaned;
    }

    private static class ScoredChunk {
        final Chunk chunk;
        final double score;

        ScoredChunk(Chunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }
    }
}
