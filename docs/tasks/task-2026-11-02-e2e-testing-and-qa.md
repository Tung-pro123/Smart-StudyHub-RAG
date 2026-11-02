# 📋 BẢNG PHÂN CÔNG NHIỆM VỤ KIỂM THỬ E2E TOÀN DIỆN & HƯỚNG DẪN HỌC KIẾN TRÚC HỆ THỐNG
> **Ngày triển khai**: 02/11/2026  
> **Dự án**: Smart StudyHub RAG - Nền tảng Tự học Cá nhân hóa Đa môn học & Trợ lý AI Giáo trình  
> **Học phần**: HSF302 - Java Web Application & Spring Framework (FPT University)  
> **Đội ngũ thực hiện**: Nhóm 2 (Group 2)  
> **Nhánh Git chung**: `main`  
> **Tài liệu theo dõi lỗi**: [`docs/issues/ISSUES-TRACKING.md`](../issues/ISSUES-TRACKING.md)  
> **Thư mục lưu ảnh minh chứng**: `docs/evidence/[tên_thành_viên]/` (Phân chia theo từng thư mục thành viên: `tung/`, `truong/`, `binh/`, `thanh/`, `nhat/`, `an/`)

---

## 🎯 1. MỤC TIÊU & NGUYÊN TẮC KIỂM THỬ E2E (END-TO-END)

1. **Mục tiêu**: Rà soát 100% chức năng, toàn bộ trang web, URL endpoints, nút bấm, modal tương tác, các luồng thành công (happy path) và các trường hợp bảo mật/hạn ngạch (edge/adversarial path) thuộc 3 gói cước (**FREE**, **PRO**, **SQUAD**) và vai trò quản trị (**ADMIN**).
2. **Nguyên tắc phân công (Zero Conflict)**:
   - Mỗi thành viên phụ trách **1 phân hệ chuyên biệt tương ứng đúng với các module mình đã code**.
   - Kiểm thử độc lập trên **các tài khoản demo được cấp riêng**, không giẫm chân lên dữ liệu của nhau (nhờ cơ chế Data Isolation theo `user_id`).
3. **Bắt buộc chụp ảnh màn hình (Screenshot Evidence)**:
   - Mỗi test case hoàn thành **bắt buộc phải có 1 ảnh chụp màn hình rõ nét** minh chứng kết quả.
   - Đặt tên file ảnh chuẩn: `[Mã_Test]_[Tên_Thành_Viên].png` (Ví dụ: `E2E-CORE-01_tung.png`, `E2E-PLAN-02_thanh.png`).
   - Lưu toàn bộ ảnh vào đúng thư mục thành viên: `docs/evidence/[tên_thành_viên]/` (ví dụ: `docs/evidence/tung/E2E-CORE-01_tung.png`) và commit lên repo.
4. **Quy trình khi phát hiện lỗi**:
   - Nếu phát hiện lỗi (nút bấm không chạy, vỡ layout, logic không chặn đúng hạn ngạch): **Mở ngay file [`docs/issues/ISSUES-TRACKING.md`](../issues/ISSUES-TRACKING.md)**, điền thông tin theo mẫu issue để cùng fix ngay.

---

## 💡 2. MẸO KIỂM THỬ HẠN NGẠCH NHANH (FAST TESTING HACKS)
> [!TIP]
> **Làm thế nào để test hạn ngạch mà không mất hàng giờ thao tác thủ công?**

* **Mẹo 1: Test Hạn ngạch 50 câu RAG/ngày của Gói Free**:
  - Không cần ngồi hỏi đủ 50 câu! Bạn có 2 cách cực nhanh:
    - **Cách A (Khuyên dùng)**: Mở file `src/main/java/com/group2/assignment/rag/service/LlmKeyRotator.java` (hoặc Controller), tạm thời chỉnh `FREE_DAILY_RAG_LIMIT = 3;` (thay vì 50). Dùng tài khoản `student` chat 3 câu, đến câu thứ 4 hệ thống hiện thông báo đỏ chặn lại ngay! *(Test xong nhớ sửa lại 50)*.
    - **Cách B (Dùng H2 Console)**: Truy cập `http://localhost:8080/h2-console`, chạy câu lệnh SQL:
      ```sql
      UPDATE users SET daily_rag_count = 49 WHERE username = 'student';
      ```
      Sau đó vào web chat đúng 1 câu nữa là đạt 50/50, câu tiếp theo sẽ bị chặn ngay!
