# 🚀 KẾ HOẠCH PHÂN CHIA NHIỆM VỤ NÂNG CẤP GIAO DIỆN & KIỂM THỬ HỒI QUY (02/11 - 03/11/2026)
> **Chủ trì**: Lê Thanh Tùng (Team Lead)  
> **Dự án**: Smart StudyHub RAG - Smart Multi-Course Study Hub & AI Courseware Assistant  
> **Mục tiêu**: Đồng bộ toàn bộ giao diện sang **White-Card Palette & Progressive Disclosure**, bảo toàn 100% chức năng qua **Regression Testing**, **ISOLATION 100%** chống xung đột.

---

## 📌 1. NGUYÊN TẮC LÀM VIỆC & KỶ LUẬT TRÁCH NHIỆM

### 1.1. Trách nhiệm cá nhân cao nhất (Individual Accountability)
* **Team Lead Tùng không thể và không có trách nhiệm phải vi mô kiểm duyệt từng dòng code hay từng pixel** của cả 5 thành viên.
* **Từng thành viên phải chịu trách nhiệm tuyệt đối 100% về chất lượng, độ chính xác và tính toàn vẹn** của phân hệ mình phụ trách trước khi bàn giao.
* Tự giác rà soát: kiểm tra giao diện không vỡ layout, không tràn chữ trên các độ phân giải màn hình, tự chạy regression test toàn bộ chức năng cũ trước khi nộp.

### 1.2. Kỷ luật về Nút mới / Tính năng mới (Rules on New Features & AI Hallucination)
* **Mục tiêu cốt lõi**: Nghiêm cấm việc AI tự ý bịa thêm (hallucinate) các nút bấm thừa, chức năng lạ không theo kiểm soát của con người.
* **Quy trình khi thành viên muốn bổ sung nút mới / chức năng mới**:
  - Thành viên hoàn toàn được quyền sáng tạo thêm tính năng nếu thấy cần thiết để tối ưu trải nghiệm (UX) hoặc phục vụ nghiệp vụ tốt hơn.
  - **NHƯNG BẮT BUỘC PHẢI TUÂN THỦ QUY TRÌNH**:
    1. Bổ sung mô tả tính năng/nút mới đó vào **Spec / Business Rules** của module mình.
    2. Tạo một Issue mới trên bảng [`docs/issues/ISSUES-TRACKING.md`](../issues/ISSUES-TRACKING.md) ghi rõ lý do và phạm vi ảnh hưởng.
    3. Thông báo cho Team Lead Tùng để nắm bắt và đồng bộ toàn dự án.

### 1.3. Nguyên lý Phân cấp độ thông tin (Progressive Disclosure)
* **Cấp 1 (Màn hình ngoài - Overview)**: Trình bày thoáng đãng, chỉ hiển thị số liệu cốt lõi, thanh tìm kiếm, thẻ tóm tắt và nút điều hướng chính. **Tuyệt đối không hiển thị các thông tin rác, chuỗi ID kỹ thuật, hoặc khối văn bản thô dài dằng dặc làm rối mắt**.
* **Cấp 2 (Màn hình trong - Deep Dive)**: Chỉ khi sinh viên bấm vào xem chi tiết một môn, một đề thi, một slide hay một lộ trình thì mới hiển thị nội dung chuyên sâu (sử dụng Tabs, Accordion hoặc Modal xem trước).

### 1.4. Nguyên tắc Cô lập hoàn toàn (100% File Isolation)
* Mỗi thành viên chỉ thao tác độc quyền trên danh sách file template được phân công.
* **ĐÓNG BĂNG HOÀN TOÀN các file dùng chung**: `src/main/resources/templates/layout/*` và `fragments/*` do Team Lead Tùng độc quyền quản lý. Không ai được phép tự ý sửa các file này để tránh 100% xung đột Merge Conflict!

---

