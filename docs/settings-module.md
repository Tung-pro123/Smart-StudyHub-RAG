# Module Settings — Ân

## Sử dụng

1. Đăng nhập rồi mở `/settings` từ menu API Key.
2. Chọn Gemini, Groq hoặc OpenAI, nhập key của chính nhà cung cấp đó.
3. Bấm **Kiểm tra kết nối** để gửi một câu `Reply OK.`. Thao tác này không lưu key và có thể tiêu thụ quota/phí của tài khoản.
4. Bấm **Lưu vào phiên** để lưu key. Lưu chỉ kiểm tra định dạng, không xác nhận quyền truy cập API; nên ping để xác nhận.
5. Sau khi lưu, để ô key trống để kiểm tra lại key đã lưu của cùng provider. Khi đổi provider phải nhập key mới.
6. **Xóa key đã lưu** chỉ xóa cấu hình AI. Đăng xuất hoặc session hết hạn cũng xóa key.

## Hợp đồng tích hợp

- `GET /settings`: trang Thymeleaf dùng master layout hiện tại; không trả key đã lưu về HTML.
- `POST /settings/save`: form `provider`, `apiKey`, `csrfToken`; redirect về trang Settings với thông báo.
- `POST /settings/test-ping` (alias `/settings/test-connection`): form tương tự; JSON `{success, latencyMs, message}`. Key trống chỉ sử dụng lại key trong session nếu provider trùng khớp.
- `POST /settings/clear`: form `csrfToken`; xóa cấu hình và redirect.
- Các tuyến Settings dùng `JwtAuthInterceptor` hiện có. Token chống CSRF được tạo khi mở trang; các POST thiếu/sai token trả HTTP 403. Phản hồi Settings có `Cache-Control: no-store`.
- Module khác có thể inject `ApiKeyService` rồi gọi `getApiKey(session)` và `getProvider(session)`. Cần kiểm tra provider trước khi gọi API tương ứng; việc gọi AI ở RAG/Planner/Quiz thuộc các module đó.
- `SettingsConstants.SESSION_API_KEY` vẫn là `GEMINI_API_KEY` để tương thích các module Gemini. Chỉ có giá trị khi chọn Gemini; đổi sang provider khác sẽ xóa giá trị này để tránh gửi nhầm key tới Google.
- Provider và key chung nằm ở `SESSION_PROVIDER`, `SESSION_PROVIDER_API_KEY`. Không dùng database, file cấu hình, localStorage hay cookie để lưu key. DTO không sinh `toString()` chứa key.
- Key được gửi trong header `x-goog-api-key` (Gemini) hoặc `Authorization: Bearer` (Groq/OpenAI), không nằm trong URL. Không ghi log key hoặc trả nội dung lỗi gốc của nhà cung cấp.
- Timeout kết nối 5 giây, đọc phản hồi 15 giây. HTTP 400/401/403/404/429/5xx và lỗi mạng được chuyển thành thông báo tiếng Việt.

## Model và tài liệu API

- Gemini mặc định: `gemini-3.5-flash-lite`. Có thể đổi bằng `SETTINGS_GEMINI_MODEL` hoặc property `settings.gemini.model`; không đưa API key vào property này. Model cũ `gemini-1.5-flash` trong khung dự án đã được thay thế theo [lịch vòng đời Gemini](https://ai.google.dev/gemini-api/docs/deprecations).
- Gemini: [generateContent API](https://ai.google.dev/api/generate-content).
- Groq: `llama-3.3-70b-versatile`, [API reference](https://console.groq.com/docs/api-reference).
- OpenAI: `gpt-4.1-mini`, [model reference](https://developers.openai.com/api/docs/models/gpt-4.1-mini).

## Kiểm thử

`mvn test` chạy toàn bộ suite. Các test Settings sử dụng HTTP giả lập, không cần API key thật và không phát sinh phí. Chúng kiểm tra routing/header/payload, lỗi nhà cung cấp, cách ly session, đổi provider, xóa key, CSRF, đăng xuất và render template không lộ key.

Chưa thực hiện ping với key thật trong phiên làm việc này. Khi demo, dùng key cá nhân để xác nhận quota và quyền truy cập model trên tài khoản thực tế.