* **Mẹo 2: Test Hạn ngạch 3 môn học của Gói Free**:
  - Tài khoản `student` đã có sẵn **2 môn học** (`HSF302`, `MAS291`).
  - Bấm "Tạo môn học mới" thêm 1 môn thứ 3 (`SWP391`) -> Tạo thành công.
  - Bấm tiếp tạo môn thứ 4 (`PRJ301`) -> Hệ thống chặn lại, hiển thị popup bảng giá nâng cấp Pro.
  - Xóa 1 môn cũ đi -> Nút tạo mở lại bình thường.
* **Mẹo 3: Test Hạn ngạch 5 thành viên của Gói Study Squad**:
  - Tài khoản `squad_leader` đã có 3 bạn trong nhóm (`squad_leader`, `squad_member1`, `squad_member2`).
  - Nhập `@student2` mời thêm -> Lên 4 bạn.
  - Mời tiếp 1 bạn nữa cho đủ 5 bạn. Khi nhóm đủ 5 bạn, ô nhập mời thành viên tự động disable và báo *"Nhóm đã đủ tối đa 5 thành viên"*.
* **Mẹo 4: Đăng nhập nhanh 1-Click**:
  - Tại trang `http://localhost:8080/login`, bên dưới form có sẵn các huy hiệu:
    `[FREE: student]`, `[PRO: pro_student]`, `[SQUAD: squad_leader]`, `[ADMIN: admin]`.
  - Chỉ cần bấm vào huy hiệu là hệ thống tự điền username và password `123456`, bấm Enter là vào ngay!

---

## 📖 3. HƯỚNG DẪN HỌC & HIỂU SÂU KIẾN TRÚC (ĐỂ BẢO VỆ ĐỒ ÁN ĐẠT ĐIỂM A+)

1. **Kiến trúc Package-by-Feature (Theo tính năng)**:
   - Toàn bộ codebase chia thành 6 package tính năng độc lập, dễ mở rộng:
     + `core`: Quản lý môn học, Course Hub, trang chủ, cách ly dữ liệu.
     + `auth`: Đăng ký, đăng nhập session, cập nhật hồ sơ, quản lý nhóm Study Squad.
     + `rag`: Trích xuất PDF, cắt nhỏ chunks, embedding vector, chatbot giáo trình có trích dẫn nguồn.
     + `planner`: Lập kế hoạch học tập cá nhân hóa AI, phân chia nhiệm vụ tuần, theo dõi tiến độ.
     + `quiz`: Sinh đề trắc nghiệm AI từ slide, phòng thi đếm ngược, chấm điểm, Squad Leaderboard, xuất PDF.
     + `settings`: Trung tâm quản trị Admin Command Center, cấu hình API key, thống kê 3 gói cước.
2. **Cơ chế Phân lập Dữ liệu (Data Isolation) bằng `user_id`**:
   - `Course.java` liên kết `@ManyToOne User user`.
   - Khi truy vấn môn học, `CourseRepository` lọc `WHERE c.user.id = :userId`.
   - Sinh viên `student` và `pro_student` tự do tạo môn học cùng mã `HSF302` mà không trùng lặp và không nhìn thấy dữ liệu của nhau. Admin có quyền xem tất cả.
3. **Cơ chế RAG AI 0% Ảo giác (Retrieval-Augmented Generation)**:
   - Slide PDF được băm nhỏ thành các đoạn chunk (~1000 ký tự có 200 ký tự overlap).
   - Khi sinh viên hỏi, hệ thống chuyển câu hỏi thành vector, so khớp Cosine Similarity với chunks trên Database để lấy 3 đoạn tài liệu sát nhất.
   - Prompt ép buộc: *"Chỉ trả lời dựa trên trích dẫn tài liệu đính kèm, bắt buộc kèm số trang đối chứng"*.
4. **Cơ chế Feature Gating 3 Gói Cước**:
   - Mọi Service đều kiểm tra quyền hạn qua `User.java`:
     + `user.isPro()`: Trả về `true` nếu là `PRO`, `SQUAD` hoặc `ADMIN`.
     + `user.isSquad()`: Trả về `true` nếu là `SQUAD`.
     + `user.isSquadLeader()`: Trả về `true` nếu là chủ nhóm Study Squad.
   - Nếu gói Free cố tình truy cập tính năng Pro (như xuất PDF quiz, mở Study Planner, tạo môn thứ 4), Backend chặn lại ngay lập tức.
