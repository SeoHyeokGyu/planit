"use client";

import { ReactNode } from "react";
import QueryProvider from "@/providers/QueryProvider";
import SseProvider from "@/providers/SseProvider";
import { Toaster } from "@/components/ui/sonner";
import AuthWatcher from "./AuthWatcher";
import { TooltipProvider } from "@/components/ui/tooltip";

/**
 * 애플리케이션 전체에서 사용되는 모든 Context Provider들을 포함하는 중앙 컴포넌트입니다.
 */
export default function Providers({ children }: { children: ReactNode }) {
  return (
    <QueryProvider>
      <AuthWatcher />
      <TooltipProvider>
        <SseProvider>{children}</SseProvider>
      </TooltipProvider>
      <Toaster richColors expand={true} closeButton />
    </QueryProvider>
  );
}
