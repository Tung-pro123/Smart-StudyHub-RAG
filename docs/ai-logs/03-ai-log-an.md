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


## Sprint 31/10/2026 — Đồng bộ ba gói và mở rộng Admin Command Center

Yêu cầu: kiểm tra task của tài khoản AnNT206, hoàn thiện theo Ponytail, push và tạo PR; chờ chủ tài khoản yêu cầu kiểm thử trước khi merge.

Đã fetch main và đọc `docs/tasks/task-2026-10-31-pricing-tiers.md`. Nhánh `feature/pricing-admin-an` bổ sung thống kê sinh viên FREE/PRO/SQUAD, tỷ lệ trả phí không tính admin, hoạt động mới nhất, tìm/lọc sinh viên, kích hoạt SQUAD, reset RAG, che key và trạng thái ping. Tái sử dụng quyền xem/xóa môn trong Core và thêm nhãn chủ sở hữu; không thêm dependency hoặc cơ chế quản lý nhóm thuộc Auth.

Kiểm thử bổ sung cho endpoint mới, quyền/CSRF, tài khoản không tồn tại, bảo toàn role và counter khi đổi gói, tìm theo ba trường kết hợp gói, thống kê SQUAD, danh sách gần đây và che key ngắn. Lần chạy đầu phát hiện thư viện mock chưa hỗ trợ Java 25 mặc định; chạy với `-DargLine=-Dnet.bytebuddy.experimental=true` thay vì sửa dependency của nhóm. Bộ kiểm thử Settings đạt 46 ca, không lỗi hoặc bỏ qua. Không thực hiện ping thật hoặc tạo ảnh minh chứng giả.

Kết quả cuối: toàn bộ 126 kiểm thử đạt, 0 lỗi, 0 bỏ qua; git diff --check đạt. Chưa kiểm tra giao diện bằng trình duyệt hoặc ping Gemini thật. PR để nháp để bot không tự merge trước yêu cầu kiểm thử của chủ tài khoản.

## Kiểm thử trực tiếp PR #31 — 31/10/2026

Chạy localhost:8081 bằng H2 in-memory admin-ui-qa, không dùng database học tập hoặc key thật. Kiểm tra trình duyệt: đăng nhập Admin, dashboard 4 tài khoản/2 sinh viên/tỷ lệ trả phí 50%; các trang dashboard/users/settings; chuyển student FREE sang SQUAD, PRO và về FREE; tìm email kết hợp lọc FREE; reset bộ đếm 50/50 về 0/50; lưu key giả và che key, từ chối key có khoảng trắng, ping Gemini với key giả trả lỗi và dashboard hiện Offline.

Sinh viên bị chặn 403 trên cả ba trang Admin. Tạo môn QA31 thuộc student, Admin thấy nhãn Bởi: student và hoạt động mới nhất; xóa môn demo thành công, số môn trở lại 3. Kiểm tra khung hẹp khoảng 628px và rộng khoảng 1280px. Chưa xác nhận điện thoại 390px vì công cụ không áp dụng kích thước đó.

Phát hiện và sửa:
1. Spring Security xử lý logout nhưng chưa xóa STUDYHUB_JWT, khiến truy cập lại tự khôi phục quyền Admin. Bổ sung xóa cookie ở cấu hình logout. Xác minh sau sửa: mở dashboard chuyển về login?error=unauthorized. Thêm test cookie hết hạn, phiên bị hủy và không khôi phục quyền.
2. Lỗi Admin 403 hiện stack trace mặc định. Thêm xử lý lỗi riêng Admin và trang thông báo thân thiện, giữ HTTP 400/403/404. Xác minh trực tiếp và test không phản chiếu tên ngoại lệ.
3. Admin xem môn sinh viên bị gắn nhãn Môn của bạn. Sửa nhãn theo chủ sở hữu thực tế; test cả Admin và sinh viên.

Kết quả: mvn test -DargLine=-Dnet.bytebuddy.experimental=true đạt 128 tests, 0 failures/errors/skipped; git diff --check đạt. Chưa thử Gemini thành công bằng key thật, tải lớn hoặc xóa môn đã có PDF/quiz/planner. Ảnh cục bộ: target/admin-dashboard-ui.png, target/admin-users-desktop.png, target/admin-forbidden-ui.png. Giữ PR nháp chờ chủ tài khoản đánh giá trước merge.
