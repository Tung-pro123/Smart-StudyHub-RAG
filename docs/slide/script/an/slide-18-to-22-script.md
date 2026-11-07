# 🎙️ LỜI THOẠI THUYẾT TRÌNH: SLIDE 18 ➔ SLIDE 22 (NGÔ ÂN - ADMIN COMMAND CENTER, DOANH THU & QA)
> **Khối trình bày**: Khối 6 — Vận hành Admin, Phân hệ Doanh thu VNPay & Đối soát đơn hàng, Nghiệm thu QA 333 tests, Hướng dẫn cài đặt & Kết luận  
> **Thời lượng**: 4 phút liên tục  
> **Tác phong**: Vững vàng, chính xác, đĩnh đạc khi chốt hạ toàn bộ buổi thuyết trình  

---

### [SLIDE 18 — ADMIN DASHBOARD, DOANH THU VNPAY & ĐỐI SOÁT ĐƠN HÀNG] (~50 giây)
"Em xin cảm ơn bạn Thắng. Kính chào quý Thầy Cô trong Hội đồng Giảng viên và toàn thể các bạn, em là **Ngô Ân**, đảm nhiệm vai trò Quản trị hệ thống Admin, Phân hệ Doanh thu VNPay và Đảm bảo chất lượng QA của Nhóm 2.

Để một nền tảng hỗ trợ học tập SaaS phục vụ hàng trăm sinh viên vận hành chuyên nghiệp, vai trò của người quản trị viên là vô cùng quan trọng. Em đã xây dựng **Trung tâm điều hành Admin Command Center**. 
Tại đây, bên cạnh việc theo dõi sức khỏe hệ thống qua các chỉ số sinh viên, tài liệu hay lượt gọi AI RAG, em đã phát triển trọn vẹn **Phân hệ Quản trị Doanh thu & Cổng Thanh toán Trực Tuyến VNPay Sandbox**.
Hệ thống kết nối trực tiếp với cổng VNPay Sandbox qua mã định danh chính thức:
- Thẻ KPI Doanh thu tự động nhảy số theo thời gian thực, bóc tách doanh thu gói Pro 49.000đ, gói Squad 129.000đ và tổng thu trong ngày.
- Bảng Lịch sử đơn hàng lưu vết đầy đủ từng mã đơn hàng `TxnRef`, tài khoản sinh viên, ngân hàng và mã giao dịch VNPay.
- Em đã trang bị nút bấm **Xuất Báo Cáo Tài Chính CSV** chuẩn UTF-8 BOM để kế toán mở trực tiếp trên Excel không lỗi font tiếng Việt, cùng liên kết đối chiếu 1-1 với **Cổng Quản Trị VNPay Merchant Portal** tại địa chỉ `sandbox.vnpayment.vn/merchantv2` để chứng minh giao dịch thật 100%.
- Bên cạnh đó, trang quản trị còn hỗ trợ linh hoạt nâng/hạ gói cước và khôi phục hạn ngạch RAG tức thì cho sinh viên."

---

### [SLIDE 19 — CENTRALIZED SYSTEM KEY & MASKING] (~45 giây)
"Một thách thức kỹ thuật lớn trong các ứng dụng AI là việc quản lý API Key. Nếu bắt từng sinh viên phải tự đi đăng ký thẻ và lấy Google Gemini Key thì rào cản tiếp cận sẽ rất lớn. 

Nhóm em đã triển khai cơ chế **Centralized System Key**: Quản trị viên chỉ cần cấu hình khóa API một lần duy nhất tại trang Cài đặt hệ thống, toàn bộ ứng dụng sẽ tự động chia sẻ chìa khóa an toàn này cho Chatbot RAG, Quiz Generator và Study Planner. 
Để bảo vệ tài nguyên của nhóm khi trình chiếu hoặc quay video demo trước hội đồng, hệ thống kích hoạt cơ chế **API Key Masking** — tự động che giấu các ký tự nhạy cảm dạng dấu sao. Em cũng tích hợp nút bấm **Ping Test**, cho phép đo độ trễ mạng thực tế tính bằng mili-giây trực tiếp tới máy chủ Google AI. Vào đợt kiểm thử ngày 04/11, hệ thống đã đo được kết nối thực tế đạt 1206 mili-giây rất ổn định."

