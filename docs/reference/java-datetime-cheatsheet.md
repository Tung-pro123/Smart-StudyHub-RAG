# 📅 JAVA DATE & TIME CHEATSHEET (JAVA 8+ & ENTERPRISE SPRING BOOT)
> Tài liệu tra cứu nhanh toàn diện về xử lý Ngày - Giờ (Date & Time) trong Java 8+, Spring Boot, JPA, Thymeleaf và các bài thi thực hành (PE / HSF302).

---

## 📌 BẢNG TỔNG QUAN CÁC KIỂU THỜI GIAN TRONG JAVA

| Kiểu dữ liệu | Package | Ý nghĩa | Định dạng mẫu | Ứng dụng thực tế |
| :--- | :--- | :--- | :--- | :--- |
| **`LocalDate`** | `java.time` | Chỉ Ngày (năm - tháng - ngày) | `2026-11-04` | Ngày sinh, ngày hết hạn gói cước, ngày đăng ký |
| **`LocalTime`** | `java.time` | Chỉ Giờ (giờ : phút : giây : nano) | `15:45:00` | Giờ mở cửa, thời gian thi |
| **`LocalDateTime`** | `java.time` | Ngày + Giờ (không múi giờ) | `2026-11-04T15:45:00` | `createdAt`, `updatedAt`, `completedAt` |
| **`ZonedDateTime`** | `java.time` | Ngày + Giờ + Múi giờ cụ thể | `...T15:45:00+07:00[Asia/Ho_Chi_Minh]` | Hệ thống quốc tế, máy chủ lệch múi giờ |
| **`Instant`** | `java.time` | Điểm thời gian chuẩn UTC (Epoch) | `2026-11-04T08:45:00Z` | Timestamp JWT, log hệ thống, audit |
| **`Period`** | `java.time` | Khoảng cách theo Ngày/Tháng/Năm | `P1Y2M15D` | Tính tuổi, số năm kinh nghiệm |
| **`Duration`** | `java.time` | Khoảng cách theo Giờ/Phút/Giây | `PT2H30M` | Thời gian làm bài thi, thời lượng video |
| **`java.util.Date`** | `java.util` | Kiểu cũ (Legacy) | `Wed Nov 04 15:45:00 ICT 2026` | Code cũ, thư viện bên thứ 3 cũ |
| **`java.sql.Date`** | `java.sql` | Kiểu JDBC chuẩn cho SQL DATE | `2026-11-04` | JDBC thuần (PreparedStatement) |

---

## 1. KHỞI TẠO & LẤY THỜI GIAN HIỆN TẠI

```java
import java.time.*;

// 1. Thời gian hiện tại theo hệ thống
LocalDate today = LocalDate.now();                     // 2026-11-04
LocalTime nowTime = LocalTime.now();                   // 15:45:30.123
LocalDateTime now = LocalDateTime.now();               // 2026-11-04T15:45:30.123
Instant nowUtc = Instant.now();                        // 2026-11-04T08:45:30.123Z (UTC)

// 2. Chỉ định ngày giờ cụ thể
LocalDate birthday = LocalDate.of(2003, Month.NOVEMBER, 20); // hoặc (2003, 11, 20)
LocalTime quizTime = LocalTime.of(14, 30, 0);                 // 14:30:00
LocalDateTime deadline = LocalDateTime.of(2026, 12, 31, 23, 59, 59);

// 3. Lấy từng thành phần con
int year = today.getYear();             // 2026
int month = today.getMonthValue();       // 11 (1-12)
Month monthEnum = today.getMonth();      // Month.NOVEMBER
int dayOfMonth = today.getDayOfMonth();  // 4
DayOfWeek dayOfWeek = today.getDayOfWeek(); // DayOfWeek.WEDNESDAY
int dayOfWeekValue = dayOfWeek.getValue();  // 3 (Thứ 2=1 ... Chủ Nhật=7)
```

---

## 2. CỘNG / TRỪ THỜI GIAN (PLUS & MINUS) - BÀI TOÁN GIA HẠN GÓI CƯỚC & DEADLINE

> [!TIP]
> Các đối tượng `java.time` là **Immutable** (bất biến, thread-safe). Mọi phép toán `.plus()` hay `.minus()` đều trả về một đối tượng mới!

