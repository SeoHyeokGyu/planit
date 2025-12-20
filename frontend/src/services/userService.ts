import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { UserProfile, UserPasswordUpdateRequest, UserUpdateRequest, UserDashboardStats } from "@/types/user";

export const userService = {
  async getProfile(): Promise<ApiResponse<UserProfile>> {
    return api.get("/api/users/me");
  },

  async getDashboardStats(): Promise<ApiResponse<UserDashboardStats>> {
    return api.get("/api/users/me/stats");
  },

  async updateProfile(data: UserUpdateRequest): Promise<ApiResponse<UserProfile>> {
    return api.put("/api/users/me/update-profile", data);
  },

  async updatePassword(data: UserPasswordUpdateRequest): Promise<ApiResponse<void>> {
    return api.patch("/api/users/me/password", data);
  },
};