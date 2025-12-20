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
  authorNickname: string;
  challengeId: string;
  challengeTitle: string;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}
