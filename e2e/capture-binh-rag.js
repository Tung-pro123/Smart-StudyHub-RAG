const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

const EVIDENCE_DIR = path.resolve(__dirname, '..', 'docs', 'evidence', '2026-11-03-ui-redesign', 'binh');
const BASE_URL = 'http://localhost:8080';

async function waitForServer(url, timeoutMs = 60000) {
    const start = Date.now();
    console.log(`Đang chờ máy chủ tại ${url} khởi động...`);
    while (Date.now() - start < timeoutMs) {
        try {
            const res = await fetch(`${url}/login`);
            if (res.ok || res.status === 200 || res.status === 302) {
                console.log('Máy chủ đã sẵn sàng!');
                return true;
            }
        } catch (e) {
            // Wait and retry
        }
        await new Promise(r => setTimeout(r, 2000));
    }
    throw new Error('Hết thời gian chờ máy chủ khởi động.');
}

async function login(page, username, password) {
    console.log(`Đang đăng nhập với tài khoản: ${username}`);
    await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle' });
    await page.fill('input[name="username"]', username);
    await page.fill('input[name="password"]', password);
    await Promise.all([
        page.waitForNavigation({ waitUntil: 'networkidle' }),
        page.click('button[type="submit"]')
    ]);
    console.log(`Đăng nhập thành công với: ${username}`);
}

