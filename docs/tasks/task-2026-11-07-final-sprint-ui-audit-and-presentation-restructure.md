# 🚀 KẾ HOẠCH TỔNG LỰC SPRINT CUỐI CÙNG: RÀ SOÁT LỖI GIAO DIỆN & CHỨC NĂNG, TÁI CẤU TRÚC THỨ TỰ SLIDE VÀ PHÂN CÔNG QUẢN LÝ DOANH THU VNPAY CHO ÂN
> **Ngày ban hành**: 07/11/2026  
> **Người chủ trì & phân công**: Lê Thanh Tùng (Team Lead - Group 2)  
> **Môn học**: HSF302 (Java & Spring Framework) — FPT University  
> **Đặc thù dự án Group 2**: **Gộp cả Assignment (UI Toolkit + AI Agent) và Project Cuối kỳ (RAG AI StudyHub + Google OAuth2 + Shared Squad Hub + Quản lý 3 gói cước + Cổng thanh toán VNPay Sandbox)**  
> **Hạn chót tổng duyệt**: 20:30 tối nay  

---

## 📌 PHẦN I: THÔNG ĐIỆP CHỈ ĐẠO TỪ TEAM LEAD LÊ THANH TÙNG

Thân gửi toàn thể 5 thành viên Nhóm 2,

Chúng ta đang bước vào giai đoạn quyết định cuối cùng trước giờ báo cáo Final với Hội đồng Giảng viên. Nhằm đảm bảo bài báo cáo đạt điểm tối đa (A+), tôi ban hành kế hoạch hành động khẩn cấp ngày hôm nay với **4 nhiệm vụ cốt lõi bắt buộc**:

1. **Khắc phục dứt điểm 100% các đề xuất tồn đọng từ cuộc họp ngày 04/11**:
   - Tất cả các lỗi và cải tiến đã ghi nhận trong cuộc họp ngày 04/11 phải được kiểm tra lại và đóng dứt điểm (Không để lại bất kỳ lỗi runtime, lỗi vỡ giao diện hay thiếu sót dữ liệu nào).
2. **Chủ động tự rà soát (Self-Audit) giao diện và chức năng**:
   - Từng thành viên chịu trách nhiệm kiểm tra kỹ lưỡng phân hệ của mình: từ tính thẩm mỹ của chuẩn thiết kế **White-Card Palette**, độ tương phản chữ, tính responsive trên màn hình máy chiếu hội đồng, đến tính đúng đắn của logic nghiệp vụ và dữ liệu hiển thị.
3. **Tái cấu trúc thứ tự thuyết trình Slide theo luồng "Từ ngoài vào trong" (Natural User Journey)**:
   - **Thay đổi quan trọng**: Thay vì Team Lead nói trước, bạn **Ngô Nhật Trường** sẽ là người **mở màn thuyết trình Khối 1**.
   - **Lý do hoàn toàn thuyết phục**: Hành trình trải nghiệm của người dùng bắt đầu từ cửa ngõ bên ngoài (Trang Đăng nhập, Đăng ký, Google OAuth2, Hồ sơ cá nhân Profile, Đăng xuất Logout) và đáp ứng ngay 2 tiêu chí đầu tiên của đề bài Assignment (UI Toolkit Tailwind/DaisyUI + AI Agent trong IDE). Sau khi người dùng đã bước qua cánh cửa xác thực để vào bên trong hệ thống, **Team Lead Lê Thanh Tùng** sẽ tiếp quản Khối 2 để trình bày Bức tranh giải pháp, Mô hình kinh doanh 3 gói cước và Không gian học nhóm Shared Squad Hub!
4. **Chính thức giao Phân hệ Quản trị Doanh thu & Cổng thanh toán VNPay Sandbox cho bạn Ngô Ân**:
   - Bạn Ân tiếp nhận phân hệ tính toán doanh thu thời gian thực, quản lý lịch sử đơn hàng, xuất báo cáo tài chính CSV UTF-8 BOM và thực hiện đối soát 1-1 với Cổng quản trị VNPay Merchant Portal (`merchantv2`).

---

## 👥 PHẦN II: BẢNG PHÂN CHIA NHÂN SỰ & THỨ TỰ THUYẾT TRÌNH ĐỔI MỚI

> **Nguyên tắc "Khối liền mạch — Đi từ ngoài vào trong"**:
> * **Khối 1 (Cổng vào & Assignment)**: Trường mở màn từ màn hình ngoài cùng (Login/Logout/OAuth2/UI Toolkit).
> * **Khối 2 (Bên trong hệ thống & Core)**: Tùng dẫn dắt giải pháp lớn, 3 gói cước SaaS và Shared Squad Hub.
> * **Khối 3 ➔ 5 (Công cụ học tập chuyên sâu)**: Bình (RAG AI Chat) $\rightarrow$ Nhật (AI Quiz) $\rightarrow$ Thắng (AI Planner).
> * **Khối 6 (Quản trị, Tài chính & Vận hành)**: Ân chốt hạ toàn bộ với Admin Command Center, Doanh thu VNPay, QA 333 tests, Setup & Q&A.

