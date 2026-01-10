"use client";

import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Flame, TrendingUp, Award, Calendar, Activity } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import { useAllStreaks, useActivityCalendar } from "@/hooks/useStreak";
import type { StreakResponse, DailyActivityResponse, ActivityCalendarResponse } from "@/types/streak";

interface StreaksSectionProps {
  userLoginId: string;
  isOwnProfile?: boolean;
}

export default function StreaksSection({ userLoginId, isOwnProfile = false }: StreaksSectionProps) {
  const [selectedTab, setSelectedTab] = useState<"overview" | "calendar">("overview");

  // 스트릭 요약 조회
  const { data: streakSummary, isLoading: isLoadingSummary } = useAllStreaks(userLoginId);

  // 활동 캘린더 조회 (최근 30일)
  const { data: calendar, isLoading: isLoadingCalendar } = useActivityCalendar(
      userLoginId,
      30
  );

  if (isLoadingSummary) {
    return <StreaksSectionSkeleton />;
  }

  if (!streakSummary) {
    return null;
  }

  return (
      <div className="space-y-6">
        {/* 헤더 */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-r from-orange-500 to-red-500 rounded-lg flex items-center justify-center text-white shadow-lg">
              <Flame className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
                스트릭 현황
              </h2>
              <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">
                연속 달성 기록을 확인하세요
              </p>
            </div>
          </div>

          {/* 탭 선택 */}
          <div className="flex gap-2">
            <button
                onClick={() => setSelectedTab("overview")}
                className={cn(
                    "px-4 py-2 rounded-lg font-medium transition-all",
                    selectedTab === "overview"
                        ? "bg-orange-100 text-orange-700 dark:bg-orange-900 dark:text-orange-200"
                        : "bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-gray-800 dark:text-gray-400"
                )}
            >
              개요
            </button>
            <button
                onClick={() => setSelectedTab("calendar")}
                className={cn(
                    "px-4 py-2 rounded-lg font-medium transition-all",
                    selectedTab === "calendar"
                        ? "bg-orange-100 text-orange-700 dark:bg-orange-900 dark:text-orange-200"
                        : "bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-gray-800 dark:text-gray-400"
                )}
            >
              활동 잔디
            </button>
          </div>
        </div>

        {/* 전체 통계 카드 */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card className="border-2 border-orange-100 dark:border-orange-900">
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600 dark:text-gray-400 flex items-center gap-2">
                <Flame className="w-4 h-4 text-orange-500" />
                현재 스트릭
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-orange-600 dark:text-orange-400">
                {streakSummary.totalCurrentStreak}
                <span className="text-lg text-gray-500 ml-1">일</span>
              </div>
              <p className="text-xs text-gray-500 mt-1">
                {streakSummary.activeStreakCount}개 챌린지 활동 중
              </p>
            </CardContent>
          </Card>

          <Card className="border-2 border-purple-100 dark:border-purple-900">
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600 dark:text-gray-400 flex items-center gap-2">
                <Award className="w-4 h-4 text-purple-500" />
                최장 기록
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-purple-600 dark:text-purple-400">
                {streakSummary.maxLongestStreak}
                <span className="text-lg text-gray-500 ml-1">일</span>
              </div>
              <p className="text-xs text-gray-500 mt-1">역대 최고 연속 달성</p>
            </CardContent>
          </Card>

          <Card className="border-2 border-blue-100 dark:border-blue-900">
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-medium text-gray-600 dark:text-gray-400 flex items-center gap-2">
                <TrendingUp className="w-4 h-4 text-blue-500" />
                활동 일수
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold text-blue-600 dark:text-blue-400">
                {calendar?.activeDays || 0}
                <span className="text-lg text-gray-500 ml-1">일</span>
              </div>
              <p className="text-xs text-gray-500 mt-1">최근 30일 중 활동</p>
            </CardContent>
          </Card>
        </div>

        {/* 탭 컨텐츠 */}
        {selectedTab === "overview" ? (
            <StreakOverview streaks={streakSummary.streaks} />
        ) : (
            <ActivityCalendarView calendar={calendar} isLoading={isLoadingCalendar} />
        )}
      </div>
  );
}

// 챌린지별 스트릭 개요
function StreakOverview({ streaks }: { streaks: StreakResponse[] }) {
  if (streaks.length === 0) {
    return (
        <Card className="border-2 border-dashed border-gray-200">
          <CardContent className="py-12 text-center">
            <Flame className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500 font-medium">활성화된 스트릭이 없습니다</p>
            <p className="text-sm text-gray-400 mt-1">챌린지에 참여하고 인증을 시작하세요!</p>
          </CardContent>
        </Card>
    );
  }

  return (
      <div className="space-y-3">
        {streaks.map((streak) => (
            <Card
                key={streak.challengeId}
                className={cn(
                    "border-2 transition-all hover:shadow-md",
                    streak.isStreakAtRisk
                        ? "border-red-200 bg-red-50/50 dark:border-red-900 dark:bg-red-950/20"
                        : "border-gray-200 dark:border-gray-800"
                )}
            >
              <CardContent className="py-4">
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="font-bold text-gray-900 dark:text-white">
                        {streak.challengeTitle}
                      </h3>
                      {streak.isCertifiedToday && (
                          <Badge className="bg-green-100 text-green-700 border-green-200 dark:bg-green-900 dark:text-green-200">
                            오늘 인증 완료 ✓
                          </Badge>
                      )}
                      {streak.isStreakAtRisk && (
                          <Badge variant="destructive" className="bg-red-100 text-red-700 border-red-200">
                            ⚠️ 오늘 인증 필요
                          </Badge>
                      )}
                    </div>
                    <div className="flex items-center gap-4 text-sm text-gray-600 dark:text-gray-400">
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5" />
                    마지막 인증: {streak.lastCertificationDate || "없음"}
                  </span>
                    </div>
                  </div>

                  <div className="flex items-center gap-6">
                    <div className="text-center">
                      <div className="text-2xl font-bold text-orange-600 dark:text-orange-400 flex items-center gap-1">
                        <Flame className="w-5 h-5" />
                        {streak.currentStreak}
                      </div>
                      <p className="text-xs text-gray-500 mt-1">현재</p>
                    </div>
                    <div className="text-center">
                      <div className="text-2xl font-bold text-purple-600 dark:text-purple-400 flex items-center gap-1">
                        <Award className="w-5 h-5" />
                        {streak.longestStreak}
                      </div>
                      <p className="text-xs text-gray-500 mt-1">최고</p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
        ))}
      </div>
  );
}

// GitHub 스타일 활동 잔디
function ActivityCalendarView({
                                calendar,
                                isLoading,
                              }: {
  calendar?: ActivityCalendarResponse;
  isLoading: boolean;
}) {
  if (isLoading) {
    return <Skeleton className="h-64 w-full" />;
  }

  if (!calendar) {
    return null;
  }

  // 주별로 그룹화
  const weeks: DailyActivityResponse[][] = [];
  let currentWeek: DailyActivityResponse[] = [];

  calendar.activities.forEach((activity, index) => {
    currentWeek.push(activity);
    if (currentWeek.length === 7 || index === calendar.activities.length - 1) {
      weeks.push([...currentWeek]);
      currentWeek = [];
    }
  });

  const getActivityColor = (level: number) => {
    switch (level) {
      case 0:
        return "bg-gray-100 dark:bg-gray-800";
      case 1:
        return "bg-green-200 dark:bg-green-900";
      case 2:
        return "bg-green-400 dark:bg-green-700";
      case 3:
        return "bg-green-600 dark:bg-green-500";
      case 4:
        return "bg-green-800 dark:bg-green-300";
      default:
        return "bg-gray-100 dark:bg-gray-800";
    }
  };

  return (
      <Card className="border-2">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="w-5 h-5 text-green-600" />
            활동 잔디 (최근 30일)
          </CardTitle>
          <CardDescription>
            {calendar.totalCertifications}개 인증 · {calendar.activeDays}일 활동
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            {/* 요일 레이블 */}
            <div className="flex gap-1 mb-2">
              <div className="w-12"></div>
              {["일", "월", "화", "수", "목", "금", "토"].map((day, i) => (
                  <div key={i} className="w-3 text-xs text-gray-500 text-center">
                    {day}
                  </div>
              ))}
            </div>

            {/* 잔디 그리드 */}
            {weeks.map((week, weekIndex) => (
                <div key={weekIndex} className="flex gap-1 items-center">
                  <div className="w-12 text-xs text-gray-500">
                    {weekIndex === 0 && new Date(week[0].date).toLocaleDateString("ko", { month: "short" })}
                  </div>
                  {week.map((activity) => (
                      <div
                          key={activity.date}
                          className={cn(
                              "w-3 h-3 rounded-sm transition-all hover:scale-110 cursor-pointer",
                              getActivityColor(activity.activityLevel)
                          )}
                          title={`${activity.date}: ${activity.certificationCount}개 인증`}
                      />
                  ))}
                </div>
            ))}
          </div>

          {/* 범례 */}
          <div className="flex items-center justify-end gap-2 mt-4 text-xs text-gray-500">
            <span>적음</span>
            {[0, 1, 2, 3, 4].map((level) => (
                <div
                    key={level}
                    className={cn("w-3 h-3 rounded-sm", getActivityColor(level))}
                />
            ))}
            <span>많음</span>
          </div>
        </CardContent>
      </Card>
  );
}

// 스켈레톤
function StreaksSectionSkeleton() {
  return (
      <div className="space-y-6">
        <div className="flex items-center gap-3">
          <Skeleton className="w-10 h-10 rounded-lg" />
          <div className="flex-1">
            <Skeleton className="h-6 w-32 mb-2" />
            <Skeleton className="h-4 w-48" />
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[...Array(3)].map((_, i) => (
              <Card key={i}>
                <CardHeader className="pb-3">
                  <Skeleton className="h-4 w-24" />
                </CardHeader>
                <CardContent>
                  <Skeleton className="h-10 w-20 mb-2" />
                  <Skeleton className="h-3 w-32" />
                </CardContent>
              </Card>
          ))}
        </div>

        <div className="space-y-3">
          {[...Array(3)].map((_, i) => (
              <Card key={i}>
                <CardContent className="py-4">
                  <Skeleton className="h-20 w-full" />
                </CardContent>
              </Card>
          ))}
        </div>
      </div>
  );
}