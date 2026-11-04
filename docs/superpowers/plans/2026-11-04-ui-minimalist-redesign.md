# Kế Hoạch Tái Thiết Kế Giao Diện Tối Giản, Gọn Gàng & Tối Ưu Responsive

> **Tài liệu tham khảo & Minh chứng:**
> - Kế hoạch này áp dụng cho 4 trang web chính:
>   1. `/courses`: **Danh sách Môn Học & Không Gian Học Tập AI**
>   2. `/courses/{id}`: **Chi Tiết Môn Học**
>   3. `/courses/{id}/documents`: **Tài Liệu Giáo Trình & Cắt Chunks RAG**
>   4. `/courses/{id}/chat`: **AI Chatbot Giáo Trình & Multi-Session**
> - Ảnh chụp hiện trạng (Before) đã được tự động ghi lại bằng Playwright tại `docs/evidence/ui-audit-before/`.

---

## 1. Mục Tiêu & Định Hướng Thiết Kế (Goal Description)

### Hiện trạng thực tế qua kiểm tra Playwright:
1. **Thông tin thừa thãi, nặng tính kỹ thuật nội bộ:** Các banner "khoe" thông số như *"Apache PDFBox 3.0.3"*, *"Engine trích xuất"*, *"Phương thức học tập BYOC tự do mọi trường"*, *"HSF302 Assignment • Smart StudyHub Platform"*... chiếm diện tích lớn, gây phân tâm và làm rối mắt người dùng sinh viên.
2. **Khối Hero Banner quá to (Quá tải chiều dọc):** Khối nền đen gradient chiếm từ 30% - 50% màn hình đầu tiên, khiến sinh viên phải cuộn trang mới thấy danh sách môn học hoặc tài liệu.
3. **Chưa tối ưu Responsive trên Mobile/Tablet (390px - 768px):** 
   - Trên mobile, các thẻ thống kê xếp dọc thành 4 hàng dài chiếm trọn 2 màn hình.
   - Bảng tài liệu và dropzone tải PDF bị co rúm, cuộn ngang khó khăn.
   - Giao diện Chat chưa có cơ chế toggle mở/thu gọn Sidebar lịch sử hội thoại trên di động, làm mất không gian của khung trò chuyện.

### Triết lý thiết kế mới (Minimalist & Modern Clean Card):
- **Gọn gàng & Tối giản (YAGNI & Content-First):** Đưa nội dung học tập làm trọng tâm, loại bỏ toàn bộ text trang trí, badge rườm rà và các thông số kỹ thuật không cần thiết.
- **Bảng màu White-Card / Soft Palette:** Nền canvas sáng (`bg-slate-50/70`), thẻ trắng cao cấp (`bg-white border-slate-200/80 shadow-sm`), điểm xuyết gradient tinh tế làm điểm nhấn (accent).
- **Responsive hoàn hảo (Mobile-First):** Co giãn linh hoạt từ điện thoại (390px), tablet (768px) đến màn hình rộng (1440px). Tích hợp Drawer/Off-canvas Sidebar cho phòng chat trên thiết bị di động.

---

## 2. Điểm Cần Người Dùng Duyệt (User Review Required)

> [!IMPORTANT]
> **Loại bỏ các thông tin thừa thãi:**
> 1. Xóa bỏ thẻ *"Phương Thức Học Tập BYOC"* tại trang `/courses` (gộp 3 chỉ số thiết yếu: Môn học, Hạn ngạch RAG hôm nay, Gói cước vào thanh Compact Stats Bar).
> 2. Xóa bỏ badge *"PDFBox 3.0.3"* và *"Tiêu chuẩn Chunk 300-500 từ"* tại trang `/courses/{id}/documents` (thay bằng thanh Document Header tinh tế).
> 3. Tinh gọn Hero Header tại `/courses` và `/courses/{id}`: Giảm chiều cao từ ~400px xuống ~140px, văn bản súc tích, các nút hành động đặt ngang hàng công thái học.

