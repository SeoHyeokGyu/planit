import { api } from "@/lib/api";
import { ApiResponse, UserProfile, UserPasswordUpdateRequest, UserUpdateRequest } from "@/types/auth";

export const userService = {
  async getProfile(): Promise<ApiResponse<UserProfile>> {
    return api.get("/api/users/me");
  },

  async updateProfile(data: UserUpdateRequest): Promise<ApiResponse<UserProfile>> {
    return api.put("/api/users/me/update-profile", data);
  },

  async updatePassword(data: UserPasswordUpdateRequest): Promise<ApiResponse<void>> {
    return api.patch("/api/users/me/password", data);
  },
};