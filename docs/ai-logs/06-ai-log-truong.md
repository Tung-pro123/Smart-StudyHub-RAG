# 📝 NHẬT KÝ THỰC NGHIỆM AI - TRƯỜNG (MODULE XÁC THỰC, JWT & CÁ NHÂN HÓA)

**Thành viên thực hiện**: Đinh Nhật Trường  
**Phạm vi phụ trách**: Module Auth & JWT Security (`com.group2.assignment.auth`)  
- Entity `User.java` và cơ sở dữ liệu tài khoản người dùng trong H2.  
- Thuật toán băm mật khẩu an toàn `BCryptPasswordEncoder`.  
- Sinh và xác thực JSON Web Token bằng thư viện `JJWT 0.12.x` (`JwtService.java`).  
- Giao diện đăng ký, đăng nhập và hồ sơ cá nhân hóa (`login.html`, `register.html`, `profile.html`).  
- Kiêm nhiệm: Hỗ trợ Leader Tùng tổng hợp User Guide và Slide PowerPoint.  
**Công cụ AI sử dụng**: Cursor / Copilot / Gemini / ChatGPT  

---

## 🚀 TIẾN TRÌNH ÁP DỤNG AI THEO TỪNG GIAI ĐOẠN

### 🔹 GIAI ĐOẠN 1: DÙNG AI VIẾT LOGIC XÁC THỰC JWT VÀ MÃ HÓA BCRYPT (BACK-END)
* **Mục tiêu**: Xây dựng `JwtService` sinh chuỗi token HS256 có thời hạn 24 giờ và `AuthService` băm mật khẩu với BCrypt khi sinh viên đăng ký.
* **Câu Prompt gợi ý cho bạn**:
  ```text
  Tôi đang làm dự án Spring Boot 3. Hãy viết JwtService sử dụng thư viện JJWT phiên bản mới nhất 0.12.x có các phương thức:
  1. generateToken(String username, String role, String fullName): sinh chuỗi JWT ký bằng thuật toán HS256 có thời hạn 24h.
  2. extractUsername(String token): giải mã và lấy username.
  3. validateToken(String token): kiểm tra tính hợp lệ và hạn sử dụng của token.
  ```
* **AI đã giúp bạn như thế nào?**: *(Ghi lại code và cách bạn cấu hình)*
* **Ảnh chụp màn hình**: `![Trường thiết kế JwtService](images/truong-step1-jwt.png)`

---

### 🔹 GIAI ĐOẠN 2: DÙNG AI XÂY DỰNG GIAO DIỆN GLASSMORPHISM (FRONT-END)
* **Mục tiêu**: Thiết kế các màn hình `login.html`, `register.html` và `profile.html` đồng bộ với phong cách Glassmorphism của nhóm (Tailwind CSS + DaisyUI).
* **Câu Prompt gợi ý cho bạn**:
  ```text
  Viết file giao diện Thymeleaf login.html và register.html bằng DaisyUI và Tailwind CSS theo phong cách Glassmorphism nền mờ viền sáng. Form có input mật khẩu kèm nút con mắt bấm vào để ẩn/hiện mật khẩu bằng Javascript, và một hộp alert hiển thị tài khoản demo: student / 123456.
  ```
* **AI đã giúp bạn như thế nào?**: *(Ghi lại kết quả)*
* **Ảnh chụp màn hình**: `![Giao diện Đăng Nhập của Trường](images/truong-step2-ui.png)`

---

### 🔹 GIAI ĐOẠN 3: DÙNG AI VIẾT UNIT TEST VÀ SỬA LỖI (TEST & DEBUG)
* **Mục tiêu**: Viết bộ test `AuthServiceTest.java` kiểm tra các trường hợp: Đăng ký thành công, Bắt lỗi trùng username, Đăng nhập đúng sinh JWT token, và Bắt lỗi sai mật khẩu.
* **Lỗi gặp phải & Cách AI sửa giúp**: *(Ví dụ: Gặp lỗi token hết hạn hoặc lỗi ký key HS256 quá ngắn dưới 256 bits, AI hướng dẫn sinh secret key 256 bits bằng Keys.hmacShaKeyFor)*.
* **Ảnh chụp màn hình**: `![Unit Test Auth xanh](images/truong-step3-test.png)`

---

## 💡 ĐÚC KẾT KINH NGHIỆM
* **Bài học rút ra**: ....................................................................
