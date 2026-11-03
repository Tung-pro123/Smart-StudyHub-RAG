# 🕵️ KẾ HOẠCH RÀ SOÁT & NGHIỆM THU CHI TIẾT E2E - NHÓM 2
> **Chủ trì**: Lê Thanh Tùng (Team Lead)  
> **Dự án**: Smart StudyHub RAG - Smart Multi-Course Study Hub & AI Courseware Assistant  
> **Học phần**: HSF302 - Java Web Application & Spring Boot 3  
> **Thời gian rà soát**: Đợt tổng kết 02/11/2026  

---

## 🎯 1. MỤC TIÊU CỦA ĐỢT RÀ SOÁT (AUDIT OBJECTIVES)

1. **Rà soát chất lượng ảnh minh chứng**: Ảnh chụp rõ nét, đúng quy chuẩn đặt tên `[Mã_Test]_[Tên].png`, chụp đúng luồng thực tế tại `http://localhost:8080`.
2. **Kiểm tra tính toàn vẹn của mã nguồn (Code Integrity)**:
   - Các logic bảo vệ phân quyền (Feature Gating) theo 3 gói cước (**FREE**, **PRO**, **SQUAD**).
   - Cơ chế phân lập dữ liệu cá nhân (**Data Isolation**) theo `user_id`.
   - Phòng chống lỗ hổng leo quyền truy cập trái phép (**IDOR Ownership Protection**).
3. **Đóng các Issue còn tồn đọng**: Rà soát bảng [`docs/issues/ISSUES-TRACKING.md`](../issues/ISSUES-TRACKING.md) để chuyển trạng thái từ `OPEN` / `IN PROGRESS` sang `RESOLVED` và `CLOSED`.
4. **Sẵn sàng bảo vệ đồ án đạt điểm A+**: Đảm bảo từng thành viên nắm vững kiến trúc phân hệ của mình, demo trơn tru trước giảng viên.

---

## 👥 2. MA TRẬN RÀ SOÁT CHI TIẾT THEO TỪNG THÀNH VIÊN

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       THỨ TỰ RÀ SOÁT CHI TIẾT 6 MODULE                      │
│                                                                             │
│  [1. TÙNG] ──► [2. TRƯỜNG] ──► [3. BÌNH] ──► [4. THẮNG] ──► [5. NHẬT] ──► [6. ÂN]│
│   (Core/Hub)     (Auth/Squad)    (RAG/PDF)    (Planner)    (Quiz/PDF)   (Admin) │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 👑 THÀNH VIÊN 1: LÊ THANH TÙNG (TEAM LEAD)
* **Phân hệ**: Core Dashboard, Tìm kiếm tự do, Phân lập dữ liệu, Hạn mức 3 môn & Landing Page
* **Thư mục ảnh**: [`docs/evidence/2026-11-02-e2e-testing/tung/`](../evidence/2026-11-02-e2e-testing/tung/) (Đã nộp đủ 7 ảnh)

| Mã Test | Nội dung kiểm tra | Tiêu chuẩn nghiệm thu | Minh chứng | Đánh giá Team Lead |
| :--- | :--- | :--- | :---: | :---: |
| `E2E-CORE-01` | Tìm kiếm từ khóa tự do | Gõ "Spring" ra `HSF302`; gõ "XYZ999" ra thông báo rỗng | `E2E-CORE-01_tung.png` | ✅ **PASS 100%** |
| `E2E-CORE-02` | Phân lập dữ liệu cá nhân | `student` chỉ thấy 2 môn; `pro_student` chỉ thấy 7 môn | `E2E-CORE-02_tung.png` | ✅ **PASS 100%** |
| `E2E-CORE-03` | Hạn mức 3 môn Gói Free | Tạo môn thứ 3 đạt trần 3/3; cảnh báo nâng cấp; xóa môn giảm về 2/3 | `E2E-CORE-03_tung.png` | ✅ **PASS 100%** |
| `E2E-CORE-04` | Gói Pro tạo môn không giới hạn | Tạo môn thứ 8 thành công, hạn ngạch `8 / ∞ không giới hạn` | `E2E-CORE-04_tung.png` | ✅ **PASS 100%** |
| `E2E-CORE-05` | Bảo vệ quyền sở hữu (IDOR) | Nhập URL `/courses/delete/9` bị chặn với toast cảnh báo màu đỏ | `E2E-CORE-05_tung.png` | ✅ **PASS 100%** |
| `E2E-CORE-06` | Course Detail Hub | Chi tiết môn học hiển thị đầy đủ thống kê chunks, slide, 4 cổng tính năng | `E2E-CORE-06_tung.png` | ✅ **PASS 100%** *(Chờ TASK-UI-01 tút màu)* |
| `E2E-CORE-07` | Landing Page & Session Routing | Giao diện khách vãng lai hoàn chỉnh; tự redirect vào `/courses` hoặc `/admin/dashboard` | `E2E-CORE-07_tung.png` | ✅ **PASS 100%** |

