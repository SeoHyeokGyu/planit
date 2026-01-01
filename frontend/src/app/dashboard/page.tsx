"use client";

import { useAuthStore } from "@/stores/authStore";
import DashboardHeader from "@/components/dashboard/DashboardHeader";
import DashboardStats from "@/components/dashboard/DashboardStats";
import DashboardRecentFeed from "@/components/dashboard/DashboardRecentFeed";
import { useEffect, useRef } from "react";

export default function DashboardPage() {
  const renderCount = useRef(0);
  useEffect(() => {
    renderCount.current++;

    // 렌더링 최적화 확인을 위한 로그
    console.log(`[DashboardPage] Container Render #${renderCount.current}`);
  }, [renderCount]);

  const token = useAuthStore((state) => state.token);

  if (!token) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <main className="max-w-7xl mx-auto px-4 py-8">
        <DashboardHeader />
        <DashboardStats />
        <DashboardRecentFeed />
      </main>
    </div>
  );
}
