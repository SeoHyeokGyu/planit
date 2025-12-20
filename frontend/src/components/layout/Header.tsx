"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { useLogout } from "@/hooks/useAuth";
import { useUserProfile } from "@/hooks/useUser";
import { useFollowStats } from "@/hooks/useFollow";
import NotificationDropdown from "@/components/layout/NotificationDropdown";
import { Users, LogOut, User, Heart } from "lucide-react";
import { useState } from "react";

export default function Header() {
  const router = useRouter();
  const token = useAuthStore((state) => state.token);
  const logout = useLogout();
  const { data: currentUser } = useUserProfile();
  const { followerCount, followingCount } = useFollowStats(currentUser?.loginId || "");
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
                className="text-gray-600 hover:text-gray-900 font-medium transition-colors"
              >
                대시보드
              </Link>
              <Link
                href="/challenge"
                className="text-gray-600 hover:text-gray-900 font-medium transition-colors"
              >
                챌린지
              </Link>
              <Link
                href="/feed"
                className="text-gray-600 hover:text-gray-900 font-medium transition-colors"
              >
                피드
              </Link>
              <Link
                href="/users"
                className="flex items-center gap-2 text-gray-600 hover:text-gray-900 font-medium transition-colors"
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
                    className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-blue-50 hover:border-blue-200 transition-all border border-transparent group"
                    title={`${currentUser.nickname} - 프로필`}
                  >
                    {/* 아바타 */}
                    <div className="w-9 h-9 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center text-white text-sm font-bold shadow-md group-hover:shadow-lg transition-shadow">
                      {currentUser.nickname?.charAt(0).toUpperCase()}
                    </div>
                    <span className="hidden sm:inline text-sm font-semibold text-gray-700 group-hover:text-blue-600 transition-colors max-w-[100px] truncate">
                      {currentUser.nickname}
                    </span>
                  </button>

                  {/* 프로필 드롭다운 메뉴 */}
                  {isDropdownOpen && (
                    <div className="absolute right-0 mt-2 w-56 bg-white border border-gray-200 rounded-xl shadow-xl z-50">
                      {/* 프로필 헤더 */}
                      <div className="px-4 py-3 border-b border-gray-100 bg-gradient-to-r from-blue-50 to-purple-50">
                        <button
                          onClick={() => {
                            setIsDropdownOpen(false);
                            router.push("/profile");
                          }}
                          className="w-full flex items-center gap-3 hover:opacity-80 transition-opacity group"
                        >
                          <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center text-white font-bold shadow-md">
                            {currentUser.nickname?.charAt(0).toUpperCase()}
                          </div>
                          <div className="text-left flex-1">
                            <p className="font-semibold text-gray-900 group-hover:text-blue-600">
                              {currentUser.nickname}
                            </p>
                            <p className="text-xs text-gray-500">@{currentUser.loginId}</p>
                          </div>
                        </button>
                      </div>

                      {/* 팔로우 통계 */}
                      <div className="px-4 py-3 border-b border-gray-100 flex gap-4">
                        <button
                          onClick={() => {
                            setIsDropdownOpen(false);
                            router.push("/profile");
                          }}
                          className="flex-1 text-center hover:bg-gray-50 rounded-lg py-2 transition-colors group"
                        >
                          <p className="font-bold text-gray-900 group-hover:text-blue-600">
                            {followerCount}
                          </p>
                          <p className="text-xs text-gray-500">팔로워</p>
                        </button>
                        <button
                          onClick={() => {
                            setIsDropdownOpen(false);
                            router.push("/profile");
                          }}
                          className="flex-1 text-center hover:bg-gray-50 rounded-lg py-2 transition-colors group"
                        >
                          <p className="font-bold text-gray-900 group-hover:text-blue-600">
                            {followingCount}
                          </p>
                          <p className="text-xs text-gray-500">팔로잉</p>
                        </button>
                      </div>

                      {/* 메뉴 항목 */}
                      <Link
                        href="/profile"
                        className="flex items-center gap-3 px-4 py-3 hover:bg-blue-50 text-gray-700 hover:text-blue-600 transition-colors border-b border-gray-100"
                        onClick={() => setIsDropdownOpen(false)}
                      >
                        <User className="w-4 h-4" />
                        <span className="font-medium">내 프로필</span>
                      </Link>

                      {/* 로그아웃 */}
                      <button
                        onClick={() => {
                          setIsDropdownOpen(false);
                          logout();
                        }}
                        className="w-full flex items-center gap-3 px-4 py-3 hover:bg-red-50 text-gray-700 hover:text-red-600 transition-colors text-left"
                      >
                        <LogOut className="w-4 h-4" />
                        <span className="font-medium">로그아웃</span>
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
