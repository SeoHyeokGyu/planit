// src/hooks/useStreak.ts

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { streakService } from "@/services/streakService";
import {
    StreakResponse,
    StreakSummaryResponse,
    ActivityCalendarResponse,
    StreakStatisticsResponse,
    StreakLeaderboardResponse,
} from "@/types/streak";
import { toast } from "sonner";

/**
 * 특정 챌린지의 스트릭 조회
 */
export function useStreak(challengeId: string, loginId: string) {
    return useQuery<StreakResponse>({
        queryKey: ["streak", challengeId, loginId],
        queryFn: () => streakService.getStreak(challengeId, loginId),
        staleTime: 1000 * 60 * 5, // 5분
    });
}

/**
 * 사용자의 모든 스트릭 조회
 */
export function useAllStreaks(loginId: string) {
    return useQuery<StreakSummaryResponse>({
        queryKey: ["streaks", "summary", loginId],
        queryFn: () => streakService.getAllStreaks(loginId),
        staleTime: 1000 * 60 * 5, // 5분
    });
}

/**
 * 활동 캘린더 조회 (최근 N일)
 */
export function useActivityCalendar(loginId: string, days: number = 30) {
    return useQuery<ActivityCalendarResponse>({
        queryKey: ["streaks", "calendar", loginId, days],
        queryFn: () => streakService.getActivityCalendar(loginId, days),
        staleTime: 1000 * 60 * 5, // 5분
    });
}

/**
 * 스트릭 통계 조회
 */
export function useStreakStatistics(
    loginId: string,
    period: "daily" | "weekly" | "monthly" = "daily",
    days: number = 30
) {
    return useQuery<StreakStatisticsResponse>({
        queryKey: ["streaks", "statistics", loginId, period, days],
        queryFn: () => streakService.getStreakStatistics(loginId, period, days),
        staleTime: 1000 * 60 * 5, // 5분
    });
}

/**
 * 챌린지별 스트릭 리더보드
 */
export function useStreakLeaderboard(challengeId: string, limit: number = 10) {
    return useQuery<StreakLeaderboardResponse>({
        queryKey: ["streaks", "leaderboard", challengeId, limit],
        queryFn: () => streakService.getStreakLeaderboard(challengeId, limit),
        staleTime: 1000 * 60 * 5, // 5분
    });
}

/**
 * 인증 기록 뮤테이션 (일반적으로 CertificationService에서 자동 호출)
 */
export function useRecordCertification() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({
                         challengeId,
                         loginId,
                     }: {
            challengeId: string;
            loginId: string;
        }) => streakService.recordCertification(challengeId, loginId),
        onSuccess: (data, variables) => {
            // 스트릭 관련 쿼리 무효화
            queryClient.invalidateQueries({
                queryKey: ["streak", variables.challengeId, variables.loginId],
            });
            queryClient.invalidateQueries({
                queryKey: ["streaks", "summary", variables.loginId],
            });
            queryClient.invalidateQueries({
                queryKey: ["streaks", "calendar", variables.loginId],
            });

            // 성공 토스트
            if (data.currentStreak > 1) {
                toast.success(
                    `🔥 ${data.currentStreak}일 연속 달성!`,
                    {
                        description: `${data.challengeTitle}`,
                    }
                );
            } else {
                toast.success("✨ 스트릭 시작!", {
                    description: `${data.challengeTitle}`,
                });
            }
        },
        onError: (error: any) => {
            console.error("스트릭 기록 실패:", error);
            toast.error("스트릭 기록에 실패했습니다", {
                description: error.response?.data?.message || "다시 시도해주세요",
            });
        },
    });
}

/**
 * 여러 스트릭 데이터를 한번에 프리페치
 */
export function usePrefetchStreaks(loginId: string) {
    const queryClient = useQueryClient();

    return {
        prefetchAll: async () => {
            await Promise.all([
                queryClient.prefetchQuery({
                    queryKey: ["streaks", "summary", loginId],
                    queryFn: () => streakService.getAllStreaks(loginId),
                }),
                queryClient.prefetchQuery({
                    queryKey: ["streaks", "calendar", loginId, 30],
                    queryFn: () => streakService.getActivityCalendar(loginId, 30),
                }),
            ]);
        },
    };
}