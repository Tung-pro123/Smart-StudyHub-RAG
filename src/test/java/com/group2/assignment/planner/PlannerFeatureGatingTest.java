package com.group2.assignment.planner;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.planner.dto.StudyPlanRequestDto;
import com.group2.assignment.planner.entity.StudyPlan;
import com.group2.assignment.planner.entity.StudyTask;
import com.group2.assignment.planner.service.StudyPlannerService;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlannerFeatureGatingTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private StudyPlannerService studyPlannerService;

    private Course course;

    @BeforeEach
    void setUp() {
        course = courseRepository.findByCodeIgnoreCase("HSF302")
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .code("HSF302")
                        .name("JPA and Spring Framework")
                        .build()));
        when(studyPlannerService.getRecentPlans(any())).thenReturn(List.of());
        when(studyPlannerService.getPlansByCourseId(anyLong(), any())).thenReturn(List.of());
    }

    private MockHttpSession sessionFor(String username, String role, String tier) {
        User user = userRepository.save(User.builder()
                .username(username)
                .email(username + "@test.local")
                .fullName(username)
                .password("123456")
                .role(role)
                .subscriptionTier(tier)
                .build());
        course.setUser(user);
        courseRepository.save(course);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, user);
        return session;
    }

    @Test
    @DisplayName("Gói FREE thấy lớp phủ khóa và CTA nâng cấp tại /planner")
    void freeUserSeesLockedOverlay() throws Exception {
        mvc.perform(get("/planner").session(sessionFor("free_planner", "ROLE_STUDENT", "FREE")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("plannerLocked", true))
                .andExpect(model().attribute("isPro", false))
                .andExpect(content().string(containsString("AI Study Planner đang bị khóa")))
                .andExpect(content().string(containsString("Nâng cấp Gói Pro chỉ 49k/tháng")));
    }

    @Test
    @DisplayName("Gói FREE bị chặn POST /planner/generate và không gọi service")
    void freeUserCannotGeneratePlan() throws Exception {
        mvc.perform(post("/planner/generate")
                        .session(sessionFor("free_generate", "ROLE_STUDENT", "FREE"))
                        .param("courseId", String.valueOf(course.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/planner"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("showUpgradeModal", true));

        verify(studyPlannerService, never()).generateStudyPlan(any(), any());
    }

    @Test
    @DisplayName("Gói PRO được phép sinh kế hoạch và chuyển tới trang chi tiết")
    void proUserCanGeneratePlan() throws Exception {
        when(studyPlannerService.generateStudyPlan(any(), any()))
                .thenReturn(StudyPlan.builder().id(42L).title("Kế hoạch ôn thi HSF302").build());

        mvc.perform(post("/planner/generate")
                        .session(sessionFor("pro_generate", "ROLE_STUDENT", "PRO"))
                        .param("courseId", String.valueOf(course.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/planner/42"));

        verify(studyPlannerService).generateStudyPlan(any(), any());
    }

    @Test
    @DisplayName("Gói CAMPUS được mở khóa Planner")
    void campusUserIsUnlocked() throws Exception {
        mvc.perform(get("/planner").session(sessionFor("campus_planner", "ROLE_STUDENT", "CAMPUS")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("plannerLocked", false))
                .andExpect(model().attribute("isPro", true));
    }

    @Test
    @DisplayName("ADMIN được xem Planner như gói PRO")
    void adminUserIsUnlocked() throws Exception {
        mvc.perform(get("/planner").session(sessionFor("admin_planner", "ROLE_ADMIN", "PRO")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("plannerLocked", false))
                .andExpect(model().attribute("isPro", true));
    }

    @Test
    @DisplayName("Trang lịch sử cũng nhận đúng trạng thái gói cước")
    void historyPageCarriesSubscriptionAttributes() throws Exception {
        mvc.perform(get("/planner/history/{courseId}", course.getId())
                        .session(sessionFor("pro_history", "ROLE_STUDENT", "PRO")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("plannerLocked", false))
                .andExpect(model().attribute("isPro", true));
    }

    @Test
    @DisplayName("Khách chưa đăng nhập bị interceptor chuyển về trang login")
    void guestIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/planner"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=unauthorized"));
    }

    @Test
    @DisplayName("Gói SQUAD mở Planner và hiển thị ô thành viên nhóm")
    void squadUserSeesMemberInput() throws Exception {
        mvc.perform(get("/planner").session(sessionFor("squad_planner", "ROLE_STUDENT", "SQUAD")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("plannerLocked", false))
                .andExpect(model().attribute("isPro", true))
                .andExpect(content().string(containsString("Thành viên nhóm (SQUAD)")));
    }

    @Test
    @DisplayName("Gói SQUAD gửi danh sách thành viên vào service")
    void squadUserSendsAssignees() throws Exception {
        MockHttpSession session = sessionFor("squad_generate", "ROLE_STUDENT", "SQUAD");
        when(studyPlannerService.generateStudyPlan(any(), any()))
                .thenReturn(StudyPlan.builder().id(99L).title("Kế hoạch nhóm").build());

        mvc.perform(post("/planner/generate")
                        .session(session)
                        .param("courseId", String.valueOf(course.getId()))
                        .param("squadMembers", "an, binh"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/planner/99"));

        ArgumentCaptor<StudyPlanRequestDto> captor = ArgumentCaptor.forClass(StudyPlanRequestDto.class);
        verify(studyPlannerService).generateStudyPlan(captor.capture(), any());
        assertEquals(List.of("an", "binh"), captor.getValue().getSquadMembers());
    }

    @Test
    @DisplayName("Trang chi tiết hiển thị người phụ trách nhiệm vụ")
    void viewShowsAssignee() throws Exception {
        StudyPlan plan = StudyPlan.builder()
                .id(77L)
                .title("Kế hoạch nhóm")
                .status("ACTIVE")
                .progressPercent(0)
                .build();
        StudyTask task = StudyTask.builder()
                .id(1L)
                .weekNumber(1)
                .dayOfWeek("Thứ 3")
                .taskTitle("Ôn tập Spring Core")
                .durationMinutes(60)
                .isCompleted(false)
                .assignee("an")
                .build();
        when(studyPlannerService.getPlanById(anyLong(), any())).thenReturn(java.util.Optional.of(plan));
        when(studyPlannerService.getTasksByPlanId(77L)).thenReturn(List.of(task));

        mvc.perform(get("/planner/77").session(sessionFor("pro_view", "ROLE_STUDENT", "PRO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Phụ trách:")))
                .andExpect(content().string(containsString("an")));
    }

    @Test
    @DisplayName("Sinh viên chỉ thấy môn học thuộc tài khoản của mình")
    void studentOnlySeesOwnCourses() throws Exception {
        User other = userRepository.save(User.builder()
                .username("other_owner")
                .email("other_owner@test.local")
                .fullName("Other Owner")
                .password("123456")
                .role("ROLE_STUDENT")
                .subscriptionTier("PRO")
                .build());
        Course otherCourse = courseRepository.save(Course.builder()
                .code("OTHER302")
                .name("Other Course")
                .user(other)
                .build());

        mvc.perform(get("/planner").session(sessionFor("pro_isolation", "ROLE_STUDENT", "PRO")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("courses", hasItem(course)))
                .andExpect(model().attribute("courses", not(hasItem(otherCourse))));
    }

    @Test
    @DisplayName("Sinh viên không thể tạo kế hoạch cho môn học của người khác")
    void studentCannotGenerateForOtherCourse() throws Exception {
        User other = userRepository.save(User.builder()
                .username("other_generate")
                .email("other_generate@test.local")
                .fullName("Other Generate")
                .password("123456")
                .role("ROLE_STUDENT")
                .subscriptionTier("PRO")
                .build());
        Course otherCourse = courseRepository.save(Course.builder()
                .code("OTHER303")
                .name("Other Generate Course")
                .user(other)
                .build());

        mvc.perform(post("/planner/generate")
                        .session(sessionFor("pro_generate_guard", "ROLE_STUDENT", "PRO"))
                        .param("courseId", String.valueOf(otherCourse.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/planner"))
                .andExpect(flash().attributeExists("error"));

        verify(studyPlannerService, never()).generateStudyPlan(any(), any());
    }
}
