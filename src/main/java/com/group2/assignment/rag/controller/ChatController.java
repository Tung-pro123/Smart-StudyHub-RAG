package com.group2.assignment.rag.controller;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.rag.dto.ChatRequestDto;
import com.group2.assignment.rag.dto.ChatResponseDto;
import com.group2.assignment.rag.entity.ChatMessage;
import com.group2.assignment.rag.entity.ChatSession;
import com.group2.assignment.rag.repository.ChatMessageRepository;
import com.group2.assignment.rag.repository.ChatSessionRepository;
import com.group2.assignment.rag.service.LlmKeyRotator;
import com.group2.assignment.rag.service.RagEngineService;
import com.group2.assignment.settings.service.SystemAiKeyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/courses/{courseId}")
@RequiredArgsConstructor
public class ChatController {

    private final CourseRepository courseRepository;
    private final SystemAiKeyService systemAiKeyService;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RagEngineService ragEngineService;
    private final UserRepository userRepository;
    private final LlmKeyRotator llmKeyRotator;

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

        // Lấy thông tin người dùng và tự động reset hạn ngạch nếu sang ngày mới
        User currentUser = getCurrentUser(session);
        if (currentUser != null) {
            currentUser = checkAndResetDailyQuota(currentUser, session);
        }

        boolean isPro = currentUser != null && ("PRO".equalsIgnoreCase(currentUser.getSubscriptionTier()) || "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole()));
        int dailyCount = (currentUser != null && currentUser.getDailyRagCount() != null) ? currentUser.getDailyRagCount() : 0;
        int remainingQuota = isPro ? -1 : Math.max(0, 50 - dailyCount);

        boolean hasSystemKey = (systemAiKeyService != null && systemAiKeyService.isConfigured()) || llmKeyRotator.hasConfiguredKeys();
        boolean hasApiKey = hasSystemKey;

        model.addAttribute("course", course);
        model.addAttribute("sessions", sessions);
        model.addAttribute("currentSession", currentSession);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("hasSystemKey", hasSystemKey);
        model.addAttribute("hasApiKey", hasApiKey); // Giữ backward compatibility cho các template/test cũ
        model.addAttribute("isPro", isPro);
        model.addAttribute("dailyRagCount", dailyCount);
        model.addAttribute("remainingQuota", remainingQuota);

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

        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ChatResponseDto.builder()
                    .success(false)
                    .errorMessage("Vui lòng đăng nhập để sử dụng Trợ lý AI giáo trình!")
                    .build());
        }

        // Tự động kiểm tra reset ngày mới
        currentUser = checkAndResetDailyQuota(currentUser, session);

        boolean isPro = "PRO".equalsIgnoreCase(currentUser.getSubscriptionTier()) || "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole());
        int dailyCount = currentUser.getDailyRagCount() != null ? currentUser.getDailyRagCount() : 0;

        // Kiểm tra chặn nếu là gói FREE và đã đạt 50 câu/ngày
        if (!isPro && dailyCount >= 50) {
            return ResponseEntity.ok(ChatResponseDto.builder()
                    .success(false)
                    .answer("🛑 **Bạn đã sử dụng hết 50 câu hỏi RAG miễn phí hôm nay.**\n\nHãy nâng cấp [Gói Pro](/courses) để chat không giới hạn và mở khóa đầy đủ tính năng AI!")
                    .errorMessage("Đã đạt giới hạn 50 câu hỏi RAG/ngày của gói Free")
                    .remainingQuota(0)
                    .dailyRagCount(dailyCount)
                    .subscriptionTier(currentUser.getSubscriptionTier())
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        // Gọi RAG AI
        ChatResponseDto response = ragEngineService.chatWithCourse(
                courseId,
                requestDto.getSessionId(),
                requestDto.getQuestion().trim()
        );

        // Nếu thành công -> Tăng biến đếm và lưu DB nếu user tồn tại
        if (response.isSuccess()) {
            currentUser.setDailyRagCount(dailyCount + 1);
            if (currentUser.getId() != null && userRepository.existsById(currentUser.getId())) {
                currentUser = userRepository.save(currentUser);
            }
            if (session != null) {
                session.setAttribute(AuthController.SESSION_USER, currentUser);
            }

            int updatedDailyCount = currentUser.getDailyRagCount();
            response.setDailyRagCount(updatedDailyCount);
            response.setRemainingQuota(isPro ? -1 : Math.max(0, 50 - updatedDailyCount));
        } else {
            response.setDailyRagCount(dailyCount);
            response.setRemainingQuota(isPro ? -1 : Math.max(0, 50 - dailyCount));
        }
        response.setSubscriptionTier(currentUser.getSubscriptionTier());

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

        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        currentUser = checkAndResetDailyQuota(currentUser, session);
        boolean isPro = "PRO".equalsIgnoreCase(currentUser.getSubscriptionTier()) || "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole());
        int dailyCount = currentUser.getDailyRagCount() != null ? currentUser.getDailyRagCount() : 0;

        if (!isPro && dailyCount >= 50) {
            ChatResponseDto quotaExceededResponse = ChatResponseDto.builder()
                    .success(false)
                    .answer("🛑 **Bạn đã sử dụng hết 50 câu hỏi RAG miễn phí hôm nay.**\n\nHãy nâng cấp [Gói Pro](/courses) để chat không giới hạn!")
                    .errorMessage("Đã đạt giới hạn 50 câu hỏi RAG/ngày")
                    .remainingQuota(0)
                    .dailyRagCount(dailyCount)
                    .subscriptionTier(currentUser.getSubscriptionTier())
                    .createdAt(LocalDateTime.now())
                    .build();

            String acceptHeader = request.getHeader("Accept");
            String requestedWith = request.getHeader("X-Requested-With");
            if ((acceptHeader != null && acceptHeader.contains("application/json")) || "XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                return ResponseEntity.ok(quotaExceededResponse);
            }
            return "redirect:/courses/" + courseId + "/chat";
        }

        ChatResponseDto response = ragEngineService.chatWithCourse(
                courseId,
                sessionId,
                question.trim()
        );

        if (response.isSuccess()) {
            currentUser.setDailyRagCount(dailyCount + 1);
            if (currentUser.getId() != null && userRepository.existsById(currentUser.getId())) {
                currentUser = userRepository.save(currentUser);
            }
            if (session != null) {
                session.setAttribute(AuthController.SESSION_USER, currentUser);
            }

            int updatedDailyCount = currentUser.getDailyRagCount();
            response.setDailyRagCount(updatedDailyCount);
            response.setRemainingQuota(isPro ? -1 : Math.max(0, 50 - updatedDailyCount));
        } else {
            response.setDailyRagCount(dailyCount);
            response.setRemainingQuota(isPro ? -1 : Math.max(0, 50 - dailyCount));
        }
        response.setSubscriptionTier(currentUser.getSubscriptionTier());

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

    /**
     * Lấy thực thể User mới nhất từ Database theo Session
     */
    private User getCurrentUser(HttpSession session) {
        if (session == null) return null;
        User sessionUser = (User) session.getAttribute(AuthController.SESSION_USER);
        if (sessionUser == null || sessionUser.getId() == null) {
            return sessionUser;
        }
        return userRepository.findById(sessionUser.getId()).orElse(sessionUser);
    }

    /**
     * Kiểm tra và tự động Reset số câu hỏi về 0 khi bước sang ngày mới
     */
    private User checkAndResetDailyQuota(User user, HttpSession session) {
        LocalDate today = LocalDate.now();
        if (user.getLastRagResetDate() == null || today.isAfter(user.getLastRagResetDate())) {
            user.setDailyRagCount(0);
            user.setLastRagResetDate(today);
            if (user.getId() != null && userRepository.existsById(user.getId())) {
                user = userRepository.save(user);
            }
            if (session != null) {
                session.setAttribute(AuthController.SESSION_USER, user);
            }
            log.info("Đã tự động reset hạn ngạch RAG cho người dùng [{}] sang ngày mới: {}", user.getUsername(), today);
        }
        return user;
    }
}
