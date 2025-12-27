"use client";

import { useEffect } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { useAuthStore } from "@/stores/authStore";
import { toast } from "sonner";

/**
 * 인증 상태를 전역적으로 감시하는 컴포넌트
 * 인증이 풀렸을 때(401 등) 로그인 페이지로 리다이렉트합니다.
 */
export default function AuthWatcher() {
  const router = useRouter();
  const pathname = usePathname();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const queryClient = useQueryClient();

  useEffect(() => {
    // 인증이 필요 없는 공개 경로
    const publicPaths = ["/login", "/signup", "/"];
    const isPublicPath =
      publicPaths.includes(pathname) || pathname.startsWith("/api-test");

    // 인증이 안 된 상태로 보호된 경로에 있으면 로그인 페이지로 보냄
    if (!isAuthenticated && !isPublicPath) {
      // 1. 캐시 데이터 정리
      queryClient.clear();
      // 2. 알림 표시
      toast.error("인증이 만료되었습니다. 다시 로그인해주세요.");
      // 3. 로그인 페이지로 이동
      router.push("/login");
    }
  }, [isAuthenticated, pathname, router, queryClient]);

  return null;
}