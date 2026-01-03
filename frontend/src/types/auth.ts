export interface SignUpRequest {
  loginId: string;
  password: string;
  nickname: string;
}

export interface LoginRequest {
  loginId: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  data: {
    type: string;
    accessToken: string;
    loginId: string;
  };
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
}
