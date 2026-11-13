# Ôn Tập Bảo Vệ Phân Hệ RAG & PDF Chunking (Bình — HSF302)

> Bộ câu hỏi ôn tập theo mạch: **Nghiệp vụ → Code thực tế (Controller / Service / JPA) → Cách thiết kế & Bẫy phản biện** cho phần **Chunking PDF & RAG Chat**.

---

## PHẦN 1: Thầy hỏi về NGHIỆP VỤ (Business Logic & Guard)

**1. Tại sao không gửi thẳng toàn bộ file PDF cho AI mà phải chia thành các `Chunk` 300–500 từ kèm `pageNumber`?**
* **Trả lời:**
  * Giáo trình dài nhiều trang, gửi hết vào prompt sẽ vượt giới hạn context/token của LLM, gây chậm, tốn chi phí và làm AI dễ bỏ sót chi tiết (loãng ngữ cảnh).
  * Chia nhỏ **300–500 từ** giúp truy xuất đúng **Top-4 đoạn** liên quan nhất nhét vào prompt, đồng thời lưu `pageNumber` giúp AI và giao diện trả về đúng **số trang trích dẫn (Citations)** để sinh viên đối chiếu lại slide gốc.

**2. Khi người dùng upload file lỗi (không phải `.pdf`, quá 25MB, hoặc PDF dạng ảnh scan) thì hệ thống xử lý thế nào?**
* **Trả lời** (mở `PdfChunkingService.java` — `validateFile()` & `processPdf()`):
  * `validateFile()` chặn ngay nếu file rỗng, không có đuôi `.pdf` hoặc vượt **`25MB`** (`MAX_FILE_SIZE`).
  * Khi tách text bằng `PDFTextStripper`, nếu tất cả các trang đều rỗng (`chunksToSave.isEmpty()`) tức là PDF ảnh quét chưa OCR -> ném `IllegalArgumentException`, đồng thời cập nhật `Document.status = "ERROR"` và lưu `errorMessage` vào DB để dễ theo dõi.

**3. Hạn mức Free Tier và phân quyền xóa tài liệu nhóm (`squadShared`) được kiểm soát ở đâu?**
* **Trả lời:**
  * **Upload PDF**: Trong `PdfChunkingService.processPdf()`, nếu user không phải Pro (`!currentUser.isPro()`), hệ thống đếm `documentRepository.countByCourseId(courseId)` và chặn ở mức **tối đa 3 tài liệu/môn** (`MAX_DOCS_FREE = 3`).
  * **Xóa PDF**: Trong `DocumentController.deleteDocument()`, bắt buộc đăng nhập (`HttpSession`); nếu môn học có `squadShared = true`, chỉ `ADMIN` hoặc `SQUAD_LEADER` mới được xóa.
  * **Chat RAG**: Trong `ChatController.sendChatMessageJson()` / `sendChatMessageForm()`, tự reset `dailyRagCount` về `0` khi qua ngày mới (`checkAndResetDailyQuota()`), và chặn nếu user Free đã dùng đạt **50 câu/ngày** (`dailyCount >= 50`).

**4. Nếu giáo trình không đề cập câu hỏi đó hoặc môn chưa có tài liệu thì chatbot xử lý sao để không bịa (hallucination)?**
* **Trả lời** (mở `RagEngineService.buildSystemPrompt()`):
  * Nếu `chunks` rỗng, prompt chèn rõ dòng `[Hiện tại môn học chưa có tài liệu giáo trình nào được tải lên]`.
  * Nếu có chunks, prompt ép 3 quy tắc: chỉ dựa vào trích đoạn cung cấp để trả lời, cuối câu liệt kê mục `'Trích dẫn tài liệu'` kèm trang & tên file, và **nếu tài liệu không có thông tin thì phải thông báo lịch sự là giáo trình không đề cập**.

---

## PHẦN 2: Thầy hỏi về CODE & SPRING / JPA (Trọng tâm HSF302)

