import { BadgeResponse } from "@/types/badge";
import { api } from "@/lib/api";

export const badgeService = {
  /**
   * 전체 배지 목록 조회 (획득 여부 포함)
   * @param userLoginId (Optional) 특정 사용자의 획득 여부를 확인할 때 사용 (Backend API가 지원하는 경우)
   *                    현재 Backend API: /api/badges (AuthenticationPrincipal 사용)
   *                    따라서 타인의 획득 여부는 /api/badges 엔드포인트로는 불가능하고
   *                    본인 조회용으로만 사용되거나, Backend가 수정되어야 함.
   *                    하지만 /api/badges는 "내" 기준 획득 여부를 반환함.
   *                    Backend BadgeController 참고: getAllBadges(@AuthenticationPrincipal userDetails)
   *
   *                    타인의 배지 목록을 보려면 /api/badges?userId=... 같은게 필요하지만
   *                    현재는 /api/badges (내 기준) 와 /api/badges/my (내 획득 목록) 만 있음.
   *
   *                    FIXME: 타인의 배지 목록을 조회하는 API가 현재 없음.
   *                    일단 내 배지 조회용으로 구현.
   */
  getAllBadges: async (): Promise<BadgeResponse[]> => {
    const { data } = await api.get<BadgeResponse[]>("/badges");
    return data;
  },

  /**
   * 내가 획득한 배지 목록 조회
   */
  getMyBadges: async (): Promise<BadgeResponse[]> => {
    const { data } = await api.get<BadgeResponse[]>("/badges/my");
    return data;
  },
};