async function main() {
    if (!fs.existsSync(EVIDENCE_DIR)) {
        fs.mkdirSync(EVIDENCE_DIR, { recursive: true });
    }

    await waitForServer(BASE_URL);

    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext({
        viewport: { width: 1440, height: 900 },
        deviceScaleFactor: 1
    });
    const page = await context.newPage();

    try {
        // 1. Đăng nhập với tài khoản student (Free)
        await login(page, 'student', '123456');

        // Tìm course HSF302
        await page.goto(`${BASE_URL}/courses`, { waitUntil: 'networkidle' });
        // Tìm link đến HSF302
        const hsfLink = await page.$('a[href*="/courses/"]');
        let hsfUrl = `${BASE_URL}/courses/1`;
        if (hsfLink) {
            const href = await hsfLink.getAttribute('href');
            if (href && href.startsWith('/courses/')) {
                hsfUrl = `${BASE_URL}${href}`;
            }
        }
        const courseIdMatch = hsfUrl.match(/\/courses\/(\d+)/);
        const courseId = courseIdMatch ? courseIdMatch[1] : '1';
        console.log(`Course ID tìm thấy: ${courseId}`);

        // ==================== KỊCH BẢN DOCUMENTS ====================
        const docUrl = `${BASE_URL}/courses/${courseId}/documents`;
        await page.goto(docUrl, { waitUntil: 'networkidle' });
        await page.waitForTimeout(1000);

        // Kịch bản E2E-RAG-01: Giao diện Tài liệu & Cắt Chunks (Gói Free, số lượng tài liệu/chunks, White-Card Palette, nút Gradient)
        await page.setViewportSize({ width: 1440, height: 900 });
        await page.screenshot({
            path: path.join(EVIDENCE_DIR, 'E2E-RAG-01_binh.png'),
            fullPage: false
        });
        console.log('✓ Đã chụp E2E-RAG-01_binh.png');

        // Kịch bản E2E-RAG-02: Khối Dropzone tải PDF và Bảng Clean Table tài liệu có badge INDEXED
        const docTable = await page.$('table');
        if (docTable) {
            await docTable.scrollIntoViewIfNeeded();
            await page.waitForTimeout(500);
        }
        await page.screenshot({
            path: path.join(EVIDENCE_DIR, 'E2E-RAG-02_binh.png'),
            fullPage: false
        });
        console.log('✓ Đã chụp E2E-RAG-02_binh.png');

        // Kịch bản E2E-RAG-03: Trực quan hóa Chunks ngữ nghĩa theo Progressive Disclosure (mở Accordion)
        const firstChunkCheckbox = await page.$('.collapse-arrow input[type="checkbox"]');
        if (firstChunkCheckbox) {
            await firstChunkCheckbox.click();
            await page.waitForTimeout(500);
        }
        const chunkSection = await page.$('h2:has-text("Trực Quan Hóa")');
        if (chunkSection) {
            await chunkSection.scrollIntoViewIfNeeded();
            await page.waitForTimeout(500);
        }
        await page.screenshot({
            path: path.join(EVIDENCE_DIR, 'E2E-RAG-03_binh.png'),
            fullPage: false
        });
        console.log('✓ Đã chụp E2E-RAG-03_binh.png');

        // ==================== KỊCH BẢN CHATBOT ====================
        const chatUrl = `${BASE_URL}/courses/${courseId}/chat`;
        await page.goto(chatUrl, { waitUntil: 'networkidle' });
        await page.waitForTimeout(1000);

        // Kịch bản E2E-RAG-04: Giao diện Chatbot Giáo trình RAG (White-Card, session list, Quota card mỏng, robot header)
        await page.screenshot({
            path: path.join(EVIDENCE_DIR, 'E2E-RAG-04_binh.png'),
            fullPage: false
        });
        console.log('✓ Đã chụp E2E-RAG-04_binh.png');

        // Đặt câu hỏi thực tế để sinh viên và AI đàm thoại
        const questionInput = await page.$('#chatQuestionInput');
        const sendBtn = await page.$('#sendBtn');
        if (questionInput && sendBtn) {
            await questionInput.fill('Giải thích kiến trúc và luồng xử lý Spring Data JPA trong môn học?');
            await sendBtn.click();
            console.log('Đã gửi câu hỏi RAG...');
            // Chờ tin nhắn AI trả lời xuất hiện
            await page.waitForSelector('.chat-start:not(#typingIndicator)', { timeout: 30000 }).catch(() => {
                console.log('Tin nhắn AI đang chờ tải...');
            });
            await page.waitForTimeout(2000);
        }

        // Kịch bản E2E-RAG-05: Hội thoại sinh viên & AI (Bong bóng tím hồng cho sinh viên, thẻ trắng cho AI)
        await page.screenshot({
            path: path.join(EVIDENCE_DIR, 'E2E-RAG-05_binh.png'),
            fullPage: false
        });
        console.log('✓ Đã chụp E2E-RAG-05_binh.png');

        // Kịch bản E2E-RAG-06: Trích dẫn đối chứng RAG (Citations Accordion mở ra với số trang slide)
        const citationCheckbox = await page.$('.chat-bubble .collapse-arrow input[type="checkbox"]');
        if (citationCheckbox) {
            await citationCheckbox.click();
            await page.waitForTimeout(500);
        }
        await page.screenshot({
            path: path.join(EVIDENCE_DIR, 'E2E-RAG-06_binh.png'),
            fullPage: false
        });
        console.log('✓ Đã chụp E2E-RAG-06_binh.png');

        // ==================== KỊCH BẢN PRO TIER SO SÁNH ====================
        // Đăng xuất và đăng nhập pro_student
        await page.goto(`${BASE_URL}/logout`, { waitUntil: 'networkidle' });
        await login(page, 'pro_student', '123456');

        await page.goto(`${BASE_URL}/courses`, { waitUntil: 'networkidle' });
        const proCourseLink = await page.$('a[href*="/courses/"]');
        let proCourseId = '3';
        if (proCourseLink) {
            const href = await proCourseLink.getAttribute('href');
            const match = href.match(/\/courses\/(\d+)/);
            if (match) proCourseId = match[1];
        }

        // Vào chat của pro_student để chụp Badge Pro Vô Hạn
        await page.goto(`${BASE_URL}/courses/${proCourseId}/chat`, { waitUntil: 'networkidle' });
        await page.waitForTimeout(1000);

        // Kịch bản E2E-RAG-07: Hạn ngạch tài liệu / chat với tài khoản Pro vs Free
        await page.screenshot({
            path: path.join(EVIDENCE_DIR, 'E2E-RAG-07_binh.png'),
            fullPage: false
        });
        console.log('✓ Đã chụp E2E-RAG-07_binh.png');

        // ==================== RESPONSIVE SUITE (8 ẢNH) ====================
        const viewports = [
            { width: 1920, height: 1080, name: '1920' },
            { width: 1366, height: 768, name: '1366' },
            { width: 768, height: 1024, name: '768' },
            { width: 375, height: 812, name: '375' }
        ];

        // 1. Responsive cho Documents
        for (const vp of viewports) {
            await page.setViewportSize({ width: vp.width, height: vp.height });
            await page.goto(`${BASE_URL}/courses/${courseId}/documents`, { waitUntil: 'networkidle' });
            await page.waitForTimeout(500);
            await page.screenshot({
                path: path.join(EVIDENCE_DIR, `documents-${vp.name}_binh.png`),
                fullPage: false
            });
            console.log(`✓ Đã chụp documents-${vp.name}_binh.png`);
        }

        // 2. Responsive cho Chat
        for (const vp of viewports) {
            await page.setViewportSize({ width: vp.width, height: vp.height });
            await page.goto(`${BASE_URL}/courses/${courseId}/chat`, { waitUntil: 'networkidle' });
            await page.waitForTimeout(500);
            await page.screenshot({
                path: path.join(EVIDENCE_DIR, `chat-${vp.name}_binh.png`),
                fullPage: false
            });
            console.log(`✓ Đã chụp chat-${vp.name}_binh.png`);
        }

        console.log('=== TOÀN BỘ 15 ẢNH MINH CHỨNG ĐÃ ĐƯỢC CHỤP THÀNH CÔNG! ===');

    } catch (err) {
        console.error('Lỗi trong quá trình chụp ảnh:', err);
        throw err;
    } finally {
        await browser.close();
    }
}

main().catch(err => {
    console.error(err);
    process.exit(1);
});

