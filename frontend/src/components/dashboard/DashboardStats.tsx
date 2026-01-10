"use client";

import React from "react";
import { useRouter } from "next/navigation";
import { useDashboardStats } from "@/hooks/useUser";
import { Check, Trophy, Heart } from "lucide-react";

export default function DashboardStats() {
  const router = useRouter();
  const { data: dashboardStats, isLoading } = useDashboardStats();

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {/* 참여 중인 챌린지 */}
      <div className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow border border-gray-100 p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <Trophy className="w-5 h-5 text-amber-500" />
          참여 중인 챌린지
        </h2>
        {isLoading ? (
          <div className="h-10 w-16 bg-gray-200 animate-pulse rounded-md mt-1 mb-3"></div>
        ) : (
          <div
            className="text-4xl font-bold text-blue-600 mb-2 cursor-pointer hover:text-blue-700 transition-colors"
            onClick={() => router.push("/challenge/my")}
          >
            {dashboardStats?.challengeCount || 0}
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
        {isLoading ? (
          <div className="h-10 w-16 bg-gray-200 animate-pulse rounded-md mt-1 mb-3"></div>
        ) : (
          <div
            className="text-4xl font-bold text-green-600 mb-2 cursor-pointer hover:text-green-700 transition-colors"
            onClick={() => router.push("/certification/my")}
          >
            {dashboardStats?.certificationCount || 0}
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

      {/* 소셜 팔로워 */}
      <div className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow border border-gray-100 p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <Heart className="w-5 h-5 text-red-500" />
          소셜 팔로워
        </h2>
        {isLoading ? (
          <div className="flex gap-8 mb-4 mt-1">
            <div>
              <div className="h-9 w-12 bg-gray-200 animate-pulse rounded-md mb-1"></div>
              <p className="text-gray-500 text-sm">팔로워</p>
            </div>
            <div>
              <div className="h-9 w-12 bg-gray-200 animate-pulse rounded-md mb-1"></div>
              <p className="text-gray-500 text-sm">팔로잉</p>
            </div>
          </div>
        ) : (
          <div className="flex gap-8 mb-4">
            <div
              className="cursor-pointer group"
              onClick={() => router.push("/profile")}
            >
              <div className="text-3xl font-bold text-blue-600 group-hover:text-blue-700 transition-colors">
                {dashboardStats?.followerCount || 0}
              </div>
              <p className="text-gray-500 text-sm">팔로워</p>
            </div>
            <div
              className="cursor-pointer group"
              onClick={() => router.push("/profile")}
            >
              <div className="text-3xl font-bold text-purple-600 group-hover:text-purple-700 transition-colors">
                {dashboardStats?.followingCount || 0}
              </div>
              <p className="text-gray-500 text-sm">팔로잉</p>
            </div>
          </div>
        )}
        <button
          onClick={() => router.push("/profile")}
          className="inline-flex items-center gap-2 px-3 py-2 rounded-lg text-blue-600 hover:text-blue-700 hover:bg-blue-50 font-medium text-sm transition-all"
        >
          프로필에서 자세히 보기
          <span>→</span>
        </button>
      </div>
    </div>
  );
}