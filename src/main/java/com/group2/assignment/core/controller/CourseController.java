package com.group2.assignment.core.controller;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.core.dto.CourseCardDto;
import com.group2.assignment.core.entity.Chunk;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.ChunkRepository;
import com.group2.assignment.core.service.CourseService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final ChunkRepository chunkRepository;

    /**
     * Màn hình danh sách môn học / Dashboard (Yêu cầu đăng nhập, cách ly dữ liệu theo User)
     */
    @GetMapping
    public String listCourses(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "semester", required = false) String semester,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        List<CourseCardDto> courses = courseService.getAllCoursesWithStats(keyword, semester, currentUser);
        List<String> semesters = courseService.getAllSemesters();
        long totalCourses = (currentUser != null && currentUser.getId() != null && !currentUser.isAdmin())
                ? courseService.countCoursesByUser(currentUser)
                : courseService.countCourses();

        model.addAttribute("courses", courses);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("semesters", semesters);
        model.addAttribute("currentKeyword", keyword != null ? keyword : "");
        model.addAttribute("currentSemester", semester != null ? semester : "ALL");
        model.addAttribute("newCourse", new Course());
        model.addAttribute("currentUser", currentUser);

        // Thông tin hạn ngạch gói cước
        model.addAttribute("maxCoursesFree", CourseService.MAX_COURSES_FREE);
        model.addAttribute("canCreateCourse", courseService.canCreateCourse(currentUser));
        model.addAttribute("isProUser", currentUser != null && currentUser.isPro());

        return "index";
    }

    /**
     * Màn hình chi tiết / Hub điều phối của Môn học (Yêu cầu đăng nhập)
     */
    @GetMapping("/{id}")
    public String viewCourseDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            CourseCardDto courseDto = courseService.getCourseWithStats(id);
            List<Chunk> chunks = chunkRepository.findAllByCourseId(id);

            model.addAttribute("course", courseDto);
            model.addAttribute("chunks", chunks);
            model.addAttribute("editCourse", courseService.getCourseById(id).orElse(new Course()));
            return "courses/detail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses";
        }
    }

    /**
     * Xử lý tạo môn học mới (kèm kiểm tra hạn ngạch gói cước và gán người sở hữu)
     */
    @PostMapping({"/create", ""})
    public String createCourse(
            @ModelAttribute("newCourse") Course course,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để tạo môn học!");
            return "redirect:/login";
        }

        try {
            courseService.saveCourse(course, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tạo thành công môn học: " + course.getCode() + " - " + course.getName() + "!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("showUpgradeModal", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/courses";
    }

    /**
     * Xử lý cập nhật thông tin môn học (Kiểm tra quyền sở hữu)
     */
    @PostMapping("/{id}/edit")
    public String editCourse(
            @PathVariable("id") Long id,
            @ModelAttribute Course course,
            @RequestParam(value = "redirect", required = false, defaultValue = "home") String redirect,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để chỉnh sửa môn học!");
            return "redirect:/login";
        }

        try {
            courseService.updateCourse(id, course, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật thành công thông tin môn học " + course.getCode() + "!");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "⛔ " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        if ("detail".equalsIgnoreCase(redirect)) {
            return "redirect:/courses/" + id;
        }
        return "redirect:/courses";
    }

    /**
     * Xử lý xóa môn học (Kiểm tra quyền sở hữu)
     */
    @GetMapping("/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xóa môn học!");
            return "redirect:/login";
        }

        try {
            Course course = courseService.getCourseById(id).orElse(null);
            String courseCode = course != null ? course.getCode() : "#" + id;
            courseService.deleteCourse(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xóa môn học [" + courseCode + "] và toàn bộ dữ liệu liên quan thành công!");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "⛔ " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa môn học: " + e.getMessage());
        }
        return "redirect:/courses";
    }
}
