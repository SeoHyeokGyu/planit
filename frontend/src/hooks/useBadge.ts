"use client";

import { useQuery } from "@tanstack/react-query";
import { badgeService } from "@/services/badgeService";
import { BadgeResponse } from "@/types/badge";

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
    select: (badges) =>
      [...badges].sort((a, b) => {
        // 1차: 획득 여부 정렬
        if (a.isAcquired && !b.isAcquired) return -1;
        if (!a.isAcquired && b.isAcquired) return 1;
        // 2차: 이름순 정렬
        return a.name.localeCompare(b.name);
      }),
  });
}
