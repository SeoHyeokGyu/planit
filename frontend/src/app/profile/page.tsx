"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { useUserProfile } from "@/hooks/useUser";
import { Skeleton } from "@/components/ui/skeleton";
import ProfileHeader from "@/components/profile/ProfileHeader";
import CertificationsSection from "@/components/profile/CertificationsSection";
import AccountSettingsSection from "@/components/profile/AccountSettingsSection";
import { User, ShieldCheck, Activity } from "lucide-react";

// --- Main Profile Page Component ---
export default function ProfilePage() {
  const router = useRouter();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const { data: user, isLoading, isError, error } = useUserProfile();

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
      <div className="flex min-h-screen items-center justify-center bg-gray-100 p-4 text-red-500">
        <p>프로필 정보를 불러오는 데 실패했습니다: {error.message}</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <ProfileHeader user={user} />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-12">
          {/* Left Column (Main Content) */}
          <div className="lg:col-span-2 space-y-12">
            <CertificationsSection userLoginId={user.loginId} />
          </div>

          {/* Right Column (Settings) */}
          <div className="space-y-8">
            <AccountSettingsSection user={user} />
          </div>
        </div>
      </main>
    </div>
  );
}

// --- Skeleton Component for the new layout ---
function ProfilePageSkeleton() {
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 animate-pulse">
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
