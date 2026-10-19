# Google OAuth Login Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Thêm tính năng đăng nhập bằng Google OAuth2 vào module auth hiện tại của Trường.

**Architecture:** Tích hợp Spring Security OAuth2 Client với hệ thống JWT/Session hiện tại. Khi user đăng nhập Google thành công, tạo User entity mới nếu chưa tồn tại (theo email), sau đó tạo JWT session như đăng nhập thường.

**Tech Stack:** Spring Boot 3.3.4, Spring Security OAuth2 Client, JWT (jjwt 0.12.6)

**Spec:** N/A - Tính năng mới do Tùng đề xuất

---

## Global Constraints

- Giữ nguyên hệ thống JWT/Session hiện tại
- Không ảnh hưởng đến login/register thường
- User đăng nhập Google được tạo mới nếu email chưa tồn tại
- Avatar mặc định từ Google profile picture

## Review Focus

1. **OAuth2 redirect URI** - Phải khớp chính xác với Google Cloud Console
2. **Email exists** - Nếu email đã tồn tại (đăng ký thường), vẫn cho đăng nhập Google
3. **Session persistence** - JWT cookie được set sau OAuth2 callback thành công
4. **Error handling** - Xử lý trường hợp Google OAuth thất bại
5. **Security** - CSRF protection, state parameter validation

---

## File Structure

```
src/main/java/com/group2/assignment/auth/
├── config/
│   ├── SecurityConfig.java          # [CREATE] OAuth2 + Session config
│   └── WebMvcConfig.java            # [MODIFY] Thêm OAuth2 endpoints
├── dto/
│   └── OAuth2UserInfo.java          # [CREATE] DTO chứa thông tin Google user
├── service/
│   └── AuthService.java             # [MODIFY] Thêm method xử lý OAuth2
└── controller/
    └── AuthController.java          # [MODIFY] Thêm OAuth2 callback endpoints

src/main/resources/
├── application.properties            # [MODIFY] Thêm Google OAuth2 config
└── templates/auth/
    └── login.html                  # [MODIFY] Thêm nút "Login with Google"
```

---

## Tasks

### Task 1: Thêm Spring Security OAuth2 Client Dependency

**Files:**
- Modify: `pom.xml:92-92` (thêm sau spring-security-crypto)

- [ ] **Step 1: Thêm OAuth2 Client dependency vào pom.xml**

```xml
<!-- Spring Security OAuth2 Client: Đăng nhập Google OAuth -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

- [ ] **Step 2: Chạy Maven reload**

Run: `mvn dependency:resolve`
Expected: Tải thành công spring-security-oauth2-client

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "chore(auth): thêm spring-boot-starter-oauth2-client dependency"
```

---

### Task 2: Cấu Hình Google OAuth2 Trong Application Properties

**Files:**
- Modify: `src/main/resources/application.properties` (thêm vào cuối file)

- [ ] **Step 1: Thêm cấu hình Google OAuth2**

```properties
# ==========================================
# CẤU HÌNH GOOGLE OAUTH2
# ==========================================
# Link tạo OAuth2 Client: https://console.cloud.google.com/apis/credentials
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/google

spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v3/userinfo
spring.security.oauth2.client.provider.google.user-name-attribute=sub
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/application.properties
git commit -m "config(auth): thêm cấu hình Google OAuth2 trong application.properties"
```

---

### Task 3: Tạo OAuth2UserInfo DTO

**Files:**
- Create: `src/main/java/com/group2/assignment/auth/dto/OAuth2UserInfo.java`

- [ ] **Step 1: Tạo OAuth2UserInfo.java**

```java
package com.group2.assignment.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfo {
    private String googleId;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String provider; // "google"
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/group2/assignment/auth/dto/OAuth2UserInfo.java
git commit -m "feat(auth): tạo OAuth2UserInfo DTO cho Google OAuth"
```

---

### Task 4: Tạo SecurityConfig với OAuth2 Integration

**Files:**
- Create: `src/main/java/com/group2/assignment/auth/config/SecurityConfig.java`

- [ ] **Step 1: Tạo SecurityConfig.java**

