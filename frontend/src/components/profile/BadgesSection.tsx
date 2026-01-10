"use client";

import React from "react";
import { useUserBadges } from "@/hooks/useBadge";
import BadgeItem from "@/components/badge/BadgeItem";
import { Skeleton } from "@/components/ui/skeleton";
import { Trophy } from "lucide-react";

interface BadgesSectionProps {
  userLoginId: string;
  isOwnProfile: boolean;
}

export default function BadgesSection({ userLoginId, isOwnProfile }: BadgesSectionProps) {
  // 특정 사용자의 배지 목록 조회 (획득 여부 포함 - 훅 내부에서 정렬됨)
  const { data: badges, isLoading, isError } = useUserBadges(userLoginId);

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
      <div className="flex flex-col items-center justify-center py-16 text-gray-500 bg-white rounded-xl border border-gray-200 shadow-sm">
        <Trophy className="w-12 h-12 mb-4 text-gray-300" />
        <p className="text-lg font-medium">아직 배지가 없습니다.</p>
        <p className="text-sm">다양한 활동을 통해 배지를 획득해보세요!</p>
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
          <h2 className="text-xl font-bold text-gray-900">획득 현황</h2>
        </div>
        <span className="text-sm font-medium px-3 py-1 bg-blue-100 text-blue-700 rounded-full">
          {acquiredCount} / {totalCount}
        </span>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        {badges.map((badge) => (
          <BadgeItem key={badge.code} badge={badge} />
        ))}
      </div>
    </div>
  );
}
