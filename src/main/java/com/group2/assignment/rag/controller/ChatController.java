package com.group2.assignment.rag.controller;

import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.rag.dto.ChatRequestDto;
import com.group2.assignment.rag.dto.ChatResponseDto;
import com.group2.assignment.rag.entity.ChatMessage;
import com.group2.assignment.rag.entity.ChatSession;
import com.group2.assignment.rag.repository.ChatMessageRepository;
import com.group2.assignment.rag.repository.ChatSessionRepository;
import com.group2.assignment.rag.service.RagEngineService;
import com.group2.assignment.settings.SettingsConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/courses/{courseId}")
@RequiredArgsConstructor
public class ChatController {

    private final CourseRepository courseRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RagEngineService ragEngineService;

    /**
     * Giao diện Chatbot giáo trình AI
     */
    @GetMapping("/chat")
    public String showChatPage(@PathVariable Long courseId,
                               @RequestParam(value = "sessionId", required = false) Long sessionId,
                               Model model,
                               HttpSession session) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + courseId));

        List<ChatSession> sessions = chatSessionRepository.findAllByCourseIdOrderByUpdatedAtDesc(courseId);

        ChatSession currentSession = null;
        if (sessionId != null) {
            currentSession = chatSessionRepository.findById(sessionId).orElse(null);
        } else if (!sessions.isEmpty()) {
            currentSession = sessions.get(0);
        }

        List<ChatMessage> messages = Collections.emptyList();
        if (currentSession != null) {
            messages = chatMessageRepository.findAllBySessionIdOrderBySentAtAsc(currentSession.getId());
        }

        // Kiểm tra xem đã cấu hình API Key chưa
        String sessionKey = (String) session.getAttribute(SettingsConstants.SESSION_API_KEY);
        String envKey = System.getenv("GEMINI_API_KEY");
        boolean hasApiKey = (sessionKey != null && !sessionKey.isBlank()) || (envKey != null && !envKey.isBlank());

        model.addAttribute("course", course);
        model.addAttribute("sessions", sessions);
        model.addAttribute("currentSession", currentSession);
        model.addAttribute("messages", messages);
        model.addAttribute("hasApiKey", hasApiKey);

        return "rag/chat";
    }

    /**
     * API Gửi tin nhắn qua JSON (Fetch / AJAX)
     */
    @PostMapping(value = "/chat/send", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ChatResponseDto> sendChatMessageJson(@PathVariable Long courseId,
                                                               @RequestBody ChatRequestDto requestDto,
                                                               HttpSession session) {
        if (requestDto == null || requestDto.getQuestion() == null || requestDto.getQuestion().trim().isBlank()) {
            return ResponseEntity.badRequest().body(ChatResponseDto.builder()
                    .success(false)
                    .errorMessage("Nội dung câu hỏi không được rỗng!")
                    .build());
        }

        ChatResponseDto response = ragEngineService.chatWithCourse(
                courseId,
                requestDto.getSessionId(),
                requestDto.getQuestion().trim(),
                session
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Gửi tin nhắn qua HTML Form submit truyền thống
     */
    @PostMapping(value = "/chat/send", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object sendChatMessageForm(@PathVariable Long courseId,
                                      @RequestParam("question") String question,
                                      @RequestParam(value = "sessionId", required = false) Long sessionId,
                                      HttpServletRequest request,
                                      HttpSession session) {
        if (question == null || question.trim().isBlank()) {
            return "redirect:/courses/" + courseId + "/chat" + (sessionId != null ? "?sessionId=" + sessionId : "");
        }

        ChatResponseDto response = ragEngineService.chatWithCourse(
                courseId,
                sessionId,
                question.trim(),
                session
        );

        // Nếu request có header Accept application/json hoặc X-Requested-With AJAX
        String acceptHeader = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        if ((acceptHeader != null && acceptHeader.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return ResponseEntity.ok(response);
        }

        return "redirect:/courses/" + courseId + "/chat?sessionId=" + response.getSessionId();
    }

    /**
     * Dọn dẹp tin nhắn và xóa phiên chat
     */
    @PostMapping("/chat/clear")
    public String clearChatSession(@PathVariable Long courseId,
                                  @RequestParam(value = "sessionId", required = false) Long sessionId,
                                  RedirectAttributes redirectAttributes) {
        if (sessionId != null) {
            chatMessageRepository.deleteAllBySessionId(sessionId);
            chatSessionRepository.deleteById(sessionId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa lịch sử phiên trò chuyện!");
        }
        return "redirect:/courses/" + courseId + "/chat";
    }

    /**
     * Khởi tạo phiên trò chuyện mới
     */
    @GetMapping("/chat/new")
    public String startNewChat(@PathVariable Long courseId) {
        return "redirect:/courses/" + courseId + "/chat";
    }
}