> **Kết luận Module 1**: Đạt chuẩn nghiệm thu xuất sắc. Đã giải quyết `#ISSUE-02`, `#ISSUE-04`, `#ISSUE-05`, `#ISSUE-06`.

---

### 👤 THÀNH VIÊN 2: NGÔ NHẬT TRƯỜNG
* **Phân hệ**: Authentication, Đăng ký, 1-Click Login, Đổi mật khẩu BCrypt, Hồ sơ cá nhân & Study Squad
* **Thư mục ảnh**: [`docs/evidence/2026-11-02-e2e-testing/truong/`](../evidence/2026-11-02-e2e-testing/truong/) (Đã nộp đủ 9 ảnh)

| Mã Test | Nội dung kiểm tra | Tiêu chuẩn nghiệm thu | Minh chứng | Đánh giá Team Lead |
| :--- | :--- | :--- | :---: | :---: |
| `E2E-AUTH-01` | Đăng nhập 1-Click tự động | Bấm huy hiệu demo tự điền username/password | `E2E-AUTH-01_truong.png` | ✅ **PASS** *(Tùng đã fix script)* |
| `E2E-AUTH-02` | Đăng ký tài khoản mới | Đăng ký user `test_user_01`, tự gán gói Free, login ngay | `E2E-AUTH-02_truong.png` | ✅ **PASS** |
| `E2E-AUTH-03` | Cập nhật họ tên & Avatar Presets | Đổi họ tên và bấm chọn avatar preset cập nhật ngay | `E2E-AUTH-03_truong.png` | ✅ **PASS** *(Tùng đã fix script)* |
| `E2E-AUTH-04` | Đổi mật khẩu bảo mật BCrypt | Mật khẩu cũ sai báo lỗi; mật khẩu mới cập nhật mã hóa BCrypt | `E2E-AUTH-04_truong.png` | ✅ **PASS** |
| `E2E-AUTH-05` | Trưởng nhóm mời bạn vào Squad | Mời username `student2` $\rightarrow$ nhóm lên 4 bạn | `E2E-AUTH-05_truong.png` | ✅ **PASS** |
| `E2E-AUTH-06` | Chặn mời khi đủ 5 bạn | Nhóm đủ 5/5 thì ô nhập bị disabled, báo đã đạt tối đa | `E2E-AUTH-06_truong.png` | ✅ **PASS** |
| `E2E-AUTH-07` | Xóa thành viên khỏi Squad | Bấm Xóa $\rightarrow$ Thành viên bị xóa khỏi nhóm, quay về Free | `E2E-AUTH-07_truong.png` | ✅ **PASS** |
| `E2E-AUTH-08` | Đặc quyền Pro & Kiểm tra @null | `squad_member1` hiển thị đúng trưởng nhóm; `pro_student` ẩn card squad | `E2E-AUTH-08_truong.png` | ✅ **PASS** |

> **Điểm cần lưu ý khi rà soát**: Trường phát hiện 2 issue về script ngoài thẻ `<section>` ở `login.html` và `profile.html`. Cả 2 issue này Lead Tùng đã hỗ trợ fix xong trên nhánh hiện tại!

---

### 📄 THÀNH VIÊN 3: BÌNH
* **Phân hệ**: Quản lý tài liệu Slide PDF, Cắt nhỏ Chunks, Chatbot RAG Giáo trình có Citation đối chứng
* **Thư mục ảnh**: [`docs/evidence/2026-11-02-e2e-testing/binh/`](../evidence/2026-11-02-e2e-testing/binh/) (Đã nộp đủ 7 ảnh)

| Mã Test | Nội dung kiểm tra | Tiêu chuẩn nghiệm thu | Minh chứng | Đánh giá Team Lead |
| :--- | :--- | :--- | :---: | :---: |
| `E2E-RAG-01` | Tải lên slide PDF bài giảng | Tải PDF thành công, hiển thị kích thước và ngày tải | `E2E-RAG-01_binh.png` | ✅ **PASS** |
| `E2E-RAG-02` | Cắt nhỏ Chunks văn bản | Trích xuất các đoạn chunks có đánh số trang rõ ràng | `E2E-RAG-02_binh.png` | ✅ **PASS** |
| `E2E-RAG-03` | Xóa tài liệu PDF | Xóa tài liệu đồng thời dọn sạch toàn bộ chunks liên quan | `E2E-RAG-03_binh.png` | ✅ **PASS** |
| `E2E-RAG-04` | Chatbot RAG kèm Citation | Trả lời bám sát giáo trình + Citation Card đính kèm số trang | `E2E-RAG-04_binh.png` | ✅ **PASS (0% Ảo giác)** |
| `E2E-RAG-05` | Hạn mức 3 PDF Gói Free | Tải đến file thứ 4 hệ thống chặn lại và báo vượt hạn mức | `E2E-RAG-05_binh.png` | ✅ **PASS** |
| `E2E-RAG-06` | Gói Pro tải PDF không giới hạn | Tải liên tục nhiều slide bài giảng mượt mà | `E2E-RAG-06_binh.png` | ✅ **PASS** |
| `E2E-RAG-07` | Hạn ngạch 50 câu RAG/ngày Free | Chat vượt 50 câu $\rightarrow$ Hệ thống chặn và hiện banner nâng cấp | `E2E-RAG-07_binh.png` | ✅ **PASS** |

