# KẾ HOẠCH TRIỂN KHAI 3 HẠNG GÓI CƯỚC (SPRINT PLAN - 31/10/2026)
**Dự án**: Smart Multi-Course Study Hub & RAG AI Platform  
**Chủ đề Sprint**: Hoàn thiện & Đồng bộ 3 Gói Cước (`FREE` - `PRO` - `SQUAD`) trên toàn bộ 6 phân hệ.

---

## 🎯 1. BẢNG MA TRẬN 3 GÓI CƯỚC CHUẨN

| Tính năng / Phân hệ | Gói 1: Sinh Viên Tự Học (`FREE` - $0) | Gói 2: Ôn Thi Cấp Tốc (`PRO` - $2/tháng) | Gói 3: Nhóm Học Tập (`SQUAD` - $5/tháng) |
| :--- | :--- | :--- | :--- |
| **Số lượng môn học tối đa** | Tối đa **3 môn** (Xóa môn cũ mới tạo được môn mới) | **Không giới hạn** | **Không giới hạn** |
| **Số tài liệu PDF / môn** | Tối đa **3 file PDF / môn** (Chống nhồi môn làm loãng RAG) | **Không giới hạn** | **Không giới hạn (Dùng chung cho cả nhóm)** |
| **Hạn ngạch RAG Chatbot** | Giới hạn **50 câu hỏi / ngày** (Reset 00:00) | **Không giới hạn** | **Không giới hạn cho cả 5 thành viên** |
| **AI Study Planner** | **Bị khóa hoàn toàn** (Kính mờ Locked Overlay) | **Mở khóa** Timeline cá nhân chi tiết theo tuần | **Mở khóa** Timeline + Phân chia nhiệm vụ đồ án nhóm |
| **AI Quiz Generator** | Tối đa **10 câu / đề**, khóa Xuất PDF | Mở **15 - 20 câu / đề**, **Xuất file PDF đề thi** | Đề thi 20 câu + **Bảng xếp hạng điểm số nhóm (Leaderboard)** |
| **Số lượng thành viên** | 1 người | 1 người | **Nhóm tối đa 5 bạn** (Nhóm trưởng add bạn qua Username) |

---

## 👥 2. PHÂN CHIA NHIỆM VỤ CHI TIẾT CHO 6 THÀNH VIÊN

### 👑 1. TÙNG (LEAD) - CORE & QUẢN LÝ MÔN HỌC (COURSES)
* **Nhánh Git**: `feature/pricing-core-tung`
* **Package Backend**: `com.group2.assignment.core`
* **Thư mục View**: `templates/index.html`, `templates/courses/`
* **Nhiệm vụ cụ thể**:
  1. Kiểm tra hạn mức 3 môn học trong `CourseService.java` (`countCourses() < MAX_COURSES_FREE`). Khi đã có 3 môn, disable nút "Tạo môn học" trên giao diện `index.html` và hiển thị tooltip: *"Đã đạt tối đa 3 môn của Gói Free"*.
  2. Bổ sung hiển thị Badge gói cước của người dùng trên thanh Navbar:
     - Gói Free: `<span class="badge badge-ghost text-xs">FREE</span>`
     - Gói Pro: `<span class="badge badge-secondary text-xs">PRO</span>`
     - Gói Squad: `<span class="badge badge-info text-xs">SQUAD (5 TV)</span>`
  3. Đảm bảo Modal Bảng giá (`id="modal-pricing-upgrade"`) mở mượt mà từ Navbar, hiển thị đúng 3 thẻ giá chuẩn và câu từ không chứa model cụ thể ("Truy vấn RAG AI giáo trình không giới hạn").
* **Định nghĩa hoàn thành (DoD)**:
  - Tài khoản Free tạo đến môn thứ 4 bị chặn báo lỗi thân thiện. Xóa 1 môn cũ đi thì tạo được ngay.
  - Tài khoản Pro / Squad tạo môn thứ 4, 5, 6 bình thường.

---

### 🛡️ 2. TRƯỜNG - AUTH, BẢO MẬT & QUẢN LÝ NHÓM 5 BẠN (SQUAD MANAGEMENT)
* **Nhánh Git**: `feature/pricing-auth-squad-truong`
* **Package Backend**: `com.group2.assignment.auth`
* **Thư mục View**: `templates/auth/profile.html`
* **Nhiệm vụ cụ thể**:
  1. Xây dựng logic Quản lý Nhóm 5 thành viên trong `AuthService.java` và `AuthController.java`:
     - Thêm trường `squadLeaderUsername` trong `User.java` (lưu username của bạn mua gói nhóm).
     - Viết endpoint `POST /auth/squad/add-member`: Nhóm trưởng nhập `targetUsername` để mời bạn vào nhóm (kiểm tra tối đa 4 bạn + 1 nhóm trưởng = 5 người). Khi add thành công, tài khoản bạn đó tự động chuyển sang `subscriptionTier = "SQUAD"`.
     - Viết endpoint `POST /auth/squad/remove-member`: Nhóm trưởng có quyền xóa bạn ra khỏi nhóm (bạn đó về lại `FREE`).
  2. Cập nhật giao diện `profile.html`:
     - Nếu user là `SQUAD` (nhóm trưởng): Hiển thị widget **"Không gian Nhóm Học Tập (Study Squad: X/5 thành viên)"** kèm form gõ username bạn bè và nút *"Thêm vào nhóm"*.
     - Nếu user là `FREE`: Hiển thị nút *"Nâng Cấp Pro ($2)"* và *"Kích Hoạt Gói Nhóm ($5)"*.
