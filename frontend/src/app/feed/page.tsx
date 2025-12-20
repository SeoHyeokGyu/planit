"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useFeed } from "@/hooks/useFeed";
import { useAuthStore } from "@/stores/authStore";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { ArrowLeft, Heart, MessageCircle, Repeat2, Share, Calendar } from "lucide-react";
import Image from "next/image";
import { Badge } from "@/components/ui/badge";

export default function FeedPage() {
  const router = useRouter();
  const token = useAuthStore((state) => state.token);
  const [isMounted, setIsMounted] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);

  const { data: feedData, isLoading } = useFeed(currentPage, 10);

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

  const feed = feedData?.content || [];
  const hasMorePages = currentPage < (feedData?.totalPages || 1) - 1;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-2xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="flex items-center gap-4 mb-8">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => router.push("/dashboard")}
            className="hover:bg-blue-100"
          >
            <ArrowLeft className="w-6 h-6 text-gray-700" />
          </Button>
          <div className="flex-1">
            <h1 className="text-3xl font-bold text-gray-900">
              피드
            </h1>
            <p className="text-gray-600 text-sm">
              팔로우하는 사람들의 최근 활동을 확인하세요
            </p>
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setCurrentPage(0)}
            className="gap-2"
          >
            <Repeat2 className="w-4 h-4" />
            새로고침
          </Button>
        </div>

        {/* Feed List */}
        {isLoading ? (
          <div className="space-y-4">
            {[...Array(3)].map((_, i) => (
              <FeedItemSkeleton key={i} />
            ))}
          </div>
        ) : feed && feed.length > 0 ? (
          <>
            <div className="space-y-4">
              {feed.map((cert: any) => (
                <FeedItem
                  key={cert.id}
                  certification={cert}
                  onClick={() => router.push(`/certification/${cert.id}`)}
                />
              ))}
            </div>

            {/* Pagination */}
            <div className="mt-8 flex justify-between items-center">
              <Button
                variant="outline"
                onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                disabled={currentPage === 0}
              >
                이전
              </Button>
              <span className="text-sm text-gray-600">
                {currentPage + 1} / {feedData?.totalPages || 1} 페이지
              </span>
              <Button
                variant="outline"
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={!hasMorePages}
              >
                다음
              </Button>
            </div>
          </>
        ) : (
          <div className="text-center py-20 bg-white rounded-2xl shadow-sm border-2 border-dashed border-gray-200">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-50 rounded-full mb-4">
              <MessageCircle className="w-8 h-8 text-gray-300" />
            </div>
            <p className="text-gray-700 text-lg font-semibold">
              아직 피드가 없습니다
            </p>
            <p className="text-gray-500 text-sm mt-2 mb-6">
              팔로우하는 사람의 인증 활동이 여기에 표시됩니다
            </p>
            <Button
              onClick={() => router.push("/profile")}
              className="bg-blue-600 hover:bg-blue-700"
            >
              사람 팔로우하기
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}

function FeedItem({
  certification,
  onClick,
}: {
  certification: any;
  onClick: () => void;
}) {
  return (
    <Card className="cursor-pointer hover:shadow-lg transition-shadow border-0 bg-white overflow-hidden">
      {/* 작성자 정보 */}
      <div className="px-6 py-4 border-b border-gray-100">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-br from-blue-400 to-purple-500 rounded-full flex items-center justify-center text-white font-semibold text-sm">
              {certification.senderNickname?.charAt(0) || "?"}
            </div>
            <div>
              <p className="font-semibold text-gray-900">
                {certification.senderNickname || "알 수 없는 사용자"}
              </p>
              <p className="text-xs text-gray-500">
                @{certification.senderLoginId || "unknown"}
              </p>
            </div>
          </div>
          <p className="text-xs text-gray-500">
            {formatTimeAgo(new Date(certification.createdAt))}
          </p>
        </div>
      </div>

      {/* 인증 콘텐츠 */}
      <div onClick={onClick}>
        {/* 사진 */}
        {certification.photoUrl && (
          <div className="relative h-80 w-full bg-gray-100">
            <Image
              src={certification.photoUrl}
              alt={certification.title}
              layout="fill"
              objectFit="cover"
              className="hover:brightness-95 transition-all duration-300"
            />
          </div>
        )}

        {/* 텍스트 콘텐츠 */}
        <div className="px-6 py-4">
          <div className="mb-3">
            <Badge variant="secondary" className="bg-blue-50 text-blue-700 hover:bg-blue-100">
              {certification.challengeTitle}
            </Badge>
          </div>

          <h3 className="text-lg font-bold text-gray-900 mb-2 hover:text-blue-600 transition-colors">
            {certification.title}
          </h3>

          <p className="text-gray-700 text-sm leading-relaxed mb-4 line-clamp-3">
            {certification.content}
          </p>

          <div className="flex items-center gap-2 text-xs text-gray-500">
            <Calendar className="w-4 h-4" />
            {new Date(certification.createdAt).toLocaleDateString("ko-KR", {
              year: "numeric",
              month: "long",
              day: "numeric",
              hour: "2-digit",
              minute: "2-digit",
            })}
          </div>
        </div>
      </div>

      {/* 액션 버튼 */}
      <div className="px-6 py-3 border-t border-gray-100 flex items-center gap-4">
        <button className="flex items-center gap-1 text-gray-600 hover:text-red-500 transition-colors text-sm">
          <Heart className="w-5 h-5" />
          <span>23</span>
        </button>
        <button className="flex items-center gap-1 text-gray-600 hover:text-blue-500 transition-colors text-sm">
          <MessageCircle className="w-5 h-5" />
          <span>5</span>
        </button>
        <button className="flex items-center gap-1 text-gray-600 hover:text-green-500 transition-colors text-sm">
          <Repeat2 className="w-5 h-5" />
          <span>2</span>
        </button>
        <button className="ml-auto text-gray-600 hover:text-gray-900 transition-colors">
          <Share className="w-5 h-5" />
        </button>
      </div>
    </Card>
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

function formatTimeAgo(date: Date): string {
  const now = new Date();
  const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (seconds < 60) return "방금 전";
  if (seconds < 3600) return `${Math.floor(seconds / 60)}분 전`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}시간 전`;
  if (seconds < 604800) return `${Math.floor(seconds / 86400)}일 전`;

  return date.toLocaleDateString("ko-KR");
}