**5. Mở Entity `Document`, `Chunk`, `ChatSession`, `ChatMessage`: giải thích các annotation JPA đang dùng?**
* **Trả lời:**
  * Dùng `@ManyToOne(fetch = FetchType.LAZY)` trỏ tới `Course` / `Document` / `ChatSession` để tránh load dư dữ liệu thừa khi query danh sách.
  * Dùng `@OnDelete(action = OnDeleteAction.CASCADE)` của Hibernate để ràng buộc xóa tầng ở mức DB (xóa `Course` thì xóa `Document`, `Chunk`, `ChatSession`; xóa `Document` thì xóa `Chunk`).
  * Dùng `@Lob` với `columnDefinition = "CLOB"` cho `Chunk.content`, `Chunk.embeddingVector`, `ChatMessage.content`, `ChatMessage.ragContext` vì văn bản dài vượt giới hạn `VARCHAR(255)` mặc định.
  * Dùng `@PrePersist` / `@PreUpdate` để tự gán timestamp và giá trị mặc định (`status = "UPLOADED"`, `hasEmbedding = false`, `pageNumber = 1`).

**6. Tại sao trong `PdfChunkingService.deleteDocument()` có `@Transactional`, và xóa theo thứ tự nào?**
* **Trả lời:**
  * Xóa theo 3 bước: xóa file vật lý PDF trong `./uploads/courses/{courseId}/` bằng `Files.deleteIfExists()` → xóa hết chunks con (`chunkRepository.deleteAllByDocumentId(documentId)`) → xóa bản ghi cha `documentRepository.delete(document)`.
  * Bắt buộc đặt `@Transactional` vì `deleteAllByDocumentId` là thao tác xóa nhiều bản ghi trong Spring Data JPA cần nằm trong một transaction; nếu bước DB lỗi sẽ rollback, không để lại dữ liệu mồ côi.

**7. Tại sao trong `ChatController` lại có tới 2 hàm `@PostMapping("/chat/send")`?**
* **Trả lời:**
  * Hàm `sendChatMessageJson` (`consumes = MediaType.APPLICATION_JSON_VALUE`, `@ResponseBody`) phục vụ giao diện `chat.html` gửi bất đồng bộ bằng `fetch('/courses/{id}/chat/send')` để cập nhật tin nhắn, quota realtime và render Markdown bằng `marked.js` mà không reload trang.
  * Hàm `sendChatMessageForm` (`consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE`) là fallback cho form HTML submit truyền thống hoặc client không dùng JSON.
  * Ngoài ra `getCurrentUser()` query lại từ `userRepository.findById(...)` thay vì chỉ tin dữ liệu cũ trong `HttpSession` để đảm bảo `dailyRagCount` và gói cước luôn đồng bộ với DB.

**8. Em dùng `@Transactional` trên `RagEngineService.chatWithCourse()` — khi gọi LLM bên ngoài qua mạng ngay trong transaction như vậy có nhược điểm gì không?** *(Câu hỏi nâng cao)*
* **Trả lời thẳng thắn để ghi điểm:**
  * Ưu điểm hiện tại là tạo `ChatSession` và lưu cặp tin nhắn `USER` + `AI` gọn trong một luồng giao dịch nhất quán.
  * Nhược điểm nếu tải cao là giữ kết nối DB (HikariCP connection) trong lúc chờ HTTP call tới Ollama/Gemini; nếu tối ưu tiếp có thể tách bước đọc DB (`findRelevantChunks`) và bước lưu `saveMessages()` thành 2 transaction nhỏ, để lời gọi HTTP `callLlmWithRotation()` nằm ngoài transaction.

---

## PHẦN 3: Thầy hỏi về CÁCH LÀM & CÁC "BẪY" HAY BỊ VẶN

**9. [BẪY LỚN NHẤT] "Em nói làm RAG, sao trong `Chunk.java` có `embeddingVector` và `hasEmbedding` mà toàn bộ `hasEmbedding` lại là `false`? Vậy hệ thống tìm chunk kiểu gì?"**
* **Cách trả lời chuẩn (tuyệt đối không nói bừa là đang dùng Vector Search):**
  * Thú nhận đúng thực tế code: nhóm có thiết kế sẵn schema `embeddingVector` (`CLOB`) và `hasEmbedding` để mở rộng sau, nhưng bản chạy hiện tại **chưa dùng Vector DB hay gọi embedding search** (`hasEmbedding = false`).
  * Hiện tại `RagEngineService.findRelevantChunks()` dùng thuật toán **Lexical Relevance Scoring** thuần Java (chi phí $0, chạy tức thì):
    1. Chuyển câu hỏi về chữ thường, tách từ và lọc stop words tiếng Việt + tiếng Anh (`extractKeywords()`).
    2. Chấm điểm từng chunk của môn học (`calculateRelevanceScore()`): khớp nguyên văn cả cụm câu hỏi **+10 điểm**; mỗi từ khóa khớp cộng `1.0 + Math.log(1.0 + occurrences)`; cộng thêm tỷ lệ độ phủ từ khóa `coverage * 3.0`.
    3. Sắp xếp giảm dần, lấy **Top-4 chunks** có điểm > 0; nếu không khớp từ khóa nào thì **fallback lấy 4 chunk đầu tiên** của môn.

