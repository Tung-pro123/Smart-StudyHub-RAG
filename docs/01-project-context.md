# 01. BỐI CẢNH DỰ ÁN & MỤC TIÊU BÀI TẬP (PROJECT CONTEXT)

**Môn học**: HSF302 - Framework & Advanced Java Web  
**Dự án**: **Smart Multi-Course Study Hub & RAG AI** (Nền Tảng Hỗ Trợ Học Tập Đa Môn Học & Chat AI Giáo Trình)  
**Nhóm thực hiện**: Tùng (Leader), Bình, Nhật, Thắng, Ân, Trường  

---

## 🎯 1. BỐI CẢNH YÊU CẦU CỦA ĐỀ BÀI

Theo đề bài bài tập nhóm môn học, dự án được chia làm 2 chặng đường lớn:

```
[GIAI ĐOẠN 1: BÀI ASSIGNMENT (Hiện tại)]
      │
      ▼
  Nghiên cứu & Thực hành:
  • Tích hợp UI Toolkit (Tailwind CSS + DaisyUI) trong Spring Boot Thymeleaf
  • Ứng dụng AI Agent trong IDE để sinh code UI và Back-end
  • Xây dựng Mini App nền tảng: AI Tutor hỗ trợ học Java Spring (Chat API ngoài + Cài đặt API Key)
  • Bàn giao: Source Code Demo + Slide Thuyết trình + Tài liệu User Guide
      │
      ▼
[GIAI ĐOẠN 2: PROJECT CUỐI KỲ (Tương lai)]
  Nâng cấp từ nền tảng Giai đoạn 1:
  • Tích hợp RAG (Retrieval-Augmented Generation) từ tài liệu môn học (PDF, Docx)
  • Text Chunking, Vector Embeddings & Vector Database
  • Streaming Response, OAuth2 Login Google/GitHub, Hoán chuyển đa nền tảng LLM
```

---

## 💡 2. MỤC TIÊU CỐT LÕI CỦA GIAI ĐOẠN 1 (ASSIGNMENT)

### 2.1. Về mặt UI Toolkit & Front-end:
* Sử dụng **Tailwind CSS** kết hợp cùng **DaisyUI Component Library**.
* Xây dựng giao diện hiện đại mang phong cách **Glassmorphism (Kính mờ)**, hỗ trợ Dark/Light Theme.
* Ứng dụng kỹ thuật **Thymeleaf Fragments** (`th:replace`, `th:fragment`) để module hóa Master Layout (Header, Sidebar, Footer, Chat Window).

### 2.2. Về mặt Tính năng Ứng dụng:
* Xây dựng công cụ **Java Spring AI Tutor**: Cho phép sinh viên đặt câu hỏi về các chủ đề Java Spring Boot (Spring Core, IoC/DI, Spring MVC, Spring Data JPA, Thymeleaf, xử lý lỗi 404/500...).
* Hệ thống kết nối API LLM bên ngoài (Google Gemini API hoặc Groq API) với chi phí $0 (hoàn toàn miễn phí, tốc độ phản hồi nhanh).
* Trang Cài đặt (Settings) cho phép người dùng tự cấu hình API Key cá nhân, lưu trữ an toàn trong Session.

### 2.3. Về mặt Ứng dụng AI vào Lập trình (AI-Assisted Development):
* Sử dụng các AI Agent / AI Assistant trong IDE (Cursor, GitHub Copilot, Gemini Code Assist, Continue, Claude...).
* Minh chứng thực tế quá trình:
  * Prompt AI sinh mã nguồn giao diện HTML + Tailwind + DaisyUI.
  * Prompt AI sinh mã nguồn Back-end Spring Boot (Controller, Service, RestClient).
  * Quy trình nhờ AI gỡ lỗi (debug), tối ưu hóa code và giải thích kiến trúc.

### 2.4. Sản phẩm Bàn giao (Deliverables):
1. **Source Code**: Chạy được cục bộ trên máy tính (port 8080) chỉ với 1 lệnh Maven.
2. **User Guide**: Tài liệu hoàn chỉnh hướng dẫn cài đặt từ A-Z, cấu hình API Key và quy trình dùng AI.
3. **Slide Thuyết trình**: Bộ slide chuẩn chỉnh 12-15 trang phục vụ bảo vệ trước giảng viên.

---

## 🛠️ 3. CÔNG NGHỆ ÁP DỤNG TRONG GIAI ĐOẠN 1

| Tầng | Công nghệ | Phiên bản / Chi tiết |
|:---|:---|:---|
| **Ngôn ngữ** | Java | Java 17 LTS |
| **Framework Backend** | Spring Boot | 3.3.x (Web, Thymeleaf, RestClient) |
| **Template Engine** | Thymeleaf | Server-side Rendering HTML |
| **Giao diện (CSS)** | Tailwind CSS + DaisyUI | Tích hợp qua CDN / Script cấu hình Theme |
| **AI LLM API** | Google Gemini API (hoặc Groq Llama 3) | Free tier, phản hồi nhanh, chất lượng cao |
| **Quản lý dependencies** | Apache Maven | Maven Wrapper (`./mvnw`) |
| **Quản lý mã nguồn** | Git & GitHub | Quy chuẩn Branching & Conventional Commits |