> **Kết luận Module 3**: Bình hoàn thành 7/7 test cases rất chuẩn chỉ, minh chứng rõ ràng, chatbot trích dẫn đúng số trang.

---

### 🗓️ THÀNH VIÊN 4: THẮNG
* **Phân hệ**: AI Study Planner, Lộ trình học tập cá nhân hóa, Checklist nhiệm vụ tuần & Quyền riêng tư
* **Thư mục ảnh**: [`docs/evidence/2026-11-02-e2e-testing/thang/`](../evidence/2026-11-02-e2e-testing/thang/) (Đã đồng bộ đủ 6 ảnh)

| Mã Test | Nội dung kiểm tra | Tiêu chuẩn nghiệm thu | Minh chứng | Đánh giá Team Lead |
| :--- | :--- | :--- | :---: | :---: |
| `E2E-PLAN-01` | Feature Gating chặn gói Free | `student` bấm tạo lộ trình $\rightarrow$ Bị chặn, bật modal nâng cấp Pro | `E2E-PLAN-01_thang.png` | ✅ **PASS** |
| `E2E-PLAN-02` | Gói Pro sinh lộ trình ôn thi AI | Nhập mục tiêu, số giờ học/tuần $\rightarrow$ AI sinh lộ trình ôn thi chi tiết | `E2E-PLAN-02_thang.png` | ✅ **PASS** |
| `E2E-PLAN-03` | Giao diện chi tiết lộ trình tuần | Hiển thị lộ trình phân bổ theo từng tuần khoa học | `E2E-PLAN-03_thang.png` | ✅ **PASS** |
| `E2E-PLAN-04` | Interactive Task Toggle | Tick hoàn thành nhiệm vụ $\rightarrow$ Thanh tiến độ % tự nhảy mượt mà | `E2E-PLAN-04_thang.png` | ✅ **PASS** |
| `E2E-PLAN-05` | Lịch sử lộ trình theo môn học | Xem lại các kế hoạch học tập cũ theo từng môn học | `E2E-PLAN-05_thang.png` | ✅ **PASS** |
| `E2E-PLAN-06` | Bảo vệ quyền riêng tư lộ trình | Sinh viên khác gõ ID lộ trình không thuộc về mình $\rightarrow$ Bị chặn | `E2E-PLAN-06_thang.png` | ✅ **PASS** |

> **Kết luận Module 4**: Đã đổi tên thư mục từ `thanh` thành `thang` và gom toàn bộ 6 ảnh vào đúng chuẩn. Code bảo vệ quyền riêng tư của Thắng đã được merge mượt mà.

---

### 🧠 THÀNH VIÊN 5: HUỲNH NHẬT
* **Phân hệ**: AI Quiz Trắc Nghiệm, Phòng Thi Split-view, Squad Leaderboard & Xuất PDF Tiếng Việt
* **Thư mục ảnh & file**: [`docs/evidence/2026-11-02-e2e-testing/nhat/`](../evidence/2026-11-02-e2e-testing/nhat/) (Đã nộp đủ 6 ảnh + file `de-thi-pro.pdf`)

