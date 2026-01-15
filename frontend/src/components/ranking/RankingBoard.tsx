"use client";

import { useState } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { useAuthStore } from "@/stores/authStore";
import { useRankingStore } from "@/stores/rankingStore";
import {
  useRanking,
  useMyRankings,
  tabToPeriodType,
} from "@/hooks/useRanking";
import {
  RankingEntry,
  RankingTabType,
  RankingPeriodType,
} from "@/types/ranking";
import { cn } from "@/lib/utils";

// 왕관 아이콘 컴포넌트
const CrownIcon = ({ className }: { className?: string }) => (
  <svg
    className={className}
    viewBox="0 0 24 24"
    fill="currentColor"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" />
  </svg>
);

// 순위 변동 아이콘
const RankChangeIndicator = ({
  previousRank,
  currentRank,
}: {
  previousRank: number | null;
  currentRank: number;
}) => {
  if (!previousRank) {
    return (
      <Badge variant="outline" className="text-xs bg-blue-50 text-blue-600 border-blue-200">
        NEW
      </Badge>
    );
  }

  const diff = previousRank - currentRank;

  if (diff > 0) {
    return (
      <span className="flex items-center text-green-600 text-sm font-medium">
        <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M5.293 9.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L11 7.414V15a1 1 0 11-2 0V7.414L6.707 9.707a1 1 0 01-1.414 0z" clipRule="evenodd" />
        </svg>
        {diff}
      </span>
    );
  } else if (diff < 0) {
    return (
      <span className="flex items-center text-red-600 text-sm font-medium">
        <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M14.707 10.293a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 111.414-1.414L9 12.586V5a1 1 0 012 0v7.586l2.293-2.293a1 1 0 011.414 0z" clipRule="evenodd" />
        </svg>
        {Math.abs(diff)}
      </span>
    );
  }

  return <span className="text-gray-400 text-sm">-</span>;
};

// Top 3 Podium 컴포넌트
const Top3Podium = ({
  rankings,
  currentLoginId,
}: {
  rankings: RankingEntry[];
  currentLoginId: string | null;
}) => {
  const top3 = rankings.slice(0, 3);
  const [first, second, third] = [top3[0], top3[1], top3[2]];

  const podiumOrder = [second, first, third]; // 2위, 1위, 3위 순서로 표시

  const getPodiumStyle = (rank: number) => {
    switch (rank) {
      case 1:
        return {
          height: "h-32",
          bgColor: "bg-gradient-to-b from-yellow-400 to-yellow-500",
          crownColor: "text-yellow-400",
          size: "w-20 h-20",
        };
      case 2:
        return {
          height: "h-24",
          bgColor: "bg-gradient-to-b from-gray-300 to-gray-400",
          crownColor: "text-gray-400",
          size: "w-16 h-16",
        };
      case 3:
        return {
          height: "h-20",
          bgColor: "bg-gradient-to-b from-amber-600 to-amber-700",
          crownColor: "text-amber-600",
          size: "w-14 h-14",
        };
      default:
        return {
          height: "h-16",
          bgColor: "bg-gray-200",
          crownColor: "text-gray-400",
          size: "w-12 h-12",
        };
    }
  };

  if (top3.length === 0) return null;

  return (
    <div className="flex justify-center items-end gap-4 mb-8 pt-8">
      {podiumOrder.map((entry, index) => {
        if (!entry) return <div key={index} className="w-24" />;

        const style = getPodiumStyle(entry.rank);
        const isCurrentUser = entry.loginId === currentLoginId;

        return (
          <div
            key={entry.loginId}
            className={cn(
              "flex flex-col items-center",
              entry.rank === 1 ? "order-2" : entry.rank === 2 ? "order-1" : "order-3"
            )}
          >
            {/* 왕관 (1위만) */}
            {entry.rank === 1 && (
              <CrownIcon className={cn("w-8 h-8 mb-1", style.crownColor)} />
            )}

            {/* 프로필 아바타 */}
            <div
              className={cn(
                "rounded-full flex items-center justify-center font-bold text-white mb-2",
                style.size,
                isCurrentUser
                  ? "ring-4 ring-blue-500 ring-offset-2"
                  : "",
                entry.rank === 1
                  ? "bg-yellow-500 text-2xl"
                  : entry.rank === 2
                  ? "bg-gray-500 text-xl"
                  : "bg-amber-700 text-lg"
              )}
            >
              {entry.nickname?.charAt(0).toUpperCase() || entry.loginId.charAt(0).toUpperCase()}
            </div>

            {/* 닉네임 */}
            <span
              className={cn(
                "font-semibold text-sm truncate max-w-20 text-center",
                isCurrentUser && "text-blue-600"
              )}
            >
              {entry.nickname || entry.loginId}
            </span>

            {/* 점수 */}
            <span className="text-xs text-gray-500 mb-2">
              {entry.score.toLocaleString()}점
            </span>

            {/* 단상 */}
            <div
              className={cn(
                "w-24 rounded-t-lg flex items-center justify-center text-white font-bold text-xl",
                style.height,
                style.bgColor
              )}
            >
              {entry.rank}
            </div>
          </div>
        );
      })}
    </div>
  );
};

