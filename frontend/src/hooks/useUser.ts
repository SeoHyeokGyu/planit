"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { userService } from "@/services/userService";
import { useAuthStore } from "@/stores/authStore";
import { UserPasswordUpdateRequest, UserUpdateRequest } from "@/types/auth";

// --- Queries ---

/**
 * 사용자 프로필 조회를 위한 커스텀 훅
 */
export const useUserProfile = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return useQuery({
    queryKey: ["userProfile"],
    queryFn: userService.getProfile,
    enabled: isAuthenticated,
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
    },
  });
};

/**
 * 비밀번호 변경 뮤테이션을 위한 커스텀 훅
 */
export const useUpdatePassword = () => {
  return useMutation({
    mutationFn: (data: UserPasswordUpdateRequest) => userService.updatePassword(data),
  });
};
