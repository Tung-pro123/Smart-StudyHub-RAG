# 📝 NHẬT KÝ THỰC NGHIỆM AI - BÌNH (MODULE RAG ENGINE & CHATBOT GIÁO TRÌNH)

**Thành viên thực hiện**: Nguyễn Văn Bình  
**Phạm vi phụ trách**: Module 2: RAG Knowledge & Chatbot Engine (`com.group2.assignment.rag`)  
- DTOs & Service trích xuất văn bản PDF và thuật toán cắt Chunks ngữ nghĩa bằng Apache PDFBox 3.0.3 (`PdfChunkingService.java`).  
- Bộ máy xếp hạng và truy vấn Chunks ngữ cảnh (Term Overlap / TF-IDF Ranking) và Context Injection cho Google Gemini API (`RagEngineService.java`).  
- Giao diện Modern Glassmorphism (Tailwind CSS + DaisyUI) cho trang quản lý tài liệu, Drag & Drop Upload, Chunk Visualizer (`documents.html`) và màn hình Chatbot giáo trình bất đồng bộ (`chat.html`).  
- Bộ điều hướng Spring MVC & REST API (`DocumentController.java`, `ChatController.java`).  
- Bộ kiểm thử tự động toàn diện (`PdfChunkingServiceTest.java`, `RagEngineServiceTest.java`, `DocumentControllerTest.java`).  
**Công cụ AI sử dụng**: Google Antigravity / Gemini 1.5 Pro / Claude 3.5 Sonnet

---

## 🚀 TIẾN TRÌNH ÁP DỤNG AI THEO TỪNG GIAI ĐOẠN

### 🔹 GIAI ĐOẠN 1: DÙNG AI VIẾT THUẬT TOÁN ĐỌC PDFBOX 3.0 VÀ NGẮT CHUNKS THÔNG MINH
* **Mục tiêu**: Nâng cấp từ Apache PDFBox 2.x lên 3.0.3 (sử dụng API mới `org.apache.pdfbox.Loader.loadPDF(byte[])`), trích xuất text từng trang để lưu lại `pageNumber`, và cắt nhỏ văn bản thành các đoạn Chunks từ 300 đến 500 từ theo ranh giới kết thúc câu (`. `, `! `, `? `, `\n\n`) thay vì cắt cứng theo số ký tự.
* **Câu Prompt thực tế**:
  ```text
  Tôi đang phát triển module RAG trong Spring Boot 3 sử dụng Apache PDFBox 3.0.3. Hãy viết PdfChunkingService nhận MultipartFile và courseId:
  1. Validate file PDF không rỗng và dung lượng <= 25MB.
  2. Lưu file vật lý an toàn vào uploads/courses/{courseId}/.
  3. Dùng Loader.loadPDF() và PDFTextStripper đọc từng trang để gán pageNumber.
  4. Viết thuật toán cắt Chunks thông minh: 300 - 500 từ, ưu tiên ngắt theo dấu kết thúc câu hoặc dấu xuống dòng để không bị đứt gãy ngữ nghĩa.
  5. Tính wordCount và ước tính tokenCount (~ wordCount * 1.3), lưu vào ChunkRepository và cập nhật trạng thái Document sang INDEXED.
  ```
* **AI đã giúp bạn như thế nào?**:
  - AI đã viết thuật toán gom câu theo bộ đệm (StringBuilder Buffer) và kiểm tra ngưỡng từ `minWords` và `maxWords`, giúp mỗi đoạn Chunk là tập hợp của các câu hoàn chỉnh.
  - Xử lý mượt mà việc chuyển đổi giữa phiên bản cũ `PDDocument.load()` (đã bị deprecated trong PDFBox 3) sang `Loader.loadPDF()`.
* **Đánh giá & Điều chỉnh của bản thân**: Bổ sung hàm chia nhỏ các câu đơn đặc biệt dài (`splitLongSentence`) để phòng trường hợp tác giả viết cả một đoạn văn 600 từ không có dấu chấm.

