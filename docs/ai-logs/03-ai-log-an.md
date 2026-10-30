# Nhật ký dùng AI — Ân

- Module: Settings & Security.
- Công cụ: Codex.
- Ngày: 19/10/2026.

## Yêu cầu thực tế

> Tôi là Ân và đây là bảng phân công công việc của nhóm tôi. Với vai trò là một người chuyên giải quyết về phần settings, hãy giúp tôi hoàn thành công việc trên. Lưu ý, hãy đọc kĩ code để nắm rõ luồng làm việc trước khi làm, sau khi hoàn thành hãy push code lên với nhánh mới đặt tên theo format feature/noi-dung-cong-viec, chưa cần tạo pull request.

## Giao diện

Codex đọc master layout, navbar/sidebar và trang hồ sơ trước khi tạo `templates/settings/index.html`. Trang có chọn provider, ô key dạng password, nút ẩn/hiện, lưu, kiểm tra độ trễ, xóa và hướng dẫn lấy key. Script nằm trong fragment `section` để được master layout đưa vào trang.

## Backend

Codex đọc luồng JWT/session/logout và đặc tả module. Bổ sung DTO, RestClient với timeout, ApiKeyService, SettingsController. Key chỉ lưu session, không phản chiếu key vào HTML/lỗi; giữ hợp đồng `GEMINI_API_KEY` cho các module dùng Gemini và chống gửi nhầm key khi đổi provider. Các POST dùng token chống CSRF.

## Kiểm tra và xử lý lỗi

Tạo kiểm thử giả lập API cho các provider và lỗi HTTP/mạng; kiểm thử render Thymeleaf, session, logout và CSRF bằng MockMvc. Lần chạy đầu phát hiện Java 25 mặc định không tương thích Lombok 1.18.36; dùng Java 21 có sẵn cho lần chạy kiểm thử mà không đổi pom hoặc Java mặc định của máy.

Không thực hiện ping bằng API key thật. Không có prompt bổ sung hay ảnh chụp hội thoại được tạo trong nhật ký này; Ân có thể bổ sung ảnh minh chứng từ cuộc hội thoại hiện tại và nhận xét cá nhân sau khi demo.

Kết quả: `mvn test` bằng Java 21 thành công, 43 tests, 0 failures/errors/skipped; trong đó 28 tests mới của Settings. Kiểm tra `git diff --check` không phát hiện lỗi whitespace.

## Rà soát trước khi hợp nhất

Cập nhật nhánh từ main sau khi nhóm bổ sung OAuth2 và Quiz. Kiểm thử tích hợp phát hiện bộ lọc Spring Security chặn phiên JWT/session trước khi đến Settings. Bổ sung chuỗi bộ lọc chỉ áp dụng tuyến Settings để giữ xác thực qua `JwtAuthInterceptor` và kiểm tra CSRF trong controller. Bổ sung kiểm thử khôi phục phiên từ JWT cookie và chặn POST chưa đăng nhập; điều chỉnh kiểm tra chuyển hướng logout theo Spring Security mới (`/login?logout`).

Kết quả cuối: `mvn clean test` trên Java 21 thành công với 50 tests, 0 failures/errors/skipped, gồm 32 tests Settings và 18 tests từ main.

## Sprint 30/10/2026 — Admin Hub

Yêu cầu: đọc `docs/tasks/task-2026-10-30.md`, triển khai công việc của Ân trên nhánh feature mới, chạy test và push, chưa tạo PR.

Đã đọc luồng User/Auth/JWT, Settings, các điểm lấy key trong RAG/Quiz/Planner và đặc tả BR-ADMIN/BR-TIER. Tạo nhánh `feature/admin-hub` với dashboard KPI, quản lý sinh viên FREE/PRO, service quota/reset ngày Việt Nam và cấu hình/ping System Key. Thêm các trường User theo cấu trúc đã thống nhất trong task Auth để nhánh chạy độc lập; cần đối chiếu khi hợp nhất nhánh Auth.

Bỏ form key cá nhân, chuyển các caller hiện tại sang nguồn key chung để không làm đứt luồng AI. Quản trị viên thay key trong bộ nhớ máy chủ, key bền vững vẫn lấy từ môi trường. Không thêm thanh toán, cron hoặc tính năng khóa Planner/Quiz thuộc task khác.

Kiểm thử bao gồm quyền Admin lấy từ DB, CSRF, khôi phục phiên từ cookie JWT, đổi gói có hiệu lực ở request tiếp theo, bảo mật key, KPI, reset ngày và giới hạn 50. Test quota đồng thời nạp cùng dữ liệu User cũ trong nhiều transaction để kiểm tra cập nhật counter dưới khóa hàng DB. Fixture RAG dùng tài khoản thật từ repository thay cho user ID giả để tương thích cơ chế làm mới phiên.

Kết quả: `mvn clean test` trên Java 21 thành công với 75 tests, 0 failures/errors/skipped; `git diff --check` đạt. Ping dùng mock, chưa chạy với key thật và chưa kiểm tra giao diện bằng trình duyệt.