### 1.5. Quy tắc Phân trang (Pagination) & Chống tràn dữ liệu lớn
* **Vấn đề thực tế**: Khi hệ thống có 100, 500 hay 1.000 sinh viên, hàng chục slide và hàng trăm chunks, **TUYỆT ĐỐI KHÔNG ĐƯỢC ĐỔ HẾT DỮ LIỆU RA MÀN HÌNH CÙNG MỘT LÚC**. Việc show hết ra ngoài sẽ làm trình duyệt bị đơ lag, giao diện dài ngoằng vô tận và trải nghiệm tồi tệ.
* **Yêu cầu bắt buộc**:
  - **Phân trang (Pagination) hoặc Giới hạn hiển thị (Page Size 10-15 mục)**: Mọi danh sách lớn (Danh sách sinh viên ở Admin, danh sách đề thi ở Quiz, danh sách lộ trình ở Planner) bắt buộc phải có phân trang hoặc hiển thị tối đa 10-15 mục gần nhất kèm nút xem tiếp/chuyển trang.
  - **Bộ lọc thông minh (Smart Filters)**: Kết hợp ô tìm kiếm và dropdown/tabs lọc nhanh để thu hẹp phạm vi dữ liệu trước khi hiển thị.
  - **Phân tầng dữ liệu (Progressive Disclosure)**:
    * Cấp 1 (Ngoài): Chỉ hiển thị các thông tin cốt lõi nhất để nhận diện (Ví dụ: Avatar, Tên, Gói cước, Ngày tạo).
    * Cấp 2 (Trong): Chi tiết lịch sử, metadata kỹ thuật, danh sách chunks chỉ được hiển thị khi người dùng chủ động click vào (dùng Accordion, Modal hoặc Drawer).
  - **Triệt tiêu Text rác & Số liệu thừa**: Tuyệt đối **KHÔNG hiển thị các thông tin mà người dùng không cần** như: ID kỹ thuật (`id=99281`), chuỗi ngày giờ có mili-giây thô (`2026-11-02T15:24:11.129384Z`), các nhãn rườm rà dài dòng. Mọi câu chữ phải ngắn gọn, súc tích và có giá trị hành động!

### 1.6. Chuẩn hóa tư duy Quản trị (Admin System-Only Mental Model)
* **Nguyên tắc nghiệp vụ**: **Admin là người vận hành và giám sát kỹ thuật, ADMIN KHÔNG HỌC VÀ KHÔNG PHẢI SINH VIÊN!**
  - **Đã xóa bỏ hoàn toàn nút "Tạo Môn Mẫu Hệ Thống"** tại `index.html`. Admin không có nhu cầu tạo môn để học hay ôn thi.
  - Trang Admin chỉ tập trung vào: **Sức khỏe hệ thống, Quản lý tài khoản sinh viên (Nâng/Hạ gói, Reset Quota), Tải lượng RAG và Cấu hình Gemini API Key**.
  - Admin không cần và không nên duyệt danh sách môn học cá nhân như một sinh viên.

---

## 🎨 2. QUY CHUẨN DESIGN SYSTEM CHUẨN (ĐÃ LÀM MẪU TẠI COURSE HUB)

Tất cả các trang khi làm mới phải tuân theo đúng bảng mã màu và phong cách chuẩn sau:
* **Thẻ nội dung (White-Card Palette)**:
  `bg-white/95 backdrop-blur-xl border border-slate-200/80 text-slate-800 shadow-sm hover:shadow-md hover:border-indigo-300 transition-all rounded-2xl p-5`
* **Nút bấm chính (Brand Gradient Button)**:
  `btn btn-sm h-10 px-5 bg-gradient-to-r from-purple-600 via-violet-600 to-pink-600 hover:from-purple-500 hover:to-pink-500 border-none text-white rounded-xl font-bold shadow-lg shadow-purple-500/25 transition-all`
* **Nút phụ / Quay lại (Soft Button)**:
  `btn btn-sm h-10 px-4 bg-slate-100 hover:bg-slate-200 border border-slate-200 text-slate-700 rounded-xl font-semibold transition-all`
* **Tiêu đề chính**: `font-black text-slate-900 tracking-tight text-xl md:text-2xl`
* **Văn bản giải thích**: `text-xs text-slate-500 leading-relaxed`
* **Bảng biểu dữ liệu (Clean Table)**:
  Nền trắng bo tròn `rounded-2xl border border-slate-200 overflow-hidden shadow-sm`; Thead xám nhẹ `bg-slate-50 text-slate-600 font-bold uppercase text-xs`; Hàng dữ liệu `border-b border-slate-100 hover:bg-slate-50/70`.