5. **Cơ sở dữ liệu H2 Nhúng & Quản lý bằng H2 Console**:
   - Database lưu file tại `./data/studyhub.mv.db` với `AUTO_SERVER=TRUE`.
   - Truy cập: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/studyhub;AUTO_SERVER=TRUE`, User: `sa`, Pass để trống).

---

## 👥 4. BẢNG PHÂN CHIA NHIỆM VỤ TEST E2E CHI TIẾT (6 THÀNH VIÊN)

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                        SƠ ĐỒ PHÂN BỔ NHIỆM VỤ KIỂM THỬ E2E                           │
├─────────────────┬───────────────────────────────┬────────────────────────────────────┤
│ Thành viên      │ Phân hệ phụ trách             │ Tài khoản Test chính               │
├─────────────────┼───────────────────────────────┼────────────────────────────────────┤
│ 👑 TÙNG (Lead)  │ Core, Search, Isolation & Hub │ student (Free), pro_student (Pro)  │
│ 👤 TRƯỜNG       │ Auth, Register, Squad & Prof  │ squad_leader, student2, pro_student│
│ 📄 BÌNH         │ PDF Docs, Chunks & RAG AI Chat│ student (Free), pro_student (Pro)  │
│ 🗓️ THÀNH        │ AI Study Planner & Task Check │ student (Free), pro_student (Pro)  │
│ 🧠 NHẬT         │ AI Quiz, Board & PDF Export   │ squad_leader, member1, pro_student │
│ 🛡️ AN           │ Admin Command Center & AI Key │ admin (Admin), student (Chặn 403)  │
└─────────────────┴───────────────────────────────┴────────────────────────────────────┘
```

---

### 👑 1. TÙNG (LEAD) - CORE, TÌM KIẾM TỰ DO, DATA ISOLATION, HẠN MỨC 3 MÔN & ROUTING [✅ 100% HOÀN THÀNH (7/7 TEST CASES)]
* **Tài khoản sử dụng**: `student` (Free), `pro_student` (Pro), Khách vãng lai
* **URL kiểm thử**: `http://localhost:8080/`, `http://localhost:8080/courses`, `http://localhost:8080/courses/{id}`
* **Tiến độ**: Đã hoàn thành 7/7 Test Cases & nộp đủ 7 ảnh minh chứng chuẩn tại `docs/evidence/tung/`
* **Danh sách Test Cases**:
  1. `E2E-CORE-01`: **Tìm kiếm môn học theo từ khóa tự do** [✅ Hoàn thành]:
     - Nhập từ khóa `"Spring"` -> Kết quả lọc ra đúng môn `HSF302`.
     - Nhập từ khóa không tồn tại `"XYZ999"` -> Hiển thị thông báo *"Không tìm thấy môn học nào phù hợp"*.
  2. `E2E-CORE-02`: **Data Isolation (Phân lập dữ liệu môn học cá nhân)** [✅ Hoàn thành]:
     - Đăng nhập `student`: Chỉ thấy môn của `student` (`HSF302`, `MAS291`). Tuyệt đối không thấy môn của `pro_student`.
     - Đăng nhập `pro_student`: Chỉ thấy các môn của `pro_student`. Tuyệt đối không lẫn dữ liệu.
  3. `E2E-CORE-03`: **Kiểm soát hạn mức 3 môn học gói Free** [✅ Hoàn thành]:
     - `student` đang có 2 môn, tạo thêm môn thứ 3 (`SWP391`) -> Thành công.
     - Tiếp tục tạo môn thứ 4 (`PRJ301`) -> Hệ thống chặn lại, hiển thị thông báo đạt giới hạn 3 môn và mở popup bảng giá.
     - Xóa 1 môn cũ -> Nút tạo môn mở lại bình thường.
  4. `E2E-CORE-04`: **Gói Pro tạo môn học không giới hạn** [✅ Hoàn thành]:
     - Đăng nhập `pro_student`, tạo thêm môn học thứ 8 -> Tạo thành công mượt mà, không bị chặn.
  5. `E2E-CORE-05`: **Bảo vệ quyền sở hữu (Ownership Protection)** [✅ Hoàn thành]:
     - Đăng nhập `student`, cố tình nhập URL xóa môn của `pro_student` (`/courses/delete/9`) -> Hệ thống chặn lại báo lỗi quyền sở hữu: *"⛔ Bạn không có quyền xóa môn học của người khác!"*.
  6. `E2E-CORE-06`: **Course Hub - Trang Chi Tiết Môn Học (`courses/detail.html`)** [✅ Hoàn thành]:
     - Bấm vào 1 môn học -> Hiển thị Course Hub với đầy đủ: Thống kê số chunks, số tài liệu, thông tin môn học, nút sửa môn, nút sang mục Tài liệu và nút sang Chat AI. (Đã tạo `TASK-UI-01` giao Bình & Trường đồng bộ màu sắc).
  7. `E2E-CORE-07`: **Landing Page & Điều Hướng Session (Routing)** [✅ Hoàn thành]:
     - Chưa đăng nhập truy cập `http://localhost:8080/` -> Hiển thị Landing Page (Hero banner, Bảng giá 3 gói cước, FAQ Accordion mở/đóng).
     - Đã đăng nhập Sinh viên truy cập `/` -> Tự động chuyển hướng vào `/courses`.
     - Đã đăng nhập Admin truy cập `/` -> Tự động chuyển hướng vào `/admin/dashboard`.
