// frontend/src/types/certification.ts

export interface CertificationCreateRequest {
  challengeId: string;
  title: string;
  content: string;
}

export interface CertificationUpdateRequest {
  title: string;
  content: string;
}

export interface CertificationResponse {
  id: number;
  title: string;
  content: string;
  photoUrl: string | null;
  isSuitable: boolean | null; // AI 분석 적합 여부
  analysisResult?: string | null; // AI 분석 결과 (사유)
  authorNickname: string;
  senderNickname?: string; // 작성자 닉네임 (피드용)
  senderLoginId?: string; // 작성자 로그인 ID (피드용)
  challengeId: string;
  challengeTitle: string;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}
