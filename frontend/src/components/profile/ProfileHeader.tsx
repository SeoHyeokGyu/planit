"use client";

import { UserProfile } from "@/types/user";
import { useLogout } from "@/hooks/useAuth";
import { useFollowStats } from "@/hooks/useFollow";
import { Button } from "@/components/ui/button";
import { User, Calendar, LogOut, Heart } from "lucide-react";
import { componentStyles } from "@/styles/common";

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
  onFollowingsClick,
}: ProfileHeaderProps) {
  const logout = useLogout();
  const { followerCount, followingCount, isLoading } = useFollowStats(user.loginId);

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
              <div
                className={`${componentStyles.avatar.base} ${componentStyles.avatar.large} border-4 border-white/30`}
              >
                <User className="w-12 h-12 sm:w-16 sm:h-16 text-white" />
              </div>
              <div className="text-center sm:text-left">
                <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold tracking-tight">
                  {user.nickname}
                </h1>
                <p className="text-lg text-blue-100 dark:text-blue-200 mt-1">@{user.loginId}</p>
                <div className="flex items-center justify-center sm:justify-start space-x-2 text-sm text-blue-200 dark:text-blue-300 mt-2">
                  <Calendar className="w-4 h-4" />
                  <span>가입일: {new Date(user.createdAt).toLocaleDateString()}</span>
                </div>

                {/* 팔로워/팔로잉 통계 */}
                <div className="flex items-center justify-center sm:justify-start space-x-6 mt-4">
                  <button
                    onClick={handleFollowersClick}
                    disabled={isLoading}
                    className="flex items-center space-x-2 hover:opacity-80 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                  >
                    <Heart className="w-4 h-4" />
                    <div>
                      <p className="text-xl font-bold">{followerCount}</p>
                      <p className="text-xs text-blue-200">팔로워</p>
                    </div>
                  </button>
                  <button
                    onClick={handleFollowingsClick}
                    disabled={isLoading}
                    className="flex items-center space-x-2 hover:opacity-80 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
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

            {/* 자신의 프로필일 때만 로그아웃 버튼 표시 */}
            {isOwnProfile && (
              <div className="mt-6 sm:mt-0">
                <Button
                  onClick={logout}
                  variant="outline"
                  className="bg-transparent border-2 border-white text-white hover:bg-white hover:text-blue-700 transition-colors duration-200 group"
                >
                  <LogOut className="mr-2 h-4 w-4 group-hover:scale-110 transition-transform" />
                  로그아웃
                </Button>
              </div>
            )}
          </div>
        </div>
      </header>
    </>
  );
}
