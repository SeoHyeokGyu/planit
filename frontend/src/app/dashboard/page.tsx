"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "@/stores/authStore";
import { userService } from "@/services/userService";
import { useFollowStats } from "@/hooks/useFollow";
import { useUserProfile } from "@/hooks/useUser";
import { useFeed } from "@/hooks/useFeed";
import FeedItem from "@/components/feed/FeedItem";
import { Trophy, Check, Heart, Zap } from "lucide-react";
import { FeedResponse } from "@/types/feed";

export default function DashboardPage() {
  const router = useRouter();
  const token = useAuthStore((state) => state.token);
  // SSR과 클라이언트 스토리지 간의 하이드레이션 불일치를 방지하기 위한 상태
  const [isMounted, setIsMounted] = useState(false);

  const { data: userProfile, isLoading: isProfileLoading } = useQuery({
    queryKey: ["userProfile"],
    queryFn: () => userService.getProfile(),
    enabled: !!token,
  });

  const { data: dashboardStats, isLoading: isStatsLoading } = useQuery({
    queryKey: ["dashboardStats"],
    queryFn: () => userService.getDashboardStats(),
    enabled: !!token,
  });

  // 팔로워/팔로잉 통계
  const { data: currentUser } = useUserProfile();
  const { followerCount, followingCount } = useFollowStats(currentUser?.loginId || "");

  // 최근 피드 (3개만 조회)
  const { data: feedData, isLoading: isFeedLoading } = useFeed(0, 3);
  const feedList = feedData?.content || [];

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setIsMounted(true);
  }, []);

  useEffect(() => {
    if (isMounted && !token) {
      router.push("/login");
    }
  }, [isMounted, token, router]);

  if (!isMounted) {
    return null;
  }

  if (!token) {
    return null;
  }

  if (isProfileLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="flex items-center justify-center h-[calc(100vh-64px)]">
          <div className="text-gray-500">로딩 중...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-2">
            안녕하세요, {userProfile?.data?.nickname || "사용자"}님!
          </h1>
          <p className="text-gray-500">오늘도 화이팅! 🚀</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* 참여 중인 챌린지 */}
          <div className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow border border-gray-100 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <Trophy className="w-5 h-5 text-amber-500" />
              참여 중인 챌린지
            </h2>
            {isStatsLoading ? (
                <div className="h-10 w-16 bg-gray-200 animate-pulse rounded-md mt-1 mb-3"></div>
            ) : (
                <div
                  className="text-4xl font-bold text-blue-600 mb-2 cursor-pointer hover:text-blue-700 transition-colors"
                  onClick={() => router.push("/challenge/my")}
                >
                  {dashboardStats?.data?.challengeCount || 0}
                </div>
            )}
            <p className="text-gray-500 text-sm mb-4">현재 진행 중인 챌린지</p>
            <button
              onClick={() => router.push("/challenge")}
              className="inline-flex items-center gap-2 px-3 py-2 rounded-lg text-blue-600 hover:text-blue-700 hover:bg-blue-50 font-medium text-sm transition-all"
            >
              챌린지 둘러보기
              <span>→</span>
            </button>
          </div>

          {/* 완료한 인증 */}
          <div className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow border border-gray-100 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <Check className="w-5 h-5 text-green-500" />
              완료한 인증
            </h2>
            {isStatsLoading ? (
                <div className="h-10 w-16 bg-gray-200 animate-pulse rounded-md mt-1 mb-3"></div>
            ) : (
                <div
                    className="text-4xl font-bold text-green-600 mb-2 cursor-pointer hover:text-green-700 transition-colors"
                    onClick={() => router.push("/certification/my")}
                >
                    {dashboardStats?.data?.certificationCount || 0}
                </div>
            )}
            <p className="text-gray-500 text-sm mb-4">총 인증 횟수</p>
            <button
                onClick={() => router.push("/certification/my")}
                className="inline-flex items-center gap-2 px-3 py-2 rounded-lg text-green-600 hover:text-green-700 hover:bg-green-50 font-medium text-sm transition-all"
            >
                인증 목록 보기
                <span>→</span>
            </button>
          </div>

          {/* 팔로워/팔로잉 */}
          <div className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow border border-gray-100 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <Heart className="w-5 h-5 text-red-500" />
              소셜 팔로워
            </h2>
            <div className="flex gap-8 mb-4">
              <div>
                <div className="text-3xl font-bold text-blue-600">{followerCount}</div>
                <p className="text-gray-500 text-sm">팔로워</p>
              </div>
              <div>
                <div className="text-3xl font-bold text-purple-600">{followingCount}</div>
                <p className="text-gray-500 text-sm">팔로잉</p>
              </div>
            </div>
            <button
              onClick={() => router.push("/profile")}
              className="inline-flex items-center gap-2 px-3 py-2 rounded-lg text-blue-600 hover:text-blue-700 hover:bg-blue-50 font-medium text-sm transition-all"
            >
              프로필에서 자세히 보기
              <span>→</span>
            </button>
          </div>
        </div>

        {/* 최근 피드 */}
        <div className="mt-8 bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow border border-gray-100 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <Zap className="w-5 h-5 text-blue-500" />
            최근 피드
          </h2>
          
          {isFeedLoading ? (
            <div className="space-y-4">
              {[...Array(3)].map((_, i) => (
                 <div key={i} className="h-24 bg-gray-100 rounded-lg animate-pulse" />
              ))}
            </div>
          ) : feedList.length > 0 ? (
            <div className="space-y-4">
              {feedList.map((cert: FeedResponse) => (
                <FeedItem
                  key={cert.id}
                  certification={cert}
                  onClick={() => router.push(`/certification/${cert.id}`)}
                />
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-gray-500">
              아직 피드가 없습니다. 챌린지에 참여하고 인증해보세요!
            </div>
          )}

          <div className="flex justify-center mt-6">
            <button
              onClick={() => router.push("/feed")}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-lg text-blue-600 hover:text-blue-700 hover:bg-blue-50 font-medium text-sm transition-all"
            >
              피드 더보기
              <span>→</span>
            </button>
          </div>
        </div>

      </main>
    </div>
  );
}