```java
LocalDateTime now = LocalDateTime.now();

// 1. Gia hạn gói cước / tính thời hạn hết hạn
LocalDateTime tierExpiresAt = now.plusDays(30);       // Thêm 30 ngày
LocalDateTime proSemester = now.plusMonths(4);        // Thêm 4 tháng (1 học kỳ)
LocalDateTime nextYear = now.plusYears(1);            // Thêm 1 năm
LocalDateTime plusMinutes = now.plusMinutes(15);      // Thêm 15 phút (thời gian nộp bài thi)

// 2. Trừ thời gian (lọc dữ liệu trong quá khứ)
LocalDateTime sevenDaysAgo = now.minusDays(7);        // 7 ngày trước
LocalDateTime oneMonthAgo = now.minusMonths(1);       // 1 tháng trước

// 3. Dùng ChronoUnit linh hoạt
import java.time.temporal.ChronoUnit;
LocalDateTime plusDynamic = now.plus(2, ChronoUnit.WEEKS); // Thêm 2 tuần
```

---

## 3. SO SÁNH THỜI GIAN (COMPARE, BEFORE, AFTER) - KIỂM TRA HẾT HẠN

```java
LocalDate today = LocalDate.now();
LocalDate expireDate = LocalDate.of(2026, 11, 30);

// 1. Trước / Sau / Bằng nhau
boolean isExpired = today.isAfter(expireDate);      // false (chưa hết hạn)
boolean isValid = today.isBefore(expireDate);       // true
boolean isToday = today.isEqual(LocalDate.now());   // true

// 2. Kiểm tra nằm trong khoảng [startDate, endDate] (Inclusive hoặc Exclusive)
LocalDate start = LocalDate.of(2026, 11, 1);
LocalDate end = LocalDate.of(2026, 11, 10);

// Nằm trong khoảng (start <= date <= end):
boolean inRange = (!today.isBefore(start)) && (!today.isAfter(end));

// 3. Dùng compareTo (trả về <0, 0, >0)
int cmp = today.compareTo(expireDate); // < 0 nếu today trước expireDate
```

---

## 4. TÍNH KHOẢNG CÁCH / ĐỘ CHÊNH LỆCH (BETWEEN, DURATION, PERIOD)

### A. Giữa 2 Ngày (`LocalDate`) $\rightarrow$ Dùng `ChronoUnit` hoặc `Period`
```java
LocalDate date1 = LocalDate.of(2003, 5, 15);
LocalDate date2 = LocalDate.of(2026, 11, 4);

// CÁCH 1: ChronoUnit (Hay dùng nhất cho logic tính ngày quá hạn, số ngày còn lại)
long daysBetween = ChronoUnit.DAYS.between(date1, date2);    // Tổng số ngày chênh lệch
long monthsBetween = ChronoUnit.MONTHS.between(date1, date2); // Tổng số tháng chênh lệch
long yearsBetween = ChronoUnit.YEARS.between(date1, date2);   // Số năm (tính tuổi nhanh)

// CÁCH 2: Period (Tách biệt Năm - Tháng - Ngày)
Period period = Period.between(date1, date2);
System.out.printf("Chênh lệch: %d năm, %d tháng, %d ngày\n",
        period.getYears(), period.getMonths(), period.getDays());
```

### B. Giữa 2 Mốc Giờ (`LocalDateTime` hoặc `LocalTime`) $\rightarrow$ Dùng `Duration`
```java
LocalDateTime startTime = LocalDateTime.of(2026, 11, 4, 14, 0, 0);
LocalDateTime submitTime = LocalDateTime.of(2026, 11, 4, 14, 25, 45);

Duration duration = Duration.between(startTime, submitTime);
long seconds = duration.getSeconds();        // 1545 giây
long minutes = duration.toMinutes();         // 25 phút
long millis = duration.toMillis();           // milliseconds
```

---

## 5. ĐẦU NGÀY / CUỐI NGÀY (TRUY VẤN DỮ LIỆU TRONG NGÀY SQL/JPA)