---

## 👥 3. PHÂN CHIA NHIỆM VỤ CHI TIẾT CHO TỪNG THÀNH VIÊN (02/11 - 03/11/2026)

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                   MA TRẬN PHÂN CÔNG 6 PHÂN HỆ — CÔ LẬP HOÀN TOÀN (ISOLATED)             │
│                                                                                        │
│  [1. TÙNG]   ──►  courses/detail.html + Layout Frame (Đã làm mẫu White-Card)           │
│  [2. TRƯỜNG] ──►  auth/profile.html (login & register Tùng đã làm đẹp, GIỮ NGUYÊN)    │
│  [3. BÌNH]   ──►  rag/documents.html, rag/chat.html                                    │
│  [4. THẮNG]  ──►  planner/index.html, planner/view.html                                │
│  [5. NHẬT]   ──►  quiz/index.html, quiz/take.html, quiz/result.html                    │
│  [6. ÂN]     ──►  Fix E2E-ADM-06 + admin/dashboard.html, users.html, settings.html     │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

### 👑 THÀNH VIÊN 1: LÊ THANH TÙNG (TEAM LEAD)
* **Phân hệ**: **Core Module, Hub Trung Tâm & Quản Trị Khung Layout**
* **File phụ trách độc quyền**:
  - `src/main/resources/templates/courses/detail.html` *(Đã hoàn thiện làm mẫu)*
  - `src/main/resources/templates/layout/*` & `fragments/*` *(Độc quyền kiểm soát)*
* **Kế hoạch nhiệm vụ chi tiết**:
  - [x] **TASK-TUNG-01**: Cải tạo `courses/detail.html` sang White-Card Palette (4 cards phân hệ, kho chunks accordion trắng, gradient tím hồng).
  - [x] **TASK-TUNG-02**: Đóng băng và duy trì tính ổn định của navbar, sidebar và footer (chuẩn hóa Admin không còn link tạo môn / học tập).
  - [x] **TASK-TUNG-03**: Điều phối nhánh Git, giải quyết các thắc mắc kiến trúc, hỗ trợ thành viên và nghiệm thu bộ minh chứng 03/11.

---

### 👤 THÀNH VIÊN 2: NGÔ NHẬT TRƯỜNG
* **Phân hệ**: **Authentication, Profile & Quản Lý Nhóm Study Squad**
* **File phụ trách độc quyền (ISOLATED)**:
  - `src/main/resources/templates/auth/profile.html`
  - *(Lưu ý: Hai trang `auth/login.html` và `auth/register.html` Team Lead Tùng đã thiết kế chuẩn mực 50/50 Split cực đẹp, **GIỮ NGUYÊN 100%, Trường TUYỆT ĐỐI KHÔNG sửa lại để tránh hỏng layout**).*
* **Kế hoạch nhiệm vụ chi tiết (02/11 - 03/11)**:
  - [ ] **TASK-TRUONG-01 (Cải Tạo Giao Diện Profile & Study Squad - White-Card Palette)**:
    + Áp dụng **Progressive Disclosure**: Chia trang cá nhân `profile.html` thành 3 khối thẻ trắng (`bg-white/95 border-slate-200/80 text-slate-800 shadow-sm`) độc lập, không nhồi nhét dồn cục:
      1. **Thẻ Thông Tin Cá Nhân & Avatar Presets**: Xem họ tên, username, email, chọn avatar preset mượt mà.
      2. **Thẻ Bảo Mật & Đổi Mật Khẩu BCrypt**: Form đổi mật khẩu gọn gàng, các trường nhập có viền sáng thanh lịch.
      3. **Thẻ Quản Lý Nhóm Study Squad**: Huy hiệu Trưởng nhóm / Thành viên với gradient tím hồng (`from-purple-600 via-violet-600 to-pink-600`); danh sách thành viên dạng Clean Table, nút xóa bạn tinh tế; tự động ẩn với gói PRO độc lập.
    + **Lưu ý đặc biệt**: Đặt toàn bộ thẻ `<script>` xử lý avatar bên trong thẻ `<section>` để Thymeleaf layout decorator không nuốt mất script.
  - [ ] **TASK-TRUONG-02 (Regression Checklist - Tự Kiểm Thử Trước Khi Nộp)**:
    + [ ] Đăng nhập form thường thành công.
    + [ ] Bấm thử cả 4 nút 1-Click Login tự điền và submit mượt mà.
    + [ ] Đăng ký tài khoản mới tự gán gói FREE.
    + [ ] Đổi họ tên và bấm chọn avatar preset cập nhật ngay.
    + [ ] Đổi mật khẩu cũ/mới kiểm tra mã hóa BCrypt.
    + [ ] Mời thành viên vào squad (tối đa 5 bạn), kiểm tra disabled ô nhập khi đủ 5.
    + [ ] Xóa thành viên khỏi nhóm quay về FREE.

