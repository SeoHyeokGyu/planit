"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useFeedInfinite } from "@/hooks/useFeed";
import { useAuthStore } from "@/stores/authStore";
import { likeCommentService } from "@/services/likeCommentService";
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
import { ArrowLeft, Heart, MessageCircle, Repeat2, Share, Calendar, Zap, Send } from "lucide-react";
import Image from "next/image";
import { Badge } from "@/components/ui/badge";
import { formatTimeAgo } from "@/lib/utils";
import { useMutation, useQuery, useQueryClient, useInfiniteQuery } from "@tanstack/react-query";
import { toast } from "sonner";
import { FeedResponse } from "@/types/feed";
import { useInView } from "react-intersection-observer";

export default function FeedPage() {
  const router = useRouter();
  const token = useAuthStore((state) => state.token);
  const [isMounted, setIsMounted] = useState(false);
  const { ref, inView } = useInView();

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
  } = useFeedInfinite(10);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  useEffect(() => {
    if (isMounted && !token) {
      router.push("/login");
    }
  }, [isMounted, token, router]);

  useEffect(() => {
    if (inView && hasNextPage) {
      fetchNextPage();
    }
  }, [inView, hasNextPage, fetchNextPage]);

  if (!isMounted) {
    return null;
  }

  if (!token) {
    return null;
  }

  const feedItems = data?.pages.flatMap((page) => page.data || []) || [];

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-2xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-500 rounded-lg flex items-center justify-center text-white">
              <Zap className="w-6 h-6" />
            </div>
            <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              피드
            </h1>
          </div>
          <p className="text-gray-600 text-sm font-medium ml-13">
            팔로우하는 사람들의 최근 활동을 확인하세요
          </p>
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
  certification: FeedResponse;
  onClick: () => void;
}) {
  const [isLiked, setIsLiked] = useState(certification.isLiked);
  const [likeCount, setLikeCount] = useState(certification.likeCount);
  const [showComments, setShowComments] = useState(false);
  const [commentCount, setCommentCount] = useState(certification.commentCount);
  const [newComment, setNewComment] = useState("");

  const toggleLikeMutation = useMutation({
    mutationFn: () => likeCommentService.toggleLike(certification.id),
    onMutate: () => {
      // Optimistic update
      const previousLiked = isLiked;
      setIsLiked(!isLiked);
      setLikeCount((prev) => (isLiked ? prev - 1 : prev + 1));
      return { previousLiked };
    },
    onError: (err, newTodo, context) => {
        // Revert on error
        if (context) {
            setIsLiked(context.previousLiked);
            setLikeCount((prev) => (context.previousLiked ? prev + 1 : prev - 1));
        }
        toast.error("좋아요 처리에 실패했습니다.");
    }
  });

  const { data: comments, refetch: refetchComments } = useQuery({
    queryKey: ["comments", certification.id],
    queryFn: () => likeCommentService.getComments(certification.id),
    enabled: showComments,
    select: (data) => data.data
  });

  const createCommentMutation = useMutation({
    mutationFn: (content: string) => likeCommentService.createComment(certification.id, content),
    onSuccess: () => {
        setNewComment("");
        refetchComments();
        setCommentCount((prev) => prev + 1);
    },
    onError: () => {
        toast.error("댓글 작성에 실패했습니다.");
    }
  });

  const handleCommentSubmit = (e: React.FormEvent) => {
      e.preventDefault();
      if (!newComment.trim()) return;
      createCommentMutation.mutate(newComment);
  };

  return (
    <Card className="border-0 bg-white overflow-hidden mb-4 shadow-sm hover:shadow-md transition-shadow">
      {/* 작성자 정보 */}
      <div className="px-6 py-4 border-b border-gray-100">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-br from-blue-400 to-purple-500 rounded-full flex items-center justify-center text-white font-semibold text-sm">
              {certification.authorNickname?.charAt(0) || "?"}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <p className="font-semibold text-gray-900">
                  {certification.authorNickname || "알 수 없는 사용자"}
                </p>
                {certification.isMine && (
                  <Badge className="text-[10px] h-5 px-1.5 py-0 bg-blue-100 text-blue-700 hover:bg-blue-200 border-0 shadow-none flex-shrink-0">
                    나
                  </Badge>
                )}
              </div>
              <p className="text-xs text-gray-500">
                @{certification.authorLoginId || "unknown"}
              </p>
            </div>
          </div>
          <p className="text-xs text-gray-500">
            {formatTimeAgo(new Date(certification.createdAt))}
          </p>
        </div>
      </div>

      {/* 인증 콘텐츠 */}
      <div onClick={onClick} className="cursor-pointer">
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
        </div>
      </div>

      {/* 액션 버튼 */}
      <div className="px-6 py-3 border-t border-gray-100 flex items-center gap-4">
        <button
            onClick={(e) => { e.stopPropagation(); toggleLikeMutation.mutate(); }}
            className={`flex items-center gap-1 transition-colors text-sm ${isLiked ? "text-red-500" : "text-gray-600 hover:text-red-500"}`}
        >
          <Heart className={`w-5 h-5 ${isLiked ? "fill-current" : ""}`} />
          <span>{likeCount}</span>
        </button>
        <button
            onClick={(e) => { e.stopPropagation(); setShowComments(!showComments); }}
            className="flex items-center gap-1 text-gray-600 hover:text-blue-500 transition-colors text-sm"
        >
          <MessageCircle className="w-5 h-5" />
          <span>{commentCount}</span>
        </button>
        <button className="ml-auto text-gray-600 hover:text-gray-900 transition-colors">
          <Share className="w-5 h-5" />
        </button>
      </div>

      {/* 댓글 섹션 */}
      {showComments && (
          <div className="px-6 py-4 bg-gray-50 border-t border-gray-100" onClick={(e) => e.stopPropagation()}>
              <div className="space-y-3 mb-4 max-h-60 overflow-y-auto">
                  {comments?.map((comment) => (
                      <div key={comment.id} className="flex gap-2 text-sm group">
                          <span className="font-bold text-gray-900 flex-shrink-0">{comment.authorNickname}</span>
                          <span className="text-gray-700 break-all">{comment.content}</span>
                          <span className="text-xs text-gray-400 ml-auto flex-shrink-0 flex items-center gap-2">
                              {formatTimeAgo(new Date(comment.createdAt))}
                          </span>
                      </div>
                  ))}
                  {comments?.length === 0 && <p className="text-center text-gray-500 text-sm py-2">첫 댓글을 남겨보세요!</p>}
              </div>
              <form onSubmit={handleCommentSubmit} className="flex gap-2">
                  <Input
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    placeholder="댓글 달기..."
                    className="flex-1 bg-white h-9 text-sm focus-visible:ring-blue-500"
                  />
                  <Button type="submit" size="sm" disabled={createCommentMutation.isPending || !newComment.trim()} className="h-9 w-9 p-0 bg-blue-600 hover:bg-blue-700">
                      <Send className="w-4 h-4 text-white" />
                  </Button>
              </form>
          </div>
      )}
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
