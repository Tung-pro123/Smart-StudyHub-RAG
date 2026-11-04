# 🎙️ LỜI THOẠI THUYẾT TRÌNH: SLIDE 1 ➔ SLIDE 4 (LÊ THANH TÙNG - TEAM LEAD)
> **Khối trình bày**: Khối 1 — Mở đầu toàn bộ bài thuyết trình  
> **Thời lượng**: 4 phút  
> **Tác phong**: Đĩnh đạc, rõ ràng, truyền cảm hứng của một Team Lead  

---

### [SLIDE 1 — TRANG BÌA] (~40 giây)
"Kính chào Thầy/Cô trong Hội đồng Giảng viên và toàn thể các bạn sinh viên có mặt trong buổi bảo vệ ngày hôm nay. 

Em tên là **Lê Thanh Tùng**, đại diện cho **Nhóm 2** lớp HSF302. Hôm nay, nhóm chúng em rất vinh dự được trình bày đồ án tốt nghiệp môn học mang tên: **SMART STUDYHUB RAG — Nền Tảng Quản Lý Học Tập Đa Môn & Luyện Thi A+ Ứng Dụng RAG AI**.

Dự án được xây dựng bởi 6 thành viên, với mô hình kiến trúc phân hệ độc lập (Vertical Slice) chuẩn doanh nghiệp. Em đảm nhiệm vai trò Project Manager & Tech Lead điều phối dự án. Đi cùng em hôm nay là 5 kỹ sư phát triển tài năng: bạn Ngô Nhật Trường phụ trách Auth và UI, bạn Nguyễn Văn Gia Bình phụ trách RAG Engine, bạn Huỳnh Nhật phụ trách Quiz Engine, bạn Lê Nguyễn Minh Thắng phụ trách Study Planner, và bạn Ngô Ân phụ trách Admin Command Center cùng Đảm bảo chất lượng QA."

---

### [SLIDE 2 — BỐI CẢNH & GIẢI PHÁP] (~60 giây)
"Thưa Thầy Cô, xuất phát điểm của đề tài bắt nguồn từ chính nỗi đau thực tế của sinh viên chúng em mỗi mùa ôn thi. 

Khi đối mặt với các môn chuyên ngành như HSF302 hay PRJ301, sinh viên thường bị ngợp trước hàng trăm trang slide và giáo trình PDF cồng kềnh. Khi tìm kiếm sự trợ giúp từ ChatGPT hay các AI thông thường, AI thường xuyên gặp phải hiện tượng 'ảo giác' — tức là tự bịa ra kiến thức, không hề có số trang đối chiếu để sinh viên dám tin cậy. Thêm vào đó, việc tự học một mình rất cô đơn và dễ nản lòng.

Chính vì vậy, nhóm chúng em đã phát triển **Smart StudyHub RAG**. Giải pháp của chúng em giải quyết trọn vẹn 3 vấn đề trên:
Thứ nhất, một không gian học tập đa môn cá nhân hóa.
Thứ hai, bộ máy RAG AI đối chứng giáo trình 100%, trả lời chuẩn xác kèm số trang thực tế.
Và thứ ba, mô hình Study Squad giúp nhóm 5 bạn cùng chia sẻ tài liệu và thi đua bảng xếp hạng Leaderboard."

---

### [SLIDE 3 — KIẾN TRÚC & 3 GÓI CƯỚC] (~60 giây)
"Về mặt công nghệ, hệ thống vận hành trên nền tảng Spring Boot 3.3.4, Java 17, Spring Data JPA kết hợp cùng cơ sở dữ liệu H2 lưu file đĩa và thư viện Apache PDFBox để bóc tách tài liệu.

Điểm đặc biệt trong thiết kế sản phẩm của chúng em là mô hình phân tầng **3 Gói Cước dịch vụ** thực tế:
- Đầu tiên là **Gói Free 0 đồng**: Bất kỳ sinh viên nào cũng có thể trải nghiệm tối đa 3 môn học, 50 câu hỏi RAG mỗi ngày.
- Thứ hai là **Gói Pro chỉ 49 nghìn đồng/tháng**: Mở khóa không giới hạn môn học, RAG vô hạn và mở khóa tính năng lập lịch AI Study Planner cùng xuất đề thi PDF chuẩn in ấn.
- Và thứ ba là **Gói Squad 129 nghìn đồng/tháng dành cho nhóm 5 bạn**: Cho phép lập nhóm học tập, cùng sử dụng tài nguyên môn học dùng chung."

---

### [SLIDE 4 — SQUAD HUB & FEATURE GATING — CHUYỂN MIC] (~80 giây)
"Một trong những phân hệ đột phá nhất của chúng em chính là **Shared Squad Hub**. 
Trưởng nhóm có thể tạo ra một mã mời 6 ký tự để các thành viên tham gia. Khi được phê duyệt, một môn học nhóm mang tiền tố `[SQUAD]` sẽ xuất hiện đồng bộ trong Dashboard của cả 5 bạn. Các bạn có thể cùng nhau thi trắc nghiệm và đua top trên Squad Leaderboard, hoặc chuyển sang chế độ làm đề Solo riêng tư khi cần tự luyện.

Bên cạnh đó, hệ thống được trang bị cơ chế **Feature Gating** chặt chẽ. Khi sinh viên gói Free chạm hạn mức 3 môn học hoặc muốn mở khóa AI Study Planner và Xuất đề thi PDF, một **Modal Bảng Giá SaaS 3 Gói Cước** hiện đại sẽ tự động hiển thị. Sinh viên có thể nâng cấp trực tiếp gói Pro hoặc gói Squad chỉ với 1 click, hệ thống tự động xử lý và cập nhật quyền hạn ngay lập tức trong phiên làm việc.

Để quý Thầy Cô có cái nhìn chi tiết hơn về cách nhóm ứng dụng UI Toolkit trong Thymeleaf và quy trình pair-programming cùng AI Agent trong IDE, em xin trân trọng kính mời bạn **Ngô Nhật Trường** tiếp nối phần trình bày của mình. Xin mời Trường!"

