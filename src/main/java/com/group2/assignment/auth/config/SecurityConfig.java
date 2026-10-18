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
                // OAuth2 endpoints
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                // Tất cả các request khác cần xác thực (JWT interceptor xử lý)
                .anyRequest().authenticated()
            )

            // Cấu hình OAuth2 Login
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/oauth2/success", true)
                .failureUrl("/login?error=oauth2_failed")
            )

            // Tắt HTTP Basic và Form Login (dùng custom controller)
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }
}
