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

  /**
   * 모든 배지 획득 조건 재검사 요청
   * @param loginId 대상 사용자 로그인 ID (현재는 본인만 가능)
   * @returns 새로 획득한 배지 개수
   */
  checkAllBadges: async (loginId: string): Promise<number> => {
    // 현재 백엔드는 토큰 기반으로 본인만 처리하지만, 인터페이스 통일성을 위해 인자를 받음
    const response = await api.post<ApiResponse<number>>("/api/badges/check-all");
    return response.data;
  },
};
