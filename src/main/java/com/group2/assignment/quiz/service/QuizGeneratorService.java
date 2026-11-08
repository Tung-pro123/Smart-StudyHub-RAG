package com.group2.assignment.quiz.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.settings.service.SystemAiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class QuizGeneratorService {

    private final ObjectMapper objectMapper;
    private final SystemAiKeyService systemAiKeyService;
    private final HttpClient httpClient;

    public QuizGeneratorService(ObjectMapper objectMapper, SystemAiKeyService systemAiKeyService) {
        this.objectMapper = objectMapper;
        this.systemAiKeyService = systemAiKeyService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<QuizQuestion> generateQuestions(String contextText, int count, String difficulty) {
        String apiKey = systemAiKeyService.getApiKey();
        if (apiKey != null && !apiKey.trim().isBlank()) {
            try {
                return callGeminiAi(contextText, count, difficulty, apiKey.trim());
            } catch (Exception e) {
                log.warn("Gọi Gemini API thất bại ({}), tự động kích hoạt Smart Semantic Fallback", e.getMessage());
            }
        }
        return generateMockQuestions(contextText, count, difficulty);
    }

    private String buildDifficultyPromptRubric(String difficulty) {
        String level = difficulty != null ? difficulty.toUpperCase().trim() : "MEDIUM";
        return switch (level) {
            case "EASY" -> """
                [TIÊU CHÍ ĐỘ KHÓ: DỄ (EASY) - NHẬN BIẾT & GHI NHỚ]
                - Mục tiêu nhận thức: Kiểm tra khả năng ghi nhớ thuật ngữ, định nghĩa, sự kiện cốt lõi xuất hiện trực tiếp trong tài liệu.
                - Dạng câu hỏi: 'Khái niệm X là gì?', 'Thành phần nào sau đây thuộc về Y?', 'Từ khóa nào phản ánh đúng...'.
                - Thiết kế đáp án: 1 đáp án đúng trực diện, 3 đáp án nhiễu (distractors) phải rõ ràng sai, không mang tính đánh đố.
                """;
            case "HARD" -> """
                [TIÊU CHÍ ĐỘ KHÓ: NÂNG CAO (HARD) - PHÂN TÍCH, ĐÁNH GIÁ & TÌNH HUỐNG]
                - Mục tiêu nhận thức: Đòi hỏi phân tích ngữ cảnh, xử lý tình huống thực tế (mini case study / scenario) hoặc so sánh đánh đổi (trade-offs / edge cases).
                - Dạng câu hỏi: 'Trong tình huống hệ thống gặp lỗi X khi thực thi Y, nguyên nhân cốt lõi là gì?', 'Phương án kiến trúc nào sau đây là tối ưu nhất khi...?', 'Khẳng định nào sau đây là SAI về mặt bản chất kỹ thuật?'.
                - Thiết kế đáp án: Cả 4 phương án A, B, C, D đều phải có vẻ hợp lý và chặt chẽ (Plausible Distractors). Thí sinh phải suy luận logic sâu sắc mới chọn đúng.
                """;
            default -> """
                [TIÊU CHÍ ĐỘ KHÓ: TRUNG BÌNH (MEDIUM) - THÔNG HIỂU & VẬN DỤNG]
                - Mục tiêu nhận thức: Kiểm tra khả năng giải thích nguyên lý, so sánh sự khác nhau giữa 2 khái niệm hoặc vận dụng quy tắc vào kịch bản ngắn.
                - Dạng câu hỏi: 'Tại sao nên áp dụng cơ chế X thay vì Y?', 'Điểm khác biệt cốt lõi giữa A và B là gì?', 'Khi thực hiện thao tác sau, kết quả nhận được sẽ là gì?'.
                - Thiết kế đáp án: Các đáp án sai phải chứa các lỗi tư duy phổ biến mà sinh viên thường nhầm lẫn.
                """;
        };
    }

    private List<QuizQuestion> callGeminiAi(String contextText, int count, String difficulty, String apiKey) throws Exception {
        String truncatedContext = contextText.length() > 8000 ? contextText.substring(0, 8000) : contextText;
        String rubric = buildDifficultyPromptRubric(difficulty);

        String systemPrompt = "Bạn là trợ lý giảng viên đại học chuyên ngành. Hãy phân tích kỹ tài liệu được cung cấp và sinh chính xác " + count + " câu hỏi trắc nghiệm 4 lựa chọn (A, B, C, D).\n"
                + rubric + "\n"
                + "Quy tắc bắt buộc: Phải trả về một mảng JSON thuần túy (JSON Array) gồm các đối tượng có cấu trúc:\n"
                + "[\n"
                + "  {\n"
                + "    \"questionText\": \"Nội dung câu hỏi bám sát mức độ khó quy định\",\n"
                + "    \"optionA\": \"Lựa chọn A\",\n"
                + "    \"optionB\": \"Lựa chọn B\",\n"
                + "    \"optionC\": \"Lựa chọn C\",\n"
                + "    \"optionD\": \"Lựa chọn D\",\n"
                + "    \"correctAnswer\": \"A\",\n"
                + "    \"explanation\": \"Lời giải thích chi tiết trích xuất từ tài liệu, giải thích rõ vì sao đáp án đúng và các phương án còn lại sai\"\n"
                + "  }\n"
                + "]\n"
                + "Không bao gồm markdown backticks (```json), chỉ trả về chuỗi JSON thô.\n\n"
                + "Nội dung tài liệu:\n" + truncatedContext;

        Map<String, Object> generationConfig = Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.4
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", systemPrompt)))
                ),
                "generationConfig", generationConfig
        );

        String jsonPayload = objectMapper.writeValueAsString(requestBody);
        List<String> models = List.of(
                "gemini-2.5-flash",
                "gemini-2.0-flash",
                "gemini-1.5-flash-latest",
                "gemini-1.5-flash",
                "gemini-1.5-pro",
                "gemini-2.5-flash-lite"
        );

        Exception lastException = null;

        for (String model : models) {
            try {
                String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .header("Content-Type", "application/json")
                        .header("x-goog-api-key", apiKey)
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("Gemini HTTP error: " + response.statusCode() + " - " + response.body());
                }

                JsonNode rootNode = objectMapper.readTree(response.body());
                String rawContent = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

                String cleanJson = rawContent.replaceAll("^```json\\s*", "").replaceAll("```\\s*$", "").trim();

                List<Map<String, String>> rawQuestions = objectMapper.readValue(cleanJson, new TypeReference<List<Map<String, String>>>() {});
                List<QuizQuestion> questions = new ArrayList<>();
                int order = 1;

                for (Map<String, String> m : rawQuestions) {
                    questions.add(QuizQuestion.builder()
                            .questionText(m.getOrDefault("questionText", "Câu hỏi trắc nghiệm"))
                            .optionA(m.getOrDefault("optionA", "Đáp án A"))
                            .optionB(m.getOrDefault("optionB", "Đáp án B"))
                            .optionC(m.getOrDefault("optionC", "Đáp án C"))
                            .optionD(m.getOrDefault("optionD", "Đáp án D"))
                            .correctAnswer(m.getOrDefault("correctAnswer", "A").trim().toUpperCase())
                            .explanation(m.getOrDefault("explanation", "Dựa theo nội dung giáo trình được cung cấp."))
                            .sortOrder(order++)
                            .build());
                }

                return questions;
            } catch (Exception e) {
                log.warn("Thử sinh câu hỏi với Gemini model '{}' thất bại: {}. Đang thử model tiếp theo...", model, e.getMessage());
                lastException = e;
            }
        }
        throw (lastException != null) ? lastException : new RuntimeException("Không tìm thấy model Gemini khả dụng!");
    }

    public List<QuizQuestion> generateMockQuestions(String contextText, int count, String difficulty) {
        List<QuizQuestion> questions = new ArrayList<>();
        String[] lines = contextText.split("[\\r\\n]+|[.!?]+\\s+");
        List<String> definitionSentences = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() >= 25 && trimmed.length() <= 180) {
                definitionSentences.add(trimmed);
            }
        }

        if (definitionSentences.isEmpty()) {
            definitionSentences.add("Khái niệm kiến thức cốt lõi của tài liệu được trích xuất.");
        }

        String level = difficulty != null ? difficulty.toUpperCase().trim() : "MEDIUM";

        for (int i = 0; i < count; i++) {
            String targetSentence = definitionSentences.get(i % definitionSentences.size());
            String otherSentence1 = definitionSentences.get((i + 1) % definitionSentences.size());
            String otherSentence2 = definitionSentences.get((i + 2) % definitionSentences.size());

            String questionPrompt;
            String explanation;

            if ("EASY".equals(level)) {
                if (targetSentence.contains(" là ") || targetSentence.contains(" is ")) {
                    String[] parts = targetSentence.split(" là | is ", 2);
                    questionPrompt = "Theo giáo trình, thuật ngữ \"" + parts[0].trim() + "\" được định nghĩa như thế nào?";
                } else {
                    questionPrompt = "Khẳng định nào sau đây phản ánh đúng nguyên văn tài liệu học tập?";
                }
                explanation = "Đáp án A trích dẫn chuẩn xác định nghĩa cốt lõi trong giáo trình môn học.";
            } else if ("HARD".equals(level)) {
                questionPrompt = "Trong kịch bản thực tế áp dụng kiến thức: \"" + targetSentence + "\", nhận định nào sau đây đánh giá ĐÚNG nguyên lý và tránh được rủi ro tiềm ẩn?";
                explanation = "Đáp án A phân tích sâu sắc bản chất kỹ thuật và các ràng buộc vận hành, các đáp án còn lại chứa lỗi tư duy thiết kế.";
            } else {
                if (targetSentence.contains(" là ") || targetSentence.contains(" is ")) {
                    String[] parts = targetSentence.split(" là | is ", 2);
                    questionPrompt = "Theo tài liệu, phát biểu nào sau đây làm rõ chính xác về \"" + parts[0].trim() + "\"?";
                } else {
                    questionPrompt = "Dựa trên nội dung giáo trình: \"" + targetSentence + "\", khẳng định nào sau đây là ĐÚNG?";
                }
                explanation = "Đáp án A phản ánh chính xác 100% nguyên văn nội dung trích xuất từ tài liệu học tập.";
            }

            questions.add(QuizQuestion.builder()
                    .questionText(questionPrompt)
                    .optionA(targetSentence)
                    .optionB("Hoàn toàn trái ngược với: " + otherSentence1)
                    .optionC("Chỉ áp dụng giới hạn trong trường hợp: " + otherSentence2)
                    .optionD("Không được định nghĩa trong phạm vi môn học này")
                    .correctAnswer("A")
                    .explanation(explanation)
                    .sortOrder(i + 1)
                    .build());
        }

        return questions;
    }
}
