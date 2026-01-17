import { Users, TrendingUp, Sparkles, Award, Search, Heart, LucideIcon } from "lucide-react";

export interface Feature {
  id: string;
  icon: LucideIcon;
  title: string;
  description: string;
}

export interface Stat {
  id: string;
  icon: LucideIcon;
  label: string;
  description: string;
}

export const FEATURES: Feature[] = [
  {
    id: "ai-recommendation",
    icon: Sparkles,
    title: "AI 챌린지 추천",
    description: "5가지 알고리즘으로 당신에게 딱 맞는 챌린지를 추천",
  },
  {
    id: "realtime-feed",
    icon: TrendingUp,
    title: "실시간 피드",
    description: "SSE 기반으로 다른 사람들의 인증을 실시간으로 확인",
  },
  {
    id: "ai-coach",
    icon: Heart,
    title: "AI 동기부여 코치",
    description: "개인화된 격려 메시지로 지속적인 동기 부여",
  },
  {
    id: "gamification",
    icon: Award,
    title: "게임화 시스템",
    description: "포인트, 레벨, 배지, 스트릭으로 성취감 극대화",
  },
  {
    id: "social-community",
    icon: Users,
    title: "소셜 커뮤니티",
    description: "같은 목표를 가진 사람들과 함께 성장",
  },
  {
    id: "ai-verification",
    icon: Search,
    title: "AI 인증 분석",
    description: "Google Cloud Vision으로 인증 사진 자동 검증",
  },
];

export const STATS: Stat[] = [
  {
    id: "realtime",
    icon: TrendingUp,
    label: "실시간",
    description: "인증 피드 & 알림",
  },
  {
    id: "ai-powered",
    icon: Sparkles,
    label: "AI 기반",
    description: "챌린지 추천 & 생성",
  },
  {
    id: "gamification-stat",
    icon: Award,
    label: "게임화",
    description: "포인트 & 배지 시스템",
  },
];

export const GRADIENT_PRIMARY = "bg-gradient-to-r from-blue-600 to-purple-600";
export const GRADIENT_PRIMARY_HOVER = "hover:from-blue-700 hover:to-purple-700";
export const GRADIENT_BG = "bg-gradient-to-b from-blue-50 via-white to-blue-50";
