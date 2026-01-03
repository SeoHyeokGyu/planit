import { BadgeResponse } from "@/types/badge";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export const badgeService = {
  /**
   * 전체 배지 목록 조회 (획득 여부 포함)
   */
  getAllBadges: async (): Promise<BadgeResponse[]> => {
    const response = await api.get<ApiResponse<BadgeResponse[]>>("/api/badges");
    return response.data;
  },

  /**
   * 내가 획득한 배지 목록 조회
   */
  getMyBadges: async (): Promise<BadgeResponse[]> => {
    const response = await api.get<ApiResponse<BadgeResponse[]>>("/api/badges/my");
    return response.data;
  },

  /**
   * 특정 사용자의 배지 목록 조회 (획득 여부 포함)
   */
  getUserBadges: async (loginId: string): Promise<BadgeResponse[]> => {
    const response = await api.get<ApiResponse<BadgeResponse[]>>(`/api/badges/user/${loginId}`);
    return response.data;
  },
};
