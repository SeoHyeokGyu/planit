import { api } from "@/lib/api";
import {
  ChallengeRequest,
  ChallengeResponse,
  ChallengeListResponse,
  ChallengeSearchRequest,
  ParticipateResponse,
  ChallengeStatisticsResponse,
  ChallengeRecommendationResponse,
  ExistingChallengeRecommendationResponse,
} from "@/types/challenge";
import { ApiResponse } from "@/types/api";

export const challengeService = {
  // 추천 챌린지
  getRecommendations: () => api.get<ApiResponse<ChallengeRecommendationResponse[]>>("/api/challenge/recommend"),

  // 기존 챌린지 추천 (참여용)
  getRecommendedExistingChallenges: () =>
    api.get<ApiResponse<ExistingChallengeRecommendationResponse[]>>("/api/challenge/recommend-existing"),

  // 목록 조회
  getChallenges: (params?: ChallengeSearchRequest) => {
    const searchParams = new URLSearchParams();
    if (params?.category) searchParams.append("category", params.category);
    if (params?.difficulty) searchParams.append("difficulty", params.difficulty);
    if (params?.sortBy) searchParams.append("sortBy", params.sortBy);
    if (params?.page) searchParams.append("page", String(params.page));
    if (params?.size) searchParams.append("size", String(params.size));

    const query = searchParams.toString();
    const url = `/api/challenge${query ? `?${query}` : ""}`;

    console.log("챌린지 조회 요청:", { params, url });

    return api.get<ApiResponse<ChallengeListResponse[]>>(url);
  },

  // 검색
  searchChallenges: (keyword: string) =>
      api.get<ApiResponse<ChallengeListResponse[]>>(
          `/api/challenge/search?keyword=${encodeURIComponent(keyword)}`
      ),

  // 내가 참여중인 챌린지
  getMyChallenges: () => api.get<ApiResponse<ChallengeListResponse[]>>("/api/challenge/my"),

  // 상세 조회
  getChallenge: (id: string) => api.get<ApiResponse<ChallengeResponse>>(`/api/challenge/${id}`),

  // 생성
  createChallenge: (data: ChallengeRequest) =>
      api.post<ApiResponse<ChallengeResponse>>("/api/challenge", data),

  // 수정
  updateChallenge: (id: string, data: ChallengeRequest) =>
      api.put<ApiResponse<ChallengeResponse>>(`/api/challenge/${id}`, data),

  // 삭제
  deleteChallenge: (id: string) => api.delete<ApiResponse<void>>(`/api/challenge/${id}`),

  // 참여
  joinChallenge: (id: string) =>
      api.post<ApiResponse<ParticipateResponse>>(`/api/challenge/${id}/join`),

  // 포기
  withdrawChallenge: (id: string) => api.post<ApiResponse<void>>(`/api/challenge/${id}/withdraw`),

  // 조회수 증가
  incrementViewCount: (id: string) => api.post<ApiResponse<void>>(`/api/challenge/${id}/view`),

  // 참여자 목록
  getParticipants: (id: string) =>
      api.get<ApiResponse<ParticipateResponse[]>>(`/api/challenge/${id}/participants`),

  // 통계
  getStatistics: (id: string) =>
      api.get<ApiResponse<ChallengeStatisticsResponse>>(`/api/challenge/${id}/statistics`),
};