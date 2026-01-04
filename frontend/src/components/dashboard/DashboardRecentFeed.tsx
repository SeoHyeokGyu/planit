"use client";

import { useRouter } from "next/navigation";
import { useFeed } from "@/hooks/useFeed";
import FeedItem from "@/components/feed/FeedItem";
import { Zap } from "lucide-react";
import { FeedResponse } from "@/types/feed";

export default function DashboardRecentFeed() {
  const router = useRouter();
  const { data: feedData, isLoading: isFeedLoading } = useFeed(0, 3);
  const feedList = feedData?.content || [];

  return (
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
  );
}