| Mã Test | Nội dung kiểm tra | Tiêu chuẩn nghiệm thu | Minh chứng | Đánh giá Team Lead |
| :--- | :--- | :--- | :---: | :---: |
| `E2E-QUIZ-01` | Sinh đề trắc nghiệm AI từ slide | Chọn môn, chọn độ khó $\rightarrow$ AI sinh đề 10 câu 4 đáp án A/B/C/D | `E2E-QUIZ-01_nhat.png` | ✅ **PASS** |
| `E2E-QUIZ-02` | Phòng thi Split-view & Đếm ngược | Giao diện thi chia cột, đồng hồ đếm ngược, lưu trạng thái câu chọn | `E2E-QUIZ-02_nhat.png` | ✅ **PASS** |
| `E2E-QUIZ-03` | Nộp bài & Chấm điểm tức thì | Hiển thị điểm số, câu đúng (xanh), câu sai (đỏ), kèm giải thích chi tiết | `E2E-QUIZ-03_nhat.png` | ✅ **PASS** |
| `E2E-QUIZ-04` | Bảng xếp hạng nhóm (Squad Leaderboard) | Hiển thị thứ hạng điểm số giữa các thành viên trong nhóm Study Squad | `E2E-QUIZ-04_nhat.png` | ✅ **PASS** |
| `E2E-QUIZ-05` | Chặn Free xuất đề thi PDF | `student` bấm xuất PDF bị từ chối 403 Forbidden | `E2E-QUIZ-05_nhat.png` | ✅ **PASS** |
| `E2E-QUIZ-06` | Gói Pro xuất PDF chuẩn in ấn | Tải file PDF về máy: font Arial tiếng Việt đẹp, Watermark "StudyHub AI", Answer Key Grid | `E2E-QUIZ-06_nhat.png` & `de-thi-pro.pdf` | ✅ **PASS XUẤT SẮC** |

> **Trả lời câu hỏi của Tùng**: **Nhật ĐÃ XONG 100%**. Nhật nộp đủ 6 ảnh minh chứng và có cả file PDF in ấn thực tế đạt chuẩn cao!

---

### 🛡️ THÀNH VIÊN 6: ÂN
* **Phân hệ**: Admin Command Center, Thống kê 3 gói cước, Quản lý sinh viên, Nâng/Hạ gói, System Gemini API Key
* **Thư mục ảnh**: [`docs/evidence/2026-11-02-e2e-testing/an/`](../evidence/2026-11-02-e2e-testing/an/) (Đã nộp đủ 8 ảnh + file summary + README)

| Mã Test | Nội dung kiểm tra | Tiêu chuẩn nghiệm thu | Minh chứng | Đánh giá Team Lead |
| :--- | :--- | :--- | :---: | :---: |
| `E2E-ADM-01` | Chặn sinh viên truy cập Admin | Sinh viên thường gõ `/admin/dashboard` $\rightarrow$ Bị chặn 403 Forbidden | `E2E-ADM-01_an.png` | ✅ **PASS** |
| `E2E-ADM-02` | Thống kê Dashboard 3 gói cước | Biểu đồ và chỉ số người dùng Free, Pro, Squad toàn hệ thống | `E2E-ADM-02_an.png` | ✅ **PASS** |
| `E2E-ADM-03` | Quản lý danh sách sinh viên | Lọc theo gói cước (FREE, PRO, SQUAD) và tìm kiếm theo tên | `E2E-ADM-03_an.png` | ✅ **PASS** |
| `E2E-ADM-04` | Nâng/Hạ gói cước trực tiếp | Admin đổi gói cước cho sinh viên cập nhật ngay tức thì | `E2E-ADM-04_an.png` | ✅ **PASS** |
| `E2E-ADM-05` | Reset Quota RAG trong ngày | Bấm Reset $\rightarrow$ Số lượt RAG hôm nay của sinh viên về 0 | `E2E-ADM-05_an.png` | ✅ **PASS** |
| `E2E-ADM-06` | Quản trị System Gemini Key & Ping | Masking API key `AIza...****`; Nút Test Ping kiểm tra kết nối | `E2E-ADM-06_an.png` | ⚠️ **BLOCKED** *(Do môi trường máy Ân thiếu key)* |

> **Điểm cần lưu ý khi rà soát**: Case `E2E-ADM-06` bị BLOCKED do máy Ân lúc test không có `GEMINI_API_KEY`. Tuy nhiên code hệ thống trên máy Lead Tùng đã hoạt động bình thường với key thật.

---

## 📋 3. CHECKLIST CÁC ĐẦU VIỆC CẦN LÀM TIẾP CỦA TEAM LEAD TÙNG

1. [x] **Đổi tên thư mục**: Đã đổi `docs/evidence/thanh` $\rightarrow$ `docs/evidence/thang` và di chuyển sạch sẽ 6 ảnh vào trong.
2. [x] **Chuẩn hóa tài liệu**: Đã đổi tất cả các từ "Thành" thành "Thắng" trong tài liệu phân công nhiệm vụ.
3. [x] **Xác nhận kết quả của Nhật**: Nhật đã nộp đủ 6 ảnh và 1 file PDF chuẩn.
4. [ ] **Thực hiện `TASK-UI-01`**: Tút lại giao diện màu sắc cho `courses/detail.html` sang **White-Card Palette** theo đúng chỉ đạo của anh Tùng.
5. [ ] **Đóng các Issue**: Cập nhật file [`docs/issues/ISSUES-TRACKING.md`](../issues/ISSUES-TRACKING.md) sau khi nghiệm thu hoàn tất.