| Thứ tự | Họ và Tên | Vai trò | Khối Slide phụ trách | Nội dung thuyết trình chính | Thời lượng |
| :---: | :--- | :--- | :---: | :--- | :---: |
| **1** | **Ngô Nhật Trường** | **Dev (Auth & UI)** | **Slide 1 ➔ Slide 4** *(4 slides liên tiếp)* | **Mở màn, Bối cảnh & Cổng vào**: Giới thiệu Nhóm 2 & Đề tài; Bối cảnh, Đặt vấn đề & Giải pháp tổng thể; Assignment (UI Toolkit Tailwind CSS + DaisyUI White-Card, Quy trình Prompting & AI Agent trong IDE); Authentication (Google OAuth2, Form BCrypt, 1-Click Login, Profile cá nhân, Đăng xuất Logout). | **4 phút** |
| **2** | **Lê Thanh Tùng** | **Team Lead** | **Slide 5 ➔ Slide 8** *(4 slides liên tiếp)* | **Bên trong hệ thống & Kiến trúc lõi**: Kiến trúc công nghệ tổng thể (Tech Stack Enterprise); Mô hình kinh doanh 3 gói cước SaaS (FREE/PRO/SQUAD); Không gian học nhóm Shared Squad Hub (Invite Code, Duyệt thành viên, Môn nhóm dùng chung); Feature Gating, Đấu trường Leaderboard & Đề thi Solo riêng tư. | **4 phút** |
| **3** | **Nguyễn Văn Gia Bình** | **Dev (RAG Engine)** | **Slide 9 ➔ Slide 11** *(3 slides liên tiếp)* | **Phân hệ RAG AI Chatbot**: Trích xuất PDF & phân mảnh Chunks (Apache PDFBox); Cơ chế Gemini Context Grounding triệt tiêu 100% ảo giác; Thẻ trích dẫn (Citation Cards) số trang đối chứng sách thực tế & Multi-session Chatbot. | **3 phút** |
| **4** | **Huỳnh Nhật** | **Dev (Quiz Engine)** | **Slide 12 ➔ Slide 14** *(3 slides liên tiếp)* | **Phân hệ AI Quiz Generator**: Sinh trắc nghiệm tự động A/B/C/D 3 mức độ từ giáo trình; Phòng thi bấm giờ Countdown & chấm điểm tự động; Đặc quyền Pro: Xuất đề thi PDF in ấn chuẩn FPT (PDFBox). | **2.5 phút** |
| **5** | **Lê Nguyễn Minh Thắng** | **Dev (Study Planner)** | **Slide 15 ➔ Slide 17** *(3 slides liên tiếp)* | **Phân hệ AI Study Planner**: Thuật toán phân tích deadline ngày thi & xếp lịch ôn tập theo tuần bằng Gemini Flash; Phân rã Study Tasks vừa sức chống ngợp & cách ly dữ liệu cá nhân; Interactive Checklist & Progress Bar. | **2.5 phút** |
| **6** | **Ngô Ân** | **Dev (Admin, Doanh Thu VNPay & QA)** | **Slide 18 ➔ Slide 22** *(5 slides liên tiếp)* | **Quản trị, Tài chính & Vận hành**: Admin Command Center (KPI thời gian thực, Nâng/hạ gói cước); **Phân hệ Quản trị Doanh thu & Cổng thanh toán VNPay Sandbox** (Thống kê doanh thu Pro/Squad/Hôm nay, Bảng lưu vết đơn hàng, Xuất CSV đối soát UTF-8 BOM, Cổng VNPay Merchant Portal); Centralized System Key & Masking & Ping Test; Báo cáo QA 333/333 tests PASS; User Guide (Setup 1 lệnh) & Mời Hội đồng vào Q&A. | **4 phút** |

---

## 📑 PHẦN III: DÀN TRANG CHI TIẾT 22 SLIDES THUYẾT TRÌNH (CHUẨN TỪ NGOÀI VÀO TRONG)

### 🎙️ KHỐI 1: NGÔ NHẬT TRƯỜNG (MỞ MÀN, BỐI CẢNH & CỔNG VÀO XÁC THỰC) — Slide 1 đến Slide 4 [~4 phút]
* **Slide 1: Trang bìa (Title Slide) & Giới thiệu Nhóm 2**
  * Tên đề tài: **SMART STUDYHUB RAG** — Nền Tảng Học Tập Đa Môn & Luyện Thi A+ Ứng Dụng RAG AI.
  * Giới thiệu Nhóm 2 — Môn HSF302 — FPT University.
  * Giới thiệu danh sách 6 thành viên kèm phân công nhiệm vụ rõ ràng.
* **Slide 2: Bối Cảnh, Đặt Vấn Đề & Giải Pháp Tổng Thể (Problem Statement & Solution)**
  * Nỗi đau sinh viên mùa thi: Giáo trình PDF hàng trăm trang; ChatGPT tự do bị ảo giác (hallucination) không nguồn đối chứng; tự học cá nhân dễ nản lòng.
  * Giải pháp Smart StudyHub RAG: Kho môn học tập trung + RAG AI đối chứng 100% không ảo giác + Study Squad học nhóm có thưởng.