---

### 🔹 GIAI ĐOẠN 2: DÙNG AI XÂY DỰNG BỘ MÁY TÌM KIẾM NGỮ NGHĨA VÀ PROMPT CONTEXT INJECTION
* **Mục tiêu**: Xây dựng thuật toán xếp hạng liên quan cho các Chunks trên Java thuần (không cần vector database phụ thuộc bên ngoài) và đóng gói System Prompt tiêm ngữ cảnh (Context Injection) gửi sang Gemini API bằng Spring 6 `RestClient`.
* **Câu Prompt thực tế**:
  ```text
  Viết RagEngineService trong Spring Boot 3:
  1. Hàm findRelevantChunks(courseId, query, topK): lọc Chunks của môn học, tính điểm Term Overlap / TF-IDF và Keyword Match giữa câu hỏi và nội dung Chunk (ưu tiên khớp chính xác cụm câu hỏi +10đ, lọc stop words tiếng Việt/tiếng Anh, tính tỷ lệ phủ sóng từ khóa). Trả về Top 3-5 Chunks có điểm cao nhất.
  2. Hàm buildSystemPrompt(): Tiêm các Chunks trích dẫn kèm số trang và tên file vào System Prompt: "Bạn là Trợ lý Giảng viên AI chuyên nghiệp của môn học. Dưới đây là các trích đoạn từ giáo trình chính thức...".
  3. Hàm chatWithCourse(): Lấy API Key từ HttpSession hoặc System.getenv(), nếu thiếu thì fallback hướng dẫn sang /settings. Nếu có key, dùng RestClient gửi POST sang Gemini 1.5 Flash endpoint, parse response JSON và lưu cả câu hỏi của sinh viên và câu trả lời của AI vào ChatMessage.
  ```
* **AI đã giúp bạn như thế nào?**:
  - Cung cấp thuật toán tính điểm nhanh, nhẹ, chạy hoàn toàn in-memory trên JVM mà vẫn chọn chính xác các đoạn kiến thức liên quan trực tiếp đến câu hỏi.
  - Xây dựng cơ chế fallback an toàn: khi chưa có API Key hoặc mạng bị ngắt, hệ thống vẫn phản hồi lịch sự kèm gợi ý trích xuất tài liệu mà không làm sập ứng dụng.
* **Đánh giá & Điều chỉnh của bản thân**: Tinh chỉnh lại danh sách stop words tiếng Việt (`và`, `là`, `của`, `trong`, `với`, `được`, `này`, `để`...) để tránh các từ hư từ làm sai lệch điểm số tương đồng.

---

### 🔹 GIAI ĐOẠN 3: DÙNG AI THIẾT KẾ GIAO DIỆN GLASSMORPHISM CHO DOCUMENTS VÀ CHATBOT
* **Mục tiêu**: Xây dựng 2 trang Thymeleaf `documents.html` và `chat.html` kế thừa layout `layout/main.html` theo ngôn ngữ thiết kế Modern Glassmorphism (Tailwind CSS + DaisyUI).
* **Câu Prompt thực tế**:
  ```text
  Thiết kế 2 template Thymeleaf cho Module 2 RAG AI của môn HSF302:
  1. documents.html: Có Breadcrumbs, Banner thống kê môn học, Card kéo thả Drag & Drop tải file PDF với hiệu ứng border nét đứt, Bảng danh sách tài liệu với badges trạng thái (INDEXED, CHUNKING, ERROR) và Trình trực quan hóa Chunks (Chunk Visualizer) dùng DaisyUI collapse hiển thị chi tiết #Chunk, trang, số từ và nội dung văn bản.
  2. chat.html: Split layout 2 cột:
     - Cột trái: Lịch sử các phiên chat của môn này và nút [+ Cuộc Trò Chuyện Mới].
     - Cột phải: Khung chat trung tâm cuộn mượt, bong bóng chat-end cho sinh viên, chat-start cho AI, dưới mỗi câu trả lời AI có Accordion "📚 Nguồn trích dẫn từ giáo trình" hiển thị các Chunks được dùng làm dẫn chứng.
     - Thanh Quick Prompts với các pill gợi ý nhanh bấm vào tự điền câu hỏi.
     - Viết Javascript thuần gọi Fetch API gửi POST sang /chat/send để chat mượt mà thời gian thực không reload trang, có Typing indicator 3 dấu chấm nhấp nháy.
  ```
