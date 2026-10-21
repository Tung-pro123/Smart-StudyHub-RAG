package com.group2.assignment.core.service;

import com.group2.assignment.core.dto.CourseCardDto;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.ChunkRepository;
import com.group2.assignment.core.repository.CourseRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final ChunkRepository chunkRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<CourseCardDto> getAllCoursesWithStats(String keyword, String semester) {
        List<Course> courses;
        boolean hasKeyword = keyword != null && !keyword.trim().isBlank();
        boolean hasSemester = semester != null && !semester.trim().isBlank() && !semester.equalsIgnoreCase("ALL");

        if (hasKeyword && hasSemester) {
            courses = courseRepository.findBySemesterIgnoreCaseAndNameContainingIgnoreCase(semester.trim(), keyword.trim());
        } else if (hasKeyword) {
            courses = courseRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrSemesterContainingIgnoreCase(keyword.trim(), keyword.trim(), keyword.trim());
        } else if (hasSemester) {
            courses = courseRepository.findBySemesterIgnoreCase(semester.trim());
        } else {
            courses = courseRepository.findAll();
        }

        return courses.stream()
                .map(c -> {
                    long chunks = chunkRepository.countByCourseId(c.getId());
                    return CourseCardDto.fromEntity(c, chunks);
                })
                .collect(Collectors.toList());
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    public CourseCardDto getCourseWithStats(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học với ID: " + id));
        long chunks = chunkRepository.countByCourseId(course.getId());
        return CourseCardDto.fromEntity(course, chunks);
    }

    public List<String> getAllSemesters() {
        return courseRepository.findAll().stream()
                .map(Course::getSemester)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Course saveCourse(Course course) {
        if (course.getCode() == null || course.getCode().trim().isBlank()) {
            throw new IllegalArgumentException("Mã môn học không được để trống!");
        }
        if (course.getName() == null || course.getName().trim().isBlank()) {
            throw new IllegalArgumentException("Tên môn học không được để trống!");
        }

        course.setCode(course.getCode().trim().toUpperCase());
        course.setName(course.getName().trim());

        // Validate trùng code
        if (course.getId() == null) {
            if (courseRepository.existsByCodeIgnoreCase(course.getCode())) {
                throw new IllegalArgumentException("Mã môn học '" + course.getCode() + "' đã tồn tại trong hệ thống!");
            }
        } else {
            if (courseRepository.existsByCodeIgnoreCaseAndIdNot(course.getCode(), course.getId())) {
                throw new IllegalArgumentException("Mã môn học '" + course.getCode() + "' đã thuộc về một môn khác!");
            }
        }

        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, Course updatedData) {
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + id));

        String newCode = updatedData.getCode().trim().toUpperCase();
        if (courseRepository.existsByCodeIgnoreCaseAndIdNot(newCode, id)) {
            throw new IllegalArgumentException("Mã môn học '" + newCode + "' đã tồn tại ở môn khác!");
        }

        existing.setCode(newCode);
        existing.setName(updatedData.getName().trim());
        existing.setDescription(updatedData.getDescription());
        existing.setSemester(updatedData.getSemester());
        existing.setCredits(updatedData.getCredits() != null ? updatedData.getCredits() : 3);
        if (updatedData.getColor() != null && !updatedData.getColor().isBlank()) {
            existing.setColor(updatedData.getColor());
        }
        if (updatedData.getIcon() != null && !updatedData.getIcon().isBlank()) {
            existing.setIcon(updatedData.getIcon());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        return courseRepository.save(existing);
    }

    @Transactional
    public void deleteCourse(Long id) {
        chunkRepository.deleteAllByCourseId(id);
        courseRepository.deleteById(id);
    }

    public long countCourses() {
        return courseRepository.count();
    }

    /**
     * Tự động khởi tạo 3 môn học mẫu phong phú nếu Database chưa có dữ liệu
     */
    @PostConstruct
    public void initSampleData() {
        if (courseRepository.count() == 0) {
            courseRepository.save(Course.builder()
                    .code("HSF302")
                    .name("JPA and Spring Framework")
                    .description("Lập trình ứng dụng web nâng cao với Spring Boot 3, Spring Data JPA, Thymeleaf và tích hợp trí tuệ nhân tạo RAG.")
                    .color("from-indigo-600 to-purple-600")
                    .icon("fa-laptop-code")
                    .semester("Fall 2026")
                    .credits(3)
                    .createdAt(LocalDateTime.now())
                    .build());

            courseRepository.save(Course.builder()
                    .code("MAS291")
                    .name("Probability and Statistics")
                    .description("Xác suất thống kê ứng dụng trong khoa học dữ liệu, kiểm định giả thuyết và mô hình hóa học máy.")
                    .color("from-emerald-600 to-teal-600")
                    .icon("fa-chart-pie")
                    .semester("Fall 2026")
                    .credits(3)
                    .createdAt(LocalDateTime.now())
                    .build());

            courseRepository.save(Course.builder()
                    .code("SWP391")
                    .name("Software Development Project")
                    .description("Dự án phát triển phần mềm theo quy trình Scrum/Agile kết hợp CI/CD, phân tích thiết kế hệ thống thực tế.")
                    .color("from-amber-500 to-rose-600")
                    .icon("fa-cubes-stacked")
                    .semester("Fall 2026")
                    .credits(3)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}
