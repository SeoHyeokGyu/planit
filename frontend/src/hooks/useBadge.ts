"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { badgeService } from "@/services/badgeService";
import { BadgeResponse } from "@/types/badge";
import { toast } from "sonner";

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
