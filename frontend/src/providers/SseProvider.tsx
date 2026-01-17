"use client";

import { useEffect } from "react";
import { useAuthStore } from "@/stores/authStore";
import { useNotificationStore } from "@/stores/notificationStore";

// 백엔드의 SSE 구독 엔드포인트
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
const SSE_ENDPOINT = "/api/subscribe"; // 실제 엔드포인트에 맞게 수정해야 합니다.

export default function SseProvider({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const token = useAuthStore((state) => state.token);
  const addNotification = useNotificationStore((state) => state.addNotification);

  useEffect(() => {
    // 사용자가 로그인하지 않은 경우 아무 작업도 하지 않음
    if (!isAuthenticated || !token) {
      return;
    }

    console.log("SSE: Authenticated. Attempting to connect...");

    const url = `${API_BASE_URL}${SSE_ENDPOINT}?token=${token}`;

    // EventSource는 'Authorization' 헤더를 직접 지원하지 않습니다.
    // 백엔드에서 쿠키 기반 인증 또는 다른 방식을 통해 SSE 연결을 인증해야 합니다.
    const eventSource = new EventSource(url, { withCredentials: true });

    eventSource.onopen = () => {
      console.log("SSE: Connection opened successfully.");
    };

    // 'notification' 이름으로 오는 커스텀 이벤트를 리스닝
    eventSource.addEventListener("notification", (event) => {
      console.log("SSE: Received 'notification' event", event.data);
      try {
        const newNotification = JSON.parse(event.data);
        addNotification(newNotification);
      } catch (error) {
        console.error("SSE: Failed to parse event data.", error);
      }
    });

    // 기본 'message' 이벤트 리스너 (서버가 이벤트 이름을 지정하지 않을 경우)
    eventSource.onmessage = (event) => {
      console.log("SSE: Received default 'message' event", event.data);
      // 필요에 따라 이 데이터도 처리할 수 있습니다.
    };

    eventSource.onerror = (error) => {
      console.error("SSE: An error occurred.", error);
      // EventSource는 기본적으로 오류 발생 시 자동으로 재연결을 시도합니다.
      // 특정 조건에서는 수동으로 연결을 닫을 수 있습니다.
      // eventSource.close();
    };

    // 컴포넌트가 언마운트되거나 사용자가 로그아웃하여 `isAuthenticated`가 false가 될 때
    // 이 cleanup 함수가 호출되어 SSE 연결을 종료합니다.
    return () => {
      console.log("SSE: Closing connection.");
      eventSource.close();
    };
  }, [isAuthenticated, token, addNotification]);

  return <>{children}</>;
}