* **Bằng chứng đã nộp**: 7 ảnh chụp màn hình (`E2E-CORE-01_tung.png` đến `E2E-CORE-07_tung.png`) lưu tại [`docs/evidence/tung/`](../evidence/tung/).

---

### 👤 2. TRƯỜNG - AUTHENTICATION, REGISTRATION, PROFILE & QUẢN LÝ NHÓM SQUAD
* **Tài khoản sử dụng**: `squad_leader`, `student2`, `squad_member1`, `pro_student`
* **URL kiểm thử**: `http://localhost:8080/login`, `http://localhost:8080/register`, `http://localhost:8080/profile`
* **Tiến độ**: **ĐÃ HOÀN TẤT THỰC NGHIỆM & NỘP ĐẦY ĐỦ 9 ẢNH MINH CHỨNG**
* **Danh sách Test Cases**:
  1. `E2E-AUTH-01`: **Đăng nhập 1-Click tự động**:
     - Tại `/login`, bấm vào huy hiệu `[SQUAD: squad_leader]` -> Ghi nhận lỗi script ngoài section.
     - **Trạng thái**: `⚠️ ISSUE-02` | **Ảnh minh chứng**: [`E2E-AUTH-01_truong.png`](../evidence/truong/E2E-AUTH-01_truong.png)
  2. `E2E-AUTH-02`: **Đăng ký tài khoản mới (`/register`)**:
     - Đăng ký user mới `test_user_01` -> Hệ thống tự động gán gói FREE -> Chuyển sang màn hình login và đăng nhập thành công.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-AUTH-02_truong.png`](../evidence/truong/E2E-AUTH-02_truong.png)
  3. `E2E-AUTH-03`: **Cập nhật họ tên & Chọn Avatar Presets**:
     - Tại `/profile`, đổi họ tên thành công, ghi nhận lỗi avatar chưa đổi do script ngoài section.
     - **Trạng thái**: `⚠️ ISSUE-03` | **Ảnh minh chứng**: [`E2E-AUTH-03_truong.png`](../evidence/truong/E2E-AUTH-03_truong.png)
  4. `E2E-AUTH-04`: **Đổi mật khẩu bảo mật BCrypt**:
     - Nhập đúng mật khẩu cũ `123456`, mật khẩu mới `654321` -> Đổi thành công, đăng nhập lại thành công.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-AUTH-04_truong.png`](../evidence/truong/E2E-AUTH-04_truong.png)
  5. `E2E-AUTH-05`: **Trưởng nhóm mời bạn học vào Study Squad**:
     - Đăng nhập `squad_leader` vào `/profile`, nhập username `student2` vào ô Mời -> Thêm thành công, danh sách tăng lên 4 bạn.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-AUTH-05_truong.png`](../evidence/truong/E2E-AUTH-05_truong.png)
  6. `E2E-AUTH-06`: **Chặn thêm thành viên khi nhóm đã đủ 5 bạn**:
     - Mời tiếp cho đủ 5/5 bạn -> Ô nhập bị disable và hiển thị thông báo *"Nhóm đã đủ tối đa 5 thành viên"*.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-AUTH-06_truong.png`](../evidence/truong/E2E-AUTH-06_truong.png)
  7. `E2E-AUTH-07`: **Xóa thành viên khỏi nhóm**:
     - Bấm nút "Xóa" cạnh `student2` -> Confirm popup -> `student2` bị xóa khỏi nhóm, quay về gói Free.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-AUTH-07_truong.png`](../evidence/truong/E2E-AUTH-07_truong.png)
  8. `E2E-AUTH-08`: **Thành viên nhóm hưởng quyền lợi Pro & Kiểm tra không lỗi @null**:
     - Subtask 1: Đăng nhập `squad_member1` vào `/profile` -> Kiểm tra thông báo: *"Bạn đang là thành viên trong Nhóm của @squad_leader"*, tuyệt đối KHÔNG có `@null`.
     - Subtask 2: Đăng nhập `pro_student` vào `/profile` -> Card Quản lý nhóm Study Squad biến mất hoàn toàn, chỉ hiển thị quyền lợi Pro cá nhân.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-AUTH-08_truong.png`](../evidence/truong/E2E-AUTH-08_truong.png) & [`E2E-AUTH-08.1_truong.png`](../evidence/truong/E2E-AUTH-08.1_truong.png)
