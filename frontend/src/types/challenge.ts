// 챌린지 생성/수정 요청
export interface ChallengeRequest {
    title: string;
    description: string;
    category?: string;
    startDate: string;
    endDate: string;
    maxParticipants?: number;
}

// 챌린지 응답
export interface ChallengeResponse {
    id: number;
    title: string;
    description: string;
    category?: string;
    startDate: string;
    endDate: string;
    maxParticipants?: number;
    currentParticipants: number;
    viewCount: number;
    creatorId: number;
    creatorNickname: string;
    createdAt: string;
    updatedAt: string;
}

// 챌린지 목록 응답
export interface ChallengeListResponse {
    id: number;
    title: string;
    category?: string;
    startDate: string;
    endDate: string;
    currentParticipants: number;
    maxParticipants?: number;
    viewCount: number;
    creatorNickname: string;
}

// 챌린지 검색 요청
export interface ChallengeSearchRequest {
    category?: string;
    status?: 'UPCOMING' | 'ONGOING' | 'ENDED';
    page?: number;
    size?: number;
}

// 참여자 응답
export interface ParticipateResponse {
    id: number;
    challengeId: number;
    userId: number;
    nickname: string;
    joinedAt: string;
}

// 통계 응답
export interface ChallengeStatisticsResponse {
    totalParticipants: number;
    completionRate: number;
    averageProgress: number;
}