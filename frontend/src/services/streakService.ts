import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";
import type {
  StreakResponse,
  StreakSummaryResponse,
  ActivityCalendarResponse,
  StreakStatisticsResponse,
  StreakLeaderboardResponse,
} from "@/types/streak";

export const streakService = {
  /**
   * 특정 챌린지의 스트릭 조회
   */
  async getStreak(
      challengeId: string,
      loginId: string
  ): Promise<ApiResponse<StreakResponse>> {
    const searchParams = new URLSearchParams({
      loginId,
    });
    return api.get(`/api/streaks/${challengeId}?${searchParams.toString()}`);
  },

  /**
   * 사용자의 모든 스트릭 조회
   */
  async getAllStreaks(loginId: string): Promise<ApiResponse<StreakSummaryResponse>> {
    const searchParams = new URLSearchParams({
      loginId,
    });
    return api.get(`/api/streaks?${searchParams.toString()}`);
  },

  /**
   * 활동 캘린더 조회 (연도별)
   */
  async getActivityCalendar(
      loginId: string,
      year?: number
  ): Promise<ApiResponse<ActivityCalendarResponse>> {
    const searchParams = new URLSearchParams({
      loginId,
    });
    if (year) {
      searchParams.append("year", String(year));
    }
    return api.get(`/api/streaks/calendar?${searchParams.toString()}`);
  },

  /**
   * 스트릭 통계 조회
   */
  async getStreakStatistics(
      loginId: string,
      period: "daily" | "weekly" | "monthly" = "daily",
      days: number = 30
  ): Promise<ApiResponse<StreakStatisticsResponse>> {
    const searchParams = new URLSearchParams({
      loginId,
      period,
      days: String(days),
    });
    return api.get(`/api/streaks/statistics?${searchParams.toString()}`);
  },

  /**
   * 챌린지별 스트릭 리더보드
   */
  async getStreakLeaderboard(
      challengeId: string,
      limit: number = 10
  ): Promise<ApiResponse<StreakLeaderboardResponse>> {
    const searchParams = new URLSearchParams({
      limit: String(limit),
    });
    return api.get(`/api/streaks/${challengeId}/leaderboard?${searchParams.toString()}`);
  },

  /**
   * 인증 기록
   */
  async recordCertification(
      challengeId: string,
      loginId: string
  ): Promise<ApiResponse<StreakResponse>> {
    const searchParams = new URLSearchParams({
      loginId,
    });
    return api.post(`/api/streaks/${challengeId}/record?${searchParams.toString()}`);
  },
};