* **Bằng chứng đã nộp**: Đầy đủ 9 ảnh chụp màn hình trong thư mục [`docs/evidence/truong/`](../evidence/truong/).

---

### 📄 3. BÌNH - QUẢN LÝ TÀI LIỆU PDF, CẮT CHUNKS, RAG CHATBOT & QUOTA RAG
* **Tài khoản sử dụng**: `student` (Free) và `pro_student` (Pro)
* **URL kiểm thử**: `http://localhost:8080/courses/{id}/documents`, `http://localhost:8080/courses/{id}/chat`
* **Tiến độ**: **7/7 Test Cases ĐÃ HOÀN THÀNH (100% PASS)**
* **Danh sách Test Cases**:
  1. `E2E-RAG-01`: **Tải lên tài liệu PDF môn học**:
     - Vào môn học `HSF302`, tải file slide PDF lên -> Hệ thống xử lý trích xuất văn bản và hiển thị tài liệu trong danh sách.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-RAG-01_binh.png`](../evidence/binh/E2E-RAG-01_binh.png)
  2. `E2E-RAG-02`: **Cắt nhỏ Chunks & Trực quan hóa đoạn trích**:
     - Kiểm tra danh sách Chunks được cắt nhỏ: hiển thị rõ số trang, đoạn văn bản trích dẫn.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-RAG-02_binh.png`](../evidence/binh/E2E-RAG-02_binh.png)
  3. `E2E-RAG-03`: **Xóa tài liệu PDF & Dọn sạch Chunks liên quan**:
     - Bấm nút Xóa tài liệu -> Tài liệu và toàn bộ các chunks liên quan được xóa sạch khỏi Database.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-RAG-03_binh.png`](../evidence/binh/E2E-RAG-03_binh.png)
  4. `E2E-RAG-04`: **Chatbot RAG Giáo trình có Citation số trang đối chứng**:
     - Hỏi AI một câu hỏi có trong slide -> AI trả lời chi tiết và đính kèm **Citation Card: Nguồn file PDF + Trang đối chứng cụ thể (0% ảo giác)**.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-RAG-04_binh.png`](../evidence/binh/E2E-RAG-04_binh.png)
  5. `E2E-RAG-05`: **Kiểm soát hạn mức 3 tài liệu PDF ở Gói Free**:
     - Tài khoản `student` tải đủ 3 tài liệu PDF vào môn học.
     - Cố tình tải tài liệu thứ 4 -> Hệ thống chặn lại báo lỗi hạn ngạch 3 tài liệu/môn của Gói Free.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-RAG-05_binh.png`](../evidence/binh/E2E-RAG-05_binh.png)
  6. `E2E-RAG-06`: **Gói Pro tải tài liệu PDF không giới hạn**:
     - Đăng nhập `pro_student`, tải liên tiếp 4-5 tài liệu PDF vào môn học -> Tải lên thành công mượt mà.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-RAG-06_binh.png`](../evidence/binh/E2E-RAG-06_binh.png)
  7. `E2E-RAG-07`: **Kiểm tra Hạn ngạch RAG 50 câu/ngày của Gói Free**:
     - Áp dụng Mẹo 1 (sửa limit về 3 hoặc update SQL `daily_rag_count = 50`).
     - Chat vượt hạn ngạch -> Hệ thống chặn lại báo lỗi đạt giới hạn 50 câu/ngày và gợi ý nâng cấp Pro.
     - **Trạng thái**: `✅ PASS` | **Ảnh minh chứng**: [`E2E-RAG-07_binh.png`](../evidence/binh/E2E-RAG-07_binh.png)
  8. `E2E-RAG-08`: **Tạo phiên chat mới & Xóa lịch sử trò chuyện**:
     - Bấm nút "Cuộc trò chuyện mới" (`/chat/new`) -> Mở phiên mới. Bấm "Xóa lịch sử" (`/chat/clear`) -> Dọn dẹp sạch sẽ phiên chat.
