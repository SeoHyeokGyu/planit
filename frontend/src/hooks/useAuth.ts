import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
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
          const payload = JSON.parse(atob(token.split(".")[1]));

          // JWT 페이로드에서 정보 추출 (프로젝트의 토큰 구조에 맞게 조정)
          const userId = payload.userId;
          const loginId = payload.sub; // 보통 sub에 loginId(username)가 들어있음

          setToken(token, userId ? parseInt(userId) : undefined, loginId);
        } catch (e) {
          console.warn("Failed to parse token payload:", e);
          setToken(token);
        }

        queryClient.invalidateQueries({ queryKey: ["userProfile"] });
        toast.success("로그인되었습니다.");
        router.push("/dashboard");
      }
    },
    onError: (error) => {
      console.error("Login failed:", error);
      toast.error(error.message || "로그인에 실패했습니다.");
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
      toast.success("회원가입이 완료되었습니다.");
      router.push("/login?signup=success");
    },
    onError: (error) => {
      console.error("Sign up failed:", error);
      toast.error(error.message || "회원가입에 실패했습니다.");
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
    try {
      await authService.logout();
      clearToken();
      queryClient.removeQueries({ queryKey: ["userProfile"] });
      toast.success("로그아웃되었습니다.");
      router.push("/login");
    } catch (error: any) {
      console.error("Logout failed:", error);
      toast.error(error.message || "로그아웃에 실패했습니다.");
    }
  };

  return logout;
};
