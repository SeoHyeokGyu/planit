import type { Metadata } from "next";
import "./globals.css";
import Providers from "@/providers/Providers";
import ConditionalHeader from "@/components/layout/ConditionalHeader";
import Footer from "@/components/layout/Footer";

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
      <body className="flex flex-col min-h-screen">
        <Providers>
          <ConditionalHeader />
          <main className="flex-1">{children}</main>
          <Footer />
        </Providers>
      </body>
    </html>
  );
}
