import { test, expect } from '@playwright/test';
import { addCoverageReport } from 'monocart-reporter';

test.describe('인증 기능 프로세스 테스트 (Mock)', () => {
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

    // 공통 API Mocking
    await page.route('**/api/users/me', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { loginId: 'testuser', nickname: '테스트유저' } }) });
    });
    await page.route('**/api/notifications/unread-count', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { count: 0 } }) });
    });

    // 챌린지 정보 Mocking (인증 작성 시 필요)
    await page.route('**/api/challenge/1', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({
          success: true,
          data: { id: '1', title: '아침 조깅', startDate: '2024-01-01T00:00:00', endDate: '2026-12-31T23:59:59' }
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
  });

  test('인증글 작성 및 성공', async ({ page }) => {
    // 인증 생성 API Mocking
    await page.route('**/api/certifications', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({ success: true, data: { id: 100 } })
      });
    });

    // 인증 상세 API Mocking (작성 후 이동됨)
    await page.route('**/api/certifications/100', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({
          success: true,
          data: {
            id: 100,
            title: '오늘의 조깅 완료',
            content: '5km를 뛰었습니다.',
            authorNickname: '테스트유저',
            challengeTitle: '아침 조깅',
            challengeId: '1',
            createdAt: new Date().toISOString()
          }
        })
      });
    });

    await page.goto('/certification/create?challengeId=1');
    
    await page.fill('input[id="title"]', '오늘의 조깅 완료');
    await page.fill('textarea[id="content"]', '5km를 뛰었습니다.');
    await page.click('button:has-text("인증하기")');

    // 상세 페이지로 이동 확인
    await expect(page).toHaveURL(/.*\/certification\/100/);
    await expect(page.getByText('오늘의 조깅 완료')).toBeVisible();
    await expect(page.getByText('5km를 뛰었습니다.')).toBeVisible();
  });

  test.afterEach(async ({ page }) => {
    const coverage = await page.coverage.stopJSCoverage();
    await addCoverageReport(coverage, test.info());
  });
});
