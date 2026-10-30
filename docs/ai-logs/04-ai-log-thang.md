# NHẬT KÝ THỰC NGHIỆM AI - THẮNG (MODULE AI STUDY PLANNER)

**Thành viên thực hiện**: Thắng  
**Module được giao**: Module 4 - AI Study Planner (`com.group2.assignment.planner`)  
**Công cụ AI sử dụng**: opencode / trợ lý AI lập trình

---

## GIAI ĐOẠN 1: PHÂN TÍCH TASK VÀ HIỆN TRẠNG CODEBASE

**Mục tiêu**: Đọc `docs/tasks/task-2026-10-30.md`, xác định đúng phạm vi task Planner: dùng System Key tập trung, khóa tính năng với gói FREE, mở khóa cho PRO/CAMPUS, bổ sung test.

**Câu prompt thực tế**:
```text
đọc task của Thắng trong D:\HSF302\Smart-StudyHub-RAG\docs\tasks\task-2026-10-30.md,
phân tích code base hiện tại và yêu cầu, lên plan thực hiện chỉ ra các chỗ cần chỉnh sửa
và thêm mới, báo cáo lại không code
```

**AI đã giúp như thế nào**:
- Chỉ ra `StudyPlannerService` đã dùng `SystemAiKeyService` nhưng vẫn còn tham số `HttpSession` dư.
- Phát hiện `PlannerController` chưa có feature gating và template `planner/index.html` chưa có Locked Glass Overlay.
- Phát hiện `application.properties` có dòng `studyhub.ai.gemini-api-key=` rỗng ghi đè cấu hình phía trên, khiến Planner luôn rơi vào mock timeline.
- Cảnh báo branch `feature/study-planner` cũ đã phân kỳ với `main`, cần tạo lại từ `main` mới nhất.

---

## GIAI ĐOẠN 2: TRIỂN KHAI FEATURE GATING Ở BACK-END

**Mục tiêu**: Chặn gói FREE gọi `POST /planner/generate`, đồng thời đưa trạng thái gói cước ra model để giao diện hiển thị overlay.

**Câu prompt thực tế**:
```text
tiến hành thực hiện theo plan và chỉnh sửa trên nhánh theo như quy định trong file task-2026-10-30.md
```

**AI đã giúp như thế nào**:
- Tạo lại nhánh `feature/study-planner` từ `main`, giữ branch cũ thành `feature/study-planner-legacy` để tránh mất commit.
- `PlannerController`: thêm helper lấy `currentUser` từ session, kiểm tra `user.isPro()`, đưa các model attribute `isPro`, `plannerLocked`, `subscriptionTier` cho cả trang `/planner` và `/planner/history/{courseId}`.
- Chặn `POST /planner/generate` với FREE: gắn flash `error` + `showUpgradeModal`, redirect về `/planner`, không gọi service.
- `StudyPlannerService`: bỏ tham số `HttpSession`, chỉ dùng `SystemAiKeyService.getApiKey()`.
- `application.properties`: bỏ dòng override rỗng để biến môi trường `GEMINI_API_KEY` thực sự đi vào System Key.

**Đánh giá & điều chỉnh của bản thân**: Giữ nguyên `User.isPro()` để ADMIN/CAMPUS cũng được xem là Pro, đúng với nghiệp vụ BR-TIER-005.

---

## GIAI ĐOẠN 3: THIẾT KẾ LOCKED GLASS OVERLAY VÀ MODAL NÂNG CẤP

**Mục tiêu**: Người dùng FREE thấy rõ tính năng bị khóa nhưng vẫn hiểu giá trị sản phẩm; PRO/CAMPUS thấy badge và form hoạt động bình thường.

**Câu prompt thực tế**:
```text
Cập nhật planner/index.html: locked glass overlay, badge gói, modal nâng cấp
```

**AI đã giúp như thế nào**:
- Thêm badge `PRO`/`FREE` cạnh tiêu đề form.
- Bọc form trong container `relative`, thêm overlay `backdrop-blur-md` với icon ổ khóa, mô tả và CTA "Nâng cấp Gói Pro chỉ 49k/tháng để kích hoạt AI Study Planner".
- Form bị làm mờ, vô hiệu hóa thao tác bằng `pointer-events-none`, nút submit bị `disabled` khi `plannerLocked`.
- Thêm modal nâng cấp cục bộ dùng chung `id="modal-pricing-upgrade"` để navbar hiện có vẫn mở được modal, kèm nút POST `/auth/upgrade-pro` và link xem bảng giá đầy đủ.
- Thêm script tự mở modal khi có flash `showUpgradeModal` sau khi người dùng FREE cố gắng sinh kế hoạch.

---

## GIAI ĐOẠN 4: VIẾT TEST VÀ GỠ LỖI MÔI TRƯỜNG

**Mục tiêu**: Chứng minh FREE bị chặn, PRO/CAMPUS/ADMIN được mở khóa, trang lịch sử nhận đúng trạng thái gói và khách chưa đăng nhập bị chuyển về login.

**Câu prompt thực tế**:
```text
Thêm test PlannerFeatureGatingTest và chạy mvn clean test
```

**AI đã giúp như thế nào**:
- Tạo `PlannerFeatureGatingTest` với 7 ca kiểm thử dùng MockMvc + session + mock `StudyPlannerService` để không gọi Gemini thật.
- Kiểm tra FREE thấy overlay, FREE gọi generate bị redirect và không gọi service; PRO sinh kế hoạch thành công; CAMPUS/ADMIN mở khóa; history mang đúng model attribute; guest bị interceptor chặn.

**Vấn đề gặp phải và cách xử lý**:
- Máy chạy mặc định JDK 26, Mockito/Byte Buddy chưa hỗ trợ nên test dùng `@MockBean` báo lỗi instrument class. Đã chạy Maven bằng `JAVA_HOME=C:\Program Files\Java\jdk-17` đúng với `java.version=17` của dự án.
- Biến môi trường `GEMINI_API_KEY` đang có sẵn trên máy làm `LlmKeyRotatorTest` và `RagEngineServiceTest` (module RAG) thay đổi số key và hành vi fallback. Đã xóa biến môi trường trong phiên chạy test để tái hiện môi trường CI sạch.

**Kết quả kiểm thử**:
- Riêng module Planner: 7/7 test đạt.
- Toàn bộ dự án: 91/91 test đạt, `BUILD SUCCESS` với JDK 17 và môi trường không có `GEMINI_API_KEY`.

---

## ĐÚC KẾT KINH NGHIỆM KHI DÙNG AI

- AI hữu ích nhất ở bước đối chiếu task với codebase: chỉ ra phần nào đã có, phần nào còn thiếu và các lỗi cấu hình dễ bị bỏ qua như dòng property rỗng ghi đè key.
- Để AI sinh code đúng ý, cần nêu rõ: file cần sửa, hành vi mong muốn, quy tắc nghiệp vụ (BR-TIER-005), và yêu cầu không phá vỡ các module khác.
- Luôn chạy test với đúng JDK của dự án và kiểm tra biến môi trường trước khi kết luận build thất bại.
