
/**
 * 개별 챌린지 스트릭 응답
 */
export interface StreakResponse {
    challengeId: string;
    challengeTitle: string;
    loginId: string;
    currentStreak: number;
    longestStreak: number;
    lastCertificationDate: string | null;
    isCertifiedToday: boolean;
    isStreakAtRisk: boolean;
}

/**
 * 사용자 전체 스트릭 요약
 */
export interface StreakSummaryResponse {
    loginId: string;
    totalCurrentStreak: number;
    maxLongestStreak: number;
    activeStreakCount: number;
    streaks: StreakResponse[];
}

/**
 * 일별 활동 데이터
 */
export interface DailyActivityResponse {
    date: string;
    certificationCount: number;
    challengeCount: number;
    activityLevel: number; // 0-4
}

/**
 * 활동 캘린더 응답 (최근 N일)
 */
export interface ActivityCalendarResponse {
    loginId: string;
    startDate: string;
    endDate: string;
    totalDays: number;
    activeDays: number;
    totalCertifications: number;
    activities: DailyActivityResponse[];
}

/**
 * 스트릭 통계 아이템
 */
export interface StatisticItem {
    label: string;
    certificationCount: number;
    challengeCount: number;
    activeDay: boolean;
}

/**
 * 스트릭 통계 응답
 */
export interface StreakStatisticsResponse {
    loginId: string;
    period: "daily" | "weekly" | "monthly";
    statistics: StatisticItem[];
}

/**
 * 스트릭 리더보드 엔트리
 */
export interface LeaderboardEntry {
    rank: number;
    loginId: string;
    userName: string | null;
    currentStreak: number;
    longestStreak: number;
}

/**
 * 스트릭 리더보드 응답
 */
export interface StreakLeaderboardResponse {
    challengeId: string;
    challengeTitle: string;
    leaders: LeaderboardEntry[];
}

/**
 * 스트릭 경고 알림
 */
export interface StreakWarningNotification {
    loginId: string;
    challengeId: string;
    challengeTitle: string;
    currentStreak: number;
    lastCertificationDate: string | null;
    message: string;
}