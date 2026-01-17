"use client";

import { UserProfile } from "@/types/user";
import { useFollowStats } from "@/hooks/useFollow";
import { User, Calendar, Heart, Users } from "lucide-react";

interface ProfileHeaderProps {
  user: UserProfile;
  isOwnProfile?: boolean;
  onFollowersClick?: () => void;
  onFollowingsClick?: () => void;
}

export default function ProfileHeader({
  user,
  isOwnProfile = false,
  onFollowersClick,
  onFollowingsClick
}: ProfileHeaderProps) {
  const { followerCount, followingCount, isLoading } = useFollowStats(
    user.loginId
  );

  const handleFollowersClick = () => {
    if (onFollowersClick) {
      onFollowersClick();
    }
  };

  const handleFollowingsClick = () => {
    if (onFollowingsClick) {
      onFollowingsClick();
    }
  };

  return (
    <>
      <header className="relative bg-gradient-to-r from-blue-600 to-purple-600 dark:from-blue-800 dark:to-purple-800 shadow-lg overflow-hidden">
        <div className="absolute inset-0 bg-black/20"></div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 text-white">
          <div className="flex flex-col sm:flex-row items-center justify-between">
            <div className="flex flex-col sm:flex-row items-center space-y-4 sm:space-y-0 sm:space-x-6">
              <div className="w-24 h-24 sm:w-28 sm:h-28 bg-white/20 rounded-full flex items-center justify-center border-4 border-white/30">
                <User className="w-12 h-12 sm:w-16 sm:h-16 text-white" />
              </div>
              <div className="text-center sm:text-left">
                <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold tracking-tight">
                  {user.nickname}
                </h1>
                <p className="text-lg text-blue-100 dark:text-blue-200 mt-1">
                  @{user.loginId}
                </p>
                <div className="flex items-center justify-center sm:justify-start space-x-2 text-sm text-blue-200 dark:text-blue-300 mt-2">
                  <Calendar className="w-4 h-4" />
                  <span>
                    가입일: {new Date(user.createdAt).toLocaleDateString()}
                  </span>
                </div>

                {/* 팔로워/팔로잉 통계 */}
                <div className="flex items-center justify-center sm:justify-start space-x-6 mt-4">
                  <button
                    onClick={handleFollowersClick}
                    disabled={isLoading}
                    className="flex items-center space-x-2 hover:opacity-80 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                    aria-label={`팔로워 ${followerCount}명 보기`}
                  >
                    <Users className="w-4 h-4" />
                    <div>
                      <p className="text-xl font-bold">{followerCount}</p>
                      <p className="text-xs text-blue-200">팔로워</p>
                    </div>
                  </button>
                  <button
                    onClick={handleFollowingsClick}
                    disabled={isLoading}
                    className="flex items-center space-x-2 hover:opacity-80 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                    aria-label={`팔로잉 ${followingCount}명 보기`}
                  >
                    <Heart className="w-4 h-4" />
                    <div>
                      <p className="text-xl font-bold">{followingCount}</p>
                      <p className="text-xs text-blue-200">팔로잉</p>
                    </div>
                  </button>
                </div>
              </div>
            </div>

          </div>
        </div>
      </header>
    </>
  );
}
