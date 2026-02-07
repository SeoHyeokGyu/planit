import { test, expect } from '@playwright/test';
import { addCoverageReport } from 'monocart-reporter';

test.describe('사용자 찾기 기능 테스트 (Mock)', () => {
  test.beforeEach(async ({ page }) => {
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

    // 2. API Mocking
    await page.route('**/api/users/me', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { loginId: 'testuser', nickname: '테스트유저' } }) });
    });
    await page.route('**/api/notifications/unread-count', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { count: 0 } }) });
    });

    // 추천 사용자 API
    await page.route('**/api/users/random*', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({
          success: true,
          data: [{ loginId: 'random1', nickname: '추천유저1', totalPoint: 1000, createdAt: new Date().toISOString() }]
        })
      });
    });

    // 팔로잉 목록 API
    await page.route('**/api/follows/testuser/followings*', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: [] }) });
    });

    // 3. 로그인 상태 주입
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.setItem('auth-storage', JSON.stringify({
        state: { token: "mock-token", userId: 1, loginId: "testuser", isAuthenticated: true, _hasHydrated: true },
        version: 0
      }));
    });

    // 4. 페이지 이동 및 로딩 확인
    await page.goto('/users');
    await expect(page.getByText('사용자 찾기', { exact: false }).first()).toBeVisible();
  });

  test('사용자 검색 및 결과 렌더링', async ({ page }) => {
    // 검색 API Mocking
    await page.route('**/api/users/search*', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({
          success: true,
          data: [{ loginId: 'founduser', nickname: '찾은유저', totalPoint: 500, createdAt: new Date().toISOString() }]
        })
      });
    });

    // 1. 검색창 찾기 (가장 명확한 placeholder 사용)
    // const searchInput = page.getByPlaceholder('사용자 이름 또는 아이디로 검색...');
    const searchInput = page.getByRole('textbox', { name: '사용자 이름 또는 아이디로 검색' });

    // 2. 검색어 입력 (fill은 자동으로 대기, 포커스, 입력을 수행함)
    await searchInput.fill('찾은유저');

    // 3. 결과 확인 (main 영역 내에서 '찾은유저' 텍스트 확인)
    const resultList = page.locator('main');
    await expect(resultList.getByText('찾은유저')).toBeVisible({ timeout: 7000 });
    await expect(resultList.getByText('@founduser')).toBeVisible();
  });

  test.afterEach(async ({ page }) => {
    const coverage = await page.coverage.stopJSCoverage();
    await addCoverageReport(coverage, test.info());
  });
});
