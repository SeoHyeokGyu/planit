"use client";

import { useAuthStore } from "@/stores/authStore";
import DashboardHeader from "@/components/dashboard/DashboardHeader";
import DashboardStats from "@/components/dashboard/DashboardStats";
import DashboardRecentFeed from "@/components/dashboard/DashboardRecentFeed";
import { useEffect, useRef } from "react";
import { layoutStyles } from "@/styles/common";

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
    <div className={layoutStyles.pageRoot}>
      <main className={layoutStyles.containerXl}>
        <DashboardHeader />
        <DashboardStats />
        <DashboardRecentFeed />
      </main>
    </div>
  );
}
