"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { useLogout } from "@/hooks/useAuth";

export default function Header() {
  const router = useRouter();
  const token = useAuthStore((state) => state.token);
  const logout = useLogout();

  return (
    <header className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <Link href="/" className="text-xl font-bold text-blue-600">
          PlanIt
        </Link>

        <nav className="flex items-center gap-6">
          {token ? (
            <>
              <Link
                href="/dashboard"
                className="text-gray-600 hover:text-gray-900 font-medium"
              >
                대시보드
              </Link>
              <Link
                href="/challenge"
                className="text-gray-600 hover:text-gray-900 font-medium"
              >
                챌린지
              </Link>
              <Link
                href="/feed"
                className="text-gray-600 hover:text-gray-900 font-medium"
              >
                피드
              </Link>
              <Link
                href="/profile"
                className="text-gray-600 hover:text-gray-900 font-medium"
              >
                프로필
              </Link>
              <button
                onClick={logout}
                className="text-gray-500 hover:text-gray-700 text-sm font-medium"
              >
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link
                href="/login"
                className="bg-white border-2 border-blue-600 text-blue-600 px-4 py-2 rounded-lg hover:bg-blue-50 font-semibold transition-all hover:shadow-md"
              >
                로그인
              </Link>
              <Link
                href="/signup"
                className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-6 py-2 rounded-lg hover:from-blue-700 hover:to-purple-700 font-semibold shadow-md hover:shadow-lg transition-all"
              >
                가입하기
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
