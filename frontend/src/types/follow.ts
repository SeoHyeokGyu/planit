/**
 * 팔로우 관련 타입 정의
 */

/**
 * 팔로우 사용자 정보
 */
export interface FollowUser {
  id: number;
  loginId: string;
  nickname: string;
}

/**
 * 팔로워/팔로잉 통계
 */
export interface FollowStats {
  followerCount: number;
  followingCount: number;
}

/**
 * 팔로우 응답 DTO
 */
export interface FollowResponse {
  followerLoginId: string;
  followingLoginId: string;
  createdAt: string;
}
