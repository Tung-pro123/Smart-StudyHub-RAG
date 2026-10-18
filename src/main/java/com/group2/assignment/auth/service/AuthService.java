package com.group2.assignment.auth.service;

import com.group2.assignment.auth.dto.ChangePasswordDto;
import com.group2.assignment.auth.dto.LoginRequestDto;
import com.group2.assignment.auth.dto.OAuth2UserInfo;
import com.group2.assignment.auth.dto.RegisterRequestDto;
import com.group2.assignment.auth.dto.UpdateProfileDto;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Đăng ký tài khoản sinh viên mới
     */
    public User register(RegisterRequestDto dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống!");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isBlank()) {
            throw new IllegalArgumentException("Email không được để trống!");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có tối thiểu 6 ký tự!");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp!");
        }

        String username = dto.getUsername().trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Tên đăng nhập '" + username + "' đã được sử dụng!");
        }

        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email '" + email + "' đã được sử dụng!");
        }

        User newUser = User.builder()
                .username(username)
                .email(email)
                .fullName(dto.getFullName() != null && !dto.getFullName().isBlank() ? dto.getFullName().trim() : username)
                .password(passwordEncoder.encode(dto.getPassword()))
                .role("ROLE_STUDENT")
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(newUser);
    }

    /**
     * Đăng nhập và tạo JWT token
     */
    public String login(LoginRequestDto dto) {
        if (dto.getUsername() == null || dto.getPassword() == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
        }

        String username = dto.getUsername().trim().toLowerCase();
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không chính xác!"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không chính xác!");
        }

        return jwtService.generateToken(user.getUsername(), user.getRole(), user.getFullName());
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    /**
     * Xử lý đăng nhập OAuth2 (Google):
     * - Tạo user mới nếu email chưa tồn tại
     * - Cập nhật avatar nếu có thay đổi
     * - Trả về User đã đăng nhập
     */
    public User processOAuth2Login(OAuth2UserInfo oauth2UserInfo) {
        // Tìm user theo email
        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(oauth2UserInfo.getEmail());

        User user;
        if (existingUser.isPresent()) {
            // User đã tồn tại (đăng ký thường) - cập nhật avatar từ Google
            user = existingUser.get();
            if (oauth2UserInfo.getAvatarUrl() != null && !oauth2UserInfo.getAvatarUrl().isBlank()) {
                user.setAvatar(oauth2UserInfo.getAvatarUrl());
            }
        } else {
            // User mới - tạo tài khoản mới
            String username = extractUsernameFromEmail(oauth2UserInfo.getEmail());
            // Đảm bảo username không trùng
            int suffix = 1;
            String baseUsername = username;
            while (userRepository.existsByUsernameIgnoreCase(username)) {
                username = baseUsername + suffix;
                suffix++;
            }

            user = User.builder()
                    .username(username)
                    .email(oauth2UserInfo.getEmail())
                    .fullName(oauth2UserInfo.getFullName() != null ? oauth2UserInfo.getFullName() : username)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Random password
                    .avatar(oauth2UserInfo.getAvatarUrl() != null ?
                            oauth2UserInfo.getAvatarUrl() :
                            "https://api.dicebear.com/7.x/bottts/svg?seed=" + username)
                    .role("ROLE_STUDENT")
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Trích xuất username từ email (phần trước @)
     */
    private String extractUsernameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "user" + System.currentTimeMillis();
        }
        String extracted = email.substring(0, email.indexOf("@"));
        // Thay thế ký tự đặc biệt
        return extracted.replaceAll("[^a-zA-Z0-9]", "");
    }

    /**
     * Cập nhật thông tin hồ sơ (Họ tên, Avatar)
     */
    public User updateProfile(String username, UpdateProfileDto dto) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));

        if (dto.getFullName() != null && !dto.getFullName().trim().isBlank()) {
            user.setFullName(dto.getFullName().trim());
        }

        if (dto.getAvatar() != null && !dto.getAvatar().trim().isBlank()) {
            user.setAvatar(dto.getAvatar().trim());
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Đổi mật khẩu tài khoản sinh viên
     */
    public void changePassword(String username, ChangePasswordDto dto) {
        if (dto.getOldPassword() == null || dto.getOldPassword().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại!");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có tối thiểu 6 ký tự!");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp!");
        }

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác!");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu hiện tại!");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Khởi tạo tài khoản sinh viên mẫu mặc định để demo
     */
    @PostConstruct
    public void initDefaultUser() {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .username("student")
                    .email("student@fpt.edu.vn")
                    .fullName("Nguyễn Văn Sinh Viên")
                    .password(passwordEncoder.encode("123456"))
                    .role("ROLE_STUDENT")
                    .createdAt(LocalDateTime.now())
                    .build());

            userRepository.save(User.builder()
                    .username("truong")
                    .email("truong@fpt.edu.vn")
                    .fullName("Ngô Nhật Trường")
                    .password(passwordEncoder.encode("123456"))
                    .role("ROLE_ADMIN")
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}
