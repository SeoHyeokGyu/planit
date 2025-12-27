"use client";

import { useFollow, useFollowings, useUnfollow } from "@/hooks/useFollow";
import { Button } from "@/components/ui/button";
import { UserPlus, UserMinus, Loader2 } from "lucide-react";
import { useAuthStore } from "@/stores/authStore";
import { useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";

interface FollowButtonProps {
  targetLoginId: string;
  variant?: "default" | "outline";
  size?: "default" | "sm" | "lg";
  initialIsFollowing?: boolean;
}

/**
 * 팔로우/언팔로우 버튼 컴포넌트
 * - 자신의 프로필에서는 표시하지 않음
 * - 팔로우 상태에 따라 버튼 텍스트/스타일 변경
 */
export default function FollowButton({
  targetLoginId,
  variant = "default",
  size = "default",
  initialIsFollowing,
}: FollowButtonProps) {
  const currentLoginId = useAuthStore((state) => state.loginId);
  const queryClient = useQueryClient();
  const [manualIsFollowing, setManualIsFollowing] = useState<boolean | null>(
    null
  );

  // initialIsFollowing이 제공되지 않으면 팔로잉 상태 조회
  const { data: followings } = useFollowings(
    initialIsFollowing === undefined ? currentLoginId || "" : "",
    0,
    100
  );

  // 팔로우/언팔로우 mutations
  const followMutation = useFollow();
  const unfollowMutation = useUnfollow();

  const isLoading = followMutation.isPending || unfollowMutation.isPending;

  // 팔로우 여부 결정
  const isFollowing =
    manualIsFollowing !== null
      ? manualIsFollowing
      : initialIsFollowing !== undefined
      ? initialIsFollowing
      : followings && Array.isArray(followings)
      ? followings.some((user) => user.loginId === targetLoginId)
      : false;

  // 자신의 프로필이면 버튼 표시 안 함
  if (currentLoginId === targetLoginId) {
    return null;
  }

  const handleClick = async () => {
    if (isFollowing) {
      unfollowMutation.mutate(targetLoginId, {
        onSuccess: () => {
          setManualIsFollowing(false);
        },
      });
    } else {
      followMutation.mutate(targetLoginId, {
        onSuccess: () => {
          setManualIsFollowing(true);
        },
      });
    }
  };

  return (
    <Button
      onClick={handleClick}
      disabled={isLoading}
      size={size}
      className={`min-w-[110px] font-semibold transition-all duration-200 cursor-pointer ${
        isFollowing
          ? "bg-gray-300 text-gray-600 border border-gray-400 hover:bg-gray-400 hover:text-gray-700"
          : "bg-gradient-to-r from-blue-600 to-purple-600 text-white hover:from-blue-700 hover:to-purple-700 shadow-md hover:shadow-lg"
      }`}
    >
      {isLoading ? (
        <>
          <Loader2 className="w-4 h-4 mr-2 animate-spin" />
          처리 중...
        </>
      ) : isFollowing ? (
        <>
          <UserMinus className="w-4 h-4 mr-2" />
          언팔로우
        </>
      ) : (
        <>
          <UserPlus className="w-4 h-4 mr-2" />
          팔로우
        </>
      )}
    </Button>
  );
}
