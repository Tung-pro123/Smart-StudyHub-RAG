package com.group2.assignment.core.service;

import com.group2.assignment.auth.entity.User;
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

    public static final int MAX_COURSES_FREE = 3;

    private final CourseRepository courseRepository;
    private final ChunkRepository chunkRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * Lấy danh sách môn học kèm thống kê và lọc theo người dùng (Data Isolation)
     * - Admin: Xem được toàn bộ môn học trong hệ thống
     * - Sinh viên: Xem môn học của chính mình (+ môn học mẫu hệ thống)
     */
    public List<CourseCardDto> getAllCoursesWithStats(String keyword, String semester, User currentUser) {
        String kw = (keyword != null && !keyword.trim().isBlank()) ? keyword.trim() : null;
        String sem = (semester != null && !semester.trim().isBlank() && !semester.equalsIgnoreCase("ALL")) ? semester.trim() : null;

        List<Course> courses;
        if (currentUser != null && currentUser.isAdmin()) {
            courses = courseRepository.searchAll(kw, sem);
        } else if (currentUser != null && currentUser.getId() != null) {
            courses = courseRepository.searchCoursesByUser(currentUser.getId(), kw, sem, true);
        } else {
            courses = courseRepository.searchAll(kw, sem);
        }

        return courses.stream()
                .map(c -> {
                    long chunks = chunkRepository.countByCourseId(c.getId());
                    return CourseCardDto.fromEntity(c, chunks);
                })
                .collect(Collectors.toList());
    }

    /**
     * Quá tải tương thích ngược (Backward Compatibility)
     */
    public List<CourseCardDto> getAllCoursesWithStats(String keyword, String semester) {
        return getAllCoursesWithStats(keyword, semester, null);
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    public CourseCardDto getCourseWithStats(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + id));
        long chunks = chunkRepository.countByCourseId(id);
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

    /**
     * Kiểm tra người dùng có quyền tạo thêm môn học hay không (dựa trên hạn ngạch gói cước)
     */
    public boolean canCreateCourse(User user) {
        if (user == null) {
            return false;
        }
        if (user.isPro()) {
            return true;
        }
        if (user.getId() != null) {
            return countCoursesByUser(user) < MAX_COURSES_FREE;
        }
        return countCourses() < MAX_COURSES_FREE;
    }

    /**
     * Đếm số lượng môn học do một người dùng cụ thể sở hữu
     */
    public long countCoursesByUser(User user) {
        if (user == null || user.getId() == null) {
            return 0;
        }
        return courseRepository.countByUserId(user.getId());
    }

    public Course saveCourse(Course course) {
        return saveCourse(course, null);
    }

    public Course saveCourse(Course course, User user) {
        if (course.getCode() == null || course.getCode().trim().isBlank()) {
            throw new IllegalArgumentException("Mã môn học không được để trống!");
        }
        if (course.getName() == null || course.getName().trim().isBlank()) {
            throw new IllegalArgumentException("Tên môn học không được để trống!");
        }

        // Kiểm tra hạn ngạch gói cước nếu là tạo mới môn học
        if (course.getId() == null && user != null && !canCreateCourse(user)) {
            throw new IllegalStateException("Bạn đã đạt giới hạn " + MAX_COURSES_FREE
                    + " môn học của Gói Tự Học (Free). Vui lòng nâng cấp lên Gói Pro để tạo không giới hạn môn học!");
        }

        course.setCode(course.getCode().trim().toUpperCase());
        course.setName(course.getName().trim());

        // Validate trùng code theo từng người dùng
        if (user != null && user.getId() != null) {
            if (course.getId() == null) {
                if (courseRepository.existsByCodeIgnoreCaseAndUserId(course.getCode(), user.getId())) {
                    throw new IllegalArgumentException("Mã môn học '" + course.getCode() + "' đã tồn tại trong danh sách môn của bạn!");
                }
            } else {
                if (courseRepository.existsByCodeIgnoreCaseAndUserIdAndIdNot(course.getCode(), user.getId(), course.getId())) {
                    throw new IllegalArgumentException("Mã môn học '" + course.getCode() + "' đã thuộc về một môn khác của bạn!");
                }
            }
            course.setUser(user);
        } else {
            // Trường hợp fallback khi chưa có user hoặc test nội bộ
            if (course.getId() == null) {
                if (courseRepository.existsByCodeIgnoreCase(course.getCode())) {
                    throw new IllegalArgumentException("Mã môn học '" + course.getCode() + "' đã tồn tại trong hệ thống!");
                }
            } else {
                if (courseRepository.existsByCodeIgnoreCaseAndIdNot(course.getCode(), course.getId())) {
                    throw new IllegalArgumentException("Mã môn học '" + course.getCode() + "' đã thuộc về một môn khác!");
                }
            }
            if (user != null) {
                course.setUser(user);
            }
        }

        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, Course updatedData, User user) {
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + id));

        // Kiểm tra quyền sở hữu (Ownership Validation)
        if (user != null && !user.isAdmin()) {
            if (existing.getUser() != null && !existing.getUser().getId().equals(user.getId())) {
                throw new SecurityException("Bạn không có quyền chỉnh sửa môn học của người khác!");
            }
        }

        String newCode = updatedData.getCode().trim().toUpperCase();
        if (user != null && user.getId() != null) {
            if (courseRepository.existsByCodeIgnoreCaseAndUserIdAndIdNot(newCode, user.getId(), id)) {
                throw new IllegalArgumentException("Mã môn học '" + newCode + "' đã tồn tại ở môn khác của bạn!");
            }
        } else {
            if (courseRepository.existsByCodeIgnoreCaseAndIdNot(newCode, id)) {
                throw new IllegalArgumentException("Mã môn học '" + newCode + "' đã tồn tại ở môn khác!");
            }
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
    public Course updateCourse(Long id, Course updatedData) {
        return updateCourse(id, updatedData, null);
    }

    @Transactional
    public void deleteCourse(Long id, User user) {
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + id));

        // Kiểm tra quyền sở hữu (Ownership Validation)
        if (user != null && !user.isAdmin()) {
            if (existing.getUser() != null && !existing.getUser().getId().equals(user.getId())) {
                throw new SecurityException("Bạn không có quyền xóa môn học của người khác!");
            }
        }

        chunkRepository.deleteAllByCourseId(id);
        courseRepository.deleteById(id);
    }

    @Transactional
    public void deleteCourse(Long id) {
        deleteCourse(id, null);
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