---

### 📄 THÀNH VIÊN 3: BÌNH
* **Phân hệ**: **Quản Lý Slide PDF, Trích Xuất Chunks & RAG AI Chatbot**
* **File phụ trách độc quyền (ISOLATED)**:
  - `src/main/resources/templates/rag/documents.html`
  - `src/main/resources/templates/rag/chat.html`
* **Kế hoạch nhiệm vụ chi tiết (02/11 - 03/11)**:
  - [x] **TASK-BINH-01 (Documents & Chunks)**:
    + Áp dụng **Progressive Disclosure**:
      * **Cấp 1**: Khu vực Dropzone kéo thả tải file PDF tinh tế, kèm bảng danh sách các tài liệu đã nạp (tên file, số trang, kích thước, ngày tải).
      * **Cấp 2**: Các đoạn chunks trích xuất **KHÔNG được bung hết ra trang** làm tràn màn hình. Phải đưa vào Accordion đóng/mở hoặc Modal xem trước *"Xem chi tiết Chunks"*.
      * Ẩn các thông số kỹ thuật rác, chỉ hiển thị số thứ tự đoạn, số từ và nút xóa tài liệu.
  - [x] **TASK-BINH-02 (RAG Chatbot Frame)**:
    + Khung chat thiết kế hiện đại, thoáng đãng:
      * Bong bóng tin nhắn người dùng: Nền gradient tím hồng `from-purple-600 via-violet-600 to-pink-600` chữ trắng.
      * Bong bóng trả lời AI: Thẻ trắng `bg-white/95 border-slate-200/80 text-slate-800`.
      * **Citation Cards đối chứng giáo trình**: Làm nổi bật số trang bằng badge màu chàm (indigo) và trích dẫn trực tiếp đoạn văn bản gốc (0% ảo giác).
      * Banner hiển thị Hạn ngạch RAG 50 câu/ngày: Thu nhỏ gọn gàng ở góc trên khung chat, có progress bar mỏng, không chiếm diện tích chat.
  - [x] **TASK-BINH-03 (Regression Checklist)**:
    + [x] Tải lên file PDF thành công, cắt chunks tự động.
    + [x] Kiểm tra hạn mức 3 PDF của tài khoản FREE (bị chặn khi nạp file thứ 4).
    + [x] Kiểm tra tài khoản PRO tải PDF không giới hạn.
    + [x] Xóa tài liệu PDF dọn sạch chunks trong database.
    + [x] Hỏi đáp RAG trả lời bám sát giáo trình, có thẻ Citation số trang.
    + [x] Vượt trần 50 câu RAG/ngày hiện banner chặn và nút nâng cấp.

---

### 🗓️ THÀNH VIÊN 4: THẮNG
* **Phân hệ**: **AI Study Planner, Lộ Trình Học Cá Nhân Hóa & Checklist Tuần**
* **File phụ trách độc quyền (ISOLATED)**:
  - `src/main/resources/templates/planner/index.html`
  - `src/main/resources/templates/planner/view.html`
