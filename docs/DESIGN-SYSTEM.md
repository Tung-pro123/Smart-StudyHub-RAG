# 🎨 BỘ QUY CHUẨN THIẾT KẾ GIAO DIỆN HỆ THỐNG (STUDYHUB AI DESIGN SYSTEM)
> **Dự án**: Smart Multi-Course Study Hub & RAG AI (HSF302 - Nhóm 2)  
> **Chỉ đạo thiết kế**: Tùng (Team Lead)  
> **Hệ quy chiếu chuẩn**: Phong cách Monochromatic Split-Screen từ đồ án SmartStorage (SWP Project)  
> **Ngày ban hành**: 02/11/2026

---

## 🏛️ 1. TRIẾT LÝ THIẾT KẾ CỐT LÕI (DESIGN PHILOSOPHY)

1. **Monochromatic Cohesion (Đồng nhất một sắc thái thương hiệu)**:
   - Kiên quyết **chấm dứt việc phối gradient cầu vồng hỗn loạn** (tím pha hồng pha cam chói gắt).
   - Sử dụng một dải màu xanh ngọc / xanh rừng chủ đạo (**Deep Emerald & Forest Green**), kết hợp với bảng màu trung tính sạch (**Pure White & Slate Neutrals**), mang lại cảm giác an tâm, tri thức và đẳng cấp như các sản phẩm SaaS quốc tế (Stripe, Linear, Supabase).
2. **Progressive Disclosure (Phân cấp & Tiết lộ thông tin có chiều sâu)**:
   - Không nhồi nhét quá nhiều nút bấm, số liệu rườm rà ở màn hình ngoài.
   - Giao diện từ ngoài vào trong: Thẻ ngoài tinh gọn, càng đi sâu vào chi tiết môn học / phòng thi AI thì các công cụ chuyên sâu mới xuất hiện.
3. **Split-Screen Layout cho Xác Thực (Auth Standards)**:
   - Toàn bộ trang Đăng nhập (`/login`) và Đăng ký (`/register`) tách biệt độc lập với Drawer/Sidebar nội bộ, áp dụng bố cục **50/50 Split-Screen**:
     - **Cột trái (50% Hero)**: Nền xanh rừng sâu lắng (`#064e3b`), logo, định vị thương hiệu, thẻ preview sản phẩm, các pills tính năng nổi bật.
     - **Cột phải (50% Form)**: Nền trắng tinh khiết, căn giữa form thao tác, input có icon trực quan, nút bấm một màu dứt khoát.

---

## 🎨 2. BẢNG MÃ MÀU CHUẨN (DESIGN TOKENS)

| Token Name | Tailwind Class | Mã Hex | Mục Đích Sử Dụng |
| :--- | :--- | :---: | :--- |
| **Brand Dark** | `from-emerald-950 via-teal-900 to-slate-900` | `#022c22` - `#064e3b` | Nền cột Hero của Auth Split-Screen, tạo chiều sâu thị giác. |
| **Brand Primary** | `bg-emerald-600`, `hover:bg-emerald-700` | `#059669` | Nút hành động chính (Primary CTA), trạng thái Active, Icon chủ đạo. |
| **Brand Accent / Glow** | `text-emerald-400`, `shadow-emerald-500/20` | `#34d399` | Điểm nhấn ánh sáng, badge làm nổi bật trên nền tối. |
| **Brand Subtle** | `bg-emerald-50`, `text-emerald-700` | `#ecfdf5` | Nền thẻ icon, chip badge trên giao diện sáng. |
| **Canvas Background** | `bg-slate-50` | `#f8fafc` | Nền tổng thể trang web (Workspace canvas). |
| **Surface Card** | `bg-white/95`, `backdrop-blur-md` | `#ffffff` | Bề mặt các thẻ thống kê, card môn học, modal pop-up. |
| **Border Line** | `border-slate-200/80` | `#e2e8f0` | Đường viền mảnh tinh tế, ngăn cách vùng nội dung. |
| **Text Heading** | `text-slate-900` | `#0f172a` | Tiêu đề lớn, tên môn học, nhãn đậm. |
| **Text Body / Muted** | `text-slate-500` / `text-slate-600` | `#64748b` | Đoạn văn mô tả, hướng dẫn phụ, placeholder. |

---

## 📐 3. QUY CHUẨN BỐ CỤC (LAYOUT PATTERNS)

