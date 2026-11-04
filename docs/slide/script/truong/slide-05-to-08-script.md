# 🎙️ LỜI THOẠI THUYẾT TRÌNH: SLIDE 5 ➔ SLIDE 8 (NGÔ NHẬT TRƯỜNG - AUTH & UI)
> **Khối trình bày**: Khối 2 — Assignment UI Toolkit, AI Agent IDE & Phân hệ Authentication / Profile  
> **Thời lượng**: 3 phút  
> **Tác phong**: Chuyên nghiệp, tập trung vào kỹ thuật lập trình Frontend & Security  

---

### [SLIDE 5 — UI TOOLKIT TRONG THYMELEAF] (~45 giây)
"Em xin cảm ơn anh Tùng. Kính chào Thầy Cô và các bạn, em là **Ngô Nhật Trường**, phụ trách phần Assignment và phân hệ Xác thực người dùng.

Để giải quyết yêu cầu thứ nhất của đề bài Assignment về việc ứng dụng UI Toolkit trong Thymeleaf, nhóm chúng em đã quyết định lựa chọn **Tailwind CSS kết hợp cùng thư viện component DaisyUI v4** thay vì Bootstrap truyền thống. Lý do là vì Tailwind CSS sở hữu cơ chế JIT Engine cực kỳ nhẹ, cho phép chúng em tùy biến sâu toàn bộ giao diện theo phong cách **White-Card Palette** hiện đại và thanh lịch. 

Về mặt kỹ thuật trong Thymeleaf, chúng em áp dụng triệt để nguyên lý DRY thông qua việc kế thừa bố cục layout master `layout/main.html` và bóc tách các thành phần tái sử dụng thành các Thymeleaf Fragment như `navbar.html` hay `sidebar.html`, giúp mã nguồn cực kỳ gọn gàng và dễ bảo trì."

---

### [SLIDE 6 — QUY TRÌNH CODE VỚI AI AGENT TRONG IDE] (~45 giây)
"Bước sang tiêu chí thứ hai của Assignment: Ứng dụng AI Agent trong môi trường IDE. Nhóm chúng em đã sử dụng môi trường **Google Antigravity IDE** tích hợp song song hai mô hình AI hàng đầu là **Gemini 2.5 Flash** và **Claude 3.5 Sonnet**.

Quy trình phát triển của chúng em không phải là phụ thuộc hoàn toàn vào AI, mà tuân thủ nghiêm ngặt 3 bước:
Thứ nhất, **Spec-Driven**: Chúng em soạn trước đặc tả kỹ thuật chi tiết về Input/Output và quy tắc nghiệp vụ trong tài liệu hướng dẫn.
Thứ hai, **Test-Driven Development (TDD)**: Yêu cầu AI sinh bộ test case kiểm thử trước, sau đó mới viết mã nguồn thực thi.
Và thứ ba, **Pair-Debugging**: Tận dụng AI để cùng phân tích các lỗi phức tạp như xung đột hạn ngạch đồng thời hay cách ly dữ liệu giữa các sinh viên."

---

### [SLIDE 7 — GOOGLE OAUTH2 & LOCAL AUTH] (~45 giây)
"Chuyển sang phân hệ chức năng thực tế của Project cuối kỳ, em đảm nhiệm toàn bộ luồng **Authentication**. 

Hệ thống hỗ trợ cơ chế đăng nhập đa kênh. Sinh viên có thể chọn **Đăng nhập 1 chạm bằng Google OAuth2** theo chuẩn OpenID Connect, hệ thống tự động đồng bộ email, họ tên và ảnh đại diện Google mà không lo quên mật khẩu. 

Bên cạnh đó, luồng đăng ký tài khoản truyền thống được bảo vệ nghiêm ngặt bằng thuật toán băm một chiều **BCrypt** với 10 vòng salt. Để thuận tiện nhất cho Thầy Cô và các bạn khi chấm bài hoặc kiểm thử, em đã thiết kế thêm các nút **1-Click Login**, chỉ cần bấm 1 click là tự động đăng nhập nhanh vào đúng tài khoản Free, Pro hoặc Squad Leader."

---

### [SLIDE 8 — BẢO MẬT PHIÊN, RBAC & CHUYỂN MIC] (~45 giây)
"Cuối cùng là cơ chế **Bảo mật phiên và Phân quyền vai trò (RBAC)**.
Hệ thống sử dụng `HttpSession` an toàn. Dựa vào vai trò người dùng, hệ thống sẽ tự động điều hướng: sinh viên vào thẳng Dashboard môn học, còn tài khoản quản trị viên sẽ được chuyển hướng về Admin Hub. Toàn bộ các đường dẫn nhạy cảm như `/admin/**` hay `/h2-console` đều có lớp guard bảo vệ, sinh viên thường cố tình truy cập sẽ bị chặn ngay lập tức. 

Sinh viên cũng có thể vào trang Hồ sơ cá nhân để quản lý gói cước và avatar của mình.

Tiếp theo, linh hồn công nghệ của dự án chính là phân hệ RAG AI Chatbot giáo trình không ảo giác. Em xin trân trọng kính mời bạn **Nguyễn Văn Gia Bình** lên trình bày chi tiết. Xin mời Bình!"

