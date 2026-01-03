import React from "react";
import { useAllBadges, useMyBadges } from "@/hooks/useBadge";
import BadgeItem from "@/components/badge/BadgeItem";
import { Skeleton } from "@/components/ui/skeleton";
import { AlertCircle, Trophy } from "lucide-react";

interface BadgesSectionProps {
  userLoginId: string;
  isOwnProfile?: boolean;
}

export default function BadgesSection({ userLoginId, isOwnProfile = false }: BadgesSectionProps) {
  // 현재 API 구조상 내 프로필일 때만 모든 배지 정보(획득 여부 포함)를 정확히 가져올 수 있음
  // 남의 프로필일 경우 Backend 지원이 없으므로 일단 렌더링하지 않거나(혹은 내 배지처럼 보일 수 있음)
  // 여기서는 isOwnProfile일 때만 렌더링하도록 처리하거나, 
  // API가 지원된다고 가정하고 일단 useAllBadges를 씁니다.
  // (사실 useAllBadges는 @AuthenticationPrincipal을 쓰므로 *항상* 내 기준 획득 여부를 반환함)
  
  // FIXME: 남의 프로필에서 그 사람의 배지를 보려면 API 수정 필요 (/api/badges/{loginId})
  // 현재는 "내 프로필" 탭에서만 이 컴포넌트를 사용하도록 기획됨.
  
  const { data: badges, isLoading, isError } = useAllBadges();

  if (!isOwnProfile) {
     return (
         <div className="flex flex-col items-center justify-center p-8 text-center bg-gray-50 dark:bg-gray-800/50 rounded-xl border border-dashed border-gray-300 dark:border-gray-700">
             <AlertCircle className="w-8 h-8 text-gray-400 mb-2"/>
             <p className="text-gray-500">
                 다른 사용자의 배지 목록 조회는 아직 지원되지 않습니다.
             </p>
         </div>
     )
  }

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
    return (
      <div className="text-center py-12 text-red-500">
        배지 정보를 불러올 수 없습니다.
      </div>
    );
  }

  if (!badges || badges.length === 0) {
     return (
      <div className="flex flex-col items-center justify-center py-16 text-gray-500 bg-white dark:bg-gray-800 rounded-xl border dark:border-gray-700 shadow-sm">
        <Trophy className="w-12 h-12 mb-4 text-gray-300 dark:text-gray-600" />
        <p className="text-lg font-medium">아직 배지가 없습니다.</p>
        <p className="text-sm">다양한 활동을 통해 배지를 획득해보세요!</p>
      </div>
    );
  }

  // 획득한 배지를 상단으로 정렬
  const sortedBadges = [...badges].sort((a, b) => {
    if (a.isAcquired && !b.isAcquired) return -1;
    if (!a.isAcquired && b.isAcquired) return 1;
    return 0; // 혹은 이름순 정렬 a.name.localeCompare(b.name)
  });
  
  const acquiredCount = badges.filter(b => b.isAcquired).length;
  const totalCount = badges.length;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Trophy className="w-5 h-5 text-yellow-500" />
            <h2 className="text-xl font-bold text-gray-900 dark:text-white">
                획득 현황
            </h2>
          </div>
          <span className="text-sm font-medium px-3 py-1 bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300 rounded-full">
              {acquiredCount} / {totalCount}
          </span>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        {sortedBadges.map((badge) => (
          <BadgeItem key={badge.code} badge={badge} />
        ))}
      </div>
    </div>
  );
}
