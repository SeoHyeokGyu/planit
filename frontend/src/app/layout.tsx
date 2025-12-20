import type { Metadata } from "next";
import "./globals.css";
import Providers from "@/providers/Providers";
import ConditionalHeader from "@/components/layout/ConditionalHeader";

export const metadata: Metadata = {
  title: "Planit - 여행 플래너",
  description: "AI 기반 여행 일정 계획 서비스",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>
        <Providers>
          <ConditionalHeader />
          {children}
        </Providers>
      </body>
    </html>
  );
}
