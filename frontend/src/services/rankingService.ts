import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";
import {
  RankingListResponse,
  AllMyRankingsResponse,
  RankingParams,
} from "@/types/ranking";

export const rankingService = {
  /**
   * 랭킹 조회 (통합 API)
   * @param params - type, page, size
   */
  async getRanking(
    params: RankingParams
  ): Promise<ApiResponse<RankingListResponse>> {
    const searchParams = new URLSearchParams({
      type: params.type,
      page: String(params.page ?? 0),
      size: String(params.size ?? 20),
    });
    return api.get(`/api/rankings?${searchParams.toString()}`);
  },

  /**
   * 주간 랭킹 조회
   */
  async getWeeklyRanking(
    page = 0,
    size = 20
  ): Promise<ApiResponse<RankingListResponse>> {
    const searchParams = new URLSearchParams({
      page: String(page),
      size: String(size),
    });
    return api.get(`/api/rankings/weekly?${searchParams.toString()}`);
  },

  /**
   * 월간 랭킹 조회
   */
  async getMonthlyRanking(
    page = 0,
    size = 20
  ): Promise<ApiResponse<RankingListResponse>> {
    const searchParams = new URLSearchParams({
      page: String(page),
      size: String(size),
    });
    return api.get(`/api/rankings/monthly?${searchParams.toString()}`);
  },

  /**
   * 전체 랭킹 조회
   */
  async getAlltimeRanking(
    page = 0,
    size = 20
  ): Promise<ApiResponse<RankingListResponse>> {
    const searchParams = new URLSearchParams({
      page: String(page),
      size: String(size),
    });
    return api.get(`/api/rankings/alltime?${searchParams.toString()}`);
  },

  /**
   * 내 랭킹 조회 (모든 기간)
   */
  async getMyRankings(): Promise<ApiResponse<AllMyRankingsResponse>> {
    return api.get("/api/rankings/me");
  },

  /**
   * SSE 스트림 상태 확인
   */
  async getStreamStatus(): Promise<
    ApiResponse<{ connectedClients: number; status: string }>
  > {
    return api.get("/api/rankings/stream/status");
  },
};