* **Bằng chứng đã nộp**: Đầy đủ 7 ảnh chụp màn hình minh chứng chất lượng cao trong thư mục [`docs/evidence/`](../evidence/).

---

### 🗓️ 4. THÀNH - AI STUDY PLANNER, LỘ TRÌNH ÔN THI & CHECKLIST NHIỆM VỤ TUẦN
* **Tài khoản sử dụng**: `student` (Free) và `pro_student` (Pro)
* **URL kiểm thử**: `http://localhost:8080/planner`, `http://localhost:8080/planner/{planId}`
* **Danh sách Test Cases**:
  1. `E2E-PLAN-01`: **Feature Gating - Chặn sinh viên Free dùng AI Study Planner**:
     - Đăng nhập `student` (Free), truy cập `/planner`.
     - Bấm nút "Tạo Kế Hoạch Ôn Thi" -> Hệ thống chặn lại, hiển thị thông báo: *"AI Study Planner là tính năng của Gói Pro. Nâng cấp để mở khóa lộ trình ôn thi cá nhân hóa!"* và bật Modal Bảng giá.
  2. `E2E-PLAN-02`: **Gói Pro khởi tạo lộ trình ôn thi cá nhân hóa bằng AI**:
     - Đăng nhập `pro_student` (Pro), truy cập `/planner`.
     - Chọn môn học (`FER202`), thiết lập 8 giờ học/tuần, chọn ngày thi mục tiêu -> Bấm "Tạo Kế Hoạch Bằng AI" -> AI sinh lộ trình thành công và chuyển sang màn hình `/planner/{planId}`.
  3. `E2E-PLAN-03`: **Giao diện Lộ Trình Học Tập Chi Tiết (`planner/view.html`)**:
     - Kiểm tra giao diện kế hoạch: Tên lộ trình, môn học, số tuần phân bổ, danh sách các đầu việc/nhiệm vụ cụ thể theo từng tuần.
  4. `E2E-PLAN-04`: **Đánh dấu hoàn thành nhiệm vụ (Interactive Task Toggle)**:
     - Bấm tick vào checkbox một nhiệm vụ (`StudyTask`) -> Gọi API `/planner/tasks/{taskId}/toggle` cập nhật thành công, thanh tiến độ (Progress Bar) tự động cập nhật phần trăm.
  5. `E2E-PLAN-05`: **Xem lịch sử lộ trình theo từng môn học (`/planner/history/{courseId}`)**:
     - Lọc xem các kế hoạch cũ đã tạo theo từng môn học, dữ liệu hiển thị chính xác.
  6. `E2E-PLAN-06`: **Bảo vệ quyền riêng tư lộ trình học tập**:
     - Đăng nhập `student`, cố tình nhập URL xem kế hoạch của `pro_student` (ví dụ `/planner/1`) -> Hệ thống chặn lại báo lỗi *"Không tìm thấy kế hoạch hoặc không thuộc tài khoản của bạn"*.
* **Bằng chứng cần nộp**: 6 ảnh chụp màn hình (`E2E-PLAN-01_thanh.png` đến `E2E-PLAN-06_thanh.png`).

---

