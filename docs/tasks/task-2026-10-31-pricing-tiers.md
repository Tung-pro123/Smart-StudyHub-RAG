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

### 👑 1. TÙNG (LEAD) - CORE & CÁ NHÂN HÓA MÔN HỌC (DATA ISOLATION & QUOTA)
* **Trạng thái**: **Đã hoàn thành xuất sắc & Đã merge vào main (PR #27)** ✅
* **Nhánh Git**: `feature/pricing-core-tung`
* **Package Backend**: `com.group2.assignment.core`
* **Thư mục View**: `templates/index.html`, `templates/courses/`
* **Nhiệm vụ cụ thể**:
  1. 🚨 **CẤP BÁCH: Tách biệt môn học theo từng tài khoản (Data Isolation)**:
     - **Nguyên nhân lỗi**: `Course.java` hiện chưa có liên kết `User` (`user_id`), và `CourseService` đang gọi `courseRepository.findAll()` lấy toàn bộ môn học trong database, khiến ai đăng nhập vào cũng thấy hết môn của người khác; đồng thời `code` bị unique toàn cục khiến 2 người không thể cùng tạo môn `HSF302`.
     - **Giải pháp xử lý**:
       + Trong `Course.java`: Thêm `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;` (hoặc `Long userId`). Bỏ unique toàn cục trên `code`, đổi thành unique theo cặp `(user_id, code)` để mỗi bạn đều có thể tự tạo môn `HSF302` cho riêng mình.
       + Trong `CourseRepository.java`: Thêm `List<Course> findAllByUserId(Long userId);`, `long countByUserId(Long userId);`, `boolean existsByCodeIgnoreCaseAndUserId(String code, Long userId);`.
       + Trong `CourseService.java`: Sửa `getAllCoursesWithStats(keyword, semester, currentUser)` chỉ lấy danh sách môn thuộc sở hữu của `currentUser` (Admin xem được tất cả).
       + Trong `CourseController.java`: Kiểm tra quyền sở hữu khi Sửa / Xóa (Sinh viên A không được phép sửa/xóa môn của Sinh viên B).
  2. **Kiểm tra hạn mức 3 môn học theo từng User**:
     - Sửa logic đếm: Đổi từ `countCourses() < 3` thành `countByUserId(currentUser.getId()) < MAX_COURSES_FREE (3 môn)`. Đảm bảo mỗi bạn Free được tạo đúng 3 môn của riêng mình!
     - Khi đã có 3 môn, disable nút "Tạo môn học" trên giao diện `index.html` và hiển thị tooltip: *"Đã đạt tối đa 3 môn của Gói Free"*. Xóa 1 môn cũ thì tạo lại được ngay.
  3. **Bổ sung hiển thị Badge gói cước trên Navbar**:
     - Gói Free: `<span class="badge badge-ghost text-xs">FREE</span>`
     - Gói Pro: `<span class="badge badge-secondary text-xs">PRO</span>`
     - Gói Squad: `<span class="badge badge-info text-xs">SQUAD (5 TV)</span>`
  4. **Kiểm tra Modal Bảng giá**: Đảm bảo mở mượt mà từ Navbar, hiển thị đúng 3 thẻ giá và câu từ chuẩn hóa không chứa model cụ thể ("Truy vấn RAG AI giáo trình không giới hạn").
* **Định nghĩa hoàn thành (DoD)**:
  - Đăng nhập `student` chỉ thấy các môn của `student`. Đăng nhập `truong` chỉ thấy các môn của `truong`. Không còn tình trạng tài khoản này thấy môn của tài khoản khác!
  - Cả 2 sinh viên đều có thể tự tạo môn `HSF302` của riêng mình mà không bị lỗi trùng mã môn.
  - Tài khoản Free tạo đến môn thứ 4 của mình bị chặn báo lỗi. Xóa 1 môn cũ thì tạo được ngay.

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

### 📊 6. ÂN - ADMIN COMMAND CENTER & HỆ THỐNG QUẢN TRỊ TOÀN DIỆN
* **Nhánh Git**: `feature/pricing-admin-an`
* **Package Backend**: `com.group2.assignment.settings`
* **Thư mục View**: `templates/admin/dashboard.html`, `templates/admin/users.html`, `templates/admin/settings.html`
* **Mục tiêu**: Nâng cấp toàn diện giao diện và tính năng trang Admin từ mức cơ bản thành **Trung tâm Chỉ huy Quản trị (Admin Command Center)** hiện đại, trực quan và bảo mật cao, phục vụ giám sát toàn bộ hoạt động hệ sinh thái StudyHub AI.
* **Nhiệm vụ cụ thể (5 Hạng Mục Cải Tiến)**:
  1. **Nâng cấp Dashboard Thống kê KPI Thời gian thực (`AdminAnalyticsService.java`, `AdminStatsDto.java` & `admin/dashboard.html`)**:
     - Bổ sung thống kê phân rã chi tiết cả 3 gói cước: Số sinh viên FREE (`freeUsersCount`), Số sinh viên PRO (`proUsersCount`), Số sinh viên SQUAD (`squadUsersCount`).
     - Đo lường Tỷ lệ Người dùng Trả phí (Paying Conversion Rate): $\text{paidConversionRate} = \frac{PRO + SQUAD}{\text{TotalStudents}} \times 100\%$.
     - Thiết kế lại giao diện Dashboard chuẩn DaisyUI Glassmorphism sang trọng:
       * **Thẻ 1 - 👥 Tổng Người Dùng**: Hiển thị tổng số tài khoản kèm thanh chia tỷ lệ 3 gói FREE (xám) / PRO (tím) / SQUAD (xanh dương).
       * **Thẻ 2 - 📚 Kho Tri Thức Hệ Thống**: Tổng số môn học toàn trường và tổng số Chunks tài liệu đã vector hóa / index.
       * **Thẻ 3 - ⚡ Tải Lượng RAG Hôm Nay**: Tổng số câu hỏi AI mà sinh viên truy vấn trong ngày (theo múi giờ Việt Nam GMT+7).
       * **Thẻ 4 - 🟢 Tình Trạng AI Gemini**: Badge online/offline, độ trễ ping mili-giây (ms), và model AI đang phục vụ.
     - Bổ sung Bảng *"Hoạt Động Hệ Thống Gần Đây"*: Danh sách 5 tài khoản đăng ký mới nhất và môn học vừa được tạo.
  2. **Nâng cấp Quản Lý Sinh Viên & Can Thiệp Gói Cước 1-Click (`admin/users.html` & `AdminController.java`)**:
     - **Thanh Tìm kiếm & Lọc Sinh Viên**: Cho phép gõ tìm theo Tên, Username hoặc Email; có bộ nút bấm lọc nhanh (Tất cả | FREE | PRO | SQUAD).
     - **Bộ Tác vụ 1-Click Action linh hoạt**:
       * Nút ⚡ **Nâng cấp PRO** (`POST /admin/users/{id}/upgrade-pro`): Kích hoạt tức thì gói cá nhân Pro.
       * Nút 👥 **Kích hoạt SQUAD** (`POST /admin/users/{id}/upgrade-squad`): Kích hoạt gói Nhóm Học Tập (Study Squad).
       * Nút ↩️ **Hạ về FREE** (`POST /admin/users/{id}/downgrade-free`): Chuyển tài khoản về hạng miễn phí.
       * Nút 🔄 **Reset Quota RAG** (`POST /admin/users/{id}/reset-rag`): Khôi phục ngay 50 câu hỏi RAG cho sinh viên trong ngày để hỗ trợ kịp thời khi demo.
     - **Hiển thị Badge Trực quan**: Cột RAG hiển thị kèm thanh mini progress bar cảnh báo đổi màu (xanh < 30 câu, vàng < 45 câu, đỏ = 50 câu).
  3. **Tối ưu Quản Trị Cấu Hình System AI Key (`admin/settings.html` & `SystemAiKeyService.java`)**:
     - Che giấu khóa (API Key Masking) dạng `AIzaSy...****` trên giao diện nhằm chống quay lén hoặc lộ key nhạy cảm khi thuyết trình demo.
     - Nút *"Kiểm Tra Kết Nối Tức Thì"* (Live Ping Test) hiển thị spinner tải và thông báo trực quan: Màu xanh (Kết nối tốt, latency X ms) hoặc Màu đỏ (Lỗi 429 Quota Exceeded / 401 Invalid Key).
  4. **Giám sát Môn Học Toàn Hệ Thống (`/courses`)**:
     - Khi Admin truy cập `/courses`: Hiển thị chế độ Quản trị viên toàn quyền, gắn nhãn rõ ràng chủ sở hữu từng môn (*"Bởi: student"*, *"Bởi: truong"*).
     - Cho phép Admin xóa bỏ các môn học rác hoặc môn thử nghiệm để giữ CSDL luôn gọn gàng.
  5. **Bảo mật CSRF & Kiểm thử Tự động (Testing)**:
     - Rà soát toàn bộ các form POST trong phân hệ Admin đảm bảo đều có Token CSRF bảo mật.
     - Bổ sung Unit Tests trong `AdminHubTest.java` kiểm thử đầy đủ các endpoint mới (`upgrade-squad`, `reset-rag`, `search-users`).
* **Định nghĩa hoàn thành (DoD)**:
  - Admin vào `/admin/dashboard` thấy giao diện hiện đại, chuyên nghiệp, hiển thị đầy đủ KPI 3 gói cước.
  - Bấm nút 1-Click can thiệp gói (Pro / Squad / Free) trên danh sách sinh viên hoạt động ngay lập tức không cần tải lại trang phức tạp.
  - Nút Test Ping Gemini phản hồi chính xác tình trạng sức khỏe AI.
  - Toàn bộ Unit Tests phân hệ Admin vượt qua 100% (`BUILD SUCCESS`).

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

