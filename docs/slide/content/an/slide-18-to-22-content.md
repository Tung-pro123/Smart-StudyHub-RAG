# Nội dung slide 18–22 — Ngô Ân

Khối 6, nói liên tục trong 3 phút 30 giây. Nguồn: main@2404431 và báo cáo nghiệm thu ngày 04/11/2026.

## Slide 18 — Quản trị hệ thống và gói cước

- Dashboard: sinh viên theo gói, môn học, chunks và lượt RAG hôm nay (GMT+7).
- Số liệu cập nhật khi tải trang hoặc bấm Làm mới; không tự cập nhật liên tục.
- Tìm kiếm, lọc và phân trang 15 sinh viên; quản lý FREE / PRO / SQUAD.
- Reset quota về 0 lượt đã dùng; đổi gói có hiệu lực từ request tiếp theo.

Ảnh thật tại `docs/evidence/2026-11-04-final-presentation/an/`: `dashboard_an.png`, `upgrade-pro_an.png`, `upgrade-squad_an.png`, `downgrade-free_an.png`, `reset-quota_an.png`.
Dữ liệu mẫu do ứng dụng seed trên database QA, không phải số liệu vận hành thực. Cấp gói thủ công không thu tiền hoặc tự gia hạn.

## Slide 19 — Khóa Gemini tập trung và kiểm tra kết nối

- Admin cấu hình System Key; giao diện che khóa dạng `AIzaSy...****`.
- Khóa nhập trên Admin Settings chỉ nằm trong bộ nhớ, không lưu database và mất khi khởi động lại.
- Cấu hình lâu dài qua `GEMINI_API_KEY` trong môi trường hoặc `.env` cục bộ.
- Ping gửi yêu cầu sinh nội dung ngắn; latency gồm cả mạng và xử lý của nhà cung cấp.

**Ping Gemini thật ngày 04/11/2026: thành công, 1206 ms.** Ảnh `docs/evidence/2026-11-04-final-presentation/an/ping-success_an.png`, chụp trên ứng dụng cổng 6969 sau khi người dùng tự cấu hình khóa. Khóa hiển thị đã che, ô nhập trống sau request. `ping-missing-key_an.png` lưu riêng nhánh lỗi trước khi cấu hình. Masking giảm nguy cơ lộ khóa trên giao diện, không bảo đảm bảo mật tuyệt đối.

## Slide 20 — Báo cáo kiểm thử ngày 04/11/2026

- **329 tests — 0 failures — 0 errors — 0 skipped — BUILD SUCCESS.**
- Riêng `AdminHubTest`: **40/40 PASS**.
- Phạm vi: phân quyền/CSRF; đổi gói/reset; phân trang; cách ly dữ liệu; đề Solo và bảng xếp hạng.
- Test tranh chấp quota ở service không phải benchmark tải toàn hệ thống.

Nguồn: `docs/evidence/2026-11-04-final-presentation/an/test-summary.txt`. Số 327 trong kế hoạch là số cũ. Không suy diễn PASS tự động thành mọi luồng thực tế đã nghiệm thu hoặc hệ thống không còn bug.

## Slide 21 — Cài đặt và chạy

- Java 17 theo cấu hình dự án; Maven 3.8+; H2 tự tạo file dữ liệu.
- Clone repository, chuyển vào thư mục dự án, sao chép `.env.example` thành `.env`.
- Điền `GEMINI_API_KEY`. Google OAuth cần cả `GOOGLE_CLIENT_ID` **và** `GOOGLE_CLIENT_SECRET`, cùng redirect URI `http://localhost:6969/login/oauth2/code/google`.
- Chạy `mvn spring-boot:run`, mở `http://localhost:6969` (cổng mặc định hiện tại; có thể ghi đè bằng `--server.port=8080`).
- Database mới: tài khoản demo `admin / 123456`; database cũ có thể khác.

Lần QA này dùng JDK 25.0.4, Maven 3.9.16, Spring Boot 3.3.4, database riêng `target/admin-final-20261104`; chưa kiểm chứng lại JDK 17. PowerShell/JDK 25 chạy test bằng `mvn -B '-Dnet.bytebuddy.experimental=true' test`. Không đưa `.env` hay khóa thật lên Git.

## Slide 22 — Tổng kết và hỏi đáp

- Sản phẩm tích hợp UI Toolkit/AI Agent, RAG, OAuth2, Quiz, Planner và Squad.
- Minh chứng bằng thao tác thực tế và kết quả kiểm thử phiên bản tổng duyệt.
- Cảm ơn Thầy/Cô; mời Hội đồng đặt câu hỏi cho toàn nhóm.

Không tuyên bố “0% ảo giác”, “không còn bug” hoặc “đạt 100% rubric” từ số test PASS. Bộ nội dung này dùng để ghép vào deck chung 22 slide; không thêm trang bìa riêng cho khối của Ân.
