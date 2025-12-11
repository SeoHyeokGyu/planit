"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { useFollowingFeed, useRealtimeFeed } from "@/hooks/useFeed";
import { FeedItem } from "@/components/feed/FeedItem";
import Header from "@/components/layout/Header";

type FeedTab = "realtime" | "following";

export default function FeedPage() {
  const router = useRouter();
  const token = useAuthStore((state) => state.token);
  const [activeTab, setActiveTab] = useState<FeedTab>("realtime");

  // 데이터 쿼리
  const { data: followingFeedData, isLoading: isLoadingFollowing } =
    useFollowingFeed(20, 0);
  const { feeds: realtimeFeeds, isLoading: isLoadingRealtime } = useRealtimeFeed(
    activeTab === "realtime"
  );

  useEffect(() => {
    if (!token) {
      router.push("/login");
    }
  }, [token, router]);

  if (!token) {
    return null;
  }

  const isLoading = activeTab === "realtime" ? isLoadingRealtime : isLoadingFollowing;
  const feeds =
    activeTab === "realtime"
      ? realtimeFeeds
      : followingFeedData?.data || [];

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-3xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">피드</h1>
          <p className="text-gray-600">팔로우하는 사용자들의 인증 내역을 확인하세요</p>
        </div>

        {/* Tabs */}
        <div className="flex gap-2 mb-8 bg-white rounded-lg p-1 shadow-sm">
          <button
            onClick={() => setActiveTab("realtime")}
            className={`flex-1 py-2 px-4 rounded-md font-medium transition-all ${
              activeTab === "realtime"
                ? "bg-blue-600 text-white"
                : "text-gray-700 hover:bg-gray-50"
            }`}
          >
            🔴 실시간 피드
          </button>
          <button
            onClick={() => setActiveTab("following")}
            className={`flex-1 py-2 px-4 rounded-md font-medium transition-all ${
              activeTab === "following"
                ? "bg-blue-600 text-white"
                : "text-gray-700 hover:bg-gray-50"
            }`}
          >
            👥 팔로우 피드
          </button>
        </div>

        {/* Content */}
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-gray-500">
              <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-600 mx-auto mb-2"></div>
              로딩 중...
            </div>
          </div>
        ) : feeds.length === 0 ? (
          <div className="bg-white rounded-xl shadow-sm p-12 text-center">
            <div className="text-gray-400 text-4xl mb-4">
              {activeTab === "realtime" ? "📡" : "👥"}
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              {activeTab === "realtime"
                ? "실시간 피드가 비어있습니다"
                : "팔로우 피드가 비어있습니다"}
            </h3>
            <p className="text-gray-500 text-sm mb-6">
              {activeTab === "realtime"
                ? "다른 사용자들의 인증이 실시간으로 표시됩니다"
                : "팔로우하는 사용자의 인증 내역이 여기에 표시됩니다"}
            </p>
            <button
              onClick={() => router.push("/challenge")}
              className="inline-block bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 font-medium"
            >
              챌린지 둘러보기
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {feeds.map((feed) => (
              <FeedItem key={feed.id} feed={feed} />
            ))}
          </div>
        )}

        {/* Realtime Indicator */}
        {activeTab === "realtime" && feeds.length > 0 && (
          <div className="mt-8 p-4 bg-blue-50 border border-blue-200 rounded-lg text-center">
            <p className="text-sm text-blue-900">
              🔴 <span className="font-semibold">라이브</span> 실시간 피드가 활성화되어 있습니다
            </p>
          </div>
        )}
      </main>
    </div>
  );
}
