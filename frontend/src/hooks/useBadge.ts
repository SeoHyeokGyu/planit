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
