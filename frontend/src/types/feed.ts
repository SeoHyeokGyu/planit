export type FeedSortType = "LATEST" | "LIKES" | "COMMENTS" | "POPULAR";

export interface FeedResponse {
  id: number;
  title: string;
  content: string;
  photoUrl: string | null;
  authorNickname: string;
  authorLoginId: string;
  challengeId: string;
  challengeTitle: string;
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
  isMine: boolean;
  createdAt: string;
  updatedAt: string;
}
