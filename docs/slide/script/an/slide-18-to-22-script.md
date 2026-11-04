# Lời thoại slide 18–22 — Ngô Ân

Một khối liên tục, mục tiêu 3 phút 30 giây: 45 + 45 + 45 + 45 + 30 giây. Tập nói với đồng hồ trước tổng duyệt; thời lượng là dự kiến.

## Chuẩn bị demo — khởi chạy thủ công (không tính vào 3 phút 30 giây)

### 1. Mở PowerShell và kiểm tra môi trường

```powershell
cd D:\FALL26\HSF302\02_Assignment\Smart-StudyHub-RAG
java -version
mvn -version
```

Dự án cấu hình Java 17, cần Maven 3.8 trở lên. Máy đã được kiểm tra có Java 25 và Maven 3.9.16; thông tin này không thay thế việc xác nhận ứng dụng khởi động thành công.

### 2. Chuẩn bị file môi trường

Chỉ sao chép mẫu nếu chưa có `.env`, tránh ghi đè cấu hình đang dùng:

```powershell
if (!(Test-Path .env)) { Copy-Item .env.example .env }
notepad .env
```

Điền key hợp lệ vào `GEMINI_API_KEY`. Nếu demo đăng nhập Google, điền thêm `GOOGLE_CLIENT_ID` và `GOOGLE_CLIENT_SECRET`; nếu chỉ đăng nhập tài khoản mẫu thì giữ placeholder Google. Lưu rồi đóng file. Không chiếu `.env` thật lên màn hình hoặc đưa key vào Git.

### 3. Chạy ứng dụng

```powershell
mvn spring-boot:run
```

Đợi dòng `Started AssignmentApplication` và thông báo Tomcat chạy cổng **6969** theo `application.properties` hiện tại. Giữ cửa sổ PowerShell mở suốt buổi demo. H2 tự tạo hoặc dùng lại database tại `./data/studyhubdb`, không cần cài máy chủ database riêng.

Nếu cần dùng **8080** thay cho cổng mặc định 6969, dùng lệnh sau thay cho lệnh trên:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8080"
```

Các URL bên dưới dùng cổng mặc định 6969; thay bằng 8080 nếu đã chọn lệnh thứ hai. Khi dùng Google OAuth, redirect URI phải khớp cổng thực tế, ví dụ `http://localhost:8080/login/oauth2/code/google`. Nếu cổng đã được sử dụng, kiểm tra ứng dụng đang chạy ở cửa sổ khác trước khi mở thêm tiến trình.

### 4. Đăng nhập và mở sẵn các tab

