# 🎯 KẾ HOẠCH TỔNG KẾT & PHÂN CHIA THUYẾT TRÌNH SLIDE (ASSIGNMENT + PROJECT GỘP)
> **Ngày lập kế hoạch**: 04/11/2026  
> **Người phân công**: Lê Thanh Tùng (Team Lead - Group 2)  
> **Môn học**: HSF302 (Java & Spring Framework)  
> **Đặc thù dự án Group 2**: **Làm gộp cả Assignment (UI Toolkit + AI Agent) và Project Cuối kỳ (RAG AI StudyHub + Google OAuth2 + Shared Squad Hub + Quản lý 3 gói cước)** trong cùng một sản phẩm hoàn chỉnh duy nhất.  
> 
> 🚨 **TASK CODE GẤP TRONG NGÀY 04/11 CHO NGUYỄN VĂN GIA BÌNH (RAG FIX)**:  
> *(Sửa triệt để 3 lỗi P0: Nút "Cuộc trò chuyện mới" bị liệt, Chữ tàng hình Header Bar, và Thiếu bộ render Markdown Table trong bong bóng chat AI — Xem toàn bộ hướng dẫn kỹ thuật & code mẫu tại [Phần V bên dưới](#-phần-v-kế-hoạch-giao-task-phân-hệ-rag-ai-chatbot-cho-bình---sửa-gấp-trước-2030)).*  
>  
> 💳 **TASK CODE & VẬN HÀNH CHO NGÔ ÂN (VNPAY REVENUE & RECONCILIATION)**:  
> *(Triển khai và làm chủ phân hệ Quản trị Doanh thu VNPay Sandbox, Lịch thanh toán & Hạn gói cước, Bảng đơn hàng thời gian thực, Xuất báo cáo CSV đối soát và Đối chiếu 1-1 với VNPay Merchant Portal — Xem hướng dẫn kỹ thuật chi tiết tại [Phần VI bên dưới](#-phần-vi-kế-hoạch-giao-task-phân-hệ-quản-trị-doanh-thu--đối-soát-đơn-hàng-vnpay-cho-ân)).*  
>  
> 🚀 **KẾ HOẠCH MỚI NHẤT (07/11/2026)**:  
> *(Xem toàn bộ kế hoạch rà soát lỗi giao diện & chức năng theo cuộc họp 04/11, tái cấu trúc thứ tự thuyết trình "Từ ngoài vào trong" [Trường ➔ Tùng ➔ Bình ➔ Nhật ➔ Thắng ➔ Ân] tại [task-2026-11-07-final-sprint-ui-audit-and-presentation-restructure.md](task-2026-11-07-final-sprint-ui-audit-and-presentation-restructure.md)).*

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
│    ✓ Nhánh 2: Login Google OAuth2 bên ngoài + Phân hệ Quản trị 3 gói cước (Feature Gating)       │
│    ✓ Topping: Trích xuất & xử lý cấu trúc PDF thông minh (Apache PDFBox) + Shared Squad Hub      │
└──────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 👥 PHẦN II: PHÂN CHIA NHÂN SỰ & NỘI DUNG THUYẾT TRÌNH (KHỐI LIÊN TỤC - NÓI 1 LẦN DỨT ĐIỂM)

> **Nguyên tắc tổ chức thuyết trình**:
> * **Mỗi thành viên chỉ lên nói ĐÚNG 1 LẦN duy nhất theo một khối slide liên tục (Contiguous Block)**, không nói xen kẽ nhảy qua nhảy lại để giữ nhịp thuyết trình mạch lạc, tự nhiên và không bị ngắt quãng.
> * Người trước trình bày xong toàn bộ phần của mình sẽ giới thiệu và chuyển mic dứt điểm cho người tiếp theo.

| STT | Họ và Tên | Vai trò | Khối Slide phụ trách | Nội dung thuyết trình chính | Thời lượng |
| :---: | :--- | :--- | :---: | :--- | :---: |
| **1** | **Ngô Nhật Trường** | **Dev (Auth & UI)** | **Slide 1 ➔ Slide 4** *(4 slides liên tiếp)* | **Mở màn & Cổng vào hệ thống**: Giới thiệu Nhóm 2 & Đề tài; Assignment 1: Ứng dụng UI Toolkit Tailwind CSS + DaisyUI White-Card trong Thymeleaf; Assignment 2: Quy trình Prompting với AI Agent trong IDE; Authentication đa kênh (Google OAuth2, Form BCrypt, 1-Click Login, Profile cá nhân, Đăng xuất Logout). | **3.5 phút** |
| **2** | **Lê Thanh Tùng** | **Team Lead** | **Slide 5 ➔ Slide 8** *(4 slides liên tiếp)* | **Bức tranh lớn & Kiến trúc lõi**: Bối cảnh & Nỗi đau sinh viên mùa thi; Giải pháp Smart StudyHub RAG; Mô hình 3 gói cước (FREE/PRO/SQUAD) & Cơ chế Feature Gating; Không gian học nhóm Shared Squad Hub (Invite Code, Duyệt thành viên, Môn nhóm dùng chung, Đấu trường Leaderboard & Đề thi Solo riêng tư). | **4 phút** |
| **3** | **Nguyễn Văn Gia Bình** | **Dev (RAG Engine)** | **Slide 9 ➔ Slide 11** *(3 slides liên tiếp)* | **Phân hệ RAG AI Chatbot**: Trích xuất PDF & phân mảnh Chunks (Apache PDFBox); Cơ chế Gemini Context Grounding triệt tiêu ảo giác; Thẻ trích dẫn (Citation Cards) số trang đối chứng & Multi-session Chat. | **3 phút** |
| **4** | **Huỳnh Nhật** | **Dev (Quiz Engine)** | **Slide 12 ➔ Slide 14** *(3 slides liên tiếp)* | **Phân hệ AI Quiz Generator**: Sinh trắc nghiệm tự động A/B/C/D 3 mức độ từ giáo trình; Phòng thi bấm giờ Countdown & chấm điểm tự động; Tính năng Pro xuất đề thi PDF chuẩn in ấn. | **2.5 phút** |
| **5** | **Lê Nguyễn Minh Thắng** | **Dev (Study Planner)** | **Slide 15 ➔ Slide 17** *(3 slides liên tiếp)* | **Phân hệ AI Study Planner**: Thuật toán phân tích ngày thi & xếp lịch ôn tập theo tuần bằng Gemini Flash Lite; Phân chia Study Tasks vừa sức chống ngợp; Interactive Checklist & Progress Bar. | **2.5 phút** |
| **6** | **Ngô Ân** | **Dev (Admin, Doanh Thu VNPay & QA)** | **Slide 18 ➔ Slide 22** *(5 slides liên tiếp)* | **Vận hành, Doanh thu VNPay & QA**: Admin Command Center (KPI, Quản trị 3 gói cước, Báo cáo Doanh thu VNPay Sandbox, Lịch sử đơn hàng, Xuất CSV đối soát tài chính, Masking System Key, Ping Latency); Báo cáo QA 333/333 tests PASS; Hướng dẫn cài đặt (User Guide); Tổng kết & mở Q&A. | **4 phút** |

---

## 📑 PHẦN III: DÀN TRANG CHI TIẾT 22 SLIDES THUYẾT TRÌNH (CHUẨN 1 LƯỢT / THÀNH VIÊN)

### 🎙️ KHỐI 1: LÊ THANH TÙNG (TEAM LEAD) — Slide 1 đến Slide 4 [~4 phút]
* **Slide 1: Title Slide (Trang bìa)**
  * Tên đề tài: **SMART STUDYHUB RAG** — Nền Tảng Học Tập Đa Môn & Luyện Thi A+ Ứng Dụng RAG AI.
  * Thông tin: Nhóm 2 — Môn HSF302 — FPT University.
  * Giới thiệu danh sách 6 thành viên kèm phân công chức danh rõ ràng (Team Lead & 5 Developers).
* **Slide 2: Bối Cảnh, Đặt Vấn Đề & Giải Pháp (Problem Statement & Solution)**
  * Nỗi đau của sinh viên: Mùa thi bị ngợp trước hàng trăm trang PDF giáo trình; dùng ChatGPT thông thường bị "bịa đặt" (hallucination) không có số trang đối chiếu; tự học cá nhân dễ nản lòng.
  * Giải pháp Smart StudyHub: RAG AI đối chứng giáo trình 100% không ảo giác + Cơ chế học nhóm Study Squad đua top có thưởng.
* **Slide 3: Kiến Trúc Công Nghệ & Mô Hình 3 Gói Cước (Tech Stack & Business Model)**
  * Tech Stack: Spring Boot 3.3.4, Java 17, Spring Data JPA, Hibernate 6, H2 File Database, Tailwind CSS/DaisyUI, Apache PDFBox, Gemini 1.5 Flash.
  * Mô hình 3 Gói Cước:
    1. `FREE` ($0): Tối đa 3 môn học, 50 câu RAG/ngày, Quiz trắc nghiệm 10 câu.
    2. `PRO` ($2/tháng - 49k): Vô hạn môn học, RAG không giới hạn, mở khóa AI Study Planner & Xuất đề thi PDF in ấn.
    3. `SQUAD` ($5/tháng nhóm 5 bạn): Dùng chung kho tài liệu, môn học nhóm `[SQUAD]`, đua top Squad Leaderboard.
* **Slide 4: Không Gian Học Nhóm Shared Squad Hub & Cơ Chế Nâng Cấp Gói Cước**
  * Shared Squad Hub: Tạo mã mời (Invite Code 6 ký tự), Trưởng nhóm phê duyệt thành viên; Kho giáo trình và môn học dùng chung; Đấu trường Quiz nhóm & Squad Leaderboard (có tùy chọn đề thi cá nhân Solo riêng tư).
  * Kiểm soát quyền năng theo gói (Feature Gating): Gói Free bị chặn khi tạo quá 3 môn hoặc khi vào Study Planner / Xuất PDF; Tự động kích hoạt Modal Bảng Giá 3 Gói Cước SaaS sang trọng.
  * Cơ chế nâng cấp tức thì: Xử lý 1-click nâng cấp qua `/auth/upgrade-pro` và `/auth/upgrade-squad` mượt mà, phản hồi ngay lập tức trong phiên.
  *(Tùng kết thúc phần trình bày của mình và trân trọng chuyển mic cho bạn Ngô Nhật Trường phụ trách phần Assignment và Authentication).*

---

### 🎙️ KHỐI 2: NGÔ NHẬT TRƯỜNG (DEV: AUTH & UI) — Slide 5 đến Slide 8 [~3 phút]
* **Slide 5: Tích Hợp UI Toolkit trong Thymeleaf (Đáp ứng Yêu cầu Assignment)**
  * Lựa chọn: **Tailwind CSS (JIT Engine) + DaisyUI + FontAwesome 6**.
  * Kỹ thuật thực hiện trong Thymeleaf: Kế thừa bố cục Layout Fragment (`layout/main.html`, `navbar.html`, `sidebar.html`), thiết kế White-Card Palette đồng bộ.
  * Lý do chọn thay vì Bootstrap: Khả năng tùy biến sâu, hỗ trợ Glassmorphism/White-card sang trọng, tối ưu dung lượng và tốc độ tải trang.
* **Slide 6: Quy Trình Code với AI Agent trong IDE (Đáp ứng Yêu cầu Assignment)**
  * Công cụ: **Google Antigravity IDE** kết hợp mô hình AI **Gemini 2.5 Flash & Claude 3.5 Sonnet**.
  * Quy trình phát triển thực tế 3 bước:
    1. Spec-Driven & Prompt Engineering (`docs/06-prompt-engineering-guide.md`).
    2. Test-Driven Development (TDD): AI sinh test case kiểm thử trước khi viết service.
    3. Pair-Debugging: AI phát hiện và xử lý lỗi đồng thời (Concurrency Quota, Data Isolation).
* **Slide 7: Phân Hệ Authentication — Đăng Nhập Đa Kênh (Google OAuth2 & Local Account)**
  * Đăng nhập 1 chạm với Google (OpenID Connect OAuth2).
  * Đăng ký & đăng nhập Form thường với mã hóa mật khẩu một chiều BCrypt an toàn.
* **Slide 8: Quản Trị Phiên, Phân Quyền Vai Trò (RBAC) & Hồ Sơ Sinh Viên (Profile)**
  * Bảo mật Session Security, tự động điều hướng đúng quyền (`ROLE_STUDENT` vào Courses, `ROLE_ADMIN` vào Admin Hub).
  * Trang Hồ Sơ Cá Nhân (Profile): Quản lý avatar, hạn ngạch gói cước, thông tin nhóm Squad.
  *(Trường kết thúc phần trình bày và chuyển mic cho bạn Nguyễn Văn Gia Bình trình bày phân hệ RAG AI Chatbot).*

---

### 🎙️ KHỐI 3: NGUYỄN VĂN GIA BÌNH (DEV: RAG ENGINE) — Slide 9 đến Slide 11 [~3 phút]
* **Slide 9: Kiến Trúc Xử Lý Tài Liệu PDF & Phân Mảnh Chunks (Apache PDFBox)**
  * Đọc file PDF giáo trình tải lên bằng `PDFTextStripper`.
  * Thuật toán phân đoạn thông minh: Cắt đoạn 300 - 500 từ theo ranh giới câu/đoạn, lưu kèm Metadata số trang và mã môn học vào `ChunkRepository`.
* **Slide 10: Cơ Chế Vector Context Grounding & Gemini Chat**
  * Thuật toán RAG: Tìm kiếm các đoạn Chunks liên quan nhất ghép vào Prompt của Google Gemini Flash.
  * Đảm bảo tính trung thực tuyệt đối: **0% ảo giác (Zero Hallucination)** vì AI chỉ được suy luận và trả lời dựa trên giáo trình được nạp.
* **Slide 11: Thẻ Trích Dẫn Đối Chứng (Citation Cards) & Đa Phiên Hội Thoại (Multi-Session)**
  * Thẻ trích dẫn (Citations): Hiển thị rõ tên file PDF và số trang cụ thể để sinh viên lật sách đối chiếu ngay lập tức.
  * Quản lý đa phiên hội thoại (Multi-Session): Lưu vết lịch sử chat độc lập theo từng môn học, tự động kiểm soát quota 50 câu/ngày cho gói Free.
  *(Bình kết thúc phần trình bày và chuyển mic cho bạn Huỳnh Nhật trình bày phân hệ AI Quiz Generator).*

---

### 🎙️ KHỐI 4: HUỲNH NHẬT (DEV: QUIZ ENGINE) — Slide 12 đến Slide 14 [~2.5 phút]
* **Slide 12: Phân Hệ AI Quiz Generator — Sinh Đề Trắc Nghiệm Tự Động**
  * Cơ chế AI quét Chunks giáo trình để sinh bộ câu hỏi 4 lựa chọn A/B/C/D theo từng chương và độ khó (Dễ, Trung bình, Khó).
  * Phân tách đề thi theo môn học, đảm bảo câu hỏi bám sát kiến thức thực tế.
* **Slide 13: Trải Nghiệm Phòng Thi Bấm Giờ & Chấm Điểm Tự Động**
  * Giao diện phòng thi trực quan, đồng hồ đếm ngược (Countdown Timer) thời gian thực.
  * Tự động thu bài khi hết giờ, chấm điểm thang 10 tức thì kèm bảng giải thích chi tiết từng câu.
* **Slide 14: Tính Năng Độc Quyền: Xuất Đề Thi PDF Chuẩn In Ấn (PDFBox Export Service)**
  * Đặc quyền gói Pro: Xuất đề thi ra định dạng PDF chuẩn đẹp (Header, Watermark, Checkbox A/B/C/D chuẩn FPT).
  * Cung cấp 2 bản xuất: Đề thi trắng để in ấn phát cho lớp/nhóm làm bài, và Bản đáp án có Answer Key & lời giải chi tiết.
  *(Nhật kết thúc phần trình bày và chuyển mic cho bạn Lê Nguyễn Minh Thắng trình bày phân hệ AI Study Planner).*

---

### 🎙️ KHỐI 5: LÊ NGUYỄN MINH THẮNG (DEV: STUDY PLANNER) — Slide 15 đến Slide 17 [~2.5 phút]
* **Slide 15: Phân Hệ AI Study Planner — Thuật Toán Xếp Lịch Ôn Thi Theo Tuần**
  * Sinh viên nhập mục tiêu điểm số và ngày thi deadline $\rightarrow$ Gemini Flash Lite tính toán phân bổ lộ trình ôn tập khoa học theo từng tuần.
  * Tự động chia nhỏ khối lượng bài học vừa sức, tránh tình trạng học dồn sát ngày thi.
* **Slide 16: Phân Chia Nhiệm Vụ Học Tập Vừa Sức & Cách Ly Dữ Liệu**
  * Tự động phân rã giáo trình thành các Study Tasks cụ thể gắn liền với từng chương.
  * Đảm bảo tính riêng tư và bảo mật dữ liệu IDOR theo từng sinh viên.
* **Slide 17: Quản Lý Study Tasks & Theo Dõi Tiến Độ Trực Quan (Interactive Checklist)**
  * Checkbox đánh dấu hoàn thành từng đầu việc học tập với tương tác mượt mà.
  * Thanh tiến độ học tập (Progress Bar) phần trăm trực quan, tiếp thêm động lực học mỗi ngày.
  *(Thắng kết thúc phần trình bày và chuyển mic cho bạn Ngô Ân phụ trách phân hệ Admin, QA và Hướng dẫn cài đặt).*

---

### 🎙️ KHỐI 6: NGÔ ÂN (DEV: ADMIN COMMAND CENTER, QA & USER GUIDE) — Slide 18 đến Slide 22 [~3.5 phút]
* **Slide 18: Admin Command Center — Giám Sát KPI, Doanh Thu VNPay & Quản Trị Gói Cước**
  * Dashboard theo dõi thời gian thực: Tổng sinh viên, số môn học, số Chunks, tổng lượt gọi AI RAG.
  * **Phân hệ Báo cáo Doanh thu VNPay Sandbox**: Thống kê Tổng doanh thu (VNĐ), Doanh thu gói PRO (49.000đ), Doanh thu gói SQUAD (129.000đ), Doanh thu hôm nay, Số lượng đơn hàng thành công.
  * **Bảng Lưu vết Đơn hàng & Lịch sử Giao dịch**: Theo dõi mã đơn hàng `txnRef`, tài khoản sinh viên, số tiền, phương thức ngân hàng (NCB/VISA), mã giao dịch VNPay `vnpayTransactionNo`, trạng thái `SUCCESS`.
  * **Nút xuất báo cáo tài chính CSV** (`/admin/revenue/export-csv`) UTF-8 BOM cho Excel và nút liên kết trực tiếp tới Cổng đối soát **VNPay Merchant Portal** (`merchantv2`).
  * Quản trị tài khoản sinh viên: Nâng/hạ 3 gói cước linh hoạt, reset hạn ngạch RAG hỗ trợ sinh viên.
* **Slide 19: Centralized AI Key, Bảo Mật Masking & Đo Latency Ping**
  * Cơ chế Centralized System Key: Admin cấu hình 1 lần, an toàn toàn hệ thống, không sợ lộ key.
  * API Key Masking (`AIzaSy...****`): Che giấu khóa nhạy cảm trên giao diện khi demo/trình chiếu.
  * Tính năng đo độ trễ mạng thực tế (Latency Ping ms) trực tiếp tới Google AI API.
* **Slide 20: Báo Cáo Đảm Bảo Chất Lượng Toàn Diện (QA Report)**
  * Nghiệm thu **333 / 333 Unit & Integration Tests PASS (100%)** bao gồm 6 test cases phân hệ VNPay Payment & Revenue (`VnPayPaymentAndRevenueTest`).
  * Kiểm thử chịu tải đồng thời (Quota Concurrency), Kiểm thử cách ly dữ liệu (Data Isolation), Kiểm thử phân quyền RBAC và bảo vệ gian lận Leaderboard.
  * Kiểm thử toàn vẹn dữ liệu thanh toán: Tạo URL thanh toán VNPay chuẩn checksum HMAC-SHA512, xử lý IPN/Return, cộng dồn 30 ngày hạn dùng gói cước và xuất file CSV đối soát.
* **Slide 21: Hướng Dẫn Cài Đặt & Chạy Dự Án (User Guide — Setup & Code)**
  * Yêu cầu môi trường: Java 17, Maven 3.8+, H2 Database file tự tạo.
  * Cấu hình file [`.env`](file:///d:/Ki5_Fall_2026/HSF302/Assignment/Assignment/.env): `GOOGLE_CLIENT_ID`, `GEMINI_API_KEY`.
  * Khởi chạy một lệnh duy nhất: `mvn spring-boot:run` $\rightarrow$ Mở trình duyệt tại `http://localhost:8080`.
* **Slide 22: Kết Luận, Lời Cảm Ơn & Mời Hội Đồng Bước Vào Q&A**
  * Đạt được trọn vẹn mục tiêu của cả Assignment và Project cuối kỳ trong một sản phẩm hoàn chỉnh, sẵn sàng ứng dụng thực tế.
  * Lời cảm ơn Thầy/Cô và mời Hội đồng Giảng viên bước vào phần Hỏi - Đáp (Q&A) cùng toàn thể 6 thành viên Nhóm 2.

---

## 🔬 PHẦN IV: QUY TRÌNH TÁI KIỂM THỬ THỰC TẾ (HANDS-ON RE-RUN PROTOCOL) TRƯỚC KHI SOẠN SLIDE

> **YÊU CẦU BẮT BUỘC TỪ TEAM LEAD LÊ THANH TÙNG**:
> * **Không thành viên nào được phép làm slide "chay" hoặc dùng ảnh vẽ mockup giả định**.
> * **Từng bạn phải tự mình chạy lại (re-run) toàn bộ luồng chức năng thực tế của phân hệ mình phụ trách trên hệ thống đang chạy (`http://localhost:8080`)** để:
>   1. Đảm bảo tính năng hoạt động trơn tru 100%, không phát sinh bất kỳ lỗi runtime, lỗi 500 hay vỡ layout nào.
>   2. Hiểu sâu sắc từng bước thao tác thực tế để tự tin trả lời vấn đáp khi Hội đồng Giảng viên hỏi sâu.
>   3. Chụp lại ảnh màn hình thực tế (ảnh sắc nét, dữ liệu thật của đề tài) để chèn trực tiếp vào các slide của mình làm minh chứng thuyết phục nhất.

---

### 1️⃣ BẠN NGÔ NHẬT TRƯỜNG (KIỂM THỬ LUỒNG AUTH & UI — SLIDE 5 ➔ 8)
* [ ] **Test Luồng 1 (1-Click & Local Login)**:
  * Truy cập `http://localhost:8080/login`.
  * Bấm thử các nút **"1-Click Đăng Nhập Nhanh"** (Gói Free: `student1`, Pro: `student_pro`, Squad: `squad_leader`) xem có tự động điền form và đăng nhập thành công vào Dashboard không.
  * Đăng xuất, thử nhập sai mật khẩu để kiểm tra thông báo lỗi tiếng Việt hiển thị đẹp mắt.
* [ ] **Test Luồng 2 (Google OAuth2 Login)**:
  * Bấm nút "Đăng nhập với Google" để kiểm chứng quy trình chuyển hướng xác thực OpenID Connect OAuth2 với tài khoản FPT/Gmail cá nhân.
* [ ] **Test Luồng 3 (Profile & Session Security)**:
  * Vào `http://localhost:8080/profile`, kiểm tra hiển thị đúng avatar, tên sinh viên, huy hiệu gói cước và hạn ngạch RAG.
  * Thử đổi avatar và cập nhật thông tin cá nhân.
* [ ] **Test Luồng 4 (Kiểm thử Phân quyền RBAC)**:
  * Khi đang đăng nhập tài khoản sinh viên thường (`student1`), thử gõ trực tiếp URL `/admin/dashboard` hoặc `/h2-console` trên thanh trình duyệt $\rightarrow$ Xác nhận hệ thống chặn lại và điều hướng an toàn (không bị lộ dữ liệu quản trị).
* 📸 **Bộ ảnh cần chụp đưa vào Slide**: Ảnh trang Login chuẩn 50/50, ảnh Popup Google OAuth2, ảnh Profile White-card có huy hiệu gói cước, ảnh code cấu hình Tailwind/DaisyUI trong IDE.

---

### 2️⃣ BẠN NGUYỄN VĂN GIA BÌNH (KIỂM THỬ LUỒNG RAG & CHUNKING — SLIDE 9 ➔ 11)
* [x] **Test Luồng 1 (Bóc tách PDF & Phân mảnh Chunks — BẮT BUỘC TEST KỸ)**:
  * Vào một môn học bất kỳ (ví dụ: `PRJ301` hoặc `LAB211`), truy cập mục **"Tài Liệu & Giáo Trình"** (`/courses/{id}/documents`).
  * Thực hiện upload 1 file giáo trình PDF thực tế (dung lượng từ 5 - 20 trang).
  * **Kiểm tra kết quả trích xuất**: Xác nhận thanh tiến trình hoàn tất, hệ thống sử dụng Apache PDFBox bóc tách văn bản thành công, bảng danh sách các đoạn Chunks hiển thị đầy đủ số trang, độ dài từ (300 - 500 từ) và nội dung đoạn trích.
* [x] **Test Luồng 2 (Hỏi đáp RAG & Citation Cards)**:
  * Vào phòng chat RAG (`/courses/{id}/chat`), bấm nút **"+ Cuộc Trò Chuyện Mới"**.
  * Đặt câu hỏi trực tiếp vào nội dung có trong file PDF vừa nạp (VD: *"Hãy tóm tắt các tính năng chính trong giáo trình vừa tải lên"*).
  * Kiểm tra câu trả lời của AI: Câu trả lời bám sát 100% nội dung tài liệu (Zero Hallucination).
  * **Kiểm tra thẻ trích dẫn Citation Cards**: Thẻ hiển thị rõ ràng: `📄 [Tên_file.pdf, Trang: X]` để đối chứng lật sách.
* [x] **Test Luồng 3 (Đa phiên hội thoại & Kiểm soát Quota)**:
  * Tạo thêm một phiên chat mới, kiểm tra danh sách lịch sử hội thoại ở cột bên trái có lưu trữ độc lập theo từng phiên không.
  * Kiểm tra bộ đếm hạn ngạch: Đặt câu hỏi và kiểm tra số lượt RAG còn lại trong ngày (giảm từ 50 xuống 49...).
* 📸 **Bộ ảnh cần chụp đưa vào Slide**: Ảnh danh sách Chunks có phân trang và số trang đối chứng, ảnh bong bóng chat AI có Citation Card chi tiết, ảnh cấu hình bóc tách text bằng PDFBox trong IDE.

---

### 3️⃣ BẠN HUỲNH NHẬT (KIỂM THỬ LUỒNG QUIZ & PDF EXPORT — SLIDE 12 ➔ 14)
* [ ] **Test Luồng 1 (AI Sinh Đề Thi Trắc Nghiệm)**:
  * Vào môn học, truy cập mục **"Luyện Đề Trắc Nghiệm"** (`/courses/{id}/quizzes`).
  * Bấm nút "Tạo Đề Thi Bằng AI" $\rightarrow$ Chọn mức độ (EASY / MEDIUM / HARD) và số lượng câu hỏi từ tài liệu đã bóc tách Chunks.
  * Xác nhận đề thi sinh ra có đầy đủ 4 đáp án A/B/C/D hợp lý, câu hỏi chuẩn kiến thức giáo trình.
* [ ] **Test Luồng 2 (Trải Nghiệm Phòng Thi Bấm Giờ & Chấm Điểm)**:
  * Bấm vào làm bài thi (`takeQuiz`): Kiểm tra đồng hồ đếm ngược (Countdown Timer) chạy lùi chuẩn từng giây.
  * Chọn các phương án A/B/C/D và bấm "Nộp bài thi" $\rightarrow$ Kiểm tra màn hình kết quả: Chấm điểm thang 10 tức thì, hiển thị huy hiệu Đạt/Chưa đạt, bảng thống kê câu đúng/sai và lời giải thích chi tiết cho từng câu.
* [ ] **Test Luồng 3 (Xuất Đề Thi PDF Chuẩn In Ấn — Gói Pro)**:
  * Với tài khoản Pro/Admin, bấm nút **"Xuất Đề Thi Ra PDF"**.
  * Tải file `.pdf` về máy, mở bằng trình đọc PDF (Chrome / Adobe Reader): Kiểm tra tiêu đề header, thông tin môn học, watermark trường, ô vuông checkbox A/B/C/D không bị lỗi font tiếng Việt Unicode.
* 📸 **Bộ ảnh cần chụp đưa vào Slide**: Modal AI sinh đề, giao diện phòng thi đang chạy đồng hồ bấm giờ, màn hình kết quả thi chi tiết, ảnh chụp trang PDF đề thi mở thực tế trên máy tính.

---

### 4️⃣ BẠN LÊ NGUYỄN MINH THẮNG (KIỂM THỬ LUỒNG STUDY PLANNER — SLIDE 15 ➔ 17)
* [ ] **Test Luồng 1 (Tạo Lộ Trình Ôn Thi AI)**:
  * Đăng nhập tài khoản Pro/Admin, vào mục **"Lịch Học Vừa Sức"** (`/planner`).
  * Nhập mục tiêu (Ví dụ: *"Ôn thi PE và Final đạt điểm 9+"*), chọn ngày thi deadline (cách hiện tại 2 - 3 tuần) và số giờ rảnh mỗi ngày $\rightarrow$ Bấm nút "Sinh Lộ Trình Ôn Tập AI".
  * Kiểm tra bảng lộ trình sinh ra: Chia khoa học theo các tuần (Week 1: Nền tảng, Week 2: Chuyên sâu, Week 3: Luyện đề tổng hợp).
* [ ] **Test Luồng 2 (Quản Lý Nhiệm Vụ & Progress Bar)**:
  * Kiểm tra từng nhiệm vụ (Study Task) có gắn nhãn chương và khối lượng vừa sức không.
  * Thử click vào các checkbox đánh dấu hoàn thành nhiệm vụ $\rightarrow$ Kiểm tra thanh tiến độ (Progress Bar) có tự động tăng tỷ lệ phần trăm (%) tương ứng không.
* [ ] **Test Luồng 3 (Bảo Mật Dữ Liệu IDOR)**:
  * Đăng xuất và đăng nhập bằng một tài khoản sinh viên khác $\rightarrow$ Đảm bảo sinh viên này không nhìn thấy lộ trình học cá nhân của sinh viên ban đầu.
* 📸 **Bộ ảnh cần chụp đưa vào Slide**: Form nhập deadline và mục tiêu điểm số, giao diện bảng lộ trình chia theo tuần của AI, thanh Progress Bar hiển thị % tiến độ học tập.

---

### 5️⃣ BẠN NGÔ ÂN (KIỂM THỬ LUỒNG ADMIN COMMAND CENTER, VNPAY REVENUE & QA — SLIDE 18 ➔ 22)
* [ ] **Test Luồng 1 (Admin Dashboard & Thống Kê Doanh Thu VNPay)**:
  * Đăng nhập tài khoản Admin (`admin` / `password123`) $\rightarrow$ Truy cập `/admin/dashboard`.
  * Kiểm tra thẻ KPI **"Doanh thu VNPay"**: Hiển thị tổng thu (VNĐ), số lượng giao dịch thành công.
  * Kiểm tra Bảng **"Lịch Sử Đơn Hàng & Doanh Thu VNPay Sandbox"**: Hiển thị danh sách các đơn hàng thực tế kèm mã đơn hàng `txnRef`, sinh viên, gói cước, số tiền, ngân hàng, mã GD VNPay và trạng thái `THÀNH CÔNG`.
* [ ] **Test Luồng 2 (Kiểm Thử Thanh Toán Sandbox Thực Tế & Nhảy Số Doanh Thu Tức Thì)**:
  * Mở trình duyệt ẩn danh hoặc dùng tài khoản sinh viên `student1`, bấm nút thanh toán gói PRO (49.000đ) qua cổng VNPay NCB.
  * Nhập thẻ test NCB: Số thẻ `9704198526191432198`, Tên `NGUYEN VAN A`, Ngày phát hành `07/15`, OTP `123456`.
  * Thanh toán thành công $\rightarrow$ Hệ thống điều hướng về `/courses` hoặc `/profile`, tài khoản được tự động nâng cấp PRO và gia hạn thêm 30 ngày.
  * Đăng nhập lại Admin (`/admin/dashboard`): Kiểm tra doanh thu tự động cộng thêm 49.000đ, bảng đơn hàng xuất hiện thêm dòng giao dịch mới mà không cần can thiệp DB!
* [ ] **Test Luồng 3 (Xuất Báo Cáo CSV & Đối Soát 1-1 Với Cổng Merchant Portal VNPay)**:
  * Tại `/admin/dashboard`, bấm nút **"Xuất CSV Đối Soát"** $\rightarrow$ Tải file `vnpay-transactions-report.csv`. Mở file bằng Excel kiểm tra tiếng Việt Unicode UTF-8 BOM hiển thị chuẩn đẹp, dữ liệu đầy đủ 8 cột.
  * Bấm nút **"Portal VNPay"** (hoặc truy cập `https://sandbox.vnpayment.vn/merchantv2/` đăng nhập `thanhtung22012006@gmail.com`) $\rightarrow$ Vào mục tra cứu giao dịch: So sánh mã GD VNPay (`vnpayTransactionNo`) trên hệ thống StudyHub khớp chính xác 1-1 với máy chủ VNPay Sandbox!
* [ ] **Test Luồng 4 (Quản Lý Sinh Viên, Centralized Key & Latency Ping)**:
  * Vào `/admin/users`: Kiểm tra phân loại gói cước, tính năng reset hạn ngạch RAG.
  * Vào `/admin/settings`: Kiểm tra API Key Masking (`AIzaSy...****`) và bấm nút **"Ping Test"** kiểm tra độ trễ ms tới Gemini API.
* [ ] **Test Luồng 5 (Nghiệm Thu Toàn Bộ 333 Tests)**:
  * Mở Terminal chạy lệnh: `mvn test` trên máy local.
  * Chụp ảnh màn hình kết quả: **Tests run: 333, Failures: 0, Errors: 0, Skipped: 0** để đưa vào Slide QA chứng minh chất lượng code không có bất kỳ lỗi nào.
* 📸 **Bộ ảnh cần chụp đưa vào Slide**: Thẻ KPI Doanh thu VNPay & Bảng đơn hàng thời gian thực, File Excel xuất CSV tiếng Việt có dấu, Ảnh đối soát giao dịch trên VNPay Merchant Portal, Màn hình Terminal `mvn test` xanh 100%.

---

### 6️⃣ TEAM LEAD LÊ THANH TÙNG (KIỂM THỬ SQUAD HUB & FEATURE GATING — SLIDE 1 ➔ 4)
* [ ] **Test Luồng 1 (Shared Squad Hub & Invite Code)**:
  * Đăng nhập tài khoản Trưởng nhóm Squad (`squad_leader` / `password123`) $\rightarrow$ Kiểm tra mã mời 6 ký tự tại trang nhóm.
  * Dùng tài khoản thành viên thứ hai nhập mã mời xin gia nhập $\rightarrow$ Trưởng nhóm bấm phê duyệt thành viên.
  * Kiểm tra kho tài liệu và môn học dùng chung `[SQUAD]` xuất hiện đồng bộ ở cả hai tài khoản.
* [ ] **Test Luồng 2 (Squad Leaderboard & Đề Thi Solo)**:
  * Làm 1 đề thi nhóm $\rightarrow$ Kiểm tra điểm số hiển thị trên bảng vinh danh Squad Leaderboard.
  * Tạo 1 đề thi cá nhân (Solo riêng tư) $\rightarrow$ Xác nhận đề thi chỉ người tạo thấy và điểm thi không bị đẩy lên bảng xếp hạng chung.
* [ ] **Test Luồng 3 (Feature Gating & Modal Bảng Giá 3 Gói Cước)**:
  * Đăng nhập tài khoản Free (`student1` / `password123`), khi đã có 3 môn học thử bấm nút "Tạo Môn Mới" hoặc truy cập `/planner` $\rightarrow$ Kiểm tra Modal Bảng Giá 3 Gói Cước SaaS (`modal-pricing-upgrade`) tự động bật lên.
  * Bấm nút "Nâng Cấp Gói Pro" $\rightarrow$ Kiểm tra hệ thống gọi `/auth/upgrade-pro`, cập nhật ngay gói PRO và thông báo thành công.
* 📸 **Bộ ảnh cần chụp đưa vào Slide**: Giao diện Quản lý Squad có Invite Code, Bảng xếp hạng Leaderboard môn nhóm, Modal Bảng Giá 3 Gói Cước với ribbon badge PRO nổi bật.

---

## 🛠️ PHẦN V: KẾ HOẠCH GIAO TASK PHÂN HỆ RAG AI CHATBOT CHO BÌNH - SỬA GẤP TRƯỚC 20:30

> **Người giao việc**: Lê Thanh Tùng (Team Lead - Group 2)  
> **Người thực hiện**: Nguyễn Văn Gia Bình (Phụ trách Phân hệ RAG Engine & AI Chatbot)  
> **Mục tiêu**: Xử lý triệt để 3 lỗi nghiêm trọng được Team Lead chỉ ra ngày 04/11 trên giao diện `/courses/{courseId}/chat`: Nút "Cuộc trò chuyện mới" bị liệt, Lỗi tàng hình chữ trên Header Bar, và Thiếu bộ render Markdown Table/Format trong bong bóng Chatbot.  
> **Hạn chót hoàn thành**: Trước 20:30 tối nay (để kịp tổng duyệt demo).

## 📸 1. TỔNG HỢP CÁC LỖI THỰC TẾ CẦN BÌNH SỬA GẤP

| STT | Vấn đề / Lỗi | Hiện trạng thực tế | Yêu cầu kết quả đạt được (DoD) | Mức độ |
| :---: | :--- | :--- | :--- | :---: |
| **1** | **Chữ nút Header bị tàng hình** | Nút **"Quản Lý Giáo Trình & Chunks"** dùng class `text-white border-white/30` trên nền canvas trắng $\rightarrow$ Chữ hoàn toàn vô hình, chỉ trơ trọi icon PDF màu đỏ. | Chuyển sang White-Card: Nền trắng, viền xám nhạt `border-slate-200`, chữ xám đậm `text-slate-700 font-bold`. Nhìn rõ ràng, sắc nét. | 🔴 **Nghiêm trọng (P0)** |
| **2** | **Badge trạng thái Header bị mờ chói** | Các badge `AI Multi-Key Engine` và `GÓI PRO` dùng màu pastel của Dark Mode (`text-emerald-300`, `text-amber-300`) trên nền trắng rất khó đọc. | Chuyển sang mã màu Light Card: `bg-emerald-50 text-emerald-700 border-emerald-200` và `bg-amber-50 text-amber-800 border-amber-300`. | 🟡 **Ưu tiên (P1)** |
| **3** | **Nút "+ Cuộc Trò Chuyện Mới" không hoạt động** | Khi bấm nút, URL redirect về `/chat`, hàm `showChatPage` lại tự động load lại phiên chat cũ nhất `sessions.get(0)`. Người dùng không thể tạo phiên chat trắng mới. | Bấm nút phải khởi tạo ngay 1 `ChatSession` mới (hoặc mở khung chat trắng tinh). Tin nhắn đầu tiên gửi đi sẽ tự động đặt tiêu đề cho phiên chat đó. | 🔴 **Nghiêm trọng (P0)** |
| **4** | **Không render Bảng & Cú pháp Markdown** | Khi AI trả lời bằng bảng Markdown (`\|:---\|:---\|`), giao diện hiển thị text thô `\| Khả năng \| PON \|...` rất xấu, không format thành HTML table, chữ đậm `**` không in đậm. | Tích hợp thư viện `marked.js` để tự động render toàn bộ bảng biểu, tiêu đề, chữ đậm, code blocks, trích dẫn chuẩn DaisyUI Table. | 🔴 **Nghiêm trọng (P0)** |

---

## 🛠️ 2. HƯỚNG DẪN KỸ THUẬT CHI TIẾT DÀNH CHO BÌNH

### Task 1: Sửa lỗi hiển thị trên Header Bar (`src/main/resources/templates/rag/chat.html`)
* **Vị trí**: Dòng 35 - 56 trong `chat.html`.
* **Code hiện tại bị lỗi**:
  ```html
  <!-- LỖI: text-white trên nền trắng -->
  <a th:href="@{/courses/{id}/documents(id=${course.id})}" class="btn btn-sm h-10 px-4 btn-outline border-white/30 text-white hover:bg-white/10 rounded-xl font-bold gap-2">
      <i class="fa-solid fa-file-pdf text-rose-400"></i>
      <span>Quản Lý Giáo Trình & Chunks</span>
  </a>
  ```
* **Bình thay thế bằng chuẩn White-Card Palette**:
  ```html
  <!-- ĐÃ SỬA: Chuẩn White Card, chữ rõ ràng 100% -->
  <a th:href="@{/courses/{id}/documents(id=${course.id})}" 
     class="btn btn-sm h-10 px-4 bg-white hover:bg-slate-50 border border-slate-200 text-slate-700 shadow-sm rounded-xl font-bold gap-2 transition-all">
      <i class="fa-solid fa-file-pdf text-rose-500"></i>
      <span>Quản Lý Giáo Trình & Chunks</span>
  </a>
  ```
* **Sửa lại màu 2 Badge trạng thái cho rõ nét**:
  ```html
  <div th:if="${hasSystemKey}" class="inline-flex items-center gap-2 py-1.5 px-3.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200 text-xs font-bold shadow-xs">
      <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
      <span>AI Multi-Key Engine: Đã Sẵn Sàng</span>
  </div>

  <div th:if="${isPro}" class="inline-flex items-center gap-1.5 py-1.5 px-3.5 rounded-full bg-gradient-to-r from-amber-50 to-yellow-50 border border-amber-200 text-amber-800 text-xs font-black shadow-xs">
      <i class="fa-solid fa-crown text-amber-500"></i>
      <span>GÓI PRO: VÔ HẠN RAG</span>
  </div>
  ```

---

### Task 2: Triển khai hoàn chỉnh tính năng "Cuộc Trò Chuyện Mới"
* **File cần sửa**: `ChatController.java` (`com.group2.assignment.rag.controller`).
* **Vị trí**: Hàm `startNewChat()` (dòng 264) và `showChatPage()` (dòng 52).

#### A. Trong `ChatController.java`:
```java
/**
 * Khởi tạo phiên trò chuyện mới: Tạo một ChatSession mới trong DB và redirect vào đúng phiên đó
 */
@GetMapping("/chat/new")
public String startNewChat(@PathVariable Long courseId, HttpSession session) {
    Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + courseId));

    User currentUser = getCurrentUser(session);
    
    // Tạo phiên chat mới với tiêu đề mặc định
    ChatSession newSession = chatSessionRepository.save(ChatSession.builder()
            .course(course)
            .user(currentUser)
            .title("Cuộc trò chuyện mới")
            .build());

    return "redirect:/courses/" + courseId + "/chat?sessionId=" + newSession.getId();
}
```

#### B. Trong `ChatController.sendChatMessageJson()`:
Khi người dùng gửi câu hỏi đầu tiên vào phiên vừa tạo có tên là `"Cuộc trò chuyện mới"`, tự động cập nhật tiêu đề thành nội dung câu hỏi đầu tiên (cắt tối đa 40 ký tự) để sidebar hiển thị lịch sử thông minh:
```java
if ("Cuộc trò chuyện mới".equals(sessionEntity.getTitle())) {
    String autoTitle = requestDto.getQuestion().trim();
    if (autoTitle.length() > 40) {
        autoTitle = autoTitle.substring(0, 37) + "...";
    }
    sessionEntity.setTitle(autoTitle);
    chatSessionRepository.save(sessionEntity);
}
```

---

### Task 3: Tích hợp thư viện `marked.js` render Markdown Table & Cú pháp AI
* **File cần sửa**: `src/main/resources/templates/rag/chat.html`.

#### A. Thêm thư viện `marked.js` vào thẻ `<head>` hoặc cuối trang:
```html
<script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
```

#### B. Thêm cấu hình CSS cho bảng Markdown trong `chat.html`:
```html
<style>
    /* Styling bảng Markdown do AI sinh ra */
    .markdown-body table {
        width: 100%;
        border-collapse: collapse;
        margin: 0.75rem 0;
        font-size: 0.8125rem;
    }
    .markdown-body th {
        background-color: #f1f5f9;
        color: #1e293b;
        font-weight: 700;
        padding: 0.5rem 0.75rem;
        border: 1px solid #cbd5e1;
        text-align: left;
    }
    .markdown-body td {
        padding: 0.5rem 0.75rem;
        border: 1px solid #e2e8f0;
        color: #334155;
    }
    .markdown-body tr:nth-child(even) td {
        background-color: #f8fafc;
    }
    .markdown-body ul {
        list-style-type: disc;
        margin-left: 1.25rem;
        margin-top: 0.25rem;
    }
    .markdown-body ol {
        list-style-type: decimal;
        margin-left: 1.25rem;
        margin-top: 0.25rem;
    }
    .markdown-body code {
        font-family: monospace;
        background-color: #e2e8f0;
        padding: 0.125rem 0.375rem;
        border-radius: 0.25rem;
        color: #7c3aed;
        font-size: 0.8em;
    }
</style>
```

#### C. Cập nhật script render tin nhắn AI:
```javascript
function renderMarkdownContent(text) {
    if (!text) return '';
    try {
        if (typeof marked !== 'undefined') {
            return marked.parse(text);
        }
    } catch (e) {
        console.warn('Lỗi parse marked:', e);
    }
    return escapeHtml(text);
}

document.addEventListener('DOMContentLoaded', function () {
    // Render tất cả tin nhắn markdown có sẵn từ server
    document.querySelectorAll('.ai-markdown-content').forEach(function (el) {
        const rawContent = el.getAttribute('data-raw');
        if (rawContent) {
            el.innerHTML = renderMarkdownContent(rawContent);
        }
    });
});
```

---

## ✅ 3. CRITERIA NGHIỆM THU (ACCEPTANCE CRITERIA)
1. **Header Bar**: Nhìn thấy rõ 100% chữ `"Quản Lý Giáo Trình & Chunks"` màu xám đậm nổi bật trên nền trắng, không bị tàng hình.
2. **New Chat Session**: Bấm `+ Cuộc Trò Chuyện Mới` $\rightarrow$ Màn hình chat chuyển sang phiên trắng mới, danh sách lịch sử bên trái xuất hiện phiên mới. Khi gửi tin nhắn đầu tiên, tiêu đề tự động cập nhật theo câu hỏi.
3. **Table Markdown**: Khi chatbot trả lời có bảng biểu $\rightarrow$ Bảng hiển thị dạng kẻ ô (border), có sọc xen kẽ (zebra), chữ in đậm và bullet list hiển thị đẹp mắt.
4. **Kiểm thử tự động**: Chạy `mvn test -Dtest=ChatControllerQuotaTest` và `mvn test` $\rightarrow$ Toàn bộ test cases đều **PASS 100%**.



---

## 💳 PHẦN VI: KẾ HOẠCH GIAO TASK PHÂN HỆ QUẢN TRỊ DOANH THU & ĐỐI SOÁT ĐƠN HÀNG VNPAY CHO ÂN

> **Người giao việc**: Lê Thanh Tùng (Team Lead - Group 2)  
> **Người thực hiện**: Ngô Ân (Phụ trách Admin Command Center, Phân hệ Doanh thu VNPay & QA)  
> **Mục tiêu**: Nắm vững toàn bộ kiến trúc, cơ chế vận hành của cổng thanh toán trực tuyến **VNPay Sandbox**, phân hệ tính toán doanh thu tự động, quản lý lịch thanh toán & hạn gói cước của sinh viên, xuất file báo cáo tài chính CSV và thực hiện đối soát 1-1 với Cổng quản trị VNPay Merchant Portal (`merchantv2`).  
> **Thời điểm nghiệm thu & tổng duyệt demo**: Tối nay 04/11/2026.

---

### 🌐 1. TỔNG HỢP CÁC TÍNH NĂNG NGHIỆP VỤ LIÊN ĐỚI ĐÃ TRIỂN KHAI

Khi tích hợp cổng thanh toán trực tuyến thực tế **VNPay Sandbox**, hệ thống StudyHub hình thành một chu trình khép kín giữa Người dùng - Cổng thanh toán - Quản trị viên:

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                 CHU TRÌNH THANH TOÁN & ĐỐI SOÁT DOANH THU                              │
├────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 1. SINH VIÊN (Student Client):                                                                         │
│    - Chọn gói PRO (49.000đ) hoặc SQUAD (129.000đ) tại Trang Chủ / Bảng Giá.                           │
│    - Chọn phương thức: "Thanh toán VNPay ATM NCB" hoặc "Thẻ Quốc Tế Visa".                             │
│    - Sử dụng thông tin thẻ test NCB để xác thực OTP "123456" trên cổng sandbox.vnpayment.vn.          │
│                                                                                                        │
│ 2. CỔNG THANH TOÁN VNPAY SANDBOX (Gateway):                                                            │
│    - Kiểm tra chữ ký số Checksum HMAC-SHA512 với `vnp_HashSecret`.                                     │
│    - Xác nhận trừ tiền giả lập thành công (Mã phản hồi: `vnp_ResponseCode = 00`).                      │
│    - Chuyển hướng trình duyệt về `PaymentReturnUrl`: `/payment/vnpay-return`.                          │
│                                                                                                        │
│ 3. HỆ THỐNG XỬ LÝ BACKEND (PaymentService & Subscription Lifecycle):                                   │
│    - Kiểm tra bảo mật Checksum dữ liệu trả về từ VNPay.                                                │
│    - Lưu vết giao dịch vào Database: `PaymentTransaction` (Trạng thái: `SUCCESS`).                      │
│    - Tự động nâng cấp gói cước (`PRO` hoặc `SQUAD`) và cộng dồn 30 ngày hạn dùng (`tierExpiresAt`).    │
│                                                                                                        │
│ 4. TRANG CÁ NHÂN SINH VIÊN (/profile):                                                                 │
│    - Hiển thị ngày hết hạn gói cước rõ ràng (VD: 30 ngày kể từ ngày thanh toán).                       │
│    - Bảng lịch sử biên lai điện tử: Mã đơn hàng, Gói cước, Số tiền, Ngân hàng, Mã GD VNPay.           │
│                                                                                                        │
│ 5. TRUNG TÂM ĐIỀU HÀNH ADMIN DASHBOARD (/admin/dashboard):                                             │
│    - Thẻ KPI "Doanh thu VNPay" tự động nhảy số theo thời gian thực (Real-time).                        │
│    - Phân tách doanh thu theo gói PRO vs SQUAD, doanh thu hôm nay và tổng số đơn thành công.          │
│    - Bảng Lịch sử Đơn hàng Sandbox lưu vết đầy đủ từng giao dịch.                                      │
│    - Nút bấm "Xuất CSV Đối Soát": Tải file UTF-8 BOM mở bằng Excel không lỗi font.                    │
│    - Nút bấm "Portal VNPay": Mở cổng `sandbox.vnpayment.vn/merchantv2` đối soát 1-1 mã giao dịch.      │
└────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

### 🛠️ 2. HƯỚNG DẪN KỸ THUẬT & KIẾN TRÚC CODE DÀNH CHO ÂN

#### A. Cấu trúc dữ liệu & Thuật toán tính toán doanh thu (`PaymentTransactionRepository.java`):
Ân cần nắm vững các câu lệnh truy vấn JPQL tổng hợp doanh thu phục vụ báo cáo:
```java
// 1. Tổng doanh thu toàn hệ thống từ các đơn hàng thành công
@Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.status = 'SUCCESS'")
Long getTotalRevenue();

// 2. Doanh thu phân rã theo từng gói cước (PRO hoặc SQUAD)
@Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.status = 'SUCCESS' AND p.targetTier = :tier")
Long getRevenueByTier(@Param("tier") String tier);

// 3. Doanh thu phát sinh trong ngày hôm nay (từ 00:00:00 đến hiện tại)
@Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.status = 'SUCCESS' AND p.createdAt >= :since")
Long getRevenueSince(@Param("since") LocalDateTime since);

// 4. Lịch sử giao dịch cá nhân của một sinh viên (hiển thị tại trang Profile)
List<PaymentTransaction> findTop10ByUsernameOrderByCreatedAtDesc(String username);
```

#### B. Cơ chế Subscription Lifecycle & Gia hạn cộng dồn (`PaymentService.java`):
Khi sinh viên thanh toán thành công, hệ thống không ghi đè ngày hạn dùng mà **tự động cộng dồn 30 ngày**:
```java
// Logic cộng dồn hạn dùng gói cước thông minh
LocalDateTime now = LocalDateTime.now();
LocalDateTime base = (user.getTierExpiresAt() != null && user.getTierExpiresAt().isAfter(now))
        ? user.getTierExpiresAt() // Nếu còn hạn cũ, lấy mốc hết hạn cũ để cộng tiếp
        : now;                    // Nếu đã hết hạn hoặc lần đầu mua, lấy mốc hiện tại
user.setTierExpiresAt(base.plusDays(30)); // Gia hạn thêm đúng 30 ngày
userRepository.save(user);
```

#### C. Chức năng Xuất Báo Cáo Tài Chính CSV Đối Soát (`AdminController.java`):
Xuất dữ liệu chuẩn quốc tế kèm ký tự **UTF-8 BOM (`﻿`)** để Microsoft Excel trên Windows tự động nhận diện tiếng Việt có dấu mà không bị lỗi font:
```java
@GetMapping(value = "/revenue/export-csv")
@ResponseBody
public ResponseEntity<String> exportRevenueCsv() {
    StringBuilder csv = new StringBuilder();
    csv.append('\ufeff'); // UTF-8 BOM cho Excel tiếng Việt
    csv.append("Mã Đơn Hàng (TxnRef),Sinh Viên,Gói Cước,Số Tiền (VNĐ),Ngân Hàng,Mã GD VNPay,Trạng Thái,Thời Gian\n");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    for (PaymentTransaction tx : paymentTransactionRepository.findAllByOrderByCreatedAtDesc()) {
        csv.append('"').append(tx.getTxnRef()).append("",")
           .append('"').append(tx.getUsername()).append("",")
           .append('"').append(tx.getTargetTier()).append("",")
           .append(tx.getAmount()).append(',')
           .append('"').append(tx.getBankCode()).append("",")
           .append('"').append(tx.getVnpayTransactionNo()).append("",")
           .append('"').append(tx.getStatus()).append("",")
           .append('"').append(tx.getCreatedAt().format(dtf)).append(""\n");
    }
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"vnpay-transactions-report.csv\"")
            .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
            .body(csv.toString());
}
```

---

### 💳 3. BẢNG THÔNG TIN CẤU HÌNH & THẺ TEST VNPAY SANDBOX

Ân sử dụng bộ thông tin chính thức đã được đăng ký thành công trên cổng Sandbox VNPay:

| Mục cấu hình | Giá trị môi trường Sandbox | Ghi chú kỹ thuật |
| :--- | :--- | :--- |
| **Mã Website (`vnp_TmnCode`)** | `AYM70LAS` | Terminal ID định danh ứng dụng StudyHub trên cổng VNPay |
| **Chuỗi bí mật (`vnp_HashSecret`)** | `NDJFXTYRTNJRRTHQNDDBUVGDXPTYPFMY` | Khóa bí mật tạo chữ ký số HMAC-SHA512 chống giả mạo dữ liệu |
| **Payment Gateway URL** | `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html` | Cổng thanh toán môi trường thử nghiệm |
| **Return URL** | `http://localhost:8080/payment/vnpay-return` | Đường dẫn máy chủ tiếp nhận kết quả thanh toán |
| **Cổng Quản trị Merchant Portal** | `https://sandbox.vnpayment.vn/merchantv2/` | Trang đối soát dành cho Merchant |
| **Tài khoản Merchant Admin** | `thanhtung22012006@gmail.com` | Email quản trị viên đăng ký với VNPay |

#### 💳 Thông tin thẻ test phục vụ Demo:
* **Thẻ ATM Nội Địa (Ngân hàng NCB)**:
  * Ngân hàng: **NCB**
  * Số thẻ: `9704198526191432198`
  * Tên chủ thẻ: `NGUYEN VAN A`
  * Ngày phát hành: `07/15`
  * Mật khẩu OTP: `123456`
* **Thẻ Quốc Tế (Visa Test)**:
  * Số thẻ: `4456530000001005`
  * Hạn dùng: `12/26`
  * CVV: `123`

---

### 🎙️ 4. KỊCH BẢN THUYẾT TRÌNH & DEMO TRỰC TIẾP DÀNH CHO ÂN (SLIDE 18)

> **Hướng dẫn tác phong**: Khi Tùng chuyển mic cho Ân hoặc trong phần demo live của nhóm, Ân sẽ trực tiếp mở màn hình Admin Dashboard (`/admin/dashboard`) và trình bày dõng dạc theo kịch bản:

*"Thưa quý Thầy Cô, bên cạnh việc giám sát các chỉ số kỹ thuật như số Chunks tài liệu hay lượt gọi AI RAG, một sản phẩm SaaS thương mại hoàn chỉnh bắt buộc phải có phân hệ **Quản trị Tài chính và Cổng Thanh Toán Trực Tuyến**.*

*Nhóm 2 chúng em đã tích hợp thành công cổng thanh toán **VNPay Sandbox 2.1.0** với mã định danh kết nối chính thức `AYM70LAS`. Ngay trên màn hình **Admin Command Center**, quý Thầy Cô có thể thấy:*
1. *Thẻ KPI **Doanh thu VNPay** tổng hợp tức thời: Hiện tại hệ thống ghi nhận tổng doanh thu, tự động phân tách theo gói Pro 49.000đ và gói Squad 129.000đ, cùng doanh thu phát sinh trong ngày hôm nay.*
2. *Bảng **Lịch Sử Đơn Hàng & Doanh Thu**: Lưu vết minh bạch từng giao dịch, bao gồm mã đơn hàng nội bộ `TxnRef`, tài khoản sinh viên, số tiền, ngân hàng thanh toán và đặc biệt là **Mã giao dịch đối soát của VNPay**.*
3. *Em cũng tích hợp tính năng **Xuất Báo Cáo Doanh Thu ra file CSV** chuẩn định dạng UTF-8 BOM, giúp kế toán hoặc người quản trị tải về và mở ngay trên Microsoft Excel mà không hề bị lỗi font tiếng Việt.*
4. *Để chứng minh tính minh bạch tuyệt đối, em xin phép mở **Cổng Quản Trị VNPay Merchant Portal** tại địa chỉ `sandbox.vnpayment.vn/merchantv2` — toàn bộ mã giao dịch trên StudyHub đều khớp chính xác 1-1 với máy chủ thanh toán của VNPay!*

*Tiếp theo, về mặt chất lượng mã nguồn, toàn bộ phân hệ thanh toán và hệ thống đã vượt qua trọn vẹn **333 bài kiểm thử tự động**, sẵn sàng vận hành thực chiến!"*

---

### ✅ 5. CRITERIA NGHIỆM THU DÀNH CHO ÂN (ACCEPTANCE CRITERIA)
1. **Thanh toán mượt mà**: Thao tác thanh toán gói Pro hoặc Squad bằng thẻ test NCB thành công 100%, không phát sinh lỗi 72 hay lỗi kết nối.
2. **Nhảy số thời gian thực**: Khi đơn hàng thanh toán thành công, thẻ KPI Doanh thu trên `/admin/dashboard` tự động cộng dồn số tiền tương ứng, danh sách đơn hàng cập nhật ngay lập tức.
3. **Quản lý hạn gói sinh viên**: Trang `/profile` của sinh viên hiển thị chính xác ngày hết hạn gói cước (`tierExpiresAt`) và hiển thị đầy đủ bảng lịch sử hóa đơn thanh toán cá nhân.
4. **Xuất file CSV chuẩn đẹp**: Bấm nút "Xuất CSV Đối Soát" tải về file `.csv`, mở bằng Excel thấy đầy đủ dấu tiếng Việt, không bị mã hóa ký tự lạ.
5. **Đối soát 1-1**: Mã giao dịch `vnpayTransactionNo` hiển thị trên bảng khớp hoàn toàn với mã giao dịch tra cứu trên cổng `sandbox.vnpayment.vn/merchantv2`.
