const { chromium } = require('playwright');

async function testClearChat() {
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    console.log('1. Logging in as student...');
    await page.goto('http://localhost:6969/login', { waitUntil: 'networkidle' });
    await page.fill('input[name="username"]', 'student');
    await page.fill('input[name="password"]', '123456');
    await Promise.all([
        page.waitForNavigation({ waitUntil: 'networkidle' }),
        page.click('button[type="submit"]')
    ]);

    console.log('2. Navigating to /courses/1/chat...');
    await page.goto('http://localhost:6969/courses/1/chat', { waitUntil: 'networkidle' });

    // Handle confirm dialog automatically
    page.on('dialog', async dialog => {
        console.log('Confirm dialog triggered:', dialog.message());
        await dialog.accept();
    });

    console.log('3. Searching for "Làm sạch đoạn chat" button...');
    const clearBtn = await page.$('button:has-text("Làm sạch đoạn chat")');
    if (!clearBtn) {
        console.log('⚠️ Không tìm thấy nút "Làm sạch đoạn chat" (có thể chưa có session nào)');
        await browser.close();
        return;
    }

    console.log('4. Clicking "Làm sạch đoạn chat"...');
    await Promise.all([
        page.waitForNavigation({ waitUntil: 'networkidle' }),
        clearBtn.click()
    ]);

    console.log('5. Current URL after click:', page.url());
    const content = await page.content();
    if (content.includes('Internal Server Error') || content.includes('Whitelabel Error Page') || content.includes('status=500')) {
        console.error('❌ BUG CONFIRMED: Máy chủ trả về lỗi 500 khi bấm nút làm sạch đoạn chat!');
        const errorText = await page.innerText('body');
        console.error('Error content:\n', errorText.substring(0, 300));
    } else {
        console.log('✅ SUCCESS: Nút làm sạch hoạt động trơn tru!');
    }

    await browser.close();
}

testClearChat().catch(console.error);

