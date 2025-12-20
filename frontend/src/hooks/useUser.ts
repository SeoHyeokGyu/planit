"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { userService } from "@/services/userService";
import { useAuthStore } from "@/stores/authStore";
import { UserPasswordUpdateRequest, UserUpdateRequest, UserProfile } from "@/types/user";
import { ApiResponse } from "@/types/api";

// --- Queries ---

/**
 * 사용자 프로필 조회를 위한 커스텀 훅 (자신 또는 다른 사용자)
 * @param loginId - 조회할 사용자 loginId (없으면 자신의 프로필)
 */
export const useUserProfile = (loginId?: string) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return useQuery({
    queryKey: loginId ? ["userProfile", loginId] : ["userProfile"],
    queryFn: () =>
      loginId
        ? userService.getProfileByLoginId(loginId)
        : userService.getProfile(),
    enabled: loginId ? true : isAuthenticated,
    staleTime: 1000 * 60 * 5,
    select: (data) => data.data,
  });
};


// --- Mutations ---

/**
 * 프로필(닉네임) 업데이트 뮤테이션을 위한 커스텀 훅
 */
export const useUpdateProfile = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UserUpdateRequest) => userService.updateProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["userProfile"] });
      toast.success("프로필이 업데이트되었습니다.");
    },
    onError: (error) => {
      toast.error(error.message || "프로필 업데이트 실패");
    },
  });
};

/**
 * 비밀번호 변경 뮤테이션을 위한 커스텀 훅
 */
export const useUpdatePassword = () => {
  return useMutation({
    mutationFn: (data: UserPasswordUpdateRequest) => userService.updatePassword(data),
    onSuccess: () => {
      toast.success("비밀번호가 변경되었습니다.");
    },
    onError: (error) => {
      toast.error(error.message || "비밀번호 변경 실패");
    },
  });
};
