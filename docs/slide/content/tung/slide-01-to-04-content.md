# 📑 SLIDE CONTENT: SLIDE 1 ➔ SLIDE 4 (LÊ THANH TÙNG - TEAM LEAD)
> **Khối trình bày**: Khối 1 — Mở đầu, Bối cảnh, Kiến trúc 3 gói cước & Shared Squad Hub / Feature Gating  
> **Thời lượng**: 4 phút  

---

### SLIDE 1: TRANG BÌA (TITLE SLIDE)
* **Tiêu đề lớn**: SMART STUDYHUB RAG
* **Phụ đề**: Nền Tảng Quản Lý Học Tập Đa Môn & Luyện Thi A+ Tích Hợp RAG AI Không Ảo Giác
* **Thông tin nhóm**: Group 2 — Lớp HSF302 — Giảng viên hướng dẫn
* **Danh sách 6 thành viên**:
  1. **Lê Thanh Tùng** — Project Manager & Tech Lead
  2. **Ngô Nhật Trường** — Fullstack Dev (Auth, Google OAuth2 & UI Toolkit)
  3. **Nguyễn Văn Gia Bình** — AI Engineer (PDF Chunking & RAG Engine)
  4. **Huỳnh Nhật** — Fullstack Dev (AI Quiz Generator & PDF Export)
  5. **Lê Nguyễn Minh Thắng** — Fullstack Dev (AI Study Planner)
  6. **Ngô Ân** — Security & QA Lead (Admin Hub & Automated Testing)

---

### SLIDE 2: BỐI CẢNH, ĐẶT VẤN ĐỀ & GIẢI PHÁP
* **Thực trạng & Nỗi đau sinh viên mùa thi**:
  * Tài liệu giáo trình cồng kềnh: Hàng trăm trang PDF, slide bài giảng phân mảnh khắp nơi.
  * ChatGPT & AI thông thường bị "ảo giác" (hallucination): Trả lời chung chung, không có nguồn đối chứng, sinh viên không dám tin cậy khi ôn thi.
  * Tự học cá nhân cô độc: Thiếu động lực, dễ bỏ cuộc, học nhóm khó kiểm soát tiến độ.
* **Giải pháp Smart StudyHub**:
  * **Kho môn học tập trung**: Tự tạo và quản lý môn học cá nhân hóa.
  * **RAG Engine đối chứng 100%**: Chỉ trả lời dựa trên giáo trình đã nạp kèm số trang cụ thể.
  * **Study Squad**: Học nhóm 5 bạn cùng chia sẻ tài liệu và thi đua bảng xếp hạng Leaderboard.

---

### SLIDE 3: KIẾN TRÚC CÔNG NGHỆ & MÔ HÌNH 3 GÓI CƯỚC
* **Bức tranh công nghệ (Tech Stack)**:
  * Backend: Java 17, Spring Boot 3.3.4, Spring Data JPA, Hibernate 6.
  * Database: H2 Database nhúng lưu bền vững file đĩa (`./data/studyhubdb`), Flyway Migration.
  * Frontend: Thymeleaf SSR, Tailwind CSS (JIT Engine), DaisyUI v4, FontAwesome 6.
  * AI & PDF: Apache PDFBox 3.0.3, Google Gemini 1.5 Flash API.
* **Mô hình 3 Gói Cước**:
  * 🆓 **GÓI FREE ($0)**: Tối đa 3 môn học, 50 câu hỏi RAG/ngày, tạo Quiz 10 câu.
  * 👑 **GÓI PRO (49.000 VNĐ/tháng)**: Vô hạn môn học & RAG, mở khóa AI Study Planner, xuất đề thi PDF chuẩn in ấn.
  * 👥 **GÓI SQUAD (129.000 VNĐ/tháng - nhóm 5 bạn)**: Không gian học nhóm, dùng chung tài liệu, môn nhóm `[SQUAD]`, đua top Leaderboard.

---

### SLIDE 4: PHÂN HỆ ĐỘT PHÁ — SHARED SQUAD HUB & CƠ CHẾ NÂNG CẤP GÓI CƯỚC
* **Shared Squad Hub (Không gian học nhóm dùng chung)**:
  * Cơ chế Mã mời nhóm (Invite Code 6 ký tự độc nhất) & Trưởng nhóm phê duyệt thành viên.
  * Môn học nhóm `[SQUAD]` xuất hiện đồng bộ trong Dashboard của tất cả thành viên.
  * Đấu trường Quiz nhóm & Squad Leaderboard: Vinh danh top điểm số, hỗ trợ chế độ đề thi cá nhân (Solo riêng tư).
* **Kiểm soát quyền năng (Feature Gating) & Nâng cấp gói cước**:
  * Chặn vượt hạn ngạch 3 môn ở gói Free, chặn tính năng Pro khi chưa nâng cấp.
  * Modal Bảng Giá 3 Gói Cước hiện đại kích hoạt tự động (`modal-pricing-upgrade`).
  * Xử lý nâng cấp 1-click tức thì qua `/auth/upgrade-pro` và `/auth/upgrade-squad`.
* **Ảnh minh chứng đưa vào slide**: Ảnh giao diện Quản lý Squad, ảnh bảng Leaderboard, ảnh Modal Bảng Giá 3 Gói Cước.

