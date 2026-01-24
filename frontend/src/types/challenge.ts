// 챌린지 생성/수정 요청
export interface ChallengeRequest {
  title: string;
  description: string;
  category: string;
  difficulty: string;
  loginId?: string; // 선택적 필드 (내부에서 추가)
  startDate: string; // ISO 8601 format (LocalDateTime)
  endDate: string; // ISO 8601 format (LocalDateTime)
}

// 챌린지 응답
export interface ChallengeResponse {
  id: string; // "CHL-XXXXXXXX" format
  title: string;
  description: string;
  category: string;
  difficulty: string;
  startDate: string;
  endDate: string;
  createdId: string;
  viewCnt: number;
  participantCnt: number;
  certificationCnt: number;
  isActive: boolean; // 진행 중 여부
  isUpcoming: boolean; // 시작 전 여부
  isEnded: boolean; // 종료 여부
  createdAt?: string;
  updatedAt?: string;
}

// 챌린지 목록 응답
export interface ChallengeListResponse {
  id: string; // "CHL-XXXXXXXX" format
  title: string;
  description: string;
  category: string;
  difficulty: string;
  startDate: string;
  endDate: string;
  viewCnt: number;
  participantCnt: number;
  certificationCnt: number;
  isActive: boolean; // 진행 중 여부
  isUpcoming: boolean; // 시작 전 여부
  isEnded: boolean; // 종료 여부
}

// 챌린지 정렬 타입
export type ChallengeSortType = "LATEST" | "NAME" | "DIFFICULTY" | "POPULAR";

// 챌린지 검색 요청
export interface ChallengeSearchRequest {
  category?: string;
  difficulty?: string; // 'EASY' | 'MEDIUM' | 'HARD'
  page?: number;
  size?: number;
  sortBy?: ChallengeSortType;
}

// 참여자 응답
export interface ParticipateResponse {
  id: string; // Challenge ID
  loginId: string; // User login ID
  status: "ACTIVE" | "COMPLETED" | "WITHDRAWN";
  certificationCnt: number;
  joinedAt: string;
  completedAt?: string;
  withdrawnAt?: string;
}

// 통계 응답
export interface ChallengeStatisticsResponse {
  totalParticipants: number;
  completionRate: number;
  averageProgress: number;
}

// 챌린지 추천 응답
export interface ChallengeRecommendationResponse {
  title: string;
  description: string;
  category: string;
  difficulty: string;
  reason: string; // 추천 사유
}