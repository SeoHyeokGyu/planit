import { test, expect } from '@playwright/test';
import { addCoverageReport } from 'monocart-reporter';

test.describe('대시보드 기능 테스트 (Mock)', () => {
  test.beforeEach(async ({ page }) => {
    // V8 커버리지 수집 시작
    await page.coverage.startJSCoverage();

    // 1. 브라우저의 EventSource(SSE)를 Mocking하는 스크립트 주입
    await page.addInitScript(() => {
      class MockEventSource {
        url: string;
        listeners: Record<string, Function> = {};
        onopen: Function | null = null;

        constructor(url: string) {
          this.url = url;
          // 연결 성공 시뮬레이션
          setTimeout(() => this.onopen?.(), 10);
          (window as any).lastEventSource = this; // 테스트에서 접근 가능하게 노출
        }

        addEventListener(type: string, callback: Function) {
          this.listeners[type] = callback;
        }

        close() {}

        // 테스트에서 알림을 보낼 때 사용할 커스텀 메서드
        emit(type: string, data: any) {
          if (this.listeners[type]) {
            this.listeners[type]({ data: JSON.stringify(data) });
          }
        }
      }
      (window as any).EventSource = MockEventSource;
    });

    // 2. API Mocking
    await page.route('**/api/users/me/stats', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            nickname: '테스트유저',
            challengeCount: 12,
            certificationCount: 45,
            followerCount: 128,
            followingCount: 50
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
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { count: 0 } }) });
    });

    await page.route('**/api/feed*', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { content: [], last: true } }) });
    });

    // 3. 로그인 상태 주입
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.setItem('auth-storage', JSON.stringify({
        state: { token: "mock-token", userId: 1, loginId: "testuser", isAuthenticated: true, _hasHydrated: true },
        version: 0
      }));
    });

    await page.goto('/dashboard');
  });

  test('대시보드 통계 숫자가 올바르게 표시됨', async ({ page }) => {
    await expect(page.getByText('참여 중인 챌린지')).toBeVisible();
    await expect(page.getByText('12', { exact: true })).toBeVisible();
    await expect(page.getByText('완료한 인증')).toBeVisible();
    await expect(page.getByText('45', { exact: true })).toBeVisible();
  });

  test('실시간 알림 수신 시 뱃지 개수 확인', async ({ page }) => {
    // 알림 3개를 실시간으로 받은 것처럼 시뮬레이션
    await page.evaluate(() => {
      const es = (window as any).lastEventSource;
      if (es) {
        es.emit('notification', { id: '1', type: 'INFO', message: '첫 번째 알림', createdAt: new Date().toISOString() });
        es.emit('notification', { id: '2', type: 'SUCCESS', message: '두 번째 알림', createdAt: new Date().toISOString() });
        es.emit('notification', { id: '3', type: 'NEW_FEED', message: '세 번째 알림', createdAt: new Date().toISOString() });
      }
    });

    // Header 영역 내의 알림 숫자 '3'이 나타나는지 확인
    const badge = page.locator('header').getByText('3');
    await badge.waitFor({ state: 'visible', timeout: 10000 });
    await expect(badge).toBeVisible();

    // 알림 드롭다운을 열어 메시지 내용까지 확인 (선택 사항)
    await page.getByTitle('알림').click();
    await expect(page.locator('header').getByText('첫 번째 알림')).toBeVisible();
    await expect(page.locator('header').getByText('세 번째 알림')).toBeVisible();
  });

  test.afterEach(async ({ page }) => {
    // V8 커버리지 수집 종료 및 리포트 추가
    const coverage = await page.coverage.stopJSCoverage();
    await addCoverageReport(coverage, test.info());
  });
});
