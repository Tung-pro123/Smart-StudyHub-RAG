package com.group2.assignment.core.config;

import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    public void run(String... args) {
        log.info("Bắt đầu khởi tạo & liên kết dữ liệu môn học demo theo từng tài khoản (Data Isolation)...");

        User student = userRepository.findByUsernameIgnoreCase("student").orElse(null);
        User proStudent = userRepository.findByUsernameIgnoreCase("pro_student").orElse(null);
        User squadLeader = userRepository.findByUsernameIgnoreCase("squad_leader").orElse(null);

        // 1. Gán môn HSF302 và MAS291 cho student (Gói Free: 2 môn, tạo thêm 1 môn nữa là đạt hạn mức tối đa 3 môn)
        if (student != null) {
            assignOrCreateCourse("HSF302", "JPA and Spring Framework",
                    "Lập trình ứng dụng web nâng cao với Spring Boot 3, Spring Data JPA, Thymeleaf và RAG AI.",
                    "from-indigo-600 to-purple-600", "fa-laptop-code", "Fall 2026", 3, student);

            assignOrCreateCourse("MAS291", "Probability and Statistics",
                    "Xác suất thống kê ứng dụng trong khoa học dữ liệu, kiểm định giả thuyết và machine learning.",
                    "from-emerald-600 to-teal-600", "fa-chart-pie", "Fall 2026", 3, student);
        }

        // 2. Tạo 4 môn học cho pro_student (Gói Pro: không giới hạn môn học)
        if (proStudent != null) {
            assignOrCreateCourse("FER202", "Front-End Web Development with React",
                    "Xây dựng giao diện Single Page Application hiện đại với ReactJS, Tailwind CSS và State Management.",
                    "from-blue-600 to-cyan-600", "fa-brands fa-react", "Fall 2026", 3, proStudent);

            assignOrCreateCourse("SWE201", "Introduction to Software Engineering",
                    "Quy trình công nghệ phần mềm chuyên nghiệp, mô hình Waterfall, Agile/Scrum và kiểm thử.",
                    "from-violet-600 to-fuchsia-600", "fa-layer-group", "Fall 2026", 3, proStudent);

            assignOrCreateCourse("DBI202", "Introduction to Databases",
                    "Thiết kế cơ sở dữ liệu quan hệ, chuẩn hóa dữ liệu 3NF, BCNF và tối ưu hóa truy vấn SQL nâng cao.",
                    "from-amber-500 to-orange-600", "fa-database", "Fall 2026", 3, proStudent);

            assignOrCreateCourse("ITE302c", "Ethics in Information Technology",
                    "Đạo đức nghề nghiệp và pháp lý trong công nghệ thông tin, an toàn thông tin và AI ethics.",
                    "from-teal-600 to-cyan-700", "fa-scale-balanced", "Fall 2026", 3, proStudent);
        }

        // 3. Tạo 2 môn học cho squad_leader (Gói Study Squad)
        if (squadLeader != null) {
            assignOrCreateCourse("EXE101", "Experiential Entrepreneurship",
                    "Dự án khởi nghiệp đổi mới sáng tạo, nghiên cứu thị trường và pitching dự án nhóm.",
                    "from-rose-500 to-pink-600", "fa-lightbulb", "Fall 2026", 3, squadLeader);

            assignOrCreateCourse("LAB211", "OOP Java Programming Lab",
                    "Thực hành lập trình hướng đối tượng Java thuần, thiết kế mẫu Design Pattern và tối ưu mã nguồn.",
                    "from-sky-600 to-indigo-700", "fa-diagram-project", "Fall 2026", 3, squadLeader);
        }

        log.info("Hoàn tất thiết lập môn học demo cho tất cả các gói cước!");
    }

    private void assignOrCreateCourse(String code, String name, String desc, String color, String icon,
                                      String semester, int credits, User owner) {
        Course existing = courseRepository.findByCodeIgnoreCase(code).orElse(null);
        if (existing != null) {
            // Nếu môn đã tồn tại nhưng chưa có chủ sở hữu, gán cho owner
            if (existing.getUser() == null) {
                existing.setUser(owner);
                courseRepository.save(existing);
            }
        } else {
            // Nếu chưa có, tạo mới
            courseRepository.save(Course.builder()
                    .code(code)
                    .name(name)
                    .description(desc)
                    .color(color)
                    .icon(icon)
                    .semester(semester)
                    .credits(credits)
                    .user(owner)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}
