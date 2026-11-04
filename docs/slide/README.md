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
│   ├── tung/                          <-- Slide 1 ➔ 4 (Lê Thanh Tùng - 4 phút)
│   ├── truong/                        <-- Slide 5 ➔ 8 (Ngô Nhật Trường - 3 phút)
│   ├── binh/                          <-- Slide 9 ➔ 11 (Nguyễn Văn Gia Bình - 3 phút)
│   ├── nhat/                          <-- Slide 12 ➔ 14 (Huỳnh Nhật - 2.5 phút)
│   ├── thang/                         <-- Slide 15 ➔ 17 (Lê Nguyễn Minh Thắng - 2.5 phút)
│   └── an/                            <-- Slide 18 ➔ 22 (Ngô Ân - 3.5 phút)
│
└── script/                            <-- KỊCH BẢN LỜI THOẠI NÓI THỰC TẾ (Word-by-word, câu chào, câu chốt chuyển mic)
    ├── tung/                          <-- Lời thoại mở đầu, bối cảnh, 3 gói cước, Squad Hub & Feature Gating
    ├── truong/                        <-- Lời thoại Assignment UI Toolkit, AI Agent IDE & OAuth2
    ├── binh/                          <-- Lời thoại Apache PDFBox Chunking, Context Grounding & Citations
    ├── nhat/                          <-- Lời thoại AI Quiz Generator, Countdown Timer & Xuất PDF
    ├── thang/                         <-- Lời thoại AI Study Planner theo tuần & Quản lý Task
    └── an/                            <-- Lời thoại Admin Dashboard, Masking Key, QA 327 tests, User Guide & Q&A
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

