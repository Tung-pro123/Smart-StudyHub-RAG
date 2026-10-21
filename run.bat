@echo off
chcp 65001 >nul
echo ===================================================
echo   🎓 SMART STUDYHUB RAG - KHỞI CHẠY HỆ THỐNG
echo ===================================================

REM Tự động phát hiện JDK 17 / JDK 21 nếu JAVA_HOME chưa chuẩn
if exist "C:\Program Files\Java\jdk-17.0.12" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-17.0.12"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
) else if exist "C:\Program Files\Java\jdk-21.0.12" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

echo [INFO] Đang sử dụng Java:
java -version

echo.
echo [INFO] Đang nạp biến môi trường từ .env (nếu có)...
if exist ".env" (
    for /F "usebackq tokens=1,* delims==" %%A in (".env") do (
        set "line=%%A"
        REM Bỏ qua comment bắt đầu bằng #
        if not "!line:~0,1!"=="#" (
            if not "%%B"=="" (
                set "%%A=%%B"
            )
        )
    )
    echo [INFO] Đã nạp xong cấu hình từ .env!
) else (
    echo [WARN] Chưa có file .env. Bạn có thể copy từ .env.example để cấu hình Google OAuth2.
)

echo.
echo [INFO] Đang khởi chạy Spring Boot...
mvn spring-boot:run
pause
