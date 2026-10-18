package com.tung.assignment.core.service;

import com.tung.assignment.core.entity.Course;
import com.tung.assignment.core.repository.CourseRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    public long countCourses() {
        return courseRepository.count();
    }

    /**
     * Tự động khởi tạo 2 môn học mẫu nếu Database chưa có dữ liệu
     */
    @PostConstruct
    public void initSampleData() {
        if (courseRepository.count() == 0) {
            courseRepository.save(Course.builder()
                    .code("HSF302")
                    .name("JPA and Spring Framework")
                    .description("Lập trình ứng dụng web nâng cao với Spring Boot, Spring Data JPA, Thymeleaf và tích hợp AI.")
                    .color("from-blue-600 to-indigo-600")
                    .icon("fa-laptop-code")
                    .semester("Fall 2026")
                    .credits(3)
                    .createdAt(LocalDateTime.now())
                    .build());

            courseRepository.save(Course.builder()
                    .code("MAS291")
                    .name("Probability and Statistics")
                    .description("Xác suất thống kê ứng dụng trong khoa học dữ liệu và mô hình hóa học máy.")
                    .color("from-emerald-600 to-teal-600")
                    .icon("fa-chart-pie")
                    .semester("Fall 2026")
                    .credits(3)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}
