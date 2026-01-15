"use client";

import { useQuery, useQueryClient } from "@tanstack/react-query";
import { rankingService } from "@/services/rankingService";
import { useAuthStore } from "@/stores/authStore";
import { useRankingStore } from "@/stores/rankingStore";
import { RankingTabType, RankingPeriodType } from "@/types/ranking";

/**
 * 랭킹 조회를 위한 커스텀 훅 (통합 API)
 * @param type - 랭킹 타입 (weekly, monthly, all)
 * @param page - 페이지 번호
 * @param size - 페이지 크기
 */
export const useRanking = (
  type: "weekly" | "monthly" | "all",
  page = 0,
  size = 20
) => {
  return useQuery({
    queryKey: ["ranking", type, page, size],
    queryFn: () => rankingService.getRanking({ type, page, size }),
    staleTime: 1000 * 30, // 30초
    select: (data) => data.data,
  });
};

/**
 * 주간 랭킹 조회 훅
 */
export const useWeeklyRanking = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ["ranking", "weekly", page, size],
    queryFn: () => rankingService.getWeeklyRanking(page, size),
    staleTime: 1000 * 30,
    select: (data) => data.data,
  });
};

/**
 * 월간 랭킹 조회 훅
 */
export const useMonthlyRanking = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ["ranking", "monthly", page, size],
    queryFn: () => rankingService.getMonthlyRanking(page, size),
    staleTime: 1000 * 30,
    select: (data) => data.data,
  });
};

/**
 * 전체 랭킹 조회 훅
 */
export const useAlltimeRanking = (page = 0, size = 20) => {
  return useQuery({
    queryKey: ["ranking", "alltime", page, size],
    queryFn: () => rankingService.getAlltimeRanking(page, size),
    staleTime: 1000 * 30,
    select: (data) => data.data,
  });
};

/**
 * 내 랭킹 조회 훅
 */
export const useMyRankings = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return useQuery({
    queryKey: ["ranking", "me"],
    queryFn: () => rankingService.getMyRankings(),
    enabled: isAuthenticated,
    staleTime: 1000 * 30,
    select: (data) => data.data,
  });
};

/**
 * 실시간 Top 10 조회 훅 (Store에서 가져옴)
 */
export const useRealtimeTop10 = (periodType: RankingPeriodType) => {
  const weeklyTop10 = useRankingStore((state) => state.weeklyTop10);
  const monthlyTop10 = useRankingStore((state) => state.monthlyTop10);
  const alltimeTop10 = useRankingStore((state) => state.alltimeTop10);
  const isConnected = useRankingStore((state) => state.isConnected);

  const top10 = {
    WEEKLY: weeklyTop10,
    MONTHLY: monthlyTop10,
    ALLTIME: alltimeTop10,
  }[periodType];

  return { top10, isConnected };
};

/**
 * 랭킹 쿼리 무효화 훅
 */
export const useInvalidateRanking = () => {
  const queryClient = useQueryClient();

  const invalidateAll = () => {
    queryClient.invalidateQueries({ queryKey: ["ranking"] });
  };

  const invalidateByType = (type: RankingTabType) => {
    queryClient.invalidateQueries({ queryKey: ["ranking", type] });
  };

  return { invalidateAll, invalidateByType };
};

/**
 * 탭 타입을 기간 타입으로 변환
 */
export const tabToPeriodType = (tab: RankingTabType): RankingPeriodType => {
  const map: Record<RankingTabType, RankingPeriodType> = {
    weekly: "WEEKLY",
    monthly: "MONTHLY",
    alltime: "ALLTIME",
  };
  return map[tab];
};

/**
 * 기간 타입을 탭 타입으로 변환
 */
export const periodTypeToTab = (periodType: RankingPeriodType): RankingTabType => {
  const map: Record<RankingPeriodType, RankingTabType> = {
    WEEKLY: "weekly",
    MONTHLY: "monthly",
    ALLTIME: "alltime",
  };
  return map[periodType];
};