* **Kế hoạch nhiệm vụ chi tiết (02/11 - 03/11)**:
  - [ ] **TASK-THANG-01 (Planner Hub - Cấp 1)**:
    + Thiết kế form cấu hình AI Planner dạng White-Card sang trọng: ô nhập mục tiêu điểm số (A/B/C), số giờ học/tuần, ngày thi với nút bấm gradient.
    + Danh sách các lộ trình ôn thi cũ theo môn học hiển thị dạng thẻ card tóm tắt (tên lộ trình, môn học, ngày tạo, thanh % tiến độ tổng quát). Bấm vào thẻ mới chuyển hướng sang Cấp 2.
  - [ ] **TASK-THANG-02 (Chi Tiết Lộ Trình Tuần - Cấp 2)**:
    + Áp dụng **Progressive Disclosure**: Lộ trình ôn thi nhiều tuần **KHÔNG kéo dài một dải vô tận**. Phân chia các tuần học theo dạng **Tabs tuần (Tuần 1, Tuần 2, Tuần 3...)** hoặc **Accordion tuần**.
    + Checklist nhiệm vụ học tập: Checkbox tương tác bo góc, khi bấm tick có hiệu ứng gạch ngang chữ mượt mà, thanh tiến độ % hoàn thành tự động cập nhật realtime.
  - [ ] **TASK-THANG-03 (Regression Checklist)**:
    + [ ] Sinh viên gói FREE bấm tạo lộ trình $\rightarrow$ Bị chặn, bật modal nâng cấp PRO.
    + [ ] Sinh viên gói PRO tạo lộ trình AI thành công theo đúng mục tiêu.
    + [ ] Hiển thị đầy đủ danh sách tuần và các nhiệm vụ tương ứng.
    + [ ] Bấm tick/untick checkbox cập nhật % tiến độ chính xác.
    + [ ] Kiểm tra xem lại lịch sử lộ trình theo môn học.
    + [ ] Gõ URL xem lộ trình của sinh viên khác bị chặn 403 (Bảo vệ IDOR).

---

### 🧠 THÀNH VIÊN 5: HUỲNH NHẬT
* **Phân hệ**: **AI Quiz Generator, Phòng Thi Trắc Nghiệm & Squad Leaderboard**
* **File phụ trách độc quyền (ISOLATED)**:
  - `src/main/resources/templates/quiz/index.html`
  - `src/main/resources/templates/quiz/take.html`
  - `src/main/resources/templates/quiz/result.html`
  - *(Giữ nguyên `quiz/pdf-template.html` đã chuẩn in ấn OpenHtmlToPdf)*
* **Kế hoạch nhiệm vụ chi tiết (02/11 - 03/11)**:
  - [ ] **TASK-NHAT-01 (Quiz Hub & Squad Leaderboard - Cấp 1)**:
    + Card cấu hình sinh đề thi AI (chọn slide tài liệu, độ khó DỄ/VỪA/KHÓ, số câu hỏi, thời gian thi) thiết kế dạng White-Card hiện đại.
    + **Bảng xếp hạng nhóm (Squad Leaderboard)**: Đưa lên phong cách sáng sạch sẽ, top 1/2/3 gắn huy hiệu Vàng/Bạc/Đồng nổi bật, điểm số và thời gian làm bài rõ ràng.
    + Danh sách đề thi đã tạo hiển thị dạng thẻ card gọn gàng, kèm nút "Vào phòng thi".
  - [ ] **TASK-NHAT-02 (Phòng Thi Split-view - Cấp 2)**:
    + Tút lại phòng thi trắc nghiệm chia 2 cột:
      * Cột trái: Ma trận câu hỏi (grid 1..10) đổi màu khi đã chọn đáp án, đồng hồ đếm ngược nổi bật.
      * Cột phải: Câu hỏi hiện tại to rõ, 4 đáp án A/B/C/D dạng nút bấm lớn dễ chọn trên cả mobile và desktop.
  - [ ] **TASK-NHAT-03 (Kết Quả Thi & Xuất PDF - Cấp 2)**:
    + Card điểm số to rõ, biểu đồ tròn hoặc thanh tiến độ trực quan.
    + Danh sách câu hỏi sau thi: Câu đúng hiển thị viền xanh lá, câu sai hiển thị viền đỏ kèm giải thích chi tiết nguồn gốc kiến thức.
    + Nút xuất file PDF tiếng Việt đồng bộ gradient tím hồng.
  - [ ] **TASK-NHAT-04 (Regression Checklist)**:
    + [ ] Sinh đề thi trắc nghiệm AI 10 câu 4 đáp án từ slide thành công.
    + [ ] Vào phòng thi Split-view, chọn đáp án lưu trạng thái.
    + [ ] Đồng hồ đếm ngược chạy chuẩn, hết giờ tự động nộp bài.
    + [ ] Chấm điểm tức thì, câu đúng xanh / câu sai đỏ kèm giải thích.
    + [ ] Bảng xếp hạng nhóm cập nhật điểm số thành viên.
    + [ ] Sinh viên FREE bấm xuất PDF bị từ chối 403.
    + [ ] Sinh viên PRO xuất PDF tiếng Việt chuẩn có Watermark và bảng Answer Key.

