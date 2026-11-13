const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

const OUTPUT_DIR = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-before');
const BASE_URL = 'http://localhost:6969';

async function main() {
    if (!fs.existsSync(OUTPUT_DIR)) {
        fs.mkdirSync(OUTPUT_DIR, { recursive: true });
    }

    const browser = await chromium.launch({ headless: true });
    
    // Desktop context
    const desktopContext = await browser.newContext({
        viewport: { width: 1440, height: 900 },
        deviceScaleFactor: 1
    });
    const desktopPage = await desktopContext.newPage();

    // Mobile context
    const mobileContext = await browser.newContext({
        viewport: { width: 390, height: 844 },
        deviceScaleFactor: 2,
        isMobile: true
    });
    const mobilePage = await mobileContext.newPage();

    console.log('1. Logging in as student on desktop...');
    await desktopPage.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle' });
    await desktopPage.fill('input[name="username"]', 'student');
    await desktopPage.fill('input[name="password"]', '123456');
    await Promise.all([
        desktopPage.waitForNavigation({ waitUntil: 'networkidle' }),
        desktopPage.click('button[type="submit"]')
    ]);

    console.log('2. Logging in as student on mobile...');
    await mobilePage.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle' });
    await mobilePage.fill('input[name="username"]', 'student');
    await mobilePage.fill('input[name="password"]', '123456');
    await Promise.all([
        mobilePage.waitForNavigation({ waitUntil: 'networkidle' }),
        mobilePage.click('button[type="submit"]')
    ]);

    const pagesToCapture = [
        { name: '01-courses-list', url: `${BASE_URL}/courses` },
        { name: '02-course-detail', url: `${BASE_URL}/courses/1` },
        { name: '03-course-documents', url: `${BASE_URL}/courses/1/documents` },
        { name: '04-course-chat', url: `${BASE_URL}/courses/1/chat` }
    ];

    for (const item of pagesToCapture) {
        console.log(`Capturing ${item.name} (${item.url})...`);
        
        // Desktop
        await desktopPage.goto(item.url, { waitUntil: 'networkidle' });
        await desktopPage.waitForTimeout(1000);
        await desktopPage.screenshot({
            path: path.join(OUTPUT_DIR, `${item.name}-desktop.png`),
            fullPage: true
        });

        // Mobile
        await mobilePage.goto(item.url, { waitUntil: 'networkidle' });
        await mobilePage.waitForTimeout(1000);
        await mobilePage.screenshot({
            path: path.join(OUTPUT_DIR, `${item.name}-mobile.png`),
            fullPage: true
        });
        console.log(`✓ Saved ${item.name} (desktop + mobile)`);
    }

    await browser.close();
    console.log('Finished capturing all pages!');
}

main().catch(console.error);

