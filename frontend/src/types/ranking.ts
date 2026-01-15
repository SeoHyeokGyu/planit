/**
 * 랭킹 기간 유형
 */
export type RankingPeriodType = "WEEKLY" | "MONTHLY" | "ALLTIME";

/**
 * 랭킹 엔트리 (개별 사용자 랭킹 정보)
 */
export interface RankingEntry {
  rank: number;
  userId: number | null;
  loginId: string;
  nickname: string | null;
  score: number;
}

/**
 * 랭킹 목록 응답
 */
export interface RankingListResponse {
  periodType: RankingPeriodType;
  periodKey: string;
  rankings: RankingEntry[];
  totalParticipants: number;
  page: number;
  size: number;
  totalPages: number;
  isFirst: boolean;
  isLast: boolean;
}

/**
 * 내 랭킹 응답 (단일 기간)
 */
export interface MyRankingResponse {
  periodType: RankingPeriodType;
  periodKey: string;
  rank: number | null;
  score: number;
  totalParticipants: number;
}

/**
 * 모든 기간의 내 랭킹 응답
 */
export interface AllMyRankingsResponse {
  weekly: MyRankingResponse;
  monthly: MyRankingResponse;
  alltime: MyRankingResponse;
}

/**
 * 랭킹 조회 파라미터
 */
export interface RankingParams {
  type: "weekly" | "monthly" | "all";
  page?: number;
  size?: number;
}

// ==================== SSE 관련 타입 ====================

/**
 * 업데이트된 사용자 정보 (SSE 이벤트용)
 */
export interface UpdatedUserInfo {
  userId: number | null;
  loginId: string;
  nickname: string | null;
  previousRank: number | null;
  currentRank: number;
  scoreDelta: number;
  newScore: number;
}

/**
 * 랭킹 업데이트 SSE 이벤트
 */
export interface RankingUpdateEvent {
  eventType: "RANKING_UPDATE" | "INITIAL_RANKING";
  periodType: RankingPeriodType;
  periodKey: string;
  top10: RankingEntry[];
  updatedUser: UpdatedUserInfo | null;
  timestamp: string;
}

/**
 * SSE 연결 상태
 */
export interface SseConnectionStatus {
  connectedClients: number;
  status: string;
}

/**
 * 랭킹 탭 타입 (UI용)
 */
export type RankingTabType = "weekly" | "monthly" | "alltime";

/**
 * 랭킹 변화 방향
 */
export type RankChangeDirection = "up" | "down" | "same" | "new";