**10. Thuật toán cắt chunk 300–500 từ trong `splitTextIntoChunks()` làm thế nào để không cắt ngang giữa một câu?**
* **Trả lời** (mở `PdfChunkingService.java` dòng 184–239):
  * Đầu tiên dùng `PDFTextStripper` với `setSortByPosition(true)` đọc **lần lượt từng trang** (`setStartPage(page)` / `setEndPage(page)`) để biết chính xác mỗi chunk thuộc trang nào.
  * Trong mỗi trang, nếu tổng số từ `<= 500` (`DEFAULT_MAX_WORDS`) thì giữ nguyên 1 chunk.
  * Nếu dài hơn, tách văn bản theo ranh giới câu/xuống dòng bằng regex `(?<=[.!?\\n])\\s+`, rồi gom dần từng câu vào buffer; chỉ ngắt sang chunk mới khi buffer đã đạt tối thiểu `300` từ (`DEFAULT_MIN_WORDS`) và thêm câu tiếp theo sẽ vượt `500` từ. Trường hợp cá biệt 1 câu dài hơn `500` từ mới gọi `splitLongSentence()` cắt theo từ.

**11. Tại sao phải tách riêng `buildSystemPrompt()` và `buildCleanRagContext()`? Lưu cái nào vào DB?**
* **Trả lời:**
  * `buildSystemPrompt()` chứa vai trò AI, các quy tắc chống bịa và prompt điều hướng — chỉ gửi qua LLM để sinh câu trả lời, **không lưu vào DB và không trả về client**.
  * `buildCleanRagContext()` chỉ giữ lại các trích đoạn sạch dạng `[#Chunk X | tên_file - Trang Y]` + nội dung đoạn văn, lưu vào cột `ChatMessage.ragContext` để lần sau mở lại lịch sử chat, hàm JS `parseRawContextToCitations()` trong `chat.html` đọc thuộc tính `data-raw-context` và dựng lại khối Citations đóng/mở được cho sinh viên xem.

**12. Tại sao chọn gọi Ollama (`qwen3:8b`) trước rồi mới fallback sang Gemini? Nếu hết quota hoặc lỗi mạng thì sao?**
* **Trả lời** (mở `RagEngineService.callLlmWithRotation()` & `LlmKeyRotator.java`):
  * Nếu `studyhub.ai.base-url` có giá trị (mặc định `http://localhost:11434/v1` trỏ tới Ollama `qwen3:8b`, hoặc LiteLLM Proxy), hệ thống gọi endpoint OpenAI-compatible `/chat/completions` bằng `RestClient`.
  * Nếu gọi local thất bại mà hệ thống có cấu hình Gemini API key, tự động **auto-fallback** sang `callGeminiApi()`.
  * Bên Gemini có `LlmKeyRotator` xoay vòng nhiều key theo **Round-Robin**; key nào dính lỗi quota (`429` / `RESOURCE_EXHAUSTED`) bị đưa vào **Cooldown 60 giây** và tự chuyển sang key/model kế tiếp trong danh sách `GEMINI_MODELS`.

**13. [Chi tiết nhỏ hay bị soi] Tại sao comment ghi Apache PDFBox 3.0.3 nhưng `pom.xml` lại dùng `2.0.31`?**
* **Trả lời:**
  * Trong `pom.xml` (dòng 46–51) nhóm chủ động hạ xuống `2.0.31` để tương thích 100% với thư viện xuất PDF đề thi của Nhật (`openhtmltopdf-pdfbox:1.0.10`, tránh lỗi `NoSuchFieldError` lúc runtime).

---

## Mẹo nhỏ trước khi lên bảo vệ (1 phút chuẩn bị)
* Nhớ sẵn 4 file chính của bạn để thầy hỏi tới đâu bấm mở ngay tới đó:
  1. `DocumentController.java` & `ChatController.java` (Endpoint, Session, Quota)
  2. `PdfChunkingService.java` (Upload, PDFBox, `splitTextIntoChunks`)
  3. `RagEngineService.java` (Lexical scoring, `buildSystemPrompt`, `buildCleanRagContext`, `callLlmWithRotation`)
  4. `Chunk.java` & `ChatMessage.java` (JPA `@Lob`, `@ManyToOne`, `@OnDelete`)
