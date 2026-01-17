"use client";

import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { Flame, TrendingUp, Award, Calendar, Activity, ChevronLeft, ChevronRight } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import { useAllStreaks, useActivityCalendar } from "@/hooks/useStreak";
import type {
  StreakResponse,
  DailyActivityResponse,
  ActivityCalendarResponse,
} from "@/types/streak";
import { pageHeaderStyles, iconGradients } from "@/styles/pageHeader";
import { EmptyState } from "@/components/ui/empty-state";

interface StreaksSectionProps {
  userLoginId: string;
  isOwnProfile?: boolean;
}

export default function StreaksSection({ userLoginId, isOwnProfile = false }: StreaksSectionProps) {
  const [selectedTab, setSelectedTab] = useState<"overview" | "calendar">("overview");
  const [selectedYear, setSelectedYear] = useState<number>(new Date().getFullYear());

  // 스트릭 요약 조회
  const {
    data: streakSummary,
    isLoading: isLoadingSummary,
    error: summaryError,
  } = useAllStreaks(userLoginId);

  // 활동 캘린더 조회 (연도별)
  const {
    data: calendar,
    isLoading: isLoadingCalendar,
    error: calendarError,
  } = useActivityCalendar(userLoginId, selectedYear);

  if (isLoadingSummary) {
    return <StreaksSectionSkeleton />;
  }

  if (!streakSummary) {
    return (
        <Card className="shadow-lg rounded-xl bg-white">
          <CardHeader>
            <CardTitle className="text-gray-900">스트릭</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-red-500">스트릭 데이터를 불러올 수 없습니다.</p>
            {summaryError && (
                <p className="text-sm text-gray-500 mt-2">
                  {summaryError instanceof Error ? summaryError.message : "Unknown error"}
                </p>
            )}
          </CardContent>
        </Card>
    );
  }

  return (
      <Card className="shadow-lg rounded-xl bg-white">
        <CardHeader className="flex flex-col sm:flex-row items-start sm:items-center sm:justify-between pb-4">
          <div className="flex items-center space-x-3 mb-4 sm:mb-0">
            <div className={`${pageHeaderStyles.iconBase} ${iconGradients.streak}`}>
              <Flame className="w-6 h-6" />
            </div>
            <div>
              <CardTitle className="text-2xl font-bold text-gray-900">스트릭 현황</CardTitle>
              <CardDescription className="text-gray-600">연속 달성 기록을 확인하세요</CardDescription>
            </div>
          </div>

          {/* 탭 선택 */}
          <div className="flex gap-2">
            <Button
                variant="outline"
                size="sm"
                onClick={() => setSelectedTab("overview")}
                className={cn(
                    pageHeaderStyles.tabButton.base,
                    selectedTab === "overview"
                        ? pageHeaderStyles.tabButton.active
                        : pageHeaderStyles.tabButton.inactive
                )}
            >
              개요
            </Button>
            <Button
                variant="outline"
                size="sm"
                onClick={() => setSelectedTab("calendar")}
                className={cn(
                    pageHeaderStyles.tabButton.base,
                    selectedTab === "calendar"
                        ? pageHeaderStyles.tabButton.active
                        : pageHeaderStyles.tabButton.inactive
                )}
            >
              활동 잔디
            </Button>
          </div>
        </CardHeader>

        <CardContent className="space-y-6">
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
                <p className="text-xs text-gray-500 mt-1">{selectedYear}년 활동</p>
              </CardContent>
            </Card>
          </div>

          {/* 탭 컨텐츠 */}
          {selectedTab === "overview" ? (
              <StreakOverview streaks={streakSummary.streaks} />
          ) : (
              <ActivityCalendarView
                  calendar={calendar}
                  isLoading={isLoadingCalendar}
                  selectedYear={selectedYear}
                  onYearChange={setSelectedYear}
              />
          )}
        </CardContent>
      </Card>
  );
}