Mở [trang đăng nhập](http://localhost:6969/login), dùng tài khoản mẫu **admin / 123456**. Đây là mật khẩu khởi tạo cho database mới; tài khoản đã đổi mật khẩu phải dùng mật khẩu hiện tại.

- [Dashboard](http://localhost:6969/admin/dashboard).
- [Quản lý sinh viên](http://localhost:6969/admin/users).
- [Cấu hình Gemini](http://localhost:6969/admin/settings).

### 5. Chuẩn bị tình huống hỗ trợ sinh viên và kiểm tra AI

1. Tìm tài khoản demo `student`, đưa về FREE. Mật khẩu khởi tạo là `123456` nếu chưa bị đổi.
2. Mở cửa sổ ẩn danh, đăng nhập `student`. Trong môn đã có tài liệu, gửi một vài câu hỏi RAG thành công để bộ đếm hôm nay lớn hơn 0.
3. Quay về Admin, tải lại danh sách và xác nhận quota đã dùng. Giữ nguyên tới lúc demo; khi trình bày thực hiện **PRO → SQUAD → FREE → Reset RAG**.
4. Nếu muốn khớp câu “từ năm về không” trên slide 18, chuẩn bị đúng 5 lượt; nếu không, đọc số thực tế đang hiển thị.
5. Tại Settings, bấm **Kiểm tra kết nối tức thì** trước buổi trình bày. Nếu lỗi, kiểm tra key, quota hoặc mạng; không coi thông báo lỗi có số ms là ping thành công.

Chỉ đổi gói/reset tài khoản demo được phép sử dụng. Reset thay đổi dữ liệu thật và không tự phục hồi số lượt cũ. Key trong `.env` được nạp lúc khởi động; sửa file cần chạy lại ứng dụng. Key cập nhật trên giao diện chỉ giữ tới khi ứng dụng dừng.

### 6. Mở minh chứng và kết thúc buổi demo

- Mở sẵn [báo cáo test tổng duyệt ngày 04/11](../../../evidence/2026-11-04-final-presentation/an/test-summary.txt), trỏ dòng tổng kết và `BUILD SUCCESS`; đối chiếu số test với slide 20. Không chạy lại toàn bộ test trong 3 phút 30 giây.
- Mở `.env.example` chứa placeholder để minh họa cài đặt, không mở `.env` thật.
- Sau buổi demo, quay về PowerShell, nhấn **Ctrl+C** để dừng. Dữ liệu H2 vẫn được giữ cho lần chạy sau.

## Slide 18 — Quản trị hệ thống (45 giây)

Em cảm ơn bạn Thắng. Em là Ngô Ân, phụ trách quản trị và đảm bảo chất lượng. Dashboard hiển thị số sinh viên theo gói, môn học, chunks và lượt RAG trong ngày theo giờ Việt Nam. Số liệu được lấy khi tải trang; quản trị viên bấm Làm mới để cập nhật. Trang sinh viên hỗ trợ tìm kiếm, lọc gói và phân trang. Trong lần kiểm tra này, em đã đổi tài khoản mẫu từ Free sang Pro, Squad rồi về Free; reset lượt đã dùng từ năm về không và tải lại để xác minh. Đây là thao tác cấp gói thủ công, chưa phải thanh toán tự động.

## Slide 19 — System Key và ping (45 giây)

Hệ thống cho phép quản trị viên cấu hình khóa Gemini tập trung. Giao diện chỉ hiển thị một phần tiền tố và dấu sao để giảm nguy cơ lộ khóa khi demo. Khóa nhập tại trang quản trị nằm trong bộ nhớ máy chủ, không lưu vào database và mất khi khởi động lại. Muốn cấu hình lâu dài, nhóm dùng biến môi trường hoặc tệp .env cục bộ. Nút kiểm tra gửi một yêu cầu sinh nội dung ngắn đến Gemini; số mili giây gồm cả thời gian mạng và xử lý. Trong lần chạy thực tế ngày 4 tháng 11, kết nối Gemini thành công với thời gian phản hồi một nghìn hai trăm linh sáu mili giây, có ảnh minh chứng đã che khóa.

Độ trễ là kết quả một lần đo, không phải cam kết tốc độ mọi truy vấn.

## Slide 20 — Báo cáo chất lượng (45 giây)

Trên phiên bản mã nguồn kiểm tra ngày 4 tháng 11, Maven ghi nhận ba trăm hai mươi chín test, không thất bại, không lỗi và không bỏ qua; riêng Admin có bốn mươi test đạt. Con số này thay số ba trăm hai mươi bảy trong kế hoạch cũ. Các kiểm tra bao gồm phân quyền quản trị, CSRF, thay đổi gói, reset quota, phân trang, cách ly dữ liệu và đề thi Solo. Test đồng thời xác minh tranh chấp quota ở service, chưa phải phép đo tải toàn hệ thống. Kết quả tự động được kết hợp với thao tác trình duyệt và phép ping Gemini thật, mỗi phần có bằng chứng riêng.

## Slide 21 — Cài đặt và chạy (45 giây)

Để chạy dự án, nhóm chuẩn bị Java 17 theo cấu hình và Maven từ 3.8. Sau khi clone mã nguồn, chuyển vào thư mục dự án và sao chép .env.example thành .env. Điền Gemini API Key; đăng nhập Google còn cần cả Client ID, Client Secret và địa chỉ chuyển hướng đúng cổng đang chạy. Chạy mvn spring-boot:run rồi mở localhost cổng 6969 theo cấu hình hiện tại. H2 tự tạo file dữ liệu, không cần cài thêm máy chủ database. Database mới có tài khoản demo admin, mật khẩu một hai ba bốn năm sáu. Lần QA của em chạy trên JDK 25 với database riêng; khóa và file môi trường không được đưa lên Git.

## Slide 22 — Cảm ơn và hỏi đáp (30 giây)

Smart StudyHub kết hợp nội dung Assignment và Project trong một hệ thống: giao diện Thymeleaf với UI Toolkit, trợ lý RAG, xác thực Google, luyện đề, lập kế hoạch và học nhóm. Nhóm sử dụng kết quả kiểm thử cùng ảnh chụp từ ứng dụng để trình bày những gì đã thực hiện, đồng thời ghi rõ phần còn cần xác minh. Thay mặt Nhóm 2, em cảm ơn Thầy Cô đã hướng dẫn và lắng nghe. Sau đây, nhóm xin mời Thầy Cô đặt câu hỏi.

## Chuẩn bị vấn đáp

- **Vì sao không đưa khóa vào database?** System Key giữ trong bộ nhớ; môi trường triển khai cấp khóa lúc khởi động. Masking chỉ bảo vệ phần hiển thị.
- **Ai được đổi gói?** Controller đọc lại vai trò từ database; POST cần CSRF hợp lệ. Không chỉ dựa vào nút bị ẩn.
- **Ping có phải thời gian mạng?** Thời gian đầu cuối của một yêu cầu AI ngắn, gồm mạng và xử lý; không bảo đảm tốc độ mọi câu hỏi.
- **329 test có chứng minh hết bug?** Không. Đây là số test thực sự chạy ở phiên bản tổng duyệt; kết nối thật và kiểm thử trình duyệt có bằng chứng riêng.
- **Quota đồng thời được test ở đâu?** Test gọi `SubscriptionQuotaService.recordSuccessfulRagQuery`; không suy rộng thành kiểm thử tải mọi endpoint RAG.
