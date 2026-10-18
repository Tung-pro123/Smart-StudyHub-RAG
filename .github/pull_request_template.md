## 📌 Mô tả công việc (Pull Request Description)
- [Mô tả chi tiết những gì bạn đã làm trong nhánh này]
- [Ví dụ: Xây dựng giao diện upload tài liệu PDF và tích hợp Apache PDFBox để trích xuất văn bản]

## 🎯 Module phụ trách
- [ ] `core`: Khởi tạo Entity Course, Chunk và cấu trúc dữ liệu dùng chung
- [ ] `rag`: Trích xuất PDF, RAG Engine và Chatbot hỏi đáp giáo trình (Bình)
- [ ] `settings`: Giao diện cấu hình API Key và kiểm tra kết nối AI (Ân)
- [ ] `planner`: Lên kế hoạch ôn tập thông minh Study Planner (Thắng)
- [ ] `quiz`: Bộ sinh trắc nghiệm tự động và chấm điểm tức thì (Nhật)
- [ ] `docs`: Tài liệu hướng dẫn User Guide và Slide thuyết trình (Trường)

## 📋 Checklist kiểm tra trước khi tạo PR
- [ ] Tiêu đề PR tuân thủ đúng chuẩn Conventional Commits bằng **100% Tiếng Việt** (Ví dụ: `feat(quiz): hoàn thiện bộ sinh trắc nghiệm và chấm điểm tự động`)
- [ ] Tên nhánh đúng chuẩn: `feature/<tên-chức-năng>` hoặc `fix/<tên-lỗi>`
- [ ] Đã chạy `mvn clean test` ở máy local và BUILD SUCCESS
- [ ] Không sửa đổi các file ngoài phạm vi module được phân công
