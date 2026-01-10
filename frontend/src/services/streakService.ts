import { api } from "@/lib/api";
import {
    StreakResponse,
    StreakSummaryResponse,
    ActivityCalendarResponse,
    StreakStatisticsResponse,
    StreakLeaderboardResponse,
} from "@/types/streak";
import { ApiResponse } from "@/types/api";

/**
 * 스트릭 API 서비스
 */
export const streakService = {
    /**
     * 특정 챌린지의 스트릭 조회
     */
    getStreak: async (challengeId: string, loginId: string): Promise<StreakResponse> => {
        const response = await api.get<ApiResponse<StreakResponse>>(
            `/api/streaks/${challengeId}?loginId=${loginId}`
        );
        return response.data;
    },

    /**
     * 사용자의 모든 스트릭 조회 (요약)
     */
    getAllStreaks: async (loginId: string): Promise<StreakSummaryResponse> => {
        const response = await api.get<ApiResponse<StreakSummaryResponse>>(
            `/api/streaks?loginId=${loginId}`
        );
        return response.data;
    },

    /**
     * 인증 성공 시 스트릭 기록 (내부적으로 CertificationService에서 호출)
     */
    recordCertification: async (
        challengeId: string,
        loginId: string
    ): Promise<StreakResponse> => {
        const response = await api.post<ApiResponse<StreakResponse>>(
            `/api/streaks/${challengeId}/record?loginId=${loginId}`
        );
        return response.data;
    },

    /**
     * 활동 캘린더 조회 (최근 N일)
     */
    getActivityCalendar: async (
        loginId: string,
        days: number = 30
    ): Promise<ActivityCalendarResponse> => {
        const response = await api.get<ApiResponse<ActivityCalendarResponse>>(
            `/api/streaks/calendar?loginId=${loginId}&days=${days}`
        );
        return response.data;
    },

    /**
     * 스트릭 통계 조회 (일별/주별/월별)
     */
    getStreakStatistics: async (
        loginId: string,
        period: "daily" | "weekly" | "monthly" = "daily",
        days: number = 30
    ): Promise<StreakStatisticsResponse> => {
        const response = await api.get<ApiResponse<StreakStatisticsResponse>>(
            `/api/streaks/statistics?loginId=${loginId}&period=${period}&days=${days}`
        );
        return response.data;
    },

    /**
     * 챌린지별 스트릭 리더보드
     */
    getStreakLeaderboard: async (
        challengeId: string,
        limit: number = 10
    ): Promise<StreakLeaderboardResponse> => {
        const response = await api.get<ApiResponse<StreakLeaderboardResponse>>(
            `/api/streaks/${challengeId}/leaderboard?limit=${limit}`
        );
        return response.data;
    },
};