```java
package com.group2.assignment.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình Spring Security với OAuth2 Login và Session Management.
 * Tích hợp với hệ thống JWT hiện tại.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF vì dùng JWT cookie (SPA/Mobile friendly)
            .csrf(csrf -> csrf.disable())
            
            // Cấu hình authorization
            .authorizeHttpRequests(auth -> auth
                // Cho phép truy cập public
                .requestMatchers("/", "/login", "/register", "/logout", 
                                 "/css/**", "/js/**", "/images/**", 
                                 "/webjars/**", "/favicon.ico", "/h2-console/**").permitAll()
                // Tất cả các request khác cần xác thực (JWT interceptor xử lý)
                .anyRequest().authenticated()
            )
            
            // Cấu hình OAuth2 Login
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=oauth2_failed")
            )
            
            // Tắt HTTP Basic và Form Login (dùng custom controller)
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/group2/assignment/auth/config/SecurityConfig.java
git commit -m "feat(auth): tạo SecurityConfig với OAuth2 Login integration"
```

---

### Task 5: Cập Nhật AuthService - Thêm Method Xử Lý OAuth2

**Files:**
- Modify: `src/main/java/com/group2/assignment/auth/service/AuthService.java:82-104`

- [ ] **Step 1: Thêm import và method xử lý OAuth2**

Thêm import:
```java
import com.group2.assignment.auth.dto.OAuth2UserInfo;
```

Thêm method mới (sau `findByUsername`):
```java
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
```

- [ ] **Step 2: Thêm import UUID nếu chưa có**

```java
import java.util.UUID;
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/group2/assignment/auth/service/AuthService.java
git commit -m "feat(auth): thêm processOAuth2Login method trong AuthService"
```

---

### Task 6: Cập Nhật AuthController - OAuth2 Callback & Custom Flow

**Files:**
- Modify: `src/main/java/com/group2/assignment/auth/controller/AuthController.java`

- [ ] **Step 1: Thêm import**

```java
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.group2.assignment.auth.dto.OAuth2UserInfo;
```

- [ ] **Step 2: Thêm OAuth2 callback endpoint**

Thêm method mới (sau `changePassword`):

```java
/**
 * OAuth2 Login Success Handler
 * Xử lý khi đăng nhập Google thành công
 */
@GetMapping("/oauth2/success")
public String handleOAuth2Success(
        @AuthenticationPrincipal OAuth2User oauth2User,
        HttpServletResponse response,
        HttpSession session,
        RedirectAttributes redirectAttributes) {
    try {
        if (oauth2User == null) {
            return "redirect:/login?error=oauth2_failed";
        }
        
        // Trích xuất thông tin từ Google
        OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                .googleId(oauth2User.getName())
                .email(oauth2User.getAttribute("email"))
                .fullName(oauth2User.getAttribute("name"))
                .avatarUrl(oauth2User.getAttribute("picture"))
                .provider("google")
                .build();
        
        // Xử lý đăng nhập OAuth2
        User user = authService.processOAuth2Login(userInfo);
        
        // Tạo JWT token
        String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getFullName());
        
        // Lưu JWT vào Cookie
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);
        
        // Lưu user vào session
        session.setAttribute(SESSION_USER, user);
        
        redirectAttributes.addFlashAttribute("successMessage",
                "Đăng nhập Google thành công! Chào mừng " + user.getFullName());
        
        return "redirect:/";
    } catch (Exception e) {
        return "redirect:/login?error=oauth2_failed";
    }
}
```

- [ ] **Step 3: Cập nhật SecurityConfig để sử dụng custom success URL**

Sửa `.defaultSuccessUrl("/", true)` thành `.defaultSuccessUrl("/oauth2/success", false)`

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/group2/assignment/auth/controller/AuthController.java
git add src/main/java/com/group2/assignment/auth/config/SecurityConfig.java
git commit -m "feat(auth): thêm OAuth2 callback endpoint và success handler"
```

---

### Task 7: Cập Nhật Login Page - Thêm Nút Google

**Files:**
- Modify: `src/main/resources/templates/auth/login.html`

- [ ] **Step 1: Thêm nút Google Login**

Tìm vị trí form đăng nhập, thêm divider và nút Google:

```html
<!-- Divider: Hoặc đăng nhập với -->
<div class="flex items-center gap-3 my-4">
    <div class="flex-1 h-px bg-base-300"></div>
    <span class="text-xs text-base-content/50 font-medium">Hoặc đăng nhập với</span>
    <div class="flex-1 h-px bg-base-300"></div>
