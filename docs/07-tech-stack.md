# 07. TỔNG HỢP CÔNG NGHỆ DỰ ÁN (FULL TECH STACK SPECIFICATION)
**Dự án**: **Smart Multi-Course Study Hub & RAG AI**  
**Môn học**: HSF302 - Framework & Advanced Java Web  

Tài liệu này tổng hợp toàn bộ các tầng công nghệ, thư viện, mô hình AI và công cụ phát triển được nhóm thống nhất sử dụng trong dự án.

---

## 📌 1. BẢNG TỔNG QUAN TECH STACK (AT A GLANCE)

| Tầng Công Nghệ | Công Nghệ / Thư Viện | Phiên bản | Vai trò & Mục đích sử dụng | Chi phí |
|:---|:---|:---|:---|:---|
| **Ngôn ngữ cốt lõi** | **Java** | 17 LTS | Ngôn ngữ lập trình chính cho toàn bộ hệ thống Backend. | Miễn phí |
| **Backend Framework** | **Spring Boot** | 3.3.x | Xây dựng kiến trúc MVC, quản lý Bean, REST Endpoint và Web Controller. | Miễn phí |
| **Template Engine** | **Thymeleaf** | 3.x | Render mã HTML ở phía Server (Server-Side Rendering), binding dữ liệu từ Spring Model. | Miễn phí |
| **CSS Framework** | **Tailwind CSS** | v3.x | Xây dựng giao diện Responsive với phong cách Utility-first. | Miễn phí |
| **Component Library** | **DaisyUI** | v4.x | Cung cấp sẵn các component cao cấp: Chat bubbles, Drawer menu, Modal, Timeline, Steps... | Miễn phí |
| **Trích xuất PDF** | **Apache PDFBox** | 3.x | Đọc nội dung văn bản và trích xuất text từ file PDF giáo trình để phục vụ Chunking. | Miễn phí |
| **Chat LLM API** | **Google Gemini** | gemini-1.5-flash | Mô hình ngôn ngữ chính: Trả lời câu hỏi RAG, lập Study Planner và sinh Đề thi trắc nghiệm Quiz. | Miễn phí (Free Tier) |
| **Chat LLM Dự phòng** | **Groq Cloud** | llama-3.3-70b | Mô hình ngôn ngữ dự phòng siêu tốc độ cao (Inference speed cực nhanh). | Miễn phí (Free Tier) |
| **Embedding Model** | **Google Cloud** | text-embedding-004 | Vector hóa văn bản thành mảng vector 768 chiều (dùng chung chính Gemini API Key). | Miễn phí (Free Tier) |
| **Cơ sở dữ liệu (Database)** | **H2 Database** | v2.2+ | Hệ quản trị CSDL quan hệ nhúng (Embedded SQL DB), lưu trữ file bền vững, không cần cài đặt. | Miễn phí |
| **Tầng ORM / Data Access** | **Spring Data JPA / Hibernate** | 3.3.x | Quản lý Entity, Repository, tự động ánh xạ bảng và sinh cấu trúc CSDL chuẩn môn HSF302. | Miễn phí |
| **Giao diện quản trị DB** | **H2 Web Console** | - | Trang web trực quan xem dữ liệu các bảng và chạy câu lệnh SQL tại `http://localhost:8080/h2-console`. | Miễn phí |
| **Vector Engine** | **In-Memory + Cosine Similarity** | Java 17 | Tính toán độ tương đồng góc Cosine trên vector 768 chiều từ Chunks trong H2 Database. | Miễn phí |
| **Build & Dependencies** | **Apache Maven** | Maven 3.9+ | Quản lý vòng đời dự án và dependencies qua file `pom.xml` (`mvnw`). | Miễn phí |
| **Quản lý mã nguồn** | **Git & GitHub** | - | Quản lý phân nhánh `feature/`, theo dõi tiến độ và tích hợp code cả nhóm. | Miễn phí |

---

## 🎨 2. TẦNG GIAO DIỆN (FRONT-END & UI/UX STACK)

1. **Thymeleaf Template Engine**:
   * Áp dụng triệt để kỹ thuật **Thymeleaf Fragments** (`th:replace`, `th:fragment`) để module hóa Master Layout: `navbar.html`, `sidebar.html`, `footer.html`.
   * Dynamic Data Binding: Dùng `th:each`, `th:if`, `th:text`, `th:field` để đổ dữ liệu môn học, tin nhắn chat và đề thi trắc nghiệm.
2. **Tailwind CSS + DaisyUI (Nạp qua CDN Script)**:
   * **Không cần cài đặt Node.js/npm**: Nhúng trực tiếp qua thẻ `<script src="https://cdn.tailwindcss.com"></script>` kết hợp plugin DaisyUI để đảm bảo máy của cả 6 bạn đều chạy được ngay lập tức.
   * **Ngôn ngữ thiết kế Glassmorphism**: Sử dụng các class `backdrop-blur-xl bg-white/30 dark:bg-slate-900/40 border border-white/20 shadow-2xl` tạo phong cách kính mờ hiện đại như các sản phẩm thương mại.
   * **Hỗ trợ Đa Theme (Theme Switcher)**: Tận dụng cơ chế đổi màu tự động của DaisyUI (Light Mode / Dark Mode / Synthwave).
   * **Các Component then chốt**:
     * `chat chat-start` và `chat chat-end`: Dựng khung đàm thoại ChatGPT.
     * `timeline` / `steps`: Dựng lộ trình học tập của Module 4 (Study Planner).
     * `accordion`: Hiển thị danh sách các đoạn Chunks của Module 2 (Bạn Bình).
     * `modal` & `alert`: Hiển thị thông báo kết nối API Key và kết quả chấm điểm Quiz.

