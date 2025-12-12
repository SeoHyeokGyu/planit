import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { authService } from "@/services/authService";
import { useAuthStore } from "@/stores/authStore";
import { LoginRequest, SignUpRequest } from "@/types/auth";

// --- Mutations ---

/**
 * 로그인 뮤테이션을 위한 커스텀 훅
 */
export const useLogin = () => {
  const router = useRouter();
  const setToken = useAuthStore((state) => state.setToken);
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: LoginRequest) => authService.login(data),
    onSuccess: (response) => {
      if (response.success && response.data.accessToken) {
        const token = response.data.accessToken;
        // JWT 토큰에서 userId 추출 (payload의 sub 또는 userId 필드)
        try {
          const payload = JSON.parse(
            atob(token.split(".")[1])
          );
          const userId = payload.userId || payload.sub;
          setToken(token, userId ? parseInt(userId) : undefined);
        } catch {
          setToken(token);
        }

        queryClient.invalidateQueries({ queryKey: ["userProfile"] });
        router.push("/dashboard");
      }
    },
    onError: (error) => {
      console.error("Login failed:", error);
    },
  });
};

/**
 * 회원가입 뮤테이션을 위한 커스텀 훅
 */
export const useSignUp = () => {
  const router = useRouter();
  return useMutation({
    mutationFn: (data: SignUpRequest) => authService.signUp(data),
    onSuccess: () => {
      router.push("/login?signup=success");
    },
    onError: (error) => {
      console.error("Sign up failed:", error);
    },
  });
};

/**
 * 로그아웃을 위한 커스텀 훅
 */
export const useLogout = () => {
  const router = useRouter();
  const clearToken = useAuthStore((state) => state.clearToken);
  const queryClient = useQueryClient();

  const logout = async () => {
    await authService.logout();
    clearToken();
    queryClient.removeQueries({ queryKey: ["userProfile"] });
    router.push("/login");
  };

  return logout;
};
