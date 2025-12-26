'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useAuthStore } from '@/stores/authStore';
import { useLogout } from '@/hooks/useAuth';
import { Button } from '@/components/ui/button';
import {
  Users,
  TrendingUp,
  Sparkles,
  Award,
  Search,
  Heart,
  ArrowRight,
} from 'lucide-react';

export default function Home() {
  const [baseUrl, setBaseUrl] = useState('');
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const logout = useLogout();

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setBaseUrl(window.location.origin);
  }, []);

  const handleLogout = () => {
    logout();
  };

  const features = [
    {
      icon: Sparkles,
      title: 'AI 챌린지 추천',
      description: '5가지 알고리즘으로 당신에게 딱 맞는 챌린지를 추천',
    },
    {
      icon: TrendingUp,
      title: '실시간 피드',
      description: 'SSE 기반으로 다른 사람들의 인증을 실시간으로 확인',
    },
    {
      icon: Heart,
      title: 'AI 동기부여 코치',
      description: '개인화된 격려 메시지로 지속적인 동기 부여',
    },
    {
      icon: Award,
      title: '게임화 시스템',
      description: '포인트, 레벨, 배지, 스트릭으로 성취감 극대화',
    },
    {
      icon: Users,
      title: '소셜 커뮤니티',
      description: '같은 목표를 가진 사람들과 함께 성장',
    },
    {
      icon: Search,
      title: 'AI 인증 분석',
      description: 'Google Cloud Vision으로 인증 사진 자동 검증',
    },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
      {/* Hero Section */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-r from-blue-600/10 to-purple-600/10 blur-3xl"></div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 sm:py-32">
          <div className="text-center">
            <div className="flex justify-center mb-6">
              <div className="relative">
                <div className="absolute inset-0 bg-blue-600 blur-2xl opacity-20 rounded-full"></div>
                <h1 className="relative text-6xl sm:text-7xl md:text-8xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                  Planit
                </h1>
              </div>
            </div>
            <p className="text-xl sm:text-2xl md:text-3xl text-gray-700 font-semibold mb-4">
              AI 기반 소셜 챌린지 트래커
            </p>
            <p className="text-base sm:text-lg text-gray-600 mb-12 max-w-2xl mx-auto">
              작은 성취를 실시간으로 공유하고, 함께 성장하세요.
              <br />
              AI가 추천하는 맞춤 챌린지로 매일 새로운 도전을
            </p>

            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center mb-8">
              {isAuthenticated ? (
                <>
                  <Link href="/dashboard">
                    <Button size="lg" className="w-full sm:w-auto bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white shadow-lg hover:shadow-xl transition-all group font-semibold">
                      대시보드로 가기
                      <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                    </Button>
                  </Link>
                  <Button
                    onClick={handleLogout}
                    variant="outline"
                    size="lg"
                    className="w-full sm:w-auto border-2 border-gray-300 text-gray-700 hover:bg-gray-100 font-semibold"
                  >
                    로그아웃
                  </Button>
                </>
              ) : (
                <>
                  <Link href="/signup">
                    <Button size="lg" className="w-full sm:w-auto bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white shadow-lg hover:shadow-xl transition-all group font-semibold">
                      무료로 시작하기
                      <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                    </Button>
                  </Link>
                  <Link href="/login">
                    <Button
                      size="lg"
                      className="w-full sm:w-auto bg-white border-2 border-blue-600 text-blue-600 hover:bg-blue-50 shadow-md hover:shadow-lg font-semibold transition-all"
                    >
                      로그인
                    </Button>
                  </Link>
                </>
              )}
            </div>

            <div className="flex flex-wrap gap-3 justify-center text-sm">
              <Link
                href="/api-test/"
                className="px-4 py-2 bg-indigo-100 text-indigo-700 rounded-full hover:bg-indigo-200 transition-colors font-medium"
              >
                API 테스트
              </Link>
              <a
                href="/swagger-ui/index.html"
                target="_blank"
                rel="noopener noreferrer"
                className="px-4 py-2 bg-orange-100 text-orange-700 rounded-full hover:bg-orange-200 transition-colors font-medium"
              >
                Swagger UI
              </a>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">
              성장을 위한 완벽한 도구
            </h2>
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">
              AI 기술과 게임화 요소로 지속 가능한 습관 형성을 도와드립니다
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {features.map((feature, index) => {
              const Icon = feature.icon;
              return (
                <div
                  key={index}
                  className="group p-6 bg-gradient-to-br from-white to-gray-50 rounded-2xl border border-gray-200 hover:border-blue-300 hover:shadow-xl transition-all duration-300"
                >
                  <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4 group-hover:bg-blue-600 transition-colors">
                    <Icon className="h-6 w-6 text-blue-600 group-hover:text-white transition-colors" />
                  </div>
                  <h3 className="text-xl font-semibold text-gray-900 mb-2">
                    {feature.title}
                  </h3>
                  <p className="text-gray-600">{feature.description}</p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="py-16 bg-gradient-to-r from-blue-600 to-purple-600">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-center text-white">
            <div>
              <div className="flex justify-center mb-2">
                <TrendingUp className="h-8 w-8" />
              </div>
              <div className="text-4xl font-bold mb-2">실시간</div>
              <div className="text-blue-100">인증 피드 & 알림</div>
            </div>
            <div>
              <div className="flex justify-center mb-2">
                <Sparkles className="h-8 w-8" />
              </div>
              <div className="text-4xl font-bold mb-2">AI 기반</div>
              <div className="text-blue-100">챌린지 추천 & 생성</div>
            </div>
            <div>
              <div className="flex justify-center mb-2">
                <Award className="h-8 w-8" />
              </div>
              <div className="text-4xl font-bold mb-2">게임화</div>
              <div className="text-blue-100">포인트 & 배지 시스템</div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-white">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-6">
            오늘부터 작은 성취를 시작하세요
          </h2>
          <p className="text-lg text-gray-600 mb-8 max-w-2xl mx-auto">
            회원가입하고 AI가 추천하는 맞춤 챌린지로 새로운 습관을 만들어보세요.
            <br />
            실시간 피드에서 다른 사람들의 성취를 확인하며 함께 성장하세요.
          </p>
          {!isAuthenticated && (
            <Link href="/signup">
              <Button size="lg" className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white shadow-lg hover:shadow-xl transition-all group font-semibold px-8 py-3">
                무료로 시작하기
                <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" />
              </Button>
            </Link>
          )}
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-400 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <p className="text-sm">
              © 2025 Planit. All rights reserved.
            </p>
            {baseUrl && (
              <p className="text-xs mt-2">
                Server: <code className="text-gray-500">{baseUrl}</code>
              </p>
            )}
          </div>
        </div>
      </footer>
    </div>
  );
}
