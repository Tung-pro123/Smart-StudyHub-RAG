# 🎙️ LỜI THOẠI THUYẾT TRÌNH: SLIDE 1 ➔ SLIDE 4 (NGÔ NHẬT TRƯỜNG - MỞ MÀN, BỐI CẢNH, UI & AUTH)
> **Khối trình bày**: Khối 1 — Mở màn toàn bộ buổi báo cáo, Bối cảnh & Đặt vấn đề, Assignment (UI Toolkit + AI Agent) & Cổng vào Authentication  
> **Thời lượng**: 4 phút  
> **Tác phong**: Đĩnh đạc, rõ ràng, phong thái tự tin khi mở màn buổi bảo vệ  

---

### [SLIDE 1 — TRANG BÌA & GIỚI THIỆU NHÓM 2] (~40 giây)
"Kính chào quý Thầy Cô trong Hội đồng Giảng viên và toàn thể các bạn có mặt trong buổi bảo vệ ngày hôm nay. 

Em tên là **Ngô Nhật Trường**, đại diện cho **Nhóm 2** lớp HSF302. Hôm nay, nhóm chúng em rất vinh dự được trình bày đồ án tốt nghiệp môn học mang tên: **SMART STUDYHUB RAG — Nền Tảng Quản Lý Học Tập Đa Môn & Luyện Thi A+ Ứng Dụng RAG AI**.

Dự án của nhóm chúng em kết hợp trọn vẹn cả hai yêu cầu: Phần Assignment về giao diện hiện đại cùng AI Agent, và Phần Project cuối kỳ với kiến trúc phân hệ RAG AI, Google OAuth2 và không gian học nhóm Study Squad. 

Đi cùng em hôm nay là 5 thành viên trong nhóm: anh Lê Thanh Tùng đảm nhiệm vai trò Team Lead điều phối kiến trúc, bạn Nguyễn Văn Gia Bình phụ trách phân hệ RAG Chatbot, bạn Huỳnh Nhật phụ trách Quiz Engine, bạn Lê Nguyễn Minh Thắng phụ trách Study Planner, và bạn Ngô Ân phụ trách Admin Command Center, Quản lý Doanh thu VNPay cùng Đảm bảo chất lượng QA."

---

### [SLIDE 2 — BỐI CẢNH, ĐẶT VẤN ĐỀ & GIẢI PHÁP] (~60 giây)
"Thưa Thầy Cô, xuất phát điểm của đề tài bắt nguồn từ chính nỗi đau thực tế của sinh viên chúng em mỗi mùa ôn thi. 

Khi đối mặt với các môn chuyên ngành như HSF302 hay PRJ301, sinh viên thường bị ngợp trước hàng trăm trang slide và giáo trình PDF cồng kềnh. Khi tìm kiếm sự trợ giúp từ ChatGPT hay các AI thông thường, AI thường xuyên gặp phải hiện tượng 'ảo giác' — tức là tự bịa ra kiến thức, không hề có số trang đối chiếu để sinh viên dám tin cậy. Thêm vào đó, việc tự học một mình rất cô đơn và dễ nản lòng.

Chính vì vậy, nhóm chúng em đã phát triển **Smart StudyHub RAG** với 3 trụ cột giải pháp:
1. Không gian quản lý môn học cá nhân hóa tập trung.
2. Bộ máy RAG AI đối chứng giáo trình 100%, trả lời chuẩn xác kèm số trang thực tế.
3. Và mô hình Study Squad giúp nhóm 5 bạn cùng chia sẻ tài nguyên và thi đua bảng xếp hạng Leaderboard."

---

### [SLIDE 3 — UI TOOLKIT & AI AGENT TRONG IDE (ASSIGNMENT)] (~60 giây)
"Để hiện thực hóa giải pháp trên, nhóm chúng em bắt đầu từ hai tiêu chí cốt lõi của đề bài Assignment:

Thứ nhất, về **UI Toolkit trong Thymeleaf**: Nhóm chúng em lựa chọn **Tailwind CSS kết hợp cùng thư viện DaisyUI v4 và FontAwesome 6** thay vì Bootstrap truyền thống. Tailwind CSS sở hữu cơ chế JIT Engine cực kỳ nhẹ, cho phép chúng em tùy biến sâu toàn bộ giao diện theo phong cách **White-Card Palette** hiện đại, thanh lịch và chống mỏi mắt cho sinh viên. Chúng em áp dụng triệt để nguyên lý DRY qua việc kế thừa layout master `layout/main.html` và bóc tách các Thymeleaf Fragments như `navbar.html`, `sidebar.html`.

Thứ hai, về **Quy trình code cùng AI Agent trong IDE**: Chúng em sử dụng **Google Antigravity IDE** tích hợp **Gemini 2.5 Flash** & **Claude 3.5 Sonnet** theo quy trình 3 bước nghiêm ngặt: **Spec-Driven** định nghĩa đặc tả trước, **Test-Driven Development (TDD)** yêu cầu AI viết test trước khi code, và **Pair-Debugging** cùng AI giải quyết các lỗi concurrency và cách ly dữ liệu."

---

### [SLIDE 4 — AUTHENTICATION, PROFILE & LOGOUT — CHUYỂN MIC] (~60 giây)
"Sau khi đã có nền tảng giao diện, cánh cửa đầu tiên để sinh viên bước vào hệ thống chính là phân hệ **Authentication**.

Hệ thống hỗ trợ cơ chế đăng nhập đa kênh: Sinh viên có thể chọn **Đăng nhập 1 chạm bằng Google OAuth2** theo chuẩn OpenID Connect, tự động đồng bộ email, họ tên và ảnh đại diện Google. Bên cạnh đó, luồng đăng ký tài khoản truyền thống được bảo vệ bằng thuật toán băm **BCrypt** với 10 vòng salt an toàn. Để thuận tiện nhất cho Thầy Cô khi chấm bài, em đã thiết kế thêm các nút **1-Click Login** tự động điền tài khoản cho cả 3 cấp độ Free, Pro và Squad.

Hệ thống bảo vệ phiên bằng `HttpSession`, tự động phân quyền RBAC chặn sinh viên thường truy cập `/admin/*`. Sau khi đăng nhập, sinh viên có thể vào trang Hồ sơ cá nhân (Profile) để theo dõi hạn dùng gói cước, tra cứu hóa đơn điện tử VNPay và thực hiện **Đăng xuất (Logout)** an toàn.

Sau khi người dùng đã bước qua cánh cửa xác thực và bước vào không gian bên trong của StudyHub, em xin trân trọng chuyển mic cho bạn **Lê Thanh Tùng - Team Lead** của nhóm sẽ trình bày Kiến trúc công nghệ, Mô hình 3 gói cước và Không gian học nhóm Shared Squad Hub! Xin mời anh Tùng!"
