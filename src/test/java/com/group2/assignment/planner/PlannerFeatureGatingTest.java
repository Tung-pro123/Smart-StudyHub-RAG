package com.group2.assignment.planner;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.planner.dto.PlannerPageDto;
import com.group2.assignment.planner.dto.StudyPlanRequestDto;
import com.group2.assignment.planner.dto.TaskToggleResultDto;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
        PlannerPageDto<StudyPlan> emptyPage = PlannerPageDto.<StudyPlan>builder()
                .items(List.of())
                .page(0)
                .size(10)
                .totalItems(0)
                .totalPages(0)
                .hasPrev(false)
                .hasNext(false)
                .build();
        when(studyPlannerService.getRecentPlans(any(), anyInt(), anyInt())).thenReturn(emptyPage);
        when(studyPlannerService.getPlansByCourseId(anyLong(), any(), anyInt(), anyInt())).thenReturn(emptyPage);
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
    @DisplayName("Trang Planner trả thông tin phân trang")
    void plannerPageExposesPagination() throws Exception {
        PlannerPageDto<StudyPlan> page = PlannerPageDto.<StudyPlan>builder()
                .items(List.of())
                .page(1)
                .size(10)
                .totalItems(25)
                .totalPages(3)
                .hasPrev(true)
                .hasNext(true)
                .build();
        when(studyPlannerService.getRecentPlans(any(), eq(1), eq(10))).thenReturn(page);

        mvc.perform(get("/planner")
                        .param("page", "1")
                        .param("size", "10")
                        .session(sessionFor("pro_page", "ROLE_STUDENT", "PRO")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 3))
                .andExpect(model().attribute("hasPrev", true))
                .andExpect(model().attribute("hasNext", true));
    }

    @Test
    @DisplayName("Danh sách kế hoạch render card tóm tắt và phân trang")
    void plannerRendersPlanCardsAndPagination() throws Exception {
        StudyPlan plan = StudyPlan.builder()
                .id(1L)
                .title("Kế hoạch ôn thi HSF302")
                .status("ACTIVE")
                .progressPercent(40)
                .hoursPerWeek(6)
                .targetGrade("B")
                .createdAt(java.time.LocalDateTime.now())
                .course(Course.builder().code("HSF302").name("JPA and Spring Framework").build())
                .build();
        PlannerPageDto<StudyPlan> page = PlannerPageDto.<StudyPlan>builder()
                .items(List.of(plan))
                .page(0)
                .size(10)
                .totalItems(12)
                .totalPages(2)
                .hasPrev(false)
                .hasNext(true)
                .build();
        when(studyPlannerService.getRecentPlans(any(), anyInt(), anyInt())).thenReturn(page);

        mvc.perform(get("/planner").session(sessionFor("pro_cards", "ROLE_STUDENT", "PRO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Kế hoạch ôn thi HSF302")))
                .andExpect(content().string(containsString("40%")))
                .andExpect(content().string(containsString("page=1")));
    }

    @Test
    @DisplayName("Toggle nhiệm vụ trả JSON tiến độ realtime")
    void toggleTaskReturnsProgressJson() throws Exception {
        when(studyPlannerService.toggleTaskCompletion(anyLong(), any()))
                .thenReturn(TaskToggleResultDto.builder()
                        .taskId(5L)
                        .completed(true)
                        .progressPercent(50)
                        .planStatus("ACTIVE")
                        .build());

        mvc.perform(post("/planner/tasks/5/toggle")
                        .session(sessionFor("pro_toggle", "ROLE_STUDENT", "PRO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(5))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.progressPercent").value(50))
                .andExpect(jsonPath("$.planStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("Toggle nhiệm vụ không thuộc quyền trả 403")
    void toggleTaskForbiddenWhenNotOwner() throws Exception {
        when(studyPlannerService.toggleTaskCompletion(anyLong(), any())).thenReturn(null);

        mvc.perform(post("/planner/tasks/99/toggle")
                        .session(sessionFor("pro_toggle_forbidden", "ROLE_STUDENT", "PRO")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Trang chi tiết hiển thị người phụ trách nhiệm vụ")
    void viewShowsAssignee() throws Exception {
        StudyPlan plan = StudyPlan.builder()
                .id(77L)
                .title("Kế hoạch nhóm")
                .status("ACTIVE")
                .progressPercent(0)
                .targetGrade("A")
                .goal("Mục tiêu điểm A")
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
