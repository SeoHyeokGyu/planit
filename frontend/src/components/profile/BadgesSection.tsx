"use client";

import React from "react";
import {SortOption, useBadgeSort, useCheckBadges, useUserBadges} from "@/hooks/useBadge";
import BadgeItem from "@/components/badge/BadgeItem";
import {Skeleton} from "@/components/ui/skeleton";
import {ArrowUpDown, RefreshCcw, Trophy, Medal} from "lucide-react";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardHeader, CardTitle, CardDescription} from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { pageHeaderStyles, iconGradients } from "@/styles/pageHeader";

interface BadgesSectionProps {
  userLoginId: string;
  isOwnProfile: boolean;
}

export default function BadgesSection({userLoginId, isOwnProfile}: BadgesSectionProps) {
  // 특정 사용자의 배지 목록 조회
  const {data: badges, isLoading, isError} = useUserBadges(userLoginId);
  const {mutate: checkBadges, isPending: isChecking} = useCheckBadges();

  // 정렬 훅 사용
  const {sortedBadges, sortBy, setSortBy} = useBadgeSort(badges);

  const acquiredCount = badges?.filter((b) => b.isAcquired).length || 0;
  const totalCount = badges?.length || 0;

  if (isError) {
    return (
        <Card className="shadow-lg rounded-xl bg-white">
          <CardHeader>
            <CardTitle className="text-gray-900">배지</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-red-500">배지 정보를 불러올 수 없습니다.</p>
          </CardContent>
        </Card>
    );
  }

  return (
      <Card className="shadow-lg rounded-xl bg-white">
        <CardHeader className="flex flex-col sm:flex-row items-start sm:items-center sm:justify-between pb-4">
          <div className="flex items-center space-x-3 mb-4 sm:mb-0">
            <div className={`${pageHeaderStyles.iconBase} ${iconGradients.badge}`}>
              <Medal className="w-6 h-6"/>
            </div>
            <div>
              <CardTitle className="text-2xl font-bold text-gray-900">나의 배지</CardTitle>
              <CardDescription className="text-gray-600">
                {acquiredCount} / {totalCount}개 획득
              </CardDescription>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Select value={sortBy} onValueChange={(value) => setSortBy(value as SortOption)}>
              <SelectTrigger
                  className="w-[120px] h-11 bg-white border-2 border-gray-200 hover:border-blue-400 hover:bg-blue-50 transition-all duration-200 shadow-sm hover:shadow-md font-medium text-gray-700">
                <ArrowUpDown className="w-4 h-4 mr-2 text-blue-600"/>
                <SelectValue placeholder="정렬"/>
              </SelectTrigger>
              <SelectContent className="bg-white border-2 border-gray-200 shadow-xl z-50">
                <SelectItem value="acquired" className="focus:bg-blue-50 cursor-pointer hover:bg-blue-50 transition-colors">획득순</SelectItem>
                <SelectItem value="grade" className="focus:bg-blue-50 cursor-pointer hover:bg-blue-50 transition-colors">등급순</SelectItem>
                <SelectItem value="code" className="focus:bg-blue-50 cursor-pointer hover:bg-blue-50 transition-colors">종류순</SelectItem>
                <SelectItem value="name" className="focus:bg-blue-50 cursor-pointer hover:bg-blue-50 transition-colors">이름순</SelectItem>
              </SelectContent>
            </Select>

            {isOwnProfile && (
                <Button
                    variant="outline"
                    size="sm"
                    className="h-11 px-4 gap-2 bg-white border-2 border-gray-200 hover:border-blue-400 hover:bg-blue-50 transition-all duration-200 shadow-sm hover:shadow-md font-medium text-gray-700"
                    onClick={() => checkBadges(userLoginId)}
                    disabled={isChecking}
                >
                  <RefreshCcw className={`w-4 h-4 ${isChecking ? "animate-spin" : ""}`}/>
                  <span className="hidden sm:inline">배지 확인</span>
                </Button>
            )}
          </div>
        </CardHeader>

        <CardContent>
          {isLoading ? (
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                {[...Array(5)].map((_, i) => (
                    <Skeleton key={i} className="h-32 w-full rounded-xl"/>
                ))}
              </div>
          ) : !badges || badges.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 text-gray-500">
                <Trophy className="w-12 h-12 mb-4 text-gray-300"/>
                <p className="text-lg font-medium">아직 배지가 없습니다.</p>
                <p className="text-sm text-gray-600">다양한 활동을 통해 배지를 획득해보세요!</p>
                {isOwnProfile && (
                    <Button
                        variant="outline"
                        size="sm"
                        className="mt-4 gap-2"
                        onClick={() => checkBadges(userLoginId)}
                        disabled={isChecking}
                    >
                      <RefreshCcw className={`w-4 h-4 ${isChecking ? "animate-spin" : ""}`}/>
                      배지 획득 확인
                    </Button>
                )}
              </div>
          ) : (
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                {sortedBadges.map((badge) => (
                    <BadgeItem key={badge.code} badge={badge}/>
                ))}
              </div>
          )}
        </CardContent>
      </Card>
  );
}
