import { test, expect } from '@playwright/test';
import { addCoverageReport } from 'monocart-reporter';

test.describe('프로필 기능 테스트 (Mock)', () => {
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
        body: JSON.stringify({ 
          success: true, 
          data: { 
            loginId: 'testuser', 
            nickname: '테스트유저', 
            email: 'test@example.com', 
            createdAt: '2024-01-01T00:00:00' 
          } 
        })
      });
    });

    await page.route('**/api/notifications/unread-count', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { count: 0 } }) });
    });

    await page.route('**/api/follows/testuser/stats', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { followerCount: 10, followingCount: 20 } }) });
    });

    await page.route('**/api/streaks?*', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({ 
          success: true, 
          data: { 
            totalCurrentStreak: 5, 
            totalMaxStreak: 10,
            activeStreakCount: 1,
            maxLongestStreak: 10,
            streaks: []
          } 
        })
      });
    });

    await page.route('**/api/badges/testuser', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: [] }) });
    });

    await page.route('**/api/certifications/user/testuser*', async route => {
      // data가 바로 배열이어야 CertificationsSection에서 listData?.content (null/undefined일 때 []) 처리가 의도한 대로 동작하거나,
      // 혹은 useCertification hook의 select에서 data.data를 반환하므로 data.data가 배열이어야 함.
      await route.fulfill({ 
        status: 200, 
        body: JSON.stringify({ 
          success: true, 
          data: [], // useCertificationsByUser hook의 select에서 data.data를 content로 사용함
          pagination: { totalElements: 0, totalPages: 0, pageNumber: 0, pageSize: 10 }
        }) 
      });
    });

    await page.route('**/api/rankings/me', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { weekly: { rank: 1 } } }) });
    });

    // 3. 로그인 상태 주입
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.setItem('auth-storage', JSON.stringify({
        state: { token: "mock-token", userId: 1, loginId: "testuser", isAuthenticated: true, _hasHydrated: true },
        version: 0
      }));
    });

    await page.goto('/profile');
  });

  test('프로필 정보 표시 확인', async ({ page }) => {
    await expect(page.locator('h1, h2').filter({ hasText: '테스트유저' }).first()).toBeVisible();
  });

  test('탭 전환 확인 (스트릭)', async ({ page }) => {
    // 1. 프로필 정보 로드 대기
    await expect(page.locator('h1, h2').filter({ hasText: '테스트유저' }).first()).toBeVisible();

    // 2. 하단 탭 영역의 '스트릭' 버튼 클릭
    const streakTab = page.getByRole('button', { name: '스트릭', exact: true });
    await expect(streakTab).toBeVisible();
    await streakTab.click();
    
    // 3. StreaksSection 내의 데이터 확인
    await expect(page.getByText('스트릭 현황')).toBeVisible();
    await expect(page.locator('main').last()).toContainText('5');
  });

  test.afterEach(async ({ page }) => {
    // V8 커버리지 수집 종료 및 리포트 추가
    const coverage = await page.coverage.stopJSCoverage();
    await addCoverageReport(coverage, test.info());
  });
});
