# Implementation & Verification Plan: Task 5 (Nhật) - AI Quiz Generator, Testing Room, Squad Leaderboard & PDF Export

## 1. Mục tiêu và Phạm vi
Kế hoạch này phục vụ việc hoàn thiện các chi tiết kỹ thuật còn thiếu và thực thi kiểm thử End-to-End (E2E) cho phân hệ Quiz của thành viên **Nhật**, thu thập đầy đủ 6 ảnh chụp minh chứng (`E2E-QUIZ-01_nhat.png` -> `E2E-QUIZ-06_nhat.png`) cùng file PDF xuất mẫu (`docs/evidence/de-thi-pro.pdf`).

### Phân vai & Tài khoản kiểm thử
| Tài khoản | Mật khẩu | Hạng tài khoản | Vai trò trong kịch bản kiểm thử |
| :--- | :--- | :--- | :--- |
| `student` | `123456` | **FREE** | Kiểm thử tạo đề (10 câu), làm bài, nộp bài, và bị chặn xuất PDF (E2E-QUIZ-05) |
| `pro_student` | `123456` | **PRO** | Kiểm thử xuất PDF chuẩn in ấn tiếng Việt, Watermark & Đáp án (E2E-QUIZ-06) |
| `squad_leader` | `123456` | **SQUAD (Leader)** | Làm đề đạt 8.0 điểm cho Squad Leaderboard (E2E-QUIZ-04) |
| `squad_member1` | `123456` | **SQUAD (Member)** | Làm đề đạt 10.0 điểm đứng Top 1 Squad Leaderboard (E2E-QUIZ-04) |

---

## 2. Danh sách Task Thực Thi

### Task 1: Bổ sung Watermark & Bảng Đáp Án Nhanh vào PDF Template
- **File**: `src/main/resources/templates/quiz/pdf-template.html`
- **Yêu cầu kỹ thuật**:
  1. Thêm CSS class `.watermark` cố định góc chéo mờ `-30deg`, opacity thấp.
  2. Thêm thẻ `<div class="watermark">StudyHub AI</div>` vào thân tài liệu PDF.
  3. Bổ sung bảng tóm tắt đáp án (Answer Key Grid) dạng `<table>` ở đầu trang lời giải khi `showAnswers = true`.

### Task 2: Cấu hình Khởi tạo Dữ liệu Tự động (Auto-seed Document & Attempts)
- **File**: `src/main/java/com/group2/assignment/core/config/DemoDataSeeder.java`
- **Yêu cầu kỹ thuật**:
  1. Kiểm tra và gán file PDF mẫu cho môn học `HSF302` và `EXE101` nếu chưa có `Document`.
  2. Tự động seed sẵn 2 lượt làm bài (attempts) cho `squad_leader` (8.0 điểm) và `squad_member1` (10.0 điểm) trên một đề thi mẫu của nhóm Squad để phục vụ trực tiếp kiểm thử `E2E-QUIZ-04` mà không cần thao tác tay thủ công lặp lại.

### Task 3: Chạy Kiểm Thử Tự Động Toàn Dự Án
- **Lệnh**: `mvn test -Dtest=QuizPdfExportServiceTest,QuizDoDVerificationTest,AuthServiceTest`
- **Mục tiêu**: Đảm bảo 100% test case liên quan đến Quiz, PDF Export, Feature Gating và Squad Leaderboard đều xanh (`BUILD SUCCESS`).

### Task 4: Chạy Server & Thực Hiện 6 Bước Kiểm Thử E2E
- **Lệnh khởi chạy**: `mvn spring-boot:run`
- **Kiểm thử & Thu thập ảnh chụp màn hình**:
  - `E2E-QUIZ-01_nhat.png`: Giao diện `/quiz`, chọn môn `HSF302`, chọn độ khó, bấm sinh đề AI.
  - `E2E-QUIZ-02_nhat.png`: Giao diện `/quiz/{id}` phòng thi Split-view, đồng hồ đếm ngược, danh sách câu hỏi.
  - `E2E-QUIZ-03_nhat.png`: Giao diện `/quiz/attempt/{id}` điểm số, câu đúng/sai, giải thích chi tiết.
  - `E2E-QUIZ-04_nhat.png`: Bảng xếp hạng nhóm Squad tại `/quiz` hiển thị thứ hạng `squad_member1` (#1 - 10.0đ) và `squad_leader` (#2 - 8.0đ).
  - `E2E-QUIZ-05_nhat.png`: Đăng nhập `student` (Free), nút xuất PDF bị disabled / badge PRO VIP và route `/quiz/attempt/{id}/pdf` trả về 403 Forbidden.
  - `E2E-QUIZ-06_nhat.png`: Đăng nhập `pro_student`, xuất file PDF thành công, mở xem file hiển thị font Arial tiếng Việt, Watermark "StudyHub AI" và bảng đáp án.
  - Lưu file PDF tải về vào `docs/evidence/de-thi-pro.pdf`.

---

## 3. Tiêu Chí Nghiệm Thu (Acceptance Criteria)
- [ ] File `pdf-template.html` có watermark "StudyHub AI" và Answer Key Grid.
- [ ] Dữ liệu demo có sẵn môn học, tài liệu và điểm xếp hạng squad.
- [ ] Toàn bộ unit/integration tests liên quan đều PASS.
- [ ] Có đầy đủ 6 ảnh trong `docs/evidence/` đúng định dạng tên chuẩn.
- [ ] Có file `docs/evidence/de-thi-pro.pdf` hiển thị tiếng Việt chuẩn.

