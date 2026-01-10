"use client";

import { useQuery } from "@tanstack/react-query";
import { pointService } from "@/services/pointService";
import { useAuthStore } from "@/stores/authStore";
import { StatisticsDateRange } from "@/types/point";

/**
 * 포인트 통계 조회를 위한 커스텀 훅
 * @param dateRange - 조회할 날짜 범위 (startDate, endDate)
 */
export const usePointStatistics = (dateRange: StatisticsDateRange) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return useQuery({
    queryKey: ["pointStatistics", dateRange.startDate, dateRange.endDate],
    queryFn: () => pointService.getPointStatistics(dateRange),
    enabled: isAuthenticated && !!dateRange.startDate && !!dateRange.endDate,
    staleTime: 1000 * 60 * 5, // 5분
    select: (data) => data.data,
  });
};

/**
 * 경험치 통계 조회를 위한 커스텀 훅
 * @param dateRange - 조회할 날짜 범위 (startDate, endDate)
 */
export const useExperienceStatistics = (dateRange: StatisticsDateRange) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return useQuery({
    queryKey: ["experienceStatistics", dateRange.startDate, dateRange.endDate],
    queryFn: () => pointService.getExperienceStatistics(dateRange),
    enabled: isAuthenticated && !!dateRange.startDate && !!dateRange.endDate,
    staleTime: 1000 * 60 * 5, // 5분
    select: (data) => data.data,
  });
};
