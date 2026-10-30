package com.group2.assignment.auth.interceptor;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.service.AuthService;
import com.group2.assignment.auth.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * Interceptor bảo vệ các route riêng tư:
 * 1. Kiểm tra session hiện tại.
 * 2. Nếu mất session nhưng có Cookie STUDYHUB_JWT hợp lệ -> Tự động khôi phục session.
 * 3. Nếu chưa đăng nhập -> Chuyển hướng về trang /login kèm thông báo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER);

        if (currentUser != null && currentUser.getId() != null) {
            currentUser = authService.findByUsername(currentUser.getUsername()).orElse(null);
            if (currentUser == null) {
                session.removeAttribute(AuthController.SESSION_USER);
            } else {
                session.setAttribute(AuthController.SESSION_USER, currentUser);
            }
        }

        // 1. Đã đăng nhập trong Session -> Cho qua
        if (currentUser != null) {
            return true;
        }

        // 2. Kiểm tra Cookie JWT nếu session bị mất hoặc vừa mở lại trình duyệt
        String token = extractJwtFromCookie(request);
        if (token != null && jwtService.validateToken(token)) {
            String username = jwtService.extractUsername(token);
            Optional<User> userOpt = authService.findByUsername(username);
            if (userOpt.isPresent()) {
                session.setAttribute(AuthController.SESSION_USER, userOpt.get());
                log.info("Khôi phục phiên đăng nhập thành công cho người dùng: {}", username);
                return true;
            }
        }

        // 3. Chưa xác thực -> Chuyển hướng về /login
        String requestUri = request.getRequestURI();
        log.warn("Chặn truy cập trái phép vào route riêng tư: {}", requestUri);
        session.setAttribute("authRedirectUrl", requestUri);
        response.sendRedirect(request.getContextPath() + "/login?error=unauthorized");
        return false;
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (AuthController.AUTH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
