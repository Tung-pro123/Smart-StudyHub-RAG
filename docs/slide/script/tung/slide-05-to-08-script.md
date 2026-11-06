# 🎙️ LỜI THOẠI THUYẾT TRÌNH: SLIDE 5 ➔ SLIDE 8 (LÊ THANH TÙNG - TEAM LEAD: KIẾN TRÚC, 3 GÓI CƯỚC & SQUAD)
> **Khối trình bày**: Khối 2 — Bên trong hệ thống: Kiến trúc công nghệ, Mô hình 3 gói cước SaaS, Shared Squad Hub & Feature Gating  
> **Thời lượng**: 4 phút  
> **Tác phong**: Đĩnh đạc, chiến lược, truyền cảm hứng của một Team Lead  

---

### [SLIDE 5 — KIẾN TRÚC CÔNG NGHỆ CHUẨN ENTERPRISE] (~60 giây)
"Em xin cảm ơn bạn Trường đã giới thiệu rất rõ ràng về bối cảnh đề tài, nền tảng UI Toolkit và cánh cửa xác thực người dùng. 

Kính thưa quý Thầy Cô, sau khi người dùng đã bước qua cánh cửa đăng nhập để vào bên trong hệ thống, em xin đại diện nhóm trình bày về **Bức tranh kiến trúc công nghệ tổng thể** nâng đỡ toàn bộ nền tảng StudyHub.

Hệ thống được chúng em xây dựng theo mô hình kiến trúc phân hệ độc lập **Vertical Slice Architecture** trên nền tảng Spring Boot 3.3.4, Java 17 LTS và Spring Data JPA. 
Cơ sở dữ liệu H2 được cấu hình nhúng bền vững lưu trữ trực tiếp ra tệp tin đĩa cục bộ, đảm bảo tính toàn vẹn dữ liệu qua mỗi lần khởi động lại máy chủ mà không cần cài đặt các hệ quản trị phức tạp. 
Để xử lý tài liệu, chúng em tích hợp Apache PDFBox 3.0.3, và kết nối trực tiếp với Google Gemini 1.5 Flash API để cung cấp trí tuệ nhân tạo tốc độ cao cho toàn bộ ứng dụng."

---

### [SLIDE 6 — MÔ HÌNH KINH DOANH 3 GÓI CƯỚC SAAS] (~60 giây)
"Điểm đặc biệt trong tư duy thiết kế sản phẩm của chúng em là mô hình phân tầng **3 Gói Cước dịch vụ SaaS thực tế**:

- Đầu tiên là **Gói Free 0 đồng**: Dành cho bất kỳ sinh viên nào muốn tự học cá nhân, hỗ trợ tối đa 3 môn học và 50 câu hỏi RAG đối chứng mỗi ngày.
- Thứ hai là **Gói Pro chỉ 49 nghìn đồng/tháng**: Thiết kế cho sinh viên bước vào mùa ôn thi cao điểm — mở khóa không giới hạn môn học, RAG vô hạn và mở khóa đặc quyền lập lịch AI Study Planner cùng xuất đề thi PDF chuẩn in ấn.
- Và thứ ba là **Gói Squad 129 nghìn đồng/tháng dành cho nhóm 5 bạn**: Tính ra chỉ khoảng 25 nghìn đồng một bạn mỗi tháng, cho phép cả nhóm cùng chia sẻ tài nguyên giáo trình và thi đua bảng xếp hạng."

---

### [SLIDE 7 — PHÂN HỆ ĐỘT PHÁ: SHARED SQUAD HUB] (~60 giây)
"Một trong những phân hệ mang tính đột phá nhất của chúng em chính là **Shared Squad Hub**. 

Để học nhóm hiệu quả, Trưởng nhóm có thể sinh một mã mời độc nhất gồm 6 ký tự để gửi cho các thành viên. Khi Trưởng nhóm phê duyệt, một môn học nhóm mang tiền tố `[SQUAD]` sẽ xuất hiện đồng bộ trong Dashboard của tất cả thành viên. 

Toàn bộ slide bài giảng và tài liệu PDF chỉ cần một bạn tải lên là cả nhóm đều có thể truy cập và cùng nhau trò chuyện với AI RAG, tiết kiệm tối đa thời gian và chi phí lưu trữ cho sinh viên."

---

### [SLIDE 8 — FEATURE GATING, LEADERBOARD & SOLO — CHUYỂN MIC] (~60 giây)
"Bên cạnh đó, hệ thống được trang bị cơ chế **Feature Gating** chặt chẽ. Khi sinh viên gói Free chạm hạn mức 3 môn học hoặc muốn mở khóa AI Study Planner và Xuất đề thi PDF, một **Modal Bảng Giá SaaS 3 Gói Cước** hiện đại sẽ tự động hiển thị, cho phép nâng cấp tức thì hoặc thanh toán trực tuyến qua cổng VNPay Sandbox.

Trên không gian môn nhóm, sinh viên có thể cùng thi đấu trắc nghiệm và đua top trên **Squad Leaderboard**. Tuy nhiên, để bảo vệ quyền riêng tư học tập của từng bạn, chúng em đã phát triển chế độ **Đề thi Solo riêng tư**: Điểm số thi cá nhân sẽ được lưu độc lập và tuyệt đối không bị rò rỉ lên bảng xếp hạng chung của nhóm.

Tiếp theo, để đi sâu vào chi tiết công nghệ cốt lõi của đề tài — phân hệ RAG AI Chatbot giáo trình không ảo giác, em xin trân trọng kính mời bạn **Nguyễn Văn Gia Bình** lên trình bày. Xin mời Bình!"