### 🔹 Pattern 1: Auth Split-Screen (`/login`, `/register`)
* **Chiều cao**: `min-h-screen`, hiển thị trọn vẹn trong khung nhìn (Viewport) không bị giật trang.
* **Cột trái (Desktop `w-1/2`, ẩn trên Mobile `hidden lg:flex`)**:
  - Background: Gradient xanh rừng sâu thẳm với hiệu ứng ánh sáng ngọc bích mờ nhẹ.
  - Header: Logo StudyHub AI, tag phiên bản "Smart Multi-Course Hub & RAG AI (HSF302)".
  - Center: Tiêu đề súc tích `"Học Tập Vừa Sức, Bứt Phá Điểm Số"`, kèm thẻ mockup giáo trình và 3 feature pills:
    - 📌 *Tự Nạp Slide (BYOC) - Tự do mọi trường đại học*
    - 📌 *Đối Chứng 0% Ảo Giác - Trích dẫn chuẩn xác trang PDF*
    - 📌 *Luyện Đề Trắc Nghiệm - Sinh câu hỏi AI & lời giải chi tiết*
  - Footer: Điều khoản dịch vụ & Mã lớp HSF302.
* **Cột phải (Desktop `w-1/2`, Mobile `w-full`)**:
  - Background trắng tinh giản, form căn giữa (`max-w-md w-full mx-auto`).
  - Tiêu đề dứt khoát: **Đăng Nhập Tài Khoản** / **Đăng Ký Tài Khoản**.
  - Form Fields gọn gàng, nút xem mật khẩu nhạy bén, nút đăng nhập một màu xanh `bg-emerald-600`.
  - Hộp tiện ích 1-Click điền nhanh tài khoản demo dạng thẻ nhỏ trang nhã.
  - Nút Đăng nhập với Google & liên kết quay lại Trang chủ.

### 🔹 Pattern 2: Workspace Dashboard (`/courses`, `/planner`, `/quiz`)
* **Thanh điều hướng dọc bên trái (Left Docked Sidebar Navigation)**:
  - Cố định toàn chiều dọc màn hình (`drawer lg:drawer-open`).
  - Nền trắng tinh khôi `bg-white/95` với đường kẻ mảnh `border-r border-slate-200/80`.
  - Menu icon tối giản, trạng thái Active làm nổi bật màu xanh ngọc nhẹ `bg-emerald-50 text-emerald-700`.
* **Khu vực hiển thị nội dung (Workspace Canvas)**:
  - Sử dụng **White-card Palette**: Mỗi môn học là một thẻ card trắng nổi khối nhẹ, hover nâng card êm ái.
  - Loại bỏ các dropdown gò bó, ô tìm kiếm từ khóa rộng mở hỗ trợ mọi trường đại học.

---

## 🧩 4. QUY CHUẨN THÀNH PHẦN (UI COMPONENTS)

### 1. Ô Nhập Liệu (Input Fields)
```html
<div class="relative">
    <i class="fa-solid fa-envelope absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-xs"></i>
    <input type="text" 
           class="input input-sm h-11 bg-slate-50/70 border-slate-200 text-slate-900 placeholder:text-slate-400 pl-10 pr-10 w-full rounded-xl text-xs focus:bg-white focus:border-emerald-600 focus:ring-1 focus:ring-emerald-600 transition" />
</div>
```

### 2. Nút Bấm Chính (Primary CTA Button)
```html
<button class="btn h-11 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl border-none shadow-sm shadow-emerald-600/20 text-xs transition-all w-full flex items-center justify-center gap-2">
    <span>Đăng Nhập Ngay</span>
    <i class="fa-solid fa-arrow-right text-xs"></i>
</button>
```

### 3. Huy Hiệu Gói Cước (Subscription Badges)
- **Gói Free**: `<span class="badge badge-xs bg-slate-100 text-slate-700 font-bold border-slate-200">FREE</span>`
- **Gói Pro**: `<span class="badge badge-xs bg-emerald-100 text-emerald-800 font-extrabold border-emerald-200">PRO</span>`
- **Gói Squad**: `<span class="badge badge-xs bg-cyan-100 text-cyan-800 font-extrabold border-cyan-200">SQUAD</span>`
- **Admin**: `<span class="badge badge-xs bg-purple-100 text-purple-800 font-black border-purple-200">ADMIN</span>`

---

## 🚫 5. CÁC ĐIỀU CẤM TRONG GIAO DIỆN (UI ANTI-PATTERNS)

1. ❌ **Cấm dùng gradient hỗn loạn**: Tuyệt đối không dùng `from-indigo-600 via-purple-600 to-pink-600` làm nền nút bấm hay thẻ card.
2. ❌ **Cấm bao gói Auth trong Sidebar Drawer**: Màn hình Login/Register phải luôn là giao diện chuyên biệt toàn màn hình (Split-Screen).
3. ❌ **Cấm bỏ rơi Script bên ngoài Section**: Khi sử dụng Thymeleaf decorator, toàn bộ logic JS trang con phải nằm bên trong thẻ section được replace.
4. ❌ **Cấm dropdown cứng nhắc khi người dùng có nhu cầu tùy biến**: Tôn trọng triết lý tự do BYOC (Bring Your Own Course) cho sinh viên mọi trường.