// 챌린지별 스트릭 개요
function StreakOverview({ streaks }: { streaks: StreakResponse[] }) {
  if (streaks.length === 0) {
    return (
        <EmptyState
            icon={Flame}
            title="활성화된 스트릭이 없습니다"
            description="챌린지에 참여하고 인증을 시작하세요!"
        />
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
                        ? "border-red-200 bg-red-50/50 dark:border-red-900 dark:bg-red-950/20 hover:border-red-300"
                        : "border-gray-200 dark:border-gray-800 hover:border-blue-300"
                )}
            >
              <CardContent className="py-4">
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="font-bold text-gray-800 dark:text-white text-base">
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

// GitHub 스타일 활동 잔디 (1년치 - 주 단위 세로 배치)
function ActivityCalendarView({
                                calendar,
                                isLoading,
                                selectedYear,
                                onYearChange,
                              }: {
  calendar?: ActivityCalendarResponse;
  isLoading: boolean;
  selectedYear: number;
  onYearChange: (year: number) => void;
}) {
  if (isLoading) {
    return <Skeleton className="h-64 w-full" />;
  }

  if (!calendar) {
    return null;
  }

  const currentYear = new Date().getFullYear();
  const canGoNext = selectedYear < currentYear;
  const canGoPrev = selectedYear > currentYear - 10; // 최대 10년 전까지

  // 활동 데이터를 Map으로 변환
  const activityMap = new Map<string, DailyActivityResponse>();
  calendar.activities.forEach((activity) => {
    activityMap.set(activity.date, activity);
  });

  // 52주 전체 생성 (항상 52주)
  const startDate = new Date(selectedYear, 0, 1); // 1월 1일
  const firstDayOfWeek = startDate.getDay(); // 첫 날의 요일

  // 주별로 그룹화 (일요일 시작, 항상 52주)
  const weeks: (DailyActivityResponse | null)[][] = [];

  for (let weekIndex = 0; weekIndex < 52; weekIndex++) {
    const week: (DailyActivityResponse | null)[] = [];

    for (let dayIndex = 0; dayIndex < 7; dayIndex++) {
      // 첫 주의 경우 시작 요일 이전은 null
      if (weekIndex === 0 && dayIndex < firstDayOfWeek) {
        week.push(null);
        continue;
      }

      // 날짜 계산
      const dayOffset = weekIndex * 7 + dayIndex - firstDayOfWeek;
      const currentDate = new Date(selectedYear, 0, 1);
      currentDate.setDate(currentDate.getDate() + dayOffset);

      // 해당 연도를 벗어나면 null
      if (currentDate.getFullYear() !== selectedYear) {
        week.push(null);
        continue;
      }

      const dateStr = currentDate.toISOString().split('T')[0];
      const activity = activityMap.get(dateStr) || {
        date: dateStr,
        certificationCount: 0,
        challengeCount: 0,
        activityLevel: 0,
      };

      week.push(activity);
    }

    weeks.push(week);
  }

  const getActivityColor = (level: number) => {
    switch (level) {
      case 0:
        return "bg-gray-100 dark:bg-gray-800 hover:bg-gray-200";
      case 1:
        return "bg-green-200 dark:bg-green-900 hover:bg-green-300";
      case 2:
        return "bg-green-400 dark:bg-green-700 hover:bg-green-500";
      case 3:
        return "bg-green-600 dark:bg-green-500 hover:bg-green-700";
      case 4:
        return "bg-green-800 dark:bg-green-300 hover:bg-green-900";
      default:
        return "bg-gray-100 dark:bg-gray-800";
    }
  };

  // 월 레이블 표시 위치 계산
  const getMonthLabels = () => {
    const labels: { month: string; weekIndex: number }[] = [];
    const monthNames = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];

    // 1월부터 12월까지 각 월의 시작 주 찾기
    for (let month = 0; month < 12; month++) {
      const monthStart = new Date(selectedYear, month, 1);

      // 해당 월의 1일이 속한 주 찾기
      const daysSinceYearStart = Math.floor((monthStart.getTime() - new Date(selectedYear, 0, 1).getTime()) / (1000 * 60 * 60 * 24));
      const weekIndex = Math.floor((daysSinceYearStart + firstDayOfWeek) / 7);

      // 첫 월(1월)이거나 이전 월과 다른 주인 경우에만 추가
      if (month === 0 || weekIndex > (labels[labels.length - 1]?.weekIndex || -1)) {
        labels.push({
          month: monthNames[month],
          weekIndex,
        });
      }
    }

    return labels;
  };

  const monthLabels = getMonthLabels();

  // 월별 구분선 위치 (weekIndex 배열)
  const monthDividers = monthLabels.map(label => label.weekIndex);

  return (
      <Card className="border-2">
        <CardHeader>
          <div className="flex items-center justify-between mb-4">
            <div>
              <CardTitle className="flex items-center gap-2 text-xl font-bold">
                <Activity className="w-5 h-5 text-green-600" />
                <span className="text-gray-900">{calendar.totalCertifications}개 인증</span>
                <span className="text-gray-400">·</span>
                <span className="text-gray-900">{selectedYear}년</span>
              </CardTitle>
              <CardDescription className="text-sm font-medium text-gray-900 mt-1">
                {calendar.activeDays}일 활동 / {calendar.totalDays}일
              </CardDescription>
            </div>

            {/* 연도 선택 */}
            <div className="flex items-center gap-2">
              <Button
                  variant="outline"
                  size="sm"
                  onClick={() => onYearChange(selectedYear - 1)}
                  disabled={!canGoPrev}
                  className="h-8 w-8 p-0"
              >
                <ChevronLeft className="w-4 h-4" />
              </Button>
              <span className="text-xl font-bold text-gray-900 min-w-[70px] text-center">
              {selectedYear}
            </span>
              <Button
                  variant="outline"
                  size="sm"
                  onClick={() => onYearChange(selectedYear + 1)}
                  disabled={!canGoNext}
                  className="h-8 w-8 p-0"
              >
                <ChevronRight className="w-4 h-4" />
              </Button>
            </div>
          </div>
        </CardHeader>

        <CardContent className="overflow-x-auto">
          <div className="inline-block min-w-full">
            <div className="flex gap-[3px]">
              {/* 요일 레이블 */}
              <div className="flex flex-col gap-[3px] w-[27px]">
                <div className="h-[16px]" /> {/* 월 레이블 공간 */}
                {["월", "", "수", "", "금", "", ""].map((day, i) => (
                    <div key={i} className="h-[10px] text-xs text-gray-600 font-medium flex items-center justify-end pr-1">
                      {day}
                    </div>
                ))}
              </div>

              {/* 잔디 그리드와 월 레이블 */}
              <div className="flex-1 relative">
                {/* 월 레이블 */}
                <div className="flex gap-[3px] mb-[3px] h-[16px] relative">
                  {monthLabels.map((label, index) => (
                      <div
                          key={index}
                          className="text-xs text-gray-700 font-semibold"
                          style={{
                            position: 'absolute',
                            left: `${label.weekIndex * 13 + 15}px`,
                          }}
                      >
                        {label.month}
                      </div>
                  ))}
                </div>

                {/* 잔디 그리드 */}
                <div className="flex gap-[3px] relative">
                  {/* 월별 구분선 */}
                  {monthDividers.map((weekIndex, index) => (
                      index > 0 && (
                          <div
                              key={index}
                              className="absolute top-0 bottom-0 w-[2px] bg-gray-400"
                              style={{
                                left: `${weekIndex * 13 - 15}px`,
                              }}
                          />
                      )
                  ))}

                  {weeks.map((week, weekIndex) => (
                      <div key={weekIndex} className="flex flex-col gap-[3px]">
                        {week.map((activity, dayIndex) => (
                            <div
                                key={dayIndex}
                                className={cn(
                                    "w-[10px] h-[10px] rounded-sm transition-all cursor-pointer",
                                    activity
                                        ? getActivityColor(activity.activityLevel)
                                        : "bg-transparent"
                                )}
                                title={
                                  activity
                                      ? `${activity.date}: ${activity.certificationCount}개 인증`
                                      : ""
                                }
                            />
                        ))}
                      </div>
                  ))}
                </div>
              </div>
            </div>

            {/* 범례 */}
            <div className="flex items-center justify-end gap-2 text-xs text-gray-500 mt-4">
              <span>적음</span>
              {[0, 1, 2, 3, 4].map((level) => (
                  <div
                      key={level}
                      className={cn("w-[10px] h-[10px] rounded-sm", getActivityColor(level))}
                  />
              ))}
              <span>많음</span>
            </div>
          </div>
        </CardContent>
      </Card>
  );
}

// 스켈레톤
function StreaksSectionSkeleton() {
  return (
      <Card className="shadow-lg rounded-xl bg-white">
        <CardHeader>
          <div className="flex items-center space-x-3">
            <Skeleton className="w-10 h-10 rounded-lg" />
            <div className="flex-1">
              <Skeleton className="h-6 w-32 mb-2" />
              <Skeleton className="h-4 w-48" />
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
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
        </CardContent>
      </Card>
  );
}