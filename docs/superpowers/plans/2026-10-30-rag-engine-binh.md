# Kế Hoạch Triển Khai Module RAG Chatbot - Centralized System Key, Multi-Key Rotator & Hạn Ngạch 50 Câu/Ngày (Task của Bình)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Hiện thực hóa toàn diện nhiệm vụ của Bình trong Sprint 30/10/2026: Chuyển đổi RAG Chatbot sang Centralized System Key tập trung, tích hợp cơ chế xoay vòng Multi-Key (Round-Robin & Cooldown khi chạm 429), hỗ trợ tùy chọn LiteLLM/OpenAI endpoint, kiểm soát hạn ngạch 50 câu/ngày cho gói FREE và cập nhật giao diện trực quan theo thời gian thực.

**Architecture:**
- **Centralized Multi-Key Rotator:** Quản lý danh sách Google Gemini API Keys (`studyhub.ai.gemini-api-keys`), điều phối Round-Robin giữa các key để phân tải (tránh chạm mốc 15 RPM của Gemini Free), tự động Cooldown 60s và Failover sang key kế tiếp khi nhận mã lỗi HTTP 429 / RESOURCE_EXHAUSTED.
- **LiteLLM / OpenAI Gateway Compatibility:** Hỗ trợ cấu hình `studyhub.ai.base-url`. Nếu có giá trị (trỏ tới LiteLLM Proxy), hệ thống tự động giao tiếp qua chuẩn OpenAI `/v1/chat/completions`; nếu không có, gọi trực tiếp Gemini REST API.
- **Quota Engine Integration:** Tự động reset hạn ngạch khi sang ngày mới (`lastRagResetDate`), giới hạn 50 câu/ngày đối với gói `FREE`, mở khóa không giới hạn đối với `PRO`/`ROLE_ADMIN`.
- **Dynamic Real-time UI:** `ChatResponseDto` trả về `remainingQuota` giúp giao diện JavaScript cập nhật thanh tiến trình hạn ngạch ngay lập tức mà không cần F5.

**Tech Stack:** Spring Boot 3.x, Spring Web (RestClient), Spring Data JPA, H2 Database, Thymeleaf, TailwindCSS, DaisyUI, JUnit 5, Mockito.