</div>

<!-- Google OAuth2 Login Button -->
<div class="space-y-3">
    <a th:href="@{/oauth2/authorization/google}" 
       class="btn btn-outline w-full border-base-300 hover:bg-white hover:border-google-red hover:text-google-red rounded-xl text-xs font-bold h-12">
        <svg class="w-5 h-5 mr-2" viewBox="0 0 24 24">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
        </svg>
        Đăng nhập với Google
    </a>
</div>
```

Thêm CSS cho màu Google:
```html
<!-- Thêm vào phần style hoặc inline -->
<style>
    .hover\:border-google-red:hover { border-color: #EA4335; }
    .hover\:text-google-red:hover { color: #EA4335; }
    .hover\:bg-google-red:hover { background-color: #EA4335; }
</style>
```

- [ ] **Step 2: Cập nhật error message cho OAuth2**

Thêm xử lý error parameter:
```html
<div th:if="${param.error == 'oauth2_failed'}" class="alert alert-error shadow-lg">
    <span class="text-xs">Đăng nhập Google thất bại. Vui lòng thử lại hoặc đăng nhập bằng tài khoản thường.</span>
</div>
```

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/auth/login.html
git commit -m "feat(auth): thêm nút đăng nhập Google vào trang login"
```

---

### Task 8: Cập Nhật User Entity - Thêm Trường Provider

**Files:**
- Modify: `src/main/java/com/group2/assignment/auth/entity/User.java`

- [ ] **Step 1: Thêm trường provider**

Thêm field mới sau `role`:
```java
@Column(length = 20)
@Builder.Default
private String provider = "local"; // "local", "google"
```

Cập nhật onCreate để handle provider:
```java
@PrePersist
protected void onCreate() {
    if (this.createdAt == null) {
        this.createdAt = LocalDateTime.now();
    }
    this.updatedAt = LocalDateTime.now();
    if (this.role == null || this.role.isBlank()) {
        this.role = "ROLE_STUDENT";
    }
    if (this.provider == null || this.provider.isBlank()) {
        this.provider = "local";
    }
    if (this.avatar == null || this.avatar.isBlank()) {
        this.avatar = "https://api.dicebear.com/7.x/bottts/svg?seed=" + 
                      (this.username != null ? this.username : "student");
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/group2/assignment/auth/entity/User.java
git commit -m "feat(auth): thêm trường provider vào User entity"
```

---

### Task 9: Cập Nhật UserRepository - Thêm Method Tìm Theo Email

**Files:**
- Modify: `src/main/java/com/group2/assignment/auth/repository/UserRepository.java`

- [ ] **Step 1: Thêm method tìm user theo email**

```java
Optional<User> findByEmailIgnoreCase(String email);
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/group2/assignment/auth/repository/UserRepository.java
git commit -m "feat(auth): thêm findByEmailIgnoreCase vào UserRepository"
```

---

## Summary

| Task | Mô tả | Priority |
|------|-------|----------|
| 1 | Thêm OAuth2 Client dependency | HIGH |
| 2 | Cấu hình Google OAuth2 properties | HIGH |
| 3 | Tạo OAuth2UserInfo DTO | HIGH |
| 4 | Tạo SecurityConfig | HIGH |
| 5 | Cập nhật AuthService | HIGH |
| 6 | Cập nhật AuthController | HIGH |
| 7 | Cập nhật Login page | HIGH |
| 8 | Cập nhật User entity | MEDIUM |
| 9 | Cập nhật UserRepository | MEDIUM |

---

## Post-Implementation Checklist

- [ ] Tạo OAuth2 Client trong Google Cloud Console
- [ ] Copy Client ID và Client Secret vào application.properties
- [ ] Cấu hình Authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`
- [ ] Test đăng nhập Google thành công
- [ ] Test user mới được tạo với email chưa tồn tại
- [ ] Test user cũ (đăng ký thường) đăng nhập Google cùng email
- [ ] Verify JWT cookie được set sau OAuth2 success
