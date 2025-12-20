"use client";

import { useFollow, useFollowings, useUnfollow } from "@/hooks/useFollow";
import { Button } from "@/components/ui/button";
import { UserPlus, UserMinus, Loader2 } from "lucide-react";
import { useAuthStore } from "@/stores/authStore";
import { useEffect, useState } from "react";

interface FollowButtonProps {
  targetLoginId: string;
  variant?: "default" | "outline";
  size?: "default" | "sm" | "lg";
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
}: FollowButtonProps) {
  const currentLoginId = useAuthStore((state) => state.loginId);
  const [isFollowing, setIsFollowing] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // 현재 사용자의 팔로잉 목록 조회
  const { data: followings, isLoading: isCheckingFollow } = useFollowings(
    currentLoginId || "",
    0,
    100 // 임시로 100개씩 조회 (프로덕션에서는 페이지네이션 고려)
  );

  // 팔로우/언팔로우 mutations
  const followMutation = useFollow();
  const unfollowMutation = useUnfollow();

  // 팔로잉 목록에 포함되어 있는지 확인
  useEffect(() => {
    if (followings) {
      const isUserFollowing = followings.some(
        (user) => user.loginId === targetLoginId
      );
      setIsFollowing(isUserFollowing);
    }
  }, [followings, targetLoginId]);

  // 로딩 상태 업데이트
  useEffect(() => {
    setIsLoading(
      isCheckingFollow || followMutation.isPending || unfollowMutation.isPending
    );
  }, [
    isCheckingFollow,
    followMutation.isPending,
    unfollowMutation.isPending,
  ]);

  // 자신의 프로필이면 버튼 표시 안 함
  if (currentLoginId === targetLoginId) {
    return null;
  }

  const handleClick = async () => {
    if (isFollowing) {
      unfollowMutation.mutate(targetLoginId);
    } else {
      followMutation.mutate(targetLoginId);
    }
  };

  return (
    <Button
      variant={isFollowing ? "outline" : variant}
      onClick={handleClick}
      disabled={isLoading}
      size={size}
      className="min-w-[100px]"
    >
      {isLoading ? (
        <>
          <Loader2 className="w-4 h-4 mr-2 animate-spin" />
          처리 중...
        </>
      ) : isFollowing ? (
        <>
          <UserMinus className="w-4 h-4 mr-2" />
          팔로잉
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
