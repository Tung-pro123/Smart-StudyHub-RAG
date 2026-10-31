package com.group2.assignment.core;

import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

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
                .semester("Fall 2026")
                .credits(3)
                .build();

        assertThrows(IllegalArgumentException.class, () -> courseService.saveCourse(duplicate));
    }

    @Test
    @DisplayName("Tìm kiếm môn học theo từ khóa")
    void testSearchCoursesByKeyword() {
        List<CourseCardDto> results = courseService.getAllCoursesWithStats("Spring", null);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(c -> c.getCode().equalsIgnoreCase("HSF302")));
    }

    @Test
    @DisplayName("Lọc môn học theo học kỳ")
    void testFilterCoursesBySemester() {
        List<CourseCardDto> results = courseService.getAllCoursesWithStats(null, "Fall 2026");
        assertFalse(results.isEmpty());
    }

    @Test
    @DisplayName("Cập nhật thông tin môn học thành công")
    void testUpdateCourse() {
        Course course = courseRepository.findByCodeIgnoreCase("HSF302").orElseThrow();
        Course updateData = Course.builder()
                .code("HSF302")
                .name("Updated JPA and Spring Boot")
                .description("Updated description")
                .semester("Spring 2027")
                .credits(4)
                .build();

        Course updated = courseService.updateCourse(course.getId(), updateData);
        assertEquals("Updated JPA and Spring Boot", updated.getName());
        assertEquals("Spring 2027", updated.getSemester());
        assertEquals(4, updated.getCredits());
    }

    @Test
    @DisplayName("Xóa môn học và cascade chunks liên quan")
    void testDeleteCourse() {
        Course tempCourse = Course.builder()
                .code("TEMP101")
                .name("Temporary Course to Delete")
                .semester("Fall 2026")
                .credits(2)
                .build();
        Course saved = courseRepository.save(tempCourse);
        Long id = saved.getId();

        courseService.deleteCourse(id);
        assertTrue(courseRepository.findById(id).isEmpty());
        assertEquals(0, chunkRepository.countByCourseId(id));
    }

    @Test
    @DisplayName("Lấy chi tiết môn học kèm thống kê chunks")
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
                .code("PRJ301_PRO")
                .name("Java Web Application Development Pro")
                .semester("Fall 2026")
                .credits(3)
                .build();

        Course saved = courseService.saveCourse(extraCourse, proUser);
        assertNotNull(saved.getId());
        assertEquals("PRJ301_PRO", saved.getCode());
    }

    @Test
    @DisplayName("Cách ly dữ liệu: Hai sinh viên khác nhau đều có thể tạo môn học cùng mã code SWP490")
    void testCourseDataIsolationBetweenUsers() {
        User userA = userRepository.save(User.builder()
                .username("student_iso_a")
                .email("student_a@fpt.edu.vn")
                .fullName("Sinh Vien A")
                .password("123456")
                .role("ROLE_STUDENT")
                .subscriptionTier("FREE")
                .build());

        User userB = userRepository.save(User.builder()
                .username("student_iso_b")
                .email("student_b@fpt.edu.vn")
                .fullName("Sinh Vien B")
                .password("123456")
                .role("ROLE_STUDENT")
                .subscriptionTier("FREE")
                .build());

        Course courseA = Course.builder()
                .code("SWP490")
                .name("Graduation Capstone Project A")
                .semester("Fall 2026")
                .credits(3)
                .build();
        Course savedA = courseService.saveCourse(courseA, userA);
        assertNotNull(savedA.getId());
        assertEquals("SWP490", savedA.getCode());
        assertEquals(userA.getId(), savedA.getUser().getId());

        // Sinh viên B cũng tạo môn cùng mã SWP490 -> Phải thành công!
        Course courseB = Course.builder()
                .code("SWP490")
                .name("Graduation Capstone Project B")
                .semester("Fall 2026")
                .credits(3)
                .build();
        Course savedB = courseService.saveCourse(courseB, userB);
        assertNotNull(savedB.getId());
        assertEquals("SWP490", savedB.getCode());
        assertEquals(userB.getId(), savedB.getUser().getId());

        // Kiểm tra danh sách hiển thị của User A
        List<CourseCardDto> listA = courseService.getAllCoursesWithStats(null, null, userA);
        assertTrue(listA.stream().anyMatch(c -> c.getName().equals("Graduation Capstone Project A")));
        assertFalse(listA.stream().anyMatch(c -> c.getName().equals("Graduation Capstone Project B")));

        // Kiểm tra danh sách hiển thị của User B
        List<CourseCardDto> listB = courseService.getAllCoursesWithStats(null, null, userB);
        assertTrue(listB.stream().anyMatch(c -> c.getName().equals("Graduation Capstone Project B")));
        assertFalse(listB.stream().anyMatch(c -> c.getName().equals("Graduation Capstone Project A")));
    }

    @Test
    @DisplayName("Bảo mật quyền sở hữu: Sinh viên không được phép sửa hoặc xóa môn học của người khác")
    void testOwnershipSecurityEnforced() {
        User owner = userRepository.save(User.builder()
                .username("student_owner")
                .email("owner@fpt.edu.vn")
                .fullName("Owner Student")
                .password("123456")
                .role("ROLE_STUDENT")
                .subscriptionTier("FREE")
                .build());

        User intruder = userRepository.save(User.builder()
                .username("student_intruder")
                .email("intruder@fpt.edu.vn")
                .fullName("Intruder Student")
                .password("123456")
                .role("ROLE_STUDENT")
                .subscriptionTier("FREE")
                .build());

        Course course = Course.builder()
                .code("PRM392")
                .name("Mobile Programming")
                .semester("Fall 2026")
                .credits(3)
                .build();
        Course saved = courseService.saveCourse(course, owner);

        // Kẻ xâm nhập cố tình sửa môn của owner -> Ném SecurityException
        Course hackedCourse = Course.builder().code("PRM392").name("Hacked Name").build();
        assertThrows(SecurityException.class, () ->
                courseService.updateCourse(saved.getId(), hackedCourse, intruder));

        // Kẻ xâm nhập cố tình xóa môn của owner -> Ném SecurityException
        assertThrows(SecurityException.class, () ->
                courseService.deleteCourse(saved.getId(), intruder));

        // Chủ sở hữu xóa môn của chính mình -> Thành công
        assertDoesNotThrow(() -> courseService.deleteCourse(saved.getId(), owner));
    }
}
