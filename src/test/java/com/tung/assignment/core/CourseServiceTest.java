package com.tung.assignment.core;

import com.tung.assignment.core.dto.CourseCardDto;
import com.tung.assignment.core.entity.Course;
import com.tung.assignment.core.repository.ChunkRepository;
import com.tung.assignment.core.repository.CourseRepository;
import com.tung.assignment.core.service.CourseService;
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
}
