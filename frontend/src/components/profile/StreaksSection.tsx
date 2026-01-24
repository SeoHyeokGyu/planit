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
import { pageHeaderStyles, iconGradients } from "@/styles/common";
import { EmptyState } from "@/components/ui/empty-state";

interface StreaksSectionProps {
  userLoginId: string;
  isOwnProfile?: boolean;
}

export default function StreaksSection({ userLoginId, isOwnProfile = false }: StreaksSectionProps) {
  const [selectedTab, setSelectedTab] = useState<"overview" | "calendar">("overview");
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1; // 1-12
  const [selectedYear, setSelectedYear] = useState<number>(currentYear);
  const [selectedMonth, setSelectedMonth] = useState<number>(currentMonth);

  // 스트릭 요약 조회
  const {
    data: streakSummary,
    isLoading: isLoadingSummary,
    error: summaryError,
  } = useAllStreaks(userLoginId);

  // 활동 캘린더 조회 (연도별 - 통계용)
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
                <p className="text-sm text-blue-600 mt-2">
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
              <CardDescription className="text-blue-700">연속 달성 기록을 확인하세요</CardDescription>
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
              캘린더
            </Button>
          </div>
        </CardHeader>

        <CardContent className="space-y-6">
          {/* 전체 통계 카드 */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Card className="border-2 border-orange-100">
              <CardHeader className="pb-3">
                <CardTitle className="text-sm font-medium text-blue-700 flex items-center gap-2">
                  <Flame className="w-4 h-4 text-orange-500" />
                  현재 스트릭
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold text-orange-600">
                  {streakSummary.totalCurrentStreak}
                  <span className="text-lg text-blue-600 ml-1">일</span>
                </div>
                <p className="text-xs text-blue-600 mt-1">
                  {streakSummary.activeStreakCount}개 챌린지 활동 중
                </p>
              </CardContent>
            </Card>

            <Card className="border-2 border-purple-100">
              <CardHeader className="pb-3">
                <CardTitle className="text-sm font-medium text-blue-700 flex items-center gap-2">
                  <Award className="w-4 h-4 text-purple-500" />
                  최장 기록
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold text-purple-600">
                  {streakSummary.maxLongestStreak}
                  <span className="text-lg text-blue-600 ml-1">일</span>
                </div>
                <p className="text-xs text-blue-600 mt-1">역대 최고 연속 달성</p>
              </CardContent>
            </Card>

            <Card className="border-2 border-blue-100">
              <CardHeader className="pb-3">
                <CardTitle className="text-sm font-medium text-blue-700 flex items-center gap-2">
                  <TrendingUp className="w-4 h-4 text-blue-500" />
                  활동 일수
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold text-blue-600">
                  {calendar?.activeDays || 0}
                  <span className="text-lg text-blue-600 ml-1">일</span>
                </div>
                <p className="text-xs text-blue-600 mt-1">{selectedYear}년 활동</p>
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
                  selectedMonth={selectedMonth}
                  onYearChange={setSelectedYear}
                  onMonthChange={setSelectedMonth}
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
                        ? "border-red-200 bg-red-50/50 hover:border-red-300"
                        : "border-gray-200 hover:border-blue-300"
                )}
            >
              <CardContent className="py-4">
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="font-bold text-gray-800 text-base">
                        {streak.challengeTitle}
                      </h3>
                      {streak.isCertifiedToday && (
                          <Badge className="bg-green-100 text-green-700 border-green-200">
                            오늘 인증 완료 ✓
                          </Badge>
                      )}
                      {streak.isStreakAtRisk && (
                          <Badge variant="destructive" className="bg-red-100 text-red-700 border-red-200">
                            ⚠️ 오늘 인증 필요
                          </Badge>
                      )}
                    </div>
                    <div className="flex items-center gap-4 text-sm text-blue-700">
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5" />
                    마지막 인증: {streak.lastCertificationDate || "없음"}
                  </span>
                    </div>
                  </div>

                  <div className="flex items-center gap-6">
                    <div className="text-center">
                      <div className="text-2xl font-bold text-orange-600 flex items-center gap-1">
                        <Flame className="w-5 h-5" />
                        {streak.currentStreak}
                      </div>
                      <p className="text-xs text-blue-600 mt-1">현재</p>
                    </div>
                    <div className="text-center">
                      <div className="text-2xl font-bold text-purple-600 flex items-center gap-1">
                        <Award className="w-5 h-5" />
                        {streak.longestStreak}
                      </div>
                      <p className="text-xs text-blue-600 mt-1">최고</p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
        ))}
      </div>
  );
}