> [!NOTE]
> Tất cả các tính năng nghiệp vụ, logic backend, bảo mật phân quyền gói cước (FREE/PRO/SQUAD), cơ chế kiểm soát quota 50 câu/ngày và 330 unit tests hiện tại vẫn **được bảo toàn 100%**.

---

## 3. Kế Hoạch Sửa Đổi Chi Tiết Theo Từng Trang (Proposed Changes)

### Phân Hệ 1: Danh Sách Môn Học (`src/main/resources/templates/index.html`)
- **Hero Banner:** Thu nhỏ kích thước padding (`p-5 sm:p-7`), chuyển sang tone Dark Slate tinh tế hoặc Light Glass Card hiện đại, loại bỏ ambient cyber glow quá khổ, giữ lại tiêu đề súc tích và cụm nút chính: *"Tạo Môn Học Mới"*, *"Bảng Giá Gói Cước"*.
- **Quick Stats Bar:**
  - Loại bỏ hoàn toàn card thứ 4 (BYOC thừa thãi).
  - Thu gọn 3 card còn lại (Môn học, Quota RAG, Gói cước) thành **Thanh chỉ số ngang (Horizontal Metric Strip)** nhỏ gọn, tiết kiệm 70% không gian dọc.
- **Thanh tìm kiếm & Lọc môn học:** Thiết kế inline gọn gàng, ô input bo tròn mềm mại với icon kính lúp, loại bỏ các nút dư thừa.
- **Danh sách Course Cards:**
  - Thiết kế dạng Bento Grid (`grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5`).
  - Card bo tròn `rounded-2xl`, viền `border-slate-200`, hiệu ứng hover nhẹ nhàng.
  - Phân bổ 3 nút CTA trực quan dưới chân card: *"Vào Môn"*, *"Chat AI"*, *"Tài Liệu"*.
- **Mobile Responsive:**
  - 1 cột mượt mà, không bị cuộn ngang.
  - Nút bấm to tối thiểu 44px đạt chuẩn touch target trên điện thoại.

---

### Phân Hệ 2: Chi Tiết Môn Học (`src/main/resources/templates/courses/detail.html`)
- **Breadcrumbs & Course Header:**
  - Thay thế khối banner cồng kềnh bằng **Course Profile Header** gọn gàng: Icon môn học (48x48px), Mã môn (badge tím), Tên môn học rõ nét, Tín chỉ, Học kỳ và Badge trạng thái *"Sẵn sàng hỏi đáp AI"*.
  - Nút *"Chỉnh sửa"* và *"Xóa môn"* thu gọn thành các nút icon action tinh tế ở góc phải.
- **4 Feature Gateway Hub Cards:**
  - Thu gọn kích thước card từ dạng khối lớn sang dạng **Action Tile hiện đại** (Icon sinh động + Tiêu đề + 1 dòng mô tả ngắn + Nút truy cập nhanh).
  - 4 phân hệ được chia lưới 4 cột trên Desktop, 2 cột trên Tablet, 1 cột trên Mobile.
- **Tab Nội Dung Môn Học & Hoạt Động Gần Đây:**
  - Bố trí 2 cột: Cột trái (70%) là Tài liệu giáo trình & Đề thi gần đây; Cột phải (30%) là Thông số tóm tắt của môn học.

---

### Phân Hệ 3: Quản Lý Tài Liệu & Cắt Chunks (`src/main/resources/templates/rag/documents.html`)
- **Header:**
  - Loại bỏ các card thống kê thừa *"PDFBox 3.0.3"*, *"Tiêu chuẩn Chunk 300-500 từ"*.
  - Thay bằng thanh Header tinh gọn hiển thị: Số tài liệu hiện có (kèm hạn ngạch gói FREE/PRO), Tổng số Chunks đã tạo, và nút chuyển nhanh sang Khung Chat.
