# 03. QUY TẮC LÀM VIỆC NHÓM (TEAM WORKING RULES & CULTURE)

Bản quy tắc này được lập ra nhằm giúp 6 thành viên phối hợp nhịp nhàng, đúng tiến độ, bảo đảm tinh thần trách nhiệm và đạt kết quả cao nhất cho môn học HSF302.

---

## ⏰ 1. QUY ĐỊNH VỀ THỜI GIAN & TIẾN ĐỘ (DEADLINES)

1. **Tuân thủ Deadline nội bộ**:
   * Deadline của nhóm luôn được đặt **trước hạn nộp của giảng viên từ 2 đến 3 ngày**.
   * Khoảng thời gian dôi ra dùng để: Chạy thử toàn diện (Cross-testing), sửa lỗi phát sinh, tổng duyệt slide và quay clip demo dự phòng.
2. **Quy tắc Báo cáo Tiến độ (Check-in)**:
   * **Thời gian chốt**: Đúng **21h00 trước ngày học 1 ngày** (ví dụ học Thứ 3 thì 21h00 tối Thứ 2 báo cáo).
   * Mỗi bạn nhắn 1 tin nhắn ngắn gọn vào nhóm chung theo mẫu 3 câu:
     * *Đã hoàn thành phần việc nào trong tuần/buổi qua?*
     * *Dự định làm gì tiếp theo trước buổi học tới?*
     * *Có gặp khó khăn hay vướng mắc gì không?*
3. **Quy tắc Báo "Kẹt việc" (Blocker Alert - Quy tắc 4 giờ)**:
   * Nếu bạn gặp lỗi lập trình (lỗi Spring Boot, CSS bị vỡ, lỗi gọi API...) và đã tự mò quá **4 tiếng** mà không giải quyết được:
   * 👉 **Phải lập tức nhắn vào nhóm** để các bạn khác hoặc AI hỗ trợ, **tuyệt đối không im lặng giấu lỗi** dẫn đến trễ cả dự án!

---

## 💬 2. KÊNH GIAO TIẾP & PHẢN HỒI (COMMUNICATION)

* **Kênh trao đổi chính**: Nhóm Chat Zalo / Discord.
* **Quy tắc phản hồi**:
  * Khi có thông báo quan trọng gắn thẻ `@all`, các thành viên cần thả biểu cảm (👍/❤️) hoặc phản hồi trong vòng **tối đa 4 tiếng** (trừ giờ ngủ đêm).
  * Trong trường hợp bận việc gia đình, đi làm thêm hoặc ốm đau: Phải thông báo trước cho Trưởng nhóm ít nhất nửa ngày để san sẻ công việc.
* **Văn hóa góp ý**:
  * Tập trung vào giải quyết vấn đề (Problem-solving), không chỉ trích cá nhân.
  * Tôn trọng ý kiến đóng góp của từng thành viên.

---

## 💻 3. TIÊU CHUẨN CODE CHUNG (CODING CONVENTIONS)

Để code của 6 người ghép lại trông như do 1 người viết:

### Đối với Java / Spring Boot:
* **Tên Class**: Viết hoa chữ cái đầu theo PascalCase (`ChatController`, `GeminiAiService`, `MessageDto`).
* **Tên Hàm & Biến**: Viết theo camelCase (`sendMessage`, `apiKey`, `isSuccess`).
* **Tên Package**: Toàn bộ viết chữ thường (`com.tung.assignment.controller`, `com.tung.assignment.service`).
* **Cấu trúc tầng rõ ràng**:
  * Controller: Chỉ điều hướng, không viết logic nặng tại đây.
  * Service: Chứa logic gọi API và xử lý dữ liệu.
  * DTO/Model: Chứa dữ liệu truyền qua lại giữa View và Backend.

### Đối với HTML / Thymeleaf & CSS:
* **Định dạng file**: Tất cả các file HTML đặt trong `src/main/resources/templates/`.
* **Cú pháp Thymeleaf**: Sử dụng tiền tố chuẩn `th:text`, `th:each`, `th:replace`, `th:field`.
* **Class CSS**: Sử dụng chuẩn của Tailwind CSS và DaisyUI, hạn chế viết CSS nội tuyến `style="..."` nếu không thực sự cần thiết.

---

## 🛡️ 4. CHÍNH SÁCH ĐÁNH GIÁ ĐÓNG GÓP (CONTRIBUTION REVIEW)

* Vào cuối giai đoạn, nhóm sẽ cùng ngồi lại đánh giá mức độ hoàn thành nhiệm vụ của từng người (Peer Assessment %):
  * **100%**: Hoàn thành đúng hạn, code/tài liệu chất lượng, nhiệt tình hỗ trợ đồng đội.
  * **Dưới 80%**: Thường xuyên trễ hẹn không báo trước, làm đối phó, không tương tác trong nhóm.
* Mức đánh giá này sẽ được phản ánh trung thực vào bảng phân công nộp cho giảng viên bộ môn.
