"use client";

import { useState } from "react";
import Image from "next/image";
import { Heart, Trash2 } from "lucide-react";
import { FeedEvent } from "@/services/feedService";
import { useLikeFeed, useDeleteFeed } from "@/hooks/useFeed";
import { useAuthStore } from "@/stores/authStore";

interface FeedItemProps {
  feed: FeedEvent;
}

export function FeedItem({ feed }: FeedItemProps) {
  const [isLiking, setIsLiking] = useState(false);
  const userId = useAuthStore((state) => state.userId);
  const { mutate: toggleLike } = useLikeFeed();
  const { mutate: deleteFeed } = useDeleteFeed();

  const handleLike = async () => {
    setIsLiking(true);
    try {
      toggleLike(feed.id);
    } finally {
      setIsLiking(false);
    }
  };

  const handleDelete = () => {
    if (confirm("이 피드를 삭제하시겠습니까?")) {
      deleteFeed(feed.id);
    }
  };

  const isOwner = userId === feed.userId;
  const formattedDate = new Date(feed.createdAt).toLocaleDateString("ko-KR", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });

  return (
    <div className="bg-white rounded-xl shadow-sm p-6 mb-4">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white font-semibold">
            {feed.userNickname.charAt(0).toUpperCase()}
          </div>
          <div>
            <p className="font-semibold text-gray-900">{feed.userNickname}</p>
            <p className="text-xs text-gray-500">{formattedDate}</p>
          </div>
        </div>

        {isOwner && (
          <button
            onClick={handleDelete}
            className="text-gray-400 hover:text-red-500 transition-colors"
            title="삭제"
          >
            <Trash2 size={18} />
          </button>
        )}
      </div>

      {/* Challenge Info */}
      <div className="mb-3 p-3 bg-blue-50 rounded-lg">
        <p className="text-sm font-medium text-blue-900">
          🎯 {feed.challengeName}
        </p>
      </div>

      {/* Message */}
      {feed.message && (
        <p className="text-gray-700 mb-4">{feed.message}</p>
      )}

      {/* Image */}
      {feed.certificationImageUrl && (
        <div className="relative w-full h-96 mb-4 rounded-lg overflow-hidden bg-gray-100">
          <Image
            src={feed.certificationImageUrl}
            alt="인증 이미지"
            fill
            className="object-cover"
          />
        </div>
      )}

      {/* Footer - Like & Stats */}
      <div className="flex items-center gap-4 pt-4 border-t border-gray-100">
        <button
          onClick={handleLike}
          disabled={isLiking}
          className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
            feed.isLiked
              ? "bg-red-50 text-red-600"
              : "text-gray-600 hover:bg-gray-50"
          }`}
        >
          <Heart
            size={18}
            fill={feed.isLiked ? "currentColor" : "none"}
          />
          <span className="text-sm font-medium">{feed.likesCount}</span>
        </button>
      </div>
    </div>
  );
}