// 월별 캘린더 뷰
function ActivityCalendarView({
                                calendar,
                                isLoading,
                                selectedYear,
                                selectedMonth,
                                onYearChange,
                                onMonthChange,
                              }: {
  calendar?: ActivityCalendarResponse;
  isLoading: boolean;
  selectedYear: number;
  selectedMonth: number;
  onYearChange: (year: number) => void;
  onMonthChange: (month: number) => void;
}) {
  if (isLoading) {
    return <Skeleton className="h-64 w-full" />;
  }

  if (!calendar) {
    return null;
  }

  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1;

  const canGoNext = selectedYear < currentYear || (selectedYear === currentYear && selectedMonth < currentMonth);
  const canGoPrev = selectedYear > currentYear - 10; // 최대 10년 전까지

  // 이전/다음 월 이동
  const handlePrevMonth = () => {
    if (selectedMonth === 1) {
      onYearChange(selectedYear - 1);
      onMonthChange(12);
    } else {
      onMonthChange(selectedMonth - 1);
    }
  };

  const handleNextMonth = () => {
    if (selectedMonth === 12) {
      onYearChange(selectedYear + 1);
      onMonthChange(1);
    } else {
      onMonthChange(selectedMonth + 1);
    }
  };

  // 활동 데이터를 Map으로 변환
  const activityMap = new Map<string, DailyActivityResponse>();
  calendar.activities.forEach((activity) => {
    activityMap.set(activity.date, activity);
  });

  // 해당 월의 캘린더 생성
  const firstDayOfMonth = new Date(selectedYear, selectedMonth - 1, 1);
  const lastDayOfMonth = new Date(selectedYear, selectedMonth, 0);
  const daysInMonth = lastDayOfMonth.getDate();
  const startDayOfWeek = firstDayOfMonth.getDay(); // 0: 일요일

  // 캘린더 주 배열 생성
  const weeks: (DailyActivityResponse | null)[][] = [];
  let currentWeek: (DailyActivityResponse | null)[] = [];

  // 첫 주 빈 칸 채우기
  for (let i = 0; i < startDayOfWeek; i++) {
    currentWeek.push(null);
  }

  // 날짜 채우기
  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = `${selectedYear}-${String(selectedMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    const activity = activityMap.get(dateStr) || {
      date: dateStr,
      certificationCount: 0,
      challengeCount: 0,
      activityLevel: 0,
    };

    currentWeek.push(activity);

    // 토요일이면 주 완성하고 새 주 시작
    if (currentWeek.length === 7) {
      weeks.push(currentWeek);
      currentWeek = [];
    }
  }

  // 마지막 주 빈 칸 채우기
  if (currentWeek.length > 0) {
    while (currentWeek.length < 7) {
      currentWeek.push(null);
    }
    weeks.push(currentWeek);
  }

  // 파란색 계열 색상 (0: 활동 없음, 1-4: 점점 진해짐)
  const getActivityColor = (level: number) => {
    switch (level) {
      case 0:
        return "bg-gray-100 hover:bg-gray-200";
      case 1:
        return "bg-blue-100 hover:bg-blue-200";
      case 2:
        return "bg-blue-300 hover:bg-blue-400";
      case 3:
        return "bg-blue-500 hover:bg-blue-600";
      case 4:
        return "bg-blue-700 hover:bg-blue-800";
      default:
        return "bg-gray-100";
    }
  };

  // 해당 월의 통계 계산
  const monthActivities = calendar.activities.filter(activity => {
    const activityDate = new Date(activity.date);
    return activityDate.getFullYear() === selectedYear &&
        activityDate.getMonth() + 1 === selectedMonth;
  });

  const monthActiveDays = monthActivities.filter(a => a.certificationCount > 0).length;
  const monthTotalCertifications = monthActivities.reduce((sum, a) => sum + a.certificationCount, 0);

  return (
      <Card className="border-2">
        <CardHeader>
          <div className="flex items-center justify-between mb-4">
            <div>
              <CardTitle className="flex items-center gap-2 text-xl font-bold">
                <Activity className="w-5 h-5 text-blue-600" />
                <span className="text-gray-900">{monthTotalCertifications}개 인증</span>
                <span className="text-blue-500">·</span>
                <span className="text-gray-900">{selectedYear}년 {selectedMonth}월</span>
              </CardTitle>
              <CardDescription className="text-sm font-medium text-gray-900 mt-1">
                {monthActiveDays}일 활동 / {daysInMonth}일
              </CardDescription>
            </div>

            {/* 월 선택 */}
            <div className="flex items-center gap-2">
              <Button
                  variant="outline"
                  size="sm"
                  onClick={handlePrevMonth}
                  disabled={!canGoPrev}
                  className="h-8 w-8 p-0"
              >
                <ChevronLeft className="w-4 h-4" />
              </Button>
              <span className="text-xl font-bold text-gray-900 min-w-[100px] text-center">
                {selectedYear}.{String(selectedMonth).padStart(2, '0')}
              </span>
              <Button
                  variant="outline"
                  size="sm"
                  onClick={handleNextMonth}
                  disabled={!canGoNext}
                  className="h-8 w-8 p-0"
              >
                <ChevronRight className="w-4 h-4" />
              </Button>
            </div>
          </div>
        </CardHeader>

        <CardContent>
          <div className="space-y-2">
            {/* 요일 헤더 */}
            <div className="grid grid-cols-7 gap-2 mb-2">
              {["일", "월", "화", "수", "목", "금", "토"].map((day, index) => (
                  <div
                      key={day}
                      className={cn(
                          "text-center text-sm font-semibold",
                          index === 0 ? "text-red-600" : index === 6 ? "text-blue-600" : "text-gray-700"
                      )}
                  >
                    {day}
                  </div>
              ))}
            </div>

            {/* 캘린더 그리드 */}
            <div className="space-y-2">
              {weeks.map((week, weekIndex) => (
                  <div key={weekIndex} className="grid grid-cols-7 gap-2">
                    {week.map((activity, dayIndex) => {
                      if (!activity) {
                        return <div key={dayIndex} className="aspect-square" />;
                      }

                      const day = new Date(activity.date).getDate();
                      const isToday = activity.date === new Date().toISOString().split('T')[0];

                      return (
                          <div
                              key={dayIndex}
                              className={cn(
                                  "aspect-square rounded-lg transition-all relative flex flex-col items-center justify-center",
                                  getActivityColor(activity.activityLevel),
                                  isToday && "ring-2 ring-orange-500 ring-offset-2"
                              )}
                              title={`${activity.date}: ${activity.certificationCount}개 인증`}
                          >
                            <span className={cn(
                                "text-base font-bold",
                                activity.activityLevel >= 3 ? "text-white" : "text-gray-700"
                            )}>
                              {day}
                            </span>
                            {activity.certificationCount > 0 && (
                                <span className={cn(
                                    "text-[10px] font-bold",
                                    activity.activityLevel >= 3 ? "text-white" : "text-blue-600"
                                )}>
                                  {activity.certificationCount}
                                </span>
                            )}
                          </div>
                      );
                    })}
                  </div>
              ))}
            </div>

            {/* 범례 */}
            <div className="flex items-center justify-end gap-2 text-xs text-blue-600 mt-6 pt-4 border-t">
              <span>적음</span>
              {[0, 1, 2, 3, 4].map((level) => (
                  <div
                      key={level}
                      className={cn("w-6 h-6 rounded", getActivityColor(level))}
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