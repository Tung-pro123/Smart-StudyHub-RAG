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
                // Các tuyến đường yêu cầu bắt buộc phải đăng nhập
                .addPathPatterns(
                        "/courses/new",
                        "/courses/*/edit",
                        "/courses/*/delete",
                        "/courses/*/chat/**",
                        "/courses/*/documents/**",
                        "/profile/**",
                        "/planner/**",
                        "/quiz/**",
                        "/settings/**"
                )
                // Loại trừ các tuyến đường công khai và tài nguyên tĩnh
                .excludePathPatterns(
                        "/",
                        "/courses",
                        "/courses/*",
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