- **Khối Tải File (Dropzone Upload):**
  - Thu gọn giao diện kéo thả file PDF thành khu vực Upload nhỏ gọn, hiện đại với đường viền nét đứt thanh mảnh (`border-2 border-dashed border-indigo-200`).
  - Lược bớt text hướng dẫn rườm rà.
- **Bảng Tài Liệu Đã Nạp (Clean Document Table):**
  - Thiết kế bảng phẳng (Flat Modern Table), viền kẻ nhẹ, phân biệt rõ: Tên file PDF, Kích thước, Số trang, Số Chunks đã cắt, Trạng thái `INDEXED`, Nút xem Chunks và Nút xóa.
- **Khối Xem Chi Tiết Chunks:**
  - Trình bày danh sách Chunks dưới dạng các card văn bản sạch sẽ (accordion hoặc scroll container tối đa 400px), hiển thị số trang gốc `[Trang: X]` rõ nét.
- **Mobile Responsive:**
  - Tự động chuyển Dropzone lên trên và Bảng tài liệu xuống dưới dạng Card List trên màn hình nhỏ.

---

### Phân Hệ 4: AI Chatbot Giáo Trình (`src/main/resources/templates/rag/chat.html`)
- **Header Bar:**
  - Giữ phong cách White-Card đã sửa lỗi ở P0, làm sạch các khoảng cách thừa.
  - Thêm nút Toggle Drawer trên mobile (`<button id="toggle-sidebar">`) để đóng/mở sidebar lịch sử hội thoại trên màn hình nhỏ.
- **Sidebar Lịch Sử Trò Chuyện:**
  - Thu gọn kích thước cố định, danh sách các phiên chat hiển thị tiêu đề gọn gàng với icon thùng rác mờ khi hover.
  - Thẻ đếm Hạn ngạch RAG (43/50) thu gọn thành một thanh Progress Bar tinh tế.
- **Khung Chat & Bong Bóng Tin Nhắn:**
  - Tin nhắn Người dùng: Bo góc mềm mại, nền tím gradient nhẹ nhàng hoặc Dark Slate sang trọng.
  - Tin nhắn AI: Nền trắng sạch sẽ, viền xám siêu nhẹ, tích hợp thẻ Citation Cards nằm ngang (Horizontal Pills) tiết kiệm diện tích.
  - Bảng biểu Markdown kẻ ô chuẩn chỉ (`marked.js`).
- **Thanh Nhập Liệu (Input Box):**
  - Thiết kế dạng Floating Rounded Bar bám đáy (sticky), chiều cao vừa vặn, nút Gửi nổi bật.

---

## 4. Kế Hoạch Kiểm Chứng (Verification Plan)

### A. Kiểm thử Tự Động với Playwright (Automated Verification)
Tạo script `e2e/capture-audit-after.js` và `e2e/verify-responsive.js`:
1. Chụp lại toàn bộ 4 trang ở cả 2 kích thước Viewport:
   - **Desktop:** `1440 x 900`
   - **Mobile (iPhone 14 / Standard Mobile):** `390 x 844`
2. Lưu kết quả vào thư mục `docs/evidence/ui-audit-after/`.
3. Kiểm tra tính năng tương tác chính:
   - Thử tìm kiếm môn học tại `/courses`.
   - Thử bấm vào các nút điều hướng Gateway tại `/courses/1`.
   - Thử mở accordion Chunks tại `/courses/1/documents`.
   - Thử gửi tin nhắn và bấm "Làm sạch đoạn chat" tại `/courses/1/chat`.

### B. Kiểm thử Hồi Quy Hệ Thống (Automated Unit Tests)
Chạy lệnh kiểm thử toàn diện:
```powershell
mvn clean test
```
**Tiêu chí thành công:** Toàn bộ **330/330 test cases PASS 100%**, không xảy ra bất kỳ lỗi logic nào.

