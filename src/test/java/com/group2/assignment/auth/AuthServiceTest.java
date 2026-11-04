package com.group2.assignment.auth;

import com.group2.assignment.auth.dto.ChangePasswordDto;
import com.group2.assignment.auth.dto.LoginRequestDto;
import com.group2.assignment.auth.dto.RegisterRequestDto;
import com.group2.assignment.auth.dto.UpdateProfileDto;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.auth.service.AuthService;
import com.group2.assignment.auth.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Đăng ký tài khoản sinh viên mới thành công")
    void testRegisterSuccess() {
        RegisterRequestDto dto = RegisterRequestDto.builder()
                .username("truongfpt")
                .email("truongfpt@fpt.edu.vn")
                .fullName("Ngô Nhật Trường")
                .password("password123")
                .confirmPassword("password123")
                .build();

        User user = authService.register(dto);
        assertNotNull(user.getId());
        assertEquals("truongfpt", user.getUsername());
        assertNotEquals("password123", user.getPassword(), "Mật khẩu phải được băm BCrypt");
    }

    @Test
    @DisplayName("Đăng ký trùng username phải ném ngoại lệ")
    void testRegisterDuplicateUsernameThrows() {
        RegisterRequestDto dto = RegisterRequestDto.builder()
                .username("student") // Trùng username mặc định
                .email("student_new@fpt.edu.vn")
                .password("123456")
                .confirmPassword("123456")
                .build();

        assertThrows(IllegalArgumentException.class, () -> authService.register(dto));
    }

    @Test
    @DisplayName("Đăng nhập thành công và sinh JWT token hợp lệ")
    void testLoginSuccessAndGenerateJwt() {
        LoginRequestDto dto = LoginRequestDto.builder()
                .username("student")
                .password("123456")
                .build();

        String token = authService.login(dto);
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("student", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Đăng nhập sai mật khẩu phải ném ngoại lệ")
    void testLoginWrongPasswordThrows() {
        LoginRequestDto dto = LoginRequestDto.builder()
                .username("student")
                .password("wrongpassword")
                .build();

        assertThrows(IllegalArgumentException.class, () -> authService.login(dto));
    }

    @Test
    @DisplayName("Cập nhật thông tin hồ sơ thành công")
    void testUpdateProfileSuccess() {
        UpdateProfileDto dto = UpdateProfileDto.builder()
                .fullName("Ngô Nhật Trường Pro")
                .avatar("https://api.dicebear.com/7.x/bottts/svg?seed=truong_updated")
                .build();

        User updatedUser = authService.updateProfile("truong", dto);
        assertEquals("Ngô Nhật Trường Pro", updatedUser.getFullName());
        assertEquals("https://api.dicebear.com/7.x/bottts/svg?seed=truong_updated", updatedUser.getAvatar());
    }

    @Test
    @DisplayName("Đổi mật khẩu thành công và xác thực được với mật khẩu mới")
    void testChangePasswordSuccess() {
        ChangePasswordDto dto = ChangePasswordDto.builder()
                .oldPassword("123456")
                .newPassword("newSecretPassword2026")
                .confirmPassword("newSecretPassword2026")
                .build();

        authService.changePassword("student", dto);

        // Đăng nhập lại với mật khẩu mới
        LoginRequestDto loginDto = LoginRequestDto.builder()
                .username("student")
                .password("newSecretPassword2026")
                .build();

        String token = authService.login(loginDto);
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("Đổi mật khẩu sai mật khẩu cũ phải ném ngoại lệ")
    void testChangePasswordWrongOldPasswordThrows() {
        ChangePasswordDto dto = ChangePasswordDto.builder()
                .oldPassword("wrongOldPass")
                .newPassword("newSecretPassword2026")
                .confirmPassword("newSecretPassword2026")
                .build();

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword("student", dto));
    }

    @Test
    @DisplayName("Đổi mật khẩu xác nhận không khớp phải ném ngoại lệ")
    void testChangePasswordMismatchedConfirmThrows() {
        ChangePasswordDto dto = ChangePasswordDto.builder()
                .oldPassword("123456")
                .newPassword("newSecretPassword2026")
                .confirmPassword("mismatchPassword")
                .build();

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword("student", dto));
    }

    @Test
    @DisplayName("Nâng cấp gói cước hội viên thành công từ FREE lên PRO")
    void testUpgradeSubscriptionTier_Success() {
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase("student");
        assertTrue(optionalUser.isPresent());
        User user = optionalUser.get();
        assertEquals("FREE", user.getSubscriptionTier());
        assertFalse(user.isPro());

        User upgradedUser = authService.upgradeSubscriptionTier("student", "PRO");
        assertEquals("PRO", upgradedUser.getSubscriptionTier());
        assertTrue(upgradedUser.isPro());
    }

    @Test
    @DisplayName("Nâng cấp gói cước thành công lên SQUAD (Study Squad) và kích hoạt quyền isPro")
    void testUpgradeSubscriptionTier_SquadSuccess() {
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase("student");
        assertTrue(optionalUser.isPresent());

        User upgradedUser = authService.upgradeSubscriptionTier("student", "SQUAD");
        assertEquals("SQUAD", upgradedUser.getSubscriptionTier());
        assertTrue(upgradedUser.isPro());
    }


    @Test
    @DisplayName("Nâng cấp gói cước cho username không tồn tại ném ngoại lệ")
    void testUpgradeSubscriptionTier_UserNotFoundThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                authService.upgradeSubscriptionTier("non_existent_student_2026", "PRO")
        );
    }

    @Test
    @DisplayName("Kiểm tra khởi tạo các tài khoản Demo chuẩn với đúng phân cấp gói cước và vai trò")
    void testDemoUsers_InitializedWithCorrectTiers() {
        // 1. Student Demo
        Optional<User> studentOpt = userRepository.findByUsernameIgnoreCase("student");
        assertTrue(studentOpt.isPresent());
        User student = studentOpt.get();
        assertEquals("ROLE_STUDENT", student.getRole());
        assertEquals("FREE", student.getSubscriptionTier());
        assertFalse(student.isPro());

        // 2. Pro Student Demo
        Optional<User> proStudentOpt = userRepository.findByUsernameIgnoreCase("pro_student");
        assertTrue(proStudentOpt.isPresent());
        User proStudent = proStudentOpt.get();
        assertEquals("ROLE_STUDENT", proStudent.getRole());
        assertEquals("PRO", proStudent.getSubscriptionTier());
        assertTrue(proStudent.isPro());

        // 3. Admin Demo
        Optional<User> adminOpt = userRepository.findByUsernameIgnoreCase("admin");
        assertTrue(adminOpt.isPresent());
        User admin = adminOpt.get();
        assertEquals("ROLE_ADMIN", admin.getRole());
        assertTrue(admin.isAdmin());
        assertTrue(admin.isPro());
    }

    @Test
    @DisplayName("Nhóm trưởng thêm thành viên vào nhóm học tập SQUAD thành công")
    void testAddMemberToSquad_Success() {
        // Tạo nhóm trưởng
        User leader = authService.upgradeSubscriptionTier("truong", "SQUAD");
        assertEquals("SQUAD", leader.getSubscriptionTier());

        // Thêm student vào nhóm
        User member = authService.addMemberToSquad("truong", "student");
        assertEquals("SQUAD", member.getSubscriptionTier());
        assertEquals("truong", member.getSquadLeaderUsername());
        assertTrue(member.isPro());

        // Kiểm tra danh sách thành viên nhóm
        var squad = authService.getSquadMembers("truong");
        assertEquals(2, squad.size(), "Nhóm gồm nhóm trưởng và 1 thành viên");
    }

    @Test
    @DisplayName("Thêm thành viên vượt quá 5 người ném IllegalStateException")
    void testAddMemberToSquad_MaxFiveMembers_Throws() {
        User leader = authService.upgradeSubscriptionTier("truong", "SQUAD");

        // Tạo sẵn 4 bạn
        for (int i = 1; i <= 4; i++) {
            String u = "member_" + i;
            if (!userRepository.existsByUsernameIgnoreCase(u)) {
                userRepository.save(User.builder()
                        .username(u)
                        .email(u + "@fpt.edu.vn")
                        .fullName("Thành viên " + i)
                        .password("123456")
                        .build());
            }
            authService.addMemberToSquad("truong", u);
        }

        // Tạo người thứ 6 (tính cả leader là 5 rồi, add thêm là người thứ 6)
        String u6 = "member_extra";
        if (!userRepository.existsByUsernameIgnoreCase(u6)) {
            userRepository.save(User.builder()
                    .username(u6)
                    .email(u6 + "@fpt.edu.vn")
                    .fullName("Thành viên Extra")
                    .password("123456")
                    .build());
        }

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                authService.addMemberToSquad("truong", u6)
        );
        assertTrue(ex.getMessage().contains("tối đa 5"));
    }

    @Test
    @DisplayName("Nhóm trưởng xóa thành viên khỏi nhóm đưa về lại gói FREE thành công")
    void testRemoveMemberFromSquad_Success() {
        authService.upgradeSubscriptionTier("truong", "SQUAD");
        authService.addMemberToSquad("truong", "student");

        User removed = authService.removeMemberFromSquad("truong", "student");
        assertEquals("FREE", removed.getSubscriptionTier());
        assertNull(removed.getSquadLeaderUsername());
    }
    @Test
    @DisplayName("Nhóm trưởng thêm thành viên bằng Member Tag (#STU-XXXX) thành công")
    void testAddMemberToSquad_UsingMemberTag_Success() {
        User leader = authService.upgradeSubscriptionTier("truong", "SQUAD");
        User student = userRepository.findByUsernameIgnoreCase("student").orElseThrow();
        String memberTag = student.getMemberTag();
        assertNotNull(memberTag);

        User member = authService.addMemberToSquad("truong", memberTag);
        assertEquals("SQUAD", member.getSubscriptionTier());
        assertEquals("truong", member.getSquadLeaderUsername());
        assertEquals("ACTIVE", member.getSquadStatus());
        assertTrue(member.isSquad());
    }

    @Test
    @DisplayName("Nhóm trưởng thêm thành viên bằng Gmail thành công")
    void testAddMemberToSquad_UsingEmail_Success() {
        authService.upgradeSubscriptionTier("truong", "SQUAD");
        User student = userRepository.findByUsernameIgnoreCase("student").orElseThrow();

        User member = authService.addMemberToSquad("truong", student.getEmail());
        assertEquals("SQUAD", member.getSubscriptionTier());
        assertEquals("truong", member.getSquadLeaderUsername());
        assertEquals("ACTIVE", member.getSquadStatus());
    }

    @Test
    @DisplayName("Thành viên gửi yêu cầu tham gia bằng Mã Mời (SQ-XXXX) chuyển sang trạng thái PENDING")
    void testRequestJoinSquadByCode_Success_BecomesPending() {
        User leader = authService.upgradeSubscriptionTier("truong", "SQUAD");
        String inviteCode = leader.getSquadInviteCode();
        assertNotNull(inviteCode);

        User pendingStudent = authService.requestJoinSquadByCode("student", inviteCode);
        assertTrue(pendingStudent.isSquadPending());
        assertEquals("PENDING", pendingStudent.getSquadStatus());
        assertEquals("truong", pendingStudent.getSquadLeaderUsername());
        assertFalse(pendingStudent.isSquad(), "Chưa được duyệt thì chưa được tính là SQUAD active");

        // Leader thấy trong danh sách chờ duyệt, nhưng chưa tính vào sĩ số nhóm active
        var pendingList = authService.getPendingSquadRequests("truong");
        assertEquals(1, pendingList.size());
        assertEquals("student", pendingList.get(0).getUsername());

        var squadMembers = authService.getSquadMembers("truong");
        assertEquals(1, squadMembers.size(), "Chỉ có leader trong danh sách active");
    }

    @Test
    @DisplayName("Nhóm trưởng phê duyệt yêu cầu kích hoạt thành công gói SQUAD cho thành viên")
    void testApproveSquadRequest_Success() {
        User leader = authService.upgradeSubscriptionTier("truong", "SQUAD");
        authService.requestJoinSquadByCode("student", leader.getSquadInviteCode());

        // Phê duyệt
        User approved = authService.approveSquadRequest("truong", "student");
        assertEquals("SQUAD", approved.getSubscriptionTier());
        assertEquals("ACTIVE", approved.getSquadStatus());
        assertTrue(approved.isSquad());
        assertTrue(approved.isPro());

        // Danh sách chờ đã hết
        var pendingList = authService.getPendingSquadRequests("truong");
        assertTrue(pendingList.isEmpty());

        // Sĩ số nhóm active tăng lên 2
        var squadMembers = authService.getSquadMembers("truong");
        assertEquals(2, squadMembers.size());
    }

    @Test
    @DisplayName("Nhóm trưởng từ chối yêu cầu gia nhập nhóm xóa sạch trạng thái PENDING")
    void testRejectSquadRequest_Success() {
        User leader = authService.upgradeSubscriptionTier("truong", "SQUAD");
        authService.requestJoinSquadByCode("student", leader.getSquadInviteCode());

        User rejected = authService.rejectSquadRequest("truong", "student");
        assertNull(rejected.getSquadStatus());
        assertNull(rejected.getSquadLeaderUsername());
        assertEquals("FREE", rejected.getSubscriptionTier());
        assertFalse(rejected.isSquadPending());
    }

    @Test
    @DisplayName("Thành viên tự hủy yêu cầu chờ duyệt thành công")
    void testCancelSquadRequest_Success() {
        User leader = authService.upgradeSubscriptionTier("truong", "SQUAD");
        authService.requestJoinSquadByCode("student", leader.getSquadInviteCode());

        User cancelled = authService.cancelSquadRequest("student");
        assertNull(cancelled.getSquadStatus());
        assertNull(cancelled.getSquadLeaderUsername());
        assertFalse(cancelled.isSquadPending());
    }

    @Test
    @DisplayName("Gia nhập nhóm bằng mã mời khi nhóm đã đủ 5 người ném lỗi")
    void testRequestJoinSquadByCode_WhenSquadFull_Throws() {
        User leader = authService.upgradeSubscriptionTier("truong", "SQUAD");
        for (int i = 1; i <= 4; i++) {
            String u = "member_full_" + i;
            if (!userRepository.existsByUsernameIgnoreCase(u)) {
                userRepository.save(User.builder()
                        .username(u)
                        .email(u + "@fpt.edu.vn")
                        .fullName("Thành viên " + i)
                        .password("123456")
                        .build());
            }
            authService.addMemberToSquad("truong", u);
        }

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                authService.requestJoinSquadByCode("student", leader.getSquadInviteCode())
        );
        assertTrue(ex.getMessage().contains("tối đa 5/5"));
    }
}