* **Định nghĩa hoàn thành (DoD)**:
  - Tài khoản mua gói Nhóm add thử bạn `an` và `binh` $\rightarrow$ tài khoản `an` và `binh` tự động lên Pro/Squad.
  - Nhập người thứ 6 vào hệ thống ném cảnh báo: *"Nhóm đã đủ tối đa 5 thành viên!"*.

---

### 🌟 3. BÌNH - RAG CHATBOT & KIỂM SOÁT TÀI LIỆU (DOCUMENT LIMIT)
* **Nhánh Git**: `feature/pricing-rag-binh`
* **Package Backend**: `com.group2.assignment.rag`
* **Thư mục View**: `templates/rag/chat.html`, `templates/rag/documents.html`
* **Nhiệm vụ cụ thể**:
  1. Kiểm soát 50 câu RAG/ngày trong `ChatController.java`:
     - Gói Free: Đếm câu hỏi thời gian thực `dailyRagCount`. Câu thứ 51 trả về ChatBubble thông báo hết hạn ngạch ngày và gợi ý nâng cấp.
     - Gói Pro & Squad: Không giới hạn, biểu tượng vô hạn `∞`.
  2. Ngăn chặn hiện tượng *"Nhồi nhiều môn vào 1 môn làm loãng RAG"* trong `DocumentController.java`:
     - Gói Free: Giới hạn tối đa **3 tài liệu PDF / môn học** (`MAX_DOCS_FREE = 3`). Khi tải file thứ 4 báo lỗi: *"Gói Free giới hạn 3 tài liệu/môn để đảm bảo độ chính xác RAG. Vui lòng nâng cấp để tải trọn bộ giáo trình!"*.
     - Gói Pro & Squad: Tải không giới hạn slide/giáo trình.
  3. Đảm bảo Citation Card trích dẫn chính xác số trang và tên file PDF.
* **Định nghĩa hoàn thành (DoD)**:
  - Sinh viên Free hỏi đến câu 50 bình thường, câu 51 bị chặn. Sang ngày hôm sau (hoặc test reset) được chat tiếp.
  - Sinh viên Free upload đến file PDF thứ 4 trong 1 môn bị chặn.

---

### 📅 4. THẮNG - AI STUDY PLANNER (CÁ NHÂN & NHÓM)
* **Nhánh Git**: `feature/pricing-planner-thang`
* **Package Backend**: `com.group2.assignment.planner`
* **Thư mục View**: `templates/planner/index.html`, `templates/planner/view.html`
* **Nhiệm vụ cụ thể**:
  1. Hoàn thiện Feature Gating cho gói Free:
     - Tại `planner/index.html`: Nếu `user.subscriptionTier == 'FREE'`, phủ lớp kính mờ (Locked Overlay) che form lập lịch kèm nút bấm mở modal nâng cấp.
     - Tại `PlannerController.java`: Chặn cứng endpoint `POST /planner/generate` nếu là user Free.
  2. Mở khóa cho Gói Pro:
     - Sinh lộ trình học tập cá nhân dựa trên Chunks giáo trình và lịch rảnh.
  3. Điểm nhấn cho Gói Squad (Study Squad):
     - Tại `planner/view.html`: Thêm nhãn hoặc trường `assignee` (Phân công bạn làm task nào trong đồ án nhóm môn học).
* **Định nghĩa hoàn thành (DoD)**:
  - Đăng nhập tài khoản `student` (Free) vào `/planner` thấy rõ giao diện bị khóa kính mờ, không thể submit.
  - Đăng nhập `pro_student` hoặc `squad_user` mở form bình thường, sinh timeline thành công.

---

