import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright 설정 파일
 * Mock 테스트 및 E2E 테스트를 위한 통합 설정
 */
export default defineConfig({
  testDir: './tests/e2e',
  /* 파일을 병렬로 실행할지 여부 */
  fullyParallel: true,
  /* 소스 코드 내 .only 사용 금지 (CI 환경용) */
  forbidOnly: !!process.env.CI,
  /* 실패 시 재시도 횟수 */
  retries: process.env.CI ? 2 : 0,
  /* 테스트 실행 시 사용할 워커 수 */
  workers: process.env.CI ? 1 : undefined,
  /* 리포터 설정: HTML 리포트와 Monocart(커버리지) 리포트 사용 */
  reporter: [
    ['list'],
    ['monocart-reporter', {
        coverage: {
            entryFilter: (entry: any) => entry.url.includes('src'),
            sourceFilter: (sourcePath: string) => sourcePath.includes('src/app'),
            lcov: true,
            reports: ['v8', 'console-summary', 'lcov'],
        }
    }]
  ],
  /* 각 테스트의 최대 실행 시간 (10초로 복구) */
  timeout: 5000,
  expect: {
    /* assertion 타임아웃 (5초) */
    timeout: 5000,
  },
  /* 공통 브라우저 옵션 */
  use: {
    /* 테스트 시 접속할 기본 URL (Next.js 서버 주소) */
    baseURL: 'http://localhost:3000',
    /* 실패 시에만 트레이스 수집 */
    trace: 'on-first-retry',
    /* 실패 시에만 비디오 녹화 */
    video: 'on-first-retry',
  },

  /* 브라우저 프로젝트 설정 */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  /* 테스트 실행 전 로컬 서버 구동 설정 */
  webServer: {
    command: 'NEXT_PUBLIC_API_URL=http://localhost:8080 npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: true,
    timeout: 120000,
    stderr: 'pipe',
  },
});
