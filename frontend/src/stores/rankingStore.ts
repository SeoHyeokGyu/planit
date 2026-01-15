import { create } from "zustand";
import {
  RankingEntry,
  RankingPeriodType,
  RankingUpdateEvent,
} from "@/types/ranking";

interface RankingState {
  // Top 10 랭킹 (기간별)
  weeklyTop10: RankingEntry[];
  monthlyTop10: RankingEntry[];
  alltimeTop10: RankingEntry[];

  // SSE 연결 상태
  isConnected: boolean;
  connectedClients: number;

  // 마지막 업데이트 정보
  lastUpdate: RankingUpdateEvent | null;

  // Actions
  setTop10: (periodType: RankingPeriodType, rankings: RankingEntry[]) => void;
  updateFromSseEvent: (event: RankingUpdateEvent) => void;
  setConnectionStatus: (isConnected: boolean, clients?: number) => void;
  clearLastUpdate: () => void;
}

export const useRankingStore = create<RankingState>((set, get) => ({
  weeklyTop10: [],
  monthlyTop10: [],
  alltimeTop10: [],
  isConnected: false,
  connectedClients: 0,
  lastUpdate: null,

  setTop10: (periodType, rankings) => {
    switch (periodType) {
      case "WEEKLY":
        set({ weeklyTop10: rankings });
        break;
      case "MONTHLY":
        set({ monthlyTop10: rankings });
        break;
      case "ALLTIME":
        set({ alltimeTop10: rankings });
        break;
    }
  },

  updateFromSseEvent: (event) => {
    const { periodType, top10 } = event;

    // Top 10 업데이트
    switch (periodType) {
      case "WEEKLY":
        set({ weeklyTop10: top10, lastUpdate: event });
        break;
      case "MONTHLY":
        set({ monthlyTop10: top10, lastUpdate: event });
        break;
      case "ALLTIME":
        set({ alltimeTop10: top10, lastUpdate: event });
        break;
    }
  },

  setConnectionStatus: (isConnected, clients) => {
    set({
      isConnected,
      ...(clients !== undefined && { connectedClients: clients }),
    });
  },

  clearLastUpdate: () => {
    set({ lastUpdate: null });
  },
}));