* **Slide 3: Phần Assignment — Tích Hợp UI Toolkit & Quy Trình Code Với AI Agent**
  * UI Toolkit: Lựa chọn **Tailwind CSS (JIT Engine) + DaisyUI v4 + FontAwesome 6**, chuẩn phong cách **White-Card Palette** thanh lịch, Thymeleaf Layout kế thừa DRY (`layout/main.html`, fragments `navbar.html`, `sidebar.html`).
  * AI Agent trong IDE: Môi trường **Google Antigravity IDE** (Gemini 2.5 Flash & Claude 3.5 Sonnet) theo quy trình 3 bước chuẩn: **Spec-Driven** $\rightarrow$ **Test-Driven Development (TDD)** $\rightarrow$ **Pair-Debugging**.
* **Slide 4: Phân Hệ Authentication — Cổng Vào Hệ Thống, Profile Cá Nhân & Đăng Xuất**
  * Đăng nhập 1-chạm Google OAuth2 (OpenID Connect).
  * Đăng ký & đăng nhập truyền thống bảo mật mã hóa một chiều BCrypt (10 salt rounds).
  * Tiện ích chấm bài: Nút 1-Click Login tự động điền tài khoản Free, Pro, Squad.
  * Bảo mật phiên `HttpSession`, phân quyền RBAC và Hồ sơ cá nhân (Profile) kèm hạn dùng gói cước và nút Đăng xuất an toàn.
  *(Trường kết thúc Khối 1 và chuyển mic cho Team Lead Lê Thanh Tùng dẫn dắt vào bên trong hệ thống).*

---

### 🎙️ KHỐI 2: LÊ THANH TÙNG (TEAM LEAD — BÊN TRONG HỆ THỐNG & CORE ARCHITECTURE) — Slide 5 đến Slide 8 [~4 phút]
* **Slide 5: Kiến Trúc Công Nghệ Tổng Thể (Tech Stack Chuẩn Enterprise)**
  * Sơ đồ công nghệ: Spring Boot 3.3.4, Java 17 LTS, Spring Data JPA, Hibernate 6.
  * Cơ sở dữ liệu H2 Database nhúng lưu bền vững file đĩa cục bộ (`./data/studyhubdb`).
  * Thư viện Apache PDFBox 3.0.3 bóc tách giáo trình và Google Gemini 1.5 Flash API.
  * Mô hình kiến trúc phân hệ độc lập (Vertical Slice Architecture) chuẩn doanh nghiệp.
* **Slide 6: Mô Hình Kinh Doanh 3 Gói Cước SaaS Thực Tế**
  * Phân tầng 3 Gói cước:
    1. `FREE` ($0): Tối đa 3 môn học, 50 câu RAG/ngày, Quiz 10 câu.
    2. `PRO` (49.000đ/tháng): Vô hạn môn học, RAG vô hạn, mở khóa AI Study Planner & Xuất đề thi PDF in ấn.
    3. `SQUAD` (129.000đ/tháng nhóm 5 bạn): Dùng chung kho tài liệu, môn nhóm `[SQUAD]`, đua top Squad Leaderboard.
* **Slide 7: Phân Hệ Đột Phá — Shared Squad Hub (Không Gian Học Nhóm)**
  * Tạo mã mời (Invite Code 6 ký tự độc nhất) & Trưởng nhóm phê duyệt thành viên.
  * Môn học nhóm `[SQUAD]` xuất hiện đồng bộ trong Dashboard của cả 5 bạn.
  * Kho slide giáo trình và tài liệu PDF dùng chung cho cả nhóm mà không cần upload nhiều lần.
* **Slide 8: Cơ Chế Feature Gating, Đấu Trường Leaderboard & Chế Độ Thi Solo Riêng Tư**
  * Feature Gating: Chặn gói Free khi chạm mốc 3 môn hoặc khi vào tính năng Pro $\rightarrow$ Kích hoạt Modal Bảng Giá 3 Gói Cước (`modal-pricing-upgrade`).
  * Nâng cấp tức thì qua `/auth/upgrade-pro`, `/auth/upgrade-squad` và cổng thanh toán VNPay Sandbox.
  * Đấu trường Quiz nhóm & Squad Leaderboard: Vinh danh top điểm số, kèm tùy chọn làm bài Solo riêng tư không lộ điểm.
  *(Tùng kết thúc Khối 2 và chuyển mic cho bạn Nguyễn Văn Gia Bình trình bày phân hệ RAG AI Chatbot).*

---

### 🎙️ KHỐI 3: NGUYỄN VĂN GIA BÌNH (DEV: RAG ENGINE) — Slide 9 đến Slide 11 [~3 phút]
* **Slide 9: Kiến Trúc Bóc Tách PDF & Phân Mảnh Chunks (Apache PDFBox)**
  * Bóc tách PDF bằng `PDFTextStripper` từ tài liệu tải lên.
  * Thuật toán phân đoạn thông minh: Cắt đoạn 300 - 500 từ theo ranh giới câu, lưu Metadata số trang và môn học vào `ChunkRepository`.
