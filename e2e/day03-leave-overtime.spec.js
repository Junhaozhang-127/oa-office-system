/**
 * 第三天：请假加班与流程启动 — E2E 测试
 * 使用 Playwright 对 OA 协同办公平台进行自动化测试
 *
 * 运行方式：
 *   npx playwright test --config=e2e/playwright.config.js
 *
 * 前置条件：
 *   1. Spring Boot 应用运行在 http://localhost:8080
 *   2. MySQL 数据库 oa_office 已初始化
 */
const { test, expect } = require('@playwright/test');

const BASE_URL = 'http://localhost:8080';

// sidebar 中 nav-item 的 onclick 属性选择器
// Note: using exact match because CSS *= substring selector fails on parentheses
const NAV = {
  workbench:   '[onclick="switchTab(0,this)"]',
  calendar:    '[onclick="switchTab(1,this)"]',
  dashboard:   '[onclick="switchTab(2,this)"]',
  approval:    '[onclick="switchTab(3,this)"]',
  notice:      '[onclick="switchTab(4,this)"]',
  leave:       '[onclick="switchTab(5,this)"]',
  overtime:    '[onclick="switchTab(6,this)"]',
  myApps:      '[onclick="switchTab(7,this)"]',
};

/* ────────── 已有功能兼容性 ────────── */
test.describe('兼容性：已有功能不受影响', () => {

  test('首页加载成功', async ({ page }) => {
    const res = await page.goto(BASE_URL);
    expect(res.status()).toBe(200);
    await expect(page.locator('.topbar .logo')).toContainText('OA协同办公平台');
  });

  test('员工列表面板正常渲染', async ({ page }) => {
    await page.goto(BASE_URL);
    await page.waitForFunction(() => {
      const tbody = document.getElementById('empTableBody');
      return tbody && !tbody.textContent.includes('加载中');
    }, { timeout: 10000 });
    await expect(page.locator('#empTableBody tr').first()).not.toContainText('加载中');
  });

  test('考勤日历面板正常渲染', async ({ page }) => {
    await page.goto(BASE_URL);
    await page.click(NAV.calendar);
    // 等待日历网格出现（empSelect 在 onload 时已填充）
    await page.waitForSelector('.cal-day', { timeout: 10000 });
    await expect(page.locator('.cal-day').first()).toBeVisible();
  });

});

/* ────────── 请假申请功能 ────────── */
test.describe('请假申请', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(BASE_URL);
    await page.click(NAV.leave);
    // 等待申请人下拉框被初始化填充
    await page.waitForFunction(() => {
      const sel = document.getElementById('leaveEmpId');
      return sel && sel.options.length > 1;
    }, { timeout: 10000 });
  });

  test('页面元素完整渲染', async ({ page }) => {
    await expect(page.locator('#leaveEmpId')).toBeVisible();
    await expect(page.locator('#leaveType')).toBeVisible();
    await expect(page.locator('#leaveStartDate')).toBeVisible();
    await expect(page.locator('#leaveEndDate')).toBeVisible();
    await expect(page.locator('#leaveReason')).toBeVisible();
    await expect(page.locator('#leaveForm button:has-text("提交申请")')).toBeVisible();
    await expect(page.locator('#leaveForm button:has-text("重置")')).toBeVisible();
  });

  test('自动计算请假天数', async ({ page }) => {
    await page.fill('#leaveStartDate', '2026-07-01');
    await page.locator('#leaveStartDate').dispatchEvent('change');
    await page.fill('#leaveEndDate', '2026-07-03');
    await page.locator('#leaveEndDate').dispatchEvent('change');
    await expect(page.locator('#leaveDays')).toContainText('3');
  });

  test('必填校验：未选择类型时阻止提交', async ({ page }) => {
    await page.locator('#leaveEmpId').selectOption({ index: 0 });
    await page.fill('#leaveStartDate', '2026-07-01');
    await page.fill('#leaveEndDate', '2026-07-02');
    await page.fill('#leaveReason', '测试必填校验');
    await page.locator('#leaveForm button:has-text("提交申请")').click();
    await expect(page.locator('#leaveMsg')).toContainText('请选择请假类型');
  });

  test('日期校验：结束日期早于开始日期时阻止提交', async ({ page }) => {
    await page.locator('#leaveEmpId').selectOption({ index: 0 });
    await page.locator('#leaveType').selectOption('1');
    await page.fill('#leaveStartDate', '2026-07-05');
    await page.fill('#leaveEndDate', '2026-07-01');
    await page.fill('#leaveReason', '测试日期校验');
    await page.locator('#leaveForm button:has-text("提交申请")').click();
    await expect(page.locator('#leaveMsg')).toContainText('结束日期必须晚于开始日期');
  });

  test('成功提交请假申请，状态为待审批', async ({ page }) => {
    await page.locator('#leaveEmpId').selectOption({ index: 0 });
    await page.locator('#leaveType').selectOption('1'); // 病假
    await page.fill('#leaveStartDate', '2026-07-10');
    await page.fill('#leaveEndDate', '2026-07-11');
    await page.fill('#leaveReason', 'E2E测试-身体不适');
    await page.locator('#leaveForm button:has-text("提交申请")').click();
    await expect(page.locator('#leaveMsg')).toContainText('提交成功');
    await expect(page.locator('#leaveMsg')).toContainText('待审批');
    await expect(page.locator('#leaveMsg')).toContainText('QJ');
  });

  test('重置按钮清空表单', async ({ page }) => {
    await page.locator('#leaveType').selectOption('2');
    await page.fill('#leaveStartDate', '2026-07-01');
    await page.fill('#leaveEndDate', '2026-07-02');
    await page.fill('#leaveReason', '测试重置');
    await page.locator('#leaveForm button:has-text("重置")').click();
    await expect(page.locator('#leaveType')).toHaveValue('');
    await expect(page.locator('#leaveStartDate')).toHaveValue('');
    await expect(page.locator('#leaveEndDate')).toHaveValue('');
    await expect(page.locator('#leaveReason')).toHaveValue('');
  });

});

