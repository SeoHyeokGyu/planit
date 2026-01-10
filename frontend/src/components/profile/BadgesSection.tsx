"use client";

import React from "react";
import { useUserBadges, useCheckBadges, useBadgeSort, SortOption } from "@/hooks/useBadge";
import BadgeItem from "@/components/badge/BadgeItem";
import { Skeleton } from "@/components/ui/skeleton";
import { Trophy, RefreshCcw, ArrowUpDown } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface BadgesSectionProps {
  userLoginId: string;
  isOwnProfile: boolean;
}

export default function BadgesSection({ userLoginId, isOwnProfile }: BadgesSectionProps) {
  // 특정 사용자의 배지 목록 조회
  const { data: badges, isLoading, isError } = useUserBadges(userLoginId);
  const { mutate: checkBadges, isPending: isChecking } = useCheckBadges();
  
  // 정렬 훅 사용
  const { sortedBadges, sortBy, setSortBy } = useBadgeSort(badges);

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
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <Trophy className="w-5 h-5 text-yellow-500" />
          <h2 className="text-xl font-bold text-gray-900 dark:text-white">획득 현황</h2>
          <span className="text-sm font-medium px-2.5 py-0.5 bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300 rounded-full ml-2">
            {acquiredCount} / {totalCount}
          </span>
        </div>

        <div className="flex items-center gap-2">
          <Select value={sortBy} onValueChange={(value) => setSortBy(value as SortOption)}>
            <SelectTrigger className="w-[120px] h-9 bg-white dark:bg-gray-950 border-slate-200 dark:border-slate-800 hover:border-blue-400 dark:hover:border-blue-500 transition-colors">
              <ArrowUpDown className="w-4 h-4 mr-2 text-blue-500 opacity-70" />
              <SelectValue placeholder="정렬" />
            </SelectTrigger>
            <SelectContent className="bg-white dark:bg-gray-950 border border-slate-200 dark:border-slate-800 shadow-xl z-50">
              <SelectItem value="acquired" className="focus:bg-blue-50 dark:focus:bg-blue-900/20">획득순</SelectItem>
              <SelectItem value="grade" className="focus:bg-blue-50 dark:focus:bg-blue-900/20">등급순</SelectItem>
              <SelectItem value="code" className="focus:bg-blue-50 dark:focus:bg-blue-900/20">종류순</SelectItem>
              <SelectItem value="name" className="focus:bg-blue-50 dark:focus:bg-blue-900/20">이름순</SelectItem>
            </SelectContent>
          </Select>

          {isOwnProfile && (
            <Button
              variant="outline"
              size="sm"
              className="h-9 px-3 gap-1.5 border-blue-200 bg-blue-50/50 text-blue-600 hover:bg-blue-100 hover:text-blue-700 hover:border-blue-300 dark:bg-blue-900/20 dark:border-blue-900 dark:text-blue-400 dark:hover:bg-blue-900/40 transition-all shadow-sm"
              onClick={() => checkBadges(userLoginId)}
              disabled={isChecking}
            >
              <RefreshCcw className={`w-4 h-4 ${isChecking ? "animate-spin" : ""}`} />
              <span className="hidden sm:inline font-medium">배지 확인</span>
            </Button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        {sortedBadges.map((badge) => (
          <BadgeItem key={badge.code} badge={badge} />
        ))}
      </div>
    </div>
  );
}
