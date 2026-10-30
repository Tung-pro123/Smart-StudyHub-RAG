package com.group2.assignment.planner;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.planner.entity.StudyPlan;
import com.group2.assignment.planner.service.StudyPlannerService;
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
        when(studyPlannerService.getRecentPlans()).thenReturn(List.of());
        when(studyPlannerService.getPlansByCourseId(anyLong())).thenReturn(List.of());
    }

    private MockHttpSession sessionFor(String username, String role, String tier) {
        User user = User.builder()
                .username(username)
                .role(role)
                .subscriptionTier(tier)
                .build();
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

        verify(studyPlannerService, never()).generateStudyPlan(any());
    }

    @Test
    @DisplayName("Gói PRO được phép sinh kế hoạch và chuyển tới trang chi tiết")
    void proUserCanGeneratePlan() throws Exception {
        when(studyPlannerService.generateStudyPlan(any()))
                .thenReturn(StudyPlan.builder().id(42L).title("Kế hoạch ôn thi HSF302").build());

        mvc.perform(post("/planner/generate")
                        .session(sessionFor("pro_generate", "ROLE_STUDENT", "PRO"))
                        .param("courseId", String.valueOf(course.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/planner/42"));

        verify(studyPlannerService).generateStudyPlan(any());
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
}
