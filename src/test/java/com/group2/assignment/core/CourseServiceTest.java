package com.group2.assignment.core;

import com.group2.assignment.auth.entity.User;
import com.group2.assignment.core.dto.CourseCardDto;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.ChunkRepository;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.core.service.CourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    @Test
    @DisplayName("Khởi tạo dữ liệu mẫu và đếm số lượng môn học")
    void testSampleDataInit() {
        long count = courseService.countCourses();
        assertTrue(count >= 2, "Hệ thống phải có ít nhất 2 môn học mẫu");
    }

    @Test
    @DisplayName("Thêm môn học mới thành công")
    void testSaveCourseSuccess() {
        Course newCourse = Course.builder()
                .code("PRN211")
                .name("Basic Cross-Platform Application Programming with .NET")
                .description("Lập trình C# .NET và kiến trúc microservices.")
                .semester("Fall 2026")
                .credits(3)
                .build();

        Course saved = courseService.saveCourse(newCourse);
        assertNotNull(saved.getId());
        assertEquals("PRN211", saved.getCode());
    }

    @Test
    @DisplayName("Không cho phép thêm môn học trùng mã code")
    void testDuplicateCourseCodeThrowsException() {
        Course duplicate = Course.builder()
                .code("hsf302") // lowercase trùng với HSF302
                .name("Duplicate Test")
                .build();

        assertThrows(IllegalArgumentException.class, () -> courseService.saveCourse(duplicate),
                "Phải ném ngoại lệ khi trùng mã môn học");
    }

    @Test
    @DisplayName("Cập nhật thông tin môn học thành công")
    void testUpdateCourse() {
        Course course = courseRepository.findByCodeIgnoreCase("HSF302").orElseThrow();
        Course updateData = Course.builder()
                .code("HSF302")
                .name("JPA and Spring Framework Advanced 2026")
                .description("Nội dung cập nhật nâng cao")
                .semester("Fall 2026")
                .credits(4)
                .color("from-blue-600 to-indigo-600")
                .icon("fa-laptop-code")
                .build();

        Course updated = courseService.updateCourse(course.getId(), updateData);
        assertEquals("JPA and Spring Framework Advanced 2026", updated.getName());
        assertEquals(4, updated.getCredits());
    }

    @Test
    @DisplayName("Tìm kiếm môn học theo từ khóa")
    void testSearchCoursesByKeyword() {
        List<CourseCardDto> results = courseService.getAllCoursesWithStats("Spring", "ALL");
        assertFalse(results.isEmpty(), "Phải tìm thấy môn HSF302 chứa từ khóa Spring");
        assertEquals("HSF302", results.get(0).getCode());
    }

    @Test
    @DisplayName("Lấy chi tiết môn học kèm thống kê Chunks")
    void testGetCourseWithStats() {
        Course course = courseRepository.findByCodeIgnoreCase("HSF302").orElseThrow();
        CourseCardDto dto = courseService.getCourseWithStats(course.getId());
        assertNotNull(dto);
        assertEquals("HSF302", dto.getCode());
        assertTrue(dto.getChunkCount() >= 0);
    }

    @Test
    @DisplayName("Gói Free vượt quá giới hạn 3 môn sẽ bị chặn tạo môn mới")
    void testFreeUserQuotaExceededThrowsException() {
        // Sample data có sẵn 3 môn (HSF302, MAS291, SWP391)
        assertTrue(courseService.countCourses() >= CourseService.MAX_COURSES_FREE);

        User freeUser = User.builder()
                .username("student_free")
                .role("ROLE_STUDENT")
                .subscriptionTier("FREE")
                .build();

        // Kiểm tra hàm canCreateCourse trả về false
        assertFalse(courseService.canCreateCourse(freeUser),
                "Người dùng Free khi đã có >= 3 môn thì canCreateCourse phải là false");

        // Cố tình lưu môn thứ 4 với user Free phải ném IllegalStateException
        Course fourthCourse = Course.builder()
                .code("MAE101")
                .name("Mathematics for Engineering")
                .semester("Fall 2026")
                .credits(3)
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                courseService.saveCourse(fourthCourse, freeUser));

        assertTrue(ex.getMessage().contains("đạt giới hạn 3 môn học"),
                "Thông báo lỗi phải chứa thông tin đạt giới hạn 3 môn học");
    }

    @Test
    @DisplayName("Gói Pro và Admin được phép tạo môn học không giới hạn")
    void testProUserCanCreateUnlimitedCourses() {
        User proUser = User.builder()
                .username("student_pro")
                .role("ROLE_STUDENT")
                .subscriptionTier("PRO")
                .build();

        assertTrue(courseService.canCreateCourse(proUser), "Người dùng Pro phải được tạo môn học");

        Course extraCourse = Course.builder()
                .code("PRJ301")
                .name("Java Web Application Development")
                .semester("Fall 2026")
                .credits(3)
                .build();

        Course saved = courseService.saveCourse(extraCourse, proUser);
        assertNotNull(saved.getId());
        assertEquals("PRJ301", saved.getCode());
    }
}