### 🚀 5. NHẬT - AI QUIZ GENERATOR & BẢNG XẾP HẠNG (LEADERBOARD)
* **Nhánh Git**: `feature/pricing-quiz-nhat`
* **Package Backend**: `com.group2.assignment.quiz`
* **Thư mục View**: `templates/quiz/index.html`, `templates/quiz/take.html`, `templates/quiz/result.html`
* **Nhiệm vụ cụ thể**:
  1. Kiểm soát số lượng câu hỏi trong `QuizController.java`:
     - Gói Free: Dropdown chọn số lượng câu chỉ cho phép tối đa **10 câu** (chọn 15, 20 câu sẽ bị vô hiệu hóa hoặc ném alert).
     - Gói Pro & Squad: Cho phép sinh đề dài **15 - 20 câu** theo từng chương chuyên sâu.
  2. Kiểm soát tính năng Xuất đề PDF & Flashcards trong `result.html`:
     - Gói Free: Nút *"Xuất đề thi PDF"* và *"Flashcards Spaced Repetition"* bị vô hiệu hóa (disabled) kèm nhãn `PRO VIP`.
     - Gói Pro & Squad: Click tải ngay file PDF đề thi được định dạng đẹp mắt để in ra giấy.
  3. Điểm nhấn cho Gói Squad:
     - Hiển thị bảng tổng kết lịch sử làm bài (Recent Attempts) theo nhóm: Hiển thị điểm số cao nhất của các bạn để thi đua.
* **Định nghĩa hoàn thành (DoD)**:
  - Sinh viên Free cố chọn 20 câu bị chặn lại. Nộp bài xong không bấm được nút Xuất PDF.
  - Sinh viên Pro sinh được đề 20 câu và xuất PDF thành công.

---

### 📊 6. ÂN - ADMIN HUB & SYSTEM ANALYTICS
* **Nhánh Git**: `feature/pricing-admin-an`
* **Package Backend**: `com.group2.assignment.settings` (hoặc `admin`)
* **Thư mục View**: `templates/admin/dashboard.html`, `templates/admin/users.html`
* **Nhiệm vụ cụ thể**:
  1. Cập nhật Dashboard thống kê (`AdminAnalyticsService.java` & `dashboard.html`):
     - Thống kê tỷ lệ phân bổ người dùng theo cả 3 gói: Số sinh viên Free, Số sinh viên Pro, Số sinh viên Squad.
     - Thanh Radial Progress hiển thị tỷ lệ % tài khoản trả phí trên tổng số sinh viên.
  2. Nâng cấp Bảng quản lý người dùng (`admin/users.html`):
     - Hiển thị rõ cột Gói cước: Huy hiệu xanh lá `FREE`, huy hiệu tím `PRO`, huy hiệu xanh dương `SQUAD`.
     - Cung cấp nút 1-Click: Nâng cấp PRO (`POST /admin/users/{id}/upgrade-pro`), Kích hoạt SQUAD, hoặc Hạ về FREE (`downgrade-free`).
  3. Đảm bảo cơ chế Reset Quota ngày mới chạy chính xác theo múi giờ `Asia/Ho_Chi_Minh` trong `SubscriptionQuotaService.java`.
* **Định nghĩa hoàn thành (DoD)**:
  - Admin vào `/admin/dashboard` thấy số liệu chia thành 3 gói rõ ràng.
  - Thử bấm nút nâng cấp tài khoản bất kỳ từ Free lên Squad hoặc Pro thì tài khoản đó được mở khóa ngay lập tức.

---

## 🚀 3. KỊCH BẢN KIỂM THỬ TỔNG THỂ (END-TO-END DEMO SCRIPT)

1. **Bước 1 (Demo Gói Free)**:
   - Đăng nhập tài khoản `student` / `123456`.
   - Xem thanh hạn ngạch: `0 / 50 câu RAG`.
   - Vào `/planner`: Thấy lớp kính mờ khóa tính năng.
   - Vào `/courses`: Đã có 3 môn, cố tạo môn thứ 4 $\rightarrow$ Bị chặn thông báo nâng cấp.
   - Xóa bớt 1 môn $\rightarrow$ Tạo lại môn mới thành công.
2. **Bước 2 (Demo Nâng cấp Pro)**:
   - Click nút *"Nâng Cấp Pro"* $\rightarrow$ Quét VietQR $\rightarrow$ Xác nhận.
   - Tài khoản đổi huy hiệu `PRO`.
   - Vào `/planner`: Form mở khóa, nhập 6 giờ/tuần $\rightarrow$ AI sinh Timeline ôn thi chi tiết.
   - Vào Chat RAG: Hỏi câu 51+ thoải mái, hạn ngạch hiển thị `VÔ HẠN`.
   - Vào Quiz: Tạo đề 20 câu và bấm *"Xuất đề thi PDF"* in ấn.
3. **Bước 3 (Demo Gói Nhóm Study Squad)**:
   - Nhóm trưởng kích hoạt Gói Nhóm $\rightarrow$ Nhập username bạn bè (ví dụ add `tung`, `binh`).
   - Đăng nhập tài khoản của bạn được mời $\rightarrow$ Thấy tài khoản tự động được mở khóa full Pro.