---

## ⚙️ 3. TẦNG BACK-END & DỊCH VỤ (BACK-END ARCHITECTURE)

1. **Spring Boot 3.3.x**:
   * **Spring Web MVC**: Định tuyến các controller (`@Controller`, `@GetMapping`, `@PostMapping`).
   * **Spring 6 RestClient**: Chuẩn HTTP Client mới nhất của Spring Boot 3 (thay thế cho `RestTemplate` cũ), cung cấp Fluent API trực quan để gửi HTTP POST request sang Google Gemini API và Groq API.
2. **Apache PDFBox**:
   * Thư viện Java mã nguồn mở hàng đầu để xử lý file PDF.
   * Cho phép đọc nhanh toàn bộ các trang sách/slide, lọc bỏ ký tự rác và trích xuất văn bản thô để phục vụ bộ máy Chunking.
3. **Lưu trữ Trạng thái & Cơ sở dữ liệu (Persistence & State Management)**:
   * **Bảo mật API Key**: Lưu trực tiếp vào `HttpSession` của người dùng (`SESSION_PROVIDER_API_KEY`). Không lưu cứng ra file cấu hình `application.properties`, không đẩy lên database công khai, đảm bảo tính an toàn cá nhân tuyệt đối.
   * **H2 Database (Lưu trữ file bền vững)**: Toàn bộ dữ liệu 11 bảng quan hệ (Môn học, Tài liệu PDF, Chunks, Đề thi Quiz, Câu hỏi, Lần làm bài, Kế hoạch học, Tài khoản người dùng...) được lưu trữ bền vững tại `./data/studyhubdb.mv.db` với chế độ `AUTO_SERVER=TRUE` cho môi trường phát triển cục bộ và `jdbc:h2:mem:testdb` cách ly cho kiểm thử tự động.
   * **Spring Data JPA & Hibernate**: Ánh xạ đầy đủ các Entity quan hệ `@OneToMany`, `@ManyToOne`, `@Entity` chuẩn doanh nghiệp, tự động đồng bộ lược đồ với Flyway Migration `V1__initial_schema.sql`.

---

## 🤖 4. TẦNG TRÍ TUỆ NHÂN TẠO & VECTOR RAG (AI & RAG STACK)

### 4.1. Lựa chọn Mô hình Ngôn ngữ Lớn (LLM):
* **Lựa chọn 1: Google Gemini API (`gemini-1.5-flash`)**:
  * *Lý do chọn*: Context window khổng lồ (hơn 1 triệu tokens), hiểu tiếng Việt cực kỳ tốt, hỗ trợ sinh JSON format chuẩn, miễn phí 15 requests/phút (hoàn toàn đủ cho đồ án học tập).
* **Lựa chọn 2: Groq Cloud API (`llama-3.3-70b-versatile`)**:
  * *Lý do chọn*: Tốc độ sinh chữ cực nhanh (gần như tức thì), tương thích hoàn toàn chuẩn OpenAI API spec, miễn phí.

### 4.2. Lựa chọn Mô hình Embedding:
* **Mô hình**: **Google `text-embedding-004`**:
  * *Lý do chọn giải pháp Cloud API thay vì Local*:
    1. **Zero-Setup**: Không cần cài đặt phần mềm nặng như Ollama, không cần tải model vài GB làm nặng máy của 6 thành viên.
    2. **Dùng chung 1 API Key**: Người dùng nhập Gemini API Key thì dùng chung key đó cho cả việc Chat lẫn tính toán Embedding.
    3. **Độ chính xác cao**: Vector 768 chiều được tối ưu hóa sâu cho việc tìm kiếm ngữ nghĩa văn bản học thuật.

### 4.3. Thuật toán Tìm kiếm Ngữ nghĩa (Vector Retrieval):
* **Thuật toán**: **Cosine Similarity (Độ tương đồng góc Cosin)** viết bằng Java thuần.
* **Cơ chế hoạt động**:
  $$\text{Cosine Similarity} = \frac{\mathbf{A} \cdot \mathbf{B}}{\|\mathbf{A}\| \|\mathbf{B}\|}$$
* Khi sinh viên đặt câu hỏi, vector của câu hỏi được so khớp với vector của tất cả các Chunks trong môn học đó ➔ Chọn ra Top 3 - 5 Chunks có số điểm cao nhất (từ 0.7 trở lên) để đưa vào ngữ cảnh của Prompt.

---

## 🛠️ 5. MÔI TRƯỜNG PHÁT TRIỂN & CÔNG CỤ (DEVELOPMENT TOOLING)

* **IDE khuyên dùng**: IntelliJ IDEA (Ultimate/Community) hoặc Visual Studio Code.
* **AI Coding Assistants trong IDE**:
  * Cursor AI, GitHub Copilot, Gemini Code Assist, Continue.dev (phục vụ tiêu chí thực nghiệm dùng AI sinh code của thầy).
* **JDK**: OpenJDK 17 hoặc Eclipse Temurin 17.
* **Trình duyệt kiểm thử**: Google Chrome, Microsoft Edge (kiểm tra F12 Responsive).
