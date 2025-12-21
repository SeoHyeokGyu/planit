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
  const [isFollowing, setIsFollowing] = useState(initialIsFollowing ?? false);
  const [isLoading, setIsLoading] = useState(false);

  // initialIsFollowing이 제공되지 않으면 팔로잉 상태 조회
  const { data: followings } = useFollowings(
    initialIsFollowing === undefined ? (currentLoginId || "") : "",
    0,
    100
  );

  // 팔로우/언팔로우 mutations
  const followMutation = useFollow();
  const unfollowMutation = useUnfollow();

  // initialIsFollowing이 제공되지 않으면 followings에서 상태 확인
  useEffect(() => {
    if (initialIsFollowing === undefined && followings && Array.isArray(followings)) {
      const isUserFollowing = followings.some(
        (user) => user.loginId === targetLoginId
      );
      setIsFollowing(isUserFollowing);
    } else if (initialIsFollowing !== undefined) {
      setIsFollowing(initialIsFollowing);
    }
  }, [followings, targetLoginId, initialIsFollowing]);

  // 로딩 상태 업데이트
  useEffect(() => {
    setIsLoading(
      followMutation.isPending || unfollowMutation.isPending
    );
  }, [
    followMutation.isPending,
    unfollowMutation.isPending,
  ]);

  // 자신의 프로필이면 버튼 표시 안 함
  if (currentLoginId === targetLoginId) {
    return null;
  }

  const handleClick = async () => {
    if (isFollowing) {
      unfollowMutation.mutate(targetLoginId, {
        onSuccess: () => {
          setIsFollowing(false);
        },
      });
    } else {
      followMutation.mutate(targetLoginId, {
        onSuccess: () => {
          setIsFollowing(true);
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
