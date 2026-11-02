# 📁 Cấu Trúc Lưu Trữ Minh Chứng Kiểm Thử E2E (Evidence Repository)

Thư mục `docs/evidence/` được phân cấp theo **từng thành viên phụ trách** để đảm bảo quản lý gọn gàng, tránh phình to và dễ dàng tra cứu minh chứng của từng module.

---

## 👥 Danh Sách Thư Mục Thành Viên

| Thư mục | Thành viên | Vai trò & Phân hệ kiểm thử | Quy chuẩn đặt tên file |
| :--- | :--- | :--- | :--- |
| [`tung/`](./tung/) | **Lê Nguyễn Minh Tùng (Lead)** | **Module 1**: Quản lý môn học, Data Isolation, Landing Page, Level 2 Hub | `E2E-CORE-0X_tung.png` |
| [`truong/`](./truong/) | **Trường** | **Module 2**: Authentication, JWT Session, 1-Click Login, Rate Limit | `E2E-AUTH-0X_truong.png` |
| [`binh/`](./binh/) | **Bình** | **Module 3**: RAG AI Pipeline, Ingestion Slide PDF, Trích dẫn số trang | `E2E-RAG-0X_binh.png` |
| [`thang/`](./thang/) | **Thắng** | **Module 4**: AI Study Planner, Lịch học vừa sức theo tuần, Spaced Repetition | `E2E-PLAN-0X_thang.png` |
| [`nhat/`](./nhat/) | **Huỳnh Nhật** | **Module 5**: AI Quiz Generator, Đấu trường nhóm, Xuất đề PDF | `E2E-QUIZ-0X_nhat.png`, `de-thi-pro.pdf` |
| [`an/`](./an/) | **An** | **Module 6**: Admin Command Center, User & Tier Quota, Cấu hình API Key | `E2E-ADMIN-0X_an.png` |

---

## 📌 Quy Định Đặt Tên & Nộp Minh Chứng
1. **Minh chứng kiểm thử E2E**:
   - `docs/evidence/[thành_viên]/[Mã_Test]_[thành_viên].png`
   - Ví dụ: `docs/evidence/tung/E2E-CORE-01_tung.png`, `docs/evidence/nhat/E2E-QUIZ-01_nhat.png`
2. **Minh chứng Issue/Bug**:
   - `docs/evidence/[thành_viên]/[Mã_Issue]_[thành_viên].png`
   - Ví dụ: `docs/evidence/thang/ISSUE-02_thang.png`
3. **Tài liệu/File xuất ra (nếu có)**:
   - Đặt trực tiếp vào thư mục của thành viên sở hữu (Ví dụ: `docs/evidence/nhat/de-thi-pro.pdf`).
