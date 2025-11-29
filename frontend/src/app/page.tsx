'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useAuthStore } from '@/stores/authStore';
import { useLogout } from '@/hooks/useAuth'; // Import useLogout hook
import { Button } from '@/components/ui/button'; // Import Button component

export default function Home() {
  const [baseUrl, setBaseUrl] = useState('');
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const logout = useLogout(); // Initialize useLogout hook

  useEffect(() => {
    setBaseUrl(window.location.origin);
  }, []);

  const handleLogout = () => {
    logout();
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-blue-50 to-white">
      <div className="text-center">
        <h1 className="text-6xl font-bold text-blue-600 mb-4">Planit</h1>
        <p className="text-xl text-gray-600 mb-2">매일매일 성장하는 챌린지</p>
        <p className="text-gray-500 mb-8">새로운 습관을 만들고, 목표를 달성하세요</p>

        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
          {isAuthenticated ? (
            <>
              <Link
                href="/profile"
                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold"
              >
                내 프로필 가기 →
              </Link>
              <Button
                onClick={handleLogout}
                variant="destructive"
                className="px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors font-semibold cursor-pointer"
              >
                로그아웃
              </Button>
            </>
          ) : (
            <>
              <Link
                href="/login"
                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold"
              >
                로그인 →
              </Link>
              <Link
                href="/signup"
                className="px-6 py-3 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors font-semibold"
              >
                회원가입 →
              </Link>
            </>
          )}

          <Link
            href="/api-test/"
            className="px-6 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors font-semibold"
          >
            API 테스트 →
          </Link>
          <a
            href="/swagger-ui/index.html"
            target="_blank"
            rel="noopener noreferrer"
            className="px-6 py-3 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors font-semibold"
          >
            Swagger UI →
          </a>
        </div>

        {baseUrl && (
          <div className="mt-12 p-4 bg-white rounded-lg shadow-sm max-w-md mx-auto">
            <p className="text-sm text-gray-600 mb-2">서버 URL:</p>
            <code className="text-xs bg-gray-100 px-3 py-1 rounded">
              {baseUrl}
            </code>
          </div>
        )}
      </div>
    </div>
  );
}
