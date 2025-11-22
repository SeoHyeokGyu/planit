import { api } from "@/lib/api";
import {
  SignUpRequest,
  LoginRequest,
  LoginResponse,
  ApiResponse,
} from "@/types/auth";

export const authService = {
  async signUp(data: SignUpRequest): Promise<ApiResponse<void>> {
    return api.post("/api/auth/signup", data);
  },

  async login(data: LoginRequest): Promise<LoginResponse> {
    return api.post("/api/auth/login", data);
  },

  async logout(): Promise<void> {
    // 로컬 스토리지에서 토큰 제거는 useAuthStore의 clearToken에서 처리됩니다.
    // 백엔드에 로그아웃 요청이 필요한 경우 여기에 추가합니다.
  },
};