---

### 🛡️ THÀNH VIÊN 6: ÂN
* **Báo cáo local 03/11**: [Kết quả, ảnh và giới hạn](../evidence/2026-11-03-ui-redesign/an/README.md). AN-02/03/04 đã triển khai; AN-01 và dòng Ping thật của AN-05 vẫn **BLOCKED** (chưa có Gemini key hợp lệ). 307/307 test tự động PASS; 5/6 luồng E2E PASS, không coi mock là kết nối thật. Không sửa layout/fragments hoặc ảnh cũ; chưa push.
* **Phân hệ**: **Admin Command Center, Quản Lý Sinh Viên & Gemini API Key**
* **File phụ trách độc quyền (ISOLATED)**:
  - `src/main/resources/templates/admin/dashboard.html`
  - `src/main/resources/templates/admin/users.html`
  - `src/main/resources/templates/admin/settings.html`
  - `src/main/resources/templates/admin/error.html`
* **Kế hoạch nhiệm vụ chi tiết (02/11 - 03/11)**:
  - [ ] **TASK-AN-01 (ƯU TIÊN SỐ 1 — SỬA LỖI & HOÀN THÀNH E2E-ADM-06)**:
    + Cấu hình API Key Google Gemini hợp lệ vào môi trường máy test cá nhân hoặc nhập trực tiếp qua trang `/admin/settings` rồi bấm "Lưu cấu hình".
    + Bấm nút **"Kiểm tra kết nối tức thì"** $\rightarrow$ Hệ thống hiện thông báo xanh: *"Kết nối Google Gemini thành công! (Thời gian phản hồi: xxx ms)"*.
    + Theo chỉ đạo người dùng ngày 03/11: giữ nguyên ảnh 02/11; khi có kết quả thành công, lưu ảnh mới tại [`docs/evidence/2026-11-03-ui-redesign/an/E2E-ADM-06_an.png`](../evidence/2026-11-03-ui-redesign/an/E2E-ADM-06_an.png) và cập nhật báo cáo đóng case `BLOCKED` $\rightarrow$ `PASS 100%`.
  - [x] **TASK-AN-02 (Admin Dashboard - Chuẩn Hóa Tư Duy Vận Hành Kỹ Thuật)**:
    + 4 thẻ KPI thống kê (Tổng tài khoản sinh viên, Tổng môn học toàn hệ thống, Quota RAG hôm nay, Trạng thái AI Model) chuyển sang White-Card Palette sang trọng.
    + Biểu đồ tỷ lệ 3 gói cước (FREE, PRO, SQUAD) hiển thị trực quan, tính toán tỷ lệ trả phí chuẩn xác.
    + **XÓA BỎ HOÀN TOÀN nút/liên kết "Giám sát môn học" vào `/courses`**: Admin là quản trị viên hệ thống, không phải sinh viên, không học tập, không cần duyệt danh sách môn học chi tiết như người dùng học tập.
  - [x] **TASK-AN-03 (Quản Lý Sinh Viên - Phân Trang & Chống Tràn 1.000 Sinh Viên)**:
    + **BẮT BUỘC BỔ SUNG PHÂN TRANG (Pagination)**: Xử lý kịch bản 100 - 1.000 sinh viên, chỉ hiển thị **10 hoặc 15 sinh viên / trang** kèm thanh phân trang (`Trang trước`, `Trang 1, 2, 3...`, `Trang sau`), không được đổ hàng nghìn dòng ra DOM làm đơ lag trình duyệt.
    + **Áp dụng Progressive Disclosure & Clean Data**:
      * Thanh công cụ tìm kiếm và lọc gói cước (FREE, PRO, SQUAD) gọn gàng trên 1 hàng.
      * Bảng sinh viên dạng Clean Table: Avatar bo tròn, họ tên, email, badge gói cước rõ màu, quota RAG.
      * **Triệt tiêu text rác & metadata thừa**: Ẩn các ID kỹ thuật không cần thiết, định dạng ngày hết hạn gói cước chuẩn `dd/MM/yyyy` gọn gàng.
      * Nút thao tác Nâng cấp PRO, Kích hoạt SQUAD, Hạ về FREE, Reset RAG thiết kế nhỏ gọn dạng nút bấm thanh lịch hoặc dropdown hành động để không làm rối mắt bảng dữ liệu.
  - [x] **TASK-AN-04 (Settings & Error Page)**:
    + Tút lại trang `/admin/settings` cấu hình System Gemini Key với khung che giấu `AIzaSy...****` sang trọng.
    + Tút lại trang `/admin/error.html` báo lỗi 403 Forbidden lịch sự, rõ ràng cho sinh viên đi lạc.
  - [ ] **TASK-AN-05 (Regression Checklist)**:
    + [x] Đăng nhập `student` truy cập `/admin/dashboard` bị chặn 403 Forbidden.
    + [x] Đăng nhập `admin` vào dashboard hiển thị đủ số liệu thống kê.
    + [x] Lọc sinh viên theo từng gói FREE, PRO, SQUAD và tìm kiếm theo tên chuẩn xác.
    + [x] Nâng/hạ gói cước cho sinh viên cập nhật tức thì.
    + [x] Bấm Reset RAG Quota đưa số lượt trong ngày của sinh viên về 0.
    + [ ] Test Ping kết nối Google Gemini key thật thành công hiển thị xanh và latency.

