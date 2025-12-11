import { api } from "@/lib/api";

export interface FeedEvent {
  id: string;
  userId: number;
  userNickname: string;
  challengeId: number;
  challengeName: string;
  certificationId: number;
  certificationImageUrl: string;
  message: string;
  createdAt: string;
  likesCount: number;
  isLiked: boolean;
}

export interface FeedResponse {
  success: boolean;
  data: FeedEvent[];
}

export const feedService = {
  /**
   * 팔로우하는 사용자의 인증 피드 조회
   */
  getFollowingFeed: async (limit: number = 20, offset: number = 0) => {
    const response = await api.get<FeedResponse>(
      `/api/feed/following?limit=${limit}&offset=${offset}`
    );
    return response;
  },

  /**
   * 실시간 피드 SSE 스트림 구독
   */
  subscribeToRealtimeFeed: (onMessage: (event: FeedEvent) => void, onError?: (error: Error) => void) => {
    const token = typeof window !== "undefined"
      ? localStorage.getItem("auth-storage")
        ? JSON.parse(localStorage.getItem("auth-storage") || "{}").state?.token
        : null
      : null;

    const streamUrl = token
      ? `${api.baseURL}/api/feed/stream?token=${token}`
      : `${api.baseURL}/api/feed/stream`;

    const eventSource = new EventSource(streamUrl);

    eventSource.addEventListener("feed-event", (event) => {
      try {
        const data = JSON.parse(event.data) as FeedEvent;
        onMessage(data);
      } catch (error) {
        console.error("Failed to parse feed event:", error);
      }
    });

    eventSource.addEventListener("error", () => {
      eventSource.close();
      onError?.(new Error("SSE connection failed"));
    });

    // Return unsubscribe function
    return () => {
      eventSource.close();
    };
  },

  /**
   * 피드 좋아요 토글
   */
  toggleLike: async (feedId: string) => {
    const response = await api.post(`/api/feed/${feedId}/like`);
    return response;
  },

  /**
   * 피드 삭제
   */
  deleteFeed: async (feedId: string) => {
    const response = await api.delete(`/api/feed/${feedId}`);
    return response;
  },
};
