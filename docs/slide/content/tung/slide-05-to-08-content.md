# 📑 SLIDE CONTENT: SLIDE 5 ➔ SLIDE 8 (LÊ THANH TÙNG - TEAM LEAD: KIẾN TRÚC, 3 GÓI CƯỚC & SQUAD)
> **Khối trình bày**: Khối 2 — Bên trong hệ thống: Kiến trúc công nghệ, Mô hình 3 gói cước SaaS, Shared Squad Hub & Feature Gating  
> **Thời lượng**: 4 phút  
> **Tác phong**: Đĩnh đạc, tổng quan chiến lược, nắm chắc toàn bộ hệ thống  

---

### SLIDE 5: KIẾN TRÚC CÔNG NGHỆ TỔNG THỂ (TECH STACK ENTERPRISE)
* **Bức tranh công nghệ (Tech Stack chuẩn Enterprise)**:
  * **Backend**: Java 17 LTS, Spring Boot 3.3.4, Spring Data JPA, Hibernate 6.
  * **Database**: H2 Database nhúng lưu bền vững file đĩa cục bộ (`./data/studyhubdb`).
  * **Frontend**: Thymeleaf SSR kết hợp Tailwind CSS (JIT Engine), DaisyUI v4, FontAwesome 6.
  * **AI & PDF Engine**: Apache PDFBox 3.0.3, Google Gemini 1.5 Flash API.
* **Mô hình kiến trúc phân hệ độc lập (Vertical Slice Architecture)**:
  * Mỗi phân hệ (Auth, Core, RAG, Quiz, Planner, Payment, Admin) là một mô-đun độc lập, giảm thiểu phụ thuộc chéo, dễ dàng kiểm thử và mở rộng.

---

### SLIDE 6: MÔ HÌNH KINH DOANH 3 GÓI CƯỚC SAAS THỰC TẾ
* **Thiết kế phân tầng 3 Gói Cước dịch vụ**:
  * 🆓 **GÓI FREE ($0)**:
    * Dành cho sinh viên tự học cơ bản.
    * Giới hạn tối đa 3 môn học, 50 câu hỏi RAG/ngày, tạo Quiz trắc nghiệm 10 câu.
  * 👑 **GÓI PRO (49.000 VNĐ/tháng)**:
    * Dành cho sinh viên ôn thi nước rút.
    * Vô hạn môn học cá nhân, RAG AI không giới hạn, mở khóa AI Study Planner và xuất đề thi PDF chuẩn in ấn.
  * 👥 **GÓI SQUAD (129.000 VNĐ/tháng - nhóm 5 bạn)**:
    * Tiết kiệm chi phí: Chỉ ~25.000đ/bạn/tháng.
    * Dùng chung kho giáo trình và môn học nhóm `[SQUAD]`, thi đua bảng xếp hạng Leaderboard.

---

### SLIDE 7: PHÂN HỆ ĐỘT PHÁ — SHARED SQUAD HUB (KHÔNG GIAN HỌC NHÓM)
* **Cơ chế Mã mời nhóm độc nhất (Squad Invite Code 6 ký tự)**:
  * Trưởng nhóm sinh mã mời gửi cho các thành viên.
  * Quy trình phê duyệt thành viên an toàn: Trưởng nhóm duyệt thì thành viên mới được vào nhóm.
* **Đồng bộ hóa tài nguyên học tập**:
  * Môn học nhóm mang tiền tố `[SQUAD]` xuất hiện đồng bộ trong Dashboard của tất cả thành viên.
  * Kho slide giáo trình và tài liệu PDF dùng chung cho cả 5 bạn, tải lên 1 lần dùng cho cả nhóm.

---

### SLIDE 8: CƠ CHẾ FEATURE GATING, SQUAD LEADERBOARD & CHẾ ĐỘ THI SOLO RIÊNG TƯ
* **Kiểm soát quyền năng (Feature Gating)**:
  * Chặn vượt hạn ngạch 3 môn ở gói Free, chặn truy cập Planner/Xuất PDF khi chưa nâng cấp.
  * **Modal Bảng Giá SaaS 3 Gói Cước** hiện đại kích hoạt tự động (`modal-pricing-upgrade`).
  * Nâng cấp 1-click tức thì và tích hợp cổng thanh toán trực tuyến VNPay Sandbox.
* **Đấu trường Leaderboard & Đề thi Solo riêng tư**:
  * Bảng vinh danh Squad Leaderboard cập nhật điểm thi nhóm theo thời gian thực.
  * Tùy chọn làm bài Solo riêng tư: Đề thi cá nhân không bị đẩy lên bảng xếp hạng chung, bảo vệ quyền riêng tư học tập.
* **Ảnh minh chứng đưa vào slide**: Sơ đồ kiến trúc Vertical Slice, Bảng giá 3 Gói Cước SaaS, Giao diện Quản lý Squad có Invite Code, Bảng xếp hạng Leaderboard.
