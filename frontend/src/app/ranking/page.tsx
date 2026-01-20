"use client";

import RankingBoard from "@/components/ranking/RankingBoard";
import RankingSseProvider from "@/providers/RankingSseProvider";
import { pageHeaderStyles, layoutStyles } from "@/styles/common";

export default function RankingPage() {
  return (
    <RankingSseProvider enabled={true}>
      <div className={layoutStyles.pageRoot}>
        <div className={layoutStyles.containerMd}>
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
      </div>
    </RankingSseProvider>
  );
}
