package com.group2.assignment.planner.controller;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.planner.dto.StudyPlanRequestDto;
import com.group2.assignment.planner.entity.StudyPlan;
import com.group2.assignment.planner.entity.StudyTask;
import com.group2.assignment.planner.service.StudyPlannerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/planner")
@RequiredArgsConstructor
public class PlannerController {

    private final StudyPlannerService studyPlannerService;
    private final CourseRepository courseRepository;

    @GetMapping
    public String showPlannerPage(
            Model model,
            HttpSession session,
            @RequestParam(value = "courseId", required = false) Long courseId) {
        // Lấy danh sách tất cả môn học
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);

        // Danh sách kế hoạch gần đây để hiển thị ở cột phải
        model.addAttribute("plans", studyPlannerService.getRecentPlans());

        // Giữ lựa chọn môn học nếu điều hướng từ trang chủ (?courseId=...)
        model.addAttribute("selectedCourseId", courseId);

        addSubscriptionAttributes(model, getCurrentUser(session));

        return "planner/index";
    }

    @PostMapping("/generate")
    public String generateStudyPlan(
            @ModelAttribute StudyPlanRequestDto dto,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Feature gating: AI Study Planner chỉ dành cho gói PRO/CAMPUS (và ADMIN)
        if (!isPlannerUnlocked(currentUser)) {
            redirectAttributes.addFlashAttribute("error",
                    "🔒 AI Study Planner là tính năng của Gói Pro. Nâng cấp để mở khóa lộ trình ôn thi cá nhân hóa!");
            redirectAttributes.addFlashAttribute("showUpgradeModal", true);
            return "redirect:/planner";
        }

        // Validate input
        if (dto.getCourseId() == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn môn học!");
            return "redirect:/planner";
        }

        if (dto.getHoursPerWeek() == null || dto.getHoursPerWeek() <= 0) {
            dto.setHoursPerWeek(6); // Default
        }

        try {
            StudyPlan plan = studyPlannerService.generateStudyPlan(dto);
            redirectAttributes.addFlashAttribute("success", "Tạo kế hoạch học tập thành công!");
            return "redirect:/planner/" + plan.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo kế hoạch: " + e.getMessage());
            return "redirect:/planner";
        }
    }

    @GetMapping("/{planId}")
    public String viewPlan(
            @PathVariable Long planId,
            Model model,
            RedirectAttributes redirectAttributes) {

        var planOpt = studyPlannerService.getPlanById(planId);
        if (planOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy kế hoạch!");
            return "redirect:/planner";
        }

        StudyPlan plan = planOpt.get();
        List<StudyTask> tasks = studyPlannerService.getTasksByPlanId(planId);

        model.addAttribute("plan", plan);
        model.addAttribute("tasks", tasks);

        return "planner/view";
    }

    @PostMapping("/tasks/{taskId}/toggle")
    @ResponseBody
    public String toggleTask(@PathVariable Long taskId) {
        StudyTask task = studyPlannerService.toggleTaskCompletion(taskId);
        if (task != null) {
            return "success";
        }
        return "error";
    }

    @GetMapping("/history/{courseId}")
    public String getHistory(
            @PathVariable Long courseId,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        List<StudyPlan> plans = studyPlannerService.getPlansByCourseId(courseId);
        model.addAttribute("plans", plans);
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("selectedCourseId", courseId);

        User currentUser = getCurrentUser(session);
        addSubscriptionAttributes(model, currentUser);

        return "planner/index";
    }

    private User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(AuthController.SESSION_USER);
    }

    private boolean isPlannerUnlocked(User user) {
        return user != null && user.isPro();
    }

    private void addSubscriptionAttributes(Model model, User user) {
        boolean unlocked = isPlannerUnlocked(user);
        String tier = user != null && user.getSubscriptionTier() != null
                ? user.getSubscriptionTier()
                : "FREE";
        model.addAttribute("isPro", unlocked);
        model.addAttribute("plannerLocked", !unlocked);
        model.addAttribute("subscriptionTier", tier);
    }
}
