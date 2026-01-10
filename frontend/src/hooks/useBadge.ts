"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { badgeService } from "@/services/badgeService";
import { BadgeResponse, BadgeGrade } from "@/types/badge";
import { toast } from "sonner";
import { useMemo, useState } from "react";

export function useAllBadges() {
  return useQuery<BadgeResponse[]>({
    queryKey: ["badges", "all"],
    queryFn: badgeService.getAllBadges,
  });
}

export function useMyBadges() {
  return useQuery<BadgeResponse[]>({
    queryKey: ["badges", "my"],
    queryFn: badgeService.getMyBadges,
  });
}

export function useUserBadges(loginId: string) {
  return useQuery<BadgeResponse[]>({
    queryKey: ["badges", "user", loginId],
    queryFn: () => badgeService.getUserBadges(loginId),
    enabled: !!loginId,
    // 기본 정렬은 제거하고 컴포넌트 레벨(useBadgeSort)에서 처리하도록 변경
    // select: (badges) => ...
  });
}

export function useCheckBadges() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (loginId: string) => badgeService.checkAllBadges(loginId),
    onSuccess: (newBadgesCount, loginId) => {
      if (newBadgesCount > 0) {
        toast.success(`${newBadgesCount}개의 새로운 배지를 획득했습니다!`);
      } else {
        toast.info("새로 획득한 배지가 없습니다.");
      }
      // 해당 사용자의 배지 목록만 갱신
      queryClient.invalidateQueries({ queryKey: ["badges", "user", loginId] });
      queryClient.invalidateQueries({ queryKey: ["badges", "my"] });
    },
    onError: () => {
      toast.error("배지 확인 중 오류가 발생했습니다.");
    },
  });
}

// --- Sorting Logic ---

export type SortOption = "acquired" | "grade" | "name" | "code";

const GRADE_WEIGHT: Record<BadgeGrade, number> = {
  [BadgeGrade.PLATINUM]: 4,
  [BadgeGrade.GOLD]: 3,
  [BadgeGrade.SILVER]: 2,
  [BadgeGrade.BRONZE]: 1,
};

export function useBadgeSort(badges: BadgeResponse[] | undefined) {
  const [sortBy, setSortBy] = useState<SortOption>("acquired");

  const sortedBadges = useMemo(() => {
    if (!badges) return [];
    const list = [...badges];

    return list.sort((a, b) => {
      switch (sortBy) {
        case "grade":
          // 1. 등급 높은 순
          if (GRADE_WEIGHT[a.grade] !== GRADE_WEIGHT[b.grade]) {
            return GRADE_WEIGHT[b.grade] - GRADE_WEIGHT[a.grade];
          }
          // 2. 획득 여부
          if (a.isAcquired !== b.isAcquired) return a.isAcquired ? -1 : 1;
          return 0;

        case "name":
          return a.name.localeCompare(b.name);

        case "code":
          // 코드순 (종류별 유사 효과)
          return a.code.localeCompare(b.code);

        case "acquired":
        default:
          // 1. 획득 여부 (획득한 것 먼저)
          if (a.isAcquired !== b.isAcquired) return a.isAcquired ? -1 : 1;
          // 2. 획득 날짜 (최신순)
          if (a.isAcquired && b.isAcquired && a.acquiredAt && b.acquiredAt) {
            return new Date(b.acquiredAt).getTime() - new Date(a.acquiredAt).getTime();
          }
          // 3. 미획득인 경우 진행률 높은 순
          if (!a.isAcquired && !b.isAcquired) {
            const aProgress =
              a.requiredValue > 0 ? a.currentValue / a.requiredValue : 0;
            const bProgress =
              b.requiredValue > 0 ? b.currentValue / b.requiredValue : 0;
            return bProgress - aProgress;
          }
          return 0;
      }
    });
  }, [badges, sortBy]);

  return { sortedBadges, sortBy, setSortBy };
}
