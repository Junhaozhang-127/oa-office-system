/**
 * Playwright E2E 测试配置 — OA 协同办公平台
 *
 * 运行方式：
 *   npx playwright test --config=e2e/playwright.config.js
 */
const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: '.',
  timeout: 30000,
  expect: { timeout: 10000 },
  fullyParallel: false,
  retries: 0,
  reporter: [
    ['list'],
    ['json', { outputFile: 'e2e/results.json' }]
  ],
  use: {
    baseURL: 'http://localhost:8080',
    browserName: 'chromium',
    headless: true,
    screenshot: 'only-on-failure',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { browserName: 'chromium' },
    },
  ],
});
