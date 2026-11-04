# 🎯 KẾ HOẠCH TỔNG KẾT & PHÂN CHIA THUYẾT TRÌNH SLIDE (ASSIGNMENT + PROJECT GỘP)
> **Ngày lập kế hoạch**: 04/11/2026  
> **Người phân công**: Lê Thanh Tùng (Team Lead - Group 2)  
> **Môn học**: HSF302 (Java & Spring Framework)  
> **Đặc thù dự án Group 2**: **Làm gộp cả Assignment (UI Toolkit + AI Agent) và Project Cuối kỳ (RAG AI StudyHub + OAuth2 + VietQR)** trong cùng một sản phẩm hoàn chỉnh duy nhất.

---

## 📌 PHẦN I: KHUNG TIÊU CHÍ ĐÁNH GIÁ CỦA GIẢNG VIÊN (RUBRIC MAPPING)

Dự án **Smart StudyHub RAG** của Group 2 khớp **100% tất cả các tiêu chí** trong bảng yêu cầu của trường:

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│ 1. ASSIGNMENT:                                                                                   │
│    ✓ Sử dụng UI Toolkit trong Thymeleaf: Tailwind CSS (JIT) + DaisyUI + FontAwesome 6             │
│    ✓ Sử dụng AI Agent trong IDE để code UI & Backend: Google Antigravity + Gemini / Claude       │
│    ✓ Slide & User Guide về setup và code                                                         │
│                                                                                                  │
│ 2. PROJECT CUỐI KỲ (ĐÁP ỨNG CẢ 2 NHÁNH ĐỀ BÀI):                                                  │
│    ✓ Nhánh 1: Chat với AI (RAG Giáo trình PDF) + Cấu hình Centralized Gemini API Key             │
│    ✓ Nhánh 2: Login Google OAuth2 bên ngoài + Thanh toán Online VietQR Code tự động              │
│    ✓ Topping: Trích xuất & xử lý cấu trúc PDF thông minh (Apache PDFBox)                        │
└──────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 👥 PHẦN II: PHÂN CHIA NHÂN SỰ & NỘI DUNG THUYẾT TRÌNH SLIDE (6 THÀNH VIÊN)

| STT | Thành viên | Vai trò | Phụ trách Slide | Nội dung thuyết trình chính | Thời lượng dự kiến |
| :---: | :--- | :--- | :---: | :--- | :---: |
| **1** | **Lê Thanh Tùng** | **Team Lead** | Slide 1, 2, 5, 17, 18, 22 | Mở đầu, bối cảnh bài toán, kiến trúc 3 gói cước, Shared Squad Hub (Môn học nhóm, Invite Code, Leaderboard), Thanh toán VietQR & Tổng kết Q&A. | 4 - 5 phút |
| **2** | **Võ Quốc Trường** | Dev (Auth & UI) | Slide 3, 4, 6, 7 | Phần Assignment: Sử dụng Tailwind CSS/DaisyUI trong Thymeleaf, quy trình Prompting với AI Agent trong IDE; Login Google OAuth2 & Session Security. | 3 phút |
| **3** | **Nguyễn Thái Bình** | Dev (RAG Engine) | Slide 8, 9, 10 | Phân hệ RAG AI Chatbot: Trích xuất Chunks PDF (Apache PDFBox), Gemini Vector Grounding, Citation Cards (số trang đối chứng), Multi-Session Chat. | 3 phút |
| **4** | **Lê Hoàng Nhật** | Dev (Quiz Engine) | Slide 11, 12 | Phân hệ AI Quiz Generator: Sinh trắc nghiệm A/B/C/D theo chương, thi bấm giờ, chấm điểm tự động, xuất đề thi PDF chuẩn in ấn. | 2.5 phút |
| **5** | **Nguyễn Đức Thắng**| Dev (Study Planner)| Slide 13, 14 | Phân hệ AI Study Planner: Tự động xếp lịch ôn tập theo tuần bằng Gemini Flash Lite, phân chia Task học tập vừa sức không bị ngợp. | 2.5 phút |
| **6** | **Võ Hoài Ân** | Dev (Admin & QA) | Slide 15, 16, 19, 20 | Phân hệ Admin Command Center: Thống kê KPI, Quản trị 3 gói cước, Cấu hình Masking API Key, Đo Latency Ping; Báo cáo chất lượng 327/327 tests. | 3 phút |

