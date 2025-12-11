import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { feedService, FeedEvent } from "@/services/feedService";

/**
 * 팔로우하는 사용자의 인증 피드 조회
 */
export const useFollowingFeed = (limit: number = 20, offset: number = 0) => {
  return useQuery({
    queryKey: ["followingFeed", limit, offset],
    queryFn: () => feedService.getFollowingFeed(limit, offset),
  });
};

/**
 * 실시간 피드 SSE 구독
 */
export const useRealtimeFeed = (enabled: boolean = true) => {
  const queryClient = useQueryClient();

  const { data: feeds = [], isLoading } = useQuery({
    queryKey: ["realtimeFeed"],
    queryFn: async () => {
      return new Promise<FeedEvent[]>((resolve) => {
        const feedList: FeedEvent[] = [];

        // 초기 상태 반환
        resolve(feedList);

        // SSE 구독
        const unsubscribe = feedService.subscribeToRealtimeFeed(
          (event) => {
            // 새로운 피드 이벤트 받으면 캐시 업데이트
            queryClient.setQueryData(["realtimeFeed"], (oldData: FeedEvent[] = []) => [
              event,
              ...oldData,
            ]);
          },
          (error) => {
            console.error("Realtime feed error:", error);
          }
        );

        // Cleanup
        return () => unsubscribe();
      });
    },
    enabled,
    staleTime: Infinity, // SSE는 항상 최신 상태 유지
  });

  return { feeds, isLoading };
};

/**
 * 피드 좋아요 토글
 */
export const useLikeFeed = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (feedId: string) => feedService.toggleLike(feedId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["followingFeed"] });
      queryClient.invalidateQueries({ queryKey: ["realtimeFeed"] });
    },
  });
};

/**
 * 피드 삭제
 */
export const useDeleteFeed = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (feedId: string) => feedService.deleteFeed(feedId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["followingFeed"] });
      queryClient.invalidateQueries({ queryKey: ["realtimeFeed"] });
    },
  });
};
