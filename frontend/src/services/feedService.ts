// frontend/src/services/feedService.ts
import { api } from "@/lib/api";
import { ApiResponse, Page } from "@/types/api";
import { CertificationResponse } from "@/types/certification";

export const feedService = {
  // 팔로우하는 사람들의 최근 인증 피드 조회
  getFeed: async (
    page: number = 0,
    size: number = 10
  ): Promise<ApiResponse<Page<CertificationResponse>>> => {
    return api.get(`/api/feed?page=${page}&size=${size}`);
  },

  // 피드 새로고침 (SSE 활용 시)
  subscribeToFeed: async (): Promise<EventSource> => {
    return new Promise((resolve, reject) => {
      try {
        const eventSource = new EventSource("/api/subscribe");
        resolve(eventSource);
      } catch (error) {
        reject(error);
      }
    });
  },
};