---

## 📑 PHẦN III: DÀN TRANG CHI TIẾT BỘ SLIDE THUYẾT TRÌNH (22 SLIDES CHUẨN MỰC)

### 🟢 MỞ ĐẦU & TỔNG QUAN (Slide 1 - 2) — [TÙNG TRÌNH BÀY]
* **Slide 1: Title Slide (Trang bìa)**
  * Tên đề tài: **SMART STUDYHUB RAG** — Nền Tảng Học Tập Đa Môn & Luyện Thi A+ Ứng Dụng RAG AI.
  * Nhóm: Group 2 — Lớp HSF302.
  * Danh sách thành viên kèm chức danh rõ ràng (Team Lead & 5 Developers).
* **Slide 2: Bối Cảnh & Đặt Vấn Đề (Problem Statement)**
  * Nỗi đau: Sinh viên mùa thi bị ngợp trước hàng trăm trang PDF giáo trình; ChatGPT thuần thường bị "bịa đặt" (hallucination) và không có trang đối chứng; tự học dễ nản.
  * Giải pháp: Hệ thống RAG AI đối chứng giáo trình 100% + Cơ chế học nhóm Study Squad đua top có thưởng.

---

### 🎨 PHẦN ASSIGNMENT: UI TOOLKIT & AGENTIC AI (Slide 3 - 4) — [TRƯỜNG TRÌNH BÀY]
* **Slide 3: Tích Hợp UI Toolkit trong Thymeleaf (Đáp ứng Yêu cầu I)**
  * Lựa chọn: **Tailwind CSS (JIT Engine) + DaisyUI + FontAwesome 6**.
  * Kỹ thuật thực hiện trong Thymeleaf: Kế thừa bố cục Layout Fragment (`layout/main.html`), thiết kế White-Card Palette đồng bộ.
  * Lý do chọn thay vì Bootstrap: Tùy biến linh hoạt, hỗ trợ Glassmorphism/White-card sang trọng, tối ưu tải trang cực nhanh.
* **Slide 4: Quy Trình Code với AI Agent trong IDE (Đáp ứng Yêu cầu I)**
  * Công cụ: **Google Antigravity IDE** kết hợp mô hình AI **Gemini 2.5 Flash & Claude 3.5 Sonnet**.
  * Quy trình thực tế:
    1. Spec-Driven & Prompt Engineering (`docs/06-prompt-engineering-guide.md`).
    2. Test-Driven Development (TDD): AI sinh test case kiểm thử trước khi viết service.
    3. Pair-Debugging: AI phát hiện và xử lý lỗi đồng thời (Concurrency Quota, Data Isolation).

---

### 🏗️ KIẾN TRÚC & PHÂN BỔ 3 GÓI CƯỚC (Slide 5) — [TÙNG TRÌNH BÀY]
* **Slide 5: Kiến Trúc Công Nghệ & Mô Hình 3 Gói Cước**
  * Tech Stack: Spring Boot 3.3.4, Java 17, Spring Data JPA, Hibernate 6, H2 Database nhúng lưu file đĩa, Apache PDFBox.
  * 3 Gói Cước:
    1. `FREE` ($0): Tối đa 3 môn học, 50 câu RAG/ngày, Quiz 10 câu.
    2. `PRO` ($2/tháng - 49k): Không giới hạn môn, RAG vô hạn, mở khóa AI Study Planner & Xuất PDF.
    3. `SQUAD` ($5/tháng nhóm 5 bạn): Dùng chung tài liệu, môn nhóm `[SQUAD]`, đua top Leaderboard.

---

### 🚀 PHẦN PROJECT CUỐI KỲ: CHI TIẾT CÁC PHÂN HỆ (Slide 6 - 18)

