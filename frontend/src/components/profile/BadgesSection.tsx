"use client";

import React from "react";
import { useUserBadges, useCheckBadges } from "@/hooks/useBadge";
import BadgeItem from "@/components/badge/BadgeItem";
import { Skeleton } from "@/components/ui/skeleton";
import { Trophy, RefreshCcw } from "lucide-react";
import { Button } from "@/components/ui/button";

interface BadgesSectionProps {
  userLoginId: string;
  isOwnProfile: boolean;
}

export default function BadgesSection({ userLoginId, isOwnProfile }: BadgesSectionProps) {
  // 특정 사용자의 배지 목록 조회 (획득 여부 포함 - 훅 내부에서 정렬됨)
  const { data: badges, isLoading, isError } = useUserBadges(userLoginId);
  const { mutate: checkBadges, isPending: isChecking } = useCheckBadges();

  if (isLoading) {
    return (
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        {[...Array(5)].map((_, i) => (
          <Skeleton key={i} className="h-32 w-full rounded-xl" />
        ))}
      </div>
    );
  }

  if (isError) {
    return <div className="text-center py-12 text-red-500">배지 정보를 불러올 수 없습니다.</div>;
  }

  if (!badges || badges.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-gray-500 bg-white dark:bg-gray-800 rounded-xl border dark:border-gray-700 shadow-sm">
        <Trophy className="w-12 h-12 mb-4 text-gray-300 dark:text-gray-600" />
        <p className="text-lg font-medium">아직 배지가 없습니다.</p>
        <p className="text-sm">다양한 활동을 통해 배지를 획득해보세요!</p>
        {isOwnProfile && (
          <Button
            variant="outline"
            size="sm"
            className="mt-4 gap-2"
            onClick={() => checkBadges(userLoginId)}
            disabled={isChecking}
          >
            <RefreshCcw className={`w-4 h-4 ${isChecking ? "animate-spin" : ""}`} />
            배지 획득 확인
          </Button>
        )}
      </div>
    );
  }

  const acquiredCount = badges.filter((b) => b.isAcquired).length;
  const totalCount = badges.length;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Trophy className="w-5 h-5 text-yellow-500" />
          <h2 className="text-xl font-bold text-gray-900 dark:text-white">획득 현황</h2>
        </div>
        <div className="flex items-center gap-3">
          {isOwnProfile && (
            <Button
              variant="ghost"
              size="sm"
              className="h-8 px-2 text-xs gap-1.5 text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-gray-100"
              onClick={() => checkBadges(userLoginId)}
              disabled={isChecking}
            >
              <RefreshCcw className={`w-3.5 h-3.5 ${isChecking ? "animate-spin" : ""}`} />
              새로고침
            </Button>
          )}
          <span className="text-sm font-medium px-3 py-1 bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300 rounded-full">
            {acquiredCount} / {totalCount}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        {badges.map((badge) => (
          <BadgeItem key={badge.code} badge={badge} />
        ))}
      </div>
    </div>
  );
}