// 랭킹 리스트 아이템
const RankingListItem = ({
  entry,
  isCurrentUser,
}: {
  entry: RankingEntry;
  isCurrentUser: boolean;
}) => {
  const getRankBadgeStyle = (rank: number) => {
    if (rank === 1) return "bg-yellow-100 text-yellow-800 border-yellow-300";
    if (rank === 2) return "bg-gray-100 text-gray-800 border-gray-300";
    if (rank === 3) return "bg-amber-100 text-amber-800 border-amber-300";
    return "bg-white text-gray-600 border-gray-200";
  };

  return (
    <div
      className={cn(
        "flex items-center gap-4 p-4 rounded-lg transition-colors",
        isCurrentUser
          ? "bg-blue-50 border-2 border-blue-200"
          : "bg-white border border-gray-100 hover:bg-gray-50"
      )}
    >
      {/* 순위 */}
      <div
        className={cn(
          "w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm border-2",
          getRankBadgeStyle(entry.rank)
        )}
      >
        {entry.rank}
      </div>

      {/* 사용자 정보 */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <span
            className={cn(
              "font-medium truncate",
              isCurrentUser && "text-blue-600"
            )}
          >
            {entry.nickname || entry.loginId}
          </span>
          {isCurrentUser && (
            <Badge variant="secondary" className="text-xs">
              나
            </Badge>
          )}
        </div>
        <span className="text-sm text-gray-500">@{entry.loginId}</span>
      </div>

      {/* 점수 */}
      <div className="text-right">
        <div className="font-semibold text-lg">
          {entry.score.toLocaleString()}
        </div>
        <div className="text-xs text-gray-500">점</div>
      </div>
    </div>
  );
};

// 내 랭킹 요약 카드
const MyRankingSummary = () => {
  const { data: myRankings, isLoading } = useMyRankings();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  if (!isAuthenticated) return null;

  if (isLoading) {
    return (
      <Card className="mb-6">
        <CardContent className="pt-6">
          <Skeleton className="h-20 w-full" />
        </CardContent>
      </Card>
    );
  }

  if (!myRankings) return null;

  const periods = [
    { label: "주간", data: myRankings.weekly },
    { label: "월간", data: myRankings.monthly },
    { label: "전체", data: myRankings.alltime },
  ];

  return (
    <Card className="mb-6 bg-gradient-to-r from-blue-50 to-indigo-50 border-blue-100">
      <CardHeader className="pb-2">
        <CardTitle className="text-lg">내 랭킹</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-3 gap-4">
          {periods.map(({ label, data }) => (
            <div key={label} className="text-center">
              <div className="text-sm text-gray-500 mb-1">{label}</div>
              <div className="text-2xl font-bold text-blue-600">
                {data.rank ? `${data.rank}위` : "-"}
              </div>
              <div className="text-xs text-gray-400">
                {data.score.toLocaleString()}점
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};

// 로딩 스켈레톤
const RankingSkeleton = () => (
  <div className="space-y-3">
    {[...Array(10)].map((_, i) => (
      <Skeleton key={i} className="h-16 w-full rounded-lg" />
    ))}
  </div>
);

// 탭 콘텐츠
const TabContent = ({ type }: { type: "weekly" | "monthly" | "all" }) => {
  const [page] = useState(0);
  const { data, isLoading, error } = useRanking(type, page, 50);
  const currentLoginId = useAuthStore((state) => state.loginId);

  if (isLoading) return <RankingSkeleton />;

  if (error) {
    return (
      <div className="text-center py-8 text-gray-500">
        랭킹을 불러오는 중 오류가 발생했습니다.
      </div>
    );
  }

  if (!data || data.rankings.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        아직 랭킹 데이터가 없습니다.
      </div>
    );
  }

  return (
    <div>
      {/* Top 3 Podium */}
      <Top3Podium rankings={data.rankings} currentLoginId={currentLoginId} />

      {/* 나머지 랭킹 (4위부터) */}
      <div className="space-y-2">
        {data.rankings.slice(3).map((entry) => (
          <RankingListItem
            key={entry.loginId}
            entry={entry}
            isCurrentUser={entry.loginId === currentLoginId}
          />
        ))}
      </div>

      {/* 페이지 정보 */}
      <div className="text-center text-sm text-gray-400 mt-4">
        총 {data.totalParticipants.toLocaleString()}명 참여
      </div>
    </div>
  );
};

// 메인 RankingBoard 컴포넌트
export default function RankingBoard() {
  const [activeTab, setActiveTab] = useState<RankingTabType>("weekly");
  const isConnected = useRankingStore((state) => state.isConnected);

  return (
    <div className="space-y-4">
      {/* 내 랭킹 요약 */}
      <MyRankingSummary />

      {/* 메인 랭킹 보드 */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <span>랭킹</span>
              {isConnected && (
                <Badge variant="outline" className="text-xs bg-green-50 text-green-600 border-green-200">
                  <span className="w-2 h-2 bg-green-500 rounded-full mr-1 animate-pulse" />
                  Live
                </Badge>
              )}
            </CardTitle>
          </div>
        </CardHeader>
        <CardContent>
          <Tabs
            value={activeTab}
            onValueChange={(v) => setActiveTab(v as RankingTabType)}
          >
            <TabsList className="grid w-full grid-cols-3 mb-6">
              <TabsTrigger value="weekly">주간</TabsTrigger>
              <TabsTrigger value="monthly">월간</TabsTrigger>
              <TabsTrigger value="alltime">전체</TabsTrigger>
            </TabsList>

            <TabsContent value="weekly">
              <TabContent type="weekly" />
            </TabsContent>

            <TabsContent value="monthly">
              <TabContent type="monthly" />
            </TabsContent>

            <TabsContent value="alltime">
              <TabContent type="all" />
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}
