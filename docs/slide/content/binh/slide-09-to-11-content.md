# 📑 SLIDE CONTENT: SLIDE 9 ➔ SLIDE 11 (NGUYỄN VĂN GIA BÌNH - RAG ENGINE)
> **Khối trình bày**: Khối 3 — Xử lý PDF (PDFBox), Chunking Engine & RAG AI Chatbot kèm Citation Cards  
> **Thời lượng**: 3 phút  

---

### SLIDE 9: KIẾN TRÚC BÓC TÁCH TÀI LIỆU PDF & PHÂN MẢNH CHUNKS (APACHE PDFBOX)
* **Quy trình Ingestion Pipeline**:
  * Đọc file giáo trình PDF tải lên bằng thư viện mã nguồn mở **Apache PDFBox 3.0.3** (`PDFTextStripper`).
  * Trích xuất toàn bộ cấu trúc văn bản phân trang thực tế từ slide/giáo trình môn học.
* **Thuật toán Chunking thông minh**:
  * Không cắt chữ ngẫu nhiên theo số ký tự cứng nhắc: Cắt đoạn theo ranh giới câu và đoạn văn tự nhiên (độ dài lý tưởng 300 - 500 từ/chunk).
  * Bảo toàn ngữ nghĩa và đính kèm Metadata số trang: Mỗi chunk lưu vào CSDL luôn chứa: `pageNumber`, `documentId`, `courseId`, `content`.
  * Lưu trữ vào `ChunkRepository` để phục vụ truy vấn ngữ cảnh nhanh chóng.

---

### SLIDE 10: CƠ CHẾ CONTEXT GROUNDING & GEMINI FLASH CHATBOT
* **Nguyên lý hoạt động của RAG (Retrieval-Augmented Generation)**:
  1. **User Query**: Sinh viên đặt câu hỏi thắc mắc về một khái niệm trong môn học.
  2. **Retrieval**: Thuật toán tìm kiếm các đoạn Chunks giáo trình có mức độ tương đồng ngữ nghĩa cao nhất.
  3. **Context Injection**: Nhúng toàn bộ các đoạn Chunks liên quan vào System Prompt gửi tới mô hình **Google Gemini 1.5 Flash**.
* **Cam kết Zero Hallucination (Triệt tiêu 100% ảo giác)**:
  * Quy tắc chỉ dẫn (Strict Guardrail): AI bị ràng buộc tuyệt đối chỉ được trả lời dựa trên nội dung tài liệu được cung cấp. Nếu trong giáo trình không có, AI sẽ từ chối trả lời thay vì tự bịa đặt.

---

### SLIDE 11: THẺ TRÍCH DẪN ĐỐI CHỨNG (CITATION CARDS) & MULTI-SESSION CHAT
* **Citation Cards — Tính năng minh chứng đắt giá**:
  * Dưới mỗi câu trả lời của AI, hệ thống tự động sinh ra các thẻ trích dẫn: `📄 [Tên_Tài_Liệu.pdf, Trang: X]`.
  * Sinh viên có thể lật đúng trang sách giáo trình để đối chiếu ngay lập tức, hoàn toàn yên tâm ôn tập.
* **Quản trị đa phiên hội thoại (Multi-Session)**:
  * Nút **"+ Cuộc Trò Chuyện Mới"**: Khởi tạo phiên trò chuyện độc lập, lưu lịch sử chat trực tiếp vào cơ sở dữ liệu.
  * Hỗ trợ format phong phú: Hiển thị bảng Markdown, code block highlight, danh sách bullet point mượt mà.
  * Kiểm soát hạn ngạch gói Free: Đếm ngược tự động 50 câu hỏi/ngày.
* **Ảnh minh chứng đưa vào slide**: Danh sách Chunks có số trang trong DB/UI, Bong bóng chat AI kèm thẻ Citation Cards, Mã code xử lý PDFBox trong IDE.

