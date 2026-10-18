# 📝 NHẬT KÝ THỰC NGHIỆM AI - TRƯỜNG (MODULE XÁC THỰC, JWT & CÁ NHÂN HÓA)

**Thành viên thực hiện**: Ngô Nhật Trường  
**Phạm vi phụ trách**: Module Auth & JWT Security (`com.group2.assignment.auth`)  
- Entity `User.java` và cơ sở dữ liệu tài khoản người dùng trong H2.  
- Thuật toán băm mật khẩu an toàn `BCryptPasswordEncoder`.  
- Sinh và xác thực JSON Web Token bằng thư viện `JJWT 0.12.x` (`JwtService.java`).  
- Bộ máy chặn route riêng tư và tự động khôi phục phiên từ Cookie JWT (`JwtAuthInterceptor.java` & `WebMvcConfig.java`).  
- Giao diện đăng ký, đăng nhập, đổi mật khẩu và cập nhật hồ sơ cá nhân (`login.html`, `register.html`, `profile.html`).  
- Kiêm nhiệm: Hỗ trợ Leader Tùng tổng hợp User Guide và Slide PowerPoint.  
**Công cụ AI sử dụng**: Antigravity / Claude Code

---

## 🚀 TIẾN TRÌNH ÁP DỤNG AI THEO TỪNG GIAI ĐOẠN

### 🔹 GIAI ĐOẠN 1: DÙNG AI VIẾT LOGIC XÁC THỰC JWT VÀ BẢO VỆ ROUTE BẰNG INTERCEPTOR
* **Mục tiêu**: Xây dựng `JwtService` sinh chuỗi token HS256 có thời hạn 24 giờ, `AuthService` băm mật khẩu với BCrypt, và `JwtAuthInterceptor` tự động kiểm tra cookie `STUDYHUB_JWT` để bảo vệ các tuyến đường riêng tư (`/courses/new`, `/courses/*/chat/**`, `/planner/**`, `/quiz/**`, `/profile/**`).
* **Câu Prompt thực tế**:
  ```text
  Tôi đang phát triển module Auth trong Spring Boot 3. Hãy viết JwtAuthInterceptor implements HandlerInterceptor để:
  1. Kiểm tra session hiện tại, nếu có currentUser thì cho qua.
  2. Nếu mất session nhưng trình duyệt còn Cookie STUDYHUB_JWT hợp lệ (kiểm tra qua JwtService), tự động khôi phục currentUser vào session và cho qua.
  3. Nếu chưa đăng nhập, lưu URL đang truy cập vào session và redirect về /login?error=unauthorized.
  Đồng thời cấu hình WebMvcConfigurer đăng ký interceptor này cho các route riêng tư.
  ```
* **AI đã giúp bạn như thế nào?**:
  - AI đã tạo ra `JwtAuthInterceptor.java` và `WebMvcConfig.java` chuẩn Spring Boot 3 WebMvcConfigurer.
  - Xử lý mượt mà kịch bản người dùng bị mất session sau khi server restart: chỉ cần Cookie JWT còn hạn là hệ thống tự động tái tạo session sinh viên mà không bắt đăng nhập lại phiền hà.
* **Ảnh chụp màn hình**: `![Trường thiết kế JwtAuthInterceptor](images/truong-step1-jwt.png)`

---

### 🔹 GIAI ĐOẠN 2: DÙNG AI XÂY DỰNG GIAO DIỆN GLASSMORPHISM, ĐỔI MẬT KHẨU & CẬP NHẬT PROFILE
* **Mục tiêu**: Nâng cấp giao diện `profile.html` đồng bộ phong cách Glassmorphism của nhóm (Tailwind CSS + DaisyUI), bổ sung form cập nhật họ tên, chọn nhanh avatar Dicebear và form đổi mật khẩu an toàn.
* **Câu Prompt thực tế**:
  ```text
  Viết giao diện profile.html bằng DaisyUI và Tailwind CSS dạng Grid 2 cột:
  - Cột 1: Form cập nhật Họ tên và Avatar (có ảnh preview và 4 nút chọn nhanh avatar Dicebear bottts / avataaars).
  - Cột 2: Form đổi mật khẩu gồm Mật khẩu cũ, Mật khẩu mới (tối thiểu 6 ký tự) và Xác nhận mật khẩu mới, có nút con mắt ẩn/hiện mật khẩu bằng Javascript.
  ```
* **AI đã giúp bạn như thế nào?**:
  - Tạo layout cân đối, màu sắc hài hòa với Dark/Light theme Synthwave của dự án.
  - Thêm tương tác Javascript trực quan: click vào avatar preset là ô input tự điền link và đổi ảnh đại diện tức thì.
* **Ảnh chụp màn hình**: `![Giao diện Hồ Sơ Cá Nhân của Trường](images/truong-step2-ui.png)`

---

### 🔹 GIAI ĐOẠN 3: DÙNG AI VIẾT UNIT TEST VÀ GỠ LỖI MÔI TRƯỜNG (TEST & DEBUG)
* **Mục tiêu**: Viết bộ test toàn diện 8 bài kiểm thử trong `AuthServiceTest.java` (bao gồm đăng ký, trùng username, đăng nhập JWT, sai mật khẩu, cập nhật profile, đổi mật khẩu thành công và bắt ngoại lệ sai mật khẩu cũ).
* **Lỗi gặp phải & Cách AI sửa giúp**:
  - *Lỗi 1*: Môi trường máy tính cài đặt Java 27 khiến Lombok 1.18.36 bị lỗi `TypeTag :: UNKNOWN`. AI đã hướng dẫn cấu hình `.vscode/settings.json` trỏ runtime về `JDK 21 LTS` (`C:\Program Files\Java\jdk-21.0.12`) để Language Server và Maven chạy ổn định 100%.
  - *Lỗi 2*: Chưa import các DTO mới `UpdateProfileDto` và `ChangePasswordDto` trong `AuthService`. AI đã phát hiện và bổ sung đầy đủ.
* **Kết quả**: Bộ 8 bài test của Module Auth và 7 bài test của các module khác đều đạt `BUILD SUCCESS` (15/15 tests passed).
* **Ảnh chụp màn hình**: `![Unit Test Auth xanh](images/truong-step3-test.png)`

---

## 💡 ĐÚC KẾT KINH NGHIỆM
* **Bài học rút ra**:
  1. Sử dụng kết hợp **Cookie HttpOnly lưu JWT Token** cùng với **HttpSession** giúp ứng dụng vừa có tốc độ xử lý nhanh của Session, vừa có tính năng ghi nhớ đăng nhập dài hạn của JWT.
  2. Bắt buộc băm mật khẩu với `BCryptPasswordEncoder` (10 rounds) trước khi lưu vào database để đảm bảo an toàn thông tin người dùng.
  3. Khi gặp lỗi IDE báo đỏ, cần kiểm tra sự tương thích giữa phiên bản JDK của dự án (Java 17/21) và Java Runtime của máy tính thay vì chỉ sửa code Java.
