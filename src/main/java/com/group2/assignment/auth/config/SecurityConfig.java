package com.group2.assignment.auth.config;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF vì dùng JWT cookie (SPA/Mobile friendly)
            .csrf(csrf -> csrf.disable())

            // Cấu hình authorization: Cho phép các request đi qua để JwtAuthInterceptor kiểm soát xác thực
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/logout",
                                 "/css/**", "/js/**", "/images/**",
                                 "/webjars/**", "/favicon.ico", "/h2-console/**").permitAll()
                // OAuth2 endpoints
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                // Mọi request được chuyển tiếp cho JwtAuthInterceptor kiểm soát phiên đăng nhập và bảo vệ route
                .anyRequest().permitAll()
            );

        // Chỉ bật OAuth2 nếu có credentials hợp lệ
        if (googleClientId != null && !googleClientId.isBlank() && !googleClientId.equals("YOUR_GOOGLE_CLIENT_ID")) {
            http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/oauth2/success", true)
                .failureUrl("/login?error=oauth2_failed")
            );
        }

        // Tắt HTTP Basic và Form Login (dùng custom controller)
        http.httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }
}
