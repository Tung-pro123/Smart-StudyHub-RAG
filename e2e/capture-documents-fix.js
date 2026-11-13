const { chromium } = require('playwright');
const path = require('path');

(async () => {
    const browser = await chromium.launch({ headless: true });
    
    // Desktop View
    const desktopContext = await browser.newContext({ viewport: { width: 1440, height: 900 } });
    const desktopPage = await desktopContext.newPage();
    
    await desktopPage.goto('http://localhost:6969/login', { waitUntil: 'networkidle' });
    await desktopPage.fill('input[name="username"]', 'student');
    await desktopPage.fill('input[name="password"]', '123456');
    await Promise.all([
        desktopPage.waitForNavigation({ waitUntil: 'networkidle' }),
        desktopPage.click('button[type="submit"]')
    ]);
    
    await desktopPage.goto('http://localhost:6969/courses/1/documents', { waitUntil: 'networkidle' });
    await desktopPage.waitForTimeout(1000);
    
    const desktopPath = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', '03-course-documents-fixed-desktop.png');
    await desktopPage.screenshot({ path: desktopPath, fullPage: true });
    console.log('Saved desktop screenshot:', desktopPath);
    
    // Zoom in on the Upload Button and Chunks Accordion
    const chunkSection = await desktopPage.$('.lg\\:grid-cols-3');
    if (chunkSection) {
        const cropPath = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', '03-course-documents-zoom.png');
        await chunkSection.screenshot({ path: cropPath });
        console.log('Saved zoom screenshot:', cropPath);
    }
    
    await browser.close();
})();