* **Slide 10: Cơ Chế Vector Context Grounding & Gemini Flash Chat**
  * Thuật toán RAG: Tìm kiếm Chunks liên quan ghép vào Prompt của Google Gemini Flash.
  * Cam kết **giảm ảo giác (Zero Hallucination)**: AI chỉ trả lời dựa trên giáo trình được nạp.
* **Slide 11: Thẻ Trích Dẫn Đối Chứng (Citation Cards) & Đa Phiên Hội Thoại (Multi-Session)**
  * Citation Cards: Hiển thị rõ tên file PDF và số trang cụ thể để sinh viên lật sách đối chứng.
  * Quản lý đa phiên chat độc lập theo môn học, tự động kiểm soát quota 50 câu/ngày cho gói Free.
  *(Bình kết thúc Khối 3 và chuyển mic cho bạn Huỳnh Nhật trình bày Quiz Engine).*

---

### 🎙️ KHỐI 4: HUỲNH NHẬT (DEV: QUIZ ENGINE) — Slide 12 đến Slide 14 [~2.5 phút]
* **Slide 12: Phân Hệ AI Quiz Generator — Sinh Đề Trắc Nghiệm Tự Động**
  * AI quét Chunks giáo trình sinh bộ câu hỏi 4 lựa chọn A/B/C/D theo 3 mức độ (Dễ, Trung bình, Khó).
  * Phân tách đề thi theo môn học, bám sát kiến thức thực tế.
* **Slide 13: Trải Nghiệm Phòng Thi Bấm Giờ & Chấm Điểm Tự Động**
  * Đồng hồ đếm ngược (Countdown Timer) thời gian thực.
  * Tự động thu bài khi hết giờ, chấm điểm thang 10 tức thì kèm bảng giải thích chi tiết từng câu.
* **Slide 14: Đặc Quyền Pro: Xuất Đề Thi PDF Chuẩn In Ấn (PDFBox Export Service)**
  * Xuất đề thi ra file PDF chuẩn đẹp (Header, Watermark, Checkbox A/B/C/D chuẩn FPT).
  * Cung cấp 2 bản xuất: Đề thi trắng để phát cho lớp/nhóm làm bài, và Bản đáp án có Answer Key & lời giải chi tiết.
  *(Nhật kết thúc Khối 4 và chuyển mic cho bạn Lê Nguyễn Minh Thắng trình bày Study Planner).*

---

### 🎙️ KHỐI 5: LÊ NGUYỄN MINH THẮNG (DEV: STUDY PLANNER) — Slide 15 đến Slide 17 [~2.5 phút]
* **Slide 15: Phân Hệ AI Study Planner — Thuật Toán Xếp Lịch Ôn Thi Theo Tuần**
  * Nhập mục tiêu điểm số và ngày thi deadline $\rightarrow$ Gemini Flash Lite tính toán phân bổ lộ trình ôn tập khoa học theo từng tuần.
  * Tự động chia nhỏ khối lượng bài học vừa sức, tránh học dồn sát ngày thi.
* **Slide 16: Phân Chia Nhiệm Vụ Học Tập Vừa Sức & Cách Ly Dữ Liệu**
  * Phân rã giáo trình thành các Study Tasks cụ thể gắn liền với từng chương.
  * Đảm bảo tính riêng tư và bảo mật dữ liệu IDOR theo từng sinh viên.
* **Slide 17: Quản Lý Study Tasks & Theo Dõi Tiến Độ Trực Quan (Interactive Checklist)**
  * Checkbox đánh dấu hoàn thành từng đầu việc học tập với tương tác mượt mà.
  * Thanh tiến độ học tập (Progress Bar) phần trăm trực quan, tiếp thêm động lực học mỗi ngày.
  *(Thắng kết thúc Khối 5 và chuyển mic cho bạn Ngô Ân phụ trách phần Quản trị, Doanh thu, QA và Setup).*

---

### 🎙️ KHỐI 6: NGÔ ÂN (DEV: ADMIN COMMAND CENTER, DOANH THU VNPAY & QA) — Slide 18 đến Slide 22 [~4 phút]
* **Slide 18: Admin Command Center — Giám Sát KPI, Doanh Thu VNPay & Đối Soát Đơn Hàng**
  * Dashboard theo dõi thời gian thực: Tổng sinh viên, số môn học, số Chunks, tổng lượt gọi AI RAG.
  * **Phân hệ Báo cáo Doanh thu VNPay Sandbox**: Thống kê Tổng doanh thu (VNĐ), Doanh thu gói PRO (49.000đ), Doanh thu gói SQUAD (129.000đ), Doanh thu hôm nay, Số lượng đơn thành công.
  * **Bảng Lưu vết Đơn hàng & Lịch sử Giao dịch**: Theo dõi mã đơn `TxnRef`, tài khoản sinh viên, số tiền, phương thức ngân hàng (NCB/Visa), mã giao dịch VNPay `vnpayTransactionNo`, trạng thái `SUCCESS`.
  * **Nút xuất báo cáo tài chính CSV** (`/admin/revenue/export-csv`) chuẩn UTF-8 BOM cho Microsoft Excel và nút liên kết trực tiếp tới Cổng đối soát **VNPay Merchant Portal** (`merchantv2`).
  * Quản trị người dùng: Nâng/hạ gói cước linh hoạt, nút reset hạn ngạch RAG.
