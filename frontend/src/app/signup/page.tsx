"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { SignUpForm } from "@/components/auth/SignUpForm";
import Link from "next/link";
import { Sparkles, TrendingUp, Heart, Award } from "lucide-react";

export default function SignUpPage() {
  const router = useRouter();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  useEffect(() => {
    if (isAuthenticated) {
      router.replace("/dashboard");
    }
  }, [isAuthenticated, router]);
  const benefits = [
    {
      icon: Sparkles,
      title: "AI 챌린지 추천",
      description: "5가지 알고리즘으로 맞춤형 챌린지 제시",
    },
    {
      icon: TrendingUp,
      title: "실시간 피드",
      description: "다른 사람들의 성취를 즉시 확인",
    },
    {
      icon: Heart,
      title: "AI 동기부여",
      description: "개인화된 격려 메시지 제공",
    },
    {
      icon: Award,
      title: "게임화 시스템",
      description: "포인트, 배지, 스트릭 시스템",
    },
  ];

  return (
    <div className="flex h-screen">
      {/* Left Side - Form */}
      <div className="flex-1 flex flex-col justify-center items-center px-8 py-12 bg-white">
        <div className="w-full max-w-md">
          {/* Header */}
          <div className="mb-8">
            <Link href="/" className="inline-block mb-6">
              <h1 className="text-3xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                Planit
              </h1>
            </Link>
            <p className="text-gray-600 text-sm">새로운 여정을 시작하세요.</p>
          </div>

          {/* Form */}
          <SignUpForm />

          {/* Login Link */}
          <div className="mt-6 text-center text-sm text-gray-600">
            이미 계정이 있으신가요?{" "}
            <Link href="/login" className="text-blue-600 hover:text-blue-700 font-semibold">
              로그인
            </Link>
          </div>
        </div>
      </div>

      {/* Right Side - Benefits */}
      <div className="flex-1 hidden lg:flex flex-col justify-center items-center px-12 py-12 bg-gradient-to-br from-blue-600 to-purple-600">
        <div className="max-w-md text-white">
          <h2 className="text-4xl font-bold mb-12">성장을 위한 완벽한 도구</h2>

          <div className="space-y-8">
            {benefits.map((benefit, index) => {
              const Icon = benefit.icon;
              return (
                <div key={index} className="flex gap-4">
                  <div className="flex-shrink-0">
                    <div className="flex items-center justify-center h-12 w-12 rounded-lg bg-white/10">
                      <Icon className="h-6 w-6 text-white" />
                    </div>
                  </div>
                  <div>
                    <h3 className="font-semibold text-lg">{benefit.title}</h3>
                    <p className="text-blue-100 text-sm mt-1">{benefit.description}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
