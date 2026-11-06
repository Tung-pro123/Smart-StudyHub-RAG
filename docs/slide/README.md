# 📊 TÀI LIỆU SOẠN THẢO SLIDE THUYẾT TRÌNH - GROUP 2 (HSF302)

> **Dự án**: SMART STUDYHUB RAG — Nền Tảng Học Tập Đa Môn & Luyện Thi A+ Ứng Dụng RAG AI  
> **Chủ trì**: Lê Thanh Tùng (Team Lead)  
> **Quy chuẩn thuyết trình**: **6 Khối Liền Mạch (Sequential Contiguous Blocks) — Mỗi người lên nói ĐÚNG 1 LẦN duy nhất**, nói dứt điểm toàn bộ khối slide của mình rồi chuyển giao mic cho thành viên tiếp theo.

---

## 📁 CẤU TRÚC THƯ MỤC LƯU TRỮ

Thư mục `docs/slide/` được phân tách khoa học thành 2 phần độc lập cho 6 thành viên:

```
docs/slide/
├── README.md                          <-- Tài liệu hướng dẫn & quy chuẩn chung
│
├── content/                           <-- NỘI DUNG CHỮ & DANH MỤC ẢNH TRÊN SLIDE (Bullet points, Diagram, Ảnh thật)
│   ├── truong/                        <-- Khối 1: Slide 1 ➔ 4 (Ngô Nhật Trường - 4 phút) [Mở màn, Bối cảnh & Cổng vào Auth/UI]
│   ├── tung/                          <-- Khối 2: Slide 5 ➔ 8 (Lê Thanh Tùng - 4 phút) [Kiến trúc Tech Stack, 3 Gói Cước & Squad Hub]
│   ├── binh/                          <-- Khối 3: Slide 9 ➔ 11 (Nguyễn Văn Gia Bình - 3 phút) [RAG Engine & PDF Chunking]
│   ├── nhat/                          <-- Khối 4: Slide 12 ➔ 14 (Huỳnh Nhật - 2.5 phút) [Quiz Generator & Xuất PDF]
│   ├── thang/                         <-- Khối 5: Slide 15 ➔ 17 (Lê Nguyễn Minh Thắng - 2.5 phút) [Study Planner & Tasks]
│   └── an/                            <-- Khối 6: Slide 18 ➔ 22 (Ngô Ân - 4 phút) [Admin, Doanh Thu VNPay, QA 333 tests & Setup]
│
└── script/                            <-- KỊCH BẢN LỜI THOẠI NÓI THỰC TẾ (Word-by-word, câu chào, câu chốt chuyển mic)
    ├── truong/                        <-- Lời thoại mở màn, Bối cảnh sinh viên mùa thi, Assignment UI Toolkit, AI Agent IDE, OAuth2 & Logout
    ├── tung/                          <-- Lời thoại kiến trúc Enterprise Tech Stack, 3 gói cước SaaS, Shared Squad Hub & Feature Gating
    ├── binh/                          <-- Lời thoại Apache PDFBox Chunking, Context Grounding & Citations đối chứng
    ├── nhat/                          <-- Lời thoại AI Quiz Generator, Countdown Timer & Xuất đề thi PDF in ấn
    ├── thang/                         <-- Lời thoại AI Study Planner theo tuần, Checklist & Progress Bar
    └── an/                            <-- Lời thoại Admin Dashboard, Doanh thu VNPay & Đối soát Merchant, QA 333 tests & Q&A
```

---

## 🎯 QUY TẮC SOẠN SLIDE & LUYỆN NÓI

1. **Thư mục `content/`**:
   * Chỉ đưa các gạch đầu dòng cô đọng (Bullet points), từ khóa nổi bật, sơ đồ kiến trúc và ảnh chụp màn hình thực tế từ `http://localhost:8080`.
   * **Tuyệt đối không nhồi nhét văn bản dài** lên mặt slide để tránh làm rối mắt người xem.

2. **Thư mục `script/`**:
   * Viết văn phong nói tự nhiên, súc tích, dứt khoát.
   * Có câu chào hỏi đầu khối, các điểm nhấn giọng điệu và **câu chào kết thúc chuyển mic trang trọng** cho bạn tiếp theo.
   * Canh đúng thời lượng quy định: Tốc độ nói trung bình ~120 - 140 từ/phút.

3. **Nguyên tắc ảnh minh chứng**:
   * Bắt buộc chụp màn hình từ hệ thống đang chạy (`localhost:8080`) sau khi tự mình chạy lại (re-run) luồng chức năng.
   * Không dùng hình ảnh mockup vẽ tay hay dữ liệu ảo.

