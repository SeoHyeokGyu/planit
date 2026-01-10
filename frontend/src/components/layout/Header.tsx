"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { useLogout } from "@/hooks/useAuth";
import { useUserProfile } from "@/hooks/useUser";
import NotificationDropdown from "@/components/layout/NotificationDropdown";
import { Users, LogOut, User, LayoutDashboard, Trophy, Zap, Settings, Flame, Medal, BarChart3 } from "lucide-react";
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

        <nav className="flex items-center gap-1">
          {token ? (
            <>
              <Link
                href="/dashboard"
                className="flex items-center gap-2 px-3 py-2 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 font-medium transition-all"
              >
                <LayoutDashboard className="w-4 h-4" />
                대시보드
              </Link>
              <Link
                href="/challenge"
                className="flex items-center gap-2 px-3 py-2 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 font-medium transition-all"
              >
                <Trophy className="w-4 h-4" />
                챌린지
              </Link>
              <Link
                href="/feed"
                className="flex items-center gap-2 px-3 py-2 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 font-medium transition-all"
              >
                <Zap className="w-4 h-4" />
                피드
              </Link>
              <Link
                href="/users"
                className="flex items-center gap-2 px-3 py-2 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 font-medium transition-all"
              >
                <Users className="w-4 h-4" />
                사용자 찾기
              </Link>

              <NotificationDropdown />

              {/* 사용자 프로필 메뉴 */}
              {currentUser && (
                <div className="relative">
                  <button
                    onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                    className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 transition-all font-medium"
                    title={`${currentUser.nickname} - 프로필`}
                  >
                    {/* 아바타 */}
                    <div className="w-9 h-9 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center text-white text-sm font-bold shadow-md transition-shadow">
                      {currentUser.nickname?.charAt(0).toUpperCase()}
                    </div>
                    <span className="hidden sm:inline text-sm font-semibold text-gray-700 transition-colors max-w-[100px] truncate">
                      {currentUser.nickname}
                    </span>
                  </button>

                  {/* 프로필 드롭다운 메뉴 */}
                  {isDropdownOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-50 overflow-hidden">
                      {/* 프로필 헤더 */}
                      <div className="px-4 py-3 border-b border-gray-100 bg-gradient-to-r from-blue-50 to-purple-50">
                        <button
                          onClick={() => {
                            setIsDropdownOpen(false);
                            router.push("/profile");
                          }}
                          className="w-full flex items-center gap-3 rounded-lg hover:bg-blue-100/40 transition-all p-2 -m-2"
                        >
                          <div className="w-9 h-9 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center text-white text-sm font-bold shadow-md">
                            {currentUser.nickname?.charAt(0).toUpperCase()}
                          </div>
                          <div className="text-left flex-1 min-w-0">
                            <p className="font-semibold text-gray-900 text-sm truncate">
                              {currentUser.nickname}
                            </p>
                            <p className="text-xs text-gray-500 truncate">@{currentUser.loginId}</p>
                          </div>
                        </button>
                      </div>

                      {/* 메뉴 항목 */}
                      <Link
                        href="/profile"
                        className="flex items-center gap-3 px-4 py-3 hover:bg-blue-50 text-gray-700 hover:text-blue-600 font-medium transition-colors border-b border-gray-100"
                        onClick={() => setIsDropdownOpen(false)}
                      >
                        <User className="w-4 h-4" />
                        내 프로필
                      </Link>

                      {/* 내 활동 */}
                      <Link
                        href="/dashboard"
                        className="flex items-center gap-3 px-4 py-3 hover:bg-blue-50 text-gray-700 hover:text-blue-600 font-medium transition-colors border-b border-gray-100"
                        onClick={() => setIsDropdownOpen(false)}
                      >
                        <Flame className="w-4 h-4" />
                        내 활동
                      </Link>

                      {/* 내 통계 */}
                      <Link
                        href="/stats"
                        className="flex items-center gap-3 px-4 py-3 hover:bg-blue-50 text-gray-700 hover:text-blue-600 font-medium transition-colors border-b border-gray-100"
                        onClick={() => setIsDropdownOpen(false)}
                      >
                        <BarChart3 className="w-4 h-4" />
                        내 통계
                      </Link>

                      {/* 내 배지 */}
                      <Link
                        href="/profile?tab=badges"
                        className="flex items-center gap-3 px-4 py-3 hover:bg-blue-50 text-gray-700 hover:text-blue-600 font-medium transition-colors border-b border-gray-100"
                        onClick={() => setIsDropdownOpen(false)}
                      >
                        <Medal className="w-4 h-4" />
                        내 배지
                      </Link>
                      {/* 내 스트릭 */}
                      <Link
                          href="/profile?tab=streaks"
                          className="flex items-center gap-3 px-4 py-3 hover:bg-orange-50 text-gray-700 hover:text-orange-600 font-medium transition-colors border-b border-gray-100"
                          onClick={() => setIsDropdownOpen(false)}
                      >
                        <Flame className="w-4 h-4" />
                        내 스트릭
                      </Link>

                      {/* 설정 */}
                      <Link
                        href="/settings"
                        className="flex items-center gap-3 px-4 py-3 hover:bg-blue-50 text-gray-700 hover:text-blue-600 font-medium transition-colors border-b border-gray-100"
                        onClick={() => setIsDropdownOpen(false)}
                      >
                        <Settings className="w-4 h-4" />
                        설정
                      </Link>

                      {/* 로그아웃 */}
                      <button
                        onClick={() => {
                          setIsDropdownOpen(false);
                          logout();
                        }}
                        className="w-full flex items-center gap-3 px-4 py-3 hover:bg-red-50 text-gray-700 hover:text-red-600 font-medium transition-colors text-left"
                      >
                        <LogOut className="w-4 h-4" />
                        로그아웃
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
                className="px-3 py-2 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 font-medium transition-all"
              >
                로그인
              </Link>
              <Link
                href="/signup"
                className="px-4 py-2 rounded-lg bg-gradient-to-r from-blue-600 to-purple-600 text-white hover:from-blue-700 hover:to-purple-700 font-medium transition-all shadow-md hover:shadow-lg"
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
