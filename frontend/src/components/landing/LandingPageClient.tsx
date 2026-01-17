"use client";

import Link from "next/link";
import { useAuthStore } from "@/stores/authStore";
import { useLogout } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";
import { ArrowRight } from "lucide-react";
import { FEATURES, STATS } from "@/constants/landing";

export default function LandingPageClient() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const logout = useLogout();

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
      {/* Hero Section */}
      <section className="relative overflow-hidden" aria-labelledby="hero-title">
        <div className="absolute inset-0 bg-gradient-to-r from-blue-600/10 to-purple-600/10 blur-3xl" aria-hidden="true"></div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 sm:py-32">
          <div className="text-center">
            {/* Logo */}
            <div className="flex justify-center mb-6">
              <div className="relative">
                <div className="absolute inset-0 bg-blue-600 blur-2xl opacity-20 rounded-full" aria-hidden="true"></div>
                <h1
                  id="hero-title"
                  className="relative text-6xl sm:text-7xl md:text-8xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent"
                >
                  Planit
                </h1>
              </div>
            </div>

            {/* Subtitle */}
            <p className="text-xl sm:text-2xl md:text-3xl text-gray-700 font-semibold mb-4">
              AI 기반 소셜 챌린지 트래커
            </p>

            {/* Description */}
            <p className="text-base sm:text-lg text-gray-600 mb-12 max-w-2xl mx-auto">
              작은 성취를 실시간으로 공유하고, 함께 성장하세요.
              <br />
              AI가 추천하는 맞춤 챌린지로 매일 새로운 도전을
            </p>

            {/* CTA Buttons */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center mb-8">
              {isAuthenticated ? (
                <>
                  <Link href="/dashboard" aria-label="대시보드로 이동">
                    <Button
                      variant="gradient"
                      size="lg"
                      className="w-full sm:w-auto font-semibold group"
                    >
                      대시보드로 가기
                      <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" aria-hidden="true" />
                    </Button>
                  </Link>
                  <Button
                    onClick={logout}
                    variant="outline"
                    size="lg"
                    className="w-full sm:w-auto font-semibold"
                    aria-label="로그아웃"
                  >
                    로그아웃
                  </Button>
                </>
              ) : (
                <>
                  <Link href="/signup" aria-label="무료로 회원가입하기">
                    <Button
                      variant="gradient"
                      size="lg"
                      className="w-full sm:w-auto font-semibold group"
                    >
                      무료로 시작하기
                      <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" aria-hidden="true" />
                    </Button>
                  </Link>
                  <Link href="/login" aria-label="로그인하기">
                    <Button
                      variant="outlineGradient"
                      size="lg"
                      className="w-full sm:w-auto font-semibold"
                    >
                      로그인
                    </Button>
                  </Link>
                </>
              )}
            </div>

            {/* Developer Links */}
            <nav className="flex flex-wrap gap-3 justify-center text-sm" aria-label="개발자 도구">
              <Link
                href="/api-test"
                className="px-4 py-2 bg-indigo-100 text-indigo-700 rounded-full hover:bg-indigo-200 transition-colors font-medium focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
              >
                API 테스트
              </Link>
              <a
                href="/swagger-ui/index.html"
                target="_blank"
                rel="noopener noreferrer"
                className="px-4 py-2 bg-orange-100 text-orange-700 rounded-full hover:bg-orange-200 transition-colors font-medium focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2"
              >
                Swagger UI
              </a>
            </nav>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-white" aria-labelledby="features-title">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 id="features-title" className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">
              성장을 위한 완벽한 도구
            </h2>
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">
              AI 기술과 게임화 요소로 지속 가능한 습관 형성을 도와드립니다
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {FEATURES.map((feature) => {
              const Icon = feature.icon;
              return (
                <article
                  key={feature.id}
                  className="group p-6 bg-gradient-to-br from-white to-gray-50 rounded-2xl border border-gray-200 hover:border-blue-300 hover:shadow-xl transition-all duration-300"
                >
                  <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4 group-hover:bg-blue-600 transition-colors" aria-hidden="true">
                    <Icon className="h-6 w-6 text-blue-600 group-hover:text-white transition-colors" />
                  </div>
                  <h3 className="text-xl font-semibold text-gray-900 mb-2">{feature.title}</h3>
                  <p className="text-gray-600">{feature.description}</p>
                </article>
              );
            })}
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="py-16 bg-gradient-to-r from-blue-600 to-purple-600" aria-labelledby="stats-title">
        <h2 id="stats-title" className="sr-only">주요 기능 통계</h2>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-center text-white">
            {STATS.map((stat) => {
              const Icon = stat.icon;
              return (
                <div key={stat.id}>
                  <div className="flex justify-center mb-2" aria-hidden="true">
                    <Icon className="h-8 w-8" />
                  </div>
                  <div className="text-4xl font-bold mb-2">{stat.label}</div>
                  <div className="text-blue-100">{stat.description}</div>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-white" aria-labelledby="cta-title">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 id="cta-title" className="text-3xl sm:text-4xl font-bold text-gray-900 mb-6">
            오늘부터 작은 성취를 시작하세요
          </h2>
          <p className="text-lg text-gray-600 mb-8 max-w-2xl mx-auto">
            회원가입하고 AI가 추천하는 맞춤 챌린지로 새로운 습관을 만들어보세요.
            <br />
            실시간 피드에서 다른 사람들의 성취를 확인하며 함께 성장하세요.
          </p>
          {isAuthenticated ? (
            <Link href="/challenge" aria-label="챌린지 둘러보기">
              <Button
                variant="gradient"
                size="xl"
                className="font-semibold group"
              >
                챌린지 둘러보기
                <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" aria-hidden="true" />
              </Button>
            </Link>
          ) : (
            <Link href="/signup" aria-label="무료로 회원가입하기">
              <Button
                variant="gradient"
                size="xl"
                className="font-semibold group"
              >
                무료로 시작하기
                <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" aria-hidden="true" />
              </Button>
            </Link>
          )}
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-400 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <p className="text-sm">© 2025 Planit. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
