package com.tung.assignment.core.controller;

import com.tung.assignment.core.entity.Course;
import com.tung.assignment.core.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CourseService courseService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("totalCourses", courseService.countCourses());
        model.addAttribute("newCourse", new Course());
        return "index";
    }

    @PostMapping("/courses/create")
    public String createCourse(@ModelAttribute("newCourse") Course course) {
        if (course.getCode() != null && !course.getCode().isBlank()) {
            courseService.saveCourse(course);
        }
        return "redirect:/";
    }

    @GetMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id) {
        courseService.deleteCourse(id);
        return "redirect:/";
    }
}
