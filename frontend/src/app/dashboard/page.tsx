"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "@/stores/authStore";
import { userService } from "@/services/userService";
import Header from "@/components/layout/Header";

export default function DashboardPage() {
  const router = useRouter();
  const token = useAuthStore((state) => state.token);
  const { data: userProfile, isLoading } = useQuery({
    queryKey: ["userProfile"],
    queryFn: () => userService.getProfile(),
    enabled: !!token,
  });

  useEffect(() => {
    if (!token) {
      router.push("/login");
    }
  }, [token, router]);

  if (!token) {
    return null;
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="flex items-center justify-center h-[calc(100vh-64px)]">
          <div className="text-gray-500">로딩 중...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
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
            <div className="text-4xl font-bold text-blue-600 mb-2">0</div>
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
            <div className="text-4xl font-bold text-green-600 mb-2">0</div>
            <p className="text-gray-500 text-sm">총 인증 횟수</p>
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
