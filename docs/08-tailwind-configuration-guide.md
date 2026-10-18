# 08. HƯỚNG DẪN CẤU HÌNH TAILWIND CSS & DAISYUI TRONG THYMELEAF
**Môn học**: HSF302 - Framework & Advanced Java Web  
**Dự án**: **Smart Multi-Course Study Hub & RAG AI**  

Tài liệu này trình bày chi tiết 3 phương pháp tích hợp Tailwind CSS và DaisyUI vào Spring Boot Thymeleaf, phân tích ưu nhược điểm và giải thích lý do lựa chọn kiến trúc của nhóm.

---

## 📌 1. TỔNG QUAN 3 PHƯƠNG PHÁP TÍCH HỢP

```
[TAILWIND CSS TRONG SPRING BOOT]
 ├── Phương pháp 1: Play CDN (Script Tag) ────────► Lựa chọn cho bản Assignment (Zero-setup)
 ├── Phương pháp 2: Tailwind Standalone CLI ──────► Giải pháp biên dịch tĩnh không cần Node.js
 └── Phương pháp 3: Node.js / NPM + Maven Plugin ─► Giải pháp CI/CD cho dự án Enterprise
```

---

## 🛠️ 2. CHI TIẾT TỪNG PHƯƠNG PHÁP

### 🔹 PHƯƠNG PHÁP 1: TÍCH HỢP QUA PLAY CDN (NHÓM ĐANG SỬ DỤNG)
* **Cấu hình trong Thymeleaf** (`templates/fragments/head.html`):
  ```html
  <!-- 1. Nạp DaisyUI Component Library -->
  <link href="https://cdn.jsdelivr.net/npm/daisyui@4.12.10/dist/full.min.css" rel="stylesheet" type="text/css" />

  <!-- 2. Nạp Tailwind CSS Play CDN -->
  <script src="https://cdn.tailwindcss.com"></script>

  <!-- 3. Tùy biến cấu hình (Theme, Colors, Fonts) -->
  <script>
      tailwind.config = {
          theme: {
              extend: {
                  colors: {
                      brand: '#4f46e5'
                  }
              }
          }
      }
  </script>
  ```
* **Cơ chế**: Trình biên dịch JIT (Just-in-Time) của Tailwind chạy trực tiếp trên trình duyệt web, quét các class trong DOM và sinh CSS động.
* **Ưu điểm**:
  * Thiết lập trong 1 phút, không cần cài đặt Node.js hay công cụ biên dịch.
  * Đảm bảo 100% các thành viên và giảng viên clone code về chạy được ngay lập tức.
* **Nhược điểm**: Tệp CDN nặng hơn (~300KB), phụ thuộc vào kết nối mạng khi tải trang lần đầu.

---

### 🔹 PHƯƠNG PHÁP 2: DÙNG TAILWIND STANDALONE CLI (BIÊN DỊCH OFFLINE)
* **Khái niệm**: Tailwind cung cấp tệp thực thi nhị phân độc lập (Standalone Binary) dành riêng cho các dự án không dùng Node.js (Java Spring Boot, Go, Rust).
* **Các bước cấu hình**:
  1. Tải file `tailwindcss-windows-x64.exe` về thư mục gốc của project, đổi tên thành `tailwindcss.exe`.
  2. Tạo file `tailwind.config.js`:
     ```javascript
     module.exports = {
       content: ["./src/main/resources/templates/**/*.html"],
       theme: { extend: {} },
       plugins: [require('daisyui')],
     }
     ```
  3. Tạo file `src/main/resources/static/css/input.css`:
     ```css
     @tailwind base;
     @tailwind components;
     @tailwind utilities;
     ```
  4. Chạy lệnh biên dịch:
     ```bash
     ./tailwindcss.exe -i ./src/main/resources/static/css/input.css -o ./src/main/resources/static/css/output.css --minify
     ```
  5. Nhúng vào Thymeleaf:
     ```html
     <link rel="stylesheet" th:href="@{/css/output.css}">
     ```
