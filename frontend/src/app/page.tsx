import { Metadata } from "next";
import LandingPageClient from "@/components/landing/LandingPageClient";

export const metadata: Metadata = {
  title: "Planit - AI 기반 소셜 챌린지 트래커",
  description:
    "작은 성취를 실시간으로 공유하고, 함께 성장하세요. AI가 추천하는 맞춤 챌린지로 매일 새로운 도전을 시작하세요.",
  keywords: [
    "챌린지",
    "습관",
    "목표",
    "AI 추천",
    "소셜",
    "동기부여",
    "성취",
    "게임화",
    "트래커",
  ],
  authors: [{ name: "Planit Team" }],
  openGraph: {
    title: "Planit - AI 기반 소셜 챌린지 트래커",
    description:
      "작은 성취를 실시간으로 공유하고, 함께 성장하세요. AI가 추천하는 맞춤 챌린지로 매일 새로운 도전을 시작하세요.",
    type: "website",
    locale: "ko_KR",
  },
  twitter: {
    card: "summary_large_image",
    title: "Planit - AI 기반 소셜 챌린지 트래커",
    description:
      "작은 성취를 실시간으로 공유하고, 함께 성장하세요.",
  },
  robots: {
    index: true,
    follow: true,
  },
};

export default function Home() {
  return <LandingPageClient />;
}