* Nếu thầy bảo demo live phần của bạn: chuẩn bị sẵn 1 file PDF chữ thuần (không phải ảnh scan) vài trang, vào `/courses/{id}/documents` upload cho thầy thấy số Chunk & Trang nhảy lên ngay, rồi sang `/courses/{id}/chat` hỏi 1 câu có trong PDF đó và mở khối **Nguồn trích dẫn từ giáo trình** cho thầy đối chiếu đúng số trang.

---

## PHẦN 4: BÓC TÁCH CHI TIẾT LUỒNG XỬ LÝ RagEngineService (DÀNH CHO VẤN ĐÁP CODE)

Trái tim phân hệ RAG nằm ở file [`RagEngineService.java`](src/main/java/com/group2/assignment/rag/service/RagEngineService.java). Khi thầy yêu cầu **"Mở Service RAG ra và giải thích từng bước thực thi từ khi nhận câu hỏi đến khi trả lời"**, hãy trình bày theo đúng sơ đồ 5 bước chuẩn hóa dưới đây:

### 1. SƠ ĐỒ LUỒNG 5 BƯỚC CỦA HÀM `chatWithCourse(courseId, sessionId, question)` (Dòng 236–320)

```
[Nhận question, courseId, sessionId]
                 │
                 ▼
  ┌──────────────────────────────────────────────┐
  │ Bước 1: Quản lý Phiên (ChatSession)          │ ──► Tìm sessionId hoặc tự sinh mới với generateTitle()
  └──────────────────────────────────────────────┘
                 │
                 ▼
  ┌──────────────────────────────────────────────┐
  │ Bước 2: Truy xuất Chunks (findRelevantChunks)│ ──► Lọc Stop Words -> Chấm điểm Lexical -> Top-4 Chunks
  └──────────────────────────────────────────────┘
                 │
                 ▼
  ┌──────────────────────────────────────────────┐
  │ Bước 3: Đóng gói Prompt & Ngữ cảnh Sạch     │ ──► buildSystemPrompt (cho LLM) vs buildCleanRagContext (cho DB)
  └──────────────────────────────────────────────┘
                 │
                 ▼
  ┌──────────────────────────────────────────────┐
  │ Bước 4: Điều phối LLM (callLlmWithRotation)   │ ──► Tầng 1: Ollama Local (RestClient)
  │                                              │      └─► Lỗi? -> Tầng 2: Fallback Gemini (Key Rotator)
  └──────────────────────────────────────────────┘
                 │
                 ▼
  ┌──────────────────────────────────────────────┐
  │ Bước 5: Lưu trữ DB & Trả kết quả (DTO)       │ ──► saveMessages (USER & AI, token count, ragContext CLOB)
  └──────────────────────────────────────────────┘
```

#### Chi tiết từng bước và vị trí code:

* **Bước 1: Quản lý phiên hội thoại (`ChatSession`) (Dòng 240–254)**
  * Kiểm tra `sessionId`: Nếu truyền vào thì tìm qua `chatSessionRepository.findById(sessionId)`.
  * Nếu chưa có (`sessionId == null` hoặc không tìm thấy), khởi tạo `ChatSession` mới gắn với `Course`, đặt tiêu đề tự động qua hàm `generateTitle(question)` (Dòng 459–468: chuẩn hóa khoảng trắng và cắt lấy tối đa 37 ký tự + `...`), sau đó lưu vào DB.