### 🧠 5. NHẬT - AI QUIZ TRẮC NGHIỆM, PHÒNG THI, SQUAD LEADERBOARD & XUẤT PDF
* **Tài khoản sử dụng**: `squad_leader`, `squad_member1`, `student` (Free), `pro_student` (Pro)
* **URL kiểm thử**: `http://localhost:8080/quiz`, `http://localhost:8080/quiz/{id}`, `http://localhost:8080/quiz/attempt/{id}`
* **Danh sách Test Cases**:
  1. `E2E-QUIZ-01`: **Sinh đề thi trắc nghiệm AI từ tài liệu môn học**:
     - Tại `/quiz`, chọn môn học, chọn độ khó (EASY, MEDIUM, HARD), bấm "Tạo Đề Thi Bằng AI" -> AI sinh đề trắc nghiệm 4 đáp án A/B/C/D bám sát slide giáo trình.
  2. `E2E-QUIZ-02`: **Giao diện phòng thi Split-view & Đếm ngược thời gian**:
     - Bấm "Bắt đầu làm bài" (`/quiz/{id}`) -> Giao diện phòng thi mở ra với đồng hồ đếm ngược, danh sách câu hỏi trực quan, lưu trạng thái câu đã chọn.
  3. `E2E-QUIZ-03`: **Nộp bài & Chấm điểm tức thì kèm giải thích chi tiết**:
     - Chọn đáp án và bấm "Nộp Bài" -> Chuyển sang `/quiz/attempt/{attemptId}`, hiển thị điểm số, câu đúng (xanh), câu sai (đỏ) kèm đoạn trích giải thích.
  4. `E2E-QUIZ-04`: **Bảng xếp hạng nhóm học tập (Squad Leaderboard)**:
     - `squad_leader` làm đề đạt 8.0 điểm.
     - `squad_member1` làm cùng đề thi đó đạt 10.0 điểm.
     - Kiểm tra mục Bảng Xếp Hạng Nhóm: Hiển thị thứ hạng thi đua giữa các bạn trong nhóm Squad.
  5. `E2E-QUIZ-05`: **Feature Gating - Chặn sinh viên Free xuất đề thi PDF**:
     - Đăng nhập `student` (Free), vào bài thi, bấm nút "Xuất PDF Bài Thi" -> Hệ thống từ chối (403 Forbidden) và yêu cầu nâng cấp gói Pro.
  6. `E2E-QUIZ-06`: **Gói Pro xuất file PDF đề thi chuẩn in ấn tiếng Việt**:
     - Đăng nhập `pro_student`, bấm "Xuất PDF Bài Thi" -> Tải file `.pdf` về máy.
     - Mở file kiểm tra: Font chữ tiếng Việt `Arial.ttf` hiển thị chuẩn đẹp không lỗi font, có Watermark bản quyền *"StudyHub AI"*, bảng đáp án (Answer Key Grid) và lời giải chi tiết.
* **Bằng chứng cần nộp**: 6 ảnh chụp màn hình (`E2E-QUIZ-01_nhat.png` đến `E2E-QUIZ-06_nhat.png`) và đính kèm 1 file PDF đã xuất.

---

### 🛡️ 6. AN - ADMIN COMMAND CENTER, QUẢN TRỊ NGƯỜI DÙNG & SYSTEM API KEY
* **Kết quả kiểm thử 02/11/2026**: **5 PASS, 1 BLOCKED** trên `main@e4a9790`, URL QA `http://localhost:8081`, database QA riêng.
  - `E2E-ADM-01` đến `E2E-ADM-05`: **PASS**; `E2E-ADM-06`: **BLOCKED** vì chưa có Gemini API key hợp lệ để kiểm tra ping thành công. Masking và phản hồi khi thiếu/sai key đã kiểm tra.
  - **Báo cáo và ảnh minh chứng**: [docs/evidence/an/README.md](../evidence/an/README.md). Đã lưu đủ 6 ảnh chính đúng tên; ảnh ADM-06 thể hiện kết quả lỗi với khóa giả, không chứng minh kết nối thành công.
  - **Kiểm thử bổ sung**: `AdminHubTest` **29/29 PASS** với tùy chọn tương thích Byte Buddy trên JDK 25; không thay thế test Gemini thật.