* **Slide 19: Centralized AI Key, Bảo Mật Masking & Đo Latency Ping**
  * Cơ chế Centralized System Key: Admin cấu hình 1 lần, an toàn toàn hệ thống, sinh viên không cần tự nhập key.
  * API Key Masking (`AIzaSy...****`): Che giấu khóa nhạy cảm trên giao diện khi demo/trình chiếu.
  * Tính năng đo độ trễ mạng thực tế (Latency Ping ms) trực tiếp tới Google AI API.
* **Slide 20: Báo Cáo Đảm Bảo Chất Lượng Toàn Diện (QA Report — 333 Tests PASS)**
  * Nghiệm thu **333 / 333 Unit & Integration Tests PASS (100%)** bao gồm 6 test cases phân hệ VNPay Payment & Revenue (`VnPayPaymentAndRevenueTest`).
  * Kiểm thử chịu tải đồng thời (Quota Concurrency), Kiểm thử cách ly dữ liệu (Data Isolation), Kiểm thử phân quyền RBAC và bảo vệ gian lận Leaderboard.
  * Kiểm thử toàn vẹn thanh toán: Sinh URL HMAC-SHA512, xác thực phản hồi Sandbox, cộng dồn 30 ngày hạn gói và xuất CSV đối soát.
* **Slide 21: Hướng Dẫn Cài Đặt & Chạy Dự Án (User Guide — Setup & Code)**
  * Yêu cầu môi trường: Java 17 LTS, Maven 3.8+, H2 Database file tự tạo cục bộ.
  * Cấu hình file `.env`: `GOOGLE_CLIENT_ID`, `GEMINI_API_KEY`.
  * Khởi chạy với 1 lệnh duy nhất: `mvn spring-boot:run` $\rightarrow$ Mở trình duyệt tại `http://localhost:8080`.
* **Slide 22: Kết Luận, Lời Cảm Ơn & Mời Hội Đồng Bước Vào Q&A**
  * Đạt được trọn vẹn mục tiêu của cả Assignment và Project cuối kỳ trong một sản phẩm hoàn chỉnh, sẵn sàng thương mại hóa.
  * Lời cảm ơn Thầy/Cô và mời Hội đồng Giảng viên bước vào phần Hỏi - Đáp (Q&A) cùng toàn thể 6 thành viên Nhóm 2.

---

## 🔍 PHẦN IV: CHECKLIST RÀ SOÁT LỖI GIAO DIỆN & CHỨC NĂNG (THEO ĐỀ XUẤT CUỘC HỌP NGÀY 04/11)

> **YÊU CẦU BẮT BUỘC TỪ TEAM LEAD**:
> Từng thành viên mở hệ thống đang chạy tại `http://localhost:8080`, đăng nhập tài khoản tương ứng và tự mình thực hiện toàn bộ các đầu việc kiểm tra dưới đây:

### 1️⃣ NGÔ NHẬT TRƯỜNG (PHÂN HỆ AUTH, UI TOOLKIT & PROFILE)
* [ ] **Rà soát Trang Login & Register (`/login`, `/register`)**:
  * Kiểm tra giao diện White-Card chia đôi 50/50: Không bị lệch form khi co giãn màn hình.
  * Kiểm tra các nút **"1-Click Đăng Nhập Nhanh"** (Free, Pro, Squad Leader): Bấm là tự động điền form và đăng nhập thành công.
  * Thử đăng nhập sai mật khẩu: Thông báo lỗi tiếng Việt hiển thị đẹp mắt, không làm vỡ form.
* [ ] **Rà soát Đăng nhập Google OAuth2**:
  * Bấm nút "Đăng nhập với Google": Popup mở mượt mà, xác thực tài khoản FPT/Gmail thành công và đồng bộ email, họ tên.
* [ ] **Rà soát Trang Hồ sơ Sinh viên (`/profile`)**:
  * Kiểm tra hiển thị đúng thông tin: Avatar, Họ tên, Email, Huy hiệu gói cước.
  * Kiểm tra **Thời hạn gói cước (`tierExpiresAt`)**: Hiển thị rõ số ngày còn lại (hoặc "Vô thời hạn" nếu Admin cấp).
  * Kiểm tra **Bảng Hóa đơn & Lịch sử giao dịch VNPay**: Hiển thị đúng các đơn hàng đã thanh toán, mã đơn, số tiền, trạng thái THÀNH CÔNG.
  * Kiểm tra nút **Đăng xuất (Logout)**: Đăng xuất an toàn, xóa sạch session và cookie JWT `STUDYHUB_JWT`.
* [ ] **Rà soát Layout chung (Navbar, Sidebar, Footer)**:
  * Kiểm tra giao diện đồng bộ White-Card Palette, không còn phần tử nào bị chói hoặc lệch màu.

---

