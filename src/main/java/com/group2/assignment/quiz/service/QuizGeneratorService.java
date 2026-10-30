package com.group2.assignment.quiz.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.quiz.entity.QuizQuestion;
import lombok.RequiredArgsConstructor;
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
    private final HttpClient httpClient;

    public QuizGeneratorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<QuizQuestion> generateQuestions(String contextText, int count, String difficulty, String apiKey) {
        if (apiKey != null && !apiKey.trim().isBlank()) {
            try {
                return callGeminiAi(contextText, count, difficulty, apiKey.trim());
            } catch (Exception e) {
                log.warn("Gọi Gemini API thất bại ({}), tự động kích hoạt Smart Semantic Fallback", e.getMessage());
            }
        }
        return generateMockQuestions(contextText, count, difficulty);
    }

    private List<QuizQuestion> callGeminiAi(String contextText, int count, String difficulty, String apiKey) throws Exception {
        String truncatedContext = contextText.length() > 8000 ? contextText.substring(0, 8000) : contextText;

        String systemPrompt = "Bạn là trợ lý giảng viên đại học. Hãy phân tích kỹ tài liệu được cung cấp và sinh chính xác " + count + " câu hỏi trắc nghiệm 4 lựa chọn (A, B, C, D) với độ khó " + difficulty + ".\n"
                + "Quy tắc bắt buộc: Phải trả về một mảng JSON thuần túy (JSON Array) gồm các đối tượng có cấu trúc:\n"
                + "[\n"
                + "  {\n"
                + "    \"questionText\": \"Nội dung câu hỏi ngắn gọn, thực tế\",\n"
                + "    \"optionA\": \"Lựa chọn A\",\n"
                + "    \"optionB\": \"Lựa chọn B\",\n"
                + "    \"optionC\": \"Lựa chọn C\",\n"
                + "    \"optionD\": \"Lựa chọn D\",\n"
                + "    \"correctAnswer\": \"A\",\n"
                + "    \"explanation\": \"Lời giải thích chi tiết trích xuất từ tài liệu\"\n"
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

        for (int i = 0; i < count; i++) {
            String targetSentence = definitionSentences.get(i % definitionSentences.size());
            String otherSentence1 = definitionSentences.get((i + 1) % definitionSentences.size());
            String otherSentence2 = definitionSentences.get((i + 2) % definitionSentences.size());

            String questionPrompt;
            if (targetSentence.contains(" là ") || targetSentence.contains(" is ")) {
                String[] parts = targetSentence.split(" là | is ", 2);
                questionPrompt = "Theo tài liệu, phát biểu nào sau đây định nghĩa chính xác về \"" + parts[0].trim() + "\"?";
            } else {
                questionPrompt = "Dựa trên nội dung giáo trình: \"" + targetSentence + "\", khẳng định nào sau đây là ĐÚNG?";
            }

            questions.add(QuizQuestion.builder()
                    .questionText(questionPrompt)
                    .optionA(targetSentence)
                    .optionB("Hoàn toàn trái ngược với: " + otherSentence1)
                    .optionC("Chỉ áp dụng giới hạn trong trường hợp: " + otherSentence2)
                    .optionD("Không được định nghĩa trong phạm vi môn học này")
                    .correctAnswer("A")
                    .explanation("Đáp án A phản ánh chính xác 100% nguyên văn nội dung trích xuất từ tài liệu học tập.")
                    .sortOrder(i + 1)
                    .build());
        }

        return questions;
    }
}
