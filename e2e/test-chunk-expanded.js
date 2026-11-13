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
    
    await page.goto('http://localhost:6969/courses/1/documents', { waitUntil: 'networkidle' });
    
    // Click on the first chunk checkbox
    const firstCheckbox = await page.$('.collapse-arrow input[type="checkbox"]');
    if (firstCheckbox) {
        await firstCheckbox.click();
        await page.waitForTimeout(500);
    }
    
    const cropPath = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', '03-chunk-expanded.png');
    const chunkSection = await page.$('.lg\\:grid-cols-3');
    if (chunkSection) {
        await chunkSection.screenshot({ path: cropPath });
        console.log('Saved expanded chunk screenshot:', cropPath);
    }
    await browser.close();
})();

