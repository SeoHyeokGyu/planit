import { test, expect } from '@playwright/test';
import path from 'path';

// 스크린샷 저장 경로 설정 (상위 디렉토리의 docs/images)
const SCREENSHOT_DIR = path.resolve(__dirname, '../../../docs/images');

test.describe('프로젝트 스크린샷 생성기', () => {
  // 전체 테스트 타임아웃을 30초로 연장
  test.setTimeout(30000);

  test.beforeEach(async ({ page }) => {
    // SSE Mock
    await page.addInitScript(() => {
      class MockEventSource {
        url: string;
        listeners: Record<string, Function> = {};
        onopen: Function | null = null;
        constructor(url: string) {
          this.url = url;
          setTimeout(() => this.onopen?.(), 10);
          (window as any).lastEventSource = this;
        }
        addEventListener(type: string, callback: Function) {
          this.listeners[type] = callback;
        }
        close() {}
        emit(type: string, data: any) {
          if (this.listeners[type]) {
            this.listeners[type]({ data: JSON.stringify(data) });
          }
        }
      }
      (window as any).EventSource = MockEventSource;
    });

    // 공통 API Mocking
    await page.route('**/api/users/me', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { loginId: 'testuser', nickname: '테스트유저', email: 'test@example.com', createdAt: '2024-01-01T00:00:00' } }) });
    });
    await page.route('**/api/notifications/unread-count', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { count: 3 } }) });
    });
    // 조회수 증가 API Mocking
    await page.route('**/api/challenge/*/view', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true }) });
    });

    // 개발 모드 에러 오버레이 숨기기 (CSS 주입)
    await page.addInitScript(() => {
      const style = document.createElement('style');
      style.textContent = `
        #nextjs-portal, 
        [data-nextjs-dialog-overlay], 
        [data-nextjs-toast] { 
          display: none !important; 
        }
      `;
      document.head.appendChild(style);
    });
  });

  // 더 안정적인 로그인 주입 방식
  const setupAuth = async (page) => {
    await page.addInitScript(() => {
      window.localStorage.setItem('auth-storage', JSON.stringify({
        state: { token: "mock-token", userId: 1, loginId: "testuser", isAuthenticated: true, _hasHydrated: true },
        version: 0
      }));
    });
  };

  test('01-landing: 랜딩 페이지', async ({ page }) => {
    await page.goto('/');
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '01-landing.png'), fullPage: true });
  });

  test('01-login: 로그인 페이지', async ({ page }) => {
    await page.goto('/login');
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '01-login.png') });
  });

  test('01-signup: 회원가입 페이지', async ({ page }) => {
    await page.goto('/signup');
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '01-signup.png') });
  });

  test('02-dashboard: 대시보드', async ({ page }) => {
    await setupAuth(page);
    await page.route('**/api/users/me/stats', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { challengeCount: 5, certificationCount: 24, followerCount: 128, followingCount: 56 } }) });
    });
    
    await page.route('**/api/feed*', async route => {
      await route.fulfill({ 
        status: 200, 
        body: JSON.stringify({ 
          success: true, 
          data: [
            { id: 101, title: '아침 조깅 5km 인증', content: '오늘도 상쾌하게 시작합니다!', authorNickname: '러너킴', challengeTitle: '아침 조깅 5km', createdAt: new Date().toISOString(), likeCount: 12, commentCount: 4 },
            { id: 102, title: '알고리즘 1문제 풀이', content: '백준 골드 문제 해결!', authorNickname: '개발자리', challengeTitle: '매일 코딩', createdAt: new Date().toISOString(), likeCount: 8, commentCount: 2 },
            { id: 103, title: '독서 30분 완료', content: '클린 아키텍처 3장 읽기', authorNickname: '독서왕', challengeTitle: '하루 30분 독서', createdAt: new Date().toISOString(), likeCount: 15, commentCount: 1 }
          ],
          pagination: { totalElements: 3, totalPages: 1, pageNumber: 0, pageSize: 10 }
        }) 
      });
    });

    await page.route('**/api/streaks?*', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { totalCurrentStreak: 7, totalMaxStreak: 15 } }) });
    });

    await page.goto('/dashboard');
    await expect(page.getByText('참여 중인 챌린지')).toBeVisible({ timeout: 15000 });
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '02-dashboard.png'), fullPage: true });
  });

  test('03-challenge-list: 챌린지 목록', async ({ page }) => {
    await setupAuth(page);
    await page.route('**/api/challenge?*', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({
          success: true,
          data: [
            { id: '1', title: '아침 조깅 5km', description: '매일 아침 상쾌하게 달려요!', category: 'HEALTH', difficulty: 'EASY', participantCnt: 45, certificationCnt: 120, viewCnt: 850, startDate: '2024-01-01T09:00:00', endDate: '2026-12-31T23:59:59' },
            { id: '2', title: '매일 코딩 한 시간', description: '꾸준한 코딩 습관을 만듭니다.', category: 'STUDY', difficulty: 'MEDIUM', participantCnt: 32, certificationCnt: 240, viewCnt: 1200, startDate: '2024-01-01T09:00:00', endDate: '2026-12-31T23:59:59' },
            { id: '3', title: '하루 한 번 명상', description: '마음의 평화를 찾는 시간.', category: 'LIFESTYLE', difficulty: 'EASY', participantCnt: 18, certificationCnt: 60, viewCnt: 320, startDate: '2024-01-01T09:00:00', endDate: '2026-12-31T23:59:59' }
          ]
        })
      });
    });
    
    await page.route('**/api/challenge/recommend-existing*', async route => {
      await route.fulfill({ 
        status: 200, 
        body: JSON.stringify({ 
          success: true, 
          data: [
            { challenge: { id: '4', title: '주 3회 필라테스', description: '코어 강화와 유연성 향상!', category: 'HEALTH', difficulty: 'MEDIUM', participantCnt: 12, certificationCnt: 30, viewCnt: 150, startDate: '2024-01-01T09:00:00', endDate: '2026-12-31T23:59:59' }, reason: '최근 유산소 활동과의 밸런스를 위해 추천합니다.' },
            { challenge: { id: '5', title: '매일 물 2L 마시기', description: '가장 쉬운 건강 습관!', category: 'LIFESTYLE', difficulty: 'EASY', participantCnt: 85, certificationCnt: 420, viewCnt: 2100, startDate: '2024-01-01T09:00:00', endDate: '2026-12-31T23:59:59' }, reason: '규칙적인 수분 보충은 컨디션 유지에 필수적입니다.' },
            { challenge: { id: '6', title: 'IT 트렌드 정독하기', description: '매일 아침 기술 트렌드 파악.', category: 'STUDY', difficulty: 'NORMAL', participantCnt: 24, certificationCnt: 150, viewCnt: 600, startDate: '2024-01-01T09:00:00', endDate: '2026-12-31T23:59:59' }, reason: '자기 계발 열정을 이어가실 수 있는 맞춤 챌린지입니다.' }
          ] 
        }) 
      });
    });

    await page.goto('/challenge');
    await page.getByRole('button', { name: '내 취향 추천' }).click();
    await expect(page.getByText('AI가 회원님을 위해 골랐어요!')).toBeVisible({ timeout: 10000 });
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '03-challenge-list.png'), fullPage: true });
  });

  test('03-challenge-detail: 챌린지 상세', async ({ page }) => {
    await setupAuth(page);
    await page.route('**/api/challenge/1', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({
          success: true,
          data: { id: '1', title: '아침 조깅 5km', description: '매일 아침 5km를 달리는 챌린지입니다.', category: 'HEALTH', difficulty: 'EASY', participantCnt: 45, certificationCnt: 120, viewCnt: 850, startDate: '2024-01-01T09:00:00', endDate: '2026-12-31T23:59:59', createdId: 'runner_pro' }
        })
      });
    });
    await page.route('**/api/challenge/my', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: [{ id: '1' }] }) });
    });

    await page.goto('/challenge/1');
    await expect(page.getByText('챌린지 상세')).toBeVisible({ timeout: 10000 });
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '03-challenge-detail.png'), fullPage: true });
  });

  test('03-ai-generator: AI 챌린지 생성기', async ({ page }) => {
    await setupAuth(page);
    await page.route('**/api/challenge/recommend*', async route => {
      if (route.request().url().includes('recommend-existing')) return route.fallback();
      await route.fulfill({ 
        status: 200, 
        body: JSON.stringify({ 
          success: true, 
          data: [
            { 
              title: '침대에 눕기 전 10분 감사 일기 작성하기', 
              description: '하루를 마무리하며 긍정적인 사고를 훈련하는 마음 챙김 습관입니다.', 
              category: 'LIFESTYLE', 
              difficulty: 'EASY', 
              reason: '최근 활동이 많아진 회원님께 심리적 리프레시를 제안합니다.' 
            },
            { 
              title: '점심 식사 후 가벼운 산책 20분', 
              description: '식후 혈당 조절과 오후 집중력 향상을 위한 건강한 걷기.', 
              category: 'HEALTH', 
              difficulty: 'MEDIUM', 
              reason: '활동 데이터가 저녁에 집중되어 있어 낮 시간대 활동을 추천합니다.' 
            },
            { 
              title: '매일 IT 트렌드 뉴스레터 정독', 
              description: '전문성 강화를 위해 매일 기술 트렌드 하나를 요약합니다.', 
              category: 'STUDY', 
              difficulty: 'NORMAL', 
              reason: '학습 분야의 성취도를 높이기 위한 맞춤형 제안입니다.' 
            }
          ] 
        }) 
      });
    });

    await page.goto('/challenge/create');
    await page.getByRole('button', { name: '내 취향 추천' }).click();
    await expect(page.getByText('AI 추천 챌린지')).toBeVisible({ timeout: 10000 });
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '03-ai-generator.png'), fullPage: true });
  });

  test('04-certification-create: 인증 작성', async ({ page }) => {
    await setupAuth(page);
    await page.route('**/api/challenge/1', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { id: '1', title: '아침 조깅 5km', startDate: '2024-01-01T00:00:00', endDate: '2026-12-31T23:59:59' } }) });
    });
    await page.goto('/certification/create?challengeId=1');
    await page.fill('input[id="title"]', '상쾌한 모닝 조깅 완료!');
    await page.fill('textarea[id="content"]', '오늘도 성공적으로 달렸습니다!');
    await page.setInputFiles('input[type="file"]', path.join(__dirname, 'dummy.png'));
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '04-certification-create.png') });
  });

  test('04-certification-detail: 인증 상세', async ({ page }) => {
    await setupAuth(page);
    await page.route('**/api/certifications/1', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({
          success: true,
          data: { 
            id: 1, title: '1일차 조깅 완료', content: '5km 완주 성공!', authorNickname: '테스트유저', challengeTitle: '아침 조깅 5km', challengeId: '1', createdAt: new Date().toISOString(),
            photoUrl: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAZAAAAGQAQMAAAC6ca99AAAAA1BMVEUAAACnej3aAAAAAXRSTlMAQObYZgAAAC5JREFUGBntwTEBAAAAwiD7p14HB2AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI8Bt9AAAfS7v7sAAAAASUVORK5CYII=', 
            isSuitable: true, analysisResult: '매우 적합한 인증입니다.'
          }
        })
      });
    });
    await page.goto('/certification/1');
    await page.waitForTimeout(3000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '04-certification-detail.png'), fullPage: true });
  });

  test('04-my-certifications: 내 인증 목록', async ({ page }) => {
    await setupAuth(page);
    await page.route('**/api/certifications/user/*', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({
          success: true,
          data: [
            { id: 1, title: '1일차 인증', content: '조깅 성공!', challengeTitle: '아침 조깅 5km', createdAt: '2024-02-01T07:00:00' },
            { id: 2, title: '2일차 인증', content: '어제보다 더 빠르게!', challengeTitle: '아침 조깅 5km', createdAt: '2024-02-02T07:15:00' }
          ],
          pagination: { totalElements: 2, totalPages: 1 }
        })
      });
    });
    await page.goto('/certification/my');
    await expect(page.getByText('나의 인증 목록')).toBeVisible({ timeout: 10000 });
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '04-my-certifications.png'), fullPage: true });
  });

  test('05-feed: 실시간 피드', async ({ page }) => {
    await setupAuth(page);
    await page.route('**/api/feed*', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({
          success: true,
          data: [
            { id: 1, title: '조깅 챌린지 인증', content: '오늘도 달립니다!', authorNickname: '러너킴', challengeTitle: '아침 조깅', createdAt: new Date().toISOString(), likeCount: 5, commentCount: 2 },
            { id: 2, title: '미라클 모닝 10일차', content: '새벽 공기가 좋네요.', authorNickname: '얼리버드', challengeTitle: '미라클 모닝', createdAt: new Date().toISOString(), likeCount: 12, commentCount: 5 }
          ],
          pagination: { totalElements: 2, totalPages: 1 }
        })
      });
    });
    await page.goto('/feed');
    // 제목(h1)을 명확히 타겟팅하여 모호성 해결
    await expect(page.getByRole('heading', { name: '피드', exact: true })).toBeVisible({ timeout: 10000 });
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '05-feed.png'), fullPage: true });
  });

  test('06-ranking: 리더보드', async ({ page }) => {
    await setupAuth(page);
    await page.route('**/api/rankings/me', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { weekly: { rank: 5, score: 7200 }, monthly: { rank: 12, score: 24500 }, alltime: { rank: 15, score: 45000 } } }) });
    });
    await page.route('**/api/rankings?*', async route => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({
          success: true,
          data: {
            rankings: [
              { rank: 1, loginId: 'champion', nickname: '👑 챌린지마스터', score: 12500 },
              { rank: 2, loginId: 'runner', nickname: '열정러너', score: 11200 },
              { rank: 3, loginId: 'coder', nickname: '코딩귀신', score: 9800 }
            ],
            totalParticipants: 156
          }
        })
      });
    });
    await page.goto('/ranking');
    await expect(page.getByText('👑 챌린지마스터')).toBeVisible({ timeout: 10000 });
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '06-ranking.png'), fullPage: true });
  });

  test('07-my-profile: 내 프로필', async ({ page }) => {
    await setupAuth(page);
    await page.route('**/api/follows/*/stats', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { followerCount: 128, followingCount: 56 } }) });
    });
    await page.route('**/api/streaks?*', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: { totalCurrentStreak: 7, totalMaxStreak: 15 } }) });
    });
    await page.route('**/api/badges/testuser', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: [{ name: '첫 걸음', isAcquired: true, acquiredAt: '2024-01-05T10:00:00' }] }) });
    });
    await page.route('**/api/certifications/user/testuser*', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: [{ id: 1, title: '모닝 조깅', createdAt: '2024-02-05T07:30:00', photoUrl: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAZAAAAGQAQMAAAC6ca99AAAAA1BMVEUAAACnej3aAAAAAXRSTlMAQObYZgAAAC5JREFUGBntwTEBAAAAwiD7p14HB2AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI8Bt9AAAfS7v7sAAAAASUVORK5CYII=' }], pagination: { totalElements: 24 } }) });
    });
    await page.goto('/profile');
    await expect(page.getByText('테스트유저')).toBeVisible({ timeout: 15000 });
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '07-my-profile.png'), fullPage: true });
  });

  test('08-stats: 포인트 통계', async ({ page }) => {
    await setupAuth(page);
    await page.route('**/api/points/me/statistics*', async route => {
      // 30일치 데이터 생성
      const statistics = Array.from({ length: 30 }, (_, i) => {
        const date = new Date();
        date.setDate(date.getDate() - (29 - i));
        const dateStr = date.toISOString().split('T')[0];
        const pointsEarned = Math.floor(Math.random() * 150) + 20; // 20~170 사이 랜덤 포인트
        return {
          date: dateStr,
          pointsEarned,
          cumulativePoints: 5000 + (i * 100) // 누적 포인트 증가 시뮬레이션
        };
      });

      await route.fulfill({ 
        status: 200, 
        body: JSON.stringify({ 
          success: true, 
          data: { 
            summary: { totalPointsEarned: 8540, averagePointsPerDay: 115, totalTransactions: 248 }, 
            statistics 
          } 
        }) 
      });
    });
    await page.goto('/stats');
    await expect(page.getByText('포인트 통계')).toBeVisible({ timeout: 10000 });
    // 차트 애니메이션 및 렌더링을 위해 충분히 대기
    await page.waitForTimeout(4000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '08-stats.png'), fullPage: true });
  });

  test('09-notifications: 알림 드롭다운', async ({ page }) => {
    await setupAuth(page);
    await page.goto('/dashboard');
    const bellButton = page.locator('button[title="알림"]');
    await expect(bellButton).toBeVisible({ timeout: 15000 });
    await page.evaluate(() => {
      const es = (window as any).lastEventSource;
      if (es) {
        es.emit('notification', { id: '1', type: 'INFO', message: '러너킴님이 팔로우를 시작했습니다.', createdAt: new Date().toISOString() });
      }
    });
    await page.waitForTimeout(1000);
    await bellButton.click();
    await page.waitForTimeout(1000);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '09-notifications.png') });
  });

  test('10-users: 사용자 찾기', async ({ page }) => {
    await setupAuth(page);
    await page.route('**/api/users/random*', async route => {
      await route.fulfill({ status: 200, body: JSON.stringify({ success: true, data: [{ loginId: 'user1', nickname: '챌린지마스터', totalPoint: 12500, createdAt: '2024-01-01T00:00:00' }] }) });
    });
    await page.goto('/users');
    await expect(page.getByText('사용자 찾기')).toBeVisible({ timeout: 10000 });
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '10-users.png'), fullPage: true });
  });

  test('11-settings: 계정 설정', async ({ page }) => {
    await setupAuth(page);
    await page.goto('/settings');
    // Role 대신 텍스트 자체로 검색하여 안정성 확보
    await expect(page.getByText('계정 설정', { exact: true }).first()).toBeVisible({ timeout: 15000 });
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, '11-settings.png'), fullPage: true });
  });
});
