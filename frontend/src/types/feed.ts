export interface FeedResponse {
  id: number;
  title: string;
  content: string;
  photoUrl: string | null;
  authorNickname: string;
  authorLoginId: string;
  challengeId: string;
  challengeTitle: string;
  createdAt: string;
  updatedAt: string;
}
