"use client";

import { useEffect, useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { useUserProfile } from "@/hooks/useUser";
import { useFollowStats, useFollowers, useFollowings } from "@/hooks/useFollow";
import { useUserBadges } from "@/hooks/useBadge";
import { useAllStreaks } from "@/hooks/useStreak";
import { useCertificationsByUser } from "@/hooks/useCertification";
import { useMyRankings } from "@/hooks/useRanking";
import { Skeleton } from "@/components/ui/skeleton";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import ProfileHeader from "@/components/profile/ProfileHeader";
import CertificationsSection from "@/components/profile/CertificationsSection";
import StreaksSection from "@/components/profile/StreaksSection";
import FollowButton from "@/components/follow/FollowButton";
import { User, FileText, Medal, Flame, Crown, Calendar, TrendingUp, Award } from "lucide-react";
import BadgesSection from "@/components/profile/BadgesSection";

function ProfileContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const initialTab = searchParams.get("tab") as "certifications" | "badges" | "streaks" | null;

  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const { data: user, isLoading, isError, error } = useUserProfile();

  const [activeTab, setActiveTab] = useState<"certifications" | "badges" | "streaks">(
      (initialTab && ["certifications", "badges", "streaks"].includes(initialTab))
          ? initialTab
          : "certifications"
  );

  // 팔로워/팔로잉 모달 상태
  const [showFollowersModal, setShowFollowersModal] = useState(false);
  const [showFollowingsModal, setShowFollowingsModal] = useState(false);

  // 팔로워/팔로잉 데이터 조회
  const { followerCount = 0, followingCount = 0 } = useFollowStats(user?.loginId || "");

  const {
    data: followers,
    isLoading: isLoadingFollowers,
    isError: isErrorFollowers,
  } = useFollowers(user?.loginId || "", 0, 50);

  const {
    data: followings,
    isLoading: isLoadingFollowings,
    isError: isErrorFollowings,
  } = useFollowings(user?.loginId || "", 0, 50);

  // Quick Stats 데이터 조회
  const { data: badges } = useUserBadges(user?.loginId || "");
  const { data: streakSummary } = useAllStreaks(user?.loginId || "");
  const { data: certifications } = useCertificationsByUser(user?.loginId || "", 0, 1);
  const { data: myRankings } = useMyRankings();

  // Quick Stats 계산
  const acquiredBadgesCount = badges?.filter((b) => b.isAcquired).length || 0;
  const totalBadgesCount = badges?.length || 0;
  const currentStreak = streakSummary?.totalCurrentStreak || 0;
  const totalCertifications = certifications?.totalElements || 0;
  const weeklyRank = myRankings?.weekly?.rank || null;

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
      <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
        <ProfileHeader
          user={user}
          isOwnProfile={true}
          onFollowersClick={() => setShowFollowersModal(true)}
          onFollowingsClick={() => setShowFollowingsModal(true)}
        />

        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Quick Stats Summary */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
            <Card
              className="bg-gradient-to-br from-orange-50 to-orange-100 border-orange-200 cursor-pointer hover:shadow-lg transition-shadow"
              onClick={() => setActiveTab("streaks")}
            >
              <CardContent className="p-4 flex items-center gap-3">
                <div className="w-12 h-12 bg-gradient-to-br from-orange-500 to-red-500 rounded-xl flex items-center justify-center text-white shadow-md">
                  <Flame className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-2xl font-bold text-orange-700">{currentStreak}일</p>
                  <p className="text-xs text-orange-600 font-medium">현재 스트릭</p>
                </div>
              </CardContent>
            </Card>

            <Card
              className="bg-gradient-to-br from-purple-50 to-purple-100 border-purple-200 cursor-pointer hover:shadow-lg transition-shadow"
              onClick={() => setActiveTab("badges")}
            >
              <CardContent className="p-4 flex items-center gap-3">
                <div className="w-12 h-12 bg-gradient-to-br from-purple-500 to-pink-500 rounded-xl flex items-center justify-center text-white shadow-md">
                  <Medal className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-2xl font-bold text-purple-700">{acquiredBadgesCount}개</p>
                  <p className="text-xs text-purple-600 font-medium">획득 배지</p>
                </div>
              </CardContent>
            </Card>

            <Card
              className="bg-gradient-to-br from-blue-50 to-blue-100 border-blue-200 cursor-pointer hover:shadow-lg transition-shadow"
              onClick={() => setActiveTab("certifications")}
            >
              <CardContent className="p-4 flex items-center gap-3">
                <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-xl flex items-center justify-center text-white shadow-md">
                  <FileText className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-2xl font-bold text-blue-700">{totalCertifications}개</p>
                  <p className="text-xs text-blue-600 font-medium">총 인증</p>
                </div>
              </CardContent>
            </Card>

            <Card
              className="bg-gradient-to-br from-yellow-50 to-amber-100 border-yellow-200 cursor-pointer hover:shadow-lg transition-shadow"
              onClick={() => router.push("/ranking")}
            >
              <CardContent className="p-4 flex items-center gap-3">
                <div className="w-12 h-12 bg-gradient-to-br from-yellow-500 to-amber-500 rounded-xl flex items-center justify-center text-white shadow-md">
                  <Crown className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-2xl font-bold text-yellow-700">
                    {weeklyRank ? `${weeklyRank}위` : "-"}
                  </p>
                  <p className="text-xs text-yellow-600 font-medium">주간 랭킹</p>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* 2-Column Layout */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Left Column - Tab Content (2/3 width) */}
            <div className="lg:col-span-2">
              {/* Sticky Tabs */}
              <div
                className="sticky top-16 z-20 bg-gradient-to-b from-blue-50 via-white to-transparent pt-2 pb-4 -mx-4 px-4 sm:-mx-6 sm:px-6 lg:-mx-8 lg:px-8"
              >
                <div
                  className="flex gap-1 border-b-2 border-gray-200 dark:border-gray-700 bg-white rounded-t-lg shadow-sm"
                  role="tablist"
                  aria-label="프로필 탭"
                >
                  <button
                      onClick={() => setActiveTab("certifications")}
                      className={`flex items-center gap-2 px-5 py-3 font-semibold text-base transition-all border-b-4 rounded-t-lg ${
                          activeTab === "certifications"
                              ? "border-blue-600 text-blue-700 bg-blue-100 shadow-sm"
                              : "border-transparent text-gray-700 hover:text-blue-600 hover:bg-gray-100"
                      }`}
                      role="tab"
                      aria-selected={activeTab === "certifications"}
                      aria-controls="tab-certifications"
                  >
                    <FileText className="w-5 h-5" />
                    인증
                  </button>
                  <button
                      onClick={() => setActiveTab("badges")}
                      className={`flex items-center gap-2 px-5 py-3 font-semibold text-base transition-all border-b-4 rounded-t-lg ${
                          activeTab === "badges"
                              ? "border-blue-600 text-blue-700 bg-blue-100 shadow-sm"
                              : "border-transparent text-gray-700 hover:text-blue-600 hover:bg-gray-100"
                      }`}
                      role="tab"
                      aria-selected={activeTab === "badges"}
                      aria-controls="tab-badges"
                  >
                    <Medal className="w-5 h-5" />
                    배지
                  </button>
                  <button
                      onClick={() => setActiveTab("streaks")}
                      className={`flex items-center gap-2 px-5 py-3 font-semibold text-base transition-all border-b-4 rounded-t-lg ${
                          activeTab === "streaks"
                              ? "border-orange-600 text-orange-700 bg-orange-100 shadow-sm"
                              : "border-transparent text-gray-700 hover:text-orange-600 hover:bg-gray-100"
                      }`}
                      role="tab"
                      aria-selected={activeTab === "streaks"}
                      aria-controls="tab-streaks"
                  >
                    <Flame className="w-5 h-5" />
                    스트릭
                  </button>
                </div>
              </div>

              {/* Tab Content */}
              <div className="mt-4">
                {activeTab === "certifications" && (
                    <div id="tab-certifications" role="tabpanel" aria-labelledby="certifications-tab">
                      <CertificationsSection userLoginId={user.loginId} />
                    </div>
                )}

                {activeTab === "badges" && (
                    <div id="tab-badges" role="tabpanel" aria-labelledby="badges-tab">
                      <BadgesSection userLoginId={user.loginId} isOwnProfile={true} />
                    </div>
                )}

                {activeTab === "streaks" && (
                    <div id="tab-streaks" role="tabpanel" aria-labelledby="streaks-tab">
                      <StreaksSection userLoginId={user.loginId} isOwnProfile={true} />
                    </div>
                )}
              </div>
            </div>

            {/* Right Column - Sidebar (1/3 width) */}
            <div className="space-y-6">
              {/* User Info Card */}
              <Card className="shadow-lg rounded-xl bg-white">
                <CardHeader className="pb-3">
                  <CardTitle className="text-lg font-bold text-gray-900 flex items-center gap-2">
                    <User className="w-5 h-5 text-blue-600" />
                    사용자 정보
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <p className="text-sm text-gray-500">이름</p>
                    <p className="font-semibold text-gray-900">{user.nickname}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">아이디</p>
                    <p className="font-semibold text-gray-900">@{user.loginId}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">가입일</p>
                    <p className="font-semibold text-gray-900 flex items-center gap-2">
                      <Calendar className="w-4 h-4 text-gray-400" />
                      {new Date(user.createdAt).toLocaleDateString("ko-KR", {
                        year: "numeric",
                        month: "long",
                        day: "numeric"
                      })}
                    </p>
                  </div>
                </CardContent>
              </Card>

              {/* Achievement Summary Card */}
              <Card className="shadow-lg rounded-xl bg-white">
                <CardHeader className="pb-3">
                  <CardTitle className="text-lg font-bold text-gray-900 flex items-center gap-2">
                    <Award className="w-5 h-5 text-purple-600" />
                    달성 현황
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">최장 스트릭</span>
                    <span className="font-bold text-orange-600">
                      {streakSummary?.maxLongestStreak || 0}일
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">활성 챌린지</span>
                    <span className="font-bold text-blue-600">
                      {streakSummary?.activeStreakCount || 0}개
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">획득 배지</span>
                    <span className="font-bold text-purple-600">
                      {acquiredBadgesCount} / {totalBadgesCount}개
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">월간 랭킹</span>
                    <span className="font-bold text-yellow-600">
                      {myRankings?.monthly?.rank ? `${myRankings.monthly.rank}위` : "-"}
                    </span>
                  </div>
                </CardContent>
              </Card>

              {/* Quick Links Card */}
              <Card className="shadow-lg rounded-xl bg-white">
                <CardHeader className="pb-3">
                  <CardTitle className="text-lg font-bold text-gray-900 flex items-center gap-2">
                    <TrendingUp className="w-5 h-5 text-green-600" />
                    바로가기
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-2">
                  <button
                    onClick={() => router.push("/dashboard")}
                    className="w-full text-left px-4 py-3 rounded-lg bg-gray-50 hover:bg-blue-50 hover:text-blue-600 font-medium transition-colors"
                  >
                    대시보드
                  </button>
                  <button
                    onClick={() => router.push("/challenge")}
                    className="w-full text-left px-4 py-3 rounded-lg bg-gray-50 hover:bg-blue-50 hover:text-blue-600 font-medium transition-colors"
                  >
                    챌린지 참여하기
                  </button>
                  <button
                    onClick={() => router.push("/settings")}
                    className="w-full text-left px-4 py-3 rounded-lg bg-gray-50 hover:bg-blue-50 hover:text-blue-600 font-medium transition-colors"
                  >
                    설정
                  </button>
                </CardContent>
              </Card>
            </div>
          </div>
        </main>

        {/* 팔로워 모달 */}
        <Dialog open={showFollowersModal} onOpenChange={setShowFollowersModal}>
          <DialogContent className="max-w-lg max-h-[80vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle className="text-xl font-bold">팔로워 ({followerCount})</DialogTitle>
            </DialogHeader>
            <div className="space-y-3 mt-4">
              {isLoadingFollowers ? (
                <div className="flex items-center justify-center py-8">
                  <div className="animate-spin">
                    <User className="w-8 h-8 text-blue-600" />
                  </div>
                </div>
              ) : isErrorFollowers ? (
                <div className="text-center py-8 text-red-500">
                  팔로워 목록을 불러올 수 없습니다.
                </div>
              ) : (followers?.length ?? 0) > 0 ? (
                followers?.map((follower) => (
                  <div
                    key={follower.loginId}
                    className="flex items-center justify-between p-3 rounded-lg border border-gray-200 hover:border-blue-400 hover:bg-blue-50 transition-all"
                  >
                    <div
                      className="flex-1 cursor-pointer"
                      onClick={() => {
                        setShowFollowersModal(false);
                        router.push(`/profile/${follower.loginId}`);
                      }}
                    >
                      <p className="font-semibold text-gray-900">{follower.nickname}</p>
                      <p className="text-sm text-gray-500">@{follower.loginId}</p>
                    </div>
                    <FollowButton
                      targetLoginId={follower.loginId}
                      variant="outline"
                      size="sm"
                      initialIsFollowing={followings?.some(f => f.loginId === follower.loginId) ?? false}
                    />
                  </div>
                ))
              ) : (
                <div className="text-center py-8 text-gray-500">
                  팔로워가 없습니다.
                </div>
              )}
            </div>
          </DialogContent>
        </Dialog>

        {/* 팔로잉 모달 */}
        <Dialog open={showFollowingsModal} onOpenChange={setShowFollowingsModal}>
          <DialogContent className="max-w-lg max-h-[80vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle className="text-xl font-bold">팔로잉 ({followingCount})</DialogTitle>
            </DialogHeader>
            <div className="space-y-3 mt-4">
              {isLoadingFollowings ? (
                <div className="flex items-center justify-center py-8">
                  <div className="animate-spin">
                    <User className="w-8 h-8 text-blue-600" />
                  </div>
                </div>
              ) : isErrorFollowings ? (
                <div className="text-center py-8 text-red-500">
                  팔로잉 목록을 불러올 수 없습니다.
                </div>
              ) : (followings?.length ?? 0) > 0 ? (
                followings?.map((following) => (
                  <div
                    key={following.loginId}
                    className="flex items-center justify-between p-3 rounded-lg border border-gray-200 hover:border-blue-400 hover:bg-blue-50 transition-all"
                  >
                    <div
                      className="flex-1 cursor-pointer"
                      onClick={() => {
                        setShowFollowingsModal(false);
                        router.push(`/profile/${following.loginId}`);
                      }}
                    >
                      <p className="font-semibold text-gray-900">{following.nickname}</p>
                      <p className="text-sm text-gray-500">@{following.loginId}</p>
                    </div>
                    <FollowButton
                      targetLoginId={following.loginId}
                      variant="outline"
                      size="sm"
                      initialIsFollowing={true}
                    />
                  </div>
                ))
              ) : (
                <div className="text-center py-8 text-gray-500">
                  팔로잉이 없습니다.
                </div>
              )}
            </div>
          </DialogContent>
        </Dialog>
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
      <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50 animate-pulse">
        {/* Header Skeleton */}
        <header className="bg-white shadow-sm">
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
              <div className="p-6 bg-white rounded-xl shadow-sm">
                <Skeleton className="h-8 w-40 mb-6" />
                <div className="flex items-center justify-end mb-4">
                  <Skeleton className="h-9 w-32" />
                </div>
                <Skeleton className="h-64 w-full" />
              </div>
            </div>

            {/* Right Column Skeleton */}
            <div className="space-y-8">
              <div className="p-6 bg-white rounded-xl shadow-sm">
                <Skeleton className="h-7 w-32 mb-6" />
                <div className="space-y-4">
                  <Skeleton className="h-10 w-full" />
                  <Skeleton className="h-10 w-full" />
                </div>
              </div>
              <div className="p-6 bg-white rounded-xl shadow-sm">
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