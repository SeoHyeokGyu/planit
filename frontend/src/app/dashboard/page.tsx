"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "@/stores/authStore";
import { userService } from "@/services/userService";

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

  useEffect(() => {
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
        <h1 className="text-3xl font-bold text-gray-900 mb-8">
          안녕하세요, {userProfile?.data?.nickname || "사용자"}님!
        </h1>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* 참여 중인 챌린지 */}
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              참여 중인 챌린지
            </h2>
            {isStatsLoading ? (
                <div className="h-10 w-16 bg-gray-200 animate-pulse rounded-md mt-1 mb-3"></div>
            ) : (
                <div 
                  className="text-4xl font-bold text-blue-600 mb-2 cursor-pointer hover:underline decoration-blue-400 underline-offset-4"
                  onClick={() => router.push("/challenge/my")}
                >
                  {dashboardStats?.data?.challengeCount || 0}
                </div>
            )}
            <p className="text-gray-500 text-sm">현재 진행 중인 챌린지</p>
            <button
              onClick={() => router.push("/challenge")}
              className="mt-4 text-blue-600 hover:text-blue-700 text-sm font-medium"
            >
              챌린지 둘러보기 &rarr;
            </button>
          </div>

          {/* 완료한 인증 */}
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              완료한 인증
            </h2>
            {isStatsLoading ? (
                <div className="h-10 w-16 bg-gray-200 animate-pulse rounded-md mt-1 mb-3"></div>
            ) : (
                <div 
                    className="text-4xl font-bold text-green-600 mb-2 cursor-pointer hover:underline decoration-green-400 underline-offset-4"
                    onClick={() => router.push("/certification/my")}
                >
                    {dashboardStats?.data?.certificationCount || 0}
                </div>
            )}
            <p className="text-gray-500 text-sm">총 인증 횟수</p>
            <button
                onClick={() => router.push("/certification/my")}
                className="mt-4 text-blue-600 hover:text-blue-700 text-sm font-medium"
            >
                인증 목록 보기 &rarr;
            </button>
          </div>

          {/* 팔로워/팔로잉 */}
          <div className="bg-white rounded-xl shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              소셜
            </h2>
            <div className="flex gap-8">
              <div>
                <div className="text-2xl font-bold text-gray-900">0</div>
                <p className="text-gray-500 text-sm">팔로워</p>
              </div>
              <div>
                <div className="text-2xl font-bold text-gray-900">0</div>
                <p className="text-gray-500 text-sm">팔로잉</p>
              </div>
            </div>
            <button
              onClick={() => router.push("/profile")}
              className="mt-4 text-blue-600 hover:text-blue-700 text-sm font-medium"
            >
              프로필 보기 &rarr;
            </button>
          </div>
        </div>

        {/* 최근 피드 */}
        <div className="mt-8 bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            최근 피드
          </h2>
          <div className="text-center py-8 text-gray-500">
            아직 피드가 없습니다. 챌린지에 참여하고 인증해보세요!
          </div>
          <button
            onClick={() => router.push("/feed")}
            className="text-blue-600 hover:text-blue-700 text-sm font-medium"
          >
            피드 보기 &rarr;
          </button>
        </div>
      </main>
    </div>
  );
}
