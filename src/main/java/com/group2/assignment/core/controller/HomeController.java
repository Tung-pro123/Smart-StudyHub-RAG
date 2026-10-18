package com.group2.assignment.core.controller;

import com.group2.assignment.core.dto.CourseCardDto;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CourseService courseService;

    @GetMapping("/")
    public String index(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "semester", required = false) String semester,
            Model model) {

        List<CourseCardDto> courses = courseService.getAllCoursesWithStats(keyword, semester);
        List<String> semesters = courseService.getAllSemesters();

        model.addAttribute("courses", courses);
        model.addAttribute("totalCourses", courseService.countCourses());
        model.addAttribute("semesters", semesters);
        model.addAttribute("currentKeyword", keyword != null ? keyword : "");
        model.addAttribute("currentSemester", semester != null ? semester : "ALL");
        model.addAttribute("newCourse", new Course());

        return "index";
    }
}
