package com.group2.assignment.planner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Giữ module Study Planner tương thích với JwtAuthInterceptor (session/JWT) hiện có.
 * Spring Security mặc định chặn mọi request chưa xác thực (403), nên cần permit các
 * route /planner/** để JwtAuthInterceptor đảm nhiệm việc xác thực và điều hướng login.
 */
@Configuration
public class PlannerSecurityConfig {
    @Bean
    @Order(2)
    public SecurityFilterChain plannerSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher("/planner", "/planner/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .build();
    }
}
