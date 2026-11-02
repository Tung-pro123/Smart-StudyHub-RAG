# 🐛 BẢNG THEO DÕI VÀ XỬ LÝ LỖI (BUG & ISSUE TRACKING LOG)
> **Dự án**: Smart StudyHub RAG - Nền tảng Tự học & Trợ lý AI Giáo trình (HSF302 - Nhóm 2)  
> **Tài liệu nhiệm vụ test**: [`docs/tasks/task-2026-11-02-e2e-testing-and-qa.md`](../tasks/task-2026-11-02-e2e-testing-and-qa.md)  
> **Cập nhật lần cuối**: 02/11/2026

---

## 📌 1. NGUYÊN TẮC QUẢN LÝ LỖI (BUG HANDLING RULES)

1. **Mọi lỗi phát hiện trong quá trình test E2E** đều phải được ghi nhận ngay vào bảng bên dưới để tránh bỏ sót.
2. **Quy trình xử lý 4 bước**:
   - **Bước 1 (Ghi nhận)**: Người test điền thông tin vào bảng danh sách Issue với trạng thái `🔴 OPEN`.
   - **Bước 2 (Tiếp nhận)**: Thành viên phụ trách phân hệ đó (Assignee) xác nhận và chuyển sang `🟡 IN PROGRESS`.
   - **Bước 3 (Sửa lỗi)**: Viết code sửa lỗi, chạy unit test kiểm tra và commit lên Git với cú pháp: `fix(module): mo ta sua loi (fix #ISSUE-XX)`. Chuyển trạng thái sang `🟢 RESOLVED`.
   - **Bước 4 (Nghiệm thu)**: Người báo lỗi kiểm tra lại trên trình duyệt, nếu đã khắc phục hoàn toàn thì chuyển thành `✅ CLOSED`.

---

## 🚦 2. PHÂN LOẠI MỨC ĐỘ NGHIÊM TRỌNG (SEVERITY LEVEL)

| Mức độ | Định nghĩa | Thời gian khắc phục tối đa |
| :---: | :--- | :---: |
| **💥 BLOCKER** | Hệ thống sập (Crash 500), trắng trang, không thể tiếp tục luồng học tập hoặc tính năng cốt lõi bị tê liệt hoàn toàn. | **Trong vòng 1 giờ** |
| **🚨 HIGH** | Logic nghiệp vụ sai (ví dụ: Gói Free tạo được > 3 môn, RAG không trích dẫn trang, tài khoản xem trộm môn của nhau). | **Trong vòng 2 giờ** |
| **⚠️ MEDIUM** | Lỗi hiển thị, vỡ giao diện (UI broken), thông báo không rõ ràng, nút bấm lệch vị trí trên mobile. | **Trong vòng 4 giờ** |
| **ℹ️ LOW** | Lỗi chính tả, câu chữ chưa chuẩn hóa, màu sắc chưa tối ưu, gợi ý cải tiến nhỏ. | **Trước giờ thuyết trình** |

---

## 📋 3. BẢNG THEO DÕI DANH SÁCH ISSUES & TIẾN ĐỘ XỬ LÝ

