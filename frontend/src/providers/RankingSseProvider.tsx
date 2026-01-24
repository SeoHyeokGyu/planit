"use client";

import { useEffect, useRef, useCallback } from "react";
import { useAuthStore } from "@/stores/authStore";
import { useRankingStore } from "@/stores/rankingStore";
import { useInvalidateRanking, periodTypeToTab } from "@/hooks/useRanking";
import { RankingUpdateEvent, SseConnectionStatus } from "@/types/ranking";
import { toast } from "sonner";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
const SSE_ENDPOINT = "/api/rankings/stream";

interface RankingSseProviderProps {
  children: React.ReactNode;
  enabled?: boolean; // SSE 활성화 여부 (특정 페이지에서만 사용)
}

export default function RankingSseProvider({
  children,
  enabled = true,
}: RankingSseProviderProps) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const token = useAuthStore((state) => state.token);
  const currentLoginId = useAuthStore((state) => state.loginId);
  const updateFromSseEvent = useRankingStore(
    (state) => state.updateFromSseEvent
  );
  const setConnectionStatus = useRankingStore(
    (state) => state.setConnectionStatus
  );
  const { invalidateByType } = useInvalidateRanking();

  const eventSourceRef = useRef<EventSource | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const reconnectAttempts = useRef(0);
  const maxReconnectAttempts = 5;

  const connect = useCallback(() => {
    if (!enabled || !token) return;

    // 기존 연결 정리
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    console.log("Ranking SSE: Connecting...");
    const url = `${API_BASE_URL}${SSE_ENDPOINT}?token=${token}`;
    const eventSource = new EventSource(url, { withCredentials: true });
    eventSourceRef.current = eventSource;

    eventSource.onopen = () => {
      console.log("Ranking SSE: Connected");
      reconnectAttempts.current = 0;
      setConnectionStatus(true);
    };

    // 연결 성공 이벤트
    eventSource.addEventListener("connect", (event) => {
      try {
        const data: SseConnectionStatus = JSON.parse(event.data);
        console.log("Ranking SSE: Connection confirmed", data);
        setConnectionStatus(true, data.connectedClients);
      } catch (error) {
        console.error("Ranking SSE: Failed to parse connect event", error);
      }
    });

    // 랭킹 업데이트 이벤트
    eventSource.addEventListener("ranking", (event) => {
      try {
        const data: RankingUpdateEvent = JSON.parse(event.data);
        console.log("Ranking SSE: Received ranking update", data);

        // Store 업데이트
        updateFromSseEvent(data);

        // TanStack Query 캐시 무효화
        invalidateByType(periodTypeToTab(data.periodType));

        // 현재 사용자가 업데이트된 경우 토스트 알림
        if (
          data.updatedUser &&
          data.updatedUser.loginId === currentLoginId &&
          data.eventType === "RANKING_UPDATE"
        ) {
          const { previousRank, currentRank, scoreDelta } = data.updatedUser;

          if (previousRank && currentRank < previousRank) {
            // 순위 상승
            toast.success(
              `축하합니다! 순위가 ${previousRank}위에서 ${currentRank}위로 올랐습니다! (+${scoreDelta}점)`,
              { duration: 5000 }
            );
          } else if (previousRank && currentRank > previousRank) {
            // 순위 하락
            toast.info(
              `순위가 ${previousRank}위에서 ${currentRank}위로 변경되었습니다.`,
              { duration: 3000 }
            );
          } else if (!previousRank && currentRank <= 10) {
            // 새로 Top 10 진입
            toast.success(
              `Top 10에 진입했습니다! 현재 ${currentRank}위입니다!`,
              { duration: 5000 }
            );
          }
        }
      } catch (error) {
        console.error("Ranking SSE: Failed to parse ranking event", error);
      }
    });

    // Heartbeat 이벤트 (연결 유지 확인)
    eventSource.addEventListener("heartbeat", () => {
      // Heartbeat는 조용히 처리
    });

    eventSource.onerror = (error) => {
      console.error("Ranking SSE: Connection error", error);
      setConnectionStatus(false);

      // 자동 재연결 시도
      if (reconnectAttempts.current < maxReconnectAttempts) {
        const delay = Math.min(1000 * Math.pow(2, reconnectAttempts.current), 30000);
        console.log(`Ranking SSE: Reconnecting in ${delay}ms...`);

        reconnectTimeoutRef.current = setTimeout(() => {
          reconnectAttempts.current++;
          connect();
        }, delay);
      } else {
        console.log("Ranking SSE: Max reconnect attempts reached");
        eventSource.close();
      }
    };
  }, [
    enabled,
    token,
    currentLoginId,
    updateFromSseEvent,
    setConnectionStatus,
    invalidateByType,
  ]);

  useEffect(() => {
    if (enabled) {
      connect();
    }

    return () => {
      console.log("Ranking SSE: Cleanup");
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
        reconnectTimeoutRef.current = null;
      }
      setConnectionStatus(false);
    };
  }, [enabled, connect, setConnectionStatus]);

  return <>{children}</>;
}
