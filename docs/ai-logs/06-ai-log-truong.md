# 📝 NHẬT KÝ THỰC NGHIỆM AI - TRƯỜNG (MODULE XÁC THỰC, JWT & CÁ NHÂN HÓA)

**Thành viên thực hiện**: Ngô Nhật Trường  
**Phạm vi phụ trách**: Module Auth & JWT Security (`com.group2.assignment.auth`)  
- Entity `User.java` và cơ sở dữ liệu tài khoản người dùng trong H2 (hỗ trợ tài khoản Local và Google OAuth2).  
- Thuật toán băm mật khẩu an toàn `BCryptPasswordEncoder`.  
- Sinh và xác thực JSON Web Token bằng thư viện `JJWT 0.12.x` (`JwtService.java`).  
- Bộ máy chặn route riêng tư và tự động khôi phục phiên từ Cookie JWT (`JwtAuthInterceptor.java` & `WebMvcConfig.java`).  
- Giao diện đăng ký, đăng nhập, đổi mật khẩu và cập nhật hồ sơ cá nhân (`login.html`, `register.html`, `profile.html`).  
- Tích hợp đăng nhập 1 chạm với Google OAuth2 (`spring-boot-starter-oauth2-client`, `SecurityConfig.java`, `OAuth2UserInfo.java`).  
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
  - *Lỗi 1*: Môi trường máy tính cài đặt Java 27 khiến Lombok 1.18.36 bị lỗi `TypeTag :: UNKNOWN`. AI đã hướng dẫn cấu hình `.vscode/settings.json` trỏ runtime về `JDK 21 LTS` (`C:\Program Files\Java\jdk-21.0.12`) và JDK 17 cho Maven để Language Server và Maven chạy ổn định 100%.
  - *Lỗi 2*: Chưa import các DTO mới `UpdateProfileDto` và `ChangePasswordDto` trong `AuthService`. AI đã phát hiện và bổ sung đầy đủ.
* **Kết quả**: Bộ 8 bài test của Module Auth và 7 bài test của các module khác đều đạt `BUILD SUCCESS` (15/15 tests passed).
* **Ảnh chụp màn hình**: `![Unit Test Auth xanh](images/truong-step3-test.png)`

---

### 🔹 GIAI ĐOẠN 4: DÙNG AI TÍCH HỢP GOOGLE OAUTH2 LOGIN VÀ GIẢI PHÁP CONDITIONAL SECURITY CHO CI/CD
* **Mục tiêu**: Tích hợp phương thức đăng nhập bằng Google OAuth2 song song với đăng nhập tài khoản thường, tự động liên kết / tạo mới tài khoản sinh viên với provider `GOOGLE`, cấp phát JWT cookie và bảo toàn tính độc lập của test suites khi chạy trên môi trường CI/CD không có credentials.
* **Câu Prompt thực tế**:
  ```text
  Tôi muốn tích hợp Google OAuth2 Login vào ứng dụng Spring Boot 3 song song với hệ thống JWT hiện tại:
  1. Thêm dependency spring-boot-starter-oauth2-client và cấu hình application.properties.
  2. Tạo SecurityConfig hỗ trợ OAuth2 Login và callback success handler.
  3. Quan trọng: Khi chạy CI/CD hoặc unit test trên máy local không có sẵn Google Client ID / Secret thật, cấu hình phải có cơ chế fallback (@ConditionalOnProperty) để không làm gãy các test suites hiện tại.
  4. Trong AuthService, viết processOAuth2Login để tự động tạo mới tài khoản nếu chưa có hoặc liên kết tài khoản theo email, sau đó cấp phát JWT Cookie STUDYHUB_JWT.
  5. Cập nhật trang login.html thêm nút 'Đăng nhập với Google' đồng bộ với giao diện hiện tại.
  ```
* **AI đã giúp bạn như thế nào?**:
  - Thiết kế kiến trúc `@ConditionalOnProperty(prefix = "spring.security.oauth2.client.registration.google", name = "client-id")` cho `SecurityConfig`: Khi chưa cấu hình ID/Secret thật, Security filter chain của OAuth2 sẽ không khởi tạo nhằm tránh exception `IllegalStateException: ClientRegistrationRepository bean not found`, giúp toàn bộ test suites và CI/CD GitHub Actions vẫn chạy xanh mượt mà.
  - Xử lý nghiệp vụ `processOAuth2Login`: trích xuất `OAuth2UserInfo` (email, fullName, avatar), tự sinh username chuẩn, gán mật khẩu ngẫu nhiên băm BCrypt, lưu `provider = Provider.GOOGLE` và phát hành JWT Token HttpOnly Cookie chuẩn xác.
  - Thiết kế nút đăng nhập Google Glassmorphism tinh tế trên `login.html` với logo Google SVG sắc nét.
