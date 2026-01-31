import Link from "next/link";
import { ArrowLeft, Target, Users, Zap, Award } from "lucide-react";

export default function AboutPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* 헤더 */}
        <div className="mb-8">
          <Link
            href="/"
            className="inline-flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors mb-4"
          >
            <ArrowLeft className="w-4 h-4" />
            홈으로 돌아가기
          </Link>
          <h1 className="text-4xl font-bold text-gray-900 mb-4">
            Planit 소개
          </h1>
          <p className="text-xl text-gray-600">
            AI 기반 소셜 챌린지 트래커
          </p>
        </div>

        {/* 서비스 설명 */}
        <div className="bg-white rounded-lg shadow-sm p-8 mb-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">
            Planit은 무엇인가요?
          </h2>
          <p className="text-gray-700 leading-relaxed mb-4">
            Planit은 여러분의 작은 성취를 실시간으로 공유하고, 함께 성장하는 AI 기반 소셜 챌린지 플랫폼입니다.
            일상의 목표를 챌린지로 만들고, 인증을 통해 실천하며, 커뮤니티와 함께 동기부여를 받을 수 있습니다.
          </p>
          <p className="text-gray-700 leading-relaxed">
            운동, 독서, 학습, 생활습관 등 다양한 카테고리의 챌린지에 참여하여
            꾸준한 습관을 만들고 목표를 달성해보세요.
          </p>
        </div>

        {/* 주요 기능 */}
        <div className="bg-white rounded-lg shadow-sm p-8 mb-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">
            주요 기능
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="flex gap-4">
              <div className="flex-shrink-0">
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <Target className="w-6 h-6 text-blue-600" />
                </div>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900 mb-2">
                  다양한 챌린지
                </h3>
                <p className="text-gray-600 text-sm">
                  운동, 독서, 학습 등 다양한 카테고리의 챌린지를 만들고 참여할 수 있습니다.
                </p>
              </div>
            </div>

            <div className="flex gap-4">
              <div className="flex-shrink-0">
                <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                  <Zap className="w-6 h-6 text-purple-600" />
                </div>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900 mb-2">
                  AI 인증 분석
                </h3>
                <p className="text-gray-600 text-sm">
                  AI가 인증 내용을 분석하여 챌린지 적합성을 자동으로 판단합니다.
                </p>
              </div>
            </div>

            <div className="flex gap-4">
              <div className="flex-shrink-0">
                <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                  <Users className="w-6 h-6 text-green-600" />
                </div>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900 mb-2">
                  실시간 소셜 피드
                </h3>
                <p className="text-gray-600 text-sm">
                  다른 사용자들의 인증을 실시간으로 확인하고 서로 응원할 수 있습니다.
                </p>
              </div>
            </div>

            <div className="flex gap-4">
              <div className="flex-shrink-0">
                <div className="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
                  <Award className="w-6 h-6 text-orange-600" />
                </div>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900 mb-2">
                  랭킹 & 배지
                </h3>
                <p className="text-gray-600 text-sm">
                  활동에 따른 포인트와 배지를 획득하고 랭킹에서 경쟁할 수 있습니다.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* 기술 스택 */}
        <div className="bg-white rounded-lg shadow-sm p-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">
            기술 스택
          </h2>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="font-semibold text-gray-900">Frontend</p>
              <p className="text-sm text-gray-600 mt-1">Next.js 15</p>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="font-semibold text-gray-900">Backend</p>
              <p className="text-sm text-gray-600 mt-1">Spring Boot</p>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="font-semibold text-gray-900">Database</p>
              <p className="text-sm text-gray-600 mt-1">PostgreSQL</p>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="font-semibold text-gray-900">Cache</p>
              <p className="text-sm text-gray-600 mt-1">Redis</p>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="font-semibold text-gray-900">AI</p>
              <p className="text-sm text-gray-600 mt-1">Google GenAI</p>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="font-semibold text-gray-900">Language</p>
              <p className="text-sm text-gray-600 mt-1">TypeScript, Kotlin</p>
            </div>
          </div>
        </div>

        {/* CTA */}
        <div className="mt-8 text-center">
          <Link
            href="/challenge"
            className="inline-flex items-center justify-center px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            챌린지 둘러보기
          </Link>
        </div>
      </div>
    </div>
  );
}
