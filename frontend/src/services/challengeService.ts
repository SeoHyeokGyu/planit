import { api } from "@/lib/api";
import {
    ChallengeRequest,
    ChallengeResponse,
    ChallengeListResponse,
    ChallengeSearchRequest,
    ParticipateResponse,
    ChallengeStatisticsResponse,
} from "@/types/challenge";
import { ApiResponse } from "@/types/api";

export const challengeService = {
    // 목록 조회
    getChallenges: (params?: ChallengeSearchRequest) => {
        const searchParams = new URLSearchParams();
        if (params?.category) searchParams.append("category", params.category);
        if (params?.status) searchParams.append("status", params.status);
        if (params?.page) searchParams.append("page", String(params.page));
        if (params?.size) searchParams.append("size", String(params.size));

        const query = searchParams.toString();
        return api.get<ApiResponse<ChallengeListResponse[]>>(
            `/api/v1/challenges${query ? `?${query}` : ""}`
        );
    },

    // 검색
    searchChallenges: (keyword: string) =>
        api.get<ApiResponse<ChallengeListResponse[]>>(
            `/api/v1/challenges/search?keyword=${encodeURIComponent(keyword)}`
        ),

    // 상세 조회
    getChallenge: (id: number) =>
        api.get<ApiResponse<ChallengeResponse>>(`/api/v1/challenges/${id}`),

    // 생성
    createChallenge: (data: ChallengeRequest) =>
        api.post<ApiResponse<ChallengeResponse>>("/api/v1/challenges", data),

    // 수정
    updateChallenge: (id: number, data: ChallengeRequest) =>
        api.put<ApiResponse<ChallengeResponse>>(`/api/v1/challenges/${id}`, data),

    // 삭제
    deleteChallenge: (id: number) =>
        api.delete<ApiResponse<void>>(`/api/v1/challenges/${id}`),

    // 참여
    joinChallenge: (id: number) =>
        api.post<ApiResponse<ParticipateResponse>>(`/api/v1/challenges/${id}/join`),

    // 탈퇴
    withdrawChallenge: (id: number) =>
        api.post<ApiResponse<void>>(`/api/v1/challenges/${id}/withdraw`),

    // 조회수 증가
    incrementViewCount: (id: number) =>
        api.post<ApiResponse<void>>(`/api/v1/challenges/${id}/view`),

    // 참여자 목록
    getParticipants: (id: number) =>
        api.get<ApiResponse<ParticipateResponse[]>>(`/api/v1/challenges/${id}/participants`),

    // 통계
    getStatistics: (id: number) =>
        api.get<ApiResponse<ChallengeStatisticsResponse>>(`/api/v1/challenges/${id}/statistics`),
};