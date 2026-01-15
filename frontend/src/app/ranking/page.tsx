"use client";

import RankingBoard from "@/components/ranking/RankingBoard";
import RankingSseProvider from "@/providers/RankingSseProvider";
import { pageHeaderStyles } from "@/styles/pageHeader";

export default function RankingPage() {
  return (
    <RankingSseProvider enabled={true}>
      <div className="container mx-auto px-4 py-6 max-w-2xl">
        {/* 페이지 헤더 */}
        <div className={pageHeaderStyles.container}>
          <h1 className={pageHeaderStyles.title}>랭킹</h1>
          <p className={pageHeaderStyles.description}>
            포인트를 획득하여 순위를 올려보세요!
          </p>
        </div>

        {/* 랭킹 보드 */}
        <RankingBoard />
      </div>
    </RankingSseProvider>
  );
}