* **Bước 2: Tìm kiếm ngữ cảnh liên quan (`findRelevantChunks`) (Dòng 55–146)**
  * Lấy toàn bộ chunks của môn học qua `chunkRepository.findAllByCourseId(courseId)`.
  * **Bộ lọc Stop Words (`extractKeywords`, Dòng 129–146)**: Chuyển câu hỏi về chữ thường, dùng regex `[^a-zA-Z0-9a-zA-ZÀ-ỹ\\s]` loại bỏ ký tự đặc biệt, lọc bỏ danh sách 30+ stop words tiếng Việt (*"và", "là", "của", "cho", "trong", "được", "các", "những", "này", "để", "thì", "về", "như", "gì", "nào", "sao", "thế", "hãy", "giải", "thích"...*) và tiếng Anh (*"the", "a", "an", "is", "are", "what", "how"...*), chỉ giữ lại các từ có độ dài `>= 2`.

  #### 🔍 BÓC TÁCH CHI TIẾT HÀM `extractKeywords(String query)` (Dòng 129–146)
  ```java
  public Set<String> extractKeywords(String query) {
      Set<String> stopWords = Set.of(
              "và", "là", "của", "cho", "trong", "với", "được", "các", "có", "những",
              "này", "để", "thì", "về", "như", "một", "ở", "gì", "nào", "sao", "thế",
              "ai", "đâu", "hãy", "biết", "giải", "thích",
              "the", "a", "an", "is", "are", "in", "on", "at", "for", "of", "and", "or", "to", "with", "what", "how"
      );

      String clean = query.replaceAll("[^a-zA-Z0-9a-zA-ZÀ-ỹ\\s]", " ").toLowerCase();
      String[] tokens = clean.split("\\s+");
      Set<String> keywords = new HashSet<>();
      for (String token : tokens) {
          if (token.length() >= 2 && !stopWords.contains(token)) {
              keywords.add(token);
          }
      }
      return keywords;
  }
  ```

  * **Mục đích**: Chuyển câu hỏi tự nhiên của người dùng (chứa nhiều từ nối, từ đệm, dấu câu lộn xộn) thành một tập hợp từ khóa thực sự mang giá trị ngữ nghĩa (`Set<String>`), dùng làm "đầu vào chuẩn hóa" để so khớp và tính điểm độ liên quan với các đoạn Chunk.
  * **Bóc tách 4 thao tác xử lý trong hàm**:
    1. **Tạo `stopWords` (`Set.of(...)`)**: Danh sách các từ dừng có tần suất xuất hiện cực lớn nhưng không mang giá trị phân biệt nội dung (quan hệ từ, trợ từ, từ để hỏi tiếng Việt + giới từ, mạo từ tiếng Anh). Nếu không loại bỏ, hầu hết các chunk đều bị "dính" điểm của những từ này, làm loãng độ chính xác.
    2. **Làm sạch chuỗi bằng Regex (`query.replaceAll(...)`)**:
       * Pattern `[^a-zA-Z0-9a-zA-ZÀ-ỹ\\s]`: Bất kỳ ký tự nào **không phải** chữ cái ASCII, chữ số, chữ cái Tiếng Việt có dấu (`À-ỹ`), hoặc khoảng trắng (`\\s`) đều bị biến thành dấu cách (`" "`).
       * Tác dụng: Loại sạch toàn bộ dấu câu (`?`, `!`, `,`, `.`, `:`, `-`, `/`), ký tự đặc biệt (`@`, `#`, `$`, `%`, `*`, `(`, `)`...) tránh lỗi dính chữ (ví dụ: `Spring?` ➔ `spring`).
       * `.toLowerCase()`: Chuẩn hóa toàn bộ về chữ thường để không bị lệch chữ hoa/thường (`Spring` khớp hoàn toàn với `spring`).
    3. **Tách từ (`clean.split("\\s+")`)**: Tách chuỗi theo một hoặc nhiều khoảng trắng liên tiếp, tránh sinh ra các phần tử rỗng.
    4. **Lọc điều kiện & Khử trùng lặp (`Set<String> keywords`)**:
       * Điều kiện `token.length() >= 2`: Loại bỏ các ký tự đơn lẻ vô nghĩa (chữ cái rời như `a`, `i`, `s` hoặc dấu còn sót).
       * Điều kiện `!stopWords.contains(token)`: Kiểm tra độ phức tạp $O(1)$ nhờ `Set`, bỏ qua toàn bộ từ dừng.
       * Dùng `HashSet`: Tự động khử trùng lặp (nếu sinh viên nhập từ khóa 2 lần thì chỉ giữ 1 từ duy nhất trong tập từ khóa).

  * **Bảng ví dụ trực quan Input & Output thực tế**:

  | # | Input (`String query`) | Chuỗi sau clean (`clean`) | Danh sách token bị loại bỏ | Output trả về (`Set<String>`) |
  |---|---|---|---|---|
  | **VD 1** | `"Hãy giải thích cho tôi biết JPA Lifecycle trong Spring Boot là gì?"` | `"hãy giải thích cho tôi biết jpa lifecycle trong spring boot là gì "` | `"hãy"`, `"giải"`, `"thích"`, `"cho"`, `"biết"`, `"trong"`, `"là"`, `"gì"` (Stop words) | `["tôi", "jpa", "lifecycle", "spring", "boot"]` *(5 từ khóa cốt lõi)* |
  | **VD 2** | `"@Transactional trong Spring Boot có tác dụng gì??? (VD: rollback)"` | `" transactional trong spring boot có tác dụng gì     vd  rollback "` | `@`, `?`, `(`, `)`, `:` (Ký tự đặc biệt); `"trong"`, `"có"`, `"gì"` (Stop words) | `["transactional", "spring", "boot", "tác", "dụng", "vd", "rollback"]` |
  | **VD 3** | `"What is the difference between Lazy and Eager loading?"` | `"what is the difference between lazy and eager loading "` | `"what"`, `"is"`, `"the"`, `"and"` (Stop words tiếng Anh) | `["difference", "between", "lazy", "eager", "loading"]` |
  | **VD 4** | `"Học lập trình C++ / C# ở đâu?"` | `"học lập trình c     c  ở đâu "` | `+`, `#`, `/` (Ký tự đặc biệt); `"ở"`, `"đâu"` (Stop words); `"c"` (Bị loại vì `length() < 2`) | `["học", "lập", "trình"]` |

  * **Vai trò của Output `Set<String>` đối với các hàm phía sau**:
    * Được truyền thẳng vào hàm `calculateRelevanceScore(chunk, lowerQuery, queryKeywords)`:
      * Duyệt từng từ khóa trong `Set`: đếm số lần xuất hiện trong chunk để cộng điểm logarit: `score += 1.0 + Math.log(1.0 + occurrences)`.
      * Dùng kích thước tập hợp `queryKeywords.size()` làm mẫu số tính tỷ lệ bao phủ: `coverage = matchedKeywords / queryKeywords.size()`.

  * **Công thức tính điểm liên quan (`calculateRelevanceScore`, Dòng 94–124)**:
    1. **Khớp nguyên văn cả câu**: `content.contains(fullQuery)` ➔ **Cộng ngay +10.0 điểm** (ưu tiên tuyệt đối đoạn chứa chính xác câu hỏi của sinh viên).
    2. **Tần suất từ khóa theo Logarit (TF giảm dần)**: Với mỗi keyword xuất hiện, `score += 1.0 + Math.log(1.0 + occurrences)`. Dùng hàm logarit để tránh hiện tượng lặp từ khóa làm sai lệch điểm số (Diminishing Returns).
    3. **Độ phủ từ khóa (Keyword Coverage)**: `coverage = matchedKeywords / queryKeywords.size()`, `score += coverage * 3.0` (đoạn nào chứa nhiều từ khóa khác nhau của câu hỏi sẽ được thưởng điểm cao hơn).
  * **Cơ chế Fallback an toàn (Dòng 83–86)**: Sắp xếp giảm dần theo `score`, lọc các chunk có `score > 0.0` lấy **Top-4**. Nếu câu hỏi hoàn toàn không khớp từ khóa nào (`matched.isEmpty()`), hệ thống **tự động fallback lấy 4 chunk đầu tiên của môn học** để AI vẫn có dữ liệu nền tảng phản hồi thay vì trả về khoảng trống.