---

### [SLIDE 20 — BÁO CÁO QA 333 TESTS PASS & VẬN HÀNH THỰC CHIẾN] (~45 giây)
"Về mặt chất lượng mã nguồn, nhóm em cam kết mang tới một sản phẩm không chỉ đẹp về giao diện mà còn vững chắc về kiến trúc Backend. 

Hệ thống sở hữu bộ kiểm thử tự động toàn diện với **333 trên 333 Unit và Integration Tests PASS tuyệt đối 100%**, bao gồm toàn bộ các bài test kiểm thử tích hợp luồng thanh toán VNPay và tính toán doanh thu. 
Chúng em đã kiểm thử kỹ lưỡng các bài toán thực tế hóc búa nhất:
- Kiểm thử toàn vẹn thanh toán VNPay: Sinh URL mã hóa HMAC-SHA512, xác thực phản hồi Sandbox, tính toán doanh thu và tự động gia hạn cộng dồn 30 ngày cho sinh viên.
- Kiểm thử chịu tải đồng thời chống gian lận vượt quá 50 câu RAG/ngày.
- Kiểm thử cách ly môn học cá nhân và đề thi Solo trong môn nhóm Study Squad.
- Kiểm thử phân quyền RBAC chặn triệt để sinh viên truy cập trái phép vào trang quản trị.
- Và kiểm thử thuật toán chống rò rỉ điểm số cá nhân lên bảng xếp hạng chung."

---

### [SLIDE 21 — USER GUIDE SETUP & CHẠY ỨNG DỤNG] (~45 giây)
"Nhằm đáp ứng tiêu chí hướng dẫn cài đặt trong đề bài Assignment, nhóm em đã tối ưu hóa quy trình triển khai ứng dụng xuống mức đơn giản nhất:
Môi trường chỉ yêu cầu Java 17 và Maven 3.8. Toàn bộ cơ sở dữ liệu H2 được nhúng sẵn và tự động sinh tệp tin bền vững trên đĩa cục bộ, Thầy Cô không cần phải tốn công cài đặt MySQL hay Docker.

Người dùng chỉ cần thực hiện 3 bước:
1. Clone mã nguồn từ GitHub.
2. Điền Client ID và Gemini Key vào file môi trường `.env`.
3. Và chạy duy nhất một dòng lệnh: `mvn spring-boot:run`. 
Ứng dụng sẽ tự động khởi động tại cổng 8080 cùng toàn bộ dữ liệu mẫu đã được seed sẵn."

---

### [SLIDE 22 — TỔNG KẾT, LỜI CẢM ƠN & BƯỚC VÀO Q&A] (~35 giây)
"Thưa quý Thầy Cô, bằng việc kết hợp cả hai đề bài Assignment và Project cuối kỳ vào một sản phẩm hoàn chỉnh duy nhất mang tên **Smart StudyHub RAG**, Nhóm 2 chúng em không chỉ giải quyết trọn vẹn bài toán học tập thực tế của sinh viên mà còn làm chủ được quy trình lập trình hiện đại với UI Toolkit trong Thymeleaf và AI Agent trong IDE.

Thay mặt toàn thể 6 thành viên Nhóm 2, em xin gửi lời cảm ơn chân thành và sâu sắc nhất tới quý Thầy Cô trong Hội đồng Giảng viên đã lắng nghe bài thuyết trình của nhóm. 

Sau đây, toàn thể nhóm chúng em xin trân trọng kính mời quý Thầy Cô bước vào phần Hỏi - Đáp (Q&A). Chúng em xin chân thành cảm ơn!"
