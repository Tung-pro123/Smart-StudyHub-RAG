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
| Mã Issue | Ngày tạo | Người báo | Phân hệ | Mô tả lỗi vắn tắt | Mức độ | Người sửa (Assignee) | Trạng thái | Commit Fix / Ghi chú |
| :---: | :---: | :---: | :---: | :--- | :---: | :---: | :---: | :--- |
| **`#ISSUE-01`** | 02/11 | Tùng | `Auth / Profile` | Gói PRO hiển thị nhầm thẻ Quản lý nhóm và dòng chữ `"Bạn là thành viên nhóm @null"`, card Hạn ngạch RAG bị vỡ cột mô tả. | **🚨 HIGH** | **Trường / Tùng** | **✅ CLOSED** | Đã sửa trong commit [`9998765`](https://github.com/Tung-pro123/Smart-StudyHub-RAG/commit/9998765). Đã test 290/290 test passed. |
| **`#ISSUE-02`** | 02/11 | Tùng | `UI / Navigation` | Tái cấu trúc Dashboard sang Sidebar dọc cố định bên trái, chuyển sang White-card Palette, đồng bộ màu sắc với Landing Page (Obsidian `#030712` + Gradient tím hồng), gỡ dropdown học kỳ cứng nhắc để tự do mọi trường (BYOC). | **⚠️ MEDIUM** | **Tùng** | **🟢 RESOLVED** | Hoàn tất trên nhánh `feature/tung-e2e-testing`. UI chuẩn hiện đại, responsive drawer, đồng bộ tông Landing Page. |
| **`#ISSUE-03`** | 02/11 | Tùng / Trường | `Auth / Login UI` | Nút con mắt xem mật khẩu và nút đăng nhập 1-Click tài khoản demo ở `/login` không hoạt động do thẻ `<script>` nằm ngoài thẻ `<section>` bị Thymeleaf loại bỏ. | **⚠️ MEDIUM** | **Tùng / Trường** | **🟢 RESOLVED** | Đã di chuyển `<script>` vào trong `<section>` ở `login.html`, toggle password và fillDemo chạy mượt mà. |
| **`#ISSUE-04`** | 02/11 | Tùng | `Core / Database` | Tài khoản test `student` trong DB bị set nhầm `PRO` và chứa 5 môn học, gây sai lệch khi test Data Isolation và Hạn mức 3 môn Free. | **🚨 HIGH** | **Tùng** | **🟢 RESOLVED** | Đã reset `subscription_tier = 'FREE'` và chuyển các môn test sang tài khoản Pro/Admin, chuẩn hóa student về đúng 2 môn ban đầu (`HSF302`, `MAS291`). |
| **`#ISSUE-05`** | 02/11 | Tùng | `Core / Database` | Lỗi vi phạm Unique Index `IDX_COURSES_CODE_INDEX_1` khi tạo môn học có mã đã tồn tại ở người dùng khác do ràng buộc UNIQUE cũ trong H2 DB chưa được drop. | **🚨 HIGH** | **Tùng** | **🟢 RESOLVED** | Đã drop constraint UNIQUE toàn cục `IDX_COURSES_CODE`, chuyển thành index thông thường để đảm bảo phân lập dữ liệu đa người dùng (Data Isolation). |
| **`#ISSUE-06`** | 02/11 | Tùng | `UI / Pricing Modal` | Nút chuyển đổi kỳ thanh toán 'Theo Học Kỳ' trong modal Bảng giá không bấm được do khối script nằm ngoài thẻ `<section>` bị Thymeleaf layout decorator loại bỏ. | **⚠️ MEDIUM** | **Tùng** | **🟢 RESOLVED** | Đã di chuyển khối `<script>` vào bên trong thẻ `<section>` ở `index.html`, bổ sung ID cập nhật giá và đồng bộ dải gradient Landing Page cho tab kích hoạt. |
| **`#ISSUE-07`** | 02/11 | Tùng | `UI / Course Hub` | Giao diện Chi Tiết Môn Học (`courses/detail.html`) đang dùng tông nền thẻ tối cũ (`bg-slate-900`), chưa đồng bộ với White-Card Palette và gradient tím hồng Landing Page. | **⚠️ MEDIUM** | **Tùng** | **🟢 RESOLVED** | Hoàn tất bởi Lead Tùng. Chuyển đổi 4 gateway cards và Knowledge Base sang White-Card Palette, đồng bộ gradient Landing Page tím hồng, accordion chunks hiện đại. |
| **`#ISSUE-08`** | 02/11 | Trường | `Auth / Profile` | Cập nhật hồ sơ đổi được họ tên nhưng không đổi được avatar khi chọn preset (do script `selectAvatar` nằm ngoài `<section>` trong `profile.html`). | **⚠️ MEDIUM** | **Trường** | **🟢 RESOLVED** | Chuyển thẻ `<script>` vào bên trong thẻ `<section>` của `profile.html`. |

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
  2. Áp dụng bảng màu White-card Palette tinh tế, tối giản, tương thích màu sắc landing page: Hero banner Obsidian `#030712`, hiệu ứng Ambient Cyber Glow cyan/violet, các nút CTA đồng bộ gradient Landing Page (`from-purple-600 via-violet-600 to-pink-600`).
  3. Thay thế thông báo flash thành công/lỗi màu xanh thô bằng Toast Cards bo tròn thanh thoát `bg-emerald-50/95` và `bg-rose-50/95`.
  4. Gỡ bỏ dropdown `<select name="semester">` cứng nhắc, thay bằng tìm kiếm từ khóa tự do cho sinh viên mọi trường đại học (BYOC).

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
    - Chuyển giao 3 môn test (SWP391, DBI201, CSD201) sang tài khoản Pro/Admin để bảo lưu dữ liệu.
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


### 🐛 [#ISSUE-05]: Lỗi Vi Phạm Unique Index `IDX_COURSES_CODE_INDEX_1` Khi Tạo Môn Trùng Mã Với Người Khác
* **Người báo lỗi**: Tùng (Team Lead)
* **Phân hệ**: Core / Data Isolation & Course Management
* **Tài khoản test**: `student`
* **Mức độ**: 🚨 HIGH (Chặn đứng thao tác tạo môn học của người dùng)
* **Người sửa (Assignee)**: Tùng
* **Trạng thái**: 🟢 RESOLVED
* **Nguyên nhân**:
  - Trong bảng `COURSES` của H2 Database cục bộ (`./data/studyhubdb.mv.db`), có tồn tại một ràng buộc UNIQUE cũ tên `IDX_COURSES_CODE` trên cột `code`.
  - Nghiệp vụ của StudyHub AI đã nâng cấp sang cơ chế Data Isolation (phân lập dữ liệu đa người dùng), cho phép các sinh viên khác nhau đều có thể tự do tạo môn học cùng mã (như `DBI201`, `HSF302`, `PRJ301`). Validation ở `CourseService` kiểm tra trùng theo cặp `(code, user_id)`.
  - Do cấu hình Hibernate `ddl-auto=update` không tự drop các ràng buộc UNIQUE cũ trong database thực tế, khi `admin` đang giữ môn `DBI201` thì `student` tạo môn `DBI201` bị database chặn lại và báo lỗi `Unique index or primary key violation: IDX_COURSES_CODE_INDEX_1`.
* **Khắc phục**:
  - Chạy script SQL trực tiếp trên H2 Database:
    ```sql
    ALTER TABLE COURSES DROP CONSTRAINT IF EXISTS IDX_COURSES_CODE;
    CREATE INDEX IF NOT EXISTS IDX_COURSES_CODE ON COURSES(CODE);
    ```
  - Kiểm tra lại: Sinh viên có thể tạo môn học độc lập mà không bị xung đột với môn của tài khoản khác; đồng thời trong cùng một tài khoản vẫn được chặn trùng lặp mã qua `CourseService`.

---

### 🐛 [#ISSUE-06]: Nút Chuyển Đổi Kỳ Thanh Toán 'Theo Học Kỳ' Không Bấm Được Trong Modal Bảng Giá
* **Người báo lỗi**: Tùng (Team Lead)
* **Phân hệ**: UI / Pricing Upgrade Modal (`index.html`)
* **Tài khoản test**: Mọi tài khoản (`student`, `pro_student`)
* **Mức độ**: ⚠️ MEDIUM (Ảnh hưởng trải nghiệm người dùng xem gói cước)
* **Người sửa (Assignee)**: Tùng
* **Trạng thái**: 🟢 RESOLVED
* **Nguyên nhân**:
  - `index.html` sử dụng layout decorator: `th:replace="~{layout/main :: main-layout(..., ~{::section})}"`.
  - Khối `<script>` chứa hàm `switchBillingPeriod()` và `openEditModal()` bị đặt ở cuối file ngoài thẻ `<section>`, dẫn đến Thymeleaf loại bỏ toàn bộ script khi render giao diện trình duyệt.
  - Khi người dùng nhấn vào nút "Theo Học Kỳ" (`tab-semester`), trình duyệt báo lỗi `Uncaught ReferenceError: switchBillingPeriod is not defined`.
* **Khắc phục**:
  - Đưa toàn bộ khối `<script>` vào bên trong thẻ `<section>`.
  - Bổ sung `id="card-squad-price"` và `id="card-squad-period"` vào thẻ Gói Nhóm để cập nhật giá đồng bộ.
  - Đồng bộ dải màu gradient Landing Page (`from-purple-600 via-violet-600 to-pink-600`) cho tab đang chọn.

---

### 🐛 [#ISSUE-07]: Đồng Bộ Giao Diện Course Detail Hub (`courses/detail.html`) Sang White-Card Palette
* **Người báo lỗi / đề xuất**: Tùng (Team Lead)
* **Phân hệ**: UI / Course Detail Hub (`courses/detail.html`)
* **Tài khoản test**: Mọi sinh viên truy cập màn hình chi tiết môn học (`student`, `pro_student`)
* **Mức độ**: ⚠️ MEDIUM (Chuẩn hóa Design System toàn ứng dụng)
* **Người sửa (Assignee)**: Bình & Trường (Phối hợp thực hiện theo chỉ đạo của Lead Tùng)
* **Trạng thái**: 🟢 RESOLVED
* **Mô tả & Yêu cầu khắc phục**:
  1. Chuyển đổi 4 thẻ phân hệ cấp độ 2 (Trợ Lý RAG, Tài Liệu Slide, Lịch Học Vừa Sức, Luyện Đề Trắc Nghiệm) từ thẻ tối `bg-slate-900/85` sang **White-Card Palette** (`bg-white/95 border-slate-200/80 text-slate-800 shadow-sm hover:shadow-lg`).
  2. Đồng bộ nút bấm CTA sang dải gradient chuẩn của Landing Page: `bg-gradient-to-r from-purple-600 via-violet-600 to-pink-600`.
  3. Cải tiến khối "Cơ Sở Tri Thức Của Môn Học" và trạng thái rỗng (empty state) sang phong cách sáng, sạch sẽ, tinh tế.

---

### 🐛 [#ISSUE-08]: Chọn Avatar Presets tại profile.html đổi được họ tên nhưng avatar không cập nhật
* **Người báo lỗi**: Ngô Nhật Trường
* **Phân hệ**: Auth / Profile
* **Tài khoản dùng để test**: `squad_leader`
* **Mức độ nghiêm trọng**: ⚠️ MEDIUM
* **Người chịu trách nhiệm sửa (Assignee)**: Trường / Tùng
* **Trạng thái**: 🟢 RESOLVED
* **Nguyên nhân**:
  - Thẻ `<script>` chứa hàm `selectAvatar()` đặt sau thẻ `</section>`, khiến hàm không được gọi khi click, giá trị trong input hidden `avatar` không thay đổi.
* **Khắc phục**:
  - Di chuyển toàn bộ thẻ `<script>` vào bên trong thẻ `<section>` ở `profile.html`.
* **Ảnh chụp màn hình lỗi**:
  - `![Screenshot](../evidence/truong/E2E-AUTH-03_truong.png)`

---

## 6. Trở ngại môi trường QA Admin — Ân — 02/11/2026

Các mục QA dưới đây được tách khỏi lỗi chức năng `ISSUE-01..03`. Không phát hiện lỗi chức năng Admin mới trong phạm vi đã chạy. Báo cáo: [Admin E2E](../evidence/an/README.md).

### QA-ADM-01 — BLOCKED: Thiếu Gemini key hợp lệ để nghiệm thu E2E-ADM-06

* **Người ghi nhận**: Ân; **phân hệ**: Admin / cấu hình môi trường QA.
* **Mức ảnh hưởng**: Chặn luồng nghiệm thu kết nối Gemini thành công; không phải lỗi chức năng đã xác nhận.
* **Người xử lý**: Chủ tài khoản Gemini phối hợp Ân.
* **Môi trường**: `main@e4a9790`, `http://localhost:8081`, database QA riêng, không có `GEMINI_API_KEY`.
* **Các bước tái hiện**: Đăng nhập `admin`, mở `/admin/settings`, bấm “Kiểm tra kết nối tức thì”.
* **Thực tế**: Trang báo chưa cấu hình; ping báo cần API key hợp lệ. Thử thêm khóa giả QA để kiểm tra masking: hiển thị `AIzaSy...****`; ping trả thông báo đỏ “Yêu cầu không hợp lệ. Kiểm tra API key và cấu hình model.”, 827 ms.
* **Kỳ vọng nghiệm thu còn thiếu**: Với key hợp lệ, ping thực tế Google Gemini thành công, hiển thị thông báo xanh và latency. Không đánh dấu PASS bằng key giả hay mock.
* **Hướng gỡ chặn**: Cấu hình `GEMINI_API_KEY` hợp lệ cho môi trường QA, khởi động app và chạy lại ADM-06. Người dùng đã chọn ghi nhận BLOCKED và bàn giao kết quả hiện có.
* **Bằng chứng**: [Ảnh Test Ping](../evidence/an/E2E-ADM-06_an.png), [Ảnh masking](../evidence/an/E2E-ADM-06_masked_an.png).

### QA-ADM-02 — WORKAROUND VERIFIED: Test runner mặc định lỗi trên JDK 25

* **Người ghi nhận / phụ trách**: Ân; **phân hệ**: môi trường kiểm thử tự động.
* **Ảnh hưởng**: Lệnh test Admin mặc định không khởi tạo được Mockito; chưa chứng minh lỗi nghiệp vụ.
* **Các bước tái hiện**: Dùng JDK 25.0.4, chạy `mvn -B -Dtest=AdminHubTest test` trên `main@e4a9790`.
* **Thực tế**: 29 errors ở bước khởi tạo context/mock. Nguyên nhân gốc trong log: `Java 25 (69) is not supported by the current version of Byte Buddy which officially supports Java 23 (67)`.
* **Kỳ vọng**: Test runner khởi tạo được và chạy assertions trên JDK được hỗ trợ.
* **Workaround đã xác minh**: `mvn -B -Dtest=AdminHubTest -Dnet.bytebuddy.experimental=true test` cho **29 tests, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS**. Không sửa dependency hay mã nguồn trong đợt QA này.
* **Xử lý lâu dài**: Dùng JDK tương thích với dự án (Java 17) hoặc cập nhật Byte Buddy/Mockito theo quy trình riêng; chưa nghiệm thu biện pháp lâu dài.
* **Bằng chứng**: [Surefire summary](../evidence/an/AdminHubTest-summary.txt); log local `target/admin-e2e-regression.log` và `target/admin-e2e-regression-compatible.log`.

### Đối chiếu lỗi Auth đã có

Lần chạy QA Admin tái hiện nút “FREE student” trên `/login` không tự điền form. Đây là **ISSUE-03 đã được giải quyết** bằng việc chuyển script vào bên trong section trên `feature/tung-e2e-testing`.

---

## QA UI Admin — Ân — 03/11/2026

### QA-UI-AN-01 — LOCAL VERIFIED: Phân trang backend theo TASK-AN-03

- **Phạm vi được người dùng cho phép**: `UserRepository`, `AdminController`, bốn template Admin và test liên quan; giữ nguyên layout/fragments và ảnh cũ. Chưa push.
- **Vấn đề trước thay đổi**: `searchStudents()` trả `List<User>` không giới hạn, template dựng toàn bộ sinh viên. 1.000 tài khoản tạo 1.000 hàng DOM; không đạt yêu cầu mới.
- **Xử lý**: `Page<User>`/`Pageable` cố định 15, lọc tại database, chuyển trang giữ từ khóa/gói; thao tác POST giữ ngữ cảnh, tự lùi trang nếu trang cuối rỗng. Không thêm dependency, không đổi schema/role/quota logic.
- **Kiểm chứng**: Test 1.000 bản ghi, trang đầu/kế/cuối, từ khóa + tier, trang sai/rỗng và đổi gói làm rỗng trang cuối. Toàn bộ dự án 307/307 PASS; Admin 40/40 PASS. Browser với 1.000 tài khoản QA chỉ có 15 hàng dữ liệu/trang đầy.
- **Spec**: [Admin Hub](../admin-hub.md); **minh chứng**: [Báo cáo 03/11](../evidence/2026-11-03-ui-redesign/an/README.md).

### QA-UI-AN-02 — LOCAL VERIFIED: Ping thành công nhưng chậm bị hiển thị vàng

- **Tái hiện trong template trước sửa**: `ping.success() == true` và latency > 1.200 ms gắn `alert-warning`, không đáp ứng yêu cầu hiển thị thành công xanh.
- **Xử lý**: Màu kết quả chỉ phụ thuộc thành công/thất bại, latency riêng; thông báo thành công rõ ràng, không sửa service Gemini.
- **Kiểm chứng**: Test render với phản hồi thành công giả lập 3.500 ms vẫn `alert-success`, không chứa `alert-warning` hoặc key. Đây là test logic, **không đóng ADM-06**.

### QA-UI-AN-03 — OPEN / bàn giao Lead: Điều hướng học tập trong navbar/sidebar Admin

- **Người ghi nhận**: Ân; **phụ trách**: Tùng (file dùng chung).
- **Thực tế trên main@eb0e885 và bản QA**: Admin vẫn thấy Tạo Môn Mới ở navbar và Môn Học Hệ Thống trỏ `/courses` trong sidebar. Nội dung Dashboard của Ân đã bỏ nút giám sát và link chi tiết môn học.
- **Kỳ vọng theo kế hoạch 03/11**: Điều hướng Admin tập trung vận hành hệ thống; không dùng thao tác học tập như sinh viên.
- **Phạm vi xử lý còn lại**: Lead đồng bộ `templates/fragments/navbar.html` và `sidebar.html`; Ân giữ nguyên layout/fragments theo chỉ đạo, không che tạm bằng CSS.
- **Ảnh**: [Dashboard laptop](../evidence/2026-11-03-ui-redesign/an/dashboard-1366_an.png).

### ADM-06 — vẫn BLOCKED ngày 03/11

Không có `.env` hoặc biến Gemini key trong môi trường QA. Đã thử form rỗng, key sai, masking bằng key giả và phản hồi Ping thất bại, không crash. Key thật/kết nối thật chưa nghiệm thu; giữ `QA-ADM-01` BLOCKED theo chỉ đạo người dùng. Không ghi nhận thiếu key là lỗi chức năng. [Ảnh hiện tại](../evidence/2026-11-03-ui-redesign/an/E2E-ADM-06_an.png). Giữ nguyên ảnh ngày 02/11.
