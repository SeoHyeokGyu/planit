"use client";

import { useUserProfile } from "@/hooks/useUser";
import { headerStyles, themeStyles } from "@/styles/common";

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
    <div className={headerStyles.wrapper}>
      <h1 className={`${headerStyles.title} ${themeStyles.primary.text} mb-2`}>
        안녕하세요, {userProfile?.nickname || "사용자"}님!
      </h1>
      <p className="text-gray-500 font-medium">오늘도 화이팅! 🚀</p>
    </div>
  );
}