> [!IMPORTANT]
> Khi người dùng chọn lọc theo ngày `2026-11-04`, để query DB chính xác các bản ghi có cột `DATETIME/TIMESTAMP`, ta phải query từ `2026-11-04 00:00:00` đến `2026-11-04 23:59:59.999999999`.

```java
LocalDate selectedDate = LocalDate.now();

// Đầu ngày: 2026-11-04T00:00:00
LocalDateTime startOfDay = selectedDate.atStartOfDay();

// Cuối ngày: 2026-11-04T23:59:59.999999999
LocalDateTime endOfDay = selectedDate.atTime(LocalTime.MAX);
// hoặc an toàn không sợ lỗi nano trong một số DB cũ:
LocalDateTime endOfDaySafe = selectedDate.atTime(23, 59, 59);

// Trong Spring Data JPA Repository:
// List<QuizAttempt> findByCompletedAtBetween(LocalDateTime start, LocalDateTime end);
```

---

## 6. ĐỊNH DẠNG & ÉP KIỂU CHUỖI (FORMATTING & PARSING STRING $\leftrightarrow$ DATE)

```java
import java.time.format.DateTimeFormatter;

// 1. Date/DateTime -> Chuỗi String hiển thị UI
LocalDateTime now = LocalDateTime.now();

DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
String str1 = now.format(dtf1); // "04/11/2026 15:45:30"

DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
String str2 = now.format(dtf2); // "04/11/2026"

DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("HH:mm a");
String str3 = now.format(dtf3); // "03:45 PM"

// 2. Chuỗi String nhập từ Form HTML -> LocalDate / LocalDateTime
String inputDateStr = "04/11/2026";
DateTimeFormatter parser = DateTimeFormatter.ofPattern("dd/MM/yyyy");
LocalDate parsedDate = LocalDate.parse(inputDateStr, parser);

// Chuỗi chuẩn ISO (ví dụ từ thẻ <input type="date"> gửi lên: "2026-11-04")
LocalDate isoDate = LocalDate.parse("2026-11-04"); // Không cần formatter vì là chuẩn ISO!

// Chuỗi từ thẻ <input type="datetime-local"> gửi lên: "2026-11-04T15:30"
LocalDateTime isoDateTime = LocalDateTime.parse("2026-11-04T15:30");
```

---

## 7. MA TRẬN CHUYỂN ĐỔI QUA LẠI (CONVERSION CHEAT MATRIX)

### A. Chuyển đổi giữa `LocalDate` và `LocalDateTime`
```java
LocalDate date = LocalDate.now();
LocalDateTime dateTime = date.atTime(15, 30); // hoặc date.atStartOfDay()

LocalDateTime ldt = LocalDateTime.now();
LocalDate dateOnly = ldt.toLocalDate();
LocalTime timeOnly = ldt.toLocalTime();
```

### B. Chuyển đổi giữa `java.util.Date` (Legacy) $\leftrightarrow$ `LocalDateTime` / `LocalDate`
```java
import java.util.Date;
import java.time.ZoneId;

// 1. java.util.Date -> LocalDateTime
Date oldDate = new Date();
LocalDateTime ldt = oldDate.toInstant()
                           .atZone(ZoneId.systemDefault())
                           .toLocalDateTime();

// 2. java.util.Date -> LocalDate
LocalDate ld = oldDate.toInstant()
                      .atZone(ZoneId.systemDefault())
                      .toLocalDate();

// 3. LocalDateTime -> java.util.Date
LocalDateTime myLdt = LocalDateTime.now();
Date newDate = Date.from(myLdt.atZone(ZoneId.systemDefault()).toInstant());

// 4. LocalDate -> java.util.Date
LocalDate myLd = LocalDate.now();
Date newDateFromLd = Date.from(myLd.atStartOfDay(ZoneId.systemDefault()).toInstant());
```

### C. Chuyển đổi giữa `java.sql.Date` / `java.sql.Timestamp` $\leftrightarrow$ `LocalDate` / `LocalDateTime`
```java
// java.sql.Date <-> LocalDate
java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.now());
LocalDate backToLocal = sqlDate.toLocalDate();

// java.sql.Timestamp <-> LocalDateTime
java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(LocalDateTime.now());
LocalDateTime backToLdt = timestamp.toLocalDateTime();
```

