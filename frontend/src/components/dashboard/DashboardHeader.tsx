"use client";

import { useUserProfile } from "@/hooks/useUser";

export default function DashboardHeader() {
  const { data: userProfile, isLoading } = useUserProfile();

  if (isLoading) {
    return (
      <div className="mb-8 h-20 flex flex-col justify-center">
        <div className="h-8 w-64 bg-gray-200 animate-pulse rounded-md mb-2"></div>
        <div className="h-4 w-32 bg-gray-200 animate-pulse rounded-md"></div>
      </div>
    );
  }

  return (
    <div className="mb-8">
      <h1 className="text-4xl font-bold bg-linear-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-2">
        안녕하세요, {userProfile?.nickname || "사용자"}님!
      </h1>
      <p className="text-gray-500">오늘도 화이팅! 🚀</p>
    </div>
  );
}
