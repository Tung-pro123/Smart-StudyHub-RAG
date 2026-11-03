# Báo cáo Admin UI và phân trang backend — Ân — 03/11/2026

## Kết luận

**Đã triển khai TASK-AN-02/03/04. Regression Admin: 5 PASS, ADM-06 BLOCKED.**

- Toàn bộ test dự án: **307 tests, 0 failures, 0 errors, 0 skipped — BUILD SUCCESS**.
- Riêng `AdminHubTest`: **40/40 PASS**, gồm kiểm thử phân trang 1.000 sinh viên và trường hợp trang cuối mất bản ghi sau đổi gói.
- 16 phép đo responsive (4 trang × 4 kích thước): không có tràn ngang trang. Dữ liệu đo: [responsive-checks.json](responsive-checks.json).
- Chưa push, tạo PR hay merge. Không đánh dấu TASK-AN-01 và toàn bộ TASK-AN-05 hoàn tất vì chưa có Gemini key hợp lệ.

## Môi trường và phạm vi

- Nhánh local: `feature/admin-ui-pagination`; nền `main@eb0e885b380ff2dc1a86d5e3f7b1b2b9763d450a`.
- Ngày kiểm thử: 03/11/2026 (GMT+7); Brave, JDK 25.0.4, Spring Boot 3.3.4.
- Ứng dụng QA: `http://localhost:8081`, database riêng `target/admin-ui-20261103.mv.db`. Không sửa database làm việc tại `data/`.
- Không có `.env` hoặc biến môi trường Gemini trong phiên QA. Chỉ sử dụng khóa giả để kiểm tra validation/masking và nhánh từ chối, không coi đó là key hợp lệ.
- Không sửa `templates/layout/*`, `templates/fragments/*`, ảnh ngày 02/11, cấu hình chạy, dependency, schema hay phân quyền hiện có.

## Thay đổi thực hiện

1. **Dashboard:** White-Card theo mẫu dự án; KPI chính đếm sinh viên, số tài khoản toàn hệ thống là số bổ sung; giữ công thức gói trả phí và dữ liệu GMT+7. Bỏ nút giám sát và mọi liên kết chi tiết môn học trong nội dung Dashboard. Hoạt động môn mới chỉ là thông tin vận hành, tối đa 5 mục.
2. **Danh sách sinh viên:** `UserRepository.searchStudents` trả `Page<User>` với `Pageable`; controller truy vấn **15 mục/trang từ database**, lọc tên/username/email và gói trước khi phân trang, sắp xếp username rồi ID. Không tải toàn bộ rồi giấu dòng bằng JavaScript.
3. **Điều hướng:** Trang trước/sau và tối đa 5 số trang quanh trang hiện tại; tìm/lọc mới về trang đầu; giữ từ khóa/gói/trang sau nâng/hạ gói hoặc reset. Nếu thao tác làm trang cuối rỗng, tự trả về trang còn dữ liệu. Danh sách rỗng vẫn render đúng. Trang âm, không phải số hoặc gây tràn offset trả 400.
4. **Giao diện danh sách:** Bảng desktop chuyển thành từng hàng dạng thẻ trên màn hình nhỏ; tên/email dài xuống dòng; chỉ hiện thao tác khi mở `details`, vẫn dùng các POST và CSRF hiện có. Ngày hết hạn `dd/MM/yyyy`; không lộ ID kỹ thuật trong nội dung.
5. **Settings/Error:** Thẻ trắng, nút gradient theo mẫu, guidance cấu hình lâu dài thu gọn, spinner khi ping. Kết nối thành công luôn màu xanh kể cả chậm; latency hiển thị riêng. Nhánh thất bại vẫn màu đỏ. Không sửa service Gemini hoặc giả lập thành công trên giao diện thật.

Spec cập nhật tại [admin-hub.md](../../../admin-hub.md); vấn đề phối hợp nằm trong [sổ theo dõi](../../../issues/ISSUES-TRACKING.md).

## Regression trên trình duyệt

- **ADM-01 PASS:** Đăng nhập `student`, truy cập `/admin/dashboard`: 403 và thông báo chỉ quản trị viên được truy cập. Nút Về trang chủ đưa về không gian sinh viên. [Ảnh](E2E-ADM-01_an.png).
- **ADM-02 PASS:** `admin` đăng nhập vào Dashboard; trước khi thêm dữ liệu giả: 6 sinh viên, 8 tài khoản, FREE 2/PRO 1/SQUAD 3, 9 môn, 0 chunks, RAG 130, trả phí 67%. Nút Làm mới số liệu hoạt động. Nội dung chính không còn link `/courses`. [Ảnh](E2E-ADM-02_an.png).
- **ADM-03 PASS:** Dropdown FREE/PRO/SQUAD, từ khóa `qa-page-`, tìm họ `Nguyễn`, xóa bộ lọc và không có kết quả đều đúng. Có đúng 15 hàng dữ liệu ở mỗi trang đầy. [Tìm theo tên](E2E-ADM-03_an.png), [FREE](filter-free_an.png), [PRO](filter-pro_an.png), [SQUAD](filter-squad_an.png), [Rỗng](users-empty_an.png).
- **ADM-04 PASS:** `student` FREE → PRO → SQUAD → FREE, thông báo/badge/quota thay đổi đúng và giữ tìm kiếm tên. [PRO](upgrade-pro_an.png), [SQUAD](upgrade-squad_an.png), [FREE](E2E-ADM-04_an.png).
- **ADM-05 PASS:** SQL chỉ trong database QA đặt `student` 50 lượt hôm nay; bấm Reset RAG đưa về 0/50. Đã xác minh database sau thao tác. [Trước reset](reset-before_an.png), [Sau reset](E2E-ADM-05_an.png).
- **ADM-06 BLOCKED:** Thiếu key hợp lệ. Nhánh thiếu key trả lỗi, không crash; input rỗng bị validation trình duyệt chặn; `bad secret` bị server từ chối; key giả đúng dạng chỉ phản chiếu `AIzaSy...****`, trường nhập bị xóa sau redirect. Ping key giả trả thông báo lỗi và 503 ms. Thành công thực tế Google Gemini chưa nghiệm thu. [BLOCKED/thiếu key](E2E-ADM-06_an.png), [validation](settings-invalid-key_an.png), [masking](settings-masked-key_an.png), [ping key giả](settings-invalid-ping_an.png).

