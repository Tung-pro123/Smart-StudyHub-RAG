const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

(async () => {
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
    const page = await context.newPage();
    
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
    await page.waitForSelector('#sessionsList');
    
    // Click on the second session in the list
    const sessionLinks = await page.$$('#sessionsList a');
    console.log('Found sessions:', sessionLinks.length);
    if (sessionLinks.length > 1) {
        await sessionLinks[1].click();
        await page.waitForTimeout(2000);
    }
    
    const outPath = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', '04-course-chat-with-messages-desktop.png');
    await page.screenshot({ path: outPath, fullPage: false });
    console.log('Captured chat with messages to:', outPath);

    // Also mobile view
    const mobileContext = await browser.newContext({
        viewport: { width: 390, height: 844 },
        deviceScaleFactor: 2,
        isMobile: true
    });
    const mobilePage = await mobileContext.newPage();
    await mobilePage.goto('http://localhost:6969/login', { waitUntil: 'networkidle' });
    await mobilePage.fill('input[name="username"]', 'student');
    await mobilePage.fill('input[name="password"]', '123456');
    await Promise.all([
        mobilePage.waitForNavigation({ waitUntil: 'networkidle' }),
        mobilePage.click('button[type="submit"]')
    ]);
    await mobilePage.goto('http://localhost:6969/courses/1/chat', { waitUntil: 'networkidle' });
    const mobileSessions = await mobilePage.$$('#sessionsListMobile a');
    if (mobileSessions.length > 1) {
        // First click toggle mobile sidebar
        await mobilePage.click('#btnToggleMobileSessions');
        await mobilePage.waitForTimeout(500);
        await mobileSessions[1].click();
        await mobilePage.waitForTimeout(2000);
    }
    const mobileOutPath = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', '04-course-chat-with-messages-mobile.png');
    await mobilePage.screenshot({ path: mobileOutPath, fullPage: false });
    console.log('Captured mobile chat with messages to:', mobileOutPath);

    await browser.close();
})();

