"use client";

import { usePathname } from "next/navigation";
import Header from "./Header";

export default function ConditionalHeader() {
  const pathname = usePathname();

  // 루트 경로("/")가 아닐 때만 Header를 렌더링
  if (pathname === "/") {
    return null;
  }

  return <Header />;
}