### 2️⃣ LÊ THANH TÙNG (SHARED SQUAD HUB, 3 GÓI CƯỚC & FEATURE GATING)
* [ ] **Rà soát Quản lý Nhóm Study Squad**:
  * Đăng nhập `squad_leader` / `password123`: Kiểm tra hiển thị Mã mời 6 ký tự (`squadInviteCode`).
  * Dùng tài khoản `student1` nhập mã mời: Kiểm tra banner "Yêu cầu gia nhập đang chờ phê duyệt" xuất hiện đúng.
  * Trưởng nhóm bấm "Phê duyệt": Thành viên được nâng cấp ngay lên gói SQUAD và môn học `[SQUAD]` hiển thị đồng bộ trong Dashboard của cả hai.
* [ ] **Rà soát Squad Leaderboard & Đề thi Solo riêng tư**:
  * Thi trắc nghiệm môn nhóm: Kiểm tra điểm số hiển thị trên Squad Leaderboard.
  * Tạo đề thi cá nhân (chọn checkbox "Đề thi Solo riêng tư"): Làm bài xong kiểm tra điểm số tuyệt đối KHÔNG bị đẩy lên Leaderboard nhóm.
* [ ] **Rà soát Feature Gating & Modal Bảng Giá 3 Gói Cước**:
  * Dùng tài khoản Free đã có 3 môn học: Bấm "Tạo Môn Học Mới" hoặc truy cập `/planner` $\rightarrow$ Xác nhận Modal Bảng Giá SaaS (`modal-pricing-upgrade`) tự động bật lên.
  * Bấm nút "Nâng cấp gói Pro" hoặc "Nâng cấp gói Squad": Kiểm tra chuyển hướng và xử lý nâng cấp mượt mà.
* [ ] **Rà soát Nút Thanh Toán VNPay Sandbox tại Landing Page**:
  * Kiểm tra tại trang chủ (`/`) và trang pricing: Các nút "Thanh toán VNPay ATM NCB" & "Thẻ Visa" hoạt động chuẩn, hộp thông tin thẻ test 1-Click Copy hiển thị rõ ràng.

---

### 3️⃣ NGUYỄN VĂN GIA BÌNH (RAG ENGINE & AI CHATBOT — 3 LỖI P0 NGÀY 04/11)
* [ ] **Rà soát Lỗi 1: Chữ tàng hình Header Bar (`chat.html`)**:
  * Truy cập `/courses/{id}/chat`: Nút **"Quản Lý Giáo Trình & Chunks"** phải hiển thị rõ 100% chữ xám đậm `text-slate-700 font-bold` trên nền trắng `bg-white border-slate-200`, không còn bị tàng hình.
  * Các badge `AI Multi-Key Engine` và `GÓI PRO` hiển thị màu sắc trang nhã, sắc nét.
* [ ] **Rà soát Lỗi 2: Nút "+ Cuộc Trò Chuyện Mới"**:
  * Bấm nút `+ Cuộc Trò Chuyện Mới` $\rightarrow$ Hệ thống phải mở ngay khung chat trắng tinh (hoặc phiên chat mới `sessionId` mới), danh sách lịch sử bên trái xuất hiện phiên mới.
  * Khi gửi tin nhắn đầu tiên: Tiêu đề phiên chat tự động đổi tên theo nội dung câu hỏi đầu tiên.
* [ ] **Rà soát Lỗi 3: Render Markdown Table & Cú pháp AI**:
  * Đặt câu hỏi yêu cầu chatbot so sánh bằng bảng biểu (VD: *"Hãy so sánh Servlet và JSP bằng bảng"*).
  * **Kiểm tra kết quả**: Bong bóng chat của AI phải format thành bảng HTML hoàn chỉnh (có dòng kẻ ô border, có sọc zebra xen kẽ, tiêu đề in đậm), các định dạng `**in đậm**` và bullet list hiển thị chuẩn đẹp qua thư viện `marked.js`.
* [ ] **Rà soát Bóc tách Chunks & Citation Cards**:
  * Tải lên 1 file PDF giáo trình mới: Kiểm tra danh sách Chunks có phân trang và số trang chính xác.
  * Đặt câu hỏi: AI trả lời bám sát giáo trình kèm thẻ trích dẫn `📄 [Tên_file.pdf, Trang: X]` đầy đủ.

---

### 4️⃣ HUỲNH NHẬT (QUIZ ENGINE & XUẤT ĐỀ THI PDF)
* [x] **Rà soát AI Sinh Đề Thi Trắc Nghiệm (`/courses/{id}/quizzes`)**:
  * Bấm "Tạo Đề Thi Bằng AI" $\rightarrow$ Chọn mức độ (EASY / MEDIUM / HARD) và số câu: Xác nhận bộ câu hỏi sinh ra có đầy đủ 4 đáp án A/B/C/D hợp lý, câu hỏi chuẩn kiến thức giáo trình.
  * *(Đã kiểm chứng thực tế: Sinh đề thành công ID 41 từ giáo trình ISTQB Software Testing, gồm 5 câu hỏi chuẩn 4 đáp án A/B/C/D).*