* **Ưu điểm**: File CSS siêu nhẹ (~15KB) vì áp dụng cơ chế Tree-shaking (chỉ giữ lại các class thực sự được dùng), chạy offline 100%.
* **Nhược điểm**: Cần chạy lệnh build mỗi khi có class CSS mới.

---

### 🔹 PHƯƠNG PHÁP 3: TÍCH HỢP NODE.JS & FRONTEND-MAVEN-PLUGIN (ENTERPRISE CI/CD)
* **Khái niệm**: Đưa quy trình build CSS vào vòng đời biên dịch của Apache Maven.
* **Các bước cấu hình**:
  1. Khởi tạo `package.json` và cài đặt dependencies:
     ```bash
     npm init -y
     npm install -D tailwindcss postcss autoprefixer daisyui
     ```
  2. Cấu hình `pom.xml` với `frontend-maven-plugin`:
     ```xml
     <plugin>
         <groupId>com.github.eirslett</groupId>
         <artifactId>frontend-maven-plugin</artifactId>
         <version>1.15.0</version>
         <executions>
             <execution>
                 <id>install node and npm</id>
                 <goals><goal>install-node-and-npm</goal></goals>
                 <configuration>
                     <nodeVersion>v20.11.0</nodeVersion>
                 </configuration>
             </execution>
             <execution>
                 <id>npm run build</id>
                 <goals><goal>npm</goal></goals>
                 <configuration>
                     <arguments>run build</arguments>
                 </configuration>
             </execution>
         </executions>
     </plugin>
     ```
* **Ưu điểm**: Hoàn toàn tự động hóa trong quy trình CI/CD của doanh nghiệp lớn.
* **Nhược điểm**: Cấu hình phức tạp, thời gian build lâu hơn.

---

## 📊 3. BẢNG SO SÁNH TỔNG QUAN

| Tiêu chí so sánh | Phương pháp 1: Play CDN | Phương pháp 2: Standalone CLI | Phương pháp 3: Maven Plugin |
|:---|:---|:---|:---|
| **Yêu cầu phần mềm phụ** | Không cần | Tệp thực thi `.exe` 50MB | Cần Node.js & NPM |
| **Độ phức tạp cài đặt** | Cực kỳ đơn giản (1 phút) | Trung bình (10 phút) | Phức tạp (30 phút) |
| **Dung lượng file CSS** | ~300 KB | ~15 KB (Tối ưu) | ~15 KB (Tối ưu) |
| **Khả năng chạy Offline** | Cần Internet lần đầu | 100% Offline | 100% Offline |
| **Phù hợp nhất cho** | **Assignment, Demo học tập** | Đồ án lớn độc lập | Dự án doanh nghiệp |

---

## 💡 4. LÝ DO NHÓM LỰA CHỌN PHƯƠNG PHÁP 1 (PLAY CDN) CHO BÀI ASSIGNMENT
1. **Tính tương thích và đồng bộ 100%**: Dự án có 6 thành viên làm việc trên nhiều hệ điều hành khác nhau. Dùng Play CDN loại bỏ hoàn toàn các lỗi sai khác về phiên bản Node.js hay biến môi trường PATH.
2. **Tiện lợi cho Giảng viên chấm bài**: Giảng viên chỉ cần clone mã nguồn từ GitHub về và chạy lệnh `./mvnw spring-boot:run` là toàn bộ giao diện kính mờ Glassmorphism hiển thị hoàn hảo mà không phải cài đặt bất kỳ công cụ frontend nào.
3. **Phù hợp với tính chất Assignment**: Tiết kiệm thời gian cấu hình công cụ để tập trung tối đa nguồn lực vào việc phát triển các tính năng cốt lõi (RAG Engine, Study Planner, Quiz Generator) và thực nghiệm AI Agent.
