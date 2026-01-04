"use client";

import { useEffect } from "react";
import React from "react";
import { useRouter } from "next/navigation";
import BadgesSection from "@/components/profile/BadgesSection";
import CertificationsSection from "@/components/profile/CertificationsSection";
import ProfileHeader from "@/components/profile/ProfileHeader";
import FollowButton from "@/components/follow/FollowButton";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useUserProfile } from "@/hooks/useUser";
import { ArrowLeft, User } from "lucide-react";

interface ProfilePageProps {
  params: Promise<{
    loginId: string;
  }>;
}

/**
 * 다른 사용자의 프로필 페이지
 * URL: /profile/[loginId]
 */
export default function OtherUserProfilePage({
  params,
}: ProfilePageProps) {
  const router = useRouter();
  const resolvedParams = React.use(params);
  const { data: user, isLoading, isError } = useUserProfile(resolvedParams.loginId);
  const { data: currentUser } = useUserProfile(); // 현재 로그인한 사용자 프로필

  // 자신의 프로필이면 /profile로 리다이렉트
  useEffect(() => {
    // 현재 사용자 정보가 로드되고 resolvedParams.loginId와 같으면 리다이렉트
    if (currentUser && currentUser.loginId === resolvedParams.loginId) {
      router.push("/profile");
    }
  }, [currentUser, resolvedParams.loginId, router]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50 dark:bg-gray-900">
        <div className="relative bg-gradient-to-r from-blue-600 to-purple-600 dark:from-blue-800 dark:to-purple-800 shadow-lg h-64">
          <Skeleton className="w-full h-full" />
        </div>
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <Skeleton className="h-96 w-full" />
        </main>
      </div>
    );
  }

  if (isError || !user) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50 dark:bg-gray-900 flex items-center justify-center">
        <Card className="max-w-md">
          <CardHeader>
            <CardTitle className="text-red-600">사용자를 찾을 수 없습니다</CardTitle>
            <CardDescription>
              요청하신 사용자가 존재하지 않습니다.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button
              onClick={() => router.push("/profile")}
              className="w-full"
            >
              <ArrowLeft className="w-4 h-4 mr-2" />
              내 프로필로 돌아가기
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50 dark:bg-gray-900">
      {/* 페이지 헤더 */}
      <div className="mb-6 pt-6 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <Button
          variant="ghost"
          onClick={() => router.back()}
          className="mb-4 hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-700 dark:text-gray-300 font-medium"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          돌아가기
        </Button>
        <div className="flex items-center gap-3 mb-3">
          <div className="w-10 h-10 bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg flex items-center justify-center text-white">
            <User className="w-6 h-6" />
          </div>
          <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
            {user.nickname}님의 프로필
          </h1>
        </div>
      </div>

      {/* 프로필 헤더 */}
      <div className="relative">
        <ProfileHeader user={user} />

        {/* 팔로우 버튼 - 헤더 위에 오버레이 */}
        <div className="absolute top-16 right-6 z-10">
          <FollowButton targetLoginId={user.loginId} variant="default" size="lg" />
        </div>
      </div>

      {/* 메인 콘텐츠 */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-12">
          {/* 왼쪽: 인증 기록 및 배지 */}
          <div className="lg:col-span-2 space-y-12">
            <BadgesSection userLoginId={user.loginId} isOwnProfile={false} />
            <CertificationsSection userLoginId={user.loginId} />
          </div>

          {/* 오른쪽: 정보 */}
          <div>
            <Card className="shadow-lg rounded-xl dark:bg-gray-800/50">
              <CardHeader>
                <CardTitle>사용자 정보</CardTitle>
                <CardDescription>
                  {user.nickname}님의 정보입니다.
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <p className="text-sm text-gray-500 dark:text-gray-400">이름</p>
                  <p className="font-semibold text-gray-900 dark:text-white">
                    {user.nickname}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-gray-500 dark:text-gray-400">아이디</p>
                  <p className="font-semibold text-gray-900 dark:text-white">
                    @{user.loginId}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-gray-500 dark:text-gray-400">가입일</p>
                  <p className="font-semibold text-gray-900 dark:text-white">
                    {new Date(user.createdAt).toLocaleDateString()}
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </div>
  );
}