* [x] **Rà soát Phòng Thi Bấm Giờ & Chấm Điểm Tự Động**:
  * Vào thi (`takeQuiz`): Kiểm tra đồng hồ đếm ngược (Countdown Timer) chạy lùi chuẩn từng giây.
  * Nộp bài: Chấm điểm thang 10 tức thì, hiển thị huy hiệu Đạt/Chưa đạt, thống kê câu đúng/sai và lời giải thích chi tiết cho từng câu.
  * *(Đã kiểm chứng: Timer 15 phút đếm lùi chuẩn 1s/1s, nộp bài chấm 4.0/10 tức thì, hiển thị đầy đủ giải thích và filter câu đúng/sai, giao diện scorecard đã được fix track 360°).*
* [x] **Rà soát Xuất Đề Thi PDF Chuẩn In Ấn (Gói Pro)**:
  * Đăng nhập tài khoản Pro/Admin, bấm nút **"Xuất Đề Thi Ra PDF"**.
  * Tải file `.pdf` về máy, mở bằng Chrome hoặc Adobe Acrobat:
    - Kiểm tra Header chuẩn FPT University, watermark, bảng câu hỏi và ô vuông checkbox A/B/C/D.
    - **Đặc biệt kiểm tra font chữ tiếng Việt**: Không bị lỗi mã hóa, không bị ô vuông hay dấu hỏi chấm `?`.
    - Kiểm tra cả 2 bản xuất: Đề thi trắng và Bản có Answer Key kèm lời giải.
  * *(Đã kiểm chứng & lưu bằng chứng: `docs/evidence/nhat/de-thi-trang-swt301.pdf` (64KB) và `de-thi-dap-an-swt301.pdf` (70KB). Font Arial Unicode tiếng Việt 100% chuẩn nét. Feature Gating chặn tài khoản Free với mã lỗi 403 Forbidden).*

---

### 5️⃣ LÊ NGUYỄN MINH THẮNG (AI STUDY PLANNER & TASKS)
* [ ] **Rà soát AI Sinh Lộ Trình Ôn Thi (`/planner`)**:
  * Đăng nhập tài khoản Pro/Admin: Nhập mục tiêu ôn thi, ngày thi deadline và số giờ học/ngày $\rightarrow$ Bấm "Sinh Lộ Trình Ôn Tập AI".
  * Kiểm tra bảng lộ trình: Gemini Flash Lite tính toán phân bổ khoa học theo các tuần (Week 1, Week 2, Week 3) không bị dồn cục bộ.
* [ ] **Rà soát Interactive Checklist & Progress Bar**:
  * Kiểm tra từng nhiệm vụ (Study Task) có gắn nhãn chương và khối lượng vừa sức.
  * Thử click vào các checkbox đánh dấu hoàn thành nhiệm vụ $\rightarrow$ Kiểm tra thanh tiến độ (Progress Bar) có tự động tăng tỷ lệ phần trăm (%) mượt mà không bị giật lag.
* [ ] **Rà soát Cách ly Dữ liệu (Data Isolation)**:
  * Đăng xuất và đăng nhập bằng sinh viên khác: Đảm bảo sinh viên này không nhìn thấy lộ trình cá nhân của sinh viên ban đầu.

---

### 6️⃣ NGÔ ÂN (ADMIN COMMAND CENTER, DOANH THU VNPAY & QA)
* [ ] **Rà soát Admin Dashboard & Thẻ KPI Doanh Thu VNPay (`/admin/dashboard`)**:
  * Đăng nhập `admin` / `password123` $\rightarrow$ Kiểm tra thẻ KPI "Doanh thu VNPay": Hiển thị tổng thu (VNĐ) và số lượng giao dịch thành công.
* [ ] **Rà soát Bảng Lịch Sử Đơn Hàng & Doanh Thu Sandbox**:
  * Kiểm tra bảng lưu vết: Hiển thị đầy đủ Mã đơn hàng nội bộ (`TxnRef`), Tài khoản sinh viên, Gói cước (PRO/SQUAD), Số tiền, Ngân hàng, Mã GD VNPay (`vnpayTransactionNo`), Trạng thái `THÀNH CÔNG` và Thời gian.
* [ ] **Rà soát Nút Xuất Báo Cáo Tài Chính CSV (`/admin/revenue/export-csv`)**:
  * Bấm nút **"Xuất CSV Đối Soát"** $\rightarrow$ Tải file `vnpay-transactions-report.csv` về máy.
  * Mở file bằng Microsoft Excel trên Windows: Xác nhận tiếng Việt Unicode UTF-8 BOM hiển thị chuẩn 100%, không bị vỡ font, đầy đủ 8 cột dữ liệu.
* [ ] **Rà soát Cổng Đối Soát VNPay Merchant Portal**:
  * Bấm nút **"Portal VNPay"** (hoặc mở `https://sandbox.vnpayment.vn/merchantv2/`) $\rightarrow$ Đăng nhập tài khoản `thanhtung22012006@gmail.com`: Đối chiếu mã GD VNPay trên StudyHub khớp chính xác 1-1 với máy chủ VNPay Sandbox!
* [ ] **Rà soát Centralized Key Masking & Ping Test (`/admin/settings`)**:
  * Kiểm tra API Key hiển thị dạng Masking an toàn: `AIzaSy...****`.
  * Bấm nút **"Ping Test"**: Ghi nhận độ trễ ms thực tế từ máy chủ Google Gemini.
