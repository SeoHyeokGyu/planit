export interface UserProfile {
  id: number;
  loginId: string;
  nickname: string;
  totalPoint: number;
  createdAt: string;
  updatedAt: string;
}

export interface UserUpdateRequest {
  nickname: string;
}

export interface UserPasswordUpdateRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UserDeleteRequest {
  password: string;
}

export interface UserDashboardStats {
  challengeCount: number;
  certificationCount: number;
  followerCount: number;
  followingCount: number;
}