**Spec / Reference:** [task-2026-10-30.md](file:///c:/Users/admin/Documents/A_FPT/Tempory_Project/Smart-StudyHub-RAG/docs/tasks/task-2026-10-30.md) - Section 4: Bình (Module RAG Chatbot).

## Global Constraints
- **Package Scope:** `com.group2.assignment.rag` (với các trường bổ trợ tối thiểu trong `auth`).
- **Nhánh Git:** `feature/rag-engine`.
- **Ngôn ngữ phản hồi AI / Thông báo lỗi:** Tiếng Việt thân thiện, rõ ràng, chuẩn Markdown.
- **Tương thích ngược:** Giữ nguyên các chức năng tìm kiếm ngữ nghĩa Chunks (TF-IDF/Term Overlap), Citations và định dạng câu trả lời hiện có.

---

### Task 1: Khởi Tạo Nhánh Git & Bổ Sung Các Trường Hạn Ngạch Vào User Entity

**Files:**
- Modify: `src/main/java/com/group2/assignment/auth/entity/User.java`
- Modify: `src/main/java/com/group2/assignment/auth/service/AuthService.java`
- Test: `src/test/java/com/group2/assignment/auth/AuthServiceTest.java`

**Interfaces:**
- `User.java`: Thêm các thuộc tính `subscriptionTier` (String, default "FREE"), `dailyRagCount` (Integer, default 0), `lastRagResetDate` (LocalDate), `tierExpiresAt` (LocalDateTime).
- `AuthService.java`: Nạp 3 tài khoản demo `admin` (PRO), `student` (FREE), `pro_student` (PRO).

- [ ] **Step 1: Tạo và chuyển sang nhánh git `feature/rag-engine`**
  ```powershell
  git checkout -b feature/rag-engine
  ```

- [ ] **Step 2: Cập nhật `User.java` với các trường hạn ngạch gói cước**
  Bổ sung vào `User.java`:
  ```java
  @Column(length = 20)
  @Builder.Default
  private String subscriptionTier = "FREE"; // "FREE", "PRO", "CAMPUS"

  @Builder.Default
  private Integer dailyRagCount = 0; // Đếm số câu chat RAG trong ngày

  private LocalDate lastRagResetDate; // Ngày reset gần nhất

  private LocalDateTime tierExpiresAt; // Ngày hết hạn gói Pro
  ```
  Trong `@PrePersist protected void onCreate()`:
  ```java
  if (this.subscriptionTier == null || this.subscriptionTier.isBlank()) {
      this.subscriptionTier = "FREE";
  }
  if (this.dailyRagCount == null) {
      this.dailyRagCount = 0;
  }
  if (this.lastRagResetDate == null) {
      this.lastRagResetDate = LocalDate.now();
  }
  ```

- [ ] **Step 3: Cập nhật seed tài khoản demo chuẩn trong `AuthService.java`**
  Đảm bảo tại `initDemoUsers()`:
  - `admin`: `role = "ROLE_ADMIN"`, `subscriptionTier = "PRO"`, `dailyRagCount = 0`, `lastRagResetDate = LocalDate.now()`.
  - `student`: `role = "ROLE_STUDENT"`, `subscriptionTier = "FREE"`, `dailyRagCount = 0`, `lastRagResetDate = LocalDate.now()`.
  - `pro_student`: `role = "ROLE_STUDENT"`, `subscriptionTier = "PRO"`, `dailyRagCount = 0`, `lastRagResetDate = LocalDate.now()`.

- [ ] **Step 4: Chạy kiểm thử xác minh `AuthServiceTest`**
  ```powershell
  mvn test -Dtest=AuthServiceTest
  ```
  Xác nhận `BUILD SUCCESS`.

---

### Task 2: Xây Dựng Multi-Key Rotator Engine & Cấu Hình Centralized System Key

**Files:**
- Create: `src/main/java/com/group2/assignment/rag/config/LlmProperties.java`
- Create: `src/main/java/com/group2/assignment/rag/service/LlmKeyRotator.java`
- Modify: `src/main/resources/application.properties`
- Test: `src/test/java/com/group2/assignment/rag/LlmKeyRotatorTest.java`

**Interfaces:**
- `LlmProperties`: chứa `geminiApiKeys` (List<String>), `geminiApiKey` (String), `baseUrl` (String, optional), `model` (String, default "gemini-2.5-flash").
- `LlmKeyRotator`:
  - `public String getNextKey()`: Lấy key theo Round-Robin, bỏ qua các key đang trong thời gian Cooldown.
  - `public void markKeyFailed(String key, int statusCode, String message)`: Đánh dấu key gặp lỗi 429 vào Cooldown Map (thời gian nghỉ 60s).
  - `public boolean hasConfiguredKeys()`: Kiểm tra xem hệ thống đã nạp ít nhất 1 key hay chưa.
  - `public int getTotalKeysCount()`: Tổng số key đã cấu hình.
  - `public int getHealthyKeysCount()`: Số lượng key đang khả dụng (không bị cooldown).

- [ ] **Step 1: Viết test failing cho `LlmKeyRotatorTest`**
  Kiểm tra:
  - Khởi tạo với danh sách 3 keys: key1, key2, key3.
  - Round-robin: Lần lượt gọi `getNextKey()` sẽ trả về key1 -> key2 -> key3 -> key1.
  - Failover & Cooldown: Khi `markKeyFailed(key1, 429, "ResourceExhausted")`, lần gọi tiếp theo sẽ bỏ qua key1 và chỉ xoay giữa key2 và key3.
  - Phục hồi: Sau khi hết thời gian cooldown (hoặc test với mock clock), key1 trở lại hoạt động bình thường.

- [ ] **Step 2: Viết `LlmProperties.java`**
  Map các properties từ `application.properties`:
  ```java
  @ConfigurationProperties(prefix = "studyhub.ai")
  ```
  Hỗ trợ đọc key từ:
  - `studyhub.ai.gemini-api-keys` (phân tách bởi dấu phẩy)
  - fallback sang `studyhub.ai.gemini-api-key`
  - fallback sang biến môi trường `GEMINI_API_KEYS` / `GEMINI_API_KEY`
  - `studyhub.ai.base-url` (LiteLLM hoặc OpenAI proxy endpoint)
  - `studyhub.ai.model` (mặc định "gemini-2.5-flash")

- [ ] **Step 3: Hiện thực hóa `LlmKeyRotator.java`**
  - Quản lý danh sách key dạng `List<String>`.
  - Dùng `AtomicInteger currentIndex` để Round-Robin an toàn đa luồng.
  - Dùng `ConcurrentHashMap<String, Instant> cooldownMap` lưu thời điểm hết hạn cooldown.
  - Nếu tất cả key đều bị cooldown, tự động fallback trả về key có thời gian cooldown sớm nhất để thử lại thay vì crash.

- [ ] **Step 4: Cập nhật `application.properties`**
  ```properties
  # ==========================================
  # CẤU HÌNH CENTRALIZED SYSTEM AI KEY & LLM ROTATOR
  # ==========================================
  # Danh sách Gemini API Keys cách nhau bởi dấu phẩy (hoặc 1 key đơn)
  studyhub.ai.gemini-api-keys=${GEMINI_API_KEYS:${GEMINI_API_KEY:}}
  studyhub.ai.gemini-api-key=
  # Tùy chọn LiteLLM Proxy / OpenAI-compatible endpoint (để trống nếu gọi trực tiếp Google Gemini)
  studyhub.ai.base-url=${LITELLM_BASE_URL:}
  studyhub.ai.model=gemini-2.5-flash
  ```

- [ ] **Step 5: Chạy test xác minh `LlmKeyRotatorTest`**
  ```powershell
  mvn test -Dtest=LlmKeyRotatorTest
  ```
  Xác nhận test pass 100%.

---

### Task 3: Tái Cấu Trúc RagEngineService với Centralized Multi-Key & LiteLLM Gateway

**Files:**
- Modify: `src/main/java/com/group2/assignment/rag/service/RagEngineService.java`
- Modify: `src/main/java/com/group2/assignment/rag/dto/ChatResponseDto.java`
- Modify: `src/test/java/com/group2/assignment/rag/RagEngineServiceTest.java`

**Interfaces:**
- `ChatResponseDto`: Thêm `remainingQuota` (Integer), `dailyRagCount` (Integer), `subscriptionTier` (String).
- `RagEngineService`:
  - Loại bỏ hoàn toàn sự phụ thuộc vào `HttpSession` để lấy API key.
  - `public ChatResponseDto chatWithCourse(Long courseId, Long sessionId, String question, User user)`
  - `callLlmWithRotation(String prompt)`: Tự động lặp qua các key qua `LlmKeyRotator`. Nếu gặp lỗi 429 hoặc lỗi kết nối, đánh dấu cooldown và thử ngay key tiếp theo (tối đa bằng số lượng key khả dụng).
  - Nếu có cấu hình `studyhub.ai.base-url`, gọi endpoint OpenAI `/v1/chat/completions`. Nếu không, gọi trực tiếp Gemini REST API.

- [ ] **Step 1: Cập nhật `ChatResponseDto.java`**
  Thêm các trường:
  ```java
  private Integer remainingQuota;
  private Integer dailyRagCount;
  private String subscriptionTier;
  ```

- [ ] **Step 2: Cập nhật `RagEngineService.java`**
  - Inject `LlmKeyRotator` và `LlmProperties`.
  - Bỏ code lấy key từ `HttpSession` và `SettingsConstants.SESSION_API_KEY`.
  - Xây dựng phương thức `callLlmWithFailover(String prompt)`:
    - Vòng lặp thử qua các healthy keys từ `LlmKeyRotator.getNextKey()`.
    - Phân nhánh:
      - Khi `baseUrl` có giá trị: gửi request POST JSON theo format OpenAI Chat (`{"model": "...", "messages": [{"role": "user", "content": prompt}]}`).
      - Khi không có `baseUrl`: gửi request POST tới Google Gemini REST API (`https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}`).
    - Bắt lỗi HTTP 429 / RESOURCE_EXHAUSTED: gọi `llmKeyRotator.markKeyFailed(key, 429, errorMsg)`, log cảnh báo và retry với key kế tiếp.
  - Nếu hệ thống chưa cấu hình bất kỳ key nào (`!llmKeyRotator.hasConfiguredKeys()`), trả về thông báo fallback thân thiện: *"⚠️ Hệ thống chưa được cấu hình Centralized System AI Key. Vui lòng liên hệ Quản trị viên hệ thống."*

- [ ] **Step 3: Cập nhật và chạy kiểm thử `RagEngineServiceTest`**
  - Điều chỉnh test case `testChatFallbackWhenNoApiKey` sang kiểm tra trường hợp Centralized System Key chưa được cấu hình.
  - Kiểm tra thuật toán RAG Chunks, Citations và System Prompt vẫn hoạt động chính xác.
  ```powershell
  mvn test -Dtest=RagEngineServiceTest
  ```
  Xác nhận test pass 100%.

---

### Task 4: Kiểm Soát Hạn Ngạch 50 Câu/Ngày & Tự Động Reset Ngày Mới Trong ChatController

**Files:**
- Modify: `src/main/java/com/group2/assignment/rag/controller/ChatController.java`
- Modify: `src/main/java/com/group2/assignment/auth/repository/UserRepository.java`
- Create: `src/test/java/com/group2/assignment/rag/ChatControllerQuotaTest.java`

**Business Logic Requirements:**
1. Lấy `currentUser` từ Session (`AuthController.SESSION_USER`). Tải lại bản mới nhất từ `UserRepository` để đảm bảo dữ liệu hạn ngạch luôn chính xác.
2. Tự động Reset ngày mới:
   - Nếu `user.getLastRagResetDate() == null` hoặc `LocalDate.now().isAfter(user.getLastRagResetDate())`:
     - Gán `user.setDailyRagCount(0);`
     - Gán `user.setLastRagResetDate(LocalDate.now());`
     - Lưu `userRepository.save(user);`
3. Kiểm tra hạn ngạch gói cước:
   - Nếu `subscriptionTier.equalsIgnoreCase("FREE")` và `user.getDailyRagCount() >= 50`:
     - Chặn gửi request sang AI.
     - Trả về `ChatResponseDto` với:
       - `success = false`
       - `answer = "🛑 Bạn đã sử dụng hết 50 câu hỏi RAG miễn phí hôm nay. Hãy nâng cấp Gói Pro để chat không giới hạn!"`
       - `remainingQuota = 0`
       - `dailyRagCount = user.getDailyRagCount()`
       - `subscriptionTier = "FREE"`
4. Nếu hợp lệ:
   - Gọi `ragEngineService.chatWithCourse(courseId, sessionId, question, user)`.
   - Nếu trả lời thành công (`response.isSuccess()`):
     - Tăng `user.setDailyRagCount(user.getDailyRagCount() + 1);`
     - Lưu `userRepository.save(user);`
     - Cập nhật lại session `currentUser`.
     - Điền `remainingQuota = isFree ? (50 - user.getDailyRagCount()) : -1` (giá trị -1 đại diện cho Unlimited).
5. Trong `GET /courses/{courseId}/chat`:
   - Truyền vào Model các giá trị: `currentUser`, `dailyRagCount`, `remainingQuota`, `subscriptionTier`, `hasSystemKey = llmKeyRotator.hasConfiguredKeys()`.

- [ ] **Step 1: Viết test failing cho `ChatControllerQuotaTest`**
  - Case 1: Sinh viên Free có `dailyRagCount = 49` -> gửi câu hỏi thành công, `dailyRagCount` tăng lên 50, `remainingQuota` còn 0.
  - Case 2: Sinh viên Free có `dailyRagCount = 50` -> bị chặn, trả về thông báo yêu cầu nâng cấp gói Pro, không gọi LLM.
  - Case 3: Sinh viên Pro có `dailyRagCount = 100` -> vẫn gửi bình thường, `remainingQuota = -1` (unlimited).
  - Case 4: Sinh viên Free có `lastRagResetDate` là ngày hôm qua và `dailyRagCount = 50` -> tự động reset về 0 và cho phép gửi câu hỏi bình thường.

- [ ] **Step 2: Hiện thực hóa logic kiểm soát hạn ngạch trong `ChatController.java`**
  - Inject `UserRepository` và `LlmKeyRotator`.
  - Cập nhật `showChatPage` để đưa thông số hạn ngạch vào `model`.
  - Cập nhật `sendChatMessageJson` với logic kiểm tra ngày mới, kiểm tra quota, tăng đếm và phản hồi.

- [ ] **Step 3: Chạy test xác minh `ChatControllerQuotaTest`**
  ```powershell
  mvn test -Dtest=ChatControllerQuotaTest
  ```
  Xác nhận tất cả 4 cases pass.

---

### Task 5: Cập Nhật Giao Diện Chat (Thymeleaf & Realtime JavaScript)

**Files:**
- Modify: `src/main/resources/templates/rag/chat.html`

**UI Requirements:**
1. **Header Bar Status:**
   - Thay thế nhãn cảnh báo cá nhân ("Chưa cài API Key") bằng:
     - Badge xanh: *"🤖 AI System Engine: Đã Sẵn Sàng"* (nếu `hasSystemKey == true`).
     - Badge cảnh báo nếu hệ thống chưa cài key.
   - Thêm Thẻ Hạn Ngạch Sinh Viên:
     - Với gói `FREE`: Hiển thị thanh tiến trình trực quan:
       *"Hôm nay còn **X/50** câu hỏi RAG miễn phí"* kèm thanh progress bar đổi màu (xanh khi >20, vàng khi 5-20, đỏ khi <5) và nút bấm nhanh *"⚡ Nâng cấp PRO"*.
     - Với gói `PRO` / `ADMIN`: Hiển thị badge phát sáng tím:
       *"⚡ Gói PRO: Chat RAG Không Giới Hạn"*.
2. **Realtime JavaScript Update:**
   - Trong sự kiện xử lý JSON trả về từ `/courses/{id}/chat/send`:
     - Khi nhận được `data.remainingQuota`: Cập nhật ngay con số hiển thị trên giao diện và độ rộng của thanh tiến trình (progress bar).
     - Nếu `data.remainingQuota === 0` (hết lượt):
       - Hiển thị thông báo Toast cảnh báo hết hạn ngạch.
       - Tự động hiển thị modal / banner mời nâng cấp Pro.
       - Disable ô nhập tin nhắn và nút gửi, gợi ý quay lại vào ngày mai hoặc nâng cấp gói.

- [ ] **Step 1: Cập nhật khối Header và Quota Card trong `chat.html`**
  Bổ sung khối hiển thị Quota Card tại cột bên trái hoặc ngay trên khung chat:
  ```html
  <!-- FREE TIER QUOTA BAR -->
  <div th:if="${currentUser != null and currentUser.subscriptionTier == 'FREE'}" id="quotaContainer" class="p-3 bg-base-100/60 rounded-2xl border border-base-content/10 shadow-sm mb-4 space-y-2">
      <div class="flex items-center justify-between text-xs font-bold">
          <span class="flex items-center gap-1.5 text-base-content/70">
              <i class="fa-solid fa-bolt-lightning text-warning"></i>
              Hạn ngạch RAG hôm nay:
          </span>
          <span id="quotaText" class="text-primary font-extrabold" th:text="${remainingQuota} + '/50 câu còn lại'">50/50 câu còn lại</span>
      </div>
      <div class="w-full bg-base-200 rounded-full h-2 overflow-hidden">
          <div id="quotaProgressBar" class="h-full bg-gradient-to-r from-primary to-accent transition-all duration-300"
               th:style="'width: ' + (${remainingQuota} * 2) + '%'"></div>
      </div>
  </div>
  ```

- [ ] **Step 2: Cập nhật JavaScript xử lý realtime trong `chat.html`**
  Sau khi nhận kết quả `data = await response.json()`:
  ```javascript
  if (data.remainingQuota !== undefined && data.remainingQuota !== null) {
      updateQuotaUI(data.remainingQuota);
  }
  ```
  Viết hàm `updateQuotaUI(remaining)` cập nhật text `#quotaText`, thanh `#quotaProgressBar`, và hiển thị cảnh báo nếu `remaining <= 0`.

- [ ] **Step 3: Kiểm tra hiển thị template trên trình duyệt hoặc qua file view**

---

### Task 6: Kiểm Thử Toàn Diện (End-to-End Test Suite), Verification & Commit

**Files:**
- Run full test suite: `mvn clean test`

- [ ] **Step 1: Chạy toàn bộ test suite Maven**
  ```powershell
  mvn clean test
  ```
  Xác minh `BUILD SUCCESS` không có bất kỳ lỗi biên dịch hay test failure nào.

- [ ] **Step 2: Kiểm tra trạng thái Git**
  ```powershell
  git status
  ```
  Xác nhận chỉ các file thuộc phạm vi task của Bình và các file test liên quan được thay đổi.

- [ ] **Step 3: Tạo commit theo chuẩn Conventional Commits tiếng Việt**
  ```powershell
  git add .
  git commit -m "feat(rag): hoàn thiện centralized system key, multi-key rotator và quota 50 câu/ngày"
  ```

---

## Verification Checklist (Definition of Done)
- [ ] Nhánh Git đang làm việc là `feature/rag-engine`.
- [ ] `User.java` có đủ `subscriptionTier`, `dailyRagCount`, `lastRagResetDate`.
- [ ] `LlmKeyRotator` xoay vòng Round-Robin thành công giữa nhiều key, tự động Cooldown khi gặp 429 và thử key tiếp theo.
- [ ] Hỗ trợ gọi LiteLLM / OpenAI-compatible endpoint khi có `studyhub.ai.base-url`.
- [ ] Gói FREE bị chặn khi gửi câu hỏi thứ 51 trong ngày; tự động reset về 0 khi sang ngày mới.
- [ ] Giao diện `chat.html` hiển thị thanh hạn ngạch và cập nhật realtime không cần F5.
- [ ] Lệnh `mvn clean test` đạt `BUILD SUCCESS`.

