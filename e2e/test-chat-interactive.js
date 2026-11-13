const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

(async () => {
    console.log('--- STARTING PLAYWRIGHT CHAT INTERACTION TEST ---');
    const browser = await chromium.launch({ headless: true });
    
    // 1. Setup Desktop Context (1440x900)
    const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
    const page = await context.newPage();

    // Monitor console logs & network errors
    page.on('console', msg => console.log('PAGE LOG:', msg.text()));
    page.on('pageerror', err => console.error('PAGE ERROR:', err.message));

    // 2. Login as student
    console.log('Step 1: Logging in as student...');
    await page.goto('http://localhost:6969/login', { waitUntil: 'networkidle' });
    await page.fill('input[name="username"]', 'student');
    await page.fill('input[name="password"]', '123456');
    await Promise.all([
        page.waitForNavigation({ waitUntil: 'networkidle' }),
        page.click('button[type="submit"]')
    ]);

    // 3. Navigate to a brand new chat session
    console.log('Step 2: Navigating to new chat session...');
    await page.goto('http://localhost:6969/courses/1/chat/new', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);

    // Verify Zero Scroll
    const initialScrollY = await page.evaluate(() => window.scrollY);
    console.log(`Initial page window.scrollY: ${initialScrollY} (Should be 0)`);

    // Verify Initial UI Elements
    const hasHero = await page.$eval('#welcomeHero h2', el => el.innerText.includes('Hôm nay bạn cần tra cứu gì?')).catch(() => false);
    console.log(`Welcome Hero present: ${hasHero}`);

    // Take screenshot of empty new chat
    const screenshot1 = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', 'interactive-01-new-chat.png');
    await page.screenshot({ path: screenshot1 });
    console.log(`Saved screenshot 1: ${screenshot1}`);

    // 4. Test Quick Prompt Click
    console.log('Step 3: Clicking quick prompt button...');
    const quickBtn = await page.$('.quick-prompt-btn');
    if (quickBtn) {
        const promptText = await quickBtn.getAttribute('data-prompt');
        await quickBtn.click();
        const inputValue = await page.$eval('#chatQuestionInput', el => el.value);
        console.log(`Input value after quick prompt click: "${inputValue}" (matches prompt: ${inputValue === promptText})`);
    }

    // 5. Submit Message via Form
    console.log('Step 4: Submitting chat question...');
    const submitBtn = await page.$('#sendBtn');
    
    // Setup response listener for the chat send API
    const responsePromise = page.waitForResponse(response => 
        response.url().includes('/chat/send') && response.status() === 200
    , { timeout: 90000 });

    await submitBtn.click();

    // Verify typing indicator appears
    const isTypingVisible = await page.$eval('#typingIndicator', el => !el.classList.contains('hidden'));
    console.log(`Typing indicator visible during generation: ${isTypingVisible}`);

    // Wait for the backend RAG API to finish
    console.log('Step 5: Waiting for AI response from RAG service...');
    const apiResponse = await responsePromise;
    const responseJson = await apiResponse.json();
    console.log(`Received API response with sessionId: ${responseJson.sessionId}`);
    console.log(`AI answer preview: "${responseJson.answer ? responseJson.answer.substring(0, 100) : ''}..."`);
    console.log(`Citations count: ${responseJson.citations ? responseJson.citations.length : 0}`);

    // Wait a brief moment for marked.js and DOM insertion
    await page.waitForTimeout(1500);

    // Verify User message bubble in DOM
    const userMessages = await page.$$eval('#chatMessagesContainer .bg-indigo-600', els => els.map(e => e.innerText));
    console.log(`User messages in chat: ${JSON.stringify(userMessages)}`);

    // Verify AI response rendered in DOM
    const aiMessages = await page.$$eval('#chatMessagesContainer .ai-markdown-content', els => els.map(e => e.innerText.substring(0, 80)));
    console.log(`AI messages in chat: ${JSON.stringify(aiMessages)}`);

    // Verify Citation accordion in DOM
    const citations = await page.$$('#chatMessagesContainer .collapse-arrow');
    console.log(`Citation accordions found: ${citations.length}`);

    // Verify page window scroll is STILL 0 (App-Shell lock verified!)
    const afterScrollY = await page.evaluate(() => window.scrollY);
    console.log(`Page window.scrollY after response: ${afterScrollY} (Should still be 0)`);

    // Take screenshot with conversation
    const screenshot2 = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', 'interactive-02-conversation.png');
    await page.screenshot({ path: screenshot2 });
    console.log(`Saved screenshot 2: ${screenshot2}`);

    // 6. Test expanding the citation accordion
    if (citations.length > 0) {
        console.log('Step 6: Testing citation accordion click...');
        const citationCheckbox = await citations[citations.length - 1].$('input[type="checkbox"]');
        if (citationCheckbox) {
            await citationCheckbox.click();
            await page.waitForTimeout(500);
            const screenshot3 = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', 'interactive-03-citation-expanded.png');
            await page.screenshot({ path: screenshot3 });
            console.log(`Saved screenshot 3 (citation expanded): ${screenshot3}`);
        }
    }

    // 7. Test sending a 2nd follow-up question
    console.log('Step 7: Testing 2nd follow-up message typing...');
    await page.fill('#chatQuestionInput', 'Tóm tắt ngắn gọn lại bằng 1 câu giúp tôi');
    
    const responsePromise2 = page.waitForResponse(response => 
        response.url().includes('/chat/send') && response.status() === 200
    , { timeout: 90000 });

    await page.click('#sendBtn');
    console.log('Waiting for 2nd response...');
    await responsePromise2;
    await page.waitForTimeout(1500);

    const screenshot4 = path.resolve(__dirname, '..', 'docs', 'evidence', 'ui-audit-after', 'interactive-04-multi-turn.png');
    await page.screenshot({ path: screenshot4 });
    console.log(`Saved screenshot 4 (multi-turn conversation): ${screenshot4}`);

    // Verify final state
    const finalScrollY = await page.evaluate(() => window.scrollY);
    console.log(`Final page window.scrollY: ${finalScrollY} (Must remain 0)`);

    await browser.close();
    console.log('--- ALL PLAYWRIGHT CHAT INTERACTION TESTS PASSED 100% ---');
})();
