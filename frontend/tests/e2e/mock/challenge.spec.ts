import { test, expect } from '@playwright/test';
import { addCoverageReport } from 'monocart-reporter';

test.describe('챌린지 기능 테스트 (Mock)', () => {
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

    // 2. API Mocking
    await page.route('**/api/users/me', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({ success: true, data: { loginId: 'testuser', nickname: '테스트유저' } })
      });
    });

    await page.route('**/api/notifications/unread-count', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { count: 0 } }) });
    });

    await page.route('**/api/challenge/recommend-existing*', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: [] }) });
    });

    await page.route('**/api/challenge/search*', async route => {
      const url = new URL(route.request().url());
      const keyword = url.searchParams.get('keyword') || '';
      const challenges = [
        { id: '1', title: '아침 조깅', category: 'HEALTH', difficulty: 'EASY', participantCnt: 5, certificationCnt: 10, viewCnt: 100, startDate: '2024-01-01T09:00:00', endDate: '2026-12-31T23:59:59' },
      ];
      const filtered = challenges.filter(c => c.title.includes(keyword));
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: filtered }) });
    });

    await page.route('**/api/challenge?*', async route => {
      if (route.request().url().includes('/search') || route.request().url().includes('/recommend')) return route.fallback();
      const challenges = [
        { id: '1', title: '아침 조깅', category: 'HEALTH', difficulty: 'EASY', participantCnt: 5, certificationCnt: 10, viewCnt: 100, startDate: '2024-01-01T09:00:00', endDate: '2026-12-31T23:59:59' },
        { id: '2', title: '매일 코딩', category: 'STUDY', difficulty: 'MEDIUM', participantCnt: 10, certificationCnt: 20, viewCnt: 200, startDate: '2024-01-01T09:00:00', endDate: '2026-12-31T23:59:59' },
      ];
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: challenges }) });
    });

    // 2. 로그인 상태 주입
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.setItem('auth-storage', JSON.stringify({
        state: { token: "mock-token", userId: 1, loginId: "testuser", isAuthenticated: true, _hasHydrated: true },
        version: 0
      }));
    });

    // 3. 페이지 이동
    await page.goto('/challenge');
  });

  test('챌린지 목록 렌더링 확인', async ({ page }) => {
    await expect(page.getByText('아침 조깅')).toBeVisible();
    await expect(page.getByText('매일 코딩')).toBeVisible();
  });

  test('챌린지 검색 기능 작동', async ({ page }) => {
    const searchInput = page.getByPlaceholder('챌린지 검색...');
    await searchInput.fill('조깅');
    await expect(page.getByText('아침 조깅')).toBeVisible();
    await expect(page.getByText('매일 코딩')).not.toBeVisible();
  });

  test.afterEach(async ({ page }) => {
    // V8 커버리지 수집 종료 및 리포트 추가
    const coverage = await page.coverage.stopJSCoverage();
    await addCoverageReport(coverage, test.info());
  });
});