* **Ảnh chụp màn hình**: `![Trường tích hợp Google OAuth2 Login](images/truong-step4-oauth2.png)`

---

### 🔹 GIAI ĐOẠN 5: TÍCH HỢP MÔ HÌNH GÓI CƯỚC SAAS, KIỂM SOÁT HẠN NGẠCH RAG & BỔ SUNG UNIT TEST TOÀN DIỆN
* **Mục tiêu**: Đồng bộ hóa `User.java` theo kiến trúc SaaS của nhóm (`subscriptionTier`, `dailyRagCount`, `lastRagResetDate`), hiện đại hóa giao diện `profile.html` hiển thị hạn ngạch RAG 50 câu/ngày và mở rộng bài kiểm thử lên 11 test cases trong `AuthServiceTest.java`.
* **Câu Prompt thực tế**:
  ```text
  Tôi cần đồng bộ module Auth với mô hình gói cước SaaS của dự án Smart StudyHub:
  1. Thêm phương thức upgradeSubscriptionTier(username, tier) trong AuthService và controller POST /auth/upgrade-pro.
  2. Bổ sung các test case trong AuthServiceTest kiểm tra nâng cấp từ FREE lên PRO thành công, bắt ngoại lệ khi user không tồn tại, và xác nhận 3 tài khoản demo (student, pro_student, admin) khởi tạo đúng vai trò và gói cước.
  3. Cập nhật giao diện profile.html dạng Glassmorphism: hiển thị huy hiệu PRO VIP / FREE, thanh tiến trình đo hạn ngạch 50 câu RAG/ngày (kèm cảnh báo màu khi sắp hết lượt), và nút nâng cấp 1-Click gọi POST /auth/upgrade-pro.
  ```
* **AI đã giúp bạn như thế nào?**:
  - Tối ưu hóa UI/UX: Xây dựng thẻ hạn ngạch RAG động bằng Tailwind CSS + DaisyUI với thanh progress bar chuyển màu thông minh (xanh $\rightarrow$ cam $\rightarrow$ đỏ) khi người dùng tiến gần mốc 50 câu hỏi trong ngày.
  - Thiết kế phương thức `upgradeSubscriptionTier` an toàn, cập nhật trạng thái session ngay lập tức mà không bắt buộc sinh viên phải đăng xuất.
  - Viết thêm 3 unit tests mới nâng tổng số bài kiểm thử module Auth lên 11 bài test xanh hoàn hảo (11/11 tests passed).
* **Ảnh chụp màn hình**: `![Trường hoàn thiện giao diện Quota và Unit Test](images/truong-step5-quota-tests.png)`

---

## 💡 ĐÚC KẾT KINH NGHIỆM
* **Bài học rút ra**:
  1. Sử dụng kết hợp **Cookie HttpOnly lưu JWT Token** cùng với **HttpSession** giúp ứng dụng vừa có tốc độ xử lý nhanh của Session, vừa có tính năng ghi nhớ đăng nhập dài hạn của JWT.
  2. Bắt buộc băm mật khẩu với `BCryptPasswordEncoder` (10 rounds) trước khi lưu vào database để đảm bảo an toàn thông tin người dùng.
  3. Khi gặp lỗi IDE báo đỏ, cần kiểm tra sự tương thích giữa phiên bản JDK của dự án (Java 17/21) và Java Runtime của máy tính thay vì chỉ sửa code Java.
  4. Khi tích hợp dịch vụ OAuth2 từ bên thứ ba (Google/Facebook/GitHub), luôn áp dụng mô hình **Conditional Configuration (Graceful Degradation)** để môi trường kiểm thử tự động CI/CD và máy local của đồng đội không bị gián đoạn khi chưa có credentials thực tế.
  5. Khi thiết kế mô hình SaaS Freemium (Free vs Pro), việc phân tách ranh giới rõ ràng giữa Auth (quản lý trạng thái và gói cước của User) và Quota Engine (chặn lượt truy vấn RAG và kiểm tra số môn học) giúp kiến trúc micro-modular bền vững, không gây coupling chéo giữa các module.
