"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { followService } from "@/services/followService";
import { FollowStats, FollowUser } from "@/types/follow";

/**
 * 특정 사용자의 팔로워 목록 조회
 */
export const useFollowers = (userLoginId: string, page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: ["followers", userLoginId, page, size],
    queryFn: () => followService.getFollowers(userLoginId, page, size),
    enabled: !!userLoginId,
    select: (data) => data.data,
  });
};

/**
 * 특정 사용자의 팔로잉 목록 조회
 */
export const useFollowings = (userLoginId: string, page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: ["followings", userLoginId, page, size],
    queryFn: () => followService.getFollowings(userLoginId, page, size),
    enabled: !!userLoginId,
    select: (data) => data.data,
  });
};

/**
 * 팔로워/팔로잉 통계 조회 (병렬 처리)
 */
export const useFollowStats = (userLoginId: string) => {
  const followerCountQuery = useQuery({
    queryKey: ["followerCount", userLoginId],
    queryFn: () => followService.getFollowerCount(userLoginId),
    enabled: !!userLoginId,
    select: (data) => data.data,
  });

  const followingCountQuery = useQuery({
    queryKey: ["followingCount", userLoginId],
    queryFn: () => followService.getFollowingCount(userLoginId),
    enabled: !!userLoginId,
    select: (data) => data.data,
  });

  return {
    followerCount: followerCountQuery.data ?? 0,
    followingCount: followingCountQuery.data ?? 0,
    isLoading: followerCountQuery.isLoading || followingCountQuery.isLoading,
    isError: followerCountQuery.isError || followingCountQuery.isError,
  };
};

/**
 * 팔로우 mutation
 */
export const useFollow = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (targetLoginId: string) => followService.follow(targetLoginId),
    onSuccess: (data, targetLoginId) => {
      // 팔로우 관련 캐시 무효화
      queryClient.invalidateQueries({ queryKey: ["followings"] });
      queryClient.invalidateQueries({ queryKey: ["followerCount"] });
      queryClient.invalidateQueries({ queryKey: ["followingCount"] });
      toast.success("팔로우했습니다.");
    },
    onError: (error) => {
      toast.error(error.message || "팔로우 실패");
    },
  });
};

/**
 * 언팔로우 mutation
 */
export const useUnfollow = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (targetLoginId: string) => followService.unfollow(targetLoginId),
    onSuccess: (data, targetLoginId) => {
      // 팔로우 관련 캐시 무효화
      queryClient.invalidateQueries({ queryKey: ["followings"] });
      queryClient.invalidateQueries({ queryKey: ["followerCount"] });
      queryClient.invalidateQueries({ queryKey: ["followingCount"] });
      toast.success("언팔로우했습니다.");
    },
    onError: (error) => {
      toast.error(error.message || "언팔로우 실패");
    },
  });
};