#### 1. Module Authentication & Google OAuth2 — [TRƯỜNG] (Slide 6 - 7)
* **Slide 6: Đăng Nhập Đa Kênh (Google OAuth2 & Local Account)**
  * Đăng nhập 1 chạm với Google (OpenID Connect OAuth2).
  * Đăng ký/đăng nhập Form thường với mã hóa mật khẩu một chiều BCrypt.
* **Slide 7: Quản Trị Phiên & Phân Quyền Vai Trò (RBAC)**
  * Bảo mật Session Security, tự động điều hướng đúng quyền (`ROLE_STUDENT` vào Courses, `ROLE_ADMIN` vào Admin Hub).
  * Trang Hồ Sơ Cá Nhân (Profile): Quản lý avatar, hạn ngạch gói cước, thông tin cá nhân.

#### 2. Module RAG AI Chatbot & Chunking Engine — [BÌNH] (Slide 8 - 10)
* **Slide 8: Kiến Trúc Xử Lý Tài Liệu PDF & Chunking (Apache PDFBox)**
  * Đọc file PDF tải lên bằng `PDFTextStripper`, cắt đoạn thông minh 300 - 500 từ kèm Metadata số trang.
  * Lưu trữ vào `ChunkRepository` phân tách theo từng môn học.
* **Slide 9: Cơ Chế Vector Context Grounding & Gemini Chat**
  * Thuật toán RAG: Trích xuất các đoạn Chunks liên quan nhất ghép vào Prompt của Google Gemini Flash.
  * Đảm bảo tính xác thực: **0% ảo giác** vì AI chỉ được trả lời dựa trên giáo trình.
* **Slide 10: Citation Cards Đối Chứng & Multi-Session Chat**
  * Thẻ trích dẫn (Citations) hiển thị rõ tên tài liệu và số trang cụ thể để sinh viên lật sách đối chiếu.
  * Quản lý đa phiên hội thoại (Multi-Session), tự động lưu vết lịch sử chat và kiểm soát quota 50 câu/ngày.

#### 3. Module AI Quiz Generator & Xuất Đề PDF — [NHẬT] (Slide 11 - 12)
* **Slide 11: Sinh Đề Thi Trắc Nghiệm Tự Động & Làm Bài Bấm Giờ**
  * AI quét giáo trình sinh đề 4 lựa chọn A/B/C/D theo độ khó (EASY, MEDIUM, HARD).
  * Màn hình làm bài thi bấm giờ (Countdown Timer), tự động nộp bài khi hết giờ, chấm điểm thang 10.
* **Slide 12: Xuất Đề Thi PDF Chuẩn In Ấn (Apache PDFBox Export Service)**
  * Tính năng đặc quyền gói Pro: Xuất đề thi ra file PDF in ấn có header, footer, checkbox A/B/C/D chuẩn FPT.
  * Hỗ trợ 2 chế độ: Đề thi trắng làm bài hoặc Đề thi kèm đáp án & lời giải chi tiết.

#### 4. Module AI Study Planner — [THẮNG] (Slide 13 - 14)
* **Slide 13: Lập Kế Hoạch Ôn Thi Thông Minh Theo Tuần**
  * Sinh viên nhập mục tiêu và ngày thi deadline $\rightarrow$ Gemini Flash Lite tính toán phân bổ lộ trình học theo tuần.
  * Tự động chia nhỏ khối lượng bài học vừa sức, tránh học dồn trước ngày thi.
* **Slide 14: Quản Lý Study Tasks & Theo Dõi Tiến Độ**
  * Checkbox đánh dấu hoàn thành từng đầu việc học tập.
  * Thanh tiến độ học tập (Progress Bar) trực quan, tiếp thêm động lực học mỗi ngày.

#### 5. Module Admin Command Center & Quản Trị Hệ Thống — [ÂN] (Slide 15 - 16)
* **Slide 15: Admin Dashboard & Thống Kê KPI Thời Gian Thực**
  * Theo dõi tổng sinh viên, số lượng môn học, tổng số đoạn Chunks, số câu hỏi RAG trong ngày.
  * Quản lý danh sách sinh viên: Reset hạn ngạch RAG, nâng cấp gói cước hỗ trợ sinh viên.