/* ────────── 加班申请功能 ────────── */
test.describe('加班申请', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(BASE_URL);
    await page.click(NAV.overtime);
    await page.waitForFunction(() => {
      const sel = document.getElementById('overtimeEmpId');
      return sel && sel.options.length > 1;
    }, { timeout: 10000 });
  });

  test('页面元素完整渲染', async ({ page }) => {
    await expect(page.locator('#overtimeEmpId')).toBeVisible();
    await expect(page.locator('#overtimeType')).toBeVisible();
    await expect(page.locator('#overtimeStartTime')).toBeVisible();
    await expect(page.locator('#overtimeEndTime')).toBeVisible();
    await expect(page.locator('#overtimeReason')).toBeVisible();
    await expect(page.locator('#overtimeForm button:has-text("提交申请")')).toBeVisible();
  });

  test('自动计算加班小时数', async ({ page }) => {
    await page.fill('#overtimeStartTime', '2026-07-01T18:00');
    await page.locator('#overtimeStartTime').dispatchEvent('change');
    await page.fill('#overtimeEndTime', '2026-07-01T21:30');
    await page.locator('#overtimeEndTime').dispatchEvent('change');
    await expect(page.locator('#overtimeHours')).toContainText('3.5');
  });

  test('成功提交加班申请，状态为待审批', async ({ page }) => {
    await page.locator('#overtimeEmpId').selectOption({ index: 0 });
    await page.locator('#overtimeType').selectOption('1'); // 工作日加班
    await page.fill('#overtimeStartTime', '2026-07-15T18:00');
    await page.fill('#overtimeEndTime', '2026-07-15T22:00');
    await page.fill('#overtimeReason', 'E2E测试-项目上线');
    await page.locator('#overtimeForm button:has-text("提交申请")').click();
    await expect(page.locator('#overtimeMsg')).toContainText('提交成功');
    await expect(page.locator('#overtimeMsg')).toContainText('待审批');
    await expect(page.locator('#overtimeMsg')).toContainText('JB');
  });

  test('加班必填校验', async ({ page }) => {
    await page.locator('#overtimeEmpId').selectOption({ index: 0 });
    await page.fill('#overtimeStartTime', '2026-07-01T18:00');
    await page.fill('#overtimeEndTime', '2026-07-01T21:00');
    await page.fill('#overtimeReason', '测试');
    await page.locator('#overtimeForm button:has-text("提交申请")').click();
    await expect(page.locator('#overtimeMsg')).toContainText('请选择加班类型');
  });

});

