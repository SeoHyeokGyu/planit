"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { useLogout } from "@/hooks/useAuth";
import { useUserProfile } from "@/hooks/useUser";
import NotificationDropdown from "@/components/layout/NotificationDropdown";
import { Users, LogOut, User } from "lucide-react";
import { useState } from "react";

export default function Header() {
  const router = useRouter();
  const token = useAuthStore((state) => state.token);
  const logout = useLogout();
  const { data: currentUser } = useUserProfile();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

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
              <Link
                href="/users"
                className="flex items-center gap-2 text-gray-600 hover:text-gray-900 font-medium"
              >
                <Users className="w-4 h-4" />
                사용자 찾기
              </Link>

              <NotificationDropdown />

              {/* 로그인 정보 */}
              {currentUser && (
                <div className="relative">
                  <button
                    onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                    className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors"
                  >
                    {/* 아바타 */}
                    <div className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white text-sm font-bold">
                      {currentUser.nickname?.charAt(0).toUpperCase()}
                    </div>
                    <span className="text-sm font-medium text-gray-700">
                      {currentUser.nickname}
                    </span>
                  </button>

                  {/* 드롭다운 메뉴 */}
                  {isDropdownOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-50">
                      <Link
                        href="/profile"
                        className="flex items-center gap-3 px-4 py-3 hover:bg-gray-50 border-b border-gray-100 text-gray-700"
                        onClick={() => setIsDropdownOpen(false)}
                      >
                        <User className="w-4 h-4" />
                        <span>프로필 보기</span>
                      </Link>
                      <button
                        onClick={() => {
                          setIsDropdownOpen(false);
                          logout();
                        }}
                        className="w-full flex items-center gap-3 px-4 py-3 hover:bg-gray-50 text-gray-700 text-left"
                      >
                        <LogOut className="w-4 h-4" />
                        <span>로그아웃</span>
                      </button>
                    </div>
                  )}
                </div>
              )}
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