---

---

## 🛠️ 5. HƯỚNG DẪN HẬU THI CÔNG & TỰ KIỂM THỬ CHO MỌI THÀNH VIÊN (POST-EXECUTION PROTOCOL)

> **Mỗi thành viên sau khi AI tạo hoặc chỉnh sửa giao diện xong BẮT BUỘC phải thực hiện đủ 4 bước dưới đây trước khi bàn giao cho Team Lead Tùng:**

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        QUY TRÌNH 4 BƯỚC HẬU KIỂM TRA BẮT BUỘC                          │
│                                                                                        │
│  [BƯỚC 1: KIỂM TRA LAYOUT] ──► [BƯỚC 2: PHÂN TRẦNG & TEXT] ──► [BƯỚC 3: REGRESSION]  │
│         (F12 Responsive)              (Pagination, Không rác)         (Click & Form)   │
│                                              │                                         │
│                                              ▼                                         │
│                              [BƯỚC 4: BÁO CÁO & NỘP ẢNH MINH CHỨNG]                    │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

### 📱 Bước 1 — Rà soát lỗi vỡ layout & Hiển thị đa thiết bị (Visual & Responsive Audit)
1. Mở trình duyệt, bật công cụ kiểm tra (F12) xem trang ở các kích thước:
   - Màn hình Desktop lớn (1920x1080).
   - Màn hình Laptop thông thường (1366x768).
   - Màn hình Tablet & Mobile (375px - 768px).
2. **Kiểm tra kỹ**:
   - Có cột nào bị tràn viền ngang (horizontal scrollbar không mong muốn) hay không?
   - Các đoạn văn bản dài có làm vỡ khung thẻ không? (Nếu có phải áp dụng `truncate` hoặc `line-clamp`).
   - Các icon, nút bấm có bị méo mó, lệch hàng hay đè lên chữ không? Nếu xấu phải sửa CSS ngay!

### 🧱 Bước 2 — Rà soát Phân tầng dữ liệu & Loại bỏ thông tin thừa (Information Cleanliness Audit)
1. **Kiểm tra danh sách lớn**: Nếu trang có danh sách (sinh viên, đề thi, chunks, slide), kiểm tra xem đã có **phân trang (10-15 mục/trang)** hoặc **accordion thu gọn** chưa? Tuyệt đối không để danh sách trải dài hàng nghìn dòng.
2. **Kiểm tra thông tin hiển thị**:
   - Đã xóa sạch các chuỗi ID kỹ thuật dài dòng chưa?
   - Định dạng ngày tháng đã chuyển sang dạng thân thiện `dd/MM/yyyy HH:mm` chưa (không để lộ ISO timestamp thô)?
   - Có dòng text hướng dẫn nào bị thừa thãi, rườm rà không cần thiết không? Nếu có hãy cắt bỏ.