---

## 8. THAO TÁC NÂNG CAO VỚI `TemporalAdjusters` (ĐẦU THÁNG, CUỐI THÁNG, THỨ 2 KẾ TIẾP)

```java
import java.time.temporal.TemporalAdjusters;

LocalDate today = LocalDate.now();

// 1. Ngày đầu tiên của tháng hiện tại
LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth()); // 2026-11-01

// 2. Ngày cuối cùng của tháng hiện tại (rất tiện để tính ngày kết thúc kỳ thanh toán)
LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());   // 2026-11-30

// 3. Ngày đầu năm / cuối năm
LocalDate firstDayOfYear = today.with(TemporalAdjusters.firstDayOfYear());   // 2026-01-01
LocalDate lastDayOfYear = today.with(TemporalAdjusters.lastDayOfYear());     // 2026-12-31

// 4. Tìm ngày Thứ 2 tiếp theo (Ví dụ: xếp lịch học của Study Planner)
LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));

// 5. Kiểm tra năm nhuận
boolean isLeap = today.isLeapYear(); // 2026 -> false (2024 -> true)
int daysInMonth = today.lengthOfMonth(); // 30 ngày
```

---

## 9. ỨNG DỤNG TRONG SPRING BOOT, JPA & THYMELEAF

### A. Nhận dữ liệu từ Controller Form Request
```java
@Controller
public class BookingController {

    // Nhận từ thẻ <input type="date" name="deadline">
    @PostMapping("/create-task")
    public String createTask(
            @RequestParam("deadline") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline) {
        // deadline tự động parse chuẩn yyyy-MM-dd
        return "redirect:/tasks";
    }

    // Nhận từ thẻ <input type="text" name="dob" placeholder="dd/MM/yyyy">
    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam("dob") @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dob) {
        return "redirect:/profile";
    }
}
```

### B. Trong JPA Entity
```java
@Entity
public class Course {
    // Không cần @Temporal khi dùng java.time!
    // Hibernate 6 tự động map LocalDate -> DATE, LocalDateTime -> TIMESTAMP
    private LocalDate startDate;

    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

### C. Trong Thymeleaf HTML Template (Dùng `#temporals`)
```html
<!-- Định dạng ngày giờ: dd/MM/yyyy HH:mm -->
<span th:text="${#temporals.format(quiz.createdAt, 'dd/MM/yyyy HH:mm')}">04/11/2026 15:30</span>

<!-- Chỉ lấy ngày: dd/MM/yyyy -->
<span th:text="${#temporals.format(course.startDate, 'dd/MM/yyyy')}">04/11/2026</span>

<!-- Lấy giờ: HH:mm -->
<span th:text="${#temporals.format(quizAttempt.completedAt, 'HH:mm')}">15:45</span>

<!-- Lấy thành phần: ngày, tháng, năm -->
<span th:text="${#temporals.day(quiz.createdAt)}">4</span>
<span th:text="${#temporals.month(quiz.createdAt)}">11</span>
<span th:text="${#temporals.year(quiz.createdAt)}">2026</span>
```

---

## 10. MẪU BÀI TEST NGHIỆP VỤ THƯỜNG GẶP TRONG BÀI THI PE

```java
// BÀI TOÁN 1: Kiểm tra sinh viên có được thi không (phải trước giờ deadline)
public boolean canTakeQuiz(LocalDateTime deadline) {
    if (deadline == null) return true;
    return LocalDateTime.now().isBefore(deadline);
}

// BÀI TOÁN 2: Tính số tiền phạt trễ hạn trả sách / tài liệu (5.000đ/ngày)
public long calculateLateFee(LocalDate dueDate, LocalDate returnDate) {
    if (returnDate == null || !returnDate.isAfter(dueDate)) {
        return 0; // Trả đúng hạn
    }
    long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
    return daysLate * 5000L;
}

// BÀI TOÁN 3: Kiểm tra sinh viên có đủ 18 tuổi hay không
public boolean isAtLeast18YearsOld(LocalDate dateOfBirth) {
    if (dateOfBirth == null) return false;
    return ChronoUnit.YEARS.between(dateOfBirth, LocalDate.now()) >= 18;
}
```
