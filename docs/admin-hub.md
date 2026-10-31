# Admin Command Center — công việc của Ân, 31/10/2026

## Phạm vi đã triển khai

- `/admin/dashboard`: tổng tài khoản; số sinh viên FREE, PRO, SQUAD; tỷ lệ trả phí `(PRO + SQUAD) / tổng sinh viên của ba gói`; tổng môn và chunks đã lưu; tổng lượt RAG còn ghi nhận trong ngày Việt Nam. Quản trị viên không nằm trong thống kê gói sinh viên. CAMPUS cũ vẫn được bảo toàn và không gộp vào SQUAD.
- Bốn thẻ KPI có thanh phân bổ gói, trạng thái lần ping Gemini gần nhất và model cấu hình thực tế của phép ping. Trạng thái chưa kiểm tra tách biệt với lỗi kết nối. Nút làm mới lấy lại số liệu, không tự gọi AI. Hai danh sách hiển thị tối đa 5 tài khoản và 5 môn mới nhất.
- `/admin/users?keyword=...&tier=...`: tìm không phân biệt hoa thường theo tên, username hoặc email, kết hợp bộ lọc FREE/PRO/SQUAD. Chỉ hiển thị sinh viên. Có huy hiệu gói, ngày hết hạn và tiến trình RAG: xanh dưới 30, vàng 30–44, đỏ từ 45.
- Các POST `/admin/users/{id}/upgrade-pro`, `upgrade-squad`, `downgrade-free` thay gói ngay trong database, không đổi role, xóa môn hoặc reset lượt dùng. Phiên sinh viên được nạp lại ở request được bảo vệ tiếp theo.
- POST `/admin/users/{id}/reset-rag` đặt bộ đếm về 0 và ngày reset về ngày hiện tại ở `Asia/Ho_Chi_Minh`. Dùng cùng khóa hàng với dịch vụ quota để tránh ghi đè bộ đếm khi có request đồng thời.
- `/admin/settings`: chỉ phản chiếu 6 ký tự đầu kèm `...****`; key ngắn được che hoàn toàn. Form nhập mới dùng kiểu password, không điền lại key. Ping có spinner, khóa nút trong lúc gửi và hiển thị kết quả/độ trễ; lỗi 401/403/429 có mã HTTP, không lộ phản hồi hoặc key của nhà cung cấp.
- `/courses`: dùng lại quyền xem/xóa toàn hệ thống dành cho admin trong Core, bổ sung nhãn `Bởi: username` hoặc `Môn mẫu hệ thống`. Không tạo thêm luồng xóa hoặc thay đổi quyền sở hữu.

## Bảo mật và tích hợp

Mọi endpoint Admin kiểm tra ROLE_ADMIN từ database, gửi `Cache-Control: no-store` và `Referrer-Policy: no-referrer`. Mọi POST Admin cần CSRF token của phiên. Không cho thay gói/reset quota cho admin; tài khoản không tồn tại trả 404, gói lọc không hợp lệ trả 400. Gói được cấp thủ công không xử lý thanh toán hoặc tự gia hạn; `tierExpiresAt` được xóa khi quản trị viên cấp lại gói.

System key khởi tạo từ `GEMINI_API_KEY`; cập nhật trên giao diện chỉ giữ trong bộ nhớ đến khi khởi động lại, không lưu database, cookie hoặc session. Ping kiểm tra System Key đơn; RAG hiện có cơ chế nhiều key/xoay vòng riêng nên kết quả này không đại diện cho toàn bộ pool hoặc model Planner. Quản lý thành viên nhóm tối đa 5 người thuộc module Auth của Trường; nút kích hoạt SQUAD chỉ cấp gói.

Số liệu được lấy khi tải/làm mới trang. Quota reset theo ngày theo cơ chế lười có sẵn, không thêm cron hoặc polling. Tổng RAG là tổng bộ đếm hiện hành, vì vậy thao tác hỗ trợ reset sẽ giảm số KPI; đây không phải sổ lịch sử sử dụng hay doanh thu.

## Kiểm thử và bàn giao

`AdminHubTest` kiểm tra render Thymeleaf, xác thực quyền từ DB, CSRF ở tất cả POST Admin, JWT cookie, đổi gói/refresh phiên, SQUAD, reset quota, lọc/tìm kiếm, tỷ lệ trả phí và che key. `ApiKeyServiceTest` giả lập HTTP để kiểm tra payload/header và lỗi nhà cung cấp. `SubscriptionQuotaConcurrencyTest` kiểm tra khóa bộ đếm.

Chạy `mvn clean test`. Máy dùng Java 25 cần `mvn clean test -DargLine=-Dnet.bytebuddy.experimental=true` để thư viện mock hiện có nhận phiên bản Java; không thay dependency của dự án. Không dùng key thật trong test.

PR phải để nháp trong khi chờ chủ tài khoản yêu cầu kiểm thử. Workflow hiện tại bỏ qua bước tự merge khi PR là nháp; không chuyển sang sẵn sàng hoặc merge trước yêu cầu đó.
