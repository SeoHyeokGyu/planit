"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { useLogout } from "@/hooks/useAuth";
import NotificationDropdown from "@/components/layout/NotificationDropdown";

export default function Header() {
  const router = useRouter();
  const token = useAuthStore((state) => state.token);
  const logout = useLogout();

  return (
    <header className="bg-white shadow-sm border-b sticky top-0 z-40">
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
              
              <NotificationDropdown />
              
              <button
                onClick={logout}
                className="text-gray-500 hover:text-gray-700 text-sm"
              >
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link
                href="/login"
                className="text-gray-600 hover:text-gray-900 font-medium"
              >
                로그인
              </Link>
              <Link
                href="/signup"
                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 font-medium"
              >
                회원가입
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