* **Slide 16: Quản Trị Centralized System Key & Bảo Mật Masking**
  * Tính năng Centralized AI Key: Quản trị viên nhập System Key, hệ thống tự chia sẻ an toàn cho toàn bộ tính năng AI.
  * **API Key Masking (`AIzaSy...****`)**: Cơ chế che mờ khóa nhạy cảm, chống lộ thông tin khi demo.
  * Đo độ trễ mạng thực tế (Latency Ping ms) trực tiếp tới Google AI.

#### 6. Phân Hệ Đột Phá: Shared Squad Hub & Thanh Toán VietQR — [TÙNG] (Slide 17 - 18)
* **Slide 17: Shared Squad Hub (Không Gian Học Nhóm Dùng Chung)**
  * Cơ chế Mã mời nhóm (Invite Code 6 ký tự) & Trưởng nhóm phê duyệt thành viên (Pending / Active).
  * Kho tài liệu giáo trình dùng chung & Môn học nhóm dùng chung (`[SQUAD] EXE101`).
  * Đấu trường Quiz nhóm & Squad Leaderboard theo môn học, có tùy chọn **Đề thi cá nhân (Solo)** riêng tư.
* **Slide 18: Cổng Thanh Toán VietQR Tự Động (Online Payment)**
  * Tạo mã QR thanh toán ngân hàng động chuẩn NAPAS 24/7 theo từng mã giao dịch.
  * Sinh viên quét app ngân hàng thanh toán tự động nâng cấp gói Pro/Squad không cần chờ duyệt thủ công.

---

### 🛡️ KIỂM THỬ, SETUP & KẾT LUẬN (Slide 19 - 22) — [ÂN & TÙNG TRÌNH BÀY]
* **Slide 19: Đảm Bảo Chất Lượng & Kiểm Thử Toàn Diện (QA)**
  * **327 / 327 Unit & Integration Tests PASS (100%)**.
  * Kiểm thử chịu tải đồng thời (Quota Concurrency), kiểm thử cách ly dữ liệu môn học (Data Isolation), kiểm thử bảo mật RBAC.
* **Slide 20: Hướng Dẫn Cài Đặt & Chạy Dự Án (User Guide)**
  * Clone mã nguồn: `git clone https://github.com/Tung-pro123/Smart-StudyHub-RAG.git`
  * Cấu hình biến môi trường [`.env`](file:///d:/Ki5_Fall_2026/HSF302/Assignment/Assignment/.env): `GOOGLE_CLIENT_ID`, `GEMINI_API_KEY`.
  * Khởi chạy ứng dụng: `mvn spring-boot:run` $\rightarrow$ Truy cập `http://localhost:8080`.
* **Slide 21: Kết Quả Đạt Được & Hướng Phát Triển Tương Lai**
  * Đạt được: Ứng dụng AI thực chiến không ảo giác, hỗ trợ học tập cá nhân lẫn làm việc nhóm hiệu quả.
  * Tương lai: Tích hợp giọng nói AI Audio RAG, app di động Flutter.
* **Slide 22: Q&A (Hỏi Đáp Với Hội Đồng Giảng Viên)**
  * Lời cảm ơn Thầy/Cô.
  * Danh sách các câu hỏi vấn đáp trọng tâm đã chuẩn bị sẵn câu trả lời.

---

## 🛠️ PHẦN IV: CHECKLIST GIAO TASK NGÀY 04/11 CHO BÌNH (RAG FIX)
Trước khi bước vào chụp ảnh slide thuyết trình, bạn Bình cần hoàn thành gấp 3 task sau:
1. [ ] **Fix chữ tàng hình Header Bar**: Chuyển nút `Quản Lý Giáo Trình & Chunks` sang White-Card `bg-white border-slate-200 text-slate-700`.
2. [ ] **Sửa nút "+ Cuộc Trò Chuyện Mới"**: Trong `ChatController.startNewChat()`, tạo `ChatSession` mới trong CSDL và redirect vào `sessionId` mới đó.
3. [ ] **Tích hợp `marked.js`**: Tự động render Markdown Table và format chữ đậm, bullet list trong bong bóng chat AI.
