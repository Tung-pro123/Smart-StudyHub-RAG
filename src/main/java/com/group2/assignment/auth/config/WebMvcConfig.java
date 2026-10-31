package com.group2.assignment.auth.config;

import com.group2.assignment.auth.interceptor.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                // Các tuyến đường yêu cầu bắt buộc phải đăng nhập (Bảo vệ toàn bộ không gian môn học & học tập)
                .addPathPatterns(
                        "/courses",
                        "/courses/**",
                        "/profile",
                        "/profile/**",
                        "/planner",
                        "/planner/**",
                        "/quiz",
                        "/quiz/**",
                        "/settings",
                        "/settings/**",
                        "/admin",
                        "/admin/**"
                )
                // Loại trừ các tuyến đường công khai duy nhất cho khách vãng lai và tài nguyên tĩnh
                .excludePathPatterns(
                        "/",
                        "/landing",
                        "/login",
                        "/register",
                        "/logout",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/h2-console/**"
                );
    }
}
