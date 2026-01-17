import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { useLogout } from "@/hooks/useAuth";
import { useUserProfile } from "@/hooks/useUser";
import NotificationDropdown from "@/components/layout/NotificationDropdown";
import { Users, LogOut, LayoutDashboard, Trophy, Activity, Settings, Flame, Medal, BarChart3, Crown, Target, Menu, X } from "lucide-react";
import { useState, useEffect, useRef } from "react";
import { navStyles, dropdownStyles, componentStyles } from "@/styles/common";

export default function Header() {
  const router = useRouter();
  const token = useAuthStore((state) => state.token);
  const logout = useLogout();
  const { data: currentUser } = useUserProfile();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const mobileMenuRef = useRef<HTMLDivElement>(null);

  // Click outside handler for dropdown
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
      if (mobileMenuRef.current && !mobileMenuRef.current.contains(event.target as Node)) {
        setIsMobileMenuOpen(false);
      }
    };

    if (isDropdownOpen || isMobileMenuOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isDropdownOpen, isMobileMenuOpen]);

  // Close mobile menu on route change
  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [router]);

  // Prevent body scroll when mobile menu is open
  useEffect(() => {
    if (isMobileMenuOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "unset";
    }
    return () => {
      document.body.style.overflow = "unset";
    };
  }, [isMobileMenuOpen]);

  return (
    <header className="bg-white shadow-sm border-b sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <Link href="/" className="text-xl font-bold text-blue-600">
          PlanIt
        </Link>

        <nav className="flex items-center gap-1">
          {token ? (
            <>
              {/* Desktop Navigation Links */}
              <div className="hidden md:flex items-center gap-1">
                <Link href="/dashboard" className={navStyles.link}>
                  <LayoutDashboard className="w-4 h-4" />
                  대시보드
                </Link>
                <Link href="/goals" className={navStyles.link}>
                  <Target className="w-4 h-4" />
                  목표
                </Link>
                <Link href="/challenge" className={navStyles.link}>
                  <Trophy className="w-4 h-4" />
                  챌린지
                </Link>
                <Link href="/feed" className={navStyles.link}>
                  <Activity className="w-4 h-4" />
                  피드
                </Link>
                <Link href="/ranking" className={navStyles.link.replace("text-blue-600 hover:bg-blue-50", "text-yellow-600 hover:bg-yellow-50")}>
                  <Crown className="w-4 h-4" />
                  랭킹
                </Link>
              </div>

              <NotificationDropdown />

              {/* 사용자 프로필 메뉴 */}
              {currentUser && (
                <div className="relative" ref={dropdownRef}>
                  <button
                    onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                    className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 transition-all font-medium"
                    title={`${currentUser.nickname} - 프로필`}
                    aria-expanded={isDropdownOpen}
                    aria-haspopup="menu"
                    aria-label="사용자 메뉴 열기"
                  >
                    {/* 아바타 */}
                    <div className={`${componentStyles.avatar.base} ${componentStyles.avatar.small}`}>
                      {currentUser.nickname?.charAt(0).toUpperCase()}
                    </div>
                    <span className="hidden sm:inline text-sm font-semibold text-gray-700 transition-colors max-w-[100px] truncate">
                      {currentUser.nickname}
                    </span>
                  </button>

                  {/* 프로필 드롭다운 메뉴 */}
                  {isDropdownOpen && (
                    <div
                      className={dropdownStyles.wrapper}
                      role="menu"
                      aria-orientation="vertical"
                      aria-label="사용자 메뉴"
                    >
                      {/* 프로필 헤더 */}
                      <div className={dropdownStyles.header}>
                        <button
                          onClick={() => {
                            setIsDropdownOpen(false);
                            router.push("/profile");
                          }}
                          className="w-full flex items-center gap-3 rounded-lg hover:bg-blue-100/40 transition-all p-2 -m-2"
                          role="menuitem"
                        >
                          <div className={`${componentStyles.avatar.base} ${componentStyles.avatar.small}`}>
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

                      {/* 활동 섹션 */}
                      <div className={dropdownStyles.sectionHeader}>
                        활동
                      </div>
                      <Link
                        href="/stats"
                        className={dropdownStyles.item}
                        onClick={() => setIsDropdownOpen(false)}
                        role="menuitem"
                      >
                        <BarChart3 className="w-4 h-4" />
                        내 통계
                      </Link>
                      <Link
                        href="/profile?tab=badges"
                        className={dropdownStyles.item}
                        onClick={() => setIsDropdownOpen(false)}
                        role="menuitem"
                      >
                        <Medal className="w-4 h-4" />
                        내 배지
                      </Link>
                      <Link
                        href="/profile?tab=streaks"
                        className={dropdownStyles.item}
                        onClick={() => setIsDropdownOpen(false)}
                        role="menuitem"
                      >
                        <Flame className="w-4 h-4" />
                        내 스트릭
                      </Link>
                      <Link
                        href="/users"
                        className={dropdownStyles.item}
                        onClick={() => setIsDropdownOpen(false)}
                        role="menuitem"
                      >
                        <Users className="w-4 h-4" />
                        사용자 찾기
                      </Link>

                      {/* 계정 섹션 */}
                      <div className={`${dropdownStyles.sectionHeader} border-t border-gray-100`}>
                        계정
                      </div>
                      <Link
                        href="/settings"
                        className={dropdownStyles.item}
                        onClick={() => setIsDropdownOpen(false)}
                        role="menuitem"
                      >
                        <Settings className="w-4 h-4" />
                        설정
                      </Link>
                      <button
                        onClick={() => {
                          setIsDropdownOpen(false);
                          logout();
                        }}
                        className={`${dropdownStyles.item} hover:bg-red-50 hover:text-red-600`}
                        role="menuitem"
                      >
                        <LogOut className="w-4 h-4" />
                        로그아웃
                      </button>
                    </div>
                  )}
                </div>
              )}

              {/* Mobile Hamburger Button */}
              <button
                onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                className="md:hidden flex items-center justify-center w-10 h-10 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 transition-all"
                aria-expanded={isMobileMenuOpen}
                aria-label="메뉴 열기"
              >
                {isMobileMenuOpen ? (
                  <X className="w-6 h-6" />
                ) : (
                  <Menu className="w-6 h-6" />
                )}
              </button>
            </>
          ) : (
            <>
              <Link
                href="/login"
                className="hidden sm:block px-3 py-2 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 font-medium transition-all"
              >
                로그인
              </Link>
              <Link
                href="/signup"
                className="hidden sm:block px-4 py-2 rounded-lg bg-gradient-to-r from-blue-600 to-purple-600 text-white hover:from-blue-700 hover:to-purple-700 font-medium transition-all shadow-md hover:shadow-lg"
              >
                회원가입
              </Link>

              {/* Mobile Hamburger Button (Logged Out) */}
              <button
                onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                className="sm:hidden flex items-center justify-center w-10 h-10 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 transition-all"
                aria-expanded={isMobileMenuOpen}
                aria-label="메뉴 열기"
              >
                {isMobileMenuOpen ? (
                  <X className="w-6 h-6" />
                ) : (
                  <Menu className="w-6 h-6" />
                )}
              </button>
            </>
          )}
        </nav>
      </div>

      {/* Mobile Menu Overlay */}
      {isMobileMenuOpen && (
        <div className="md:hidden fixed inset-0 top-16 z-30 bg-black/20" onClick={() => setIsMobileMenuOpen(false)}>
          <div
            ref={mobileMenuRef}
            className="absolute right-0 top-0 w-72 max-w-[calc(100vw-2rem)] h-[calc(100vh-4rem)] bg-white shadow-xl overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
            role="menu"
            aria-label="모바일 메뉴"
          >
            {token ? (
              <>
                {/* User Profile Header */}
                {currentUser && (
                  <div className="px-4 py-4 border-b border-gray-100 bg-gradient-to-r from-blue-50 to-purple-50">
                    <Link
                      href="/profile"
                      className="flex items-center gap-3"
                      onClick={() => setIsMobileMenuOpen(false)}
                    >
                      <div className={`${componentStyles.avatar.base} ${componentStyles.avatar.large}`}>
                        {currentUser.nickname?.charAt(0).toUpperCase()}
                      </div>
                      <div>
                        <p className="font-semibold text-gray-900">{currentUser.nickname}</p>
                        <p className="text-sm text-gray-500">@{currentUser.loginId}</p>
                      </div>
                    </Link>
                  </div>
                )}

                {/* Navigation Section */}
                <div className="px-2 py-3">
                  <div className="px-3 py-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
                    메뉴
                  </div>
                  <Link
                    href="/dashboard"
                    className={navStyles.mobileLink}
                    onClick={() => setIsMobileMenuOpen(false)}
                    role="menuitem"
                  >
                    <LayoutDashboard className="w-5 h-5" />
                    대시보드
                  </Link>
                  <Link
                    href="/goals"
                    className={navStyles.mobileLink}
                    onClick={() => setIsMobileMenuOpen(false)}
                    role="menuitem"
                  >
                    <Target className="w-5 h-5" />
                    목표
                  </Link>
                  <Link
                    href="/challenge"
                    className={navStyles.mobileLink}
                    onClick={() => setIsMobileMenuOpen(false)}
                    role="menuitem"
                  >
                    <Trophy className="w-5 h-5" />
                    챌린지
                  </Link>
                  <Link
                    href="/feed"
                    className={navStyles.mobileLink}
                    onClick={() => setIsMobileMenuOpen(false)}
                    role="menuitem"
                  >
                    <Activity className="w-5 h-5" />
                    피드
                  </Link>
                  <Link
                    href="/ranking"
                    className={navStyles.mobileLink.replace("text-blue-600 hover:bg-blue-50", "text-yellow-600 hover:bg-yellow-50")}
                    onClick={() => setIsMobileMenuOpen(false)}
                    role="menuitem"
                  >
                    <Crown className="w-5 h-5" />
                    랭킹
                  </Link>
                </div>

                {/* Activity Section */}
                <div className="px-2 py-3 border-t border-gray-100">
                  <div className="px-3 py-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
                    활동
                  </div>
                  <Link
                    href="/stats"
                    className={navStyles.mobileLink}
                    onClick={() => setIsMobileMenuOpen(false)}
                    role="menuitem"
                  >
                    <BarChart3 className="w-5 h-5" />
                    내 통계
                  </Link>
                  <Link
                    href="/profile?tab=badges"
                    className={navStyles.mobileLink}
                    onClick={() => setIsMobileMenuOpen(false)}
                    role="menuitem"
                  >
                    <Medal className="w-5 h-5" />
                    내 배지
                  </Link>
                  <Link
                    href="/profile?tab=streaks"
                    className={navStyles.mobileLink}
                    onClick={() => setIsMobileMenuOpen(false)}
                    role="menuitem"
                  >
                    <Flame className="w-5 h-5" />
                    내 스트릭
                  </Link>
                  <Link
                    href="/users"
                    className={navStyles.mobileLink}
                    onClick={() => setIsMobileMenuOpen(false)}
                    role="menuitem"
                  >
                    <Users className="w-5 h-5" />
                    사용자 찾기
                  </Link>
                </div>

                {/* Account Section */}
                <div className="px-2 py-3 border-t border-gray-100">
                  <div className="px-3 py-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
                    계정
                  </div>
                  <Link
                    href="/settings"
                    className={navStyles.mobileLink}
                    onClick={() => setIsMobileMenuOpen(false)}
                    role="menuitem"
                  >
                    <Settings className="w-5 h-5" />
                    설정
                  </Link>
                  <button
                    onClick={() => {
                      setIsMobileMenuOpen(false);
                      logout();
                    }}
                    className={`${navStyles.mobileLink} w-full hover:bg-red-50 hover:text-red-600 text-left`}
                    role="menuitem"
                  >
                    <LogOut className="w-5 h-5" />
                    로그아웃
                  </button>
                </div>
              </>
            ) : (
              <div className="p-4 space-y-3">
                <Link
                  href="/login"
                  className="block w-full px-4 py-3 rounded-lg text-center text-gray-700 hover:text-blue-600 hover:bg-blue-50 font-medium transition-all border border-gray-200"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  로그인
                </Link>
                <Link
                  href="/signup"
                  className="block w-full px-4 py-3 rounded-lg text-center bg-gradient-to-r from-blue-600 to-purple-600 text-white hover:from-blue-700 hover:to-purple-700 font-medium transition-all shadow-md"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  회원가입
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}