### Phân trang và dữ liệu lớn

Tạo 1.000 tài khoản giả `qa-page-0001..1000`, email miền `example.test`, trong database QA riêng. Không tạo vào dữ liệu gốc hoặc commit database. Có cả tên dài và ba gói.

- Toàn bộ danh sách: 1.006 sinh viên (1.000 QA + 6 demo), tối đa 15 hàng DOM/trang.
- Tìm riêng `qa-page-`: 1.000 kết quả, 67 trang. Trang 2 có 15 kết quả; trang cuối có 10.
- FREE trong dữ liệu QA: 334; PRO: 333; SQUAD: 333. Bộ lọc được giữ khi chuyển trang.
- Request `page=999` khi tìm dữ liệu QA trả nội dung trang cuối 67/67; Trang trước chuyển về 66/67.
- Bằng chứng: [Trang 2](pagination-page2_an.png), [Trang 2 có bộ lọc](pagination-filter-page2_an.png), [Trang cuối](pagination-last-page_an.png).
- Test tự động còn xác minh đổi gói bản ghi duy nhất ở trang cuối làm trang đó rỗng, tự lùi về trang còn dữ liệu; `size=1000` không mở rộng giới hạn 15.

## Responsive và ảnh giao diện

Từng trang được kiểm tra tại **1920×1080, 1366×768, 768×1024, 375×812**. Không có `scrollWidth > clientWidth` ở document; bảng lớn vẫn tối đa 15 hàng. Native `details` mở/đóng thao tác và guidance không cần script riêng.

- Dashboard: [1920](dashboard-1920_an.png), [1366](dashboard-1366_an.png), [768](dashboard-768_an.png), [375](dashboard-375_an.png).
- Sinh viên (có dữ liệu QA và tên dài): [1920](users-1920_an.png), [1366](users-1366_an.png), [768](users-768_an.png), [375](users-375_an.png).
- Settings: [1920](settings-1920_an.png), [1366](settings-1366_an.png), [768](settings-768_an.png), [375](settings-375_an.png).
- Lỗi: [1920](error-1920_an.png), [1366](error-1366_an.png), [768](error-768_an.png), [375](error-375_an.png).

Ảnh là capture thực tế từ trình duyệt, chuyển định dạng sang PNG, không sửa nội dung. Một số ảnh chụp viewport (để kiểm tra breakpoint), nên không thể hiện toàn bộ danh sách dài; số hàng và phân trang còn được đối chiếu DOM và test backend. Ảnh Dashboard/403 chụp toàn trang. Không thay ảnh cũ ngày 02/11.

## Kiểm thử tự động và giới hạn

Lệnh: `mvn -B -Dnet.bytebuddy.experimental=true test` (tùy chọn tương thích Mockito/Byte Buddy cho JDK 25 hiện có).

- Kết quả cuối **307/307 PASS**, trong đó Admin **40/40**. [Surefire Admin](AdminHubTest-summary.txt), [tóm tắt toàn bộ](full-test-summary.txt).
- Bao gồm role từ database, CSRF, guest/student bị chặn, bảo vệ tài khoản admin, tìm kiếm/lọc, phân trang 1.000 bản ghi, thứ tự ổn định, dữ liệu rỗng, trang sai, giữ redirect, quota và thành công Gemini chậm 3.500 ms vẫn xanh.
- Mock Gemini chỉ kiểm chứng logic/render trong test tự động, không đóng ADM-06.
- Log đầy đủ local: `target/admin-ui-final-tests.log`; log app: `target/admin-ui-server.log`. Chưa chạy CI remote vì chưa push.
- Không benchmark tải đồng thời 1.000 người dùng; 1.000 ở đây là số bản ghi danh sách. Không kiểm thử trình duyệt khác hoặc Gemini thật khi chưa có key hợp lệ.

## Bàn giao

`student` QA được khôi phục FREE, 5 lượt như trước test; đã xác minh reset lưu về 0 trước khi phục hồi. Database QA và dữ liệu giả nằm dưới `target/` bị Git ignore. Tiến trình QA được dừng khi bàn giao để xóa key giả trong bộ nhớ.

Navbar vẫn có Tạo Môn Mới và sidebar vẫn có Môn Học Hệ Thống do file dùng chung thuộc Lead. Đã bỏ điều hướng học tập trong Dashboard của Ân, không dùng CSS che tạm và không sửa `layout/fragments`. Lead cần đồng bộ phần dùng chung để đạt mô hình Admin system-only toàn ứng dụng.

**Chưa push/PR/merge. ADM-06 vẫn BLOCKED theo chỉ đạo người dùng.**
