"use client";

import { ReactNode } from "react";
import QueryProvider from "@/providers/QueryProvider";
import SseProvider from "@/providers/SseProvider";

/**
 * 애플리케이션 전체에서 사용되는 모든 Context Provider들을 포함하는 중앙 컴포넌트입니다.
 * 이 컴포넌트를 사용하여 RootLayout의 깊은 중첩 구조를 방지하고 Provider들을 한 곳에서 관리합니다.
 */
export default function Providers({ children }: { children: ReactNode }) {
  return (
    <QueryProvider>
      <SseProvider>{children}</SseProvider>
    </QueryProvider>
  );
}
