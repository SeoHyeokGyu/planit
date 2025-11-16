import { api } from "@/lib/api";
import {
  SignUpRequest,
  LoginRequest,
  LoginResponse,
  UserProfile,
  ApiResponse,
} from "@/types/auth";

export const authService = {
  async signUp(data: SignUpRequest): Promise<ApiResponse<void>> {
    return api.post("/api/auth/signup", data);
  },

  async login(data: LoginRequest): Promise<LoginResponse> {
    return api.post("/api/auth/login", data);
  },

  async getProfile(): Promise<ApiResponse<UserProfile>> {
    return api.get("/api/users/me");
  },

  async logout(): Promise<void> {
    // 로컬 스토리지에서 토큰 제거
    if (typeof window !== "undefined") {
      localStorage.removeItem("access_token");
    }
  },
};
