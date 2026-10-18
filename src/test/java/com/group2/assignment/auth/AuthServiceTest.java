package com.group2.assignment.auth;

import com.group2.assignment.auth.dto.LoginRequestDto;
import com.group2.assignment.auth.dto.RegisterRequestDto;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.service.AuthService;
import com.group2.assignment.auth.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("Đăng ký tài khoản sinh viên mới thành công")
    void testRegisterSuccess() {
        RegisterRequestDto dto = RegisterRequestDto.builder()
                .username("truongfpt")
                .email("truongfpt@fpt.edu.vn")
                .fullName("Đinh Nhật Trường")
                .password("password123")
                .confirmPassword("password123")
                .build();

        User user = authService.register(dto);
        assertNotNull(user.getId());
        assertEquals("truongfpt", user.getUsername());
        assertNotEquals("password123", user.getPassword(), "Mật khẩu phải được băm BCrypt");
    }

    @Test
    @DisplayName("Đăng ký trùng username phải ném ngoại lệ")
    void testRegisterDuplicateUsernameThrows() {
        RegisterRequestDto dto = RegisterRequestDto.builder()
                .username("student") // Trùng username mặc định
                .email("student_new@fpt.edu.vn")
                .password("123456")
                .confirmPassword("123456")
                .build();

        assertThrows(IllegalArgumentException.class, () -> authService.register(dto));
    }

    @Test
    @DisplayName("Đăng nhập thành công và sinh JWT token hợp lệ")
    void testLoginSuccessAndGenerateJwt() {
        LoginRequestDto dto = LoginRequestDto.builder()
                .username("student")
                .password("123456")
                .build();

        String token = authService.login(dto);
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("student", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Đăng nhập sai mật khẩu phải ném ngoại lệ")
    void testLoginWrongPasswordThrows() {
        LoginRequestDto dto = LoginRequestDto.builder()
                .username("student")
                .password("wrongpassword")
                .build();

        assertThrows(IllegalArgumentException.class, () -> authService.login(dto));
    }
}
