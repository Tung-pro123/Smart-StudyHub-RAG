# Admin Hub — task của Ân ngày 30/10/2026

## Sử dụng

- Đăng nhập tài khoản có `ROLE_ADMIN`, chọn **Quản trị** trên menu. Dữ liệu demo hiện tại có tài khoản `truong` mang quyền này; việc bổ sung bộ tài khoản `admin`/`student`/`pro_student` thuộc task Auth của Trường.
- `/admin/dashboard`: 4 KPI (tài khoản, môn, chunks, RAG hôm nay), tỷ lệ PRO và kết quả ping gần nhất. Số liệu được truy vấn lại khi tải trang, không tự gửi ping tốn quota.
- `/admin/users`: danh sách sinh viên, gói và lượt RAG trong ngày Việt Nam. Nâng/hạ gói là cấp quyền thủ công, không xử lý thanh toán. Không đổi role hoặc xóa dữ liệu. Gói CAMPUS được giữ nguyên.
- `/admin/settings`: kiểm tra trạng thái System Key, thay key trong bộ nhớ máy chủ và ping. Key không được phản chiếu về HTML, lưu vào session hoặc database. Khởi tạo bền vững bằng `GEMINI_API_KEY` trong môi trường/server `.env`; thay key trên UI mất khi khởi động lại. Không commit `.env`.
- `/settings` cũ chuyển admin tới cấu hình hệ thống, sinh viên về danh sách môn. Các POST nhập key cá nhân cũ trả HTTP 410.

## Tích hợp

Thay đổi tối thiểu các điểm lấy key ở RAG/Quiz/Planner sang `SystemAiKeyService.getApiKey()` để key cập nhật từ Admin có hiệu lực ngay; loại key khỏi query URL (giữ header `x-goog-api-key`). Không thêm hạn ngạch hoặc feature gating riêng của các module khác trong nhánh này.

Các trường `subscriptionTier`, `dailyRagCount`, `lastRagResetDate`, `tierExpiresAt` được thêm đúng cấu trúc task Auth vì main chưa có chúng. JPA hiện dùng `ddl-auto=update`; bản ghi cũ có tier null được coi là FREE. Cần thống nhất khi merge nhánh Auth để không thêm trùng trường. Phiên đăng nhập được nạp lại từ DB ở request được bảo vệ tiếp theo, nên thao tác đổi gói và thu hồi quyền admin không cần đăng nhập lại.

API quota dùng cho task RAG:

```java
quotaService.canQueryRag(user.getId()); // reset ngày mới và kiểm tra FREE < 50
quotaService.recordSuccessfulRagQuery(user.getId()); // gọi đúng 1 lần sau câu trả lời thành công
```

Hai hàm dùng transaction và khóa hàng DB, không tin số đếm từ session. Hàm ghi trả false nếu đã hết quota, không tăng quá 50 cho FREE. PRO/CAMPUS/admin không bị giới hạn nhưng vẫn được đếm để thống kê. Reset lười ở lần truy cập quota đầu tiên trong ngày, múi giờ Asia/Ho_Chi_Minh. Không reset usage khi hạ gói, tránh vượt quota do nâng/hạ liên tục.

Lưu ý tích hợp RAG: kiểm tra trước và ghi sau là hai transaction riêng; các request đồng thời vẫn có thể gọi AI trước khi một lần ghi bị từ chối. Nếu cần ngăn cả chi phí gọi API vượt quota trong tình huống đó, task RAG cần cơ chế giữ chỗ/hoàn trả quanh lời gọi AI. Dashboard chỉ có số RAG chính xác khi caller ghi nhận lượt thành công qua service hoặc cập nhật cùng trường theo hợp đồng.

Admin được xác thực bằng interceptor JWT/phiên và kiểm tra lại `ROLE_ADMIN` trong DB ở mọi endpoint. Tất cả POST Admin cần token CSRF. Người dùng không được tự cấp role hoặc gói qua request binding.

## Kiểm thử

`mvn clean test`: test tích hợp thật với H2/MockMvc cho quyền truy cập, CSRF, cookie JWT, đổi gói, refresh phiên, reset ngày mới và KPI. Ping được mock để không dùng key thật hoặc phát sinh phí. Test API HTTP hiện có vẫn kiểm tra payload/header và xử lý lỗi nhà cung cấp.
