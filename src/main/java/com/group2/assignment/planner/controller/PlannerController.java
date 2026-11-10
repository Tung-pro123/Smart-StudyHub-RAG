package com.group2.assignment.planner.controller;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.planner.dto.PlannerPageDto;
import com.group2.assignment.planner.dto.StudyPlanRequestDto;
import com.group2.assignment.planner.dto.TaskToggleResultDto;
import com.group2.assignment.planner.entity.StudyPlan;
import com.group2.assignment.planner.entity.StudyTask;
import com.group2.assignment.planner.service.StudyPlannerService;
import com.group2.assignment.planner.service.StudySourceMissingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = getCurrentUser(session);
        List<Course> courses = getCoursesFor(currentUser);
        model.addAttribute("courses", courses);

        PlannerPageDto<StudyPlan> plansPage = courseId != null && getOwnedCourse(courseId, currentUser) != null
                ? studyPlannerService.getPlansByCourseId(courseId, currentUser, page, size)
                : studyPlannerService.getRecentPlans(currentUser, page, size);
        addPlansPageAttributes(model, plansPage);

        model.addAttribute("selectedCourseId", courseId);

        addSubscriptionAttributes(model, currentUser);

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

        if (getOwnedCourse(dto.getCourseId(), currentUser) == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Môn học không tồn tại hoặc không thuộc tài khoản của bạn!");
            return "redirect:/planner";
        }

        try {
            StudyPlan plan = studyPlannerService.generateStudyPlan(dto, currentUser);
            redirectAttributes.addFlashAttribute("success", "Tạo kế hoạch học tập thành công!");
            return "redirect:/planner/" + plan.getId();
        } catch (StudySourceMissingException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Môn học này chưa có dữ liệu nguồn đã trích xuất. Hãy tải lên giáo trình PDF hoặc tài liệu học tập để tạo chunks trước khi sinh kế hoạch.");
            redirectAttributes.addFlashAttribute("missingSourceCourseId", dto.getCourseId());
            return "redirect:/planner?courseId=" + dto.getCourseId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo kế hoạch: " + e.getMessage());
            return "redirect:/planner";
        }
    }

    @GetMapping("/{planId}")
    public String viewPlan(
            @PathVariable Long planId,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        User currentUser = getCurrentUser(session);
        var planOpt = studyPlannerService.getPlanById(planId, currentUser);
        if (planOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Không tìm thấy kế hoạch hoặc không thuộc tài khoản của bạn");
            return "redirect:/planner";
        }

        StudyPlan plan = planOpt.get();
        List<StudyTask> tasks = studyPlannerService.getTasksByPlanId(planId);

        Map<Integer, List<StudyTask>> tasksByWeek = tasks.stream()
                .collect(Collectors.groupingBy(
                        StudyTask::getWeekNumber,
                        LinkedHashMap::new,
                        Collectors.toList()));

        model.addAttribute("plan", plan);
        model.addAttribute("tasks", tasks);
        model.addAttribute("tasksByWeek", tasksByWeek);

        return "planner/view";
    }

    @PostMapping("/tasks/{taskId}/toggle")
    @ResponseBody
    public ResponseEntity<TaskToggleResultDto> toggleTask(@PathVariable Long taskId, HttpSession session) {
        TaskToggleResultDto result = studyPlannerService.toggleTaskCompletion(taskId, getCurrentUser(session));
        if (result == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history/{courseId}")
    public String getHistory(
            @PathVariable Long courseId,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        User currentUser = getCurrentUser(session);
        if (getOwnedCourse(courseId, currentUser) == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Môn học không tồn tại hoặc không thuộc tài khoản của bạn!");
            return "redirect:/planner";
        }

        PlannerPageDto<StudyPlan> plansPage =
                studyPlannerService.getPlansByCourseId(courseId, currentUser, page, size);
        addPlansPageAttributes(model, plansPage);
        model.addAttribute("courses", getCoursesFor(currentUser));
        model.addAttribute("selectedCourseId", courseId);

        addSubscriptionAttributes(model, currentUser);

        return "planner/index";
    }

    private void addPlansPageAttributes(Model model, PlannerPageDto<StudyPlan> plansPage) {
        model.addAttribute("plans", plansPage.getItems());
        model.addAttribute("plansPage", plansPage);
        model.addAttribute("currentPage", plansPage.getPage());
        model.addAttribute("pageSize", plansPage.getSize());
        model.addAttribute("totalPages", plansPage.getTotalPages());
        model.addAttribute("totalItems", plansPage.getTotalItems());
        model.addAttribute("hasPrev", plansPage.isHasPrev());
        model.addAttribute("hasNext", plansPage.isHasNext());
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

    private List<Course> getCoursesFor(User user) {
        if (user == null) {
            return List.of();
        }
        if (user.isAdmin()) {
            return courseRepository.findAll();
        }
        if (user.getId() == null) {
            return List.of();
        }
        return courseRepository.findAllByUserId(user.getId());
    }

    private Course getOwnedCourse(Long courseId, User user) {
        if (courseId == null || user == null) {
            return null;
        }
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return null;
        }
        if (user.isAdmin()) {
            return course;
        }
        if (user.getId() == null || course.getUser() == null) {
            return null;
        }
        return user.getId().equals(course.getUser().getId()) ? course : null;
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
