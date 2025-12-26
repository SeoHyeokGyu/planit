import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

export interface CommentResponse {
  id: number;
  content: string;
  authorNickname: string;
  authorLoginId: string;
  createdAt: string;
  isMyComment: boolean;
}

export const likeCommentService = {
  toggleLike: async (certificationId: number): Promise<ApiResponse<boolean>> => {
    return api.post(`/api/certifications/${certificationId}/likes`);
  },

  getComments: async (certificationId: number): Promise<ApiResponse<CommentResponse[]>> => {
    return api.get(`/api/certifications/${certificationId}/comments`);
  },

  createComment: async (
    certificationId: number,
    content: string
  ): Promise<ApiResponse<CommentResponse>> => {
    return api.post(`/api/certifications/${certificationId}/comments`, { content });
  },

  deleteComment: async (commentId: number): Promise<ApiResponse<void>> => {
    return api.delete(`/api/comments/${commentId}`);
  },
};