* **Bước 3: Đóng gói Prompt & Tách biệt Ngữ cảnh sạch (Dòng 159–222)**
  * `buildCitations(relevantChunks)` (Dòng 189–203): Dựng danh sách `CitationDto` (`chunkIndex`, `pageNumber`, `documentFileName`, `snippet`) gửi về client qua REST.
  * `buildSystemPrompt(relevantChunks, question)` (Dòng 161–184): Tạo prompt hoàn chỉnh gửi tới LLM. Prompt định hình vai trò "Trợ lý Giảng viên AI", đính kèm các đoạn trích giáo trình dạng `--- TRÍCH ĐOẠN #i (Tài liệu: X, Trang: Y) ---`, đồng thời ép 3 quy tắc ứng xử: (1) Trả lời chi tiết định dạng Markdown; (2) Cuối bài liệt kê rõ mục `'Trích dẫn tài liệu'` kèm trang và tên file; (3) **Nếu tài liệu không có thông tin thì thông báo lịch sự là giáo trình không đề cập (chống hallucination)**.
  * `buildCleanRagContext(relevantChunks)` (Dòng 208–222): Format text sạch thuần túy chỉ gồm `[#Chunk i | tên_file - Trang Y]` và nội dung văn bản.
  * **Điểm mấu chốt trả lời thầy:** `buildSystemPrompt` chứa chỉ thị điều khiển nội bộ chỉ gửi cho LLM; còn `buildCleanRagContext` là ngữ cảnh sạch được lưu vào DB (`ChatMessage.ragContext`) để khi mở lại lịch sử chat, JavaScript ở client (`chat.html`) có thể parse và hiển thị khung accordion trích dẫn mà không lộ prompt hệ thống.

