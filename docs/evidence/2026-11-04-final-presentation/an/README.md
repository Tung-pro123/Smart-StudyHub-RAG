# Nghiệm thu Admin và chuẩn bị thuyết trình — Ngô Ân — 04/11/2026

## Trạng thái

**Hoàn tất các task Admin, QA và nội dung/lời thoại slide 18–22. Ping Gemini thật thành công: 1206 ms.**

Phạm vi giao việc: Phần II/III (slide 18–22), Phần IV mục 5 của [kế hoạch](../../../tasks/task-2026-11-04-final-presentation-slide-and-rag-fix.md). Phần V sửa RAG giao cho Bình, không nằm trong thay đổi này.

## Task 1 — Dashboard: PASS

- Chạy lại trên `http://localhost:8080` bằng tài khoản demo Admin của ứng dụng, ngày 04/11/2026 (GMT+7).
- Dữ liệu do chính ứng dụng seed trong database QA mới, không sửa database `data/studyhubdb`.
- Ban đầu: 6 sinh viên, 8 tài khoản, FREE 2 / PRO 1 / SQUAD 3, 9 môn, 0 chunks, RAG hôm nay 130.
- Đối chiếu RAG với bảng sinh viên: 85 + 20 + 12 + 8 + 5 + 0 = 130. Trả phí 4/6, làm tròn 67%.
- KPI lấy khi tải trang; không phải cập nhật liên tục. Chunks bằng 0 là trạng thái seed hiện có, không giả số liệu để chụp ảnh.
- [Ảnh Dashboard](dashboard_an.png).

## Task 2 — Quản lý gói và reset quota: PASS

- Tài khoản `student`: FREE → PRO → SQUAD → FREE. Mỗi lần có thông báo thành công, badge và hiển thị quota đúng.
- [PRO](upgrade-pro_an.png), [SQUAD](upgrade-squad_an.png), [FREE](downgrade-free_an.png).
- Reset RAG từ 5/50 về 0/50; tải lại vẫn 0/50. [Ảnh sau tải lại](reset-quota_an.png).
- Tất cả thao tác trên database QA riêng; tài khoản giữ FREE khi kết thúc. Quota QA giữ 0 để minh chứng reset; dữ liệu gốc không thay đổi.

## Task 3 — System Key và ping: PASS

- Chưa có `.env`, `GEMINI_API_KEY` hoặc `GEMINI_API_KEYS` trong môi trường chạy ban đầu. Không tìm khóa trong nguồn ngoài phạm vi dự án.
- Bấm ping khi chưa có khóa: thông báo yêu cầu nhập khóa hợp lệ, 0 ms, không crash. [Ảnh](ping-missing-key_an.png).
- Người dùng tự nhập và lưu khóa hợp lệ tại Admin Settings. Đã bấm kiểm tra kết nối thật trên bản chạy `http://localhost:6969/admin/settings` ngày 04/11/2026 (GMT+7): thông báo **Kết nối thành công với GEMINI**, thời gian phản hồi **1206 ms**. [Ảnh ping thành công](ping-success_an.png). Không dùng mock hoặc khóa giả trong phép kiểm tra này.
- Ảnh cho thấy khóa chỉ hiển thị tiền tố cùng `...****`, ô nhập trống sau request và trạng thái thành công màu xanh. Không đọc hoặc đưa khóa đầy đủ vào chat, log hay Git. Không lưu khóa vào session và xử lý lỗi được kiểm chứng bổ sung bởi test mock có sẵn (`AdminHubTest`, `ApiKeyServiceTest`).
- Latency là thời gian đầu cuối của một yêu cầu AI ngắn, không chỉ độ trễ mạng. Có thể dùng quota/phát sinh phí theo tài khoản Gemini.
- Khóa nhập ở Admin Settings nằm trong RAM, không tồn tại sau restart. Cấu hình lâu dài qua biến môi trường hoặc `.env` cục bộ.

## Task 4 — Kiểm thử toàn dự án: PASS

- Nền mã nguồn `main@2404431`, nhánh `feature/admin-final-presentation-an`.
- Lệnh thành công: `mvn -B '-Dnet.bytebuddy.experimental=true' test` trên PowerShell/JDK 25.
- **329 tests, failures 0, errors 0, skipped 0; BUILD SUCCESS**. [Tóm tắt Maven](test-summary.txt).
- **AdminHubTest: 40/40 PASS**. [Surefire](AdminHubTest-summary.txt).
- Lần `clean test` đầu bị kẹt do sandbox chặn kết nối Java agent; đã dừng tiến trình test đó, chạy lại toàn bộ test ngoài sandbox. Không coi lần bị kẹt là PASS. Lỗi tách tham số PowerShell ban đầu đã xử lý bằng cách quote toàn bộ `-D...`.
- Bao gồm phân quyền/CSRF, đổi gói/reset, phân trang, cách ly dữ liệu, Solo/leaderboard và tranh chấp quota ở service. Không phải benchmark tải toàn ứng dụng hoặc kiểm chứng mọi endpoint RAG.
- Log đầy đủ local: `target/admin-final-tests.log`. Không đưa toàn bộ log có dữ liệu test lên Git; lưu tóm tắt cùng báo cáo Surefire.

## Task 5 — Nội dung và lời thoại: HOÀN TẤT

- [Nội dung slide 18–22](../../../slide/content/an/slide-18-to-22-content.md).
- [Lời thoại và câu hỏi vấn đáp](../../../slide/script/an/slide-18-to-22-script.md).
- Giữ đúng cấu trúc content/script có sẵn để ghép deck chung; chưa tạo PowerPoint riêng.
- Sửa 327 thành 329 theo lần chạy thực tế; mô tả Dashboard đúng cơ chế; bổ sung Google Client Secret; sửa demo Admin thành `admin / 123456` trên database mới.
- Bỏ cam kết không có bug/không ảo giác; bổ sung kết quả ping thật 1206 ms và ảnh đã che khóa. Lời thoại chia 45/45/45/45/30 giây; chưa đo thời gian người trình bày đọc thật.

## Môi trường và giới hạn

- JDK 25.0.4, Maven 3.9.16, Spring Boot 3.3.4. Java 17 là target trong pom; chưa chạy lại bằng JDK 17.
- Dashboard và đổi gói/reset chạy trên QA cổng 8080, database riêng `target/admin-final-20261104`. Ping thật chạy trên bản ứng dụng người dùng khởi động tại cổng 6969; đã đối chiếu classpath Java trỏ tới `D:\FALL26\HSF302\02_Assignment\Smart-StudyHub-RAG\target\classes`. Không đổi cổng hoặc restart bản chạy của người dùng để giữ khóa trong RAM.
- Brave; ảnh chụp trực tiếp trang đang chạy, không sửa nội dung hoặc dùng mockup. Không chụp lại toàn bộ breakpoint vì không thay đổi UI/code runtime.
- Không sửa controller/service, schema, dependency, dữ liệu sản xuất hoặc module của thành viên khác.
- Bàn giao bằng nhánh riêng và PR tiếng Việt, không push thẳng main. Chỉ đưa tài liệu/ảnh của Ân vào commit; bỏ qua log local và tệp phát sinh ngoài phạm vi.
