package com.group2.assignment.auth;

import com.group2.assignment.auth.dto.ChangePasswordDto;
import com.group2.assignment.auth.dto.LoginRequestDto;
import com.group2.assignment.auth.dto.RegisterRequestDto;
import com.group2.assignment.auth.dto.UpdateProfileDto;
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
                .fullName("Ngô Nhật Trường")
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

    @Test
    @DisplayName("Cập nhật thông tin hồ sơ thành công")
    void testUpdateProfileSuccess() {
        UpdateProfileDto dto = UpdateProfileDto.builder()
                .fullName("Ngô Nhật Trường Pro")
                .avatar("https://api.dicebear.com/7.x/bottts/svg?seed=truong_updated")
                .build();

        User updatedUser = authService.updateProfile("truong", dto);
        assertEquals("Ngô Nhật Trường Pro", updatedUser.getFullName());
        assertEquals("https://api.dicebear.com/7.x/bottts/svg?seed=truong_updated", updatedUser.getAvatar());
    }

    @Test
    @DisplayName("Đổi mật khẩu thành công và xác thực được với mật khẩu mới")
    void testChangePasswordSuccess() {
        ChangePasswordDto dto = ChangePasswordDto.builder()
                .oldPassword("123456")
                .newPassword("newSecretPassword2026")
                .confirmPassword("newSecretPassword2026")
                .build();

        authService.changePassword("student", dto);

        // Đăng nhập lại với mật khẩu mới
        LoginRequestDto loginDto = LoginRequestDto.builder()
                .username("student")
                .password("newSecretPassword2026")
                .build();

        String token = authService.login(loginDto);
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("Đổi mật khẩu sai mật khẩu cũ phải ném ngoại lệ")
    void testChangePasswordWrongOldPasswordThrows() {
        ChangePasswordDto dto = ChangePasswordDto.builder()
                .oldPassword("wrongOldPass")
                .newPassword("newSecretPassword2026")
                .confirmPassword("newSecretPassword2026")
                .build();

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword("student", dto));
    }

    @Test
    @DisplayName("Đổi mật khẩu xác nhận không khớp phải ném ngoại lệ")
    void testChangePasswordMismatchedConfirmThrows() {
        ChangePasswordDto dto = ChangePasswordDto.builder()
                .oldPassword("123456")
                .newPassword("newSecretPassword2026")
                .confirmPassword("mismatchPassword")
                .build();

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword("student", dto));
    }
}
