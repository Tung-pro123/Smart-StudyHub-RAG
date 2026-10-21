package com.group2.assignment.planner.controller;

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

        return "planner/index";
    }

    @PostMapping("/generate")
    public String generateStudyPlan(
            @ModelAttribute StudyPlanRequestDto dto,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        // Validate input
        if (dto.getCourseId() == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn môn học!");
            return "redirect:/planner";
        }

        if (dto.getHoursPerWeek() == null || dto.getHoursPerWeek() <= 0) {
            dto.setHoursPerWeek(6); // Default
        }

        try {
            StudyPlan plan = studyPlannerService.generateStudyPlan(dto, session);
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
            RedirectAttributes redirectAttributes) {

        List<StudyPlan> plans = studyPlannerService.getPlansByCourseId(courseId);
        model.addAttribute("plans", plans);
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("selectedCourseId", courseId);

        return "planner/index";
    }
}