* **Bước 4: Điều phối gọi LLM đa tầng (`callLlmWithRotation`) (Dòng 325–438)**
  * **Tầng 1 - Local LLM Engine (Dòng 327–340 & 367–394)**: Kiểm tra `llmProperties.getBaseUrl()`. Mặc định đọc cấu hình `studyhub.ai.base-url=http://localhost:11434/v1` (hoặc biến môi trường `LITELLM_BASE_URL`), gọi endpoint OpenAI-compatible `/chat/completions` bằng Spring `RestClient` với model `qwen3:8b` (hoặc `gpt-3.5-turbo`).
  * **Tầng 2 - Auto-fallback sang Cloud (Dòng 331–338)**: Nếu gọi Local Ollama / LiteLLM proxy thất bại (ví dụ máy chưa bật `ollama serve`), code bắt exception, ghi log cảnh báo và tự động chuyển sang gọi Google Gemini REST API.
  * **Tầng 3 - Multi-Key Round-Robin & Model Cascading (Dòng 342–362 & 399–438)**:
    * Lấy key từ `LlmKeyRotator.getNextKey()` theo thuật toán **Round-Robin** trong danh sách key khỏe mạnh.
    * Khi gọi `callGeminiApi()`, duyệt tuần tự qua danh sách 6 model (`GEMINI_MODELS`, Dòng 43–50): `gemini-2.5-flash` ➔ `gemini-2.0-flash` ➔ `gemini-1.5-flash-latest` ➔ `gemini-1.5-flash` ➔ `gemini-1.5-pro` ➔ `gemini-2.5-flash-lite`.
    * Nếu dính lỗi `429` hoặc `RESOURCE_EXHAUSTED`, exception lập tức kích hoạt `llmKeyRotator.markKeyFailed(currentKey, 429, ...)` đưa key đó vào danh sách **Cooldown 60 giây**, và vòng lặp tự động chuyển sang API key tiếp theo.

* **Bước 5: Lưu trữ tin nhắn & Tính toán Token (`saveMessages`) (Dòng 440–457)**
  * Lưu đồng thời 2 bản ghi `ChatMessage`:
    1. Tin nhắn của sinh viên (`sender = "USER"`).
    2. Phản hồi của AI (`sender = "AI"`), lưu kèm `ragContext = cleanRagContext` vào cột `CLOB`.
  * Tính toán số token ước lượng: `(int) Math.round(words * 1.3)` (dựa trên tỷ lệ trung bình 1 từ tiếng Việt/Anh kỹ thuật tương đương ~1.3 token).

---

### 2. BỘ CÂU HỎI PHẢN BIỆN CHUYÊN SÂU VỀ `RagEngineService`

**14. [HSF302 JPA & Database Pool] Em đánh annotation `@Transactional` trên hàm `chatWithCourse()` (Dòng 235). Việc gọi API ngoại mạng (Ollama / Gemini) ngay bên trong Transaction có rủi ro gì đối với Connection Pool (HikariCP)?**
* **Trả lời chuẩn chuyên gia:**
  * **Rủi ro thực tế:** `@Transactional` khiến Spring mở một Database Connection từ HikariCP ngay khi bắt đầu hàm và giữ kết nối đó suốt quá trình thực thi. Trong khi đó, việc gọi HTTP qua mạng tới LLM (`callLlmWithRotation`) có thể mất từ **3 đến 10 giây**. Kết nối DB bị "treo" chờ I/O mạng, không được trả về pool. Nếu có 10–20 sinh viên cùng bấm chat một lúc, HikariCP pool sẽ nhanh chóng cạn kiệt (`ConnectionTimeoutException`), làm tê liệt toàn bộ ứng dụng.
  * **Giải pháp khắc phục tối ưu:** Tách `chatWithCourse` ra khỏi một transaction lớn:
    1. Transaction 1 (Read-only): Đọc Chunks và tạo `ChatSession` ban đầu.
    2. Bước gọi LLM: Chạy hoàn toàn ngoài Transaction, không chiếm giữ JDBC Connection.
    3. Transaction 2: Gọi riêng hàm `saveMessages()` để insert 2 bản ghi tin nhắn và commit ngay lập tức.

