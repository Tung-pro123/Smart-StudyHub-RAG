# 📁 Kho Lưu Trữ Minh Chứng Đồ Án (Evidence Repository)

Hệ thống thư mục `docs/evidence/` được phân cấp khoa học theo **từng mốc thời gian nhiệm vụ (Date-based Tasks)** và bên trong chia theo **từng thành viên phụ trách** để đảm bảo quản lý gọn gàng, tránh phình to và dễ dàng kiểm toán nghiệm thu:

---

## 📅 Danh Sách Các Đợt Nhiệm Vụ

### 1. [`2026-11-02-e2e-testing/`](./2026-11-02-e2e-testing/) — Kiểm Thử Chức Năng E2E 6 Phân Hệ
* **Thời gian**: 02/11/2026
* **Mục tiêu**: Nghiệm thu phân quyền 3 gói cước (FREE, PRO, SQUAD), phân lập dữ liệu (Data Isolation), phòng chống lỗ hổng IDOR và trích dẫn RAG không ảo giác.
* **Thành viên nộp**:
  - `tung/`: 7 ảnh minh chứng Module Core
  - `truong/`: 9 ảnh minh chứng Module Auth & Squad
  - `binh/`: 7 ảnh minh chứng Module RAG & Slide
  - `thang/`: 6 ảnh minh chứng Module AI Planner
  - `nhat/`: 6 ảnh minh chứng + 1 file `de-thi-pro.pdf` Module AI Quiz
  - `an/`: 8 ảnh minh chứng + file summary Module Admin

---

### 2. [`2026-11-03-ui-redesign/`](./2026-11-03-ui-redesign/) — Nâng Cấp Giao Diện Diện Rộng & Regression Testing
* **Thời gian**: 03/11/2026
* **Mục tiêu**: Đồng bộ toàn bộ các trang sang **White-Card Palette & Progressive Disclosure**, phân trang dữ liệu 10-15 mục, triệt tiêu text rác và chạy Regression Checklist trước khi nộp.
* **Thành viên nộp**:
  - `tung/`: Chi tiết môn học (`courses/detail.html`)
  - `truong/`: Hồ sơ cá nhân & Study Squad (`auth/profile.html`)
  - `binh/`: Quản lý slide chunks & Khung chat RAG (`rag/documents.html`, `rag/chat.html`)
  - `thang/`: Lộ trình học tập Tabs tuần & Checklist (`planner/index.html`, `planner/view.html`)
  - `nhat/`: Sinh đề thi, BXH Squad Leaderboard & Phòng thi Split-view (`quiz/index.html`, `quiz/take.html`, `quiz/result.html`)
  - `an/`: Admin Dashboard, Quản lý sinh viên có Phân trang, Cấu hình Gemini Key (`admin/dashboard.html`, `admin/users.html`, `admin/settings.html`)

---

## 📌 Quy Định Đặt Tên File Minh Chứng
* **Cú pháp chuẩn**: `[Mã_Test]_[Tên_Thành_Viên].png` (Ví dụ: `UI-PROF-01_truong.png`, `REG-RAG-01_binh.png`, `E2E-ADM-06_an.png`).
