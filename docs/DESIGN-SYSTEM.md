# 🎨 BỘ QUY CHUẨN THIẾT KẾ GIAO DIỆN HỆ THỐNG (STUDYHUB AI DESIGN SYSTEM)
> **Dự án**: Smart Multi-Course Study Hub & RAG AI (HSF302 - Nhóm 2)  
> **Chỉ đạo thiết kế**: Tùng (Team Lead)  
> **Hệ quy chiếu chuẩn**: Đồng bộ 100% với Brand Identity & Landing Page (`#030712`, Violet, Purple & Cyber Cyan) kết hợp Format Split-Screen 50/50  
> **Ngày cập nhật**: 02/11/2026

---

## 🏛️ 1. TRIẾT LÝ THIẾT KẾ CỐT LÕI (DESIGN PHILOSOPHY)

1. **Đồng Bộ Nhận Diện Thương Hiệu Với Landing Page (Landing Page Sync)**:
   - Hệ thống StudyHub AI mang nhận diện đặc trưng đã được khẳng định trên Landing Page:
     - Nền tối không gian số sâu thẳm (**Obsidian Dark `#030712`**).
     - Ánh sáng đa chiều Cyber Cyan (`#06b6d4`) và Tím Violet trí tuệ nhân tạo (`#8b5cf6`).
     - Nút hành động CTA biểu trưng: Gradient **Purple - Violet - Pink** (`from-purple-600 via-violet-600 to-pink-600`).
   - Tách biệt hoàn toàn với màu xanh lá của các dự án khác, tạo dấu ấn thương hiệu StudyHub AI độc nhất, đồng bộ từ trang chủ đến trang đăng nhập.

2. **Kế Thừa Format Chuẩn Quốc Tế (Split-Screen 50/50)**:
   - Trang Đăng nhập (`/login`) và Đăng ký (`/register`) sử dụng bố cục **Split-Screen 50/50 toàn màn hình**:
     - **Cột trái (50% Brand Hero)**: Tông màu tối Obsidian `#030712` với hiệu ứng ánh sáng Cyan/Violet, mockup thẻ giáo trình HSF302 kèm trích dẫn RAG AI 0% ảo giác (chuẩn theo visual trên landing page).
     - **Cột phải (50% Action Form)**: Nền trắng tinh khôi, căn giữa form thao tác, input có icon trực quan, nút bấm đồng bộ màu CTA của Landing Page.

3. **Nguyên Tắc Trực Quan & Tiết Lộ Dần (Progressive Disclosure)**:
   - Màn hình ngoài tinh tế, hiện đại; khi đăng nhập vào trong hệ thống thì thanh Navigation dạng đứng bên trái (Left Docked Sidebar) hỗ trợ không gian làm việc rộng rãi với bảng thẻ White-card Palette.

---

## 🎨 2. BẢNG MÃ MÀU CHUẨN ĐỒNG BỘ LANDING PAGE (DESIGN TOKENS)

| Token Name | Giá Trị CSS / Tailwind | Mã Hex | Mục Đích Sử Dụng |
| :--- | :--- | :---: | :--- |
| **Brand Canvas Dark** | `#030712` / `bg-[#030712]` | `#030712` | Nền tối Obsidian của Landing Page & Hero Panel Auth. |
| **Brand Hero Dark** | `from-[#030712] via-[#090d16] to-[#0f172a]` | `#030712` - `#0f172a` | Nền chuyển sắc Cột Trái Hero của Auth Split-Screen. |
| **Brand Primary CTA** | `from-purple-600 via-violet-600 to-pink-600` | `#9333ea` - `#8b5cf6` - `#db2777` | Nút hành động chính đồng bộ chuẩn với nút "Bắt Đầu Ngay" trên Landing Page. |
| **Brand Accent Violet** | `text-violet-400`, `border-violet-500/30` | `#a78bfa` | Điểm nhấn chữ, border mockup, badge công nghệ. |
| **Brand Cyber Cyan** | `text-cyan-400`, `bg-cyan-500/10` | `#22d3ee` | Ánh sáng đối lưu trên nền tối, icon công nghệ AI. |
| **Canvas Background** | `bg-slate-50` | `#f8fafc` | Nền tổng thể trang Dashboard nội bộ. |
| **Surface Card** | `bg-white/95`, `backdrop-blur-md` | `#ffffff` | Bề mặt các thẻ thống kê, card môn học, panel form sáng. |
| **Border Line** | `border-slate-200/80` | `#e2e8f0` | Đường viền mảnh tinh tế của các form input và card. |
| **Text Heading** | `text-slate-900` | `#0f172a` | Tiêu đề lớn form, tên môn học. |
| **Text Body / Muted** | `text-slate-500` / `text-slate-600` | `#64748b` | Mô tả phụ, hướng dẫn, placeholder. |

---

## 📐 3. QUY CHUẨN BỐ CỤC AUTH SPLIT-SCREEN (`/login`, `/register`)

* **Cột trái (Desktop 50%, ẩn trên Mobile)**:
  - Background: `from-[#030712] via-[#090d16] to-[#0f172a]` kèm ambient glow màu Cyan (`rgba(6,182,212,0.12)`) và Violet (`rgba(139,92,246,0.15)`).
  - Header: Logo StudyHub AI, biểu tượng nón tốt nghiệp tím, tag phiên bản `v2.4`.
  - Center: Tiêu đề `"Unlock the Hidden Knowledge in Your Courseware"`, thẻ mockup môn `HSF302` với trích dẫn RAG AI không ảo giác chuẩn theo Landing Page.
  - 3 Badges: Linh hoạt (Mọi trường đại học), Không ảo giác (Trích dẫn trang slide), An tâm (Dữ liệu cô lập 100%).
  - Footer: Chính sách học tập & HSF302 Fall 2026.
* **Cột phải (Desktop 50%, Mobile 100%)**:
  - Nền trắng tinh khôi `bg-white`.
  - Tiêu đề dứt khoát: **Đăng nhập tài khoản** / **Đăng ký tài khoản**.
  - Form inputs có icon bên trái, nút con mắt xem mật khẩu bên phải.
  - Nút Submit: **Gradient đồng bộ chuẩn Landing Page** (`bg-gradient-to-r from-purple-600 via-violet-600 to-pink-600 hover:from-purple-500 hover:to-pink-500 shadow-lg shadow-purple-500/25`).
  - Hộp điền nhanh Tài khoản Demo (1-Click) dạng thẻ nhỏ trang nhã.
  - Nút Đăng nhập Google & liên kết quay lại Trang Chủ.
