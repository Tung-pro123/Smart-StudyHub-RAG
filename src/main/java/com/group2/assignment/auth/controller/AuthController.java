package com.group2.assignment.auth.controller;

import com.group2.assignment.auth.dto.ChangePasswordDto;
import com.group2.assignment.auth.dto.LoginRequestDto;
import com.group2.assignment.auth.dto.OAuth2UserInfo;
import com.group2.assignment.auth.dto.RegisterRequestDto;
import com.group2.assignment.auth.dto.UpdateProfileDto;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.auth.service.AuthService;
import com.group2.assignment.auth.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public static final String AUTH_COOKIE_NAME = "STUDYHUB_JWT";
    public static final String SESSION_USER = "currentUser";

    @GetMapping("/login")
    public String showLoginPage(Model model, HttpSession session) {
        User loggedUser = (User) session.getAttribute(SESSION_USER);
        if (loggedUser != null) {
            return "ROLE_ADMIN".equalsIgnoreCase(loggedUser.getRole()) ? "redirect:/admin/dashboard" : "redirect:/courses";
        }
        model.addAttribute("loginDto", new LoginRequestDto());
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(
            @ModelAttribute("loginDto") LoginRequestDto dto,
            HttpServletResponse response,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            String token = authService.login(dto);

            // Lưu JWT vào Cookie trình duyệt (1 ngày)
            Cookie cookie = new Cookie(AUTH_COOKIE_NAME, token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            // Lưu thông tin người dùng vào Session
            User user = authService.findByUsername(dto.getUsername()).orElse(null);
            session.setAttribute(SESSION_USER, user);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Xin chào mừng " + (user != null ? user.getFullName() : dto.getUsername()) + " đã đăng nhập thành công!");

            String redirectUrl = (String) session.getAttribute("authRedirectUrl");
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                session.removeAttribute("authRedirectUrl");
                return "redirect:" + redirectUrl;
            }
            if (user != null && "ROLE_ADMIN".equalsIgnoreCase(user.getRole())) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/courses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model, HttpSession session) {
        User loggedUser = (User) session.getAttribute(SESSION_USER);
        if (loggedUser != null) {
            return "ROLE_ADMIN".equalsIgnoreCase(loggedUser.getRole()) ? "redirect:/admin/dashboard" : "redirect:/courses";
        }
        model.addAttribute("registerDto", new RegisterRequestDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(
            @ModelAttribute("registerDto") RegisterRequestDto dto,
            RedirectAttributes redirectAttributes) {
        try {
            authService.register(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký tài khoản thành công! Vui lòng đăng nhập với thông tin vừa tạo.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response, HttpSession session, RedirectAttributes redirectAttributes) {
        // Xóa Cookie JWT
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        // Hủy Session
        session.removeAttribute(SESSION_USER);
        session.invalidate();

        redirectAttributes.addFlashAttribute("successMessage", "Bạn đã đăng xuất an toàn khỏi hệ thống!");
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        // Tự động làm mới thông tin User từ Database để đồng bộ gói cước và nhóm
        Optional<User> freshUserOpt = userRepository.findByUsernameIgnoreCase(user.getUsername());
        if (freshUserOpt.isPresent()) {
            user = freshUserOpt.get();
            session.setAttribute(SESSION_USER, user);
        }
        model.addAttribute("user", user);

        // Nạp danh sách thành viên nhóm học tập Study Squad
        List<User> squadMembers = authService.getSquadMembers(user.getUsername());
        model.addAttribute("squadMembers", squadMembers);
        model.addAttribute("isSquadLeader", user.isSquadLeader());
        model.addAttribute("maxSquadMembers", AuthService.MAX_SQUAD_MEMBERS);

        if (!model.containsAttribute("updateProfileDto")) {
            model.addAttribute("updateProfileDto", UpdateProfileDto.builder()
                    .fullName(user.getFullName())
                    .avatar(user.getAvatar())
                    .build());
        }
        if (!model.containsAttribute("changePasswordDto")) {
            model.addAttribute("changePasswordDto", new ChangePasswordDto());
        }
        return "auth/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute("updateProfileDto") UpdateProfileDto dto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(SESSION_USER);
        if (currentUser == null) {
            return "redirect:/login";
        }
        try {
            User updatedUser = authService.updateProfile(currentUser.getUsername(), dto);
            session.setAttribute(SESSION_USER, updatedUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin hồ sơ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @ModelAttribute("changePasswordDto") ChangePasswordDto dto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(SESSION_USER);
        if (currentUser == null) {
            return "redirect:/login";
        }
        try {
            authService.changePassword(currentUser.getUsername(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công! Mật khẩu mới đã được cập nhật.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    /**
     * Nâng cấp gói Pro (Kích hoạt nhanh cho sinh viên từ bảng giá)
     */
    @PostMapping("/auth/upgrade-pro")
    public String upgradePro(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(SESSION_USER);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để nâng cấp gói tài khoản!");
            return "redirect:/login";
        }

        // CHỐNG CLICK SPAM (IDEMPOTENCY): Nếu đã là PRO, không cập nhật lặp lại
        if (currentUser.isPro()) {
            redirectAttributes.addFlashAttribute("successMessage", "Tài khoản của bạn hiện đã là gói PRO!");
            return "redirect:/courses";
        }

        User updated = authService.upgradeSubscriptionTier(currentUser.getUsername(), "PRO");
        session.setAttribute(SESSION_USER, updated);
        redirectAttributes.addFlashAttribute("successMessage",
                "🎉 Chúc mừng! Bạn đã nâng cấp thành công lên Gói PRO (Không giới hạn môn học, AI Study Planner & RAG không giới hạn)!");
        return "redirect:/courses";
    }

    /**
     * Nâng cấp gói Nhóm Học Tập (Study Squad - 5 thành viên)
     */
    @PostMapping("/auth/upgrade-squad")
    public String upgradeSquad(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(SESSION_USER);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để nâng cấp gói tài khoản!");
            return "redirect:/login";
        }

        // CHỐNG CLICK SPAM (IDEMPOTENCY): Nếu đã là SQUAD hoặc CAMPUS, không cập nhật lặp lại
        if ("SQUAD".equalsIgnoreCase(currentUser.getSubscriptionTier()) || "CAMPUS".equalsIgnoreCase(currentUser.getSubscriptionTier())) {
            redirectAttributes.addFlashAttribute("successMessage", "Tài khoản của bạn hiện đã kích hoạt Gói Nhóm Học Tập (Study Squad)!");
            return "redirect:/courses";
        }

        User updated = authService.upgradeSubscriptionTier(currentUser.getUsername(), "SQUAD");
        session.setAttribute(SESSION_USER, updated);
        redirectAttributes.addFlashAttribute("successMessage",
                "🎉 Chúc mừng! Bạn đã kích hoạt thành công Gói Nhóm Học Tập (Study Squad) cho 5 thành viên!");
        return "redirect:/profile";
    }

    /**
     * Thêm thành viên vào Nhóm Học Tập (Study Squad: tối đa 5 người)
     */
    @PostMapping("/auth/squad/add-member")
    public String addSquadMember(
            @RequestParam("targetUsername") String targetUsername,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(SESSION_USER);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để thực hiện thao tác này!");
            return "redirect:/login";
        }
        try {
            authService.addMemberToSquad(currentUser.getUsername(), targetUsername);
            redirectAttributes.addFlashAttribute("successMessage",
                    "🎉 Đã thêm thành viên @" + targetUsername.trim() + " vào Nhóm Học Tập thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    /**
     * Xóa thành viên khỏi Nhóm Học Tập (đưa về lại gói FREE)
     */
    @PostMapping("/auth/squad/remove-member")
    public String removeSquadMember(
            @RequestParam("targetUsername") String targetUsername,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(SESSION_USER);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để thực hiện thao tác này!");
            return "redirect:/login";
        }
        try {
            authService.removeMemberFromSquad(currentUser.getUsername(), targetUsername);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xóa thành viên @" + targetUsername.trim() + " khỏi Nhóm Học Tập!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }


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
}
