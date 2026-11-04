# 🎙️ LỜI THOẠI THUYẾT TRÌNH: SLIDE 9 ➔ SLIDE 11 (NGUYỄN VĂN GIA BÌNH - RAG ENGINE)
> **Khối trình bày**: Khối 3 — Xử lý PDF (PDFBox), Chunking Engine & RAG AI Chatbot kèm Citation Cards  
> **Thời lượng**: 3 phút  
> **Tác phong**: Tự tin, chuyên sâu về kỹ thuật AI & xử lý dữ liệu lớn  

---

### [SLIDE 9 — BÓC TÁCH PDF & PHÂN MẢNH CHUNKS] (~60 giây)
"Em xin cảm ơn bạn Trường. Kính chào Thầy Cô và các bạn, em là **Nguyễn Văn Gia Bình**, phụ trách bộ máy RAG AI và xử lý tài liệu PDF.

Để xây dựng một trợ lý AI học tập thực sự hữu ích, bài toán đầu tiên là làm sao đưa được toàn bộ kiến thức từ hàng trăm trang giáo trình vào hệ thống. Nhóm em đã sử dụng thư viện **Apache PDFBox 3.0.3** để đọc và trích xuất từng trang văn bản từ file PDF mà sinh viên tải lên.

Tuy nhiên, nếu nạp cả quyển sách dài vào mô hình AI thì sẽ vừa tốn token, vừa khiến AI bị loãng thông tin. Vì vậy, em đã cài đặt **Thuật toán Chunking thông minh**: thay vì cắt ngẫu nhiên theo số ký tự cứng nhắc, hệ thống sẽ phân tách văn bản theo ranh giới câu và đoạn văn tự nhiên với độ dài tối ưu từ 300 đến 500 từ. Quan trọng nhất, mỗi đoạn chunk khi lưu vào CSDL luôn được gắn chặt với siêu dữ liệu số trang gốc, sẵn sàng cho bước truy xuất ngữ cảnh."

---

### [SLIDE 10 — CONTEXT GROUNDING & GEMINI FLASH] (~60 giây)
"Bước sang cơ chế hoạt động của bộ máy **RAG (Retrieval-Augmented Generation)**:
Khi một sinh viên gửi một câu hỏi thắc mắc, thuật toán của hệ thống sẽ ngay lập tức truy vấn trong kho dữ liệu để tìm ra các đoạn chunks giáo trình có mức độ liên quan cao nhất.

Sau đó, các đoạn chunks này sẽ được nhúng trực tiếp vào prompt ngữ cảnh để gửi tới mô hình **Google Gemini 1.5 Flash**. Điểm cốt lõi tạo nên sự khác biệt của StudyHub so với ChatGPT chính là cam kết **Zero Hallucination — Triệt tiêu 100% ảo giác**. Chúng em đã thiết lập các quy tắc ràng buộc nghiêm ngặt trong System Prompt, buộc AI chỉ được trả lời dựa trên nội dung giáo trình được cung cấp. Nếu trong tài liệu không có, AI sẽ thông báo rõ ràng cho sinh viên thay vì tự suy diễn sai lệch."

---

### [SLIDE 11 — CITATION CARDS, MULTI-SESSION & CHUYỂN MIC] (~60 giây)
"Và để biến lời nói của AI thành bằng chứng có thể kiểm chứng được, hệ thống tự động gắn kèm các **Citation Cards (Thẻ trích dẫn đối chứng)** bên dưới mỗi câu trả lời. 
Thẻ này chỉ rõ: thông tin này được lấy từ file PDF nào và nằm chính xác ở trang bao nhiêu. Sinh viên chỉ cần lật đúng số trang sách là có thể đối chiếu ngay lập tức, hoàn toàn yên tâm khi ôn thi.

Ngoài ra, hệ thống hỗ trợ quản lý đa phiên hội thoại **Multi-Session**, cho phép tạo mới và lưu trữ lịch sử từng cuộc trò chuyện, tự động hiển thị bảng Markdown biểu đồ mượt mà và kiểm soát hạn ngạch 50 câu/ngày cho tài khoản Free.

Sau khi đã nắm vững lý thuyết qua Chatbot RAG, sinh viên sẽ cần luyện đề thi để kiểm tra kiến thức. Em xin trân trọng kính mời bạn **Huỳnh Nhật** lên trình bày phân hệ AI Quiz Generator. Xin mời Nhật!"