| Mã Issue | Ngày tạo | Người báo | Phân hệ | Mô tả lỗi vắn tắt | Mức độ | Người sửa (Assignee) | Trạng thái | Commit Fix / Ghi chú |
| :---: | :---: | :---: | :---: | :--- | :---: | :---: | :---: | :--- |
| **`#ISSUE-01`** | 02/11 | Tùng | `Auth / Profile` | Gói PRO hiển thị nhầm thẻ Quản lý nhóm và dòng chữ `"Bạn là thành viên nhóm @null"`, card Hạn ngạch RAG bị vỡ cột mô tả. | **🚨 HIGH** | **Trường / Tùng** | **✅ CLOSED** | Đã sửa trong commit [`9998765`](https://github.com/Tung-pro123/Smart-StudyHub-RAG/commit/9998765). Đã test 290/290 test passed. |
| **`#ISSUE-02`** | 02/11 | Tùng | `UI / Navigation` | Tái cấu trúc Dashboard sang Sidebar dọc cố định bên trái, chuyển sang White-card Palette, gỡ dropdown học kỳ cứng nhắc để tự do mọi trường (BYOC). | **⚠️ MEDIUM** | **Tùng** | **🟢 RESOLVED** | Hoàn tất trên nhánh `feature/tung-e2e-testing`. UI chuẩn hiện đại, responsive drawer. |
| **`#ISSUE-03`** | 02/11 | Tùng | `Auth / Login UI` | Nút con mắt xem mật khẩu ở trang `/login` không hoạt động do thẻ `<script>` nằm ngoài thẻ `<section>` bị Thymeleaf loại bỏ. | **⚠️ MEDIUM** | **Tùng** | **🟢 RESOLVED** | Đã di chuyển `<script>` vào trong `<section>` ở `login.html`, toggle type password/text và đổi icon fa-eye/fa-eye-slash mượt mà. |
| **`#ISSUE-04`** | 02/11 | Tùng | `Core / Database` | Tài khoản test `student` trong DB bị set nhầm `PRO` và chứa 5 môn học, gây sai lệch khi test Data Isolation và Hạn mức 3 môn Free. | **🚨 HIGH** | **Tùng** | **🟢 RESOLVED** | Đã reset `subscription_tier = 'FREE'` và dọn dẹp các môn test dư thừa, chuẩn hóa về đúng 2 môn ban đầu (`HSF302`, `MAS291`). |

---

## 📝 4. MẪU BÁO CÁO LỖI CHUẨN (ISSUE TEMPLATE)
> Khi phát hiện lỗi mới, hãy copy mẫu dưới đây và điền vào cuối file này:

```markdown
### 🐛 [#ISSUE-XX]: [Tiêu đề lỗi ngắn gọn, súc tích]
* **Người báo lỗi**: [Tên bạn]
* **Phân hệ**: [Core / Auth / RAG / Quiz / Planner / Admin]
* **Tài khoản dùng để test**: [student / pro_student / squad_leader / admin]
* **Mức độ nghiêm trọng**: [💥 BLOCKER / 🚨 HIGH / ⚠️ MEDIUM / ℹ️ LOW]
* **Người chịu trách nhiệm sửa (Assignee)**: [Tên người phụ trách phân hệ đó]
* **Trạng thái**: 🔴 OPEN

#### 1. Các bước tái hiện lỗi (Steps to Reproduce):
1. Đăng nhập vào tài khoản: `...`
2. Truy cập vào trang: `http://localhost:8080/...`
3. Nhấp vào nút: `...`
4. Nhập dữ liệu: `...`

#### 2. Kết quả thực tế bị lỗi (Actual Result):
* [Mô tả chi tiết những gì đang xảy ra trên màn hình: báo lỗi 500, nút không bấm được, hoặc dữ liệu sai lệch...]

#### 3. Kết quả kỳ vọng đúng (Expected Result):
* [Mô tả đúng theo đặc tả nghiệp vụ: hệ thống phải thông báo gì, giao diện phải hiển thị như thế nào...]

#### 4. Ảnh chụp màn hình lỗi (Screenshot Evidence):
* Lưu file ảnh vào thư mục `docs/evidence/[Mã_Issue]_[Ten].png` và chèn link vào đây:
* `![Screenshot](/docs/evidence/ISSUE-XX.png)`
```

---

### 🐛 [#ISSUE-02]: Cải Tiến Giao Diện Dashboard & Điều Hướng Dọc Trái (Vertical Sidebar)
* **Người báo lỗi / đề xuất**: Tùng (Team Lead)
* **Phân hệ**: Core Dashboard / Navigation Layout
* **Tài khoản test**: Toàn bộ tài khoản (`student`, `pro_student`, `admin`)
* **Mức độ**: ⚠️ MEDIUM (UI Transformation & Progressive Disclosure)
* **Người sửa (Assignee)**: Tùng
* **Trạng thái**: 🟢 RESOLVED
* **Mô tả & Khắc phục**:
  1. Chuyển đổi Navbar ngang sang Vertical Navigation Sidebar (`layout/main.html` + `sidebar.html`) cố định bên trái màn hình (`lg:drawer-open`).
  2. Áp dụng bảng màu White-card Palette tinh tế, tối giản, tương thích màu sắc landing page.
  3. Gỡ bỏ dropdown `<select name="semester">` cứng nhắc, thay bằng tìm kiếm từ khóa tự do cho sinh viên mọi trường đại học (BYOC).

---

### 🐛 [#ISSUE-03]: Nút Biểu Tượng Con Mắt Xem Mật Khẩu Không Bấm Được Tại `/login`
* **Người báo lỗi**: Tùng (Team Lead)
* **Phân hệ**: Auth / Login Page
* **Tài khoản test**: Mọi người dùng truy cập `http://localhost:8080/login`
* **Mức độ**: ⚠️ MEDIUM
* **Người sửa (Assignee)**: Tùng
* **Trạng thái**: 🟢 RESOLVED
* **Nguyên nhân**:
  - `login.html` sử dụng layout decorator: `th:replace="~{layout/main :: main-layout(..., ~{::section})}"`.
  - Khối `<script>` chứa hàm `togglePasswordVisibility()` và `fillDemo()` bị đặt ở cuối file ngoài thẻ `<section>`, dẫn đến Thymeleaf loại bỏ toàn bộ script khi render.
* **Khắc phục**:
  - Đưa toàn bộ khối `<script>` vào bên trong thẻ `<section>`.
  - Kiểm tra `togglePasswordVisibility('login-password', this)`: chuyển đổi `type="password"` <-> `type="text"` và đổi icon `fa-eye` <-> `fa-eye-slash` chính xác.

---

### 🐛 [#ISSUE-04]: Tài Khoản `student` Bị Nhầm Gói PRO & Dư Thừa 5 Môn Học Trong Database
* **Người báo lỗi**: Tùng (Team Lead)
* **Phân hệ**: Core / Data Isolation & Quota Gating
* **Tài khoản test**: `student`
* **Mức độ**: 🚨 HIGH (Sai lệch dữ liệu kiểm thử nghiệp vụ)
* **Người sửa (Assignee)**: Tùng
* **Trạng thái**: 🟢 RESOLVED
* **Nguyên nhân**:
  - Do các lượt test nâng cấp và tạo môn trước đó, tài khoản `student` trong file DB cục bộ (`./data/studyhubdb.mv.db`) bị chuyển thành gói `PRO` và sở hữu 5 môn học (`HSF302`, `MAS291`, `SWP391`, `DBI201`, `CSD201`).
  - Khi Tùng đăng nhập để test `E2E-CORE-02` (Data Isolation) và `E2E-CORE-03` (Hạn mức Free 3 môn), tài khoản hiển thị 5 môn và không thể kiểm tra logic chặn môn thứ 4.
* **Khắc phục**:
  - Chạy script cập nhật H2 Database:
    - Đặt lại `subscription_tier = 'FREE'` cho `student`.
    - Dọn dẹp 3 môn test dư thừa (SWP391, DBI201, CSD201) và các dữ liệu liên quan.
    - Tài khoản `student` hiện tại sở hữu đúng 2 môn ban đầu: `HSF302` và `MAS291`.

---

## 🛠️ 5. HƯỚNG DẪN DÀNH CHO NGƯỜI SỬA LỖI (ASSIGNEE)

1. **Đọc kỹ các bước tái hiện**: Tự chạy app trên máy mình và làm theo các bước trong issue để quan sát lỗi trực tiếp.
2. **Kiểm tra Log lỗi trên Terminal**:
   - Nếu là lỗi 500, xem Stack Trace trong terminal chạy Spring Boot để biết chính xác Exception phát sinh ở Class và dòng số bao nhiêu.
3. **Sửa code và viết/chạy Unit Test**:
   - Mở class tương ứng để chỉnh sửa.
   - Chạy lệnh test maven để đảm bảo không phá vỡ các chức năng cũ:
     ```bash
     mvn test -Dtest=[TenTestClass]
     ```
4. **Commit & Cập nhật Issue**:
   - Commit code với cú pháp rõ ràng: `fix(module): noi dung fix (fix #ISSUE-XX)`.
   - Vào bảng ở Mục 3, cập nhật trạng thái thành `🟢 RESOLVED` kèm mã commit.
   - Báo cho người phát hiện lỗi kiểm tra lại lần cuối để đóng issue sang `✅ CLOSED`.