**15. [Resilience & UX] Khi toàn bộ cụm Gemini API Key đều hết hạn ngạch (429) và Ollama local chưa bật, hàm `chatWithCourse()` xử lý ra sao? Sinh viên có bị văng lỗi trang trắng (Whitelabel Error) không?**
* **Trả lời** (mở `RagEngineService.java` Dòng 302–319):
  * Hệ thống bọc toàn bộ khối gọi AI trong `try-catch (Exception e)`.
  * Nếu toàn bộ key thất bại:
    1. Hệ thống không để ứng dụng crash hay văng lỗi 500, mà tự sinh câu trả lời thân thiện: *"❌ Đã xảy ra lỗi khi trao đổi với AI... Hệ thống đang tự động điều phối lại cụm API Keys. Vui lòng thử lại sau giây lát."*
    2. **Đặc biệt:** Hệ thống vẫn gọi `saveMessages()` để lưu câu hỏi của sinh viên và thông báo lỗi này vào DB kèm theo `cleanRagContext`. Nhờ đó, sinh viên vẫn xem được các trích đoạn tài liệu liên quan bên dưới ngay cả khi AI không phản hồi!
    3. Trả về `ChatResponseDto` với `success = false`, `errorMessage = e.getMessage()`, Controller trả về HTTP `200 OK` dạng JSON để frontend hiển thị thông báo lỗi mượt mà, không reload trang.

**16. [Thuật toán IR / NLP] Tại sao trong công thức `calculateRelevanceScore` (Dòng 113) lại dùng logarit `1.0 + Math.log(1.0 + occurrences)` mà không cộng thẳng số lần xuất hiện `occurrences`?**
* **Trả lời:**
  * Đây là nguyên lý **Quy luật lợi ích giảm dần (Diminishing Returns)** trong lý thuyết Hệ thống Truy hồi Thông tin (Information Retrieval - thành phần Term Frequency của TF-IDF và BM25).
  * Nếu cộng thẳng `occurrences`, một đoạn văn bản vô tình lặp lại từ khóa 20 lần (ví dụ tác giả nhắc đi nhắc lại từ "Spring" trong một đoạn giới thiệu) sẽ đạt 20 điểm và lấn át hoàn toàn một đoạn văn bản khác cô đọng hơn chỉ chứa từ khóa 2–3 lần nhưng đúng trọng tâm.
  * Dùng `1.0 + Math.log(1.0 + occurrences)` giúp điểm số tăng nhanh ở những lần xuất hiện đầu tiên (1–3 lần) nhưng sẽ bão hòa dần khi số lần xuất hiện tăng cao, đảm bảo tính khách quan và công bằng khi xếp hạng Chunks.

**17. [Chi phí & Hiệu năng] So sánh độ trễ (Latency) và chi phí vận hành giữa Ollama Local (`qwen3:8b`) và Gemini Cloud API trong dự án?**
* **Trả lời:**
  * **Chi phí vận hành**: Cả hai đều đạt mục tiêu **$0 chi phí** theo yêu cầu đề tài môn HSF302:
    * Ollama chạy offline hoàn toàn trên tài nguyên máy cục bộ của người dùng.
    * Gemini Cloud API sử dụng gói Free Tier (15 RPM / 1500 RPD) kết hợp bộ xoay vòng Multi-Key.
  * **Độ trễ & Tính sẵn sàng**:
    * **Ollama Local (`qwen3:8b`)**: Độ trễ phụ thuộc vào phần cứng (GPU/CPU). Thời gian sinh câu trả lời thường từ 4–10 giây, nhưng bảo mật 100% dữ liệu tài liệu nội bộ, không phụ thuộc vào kết nối Internet và không bao giờ bị giới hạn hạn ngạch mạng.
    * **Gemini Cloud API**: Tốc độ sinh token rất nhanh (~1–2 giây), nhưng phụ thuộc đường truyền Internet và có nguy cơ chạm trần Rate Limit (429) nếu gọi dồn dập.
  * **Ý nghĩa kiến trúc**: Mô hình **Hybrid LLM Gateway** (ưu tiên Local ➔ Fallback Cloud) giúp hệ thống linh hoạt tối đa: người dùng vừa có thể chạy offline độc lập khi demo tại trường, vừa có thể chạy fallback trên cloud mượt mà nếu máy yếu không kéo được mô hình lớn.