* **Tài khoản sử dụng**: `admin` (Admin), `student` (Kiểm tra bảo mật)
* **URL kiểm thử**: `http://localhost:8080/admin/dashboard`, `http://localhost:8080/admin/users`, `http://localhost:8080/admin/settings`
* **Danh sách Test Cases**:
  1. `E2E-ADM-01`: **Bảo vệ route quản trị (Security Access Control)**:
     - Đăng nhập `student`, cố tình nhập `http://localhost:8080/admin/dashboard` trên thanh địa chỉ -> Bị chặn với mã lỗi 403 Forbidden (`admin/error.html`).
  2. `E2E-ADM-02`: **Admin Dashboard & Thống kê 3 gói cước**:
     - Đăng nhập `admin` -> Tự động chuyển hướng vào `/admin/dashboard`.
     - Kiểm tra các số liệu: Tổng người dùng, phân bố theo gói (Free, Pro, Squad), tổng số môn học toàn hệ thống, mô hình Gemini đang dùng.
  3. `E2E-ADM-03`: **Quản lý danh sách sinh viên & Lọc theo gói**:
     - Vào `/admin/users` -> Hiển thị danh sách sinh viên, lọc dropdown theo gói (FREE, PRO, SQUAD) và tìm kiếm theo tên.
  4. `E2E-ADM-04`: **Nâng/Hạ gói cước trực tiếp cho sinh viên**:
     - Tại `student` (Free), Admin bấm "Nâng lên Pro" -> Gói cước đổi sang PRO ngay lập tức.
     - Thử bấm "Kích hoạt SQUAD" hoặc "Chuyển về FREE" -> Cập nhật thành công.
  5. `E2E-ADM-05`: **Reset Quota RAG trong ngày cho sinh viên**:
     - Một sinh viên đã chat hết 50 câu, Admin bấm "Reset RAG" -> Số lượt RAG hôm nay của sinh viên đó về lại `0`.
  6. `E2E-ADM-06`: **Quản trị System Gemini API Key & Nút Test Ping**:
     - Vào `/admin/settings` -> Kiểm tra khóa API được che giấu (`AIza...****`).
     - Bấm nút "Kiểm Tra Kết Nối AI (Test Ping)" -> Hệ thống gửi ping đến Google Gemini API và trả về thông báo màu xanh kèm độ trễ (Latency ms).
* **Bằng chứng cần nộp**: 6 ảnh chụp màn hình (`E2E-ADM-01_an.png` đến `E2E-ADM-06_an.png`).

---

## 📅 5. TIẾN ĐỘ THỰC HIỆN & DEADLINE

* **Bắt đầu**: 13:00 - Thứ Hai, 02/11/2026
* **Hạn chót nộp ảnh bằng chứng**: **20:00 - Thứ Hai, 02/11/2026**
* **Địa điểm nộp ảnh**: Thư mục thành viên tương ứng tại `docs/evidence/[tên_thành_viên]/` trên repo theo cú pháp `[Mã_Test]_[Tên_Thành_Viên].png`.
* **Tổng kết & Khắc phục lỗi**: 20:30 - Thứ Hai, 02/11/2026: Leader Tùng cùng cả nhóm rà soát file `docs/issues/ISSUES-TRACKING.md` để đóng tất cả các bug còn tồn đọng.


---

## 🎨 6. NHIỆM VỤ CẢI THIỆN & ĐỒNG BỘ GIAO DIỆN (UI POLISH TASKS)
> Phân công bổ sung theo chỉ đạo của Team Lead Tùng nhằm chuẩn hóa giao diện toàn hệ thống theo Design System Obsidian & White-Card Palette của StudyHub AI.

| Mã Nhiệm Vụ | Phân hệ / Màn hình | Mô tả công việc | Thành viên phụ trách | Hạn hoàn thành | Trạng thái |
| :---: | :--- | :--- | :---: | :---: | :---: |
| **`TASK-UI-01`** | **Course Detail Hub (`courses/detail.html`)** | Chuyển đổi 4 thẻ phân hệ (RAG, Slide, Planner, Quiz) và khối Cơ sở tri thức từ nền đen thô (`bg-slate-900`) sang **White-Card Palette** (`bg-white/95 border-slate-200 shadow-sm`), đồng bộ nút bấm sang gradient tím hồng Landing Page (`from-purple-600 via-violet-600 to-pink-600`) và typography hiện đại. | **Bình & Trường** | 22:00 - 02/11/2026 | 🟡 IN PROGRESS |

---

Chúc toàn đội Nhóm 2 test mượt mà, bắt trọn bug và bảo vệ đồ án xuất sắc đạt điểm A+! 🚀
