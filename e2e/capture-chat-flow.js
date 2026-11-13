const { chromium } = require('playwright');
const path = require('path');

(async () => {
    const browser = await chromium.launch({ headless: true });
    const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
    
    // Login
    await page.goto('http://localhost:6969/login', { waitUntil: 'networkidle' });
    await page.fill('input[name="username"]', 'student');
    await page.fill('input[name="password"]', '123456');
    await Promise.all([
        page.waitForNavigation({ waitUntil: 'networkidle' }),
        page.click('button[type="submit"]')
    ]);
    
    // Go to chat
    await page.goto('http://localhost:6969/courses/1/chat', { waitUntil: 'networkidle' });
    
    // Find the session with the question
    const sessionLinks = await page.$$('#sessionsList a');
    if (sessionLinks.length > 1) {
        await sessionLinks[1].click();
        await page.waitForTimeout(2000);
    }
    
    // Scroll container to top to see user message
    await page.evaluate(() => {
        const container = document.getElementById('chatMessagesContainer');
        if (container) container.scrollTop = 0;
    });
    await page.waitForTimeout(500);
    
    const outPath = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', '04-chat-conversation-flow.png');
    await page.screenshot({ path: outPath });
    console.log('Saved to:', outPath);
    await browser.close();
})();

