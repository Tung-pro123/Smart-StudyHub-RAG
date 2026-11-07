# Kế hoạch thực hiện phần Ngô Ân — 04/11/2026

Nguồn phân công: [kế hoạch tổng duyệt](task-2026-11-04-final-presentation-slide-and-rag-fix.md). Người dùng xác nhận phụ trách Admin, QA, slide 18–22. Không nhận phần sửa RAG của Bình.

Thực hiện tuần tự; chỉ sửa code khi tái hiện được lỗi thuộc phạm vi, kiểm tra caller và chạy test liên quan trước khi sang task tiếp theo.

1. [x] **AN-FINAL-01 — Dashboard:** chạy lại, đối chiếu KPI, chụp ảnh thật. Báo cáo: PASS.
2. [x] **AN-FINAL-02 — Gói cước và quota:** nâng/hạ FREE/PRO/SQUAD, reset quota, tải lại xác minh. Báo cáo: PASS.
3. [x] **AN-FINAL-03 — System Key:** masking và ping Gemini thật PASS, 1206 ms trên cổng 6969, có ảnh kết quả đã che khóa.
4. [x] **AN-FINAL-04 — QA:** chạy toàn bộ test, lưu số thực tế. Kết quả 329/329 PASS, riêng Admin 40/40; không dùng số 327 cũ.
5. [x] **AN-FINAL-05 — Slide 18–22:** đã sửa content/script, bổ sung ảnh và kết quả ping thật cùng hướng dẫn setup. Thời lượng dự kiến 3,5 phút, một khối liên tục.
6. [x] **AN-FINAL-06 — Bàn giao:** đã soát phạm vi, liên kết tài liệu và bằng chứng; bàn giao bằng nhánh riêng `feature/admin-final-presentation-an` và PR vào main, không push thẳng main. Trạng thái PR tra trên GitHub.

Không cần thay đổi mã nguồn runtime cho các task đã PASS. Tận dụng nội dung/lời thoại có sẵn trong `docs/slide/`; không viết lại bộ slide của thành viên khác.

Báo cáo chi tiết: [minh chứng ngày 04/11](../evidence/2026-11-04-final-presentation/an/README.md).
