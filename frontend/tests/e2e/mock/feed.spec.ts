import { test, expect } from '@playwright/test';
import { addCoverageReport } from 'monocart-reporter';

test.describe('피드 기능 테스트 (Mock)', () => {
  test.beforeEach(async ({ page }) => {
    await page.coverage.startJSCoverage();

    // SSE Mock
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

    // API Mocking
    await page.route('**/api/users/me', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { loginId: 'testuser', nickname: '테스트유저' } }) });
    });
    await page.route('**/api/notifications/unread-count', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { count: 0 } }) });
    });

    // Feed API Mocking (첫 페이지)
    await page.route('**/api/feed?page=0*', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({
          success: true,
          data: [
            { id: 1, title: '새벽 운동 완료', content: '오늘도 달렸습니다.', authorNickname: '러너', challengeTitle: '30일 달리기', createdAt: new Date().toISOString(), likeCount: 5, commentCount: 2 },
            { id: 2, title: '코딩 공부', content: '알고리즘 문제 풀이', authorNickname: '개발자', challengeTitle: '매일 코딩', createdAt: new Date().toISOString(), likeCount: 10, commentCount: 3 }
          ],
          pagination: { totalElements: 2, totalPages: 1, pageNumber: 0, pageSize: 10 }
        })
      });
    });

    // 로그인 상태 주입
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.setItem('auth-storage', JSON.stringify({
        state: { token: "mock-token", userId: 1, loginId: "testuser", isAuthenticated: true, _hasHydrated: true },
        version: 0
      }));
    });

    await page.goto('/feed');
  });

  test('피드 목록 렌더링 확인', async ({ page }) => {
    await expect(page.getByText('새벽 운동 완료')).toBeVisible();
    await expect(page.getByText('러너')).toBeVisible();
    await expect(page.getByText('30일 달리기')).toBeVisible();
  });

  test.afterEach(async ({ page }) => {
    const coverage = await page.coverage.stopJSCoverage();
    await addCoverageReport(coverage, test.info());
  });
});
