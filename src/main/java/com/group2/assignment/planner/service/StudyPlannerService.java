package com.group2.assignment.planner.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.core.entity.Chunk;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.ChunkRepository;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.planner.dto.StudyPlanRequestDto;
import com.group2.assignment.planner.entity.StudyPlan;
import com.group2.assignment.planner.entity.StudyTask;
import com.group2.assignment.planner.repository.StudyPlanRepository;
import com.group2.assignment.planner.repository.StudyTaskRepository;
import com.group2.assignment.settings.service.SystemAiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudyPlannerService {

    private final ChunkRepository chunkRepository;
    private final CourseRepository courseRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final StudyTaskRepository studyTaskRepository;
    private final ObjectMapper objectMapper;
    private final SystemAiKeyService systemAiKeyService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Transactional
    public StudyPlan generateStudyPlan(StudyPlanRequestDto dto) {
        // 1. Lấy API Key hệ thống (Centralized System Key, không lấy từ session người dùng)
        String apiKey = systemAiKeyService.getApiKey();

        // 2. Lấy Course entity từ DB (fallback về stub nếu không tìm thấy)
        Course course = courseRepository.findById(dto.getCourseId()).orElseGet(() -> {
            Course fallback = new Course();
            fallback.setId(dto.getCourseId());
            return fallback;
        });

        // 3. Lấy danh sách Chunks của môn học
        List<Chunk> chunks = chunkRepository.findAllByCourseId(dto.getCourseId());
        String contextText = chunks.stream()
                .map(Chunk::getContent)
                .collect(Collectors.joining("\n\n"));

        // 4. Gọi Gemini API để sinh Timeline
        List<Map<String, Object>> timelineData;
        if (apiKey != null && !apiKey.trim().isBlank()) {
            try {
                timelineData = callGeminiApi(dto, contextText, apiKey.trim());
            } catch (Exception e) {
                log.warn("Gọi Gemini API thất bại ({}) -> Dùng mock timeline", e.getMessage());
                timelineData = generateMockTimeline(dto);
            }
        } else {
            log.info("Không có API Key -> Dùng mock timeline");
            timelineData = generateMockTimeline(dto);
        }

        // 5. Tạo StudyPlan
        String daysString = dto.getSelectedDays() != null
                ? String.join(",", dto.getSelectedDays())
                : "";

        StudyPlan studyPlan = StudyPlan.builder()
                .course(course)
                .title("Kế hoạch ôn thi " + (course.getName() != null ? course.getName() : "Môn học"))
                .hoursPerWeek(dto.getHoursPerWeek() != null ? dto.getHoursPerWeek() : 6)
                .selectedDays(daysString)
                .examDate(dto.getExamDate())
                .goal(dto.getGoal())
                .status("ACTIVE")
                .progressPercent(0)
                .build();

        studyPlan = studyPlanRepository.save(studyPlan);

        // 6. Tạo StudyTasks
        List<StudyTask> tasks = new ArrayList<>();
        int sortOrder = 1;
        for (Map<String, Object> item : timelineData) {
            StudyTask task = StudyTask.builder()
                    .plan(studyPlan)
                    .weekNumber(((Number) item.getOrDefault("weekNumber", 1)).intValue())
                    .dayOfWeek((String) item.getOrDefault("dayOfWeek", "Thứ 3"))
                    .taskTitle((String) item.getOrDefault("taskTitle", "Ôn tập"))
                    .taskDescription((String) item.getOrDefault("taskDescription", ""))
                    .durationMinutes(((Number) item.getOrDefault("durationMinutes", 120)).intValue())
                    .isCompleted(false)
                    .sortOrder(sortOrder++)
                    .build();
            tasks.add(task);
        }

        studyTaskRepository.saveAll(tasks);

        return studyPlan;
    }

    private List<Map<String, Object>> callGeminiApi(StudyPlanRequestDto dto, String contextText, String apiKey) throws Exception {
        // Giới hạn context text để không vượt quá token limit
        String truncatedContext = contextText.length() > 6000 ? contextText.substring(0, 6000) : contextText;

        // Format ngày rảnh
        String daysList = dto.getSelectedDays() != null
                ? String.join(", ", dto.getSelectedDays())
                : "Chưa chọn";

        String examDateStr = dto.getExamDate() != null
                ? dto.getExamDate().toString()
                : "Chưa xác định";

        String systemPrompt = "Bạn là chuyên gia lập kế hoạch học tập thông minh. Hãy phân tích nội dung giáo trình và lịch rảnh của sinh viên, sau đó tạo lộ trình học tập chi tiết.\n"
                + "Quy tắc bắt buộc: Phải trả về một mảng JSON thuần túy (JSON Array) gồm các đối tượng có cấu trúc:\n"
                + "[\n"
                + "  {\n"
                + "    \"weekNumber\": 1,\n"
                + "    \"dayOfWeek\": \"Thứ 3\",\n"
                + "    \"taskTitle\": \"Ôn tập Spring Core - IoC Container\",\n"
                + "    \"taskDescription\": \"Tìm hiểu @Component, @Autowired, @Bean\",\n"
                + "    \"durationMinutes\": 120\n"
                + "  }\n"
                + "]\n"
                + "Ngày rảnh: " + daysList + "\n"
                + "Số giờ/tuần: " + dto.getHoursPerWeek() + "\n"
                + "Ngày thi: " + examDateStr + "\n"
                + "Mục tiêu: " + dto.getGoal() + "\n\n"
                + "Nội dung giáo trình:\n" + truncatedContext + "\n\n"
                + "Yêu cầu: Chia nhỏ nội dung giáo trình thành các nhiệm vụ học tập hợp lý, mỗi nhiệm vụ khoảng 60-120 phút. Không bao gồm markdown backticks (```json), chỉ trả về chuỗi JSON thô.";

        Map<String, Object> generationConfig = Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.5
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

                if (response.statusCode() == 200) {
                    JsonNode rootNode = objectMapper.readTree(response.body());
                    String rawContent = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

                    // Làm sạch JSON response
                    String cleanJson = rawContent.replaceAll("^```json\\s*", "").replaceAll("```\\s*$", "").trim();

                    List<Map<String, Object>> timelineData = objectMapper.readValue(
                            cleanJson,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );

                    return timelineData;
                } else {
                    lastException = new RuntimeException("Gemini HTTP " + response.statusCode() + " on model " + model + ": " + response.body());
                    log.warn("Model {} trả về lỗi HTTP {} -> thử model kế tiếp", model, response.statusCode());
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("Thử model {} thất bại ({}) -> chuyển model kế tiếp", model, e.getMessage());
            }
        }

        throw lastException != null ? lastException : new RuntimeException("Tất cả candidate Gemini models đều không phản hồi.");
    }

    private List<Map<String, Object>> generateMockTimeline(StudyPlanRequestDto dto) {
        List<Map<String, Object>> timeline = new ArrayList<>();

        // Tính số tuần từ ngày hiện tại đến ngày thi
        int totalWeeks = 4;
        if (dto.getExamDate() != null) {
            long daysUntilExam = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), dto.getExamDate());
            totalWeeks = Math.max(1, (int) (daysUntilExam / 7));
            totalWeeks = Math.min(totalWeeks, 8); // Tối đa 8 tuần
        }

        String[] defaultTopics = {
                "Ôn tập tổng quan môn học",
                "Tìm hiểu các khái niệm cơ bản",
                "Luyện tập ví dụ minh họa",
                "Ôn tập và giải bài tập tổng hợp"
        };

        String[] defaultDays = {"Thứ 3", "Thứ 5", "Thứ 7"};

        for (int week = 1; week <= totalWeeks; week++) {
            Map<String, Object> task = new HashMap<>();
            task.put("weekNumber", week);
            task.put("dayOfWeek", defaultDays[(week - 1) % defaultDays.length]);
            task.put("taskTitle", "Tuần " + week + ": " + defaultTopics[(week - 1) % defaultTopics.length]);
            task.put("taskDescription", "Học và ôn tập nội dung tuần " + week + " theo giáo trình");
            task.put("durationMinutes", 120);
            timeline.add(task);
        }

        return timeline;
    }

    public Optional<StudyPlan> getPlanById(Long planId) {
        return studyPlanRepository.findById(planId);
    }

    public List<StudyTask> getTasksByPlanId(Long planId) {
        return studyTaskRepository.findAllByPlanIdOrderByWeekNumberAscSortOrderAsc(planId);
    }

    @Transactional
    public StudyTask toggleTaskCompletion(Long taskId) {
        Optional<StudyTask> taskOpt = studyTaskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            StudyTask task = taskOpt.get();
            task.setIsCompleted(!task.getIsCompleted());
            if (task.getIsCompleted()) {
                task.setCompletedAt(java.time.LocalDateTime.now());
            } else {
                task.setCompletedAt(null);
            }
            task = studyTaskRepository.save(task);

            // Cập nhật progress của plan
            updatePlanProgress(task.getPlan().getId());

            return task;
        }
        return null;
    }

    private void updatePlanProgress(Long planId) {
        Optional<StudyPlan> planOpt = studyPlanRepository.findById(planId);
        if (planOpt.isPresent()) {
            StudyPlan plan = planOpt.get();
            long totalTasks = studyTaskRepository.countByPlanId(planId);
            long completedTasks = studyTaskRepository.countByPlanIdAndIsCompletedTrue(planId);

            if (totalTasks > 0) {
                int progress = (int) ((completedTasks * 100) / totalTasks);
                plan.setProgressPercent(progress);

                if (progress == 100) {
                    plan.setStatus("COMPLETED");
                }

                studyPlanRepository.save(plan);
            }
        }
    }

    public List<StudyPlan> getPlansByCourseId(Long courseId) {
        return studyPlanRepository.findAllByCourseIdOrderByCreatedAtDesc(courseId);
    }

    @Transactional(readOnly = true)
    public List<StudyPlan> getRecentPlans() {
        List<StudyPlan> plans = studyPlanRepository.findAllByOrderByCreatedAtDesc();
        // Khởi tạo quan hệ lazy để template đọc được tên môn học
        plans.forEach(plan -> {
            if (plan.getCourse() != null) {
                plan.getCourse().getName();
            }
        });
        return plans;
    }
}
