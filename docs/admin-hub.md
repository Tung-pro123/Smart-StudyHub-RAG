# Admin Hub & Command Center — Tài liệu Kiến trúc Quản trị Hệ thống

## 1. Tổng quan & Luồng Điều Hướng Tự Động (Smart Auto-Redirect)

- **Tài khoản Quản trị**: Tài khoản mang vai trò `ROLE_ADMIN` (mặc định trong hệ thống: `admin` / mật khẩu mã hóa BCrypt, hoặc `truong`).
- **Tự động điều hướng (Admin Auto-Redirect)**: Khi tài khoản Admin đăng nhập thành công hoặc truy cập trang chủ (`/`), `AuthController` và `HomeController` tự động điều hướng sang Trung tâm Chỉ huy `/admin/dashboard`.
- **Layout Tách Biệt Hoàn Toàn (Isolated Admin Layout)**:
  - Navbar Quản trị (`navbar.html`): Chỉ hiển thị biểu tượng Admin, liên kết Dashboard, Quản lý Sinh viên (`/admin/users`), Cấu hình System Key (`/admin/settings`), Badge `ADMIN` và nút Đăng xuất. Tuyệt đối loại bỏ các liên kết học tập của sinh viên (như *Lịch Học Vừa Sức*, *Luyện Đề Trắc Nghiệm*, *Nâng Cấp Gói Pro*).
  - Sidebar Quản trị (`sidebar.html`): Gồm các mục chuyên biệt cho Quản trị viên (Tổng quan KPI, Quản lý gói cước & Sinh viên, Cấu hình API Key Hệ thống).

## 2. Các Phân Hệ Quản Trị Chính

### 2.1. Dashboard Chỉ Huy (`/admin/dashboard`)
- **4 Thẻ KPI Thời Gian Thực**:
  1. **Tổng số Tài khoản Sinh viên**: Phân bổ theo 3 gói cước (`FREE`, `PRO`, `SQUAD`).
  2. **Tổng số Môn học Toàn hệ thống**: Thống kê số môn học đã tạo của tất cả sinh viên (tuân thủ Data Isolation ở phía sinh viên).
  3. **Tổng số Khối Kiến thức (Chunks)**: Đo lường dung lượng tri thức RAG trong database.
  4. **Tổng lượt RAG AI Trong Ngày**: Thống kê mức độ sử dụng AI trong ngày theo múi giờ `Asia/Ho_Chi_Minh`.
- **Thống kê Phân bổ 3 Hạng Gói Cước**: Biểu đồ / Thanh tỷ lệ trực quan giữa `FREE`, `PRO` và `SQUAD`.
- **Trạng thái Kết nối AI (Live Gemini Ping)**: Hiển thị độ trễ kết nối gần nhất (`latencyMs`), model active (`gemini-1.5-flash`), và trạng thái HTTP 200 OK.

### 2.2. Quản Lý Gói Cước & Hạn Ngạch Sinh Viên (`/admin/users`)
- **Danh sách Sinh viên Toàn diện**: Hiển thị Username, Email, Gói cước hiện tại (`badge-ghost` cho FREE, `badge-secondary` cho PRO, `badge-info` cho SQUAD), Số lượt RAG đã dùng trong ngày, và Ngày hết hạn gói.
- **Thao tác 1-Click (1-Click Actions)**:
  - Nâng cấp nhanh sang **Gói PRO** (`POST /admin/users/{id}/tier` với `tier=PRO`).
  - Nâng cấp nhanh sang **Gói SQUAD** (`tier=SQUAD`) - Nhóm học tập tối đa 5 bạn.
  - Hạ cấp về **Gói FREE** (`tier=FREE`).
  - **1-Click Reset Hạn Ngạch RAG**: Reset `daily_rag_count = 0` ngay lập tức để hỗ trợ sinh viên khi cần thiết.
- **Bảo mật & Tính toàn vẹn**:
  - Phân quyền nghiêm ngặt với `@PreAuthorize("hasRole('ADMIN')")` và CSRF token bắt buộc trên mọi POST request.
  - Không cho phép người dùng tự cấp role hoặc gói qua request parameter binding.
  - Cập nhật tier tức thì trong Database và tự động nạp lại ở request kế tiếp mà không yêu cầu sinh viên phải đăng xuất/đăng nhập lại.

### 2.3. Cấu Hình System AI Key (`/admin/settings`)
- **Quản lý Tập trung System Key**: Cung cấp Gemini API Key dùng chung cho toàn hệ thống để sinh viên sử dụng RAG, Quiz, và Planner mượt mà.
- **Bảo mật Che mờ (Masked Key Display)**: Key được che mờ dạng `AIzaSy...xxxx`, tuyệt đối không phản chiếu nguyên văn ra HTML, không lưu vào Cookie, Session hay Database.
- **Kiểm tra Kết nối Tức thì (Live Ping)**: Nút test ping đo độ trễ thực tế, trả về thời gian phản hồi (ms) và trạng thái kết nối.
- **Cơ chế Fallback Bền vững**: Khởi tạo tự động từ biến môi trường `GEMINI_API_KEY` trong file `.env` hoặc hệ thống máy chủ.

## 3. Kiến Trúc Tích Hợp Dịch Vụ Hạn Ngạch (Quota Integration)

```java
quotaService.canQueryRag(user.getId()); // Kiểm tra hạn ngạch: FREE tối đa 50 câu/ngày, PRO/SQUAD không giới hạn
quotaService.recordSuccessfulRagQuery(user.getId()); // Ghi nhận 1 lượt truy vấn RAG thành công
```

- **Data Isolation**: Môn học được phân lập theo từng tài khoản (`user_id`). Sinh viên A chỉ thấy môn của sinh viên A, không bị thấy môn của người khác hay của Admin.
- **Reset Lười Theo Ngày (Lazy Reset)**: Tự động reset `daily_rag_count` về 0 khi có lượt truy vấn đầu tiên trong ngày mới (00:00 múi giờ Việt Nam).
- **Chống Gian Lận (No Reset on Downgrade)**: Khi tài khoản bị hạ từ PRO/SQUAD về FREE, số lượt RAG đã dùng trong ngày được bảo lưu, tránh lạm dụng nâng/hạ liên tục.

## 4. Kiểm Thử Hệ Thống Quản Trị (Testing)

- Chạy kiểm thử toàn bộ hệ thống bằng lệnh: `mvn clean test`
- Đảm bảo 100% các ca kiểm thử bảo mật Admin (`AdminSecurityTest`, `UserTierManagementTest`, `DataIsolationTest`) đạt kết quả xanh (Green PASS).
