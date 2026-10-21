package com.group2.assignment.core;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Khách vãng lai truy cập GET / phải trả về view Landing Page")
    void testGuestAccessRootReturnsLanding() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("landing"));
    }

    @Test
    @DisplayName("Người dùng đã đăng nhập truy cập GET / phải tự động chuyển tiếp về /courses")
    void testLoggedInUserAccessRootRedirectsToCourses() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User fakeUser = User.builder()
                .username("student1")
                .fullName("Nguyen Van A")
                .role("STUDENT")
                .build();
        session.setAttribute(AuthController.SESSION_USER, fakeUser);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));
    }

    @Test
    @DisplayName("Truy cập alias GET /dashboard phải chuyển hướng về /courses")
    void testDashboardAliasRedirectsToCourses() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));
    }

    @Test
    @DisplayName("Truy cập GET /courses hiển thị giao diện danh sách môn học index")
    void testCoursesCatalogReturnsIndexView() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("courses"))
                .andExpect(model().attributeExists("totalCourses"));
    }
}
