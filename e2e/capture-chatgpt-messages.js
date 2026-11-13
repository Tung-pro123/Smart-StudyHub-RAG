const { chromium } = require('playwright');
const path = require('path');

(async () => {
    const browser = await chromium.launch({ headless: true });
    const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
    
    await page.goto('http://localhost:6969/login', { waitUntil: 'networkidle' });
    await page.fill('input[name="username"]', 'student');
    await page.fill('input[name="password"]', '123456');
    await Promise.all([
        page.waitForNavigation({ waitUntil: 'networkidle' }),
        page.click('button[type="submit"]')
    ]);
    
    await page.goto('http://localhost:6969/courses/1/chat', { waitUntil: 'networkidle' });
    
    // Find session that has "Môn học này gồm"
    const sessionLinks = await page.$$('#sessionsList a');
    for (const link of sessionLinks) {
        const text = await link.innerText();
        if (text.includes('Môn học này gồm')) {
            await link.click();
            await page.waitForTimeout(2000);
            break;
        }
    }
    
    const outPath = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', '05-chatgpt-with-messages.png');
    await page.screenshot({ path: outPath });
    console.log('Saved to:', outPath);
    await browser.close();
})();

