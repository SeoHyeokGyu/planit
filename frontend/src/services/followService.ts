import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { FollowResponse, FollowUser, FollowStats } from "@/types/follow";

/**
 * 팔로우 관련 API 서비스
 */
export const followService = {
  /**
   * 특정 사용자를 팔로우
   */
  follow: (targetLoginId: string) =>
    api.post<ApiResponse<FollowResponse>>(`/api/follows/${targetLoginId}`),

  /**
   * 특정 사용자 언팔로우
   */
  unfollow: (targetLoginId: string) =>
    api.delete<ApiResponse<void>>(`/api/follows/${targetLoginId}`),

  /**
   * 특정 사용자의 팔로워 목록 조회
   */
  getFollowers: (userLoginId: string, page: number = 0, size: number = 10) =>
    api.get<ApiResponse<FollowUser[]>>(
      `/api/follows/${userLoginId}/followers?page=${page}&size=${size}`
    ),

  /**
   * 특정 사용자의 팔로잉 목록 조회
   */
  getFollowings: (userLoginId: string, page: number = 0, size: number = 10) =>
    api.get<ApiResponse<FollowUser[]>>(
      `/api/follows/${userLoginId}/followings?page=${page}&size=${size}`
    ),

  /**
   * 특정 사용자의 팔로워 수 조회
   */
  getFollowerCount: (userLoginId: string) =>
    api.get<ApiResponse<number>>(`/api/follows/${userLoginId}/follower-count`),

  /**
   * 특정 사용자의 팔로잉 수 조회
   */
  getFollowingCount: (userLoginId: string) =>
    api.get<ApiResponse<number>>(`/api/follows/${userLoginId}/following-count`),
};
