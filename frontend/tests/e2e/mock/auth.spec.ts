import { test, expect } from '@playwright/test';
import { addCoverageReport } from 'monocart-reporter';

test.describe('인증 기능 테스트 (Mock)', () => {
  test.beforeEach(async ({ page }) => {
    // V8 커버리지 수집 시작
    await page.coverage.startJSCoverage();

    // 1. 브라우저의 EventSource(SSE)를 Mocking하여 에러 방지
    await page.addInitScript(() => {
      class MockEventSource {
        url: string;
        listeners: Record<string, Function> = {};
        onopen: Function | null = null;
        constructor(url: string) {
          this.url = url;
          setTimeout(() => this.onopen?.(), 10);
        }
        addEventListener(type: string, callback: Function) {
          this.listeners[type] = callback;
        }
        close() {}
      }
      (window as any).EventSource = MockEventSource;
    });

    // 2. 공통 Mock 설정
    await page.route('**/api/auth/login', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            accessToken: 'mock-token',
            refreshToken: 'mock-refresh',
            user: { loginId: 'testuser', nickname: '테스트유저' }
          }
        })
      });
    });

    await page.route('**/api/users/me', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({ success: true, data: { loginId: 'testuser', nickname: '테스트유저' } })
      });
    });

    await page.route('**/api/notifications/unread-count', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { count: 5 } }) });
    });

    await page.route('**/api/users/me/stats', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { challengeCount: 0, certificationCount: 0, followerCount: 0 } }) });
    });
  });

  test('성공적인 로그인 및 대시보드 리다이렉션', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[id="loginId"]', 'testuser');
    await page.fill('input[id="password"]', 'password123');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL(/.*\/dashboard/);
    await expect(page.locator('header')).toContainText('테스트유저');
  });

  test('성공적인 회원가입', async ({ page }) => {
    await page.route('**/api/auth/signup', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({ success: true, message: '회원가입 성공' })
      });
    });

    await page.goto('/signup');
    await page.fill('input[id="loginId"]', 'newuser');
    await page.fill('input[id="password"]', 'password123');
    await page.fill('input[id="confirmPassword"]', 'password123');
    await page.fill('input[id="nickname"]', '신규유저');
    await page.click('button:has-text("회원가입")');

    // 회원가입 성공 후 로그인 페이지로 이동하는지 확인
    await expect(page).toHaveURL(/.*\/login/);
  });

  test('로그아웃 기능 확인', async ({ page }) => {
    // 1. 로그인 상태 주입 후 대시보드로 이동
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.setItem('auth-storage', JSON.stringify({
        state: { token: "mock-token", userId: 1, loginId: "testuser", isAuthenticated: true, _hasHydrated: true },
        version: 0
      }));
    });
    await page.goto('/dashboard');

    // 2. 로그아웃 수행
    const userMenuButton = page.getByRole('button', { name: /사용자 메뉴 열기/i }).or(page.locator('button[aria-label*="메뉴"]'));
    await userMenuButton.first().click();
    await page.getByText('로그아웃').click();

    await expect(page).toHaveURL(/.*\/login/);
  });

  test.afterEach(async ({ page }) => {
    // V8 커버리지 수집 종료 및 리포트 추가
    const coverage = await page.coverage.stopJSCoverage();
    await addCoverageReport(coverage, test.info());
  });
});
