'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useAuthStore } from '@/stores/authStore';
import { useLogout } from '@/hooks/useAuth';
import { Button } from '@/components/ui/button';
import {
  MapPin,
  Calendar,
  Users,
  TrendingUp,
  Sparkles,
  Cloud,
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
    setBaseUrl(window.location.origin);
  }, []);

  const handleLogout = () => {
    logout();
  };

  const features = [
    {
      icon: MapPin,
      title: '스마트 여행지 추천',
      description: '당신의 취향과 계절, 날씨를 고려한 맞춤형 여행지 추천',
    },
    {
      icon: Calendar,
      title: '간편한 일정 관리',
      description: '직관적인 인터페이스로 여행 일정을 쉽게 계획하고 관리',
    },
    {
      icon: Cloud,
      title: '실시간 날씨 정보',
      description: '여행지의 실시간 날씨와 최적의 방문 시기 추천',
    },
    {
      icon: TrendingUp,
      title: '인기 여행지 트렌드',
      description: '인구통계 기반 트렌드 분석으로 인기 여행지 발견',
    },
    {
      icon: Users,
      title: '소셜 기능',
      description: '리뷰 공유, 여행 예산 관리, 커뮤니티 참여',
    },
    {
      icon: Award,
      title: '뱃지 & 챌린지',
      description: '여행 목표 달성으로 뱃지를 획득하고 랭킹 경쟁',
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
              AI 기반 스마트 여행 플래너
            </p>
            <p className="text-base sm:text-lg text-gray-600 mb-12 max-w-2xl mx-auto">
              당신만을 위한 완벽한 여행을 계획하세요.
              <br />
              날씨, 트렌드, 취향을 모두 고려한 맞춤형 추천 시스템
            </p>

            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center mb-8">
              {isAuthenticated ? (
                <>
                  <Link href="/profile">
                    <Button size="lg" className="w-full sm:w-auto group">
                      내 프로필 가기
                      <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                    </Button>
                  </Link>
                  <Button
                    onClick={handleLogout}
                    variant="outline"
                    size="lg"
                    className="w-full sm:w-auto"
                  >
                    로그아웃
                  </Button>
                </>
              ) : (
                <>
                  <Link href="/signup">
                    <Button size="lg" className="w-full sm:w-auto group">
                      무료로 시작하기
                      <ArrowRight className="ml-2 h-4 w-4 group-hover:translate-x-1 transition-transform" />
                    </Button>
                  </Link>
                  <Link href="/login">
                    <Button
                      variant="outline"
                      size="lg"
                      className="w-full sm:w-auto"
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
              완벽한 여행을 위한 모든 기능
            </h2>
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">
              최신 기술과 데이터 분석으로 당신의 여행을 더욱 특별하게 만들어드립니다
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
                <Search className="h-8 w-8" />
              </div>
              <div className="text-4xl font-bold mb-2">1000+</div>
              <div className="text-blue-100">여행지 정보</div>
            </div>
            <div>
              <div className="flex justify-center mb-2">
                <Heart className="h-8 w-8" />
              </div>
              <div className="text-4xl font-bold mb-2">AI 분석</div>
              <div className="text-blue-100">맞춤형 추천 시스템</div>
            </div>
            <div>
              <div className="flex justify-center mb-2">
                <Sparkles className="h-8 w-8" />
              </div>
              <div className="text-4xl font-bold mb-2">실시간</div>
              <div className="text-blue-100">날씨 & 트렌드</div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-white">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-6">
            지금 바로 시작하세요
          </h2>
          <p className="text-lg text-gray-600 mb-8 max-w-2xl mx-auto">
            회원가입하고 AI가 추천하는 완벽한 여행 계획을 만나보세요.
            <br />
            모든 기능을 무료로 이용할 수 있습니다.
          </p>
          {!isAuthenticated && (
            <Link href="/signup">
              <Button size="lg" className="group">
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
