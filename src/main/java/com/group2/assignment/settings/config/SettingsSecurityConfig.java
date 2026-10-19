package com.group2.assignment.settings.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Keep Settings compatible with the application's existing JWT/session interceptor. */
@Configuration
public class SettingsSecurityConfig {
    @Bean
    @Order(1)
    public SecurityFilterChain settingsSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher("/settings", "/settings/**")
                // SettingsController verifies its session token for every POST.
                .csrf(csrf -> csrf.disable())
                // JwtAuthInterceptor authenticates these routes, including JWT cookie restoration.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .build();
    }
}