/* ────────── 我的申请列表 ────────── */
test.describe('我的申请列表', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(BASE_URL);
    await page.click(NAV.myApps);
    await page.waitForFunction(() => {
      const sel = document.getElementById('myAppEmpId');
      return sel && sel.options.length > 1;
    }, { timeout: 10000 });
  });

  test('页面元素完整渲染', async ({ page }) => {
    await expect(page.locator('#myAppEmpId')).toBeVisible();
    await expect(page.locator('#appTableBody')).toBeVisible();
  });

  test('选择员工后加载申请列表', async ({ page }) => {
    await page.locator('#myAppEmpId').selectOption({ index: 0 });
    await page.waitForFunction(() => {
      const tbody = document.getElementById('appTableBody');
      return tbody && !tbody.textContent.includes('加载中');
    }, { timeout: 10000 });
    await expect(page.locator('#appTableBody')).not.toContainText('加载中');
  });

  test('申请列表包含请假和加班记录', async ({ page }) => {
    await page.locator('#myAppEmpId').selectOption({ index: 0 });
    await page.waitForFunction(() => {
      const tbody = document.getElementById('appTableBody');
      return tbody && !tbody.textContent.includes('加载中');
    }, { timeout: 10000 });

    const tags = page.locator('#appTableBody .tag');
    const count = await tags.count();
    expect(count).toBeGreaterThanOrEqual(1);
  });

  test('状态标签显示中文映射', async ({ page }) => {
    await page.locator('#myAppEmpId').selectOption({ index: 0 });
    await page.waitForFunction(() => {
      const tbody = document.getElementById('appTableBody');
      return tbody && !tbody.textContent.includes('加载中');
    }, { timeout: 10000 });

    const statusCell = page.locator('#appTableBody td:nth-child(6) .tag').first();
    if (await statusCell.count() > 0) {
      const text = await statusCell.textContent();
      expect(['待审批', '已完成', '已驳回', '经理已审批', '财务已审批']).toContain(text);
    }
  });

});

/* ────────── API 集成测试 ────────── */
test.describe('API 接口测试', () => {

  test('POST /api/leave-requests 返回标准格式', async ({ request }) => {
    const res = await request.post(BASE_URL + '/api/leave-requests', {
      data: { empId: 1, leaveType: 3, startDate: '2026-08-01', endDate: '2026-08-03', reason: 'API集成测试-年假' }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
    expect(body.data.status).toBe('PENDING');
    expect(body.data.days).toBe(3);
    expect(body.data.leaveNo).toMatch(/^QJ\d{16}$/);
  });

  test('POST /api/overtime-requests 返回标准格式', async ({ request }) => {
    const res = await request.post(BASE_URL + '/api/overtime-requests', {
      data: { empId: 1, overtimeType: 2, startTime: '2026-08-05T09:00:00', endTime: '2026-08-05T18:00:00', reason: 'API集成测试-周末加班' }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
    expect(body.data.status).toBe('PENDING');
    expect(body.data.hours).toBe(9);
  });

  test('GET /api/applications/my 合并请假和加班记录', async ({ request }) => {
    const res = await request.get(BASE_URL + '/api/applications/my?empId=1');
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
    expect(body.data.total).toBeGreaterThanOrEqual(1);
    expect(body.data.rows.length).toBe(body.data.total);
    const row = body.data.rows[0];
    expect(row).toHaveProperty('applicationType');
    expect(row).toHaveProperty('applicationTypeText');
    expect(row).toHaveProperty('timeRange');
    expect(row).toHaveProperty('amount');
    expect(row).toHaveProperty('amountUnit');
    expect(row).toHaveProperty('statusText');
  });

  test('请假参数校验：结束日期不能早于开始日期', async ({ request }) => {
    const res = await request.post(BASE_URL + '/api/leave-requests', {
      data: { empId: 1, leaveType: 1, startDate: '2026-09-10', endDate: '2026-09-05', reason: '测试非法日期' }
    });
    const body = await res.json();
    expect(body.code).toBe(500);
    expect(body.msg).toContain('结束日期必须晚于开始日期');
  });

  test('请假参数校验：必填字段为空时返回错误', async ({ request }) => {
    const res = await request.post(BASE_URL + '/api/leave-requests', {
      data: { empId: 1, leaveType: null, startDate: '2026-09-01', endDate: '2026-09-02', reason: '' }
    });
    const body = await res.json();
    expect(body.code).toBe(500);
  });

  test('已有 API 仍正常：/api/employee/list', async ({ request }) => {
    const res = await request.get(BASE_URL + '/api/employee/list');
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
    expect(body.data.total).toBeGreaterThanOrEqual(1);
  });

  test('已有 API 仍正常：/api/attendance/calendar', async ({ request }) => {
    const res = await request.get(BASE_URL + '/api/attendance/calendar?empId=1&year=2026&month=6');
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
    expect(body.data.calendarDays.length).toBeGreaterThan(0);
  });

});