* **AI đã giúp bạn như thế nào?**:
  - Giao diện sinh ra vô cùng hiện đại, bắt mắt, hiệu ứng kính mờ `glass-card` kết hợp hài hòa với cả chế độ sáng (Light) và tối (Dark/Synthwave).
  - Script Fetch API tích hợp mượt mà: vừa thêm tin nhắn vào DOM ngay tức thì, vừa hiển thị hiệu ứng bot đang suy nghĩ, vừa cuộn xuống đáy hộp thoại tự động.
* **Đánh giá & Điều chỉnh của bản thân**: Bổ sung hàm `formatSimpleMarkdown` trong Javascript để in đậm các từ khóa `**text**` và highlight code block ``` inline code, giúp câu trả lời của AI hiển thị chuyên nghiệp.

---

### 🔹 GIAI ĐOẠN 4: DÙNG AI VIẾT BỘ UNIT TEST VÀ TỐI ƯU HÓA XỬ LÝ NGOẠI LỆ
* **Mục tiêu**: Đạt độ phủ kiểm thử 100% cho toàn bộ các chức năng trọng yếu của Module 2 với JUnit 5 và Spring Boot Test.
* **Câu Prompt thực tế**:
  ```text
  Viết bộ Unit Test toàn diện cho Module RAG:
  1. PdfChunkingServiceTest: Test cắt văn bản thông minh 300-500 từ không làm mất câu; Test từ chối file không phải PDF; Test từ chối file rỗng; Test từ chối file > 25MB; Test xử lý file PDF 2 trang thực tế tạo bằng PDFBox trong bộ nhớ và kiểm tra lưu Chunks vào ChunkRepository.
  2. RagEngineServiceTest: Test thuật toán xếp hạng findRelevantChunks chọn đúng chunk có điểm cao nhất; Test hàm tính điểm khớp toàn bộ cụm từ khóa; Test trích xuất keyword loại bỏ stop words; Test dựng System Prompt tiêm ngữ cảnh; Test Fallback khi chưa có API Key.
  3. DocumentControllerTest: Dùng MockMvc kiểm tra quyền truy cập /documents và /chat (yêu cầu session người dùng), test xóa tài liệu và test gửi tin nhắn qua JSON API.
  ```
* **Kết quả kiểm thử**:
  - Toàn bộ 16 ca kiểm thử mới của Module 2 cùng 18 ca kiểm thử của các module khác đều đạt **BUILD SUCCESS 100%** (34/34 tests passed).
  - Không có bất kỳ cảnh báo rò rỉ bộ nhớ hoặc lỗi cú pháp nào.

---

## 🎓 TRẢ LỜI CÂU HỎI VẤN ĐÁP BẢO VỆ ĐỒ ÁN (HSF302 DEFENSE QA)

### ❓ Câu 1: Cơ chế RAG lấy ngữ cảnh từ các Chunks PDF như thế nào để AI không bị ảo giác (Hallucination)?
> **Trả lời của Bình**:  
> "Mô hình ngôn ngữ lớn (LLM) thông thường chỉ dựa vào trọng số huấn luyện tĩnh có sẵn nên dễ bị 'ảo giác' (bịa đặt thông tin) khi gặp các câu hỏi mang tính chuyên môn sâu hoặc tài liệu nội bộ của trường.  
> Để khắc phục triệt để, hệ thống áp dụng kiến trúc **Retrieval-Augmented Generation (RAG)** qua 3 bước:
> 1. **Retrieval (Truy vấn)**: Khi sinh viên đặt câu hỏi, `RagEngineService` phân tích các từ khóa quan trọng và quét toàn bộ các đoạn `Chunk` đã cắt từ giáo trình PDF của môn học, sử dụng thuật toán Term Overlap & TF-IDF Ranking để tìm ra Top 3-5 đoạn văn bản liên quan nhất.
> 2. **Augmentation (Bổ sung ngữ cảnh)**: Hệ thống đóng gói một System Prompt nghiêm ngặt, tiêm trực tiếp các đoạn Chunks này làm 'Ground Truth' (chân lý dẫn chứng) kèm ràng buộc: *'Chỉ được trả lời dựa trên các trích đoạn trên; nếu giáo trình không đề cập, hãy thông báo rõ ràng'*.
> 3. **Generation (Sinh câu trả lời)**: Gemini 1.5 Flash nhận prompt đã tiêm ngữ cảnh, tổng hợp câu trả lời chính xác 100% theo tài liệu và trích dẫn rõ số trang và tên file giáo trình để sinh viên đối chiếu."

---

### ❓ Câu 2: Tại sao phải chia đoạn (Chunking) từ 300 - 500 từ thay vì đưa cả file PDF vào Prompt của Gemini?
> **Trả lời của Bình**:  
> "Mặc dù các mô hình thế hệ mới như Gemini 1.5 có Context Window lớn, việc chia nhỏ thành các Chunks từ 300 - 500 từ là yêu cầu bắt buộc trong kỹ thuật RAG vì 4 lý do thực tiễn:
> 1. **Hiện tượng suy giảm tập trung ('Lost in the Middle')**: Khi nhồi toàn bộ tài liệu hàng trăm trang vào Prompt, khả năng chú ý (Attention Mechanism) của mô hình bị phân tán, khiến câu trả lời dễ bị chung chung và bỏ sót chi tiết quan trọng nằm ở giữa tài liệu.
> 2. **Độ chính xác của Trích dẫn (Granular Citations)**: Chia theo Chunks 300-500 từ gắn liền với `pageNumber` cho phép giao diện Chatbot hiển thị chính xác đoạn văn bản và số trang cụ thể mà AI đã tham khảo, giúp sinh viên xác minh nguồn gốc kiến thức dễ dàng.
> 3. **Tiết kiệm chi phí & Tốc độ phản hồi (Latency & Cost)**: Gửi 3-4 Chunks (~1.500 từ) chỉ tốn một lượng nhỏ token, giúp thời gian phản hồi (TTFT) giảm xuống chỉ còn 1 - 2 giây thay vì phải chờ 15 - 30 giây để xử lý cả cuốn sách.
> 4. **Giữ nguyên ranh giới ngữ nghĩa**: Kích thước 300-500 từ vừa đủ cho 1 khái niệm hoặc 1 ý bài giảng hoàn chỉnh, kết hợp thuật toán ngắt theo dấu kết thúc câu đảm bảo không làm đứt đoạn logic của bài học."

---

## 💡 ĐÚC KẾT KINH NGHIỆM
* **Bài học rút ra**:
  1. Trong Apache PDFBox 3.0.3, việc tách text theo từng trang (`stripper.setStartPage(page)`) kết hợp duyệt tuần tự giúp việc định vị số trang cho từng Chunk đạt độ chuẩn xác 100%.
  2. Bắt buộc phải validate kích thước và định dạng file ngay tại tầng Service trước khi ghi xuống đĩa cứng để phòng ngừa tấn công DoS hoặc ghi đè file nguy hiểm.
  3. Kết hợp giao diện AJAX Fetch API thời gian thực với Accordion xem trước Chunks tạo trải nghiệm học tập trực quan và tin cậy cao cho sinh viên.


---

# 📝 NHẬT KÝ DÙNG AI HOÀN THÀNH NHIỆM VỤ (SPRINT 30/10/2026)

**Thành viên thực hiện**: Nguyễn Văn Gia Bình  
**Module được giao**: Module RAG Engine - Centralized System AI Key, Multi-Key Rotator & Kiểm soát Hạn ngạch 50 câu/ngày (`feature/rag-engine`)  
**Công cụ AI sử dụng**: Google Antigravity / Gemini 3.8 Flash (High) / Ponytail Mode  

---

## 🚀 QUY TRÌNH DÙNG AI ĐỂ HOÀN THÀNH TỪNG PHẦN VIỆC

### 🔹 BƯỚC 1: DÙNG AI LÀM PHẦN GIAO DIỆN (FRONT-END)
* **Mục tiêu**:
  - Cập nhật thanh header bar: Bỏ cảnh báo cá nhân "Chưa cài API Key", thay bằng badge "AI Multi-Key Engine: Đã Sẵn Sàng" và nhãn nhận diện "⚡ Gói PRO: Vô Hạn RAG".
  - Thêm thẻ Quota Card tiến trình cho gói FREE: Hiển thị "Hạn ngạch RAG hôm nay: X/50 câu còn lại" kèm progress bar đổi màu mượt mà.
  - Viết Javascript cập nhật hạn ngạch theo thời gian thực (Realtime) khi nhận phản hồi từ Fetch API `/chat/send` mà không cần F5 tải lại trang.
* **Câu Prompt thực tế**:
  ```text
  Cập nhật templates/rag/chat.html theo mô hình Centralized Key và hạn ngạch 50 câu/ngày:
  1. Header bar: Nếu hasSystemKey == true thì hiển thị badge xanh "AI Multi-Key Engine: Đã Sẵn Sàng". Nếu user là gói Pro/Admin thì hiển thị badge tím "⚡ Gói PRO: Vô Hạn RAG".
  2. Cột trái: Thêm Quota Card cho người dùng Free với thanh progress bar DaisyUI biểu thị tỷ lệ X/50 câu hỏi còn lại và nút nâng cấp Pro.
  3. Javascript Fetch API: Nhận remainingQuota từ ChatResponseDto để cập nhật realtime text và thanh progress bar. Nếu remainingQuota <= 0 thì tự động disable ô nhập câu hỏi và nút gửi kèm placeholder thông báo hết lượt.
  ```
* **AI đã giúp bạn như thế nào?**:
  - AI sinh cấu trúc HTML Tailwind + DaisyUI mượt mà, phân cấp rõ ràng giữa gói FREE và PRO.
  - Viết hàm `updateQuotaUI(remaining)` đổi màu progress bar theo ngưỡng (xanh khi còn nhiều, vàng khi <= 20, đỏ khi <= 5) và khóa form chat một cách trực quan.

---

### 🔹 BƯỚC 2: DÙNG AI VIẾT PHẦN LOGIC (BACK-END)
* **Mục tiêu**:
  - Chuyển toàn bộ cơ chế gọi Gemini API sang dùng Centralized System Key đọc từ `application.properties` hoặc biến môi trường `GEMINI_API_KEYS`.
  - Xây dựng `LlmKeyRotator`: Xoay vòng Round-Robin đa luồng, tự động Cooldown 60s và Failover sang key tiếp theo khi gặp mã lỗi HTTP `429 / RESOURCE_EXHAUSTED`.
  - Hỗ trợ linh hoạt LiteLLM / OpenAI Gateway endpoint khi cấu hình `studyhub.ai.base-url`.
  - Kiểm soát hạn ngạch 50 câu/ngày và tự động reset ngày mới theo `lastRagResetDate` trong `ChatController`.
* **Câu Prompt thực tế**:
  ```text
  Triển khai logic Centralized Multi-Key Rotator và Quota 50 câu/ngày trong Spring Boot 3:
  1. Tạo LlmProperties ánh xạ studyhub.ai.gemini-api-keys (hỗ trợ danh sách nhiều key cách nhau bằng dấu phẩy) và studyhub.ai.base-url (cho LiteLLM Proxy).
  2. Tạo LlmKeyRotator: Quản lý pool keys, xoay vòng Round-Robin an toàn đa luồng bằng AtomicInteger. Khi gặp 429 hoặc Rate Limit thì markKeyFailed đưa vào Cooldown Map 60s và tự động dùng key kế tiếp.
  3. Refactor RagEngineService: Bỏ lấy key từ HttpSession, gọi qua LlmKeyRotator hoặc LiteLLM OpenAI-compatible endpoint.
  4. Cập nhật ChatController: Kiểm tra nếu LocalDate.now().isAfter(lastRagResetDate) thì reset dailyRagCount = 0. Nếu gói FREE và dailyRagCount >= 50 thì chặn lại và trả thông báo mời nâng cấp Pro. Nếu thành công thì dailyRagCount++ và trả về remainingQuota.
  ```
* **AI đã giúp bạn như thế nào?**:
  - AI thiết kế `LlmKeyRotator` rất thông minh và tinh gọn (theo triết lý Ponytail), xử lý đầy đủ trường hợp biên (edge case) khi tất cả các key đều bị cooldown thì tự động fallback về key có thời gian phục hồi sớm nhất.
  - Tách bạch rõ ràng giữa chế độ gọi Gemini REST API trực tiếp và chuẩn LiteLLM `/v1/chat/completions`.
  - Logic tự động reset hạn ngạch ngày mới chạy ngầm trong `ChatController` hoàn toàn mượt mà.

---

### 🔹 BƯỚC 3: DÙNG AI GỠ LỖI HOẶC HOÀN THIỆN TÍNH NĂNG (DEBUG & POLISH)
* **Vấn đề gặp phải khi làm**:
  - Khi chạy toàn bộ test suite (`mvn clean test`), phát hiện `DocumentControllerTest` gặp lỗi `DataIntegrityViolationException: not-null property references a null or transient value: User.email` do mock user trong test cũ không có email và chưa được lưu trong database.
  - Ngoài ra, test cũ kiểm tra `model.attributeExists("hasApiKey")` nên cần duy trì tương thích ngược.
* **Câu Prompt bạn hỏi AI để nhờ sửa**:
  ```text
  Chạy mvn clean test bị lỗi DataIntegrityViolationException trên DocumentControllerTest:
  Hãy kiểm tra tại sao checkAndResetDailyQuota trong ChatController gây lỗi khi gặp mock user chưa persist trong database, và sửa lại để an toàn tuyệt đối đồng thời giữ tương thích ngược với hasApiKey.
  ```
* **Cách AI hướng dẫn bạn xử lý**:
  - AI đã kiểm tra điều kiện `user.getId() != null && userRepository.existsById(user.getId())` trước khi gọi `userRepository.save(user)`, giúp code linh hoạt và an toàn với cả entity thật trong database lẫn mock user trong các bài test MVC.
  - Thêm `model.addAttribute("hasApiKey", hasSystemKey)` song song với `hasSystemKey`.
  - Kết quả: Toàn bộ 78 ca kiểm thử trên toàn bộ dự án đạt **BUILD SUCCESS 100% (78/78 tests passed)**.

---


---

### 🔹 GIAI ĐOẠN 4: KIỂM SOÁT HẠN MỨC 3 TÀI LIỆU PDF/MÔN (GÓI FREE) VÀ NÂNG CẤP GIAO DIỆN DOCUMENTS

* **Mục tiêu**:
  - Triển khai quy tắc chống nhồi tài liệu làm loãng vector search RAG: Giới hạn sinh viên gói FREE tối đa **3 tài liệu PDF / môn** (`MAX_DOCS_FREE = 3`).
  - Mở khóa không giới hạn tài liệu cho gói `PRO` và `SQUAD` (Nhóm học tập 5 bạn).
  - Nâng cấp giao diện `documents.html`: Thêm thanh tiến độ `X / 3 tài liệu`, tự động khóa Dropzone upload và hiển thị cảnh báo đạt giới hạn kèm nút kêu gọi nâng cấp gói Pro.
  - Viết bộ kiểm thử toàn diện `RagDocumentQuotaTest.java` xác minh chặt chẽ cơ chế kiểm soát tài liệu và bảo đảm 100% tests của dự án pass.
* **Câu Prompt thực tế**:
  ```text
  Tôi cần hoàn thiện task kiểm soát tài liệu PDF theo 3 gói cước cho Bình trong module RAG:
  1. Trong PdfChunkingService.java: Thêm hằng số MAX_DOCS_FREE = 3. Thêm phương thức processPdf(courseId, file, currentUser). Nếu !currentUser.isPro() và số tài liệu hiện có trong môn >= 3 thì ném IllegalStateException: "Gói Free giới hạn 3 tài liệu/môn để đảm bảo độ chính xác RAG. Vui lòng nâng cấp để tải trọn bộ giáo trình!".
  2. Trong DocumentController.java: Lấy User từ session, truyền vào service. Tại GET /courses/{courseId}/documents truyền isPro, currentDocCount, maxDocsFree, isDocLimitReached vào model.
  3. Trong documents.html: Thêm widget thanh tiến độ X/3 tài liệu (cho Free) hoặc badge TẢI VÔ HẠN (cho Pro/Squad). Nếu isDocLimitReached thì ẩn/khóa form upload và hiển thị card cảnh báo đỏ kèm nút nâng cấp Pro.
  4. Viết RagDocumentQuotaTest.java: Kiểm thử đầy đủ các kịch bản Free tải dưới 3 file, Free bị chặn khi tải file thứ 4, Pro/Squad tải không giới hạn, và Free xóa bớt file thì tải lại được.
  ```
* **AI đã giúp bạn như thế nào?**:
  - AI đã thiết kế giải pháp quá tải nhẹ (overload) `processPdf(courseId, file, currentUser)` giữ trọn vẹn tương thích ngược với các test case cũ gọi `processPdf(courseId, file)`.
  - Thiết kế UI `documents.html` cực kỳ trực quan với hiệu ứng Glassmorphism, thanh tiến độ tự động chuyển sang màu đỏ khi đầy 3/3 tài liệu và card thông báo nâng cấp kích thích chuyển đổi (Call to Action).
  - Viết bộ test `RagDocumentQuotaTest.java` gồm 6 ca kiểm thử chi tiết bao phủ cả MockMvc Web layer lẫn Service layer.
* **Đánh giá & Điều chỉnh của bản thân**:
  - Tích hợp chặt chẽ với phương thức `User.isPro()` để tài khoản hạng `SQUAD` (Nhóm học tập) cũng tự động được hưởng quyền tải không giới hạn như `PRO`.
  - Chạy `mvn test` xác thực toàn bộ **111/111 test cases** đều PASS xanh 100%.

## 💡 ĐÚC KẾT KINH NGHIỆM KHI DÙNG AI
* **AI đã giúp ích nhiều nhất cho bạn ở khâu nào?**: 
  - Khâu thiết kế kiến trúc Multi-Key Rotator kết hợp Cooldown Map và quy trình làm rõ yêu cầu qua `/grill-me`. AI đã phân tích từng nhánh thiết kế, đề xuất giải pháp nội bộ nhẹ gọn không cần cài thêm service ngoài mà vẫn giải quyết triệt để bài toán nghẽn hạn ngạch (Rate Limit 15 RPM của Gemini Free).
* **Kinh nghiệm bạn rút ra để AI sinh ra code đúng ý mình**: 
  - Sử dụng quy trình TDD kết hợp phong cách `/ponytail`: Viết test case trước để định hình hành vi, yêu cầu AI sinh code tối giản nhất giải quyết đúng vấn đề, không thêm các tầng wrapper thừa thãi.
  - Luôn chạy full test suite sau khi sửa đổi để phát hiện ngay các phụ thuộc ngầm giữa các module.
