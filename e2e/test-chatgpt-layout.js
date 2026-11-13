const { chromium } = require('playwright');
const path = require('path');

(async () => {
    const browser = await chromium.launch({ headless: true });
    
    // 1. Desktop Test (1440x900)
    const desktopContext = await browser.newContext({ viewport: { width: 1440, height: 900 } });
    const desktopPage = await desktopContext.newPage();
    
    await desktopPage.goto('http://localhost:6969/login', { waitUntil: 'networkidle' });
    await desktopPage.fill('input[name="username"]', 'student');
    await desktopPage.fill('input[name="password"]', '123456');
    await Promise.all([
        desktopPage.waitForNavigation({ waitUntil: 'networkidle' }),
        desktopPage.click('button[type="submit"]')
    ]);
    
    // Go to new chat
    await desktopPage.goto('http://localhost:6969/courses/1/chat/new', { waitUntil: 'networkidle' });
    await desktopPage.waitForTimeout(1000);
    
    // Verify zero outer scroll
    const scrollY = await desktopPage.evaluate(() => window.scrollY);
    const bodyOverflow = await desktopPage.evaluate(() => window.getComputedStyle(document.body).overflow);
    console.log('Desktop scrollY:', scrollY, 'bodyOverflow:', bodyOverflow);
    
    const newChatPath = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', '05-chatgpt-new-desktop.png');
    await desktopPage.screenshot({ path: newChatPath });
    console.log('Saved new chat screenshot:', newChatPath);
    
    // Click on an existing session
    const sessionLinks = await desktopPage.$$('#sessionsList a');
    console.log('Session links count:', sessionLinks.length);
    if (sessionLinks.length > 1) {
        await sessionLinks[1].click();
        await desktopPage.waitForTimeout(2000);
    }
    
    const sessionChatPath = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', '05-chatgpt-session-desktop.png');
    await desktopPage.screenshot({ path: sessionChatPath });
    console.log('Saved session chat screenshot:', sessionChatPath);

    // 2. Mobile Test (390x844)
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
    await mobilePage.waitForTimeout(1000);
    
    const mobileChatPath = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', '05-chatgpt-mobile.png');
    await mobilePage.screenshot({ path: mobileChatPath });
    console.log('Saved mobile chat screenshot:', mobileChatPath);

    // Mobile sidebar open
    const toggleBtn = await mobilePage.$('#btnToggleMobileSidebar');
    if (toggleBtn) {
        await toggleBtn.click();
        await mobilePage.waitForTimeout(500);
        const mobileSidebarPath = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', '05-chatgpt-mobile-sidebar.png');
        await mobilePage.screenshot({ path: mobileSidebarPath });
        console.log('Saved mobile sidebar screenshot:', mobileSidebarPath);
    }

    await browser.close();
    console.log('All tests completed successfully!');
})();

