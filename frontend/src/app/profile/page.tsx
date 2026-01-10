"use client";

import { useEffect, useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { useUserProfile } from "@/hooks/useUser";
import { useFollowStats, useFollowers, useFollowings } from "@/hooks/useFollow";
import { Skeleton } from "@/components/ui/skeleton";
import ProfileHeader from "@/components/profile/ProfileHeader";
import CertificationsSection from "@/components/profile/CertificationsSection";
import FollowButton from "@/components/follow/FollowButton";
import { User, ShieldCheck, Activity, Users, Heart, FileText, Medal } from "lucide-react";
import BadgesSection from "@/components/profile/BadgesSection";

function ProfileContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const initialTab = searchParams.get("tab") as "certifications" | "followers" | "followings" | "badges" | null;

  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const { data: user, isLoading, isError, error } = useUserProfile();
  
  const [activeTab, setActiveTab] = useState<"certifications" | "followers" | "followings" | "badges">(
    (initialTab && ["certifications", "followers", "followings", "badges"].includes(initialTab))
      ? initialTab
      : "certifications"
  );

  const [followersPage, setFollowersPage] = useState(0);
  const [followingsPage, setFollowingsPage] = useState(0);

  // 팔로워/팔로잉 데이터 조회
  const { followerCount = 0, followingCount = 0 } = useFollowStats(user?.loginId || "");

  const {
    data: followers,
    isLoading: isLoadingFollowers,
    isError: isErrorFollowers,
  } = useFollowers(user?.loginId || "", followersPage, 20);

  const {
    data: followings,
    isLoading: isLoadingFollowings,
    isError: isErrorFollowings,
  } = useFollowings(user?.loginId || "", followingsPage, 20);

  useEffect(() => {
    if (!isAuthenticated && !isLoading) {
      router.replace("/login");
    }
  }, [isAuthenticated, isLoading, router]);

  if (isLoading || !isAuthenticated || !user) {
    return <ProfilePageSkeleton />;
  }

  if (isError) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-b from-blue-50 via-white to-blue-50 p-4 text-red-500">
        <p>프로필 정보를 불러오는 데 실패했습니다: {error.message}</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50 dark:bg-gray-900">
      <ProfileHeader user={user} isOwnProfile={true} onFollowersClick={() => setActiveTab("followers")} onFollowingsClick={() => setActiveTab("followings")} />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* 탭 목록 - 개선된 가시성 */}
        <div className="flex flex-wrap gap-1 border-b-2 border-gray-200 dark:border-gray-700 mb-8">
          <button
            onClick={() => setActiveTab("certifications")}
            className={`flex items-center gap-2 px-5 py-3 font-semibold text-base transition-all border-b-4 rounded-t-lg ${
              activeTab === "certifications"
                ? "border-blue-600 text-blue-700 bg-blue-100 shadow-sm"
                : "border-transparent text-gray-700 hover:text-blue-600 hover:bg-gray-100"
            }`}
          >
            <FileText className="w-5 h-5" />
            인증
          </button>
          <button
            onClick={() => setActiveTab("followers")}
            className={`flex items-center gap-2 px-5 py-3 font-semibold text-base transition-all border-b-4 rounded-t-lg ${
              activeTab === "followers"
                ? "border-blue-600 text-blue-700 bg-blue-100 shadow-sm"
                : "border-transparent text-gray-700 hover:text-blue-600 hover:bg-gray-100"
            }`}
          >
            <User className="w-5 h-5" />
            팔로워 ({followerCount})
          </button>
          <button
            onClick={() => setActiveTab("followings")}
            className={`flex items-center gap-2 px-5 py-3 font-semibold text-base transition-all border-b-4 rounded-t-lg ${
              activeTab === "followings"
                ? "border-blue-600 text-blue-700 bg-blue-100 shadow-sm"
                : "border-transparent text-gray-700 hover:text-blue-600 hover:bg-gray-100"
            }`}
          >
            <Heart className="w-5 h-5" />
            팔로잉 ({followingCount})
          </button>
          <button
            onClick={() => setActiveTab("badges")}
            className={`flex items-center gap-2 px-5 py-3 font-semibold text-base transition-all border-b-4 rounded-t-lg ${
              activeTab === "badges"
                ? "border-blue-600 text-blue-700 bg-blue-100 shadow-sm"
                : "border-transparent text-gray-700 hover:text-blue-600 hover:bg-gray-100"
            }`}
          >
            <Medal className="w-5 h-5" />
            배지
          </button>
        </div>

        {/* 인증 탭 */}
        {activeTab === "certifications" && (
            <div>
              <CertificationsSection userLoginId={user.loginId} />
            </div>
        )}

        {/* 배지 탭 */}
        {activeTab === "badges" && (
            <div>
              <BadgesSection userLoginId={user.loginId} isOwnProfile={true} />
            </div>
        )}

        {/* 팔로워 탭 */}
        {activeTab === "followers" && (
            <div className="grid grid-cols-1 gap-6">
              {isLoadingFollowers ? (
                <div className="flex items-center justify-center h-40">
                  <div className="animate-spin">
                    <User className="w-8 h-8 text-blue-600" />
                  </div>
                </div>
              ) : isErrorFollowers ? (
                <div className="text-center py-12 text-red-500">
                  팔로워 목록을 불러올 수 없습니다.
                </div>
              ) : (followers?.length ?? 0) > 0 ? (
                <>
                  <div className="text-sm text-gray-700 font-medium">
                    총 <span className="font-bold text-blue-600">{followers?.length}</span>명의 팔로워
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {followers?.map((follower) => (
                      <div
                        key={follower.loginId}
                        className="overflow-hidden cursor-pointer hover:shadow-2xl transition-all duration-300 border-2 hover:border-blue-400 group bg-gradient-to-br from-white to-gray-50 dark:from-gray-800 dark:to-gray-700 rounded-lg"
                      >
                        <div className="p-4">
                          <div className="flex items-start justify-between mb-3">
                            <div
                              className="flex-1 hover:opacity-80 transition-opacity"
                              onClick={() => router.push(`/profile/${follower.loginId}`)}
                            >
                              <p className="font-bold text-lg text-gray-900 dark:text-white group-hover:text-blue-600 transition-colors line-clamp-1">{follower.nickname}</p>
                              <p className="text-sm text-gray-600 dark:text-gray-300">@{follower.loginId}</p>
                            </div>
                          </div>
                          <div className="flex gap-2 pt-2">
                            <button
                              onClick={() => router.push(`/profile/${follower.loginId}`)}
                              className="flex-1 px-3 py-2 text-sm font-semibold text-gray-700 hover:text-blue-600 hover:bg-blue-50 dark:text-gray-300 dark:hover:bg-gray-700 dark:hover:text-blue-400 rounded-lg transition-all border border-gray-300 dark:border-gray-600 hover:border-blue-400"
                            >
                              프로필
                            </button>
                            <div className="flex-1">
                              <FollowButton
                                targetLoginId={follower.loginId}
                                variant="outline"
                                size="sm"
                                initialIsFollowing={followings?.some(f => f.loginId === follower.loginId) ?? false}
                              />
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </>
              ) : (
                <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                  팔로워가 없습니다.
                </div>
              )}
            </div>
        )}

        {/* 팔로잉 탭 */}
        {activeTab === "followings" && (
            <div className="grid grid-cols-1 gap-6">
              {isLoadingFollowings ? (
                <div className="flex items-center justify-center h-40">
                  <div className="animate-spin">
                    <User className="w-8 h-8 text-blue-600" />
                  </div>
                </div>
              ) : isErrorFollowings ? (
                <div className="text-center py-12 text-red-500">
                  팔로잉 목록을 불러올 수 없습니다.
                </div>
              ) : (followings?.length ?? 0) > 0 ? (
                <>
                  <div className="text-sm text-gray-700 font-medium">
                    총 <span className="font-bold text-blue-600">{followings?.length}</span>명을 팔로우 중
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {followings?.map((following) => (
                      <div
                        key={following.loginId}
                        className="overflow-hidden cursor-pointer hover:shadow-2xl transition-all duration-300 border-2 hover:border-blue-400 group bg-gradient-to-br from-white to-gray-50 dark:from-gray-800 dark:to-gray-700 rounded-lg"
                      >
                        <div className="p-4">
                          <div className="flex items-start justify-between mb-3">
                            <div
                              className="flex-1 hover:opacity-80 transition-opacity"
                              onClick={() => router.push(`/profile/${following.loginId}`)}
                            >
                              <p className="font-bold text-lg text-gray-900 dark:text-white group-hover:text-blue-600 transition-colors line-clamp-1">{following.nickname}</p>
                              <p className="text-sm text-gray-600 dark:text-gray-300">@{following.loginId}</p>
                            </div>
                          </div>
                          <div className="flex gap-2 pt-2">
                            <button
                              onClick={() => router.push(`/profile/${following.loginId}`)}
                              className="flex-1 px-3 py-2 text-sm font-semibold text-gray-700 hover:text-blue-600 hover:bg-blue-50 dark:text-gray-300 dark:hover:bg-gray-700 dark:hover:text-blue-400 rounded-lg transition-all border border-gray-300 dark:border-gray-600 hover:border-blue-400"
                            >
                              프로필
                            </button>
                            <div className="flex-1">
                              <FollowButton
                                targetLoginId={following.loginId}
                                variant="outline"
                                size="sm"
                                initialIsFollowing={true}
                              />
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </>
              ) : (
                <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                  팔로잉이 없습니다.
                </div>
              )}
            </div>
        )}

      </main>
    </div>
  );
}

// --- Main Profile Page Component ---
export default function ProfilePage() {
  return (
    <Suspense fallback={<ProfilePageSkeleton />}>
      <ProfileContent />
    </Suspense>
  );
}

// --- Skeleton Component for the new layout ---
function ProfilePageSkeleton() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50 dark:bg-gray-900 animate-pulse">
      {/* Header Skeleton */}
      <header className="bg-white dark:bg-gray-800/50 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
          <div className="flex items-center space-x-6">
            <Skeleton className="h-24 w-24 rounded-full" />
            <div className="space-y-3">
              <Skeleton className="h-8 w-48" />
              <Skeleton className="h-5 w-64" />
            </div>
          </div>
        </div>
      </header>

      {/* Main Content Skeleton */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-12">
          {/* Left Column Skeleton */}
          <div className="lg:col-span-2 space-y-12">
            <div className="p-6 bg-white dark:bg-gray-800/50 rounded-xl shadow-sm">
              <Skeleton className="h-8 w-40 mb-6" />
              <div className="flex items-center justify-end mb-4">
                <Skeleton className="h-9 w-32" />
              </div>
              <Skeleton className="h-64 w-full" />
            </div>
          </div>

          {/* Right Column Skeleton */}
          <div className="space-y-8">
            <div className="p-6 bg-white dark:bg-gray-800/50 rounded-xl shadow-sm">
               <Skeleton className="h-7 w-32 mb-6" />
               <div className="space-y-4">
                 <Skeleton className="h-10 w-full" />
                 <Skeleton className="h-10 w-full" />
               </div>
            </div>
            <div className="p-6 bg-white dark:bg-gray-800/50 rounded-xl shadow-sm">
               <Skeleton className="h-7 w-40 mb-6" />
               <div className="space-y-4">
                 <Skeleton className="h-10 w-full" />
                 <Skeleton className="h-10 w-full" />
                 <Skeleton className="h-10 w-full" />
               </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