### 🧪 Bước 3 — Kiểm thử hồi quy chức năng tại trang (Functional Regression Testing)
1. **Tương tác nút bấm & Liên kết**: Bấm thử TẤT CẢ các nút bấm trên trang xem có hoạt động đúng không.
2. **Form & Nhập liệu**:
   - Thử gửi form với dữ liệu chuẩn $
ightarrow$ Phải nhận được thông báo thành công xanh.
   - Thử gửi form rỗng hoặc nhập sai $
ightarrow$ Phải hiện thông báo lỗi validation rõ ràng, không được crash 500.
3. **Modal & Dialog**: Mở thử modal bằng nút bấm và đóng modal bằng nút X/Hủy xem có mượt mà không.
4. **Bảo toàn chức năng**: Đối chiếu với bảng chức năng cũ để chắc chắn không bị mất logic nào.

### 📸 Bước 4 — Báo cáo & Nộp minh chứng hoàn thành theo Thư mục Ngày (Date-based Evidence)
> **Quy định tổ chức thư mục bằng chứng (Evidence Organization)**:
> Để không bị lẫn lộn ảnh kiểm thử cũ ngày 02/11 và ảnh giao diện mới ngày 03/11, **toàn bộ ảnh chụp màn hình mới BẮT BUỘC phải nộp vào thư mục con theo ngày:**
> `docs/evidence/2026-11-03-ui-redesign/[tên_thành_viên]/`
> - `tung/`: `docs/evidence/2026-11-03-ui-redesign/tung/`
> - `truong/`: `docs/evidence/2026-11-03-ui-redesign/truong/`
> - `binh/`: `docs/evidence/2026-11-03-ui-redesign/binh/`
> - `thang/`: `docs/evidence/2026-11-03-ui-redesign/thang/`
> - `nhat/`: `docs/evidence/2026-11-03-ui-redesign/nhat/`
> - `an/`: `docs/evidence/2026-11-03-ui-redesign/an/`

1. Chụp ảnh màn hình giao diện mới hoàn chỉnh (rõ nét, đúng kích thước).
1. Chụp ảnh màn hình giao diện mới hoàn chỉnh (rõ nét, đúng kích thước).
2. Tích chọn đầy đủ các mục trong **Regression Checklist** của mình trong file kế hoạch này.
3. Nếu có chủ động bổ sung nút mới/tính năng mới $
ightarrow$ Xác nhận đã cập nhật Spec và mở Issue trên [`docs/issues/ISSUES-TRACKING.md`](../issues/ISSUES-TRACKING.md).
4. Báo cáo Team Lead Tùng: *"Em đã hoàn thành refactor giao diện và tự regression test 100% đạt chuẩn, sẵn sàng để Lead merge!"*

## 🚦 6. QUY TRÌNH KIỂM DUYỆT VÀ NGHIỆM THU TẠI CHỖ

1. **Thành viên tự nghiệm thu tại máy**: Từng bạn phải tự chạy toàn bộ các mục trong **Regression Checklist** của mình. Nếu có bất kỳ lỗi vỡ layout hoặc hỏng chức năng, phải tự sửa ngay.
2. **Nộp minh chứng hoàn thành**:
   - Chụp ảnh giao diện mới sau khi làm đẹp.
   - Nếu có phát sinh thêm nút bấm hoặc logic mới $\rightarrow$ Bắt buộc đã bổ sung vào Spec và mở Issue trên [`docs/issues/ISSUES-TRACKING.md`](../issues/ISSUES-TRACKING.md).
3. **Team Lead Tùng chốt merge**: Team Lead Tùng kiểm tra nhanh branch của thành viên, xác nhận không vi phạm file dùng chung (`layout/*`, `fragments/*`) và tiến hành gộp vào nhánh chính.
