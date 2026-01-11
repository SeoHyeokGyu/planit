"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useFeedInfinite } from "@/hooks/useFeed";
import { useAuthStore } from "@/stores/authStore";
import FeedItem from "@/components/feed/FeedItem";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ArrowLeft, Heart, MessageCircle, Repeat2, Share, Calendar, Zap, Send, ArrowUpDown, Check } from "lucide-react";
import Image from "next/image";
import { Badge } from "@/components/ui/badge";
import { formatTimeAgo } from "@/lib/utils";
import { useMutation, useQuery, useQueryClient, useInfiniteQuery } from "@tanstack/react-query";
import { toast } from "sonner";
import { FeedResponse, FeedSortType } from "@/types/feed";
import { useInView } from "react-intersection-observer";
import { pageHeaderStyles, iconGradients } from "@/styles/pageHeader";
import { EmptyState } from "@/components/ui/empty-state";

export default function FeedPage() {
  const router = useRouter();
  const token = useAuthStore((state) => state.token);
  const { ref, inView } = useInView();
  const [sortBy, setSortBy] = useState<FeedSortType>("LATEST");

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
  } = useFeedInfinite(10, sortBy);

  useEffect(() => {
    if (inView && hasNextPage) {
      fetchNextPage();
    }
  }, [inView, hasNextPage, fetchNextPage]);

  if (!token) {
    return null;
  }

  const feedItems = data?.pages.flatMap((page) => page.data || []) || [];

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-2xl mx-auto px-4 py-8">
        {/* Header */}
        <div className={pageHeaderStyles.container}>
          <div className={pageHeaderStyles.wrapper}>
            <div className={pageHeaderStyles.titleSection}>
              <div className={pageHeaderStyles.titleWrapper}>
                <div className={`${pageHeaderStyles.iconBase} ${iconGradients.feed}`}>
                  <Zap className="w-6 h-6" />
                </div>
                <div>
                  <h1 className={pageHeaderStyles.title}>
                    피드
                  </h1>
                  <p className={`${pageHeaderStyles.description} text-sm mt-1`}>
                    팔로우하는 사람들의 최근 활동
                  </p>
                </div>
              </div>
            </div>
            <Select value={sortBy} onValueChange={(value) => setSortBy(value as FeedSortType)}>
              <SelectTrigger className={`w-[160px] ${pageHeaderStyles.standardButton}`}>
                <div className="flex items-center gap-2">
                  <ArrowUpDown className="w-4 h-4 text-blue-600" />
                  <SelectValue placeholder="정렬" />
                </div>
              </SelectTrigger>
              <SelectContent className="w-[160px] bg-white border-2 border-gray-300 shadow-xl rounded-lg p-1 animate-in fade-in-0 zoom-in-95">
                <SelectItem
                  value="LATEST"
                  className="cursor-pointer rounded-md text-gray-800 hover:bg-blue-500 hover:text-white focus:bg-blue-500 focus:text-white transition-colors duration-150 data-[state=checked]:bg-blue-600 data-[state=checked]:text-white font-semibold py-2.5"
                >
                  <div className="flex items-center justify-between w-full">
                    <span>최신순</span>
                    {sortBy === "LATEST" && <Check className="w-4 h-4 ml-2" />}
                  </div>
                </SelectItem>
                <SelectItem
                  value="LIKES"
                  className="cursor-pointer rounded-md text-gray-800 hover:bg-blue-500 hover:text-white focus:bg-blue-500 focus:text-white transition-colors duration-150 data-[state=checked]:bg-blue-600 data-[state=checked]:text-white font-semibold py-2.5"
                >
                  <div className="flex items-center justify-between w-full">
                    <span>좋아요순</span>
                    {sortBy === "LIKES" && <Check className="w-4 h-4 ml-2" />}
                  </div>
                </SelectItem>
                <SelectItem
                  value="COMMENTS"
                  className="cursor-pointer rounded-md text-gray-800 hover:bg-blue-500 hover:text-white focus:bg-blue-500 focus:text-white transition-colors duration-150 data-[state=checked]:bg-blue-600 data-[state=checked]:text-white font-semibold py-2.5"
                >
                  <div className="flex items-center justify-between w-full">
                    <span>댓글순</span>
                    {sortBy === "COMMENTS" && <Check className="w-4 h-4 ml-2" />}
                  </div>
                </SelectItem>
                <SelectItem
                  value="POPULAR"
                  className="cursor-pointer rounded-md text-gray-800 hover:bg-blue-500 hover:text-white focus:bg-blue-500 focus:text-white transition-colors duration-150 data-[state=checked]:bg-blue-600 data-[state=checked]:text-white font-semibold py-2.5"
                >
                  <div className="flex items-center justify-between w-full">
                    <span>인기순</span>
                    {sortBy === "POPULAR" && <Check className="w-4 h-4 ml-2" />}
                  </div>
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        {/* Feed List */}
        {isLoading ? (
          <div className="space-y-4">
            {[...Array(3)].map((_, i) => (
              <FeedItemSkeleton key={i} />
            ))}
          </div>
        ) : feedItems.length > 0 ? (
          <>
            <div className="space-y-4">
              {feedItems.map((cert: FeedResponse) => (
                <FeedItem
                  key={cert.id}
                  certification={cert}
                  onClick={() => router.push(`/certification/${cert.id}`)}
                />
              ))}
            </div>

            {/* Infinite Scroll Loader & Trigger */}
            <div ref={ref} className="mt-8 flex justify-center py-4">
              {isFetchingNextPage ? (
                <div className="flex items-center gap-2 text-gray-500 text-sm">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-600"></div>
                  불러오는 중...
                </div>
              ) : hasNextPage ? (
                <div className="h-4" /> // Invisible trigger area
              ) : (
                <p className="text-gray-400 text-sm">모든 피드를 확인했습니다.</p>
              )}
            </div>
          </>
        ) : (
          <EmptyState
            icon={MessageCircle}
            title="아직 피드가 없습니다"
            description="팔로우하는 사람의 인증 활동이 여기에 표시됩니다"
            actionLabel="사람 팔로우하기"
            onAction={() => router.push("/profile")}
            variant="bordered"
          />
        )}
      </div>
    </div>
  );
}

function FeedItemSkeleton() {
  return (
    <Card className="border-0 bg-white overflow-hidden">
      <div className="px-6 py-4 border-b border-gray-100">
        <div className="flex items-center gap-3">
          <Skeleton className="w-10 h-10 rounded-full" />
          <div className="flex-1">
            <Skeleton className="h-4 w-24 mb-2" />
            <Skeleton className="h-3 w-16" />
          </div>
        </div>
      </div>
      <Skeleton className="h-80 w-full" />
      <div className="px-6 py-4 space-y-2">
        <Skeleton className="h-4 w-24 mb-2" />
        <Skeleton className="h-6 w-3/4 mb-2" />
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-5/6" />
      </div>
    </Card>
  );
}
