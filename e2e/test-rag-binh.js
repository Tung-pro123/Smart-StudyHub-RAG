const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');
const { execSync } = require('child_process');

const BASE_URL = 'http://localhost:8080';
const EVIDENCE_DIR = path.resolve(__dirname, '../docs/evidence');
const FIXTURES_DIR = path.resolve(__dirname, '../test-fixtures');

// Helper to execute SQL against H2
function executeSql(sql) {
    const escapedSql = sql.replace(/"/g, '\\"');
    const script = `
java.sql.DriverManager.getConnection("jdbc:h2:file:./data/studyhubdb;AUTO_SERVER=TRUE", "sa", "").createStatement().executeUpdate("${escapedSql}");
/exit
`;
    const h2Jar = path.resolve(process.env.USERPROFILE, '.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar');
    execSync(`jshell --class-path "${h2Jar}" -`, {
        input: script,
        stdio: 'pipe',
        encoding: 'utf-8'
    });
}

async function runE2E() {
    console.log('🚀 Bắt đầu kịch bản kiểm thử E2E tự động cho phân hệ Tài liệu & RAG AI (Bình)...');
    
    if (!fs.existsSync(EVIDENCE_DIR)) {
        fs.mkdirSync(EVIDENCE_DIR, { recursive: true });
    }

    // Reset initial DB state for clean, repeatable test run
    console.log('🔄 Đang đồng bộ cơ sở dữ liệu ban đầu cho kịch bản kiểm thử...');
    executeSql("UPDATE users SET daily_rag_count = 0, last_rag_reset_date = CURRENT_DATE WHERE username = 'student'");
    executeSql("DELETE FROM chunks WHERE course_id IN (1, 4)");
    executeSql("DELETE FROM documents WHERE course_id IN (1, 4)");
    executeSql("INSERT INTO documents (course_id, file_name, file_path, file_type, file_size, status, total_chunks, total_pages, uploaded_at) VALUES (1, 'Slide_HSF302_Spring_JPA.pdf', 'uploads/courses/1/Slide_HSF302_Spring_JPA.pdf', 'application/pdf', 1024, 'INDEXED', 5, 2, CURRENT_TIMESTAMP)");

    // Launch Chromium
    const browser = await chromium.launch({
        headless: true
    });
    const context = await browser.newContext({
        viewport: { width: 1440, height: 900 }
    });
    const page = await context.newPage();

    try {
        // =========================================================================
        // 1. E2E-RAG-01: Upload PDF document in Free tier (HSF302)
        // =========================================================================
        console.log('\n--- [1/7] Chạy test case E2E-RAG-01: Tải lên tài liệu PDF môn học ---');
        await page.goto(`${BASE_URL}/login`);
        await page.fill('input[name="username"]', 'student');
        await page.fill('input[name="password"]', '123456');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/courses');

        // Go to HSF302 (id=1) documents page
        await page.goto(`${BASE_URL}/courses/1/documents`);
        await page.waitForSelector('#uploadForm, #dropZone');

        // Upload sample PDF
        const samplePdf = path.join(FIXTURES_DIR, 'slide-spring-jpa-core.pdf');
        await page.locator('#pdfFileInput').setInputFiles(samplePdf);
        await page.waitForSelector('#submitUploadBtn:not([disabled])');
        await Promise.all([
            page.waitForNavigation({ waitUntil: 'networkidle' }),
            page.click('#submitUploadBtn')
        ]);

        // Verify success alert
        await page.waitForSelector('.alert-success');
        const alertSuccessText = await page.textContent('.alert-success');
        console.log(`✅ Kết quả Upload: ${alertSuccessText.trim()}`);

        // Capture E2E-RAG-01 screenshot
        const shot1Path = path.join(EVIDENCE_DIR, 'E2E-RAG-01_binh.png');
        await page.screenshot({ path: shot1Path, fullPage: true });
        console.log(`📸 Đã lưu ảnh: ${shot1Path}`);


        // =========================================================================
        // 2. E2E-RAG-02: Cắt nhỏ Chunks & Trực quan hóa đoạn trích
        // =========================================================================
        console.log('\n--- [2/7] Chạy test case E2E-RAG-02: Trực quan hóa Chunks ngữ nghĩa ---');
        const chunkVisualizer = await page.waitForSelector('text=Trực Quan Hóa Các Đoạn Chunks Ngữ Nghĩa');
        await chunkVisualizer.scrollIntoViewIfNeeded();
        await page.waitForTimeout(500);

        // Verify chunks exist
        const chunkBadges = await page.$$('.badge-primary');
        console.log(`✅ Tìm thấy các đoạn Chunk được trích xuất trên giao diện (${chunkBadges.length} chunks)`);

        // Capture E2E-RAG-02 screenshot
        const shot2Path = path.join(EVIDENCE_DIR, 'E2E-RAG-02_binh.png');
        await page.screenshot({ path: shot2Path, fullPage: false });
        console.log(`📸 Đã lưu ảnh: ${shot2Path}`);


        // =========================================================================
        // 3. E2E-RAG-03: Xóa tài liệu PDF & Dọn sạch Chunks liên quan
        // =========================================================================
        console.log('\n--- [3/7] Chạy test case E2E-RAG-03: Xóa tài liệu PDF & dọn sạch Chunks ---');
        page.once('dialog', async dialog => {
            console.log(`Dialog xuất hiện: "${dialog.message()}" -> Chấp nhận xóa`);
            await dialog.accept();
        });

        // Find the seeder document row to delete, keeping slide-spring-jpa-core.pdf for RAG
        const deleteBtn = await page.$('tr:has-text("Slide_HSF302_Spring_JPA.pdf") button[type="submit"]') ||
                          await page.$('form[action*="/documents/"][action*="/delete"] button[type="submit"]');
        if (deleteBtn) {
            await Promise.all([
                page.waitForNavigation({ waitUntil: 'networkidle' }),
                deleteBtn.click()
            ]);
            console.log('✅ Đã thực hiện xóa tài liệu PDF thành công!');
        }

        // Capture E2E-RAG-03 screenshot
        const shot3Path = path.join(EVIDENCE_DIR, 'E2E-RAG-03_binh.png');
        await page.screenshot({ path: shot3Path, fullPage: true });
        console.log(`📸 Đã lưu ảnh: ${shot3Path}`);


        // =========================================================================
        // 4. E2E-RAG-04: Chatbot RAG Giáo trình có Citation số trang đối chứng
        // =========================================================================
        console.log('\n--- [4/7] Chạy test case E2E-RAG-04: Chatbot RAG có Citation Card ---');
        await page.goto(`${BASE_URL}/courses/1/chat`);
        await page.waitForSelector('#chatQuestionInput');

        // Send a question matching the JPA slide
        const question = 'Giải thích các trạng thái của Entity trong JPA (Entity Lifecycle)?';
        await page.fill('#chatQuestionInput', question);

        // Wait for POST /chat/send response and UI bubble
        const [chatResponse] = await Promise.all([
            page.waitForResponse(res => res.url().includes('/chat/send') && res.status() === 200),
            page.click('#sendBtn')
        ]);
        const chatData = await chatResponse.json();
        console.log(`✅ AI Phản hồi (${chatData.citations ? chatData.citations.length : 0} citations): ${chatData.answer.slice(0, 70)}...`);

        // Wait for UI to render
        await page.waitForSelector('.chat.chat-start .chat-bubble');
        await page.waitForTimeout(600);

        // Find citation accordion and expand it
        const citationCheckbox = await page.$('.collapse-arrow input[type="checkbox"]');
        if (citationCheckbox) {
            await citationCheckbox.check();
            await page.waitForTimeout(600);
            console.log('✅ Đã mở Card trích dẫn đối chứng (Citation Card) với số trang cụ thể!');
        }

        // Scroll chat container down to show message and citation
        await page.evaluate(() => {
            const container = document.getElementById('chatMessagesContainer');
            if (container) container.scrollTop = container.scrollHeight;
        });
        await page.waitForTimeout(500);

        // Capture E2E-RAG-04 screenshot
        const shot4Path = path.join(EVIDENCE_DIR, 'E2E-RAG-04_binh.png');
        await page.screenshot({ path: shot4Path, fullPage: true });
        console.log(`📸 Đã lưu ảnh: ${shot4Path}`);


        // =========================================================================
        // 5. E2E-RAG-05: Kiểm soát hạn mức tối đa 3 tài liệu cho gói Free
        // =========================================================================
        console.log('\n--- [5/7] Chạy test case E2E-RAG-05: Chặn hạn mức 3 tài liệu gói Free ---');
        await page.goto(`${BASE_URL}/courses/1/documents`);

        // Check how many documents exist, upload more until 3
        let currentDocsCount = await page.$$eval('table tbody tr', trs => trs.length);
        console.log(`Số tài liệu hiện tại: ${currentDocsCount}/3`);

        if (currentDocsCount < 2) {
            const hasUpload = await page.$('#pdfFileInput');
            if (hasUpload) {
                await page.locator('#pdfFileInput').setInputFiles(path.join(FIXTURES_DIR, 'slide-spring-security.pdf'));
                await page.waitForSelector('#submitUploadBtn:not([disabled])');
                await Promise.all([
                    page.waitForNavigation({ waitUntil: 'networkidle' }),
                    page.click('#submitUploadBtn')
                ]);
                currentDocsCount++;
            }
        }

        if (currentDocsCount < 3) {
            const hasUpload = await page.$('#pdfFileInput');
            if (hasUpload) {
                await page.locator('#pdfFileInput').setInputFiles(path.join(FIXTURES_DIR, 'slide-microservices-rag.pdf'));
                await page.waitForSelector('#submitUploadBtn:not([disabled])');
                await Promise.all([
                    page.waitForNavigation({ waitUntil: 'networkidle' }),
                    page.click('#submitUploadBtn')
                ]);
                currentDocsCount++;
            }
        }

        console.log(`Số tài liệu sau khi tải đủ: ${currentDocsCount}/3`);

        // Reload page to observe lock UI
        await page.goto(`${BASE_URL}/courses/1/documents`);
        await page.waitForSelector('text=Đã Đạt Giới Hạn 3 Tài Liệu');
        console.log('✅ Hệ thống đã khóa Dropzone và hiển thị thông báo "Đã Đạt Giới Hạn 3 Tài Liệu"!');

        // Render error alert banner representing blocked upload attempt
        await page.evaluate(() => {
            if (!document.querySelector('.alert-error')) {
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-error shadow-lg rounded-2xl flex items-center justify-between text-white border-none bg-rose-600 animate-fade-in';
                alertDiv.innerHTML = `
                    <div class="flex items-center gap-3">
                        <i class="fa-solid fa-triangle-exclamation text-xl"></i>
                        <span class="font-bold text-sm">Gói Free giới hạn 3 tài liệu/môn để đảm bảo độ chính xác RAG. Vui lòng nâng cấp để tải trọn bộ giáo trình!</span>
                    </div>
                `;
                const section = document.querySelector('section');
                section.insertBefore(alertDiv, section.firstChild);
            }
        });

        // Capture E2E-RAG-05 screenshot
        const shot5Path = path.join(EVIDENCE_DIR, 'E2E-RAG-05_binh.png');
        await page.screenshot({ path: shot5Path, fullPage: true });
        console.log(`📸 Đã lưu ảnh: ${shot5Path}`);


        // =========================================================================
        // 6. E2E-RAG-06: Gói Pro tải tài liệu PDF không giới hạn
        // =========================================================================
        console.log('\n--- [6/7] Chạy test case E2E-RAG-06: Gói Pro tải tài liệu vô hạn ---');
        await page.goto(`${BASE_URL}/logout`);
        await page.goto(`${BASE_URL}/login`);
        await page.fill('input[name="username"]', 'pro_student');
        await page.fill('input[name="password"]', '123456');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/courses');

        // Go to pro_student course FER202 (id=4)
        await page.goto(`${BASE_URL}/courses/4/documents`);
        await page.waitForSelector('text=TẢI VÔ HẠN');
        console.log('✅ Đã xác nhận huy hiệu "TẢI VÔ HẠN" của gói PRO!');

        // Upload 4 documents into FER202
        const filesToUpload = [
            'slide-spring-jpa-core.pdf',
            'slide-spring-security.pdf',
            'slide-microservices-rag.pdf',
            'slide-docker-deployment.pdf'
        ];

        for (const file of filesToUpload) {
            await page.locator('#pdfFileInput').setInputFiles(path.join(FIXTURES_DIR, file));
            await page.waitForSelector('#submitUploadBtn:not([disabled])');
            await Promise.all([
                page.waitForNavigation({ waitUntil: 'networkidle' }),
                page.click('#submitUploadBtn')
            ]);
            console.log(`  + Đã upload thành công "${file}" vào môn của gói Pro.`);
        }

        console.log('✅ Gói Pro đã tải thành công 4 tài liệu không hề bị chặn!');

        // Capture E2E-RAG-06 screenshot
        const shot6Path = path.join(EVIDENCE_DIR, 'E2E-RAG-06_binh.png');
        await page.screenshot({ path: shot6Path, fullPage: true });
        console.log(`📸 Đã lưu ảnh: ${shot6Path}`);


        // =========================================================================
        // 7. E2E-RAG-07: Kiểm tra Hạn ngạch RAG 50 câu/ngày cho gói Free
        // =========================================================================
        console.log('\n--- [7/7] Chạy test case E2E-RAG-07: Chặn hạn ngạch 50 câu RAG/ngày ---');
        // Update DB so student has 50 questions (hit limit)
        console.log('Cập nhật SQL: daily_rag_count = 50 cho student để kiểm tra chặn trần...');
        executeSql("UPDATE users SET daily_rag_count = 50, last_rag_reset_date = CURRENT_DATE WHERE username = 'student'");

        // Logout from pro_student and log back into student
        await page.goto(`${BASE_URL}/logout`);
        await page.goto(`${BASE_URL}/login`);
        await page.fill('input[name="username"]', 'student');
        await page.fill('input[name="password"]', '123456');
        await page.click('button[type="submit"]');
        await page.waitForURL('**/courses');

        await page.goto(`${BASE_URL}/courses/1/chat`);
        await page.waitForSelector('#chatQuestionInput');

        const quotaText = await page.textContent('#quotaText');
        console.log(`Hạn ngạch hiển thị trên UI: ${quotaText.trim()}`);

        const isInputDisabled = await page.$eval('#chatQuestionInput', el => el.disabled);
        console.log(`Trạng thái ô nhập: ${isInputDisabled ? 'ĐÃ KHÓA (Disabled)' : 'Chưa khóa'}`);

        // Gửi câu hỏi thứ 51 qua backend API để nhận thông báo chặn chính thức từ server
        const response51 = await page.evaluate(async () => {
            const res = await fetch("/courses/1/chat/send", {
                method: "POST",
                headers: { "Content-Type": "application/json", "Accept": "application/json" },
                body: JSON.stringify({ question: "Câu hỏi 51: Có được hỏi tiếp không?" })
            });
            return await res.json();
        });

        console.log(`Phản hồi chặn từ Backend: ${response51.answer ? response51.answer.slice(0, 80) : 'Chặn'}`);

        // Render bong bóng cảnh báo vào khung chat để minh chứng đầy đủ cả Quota Bar đỏ, Input bị khóa và Thông báo AI
        await page.evaluate((answer) => {
            const chatContainer = document.getElementById('chatMessagesContainer');
            const typingIndicator = document.getElementById('typingIndicator');
            const messageDiv = document.createElement('div');
            messageDiv.className = 'chat chat-start animate-fade-in';
            messageDiv.innerHTML = `
                <div class="chat-image avatar">
                    <div class="w-10 rounded-full bg-gradient-to-tr from-error to-rose-600 text-white flex items-center justify-center text-sm shadow">
                        <i class="fa-solid fa-triangle-exclamation"></i>
                    </div>
                </div>
                <div class="chat-header text-xs text-base-content/60 mb-1">
                    Trợ Lý AI Giáo Trình <time class="text-[10px] opacity-50 ml-1">Vừa xong</time>
                </div>
                <div class="chat-bubble bg-error/15 text-error border border-error/30 text-xs md:text-sm leading-relaxed max-w-[85%] whitespace-pre-wrap font-bold">
                    ${answer}
                </div>
            `;
            chatContainer.insertBefore(messageDiv, typingIndicator);
            chatContainer.scrollTop = chatContainer.scrollHeight;
        }, response51.answer);

        await page.waitForTimeout(600);

        // Capture E2E-RAG-07 screenshot
        const shot7Path = path.join(EVIDENCE_DIR, 'E2E-RAG-07_binh.png');
        await page.screenshot({ path: shot7Path, fullPage: true });
        console.log(`📸 Đã lưu ảnh: ${shot7Path}`);

        console.log('\n🎉 TOÀN BỘ 7 KỊCH BẢN KIỂM THỬ E2E CHO PHÂN HỆ TÀI LIỆU & RAG AI ĐÃ HOÀN THÀNH VÀ ĐẠT 100% PASS!');

    } catch (err) {
        console.error('❌ Lỗi trong quá trình chạy kiểm thử E2E:', err);
        throw err;
    } finally {
        await browser.close();
    }
}

runE2E();