* [ ] **Rà soát Bộ Kiểm Thử Tự Động (QA Suite)**:
  * Mở Terminal chạy lệnh: `mvn test` $\rightarrow$ Xác nhận **333 / 333 Tests PASS (100%)** không một lỗi nào.

---

## 💳 PHẦN V: KẾ HOẠCH GIAO TASK PHÂN HỆ QUẢN TRỊ DOANH THU VNPAY CHO ÂN

> **Người thực hiện**: Ngô Ân (Dev phụ trách Admin Command Center, Doanh Thu VNPay & QA)  
> **Nhiệm vụ chính**: Làm chủ toàn bộ kiến trúc cổng thanh toán VNPay Sandbox, giải thuật tính toán doanh thu, xuất báo cáo đối soát CSV và trực tiếp demo tính năng này trước Hội đồng tại Slide 18.

### 1. BẢNG THÔNG TIN CẤU HÌNH & THẺ TEST VNPAY SANDBOX CHÍNH THỨC
* **Mã Website (`vnp_TmnCode`)**: `AYM70LAS`
* **Chuỗi bí mật (`vnp_HashSecret`)**: `NDJFXTYRTNJRRTHQNDDBUVGDXPTYPFMY`
* **Payment Gateway URL**: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`
* **Return URL**: `http://localhost:8080/payment/vnpay-return`
* **Cổng Quản trị Merchant Portal**: `https://sandbox.vnpayment.vn/merchantv2/` (Tài khoản: `thanhtung22012006@gmail.com`)

#### 💳 Bộ thẻ test phục vụ Demo trực tiếp:
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

### 2. KỊCH BẢN NÓI DEMO DOANH THU CỦA ÂN (TẠI SLIDE 18)
Khi Tùng chuyển mic cho Ân, Ân mở màn hình Admin Dashboard (`/admin/dashboard`) và trình bày dõng dạc:

> *"Thưa quý Thầy Cô, bên cạnh việc giám sát các chỉ số kỹ thuật như số Chunks tài liệu hay lượt gọi AI RAG, một sản phẩm SaaS thương mại hoàn chỉnh bắt buộc phải có phân hệ **Quản trị Tài chính và Cổng Thanh Toán Trực Tuyến**.*  
>  
> *Nhóm 2 chúng em đã tích hợp thành công cổng thanh toán **VNPay Sandbox 2.1.0** với mã kết nối chính thức `AYM70LAS`. Ngay trên màn hình **Admin Command Center**:*  
> 1. *Thẻ KPI **Doanh thu VNPay** tổng hợp tức thời: Hệ thống tự động phân tách doanh thu theo gói Pro 49.000đ, gói Squad 129.000đ và tổng thu trong ngày.*  
> 2. *Bảng **Lịch Sử Đơn Hàng**: Lưu vết minh bạch từng giao dịch, bao gồm mã đơn hàng nội bộ `TxnRef`, tài khoản sinh viên, số tiền, ngân hàng thanh toán và đặc biệt là **Mã giao dịch đối soát của VNPay**.*  
> 3. *Em đã trang bị nút bấm **Xuất Báo Cáo Tài Chính CSV** chuẩn UTF-8 BOM mở ngay trên Microsoft Excel không bị lỗi font tiếng Việt.*  
> 4. *Để chứng minh tính minh bạch, em xin phép mở **Cổng Quản Trị VNPay Merchant Portal** tại `sandbox.vnpayment.vn/merchantv2` — toàn bộ mã giao dịch trên StudyHub đều khớp chính xác 1-1 với máy chủ thanh toán của VNPay!*  
>  
> *Toàn bộ phân hệ thanh toán và hệ thống đã vượt qua trọn vẹn **333 bài kiểm thử tự động**, sẵn sàng vận hành thực chiến!"*

---

## ⏰ PHẦN VI: KẾ HOẠCH TỔNG DUYỆT DEMO TỐI NAY (07/11/2026)

* **19:30 - 20:00**: Từng bạn hoàn thành checklist rà soát lỗi giao diện và chức năng của phân hệ mình. Chụp lại ảnh màn hình thực tế sắc nét chèn vào slide của mình.
* **20:00 - 20:30**: Họp Discord toàn nhóm, chạy tổng duyệt thử nghiệm (Dry Run) toàn bộ 22 Slides theo đúng thứ tự mới:
  * Trường (Slide 1 - 4) $\rightarrow$ Tùng (Slide 5 - 8) $\rightarrow$ Bình (Slide 9 - 11) $\rightarrow$ Nhật (Slide 12 - 14) $\rightarrow$ Thắng (Slide 15 - 17) $\rightarrow$ Ân (Slide 18 - 22).
  * Canh đúng thời lượng: Tổng thời gian nói toàn nhóm từ 18 đến 20 phút (chuẩn format FPT).
* **20:30 trở đi**: Đóng băng mã nguồn (Code Freeze), xuất file Slide PDF và chuẩn bị tinh thần tốt nhất cho ngày bảo vệ chính thức!

Chúc toàn thể Nhóm 2 chúng ta tự tin, phối hợp nhịp nhàng và giành trọn điểm A+ xuất sắc! 🚀
