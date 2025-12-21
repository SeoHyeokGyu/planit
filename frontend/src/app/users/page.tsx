"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { UserProfile } from "@/types/user";
import { useFollowings } from "@/hooks/useFollow";
import { useAuthStore } from "@/stores/authStore";
import FollowButton from "@/components/follow/FollowButton";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Search, Users, Loader2 } from "lucide-react";

/**
 * 사용자 검색 페이지
 * URL: /users
 */
export default function UsersPage() {
  const router = useRouter();
  const currentLoginId = useAuthStore((state) => state.loginId);
  const [keyword, setKeyword] = useState("");
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isError, setIsError] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  // 현재 사용자의 팔로잉 목록 조회
  const { data: followings } = useFollowings(currentLoginId || "", 0, 100);

  // Debounce 검색 (500ms)
  useEffect(() => {
    const timer = setTimeout(() => {
      if (keyword.trim()) {
        searchUsers(keyword);
      } else {
        setUsers([]);
        setHasSearched(false);
      }
    }, 500);

    return () => clearTimeout(timer);
  }, [keyword]);

  const searchUsers = async (searchKeyword: string) => {
    if (!searchKeyword.trim()) {
      setUsers([]);
      return;
    }

    setIsLoading(true);
    setIsError(false);
    setHasSearched(true);

    try {
      const response = await api.get<ApiResponse<UserProfile[]>>(
        `/api/users/search?keyword=${encodeURIComponent(searchKeyword)}&page=0&size=20`
      );
      setUsers(response.data || []);
    } catch (error) {
      setIsError(true);
      console.error("사용자 검색 실패:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleNavigateToProfile = (loginId: string) => {
    router.push(`/profile/${loginId}`);
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 헤더 */}
        <div className="mb-8">
          <div className="flex items-center space-x-3 mb-2">
            <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-500 rounded-lg flex items-center justify-center text-white">
              <Users className="w-6 h-6" />
            </div>
            <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              사용자 찾기
            </h1>
          </div>
          <p className="text-gray-700 font-medium">
            관심 있는 사용자를 검색하고 팔로우하세요
          </p>
        </div>

        {/* 검색 입력 */}
        <Card className="mb-8 border-2 shadow-lg bg-white">
          <CardContent className="pt-6">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <Input
                placeholder="사용자 이름 또는 아이디로 검색..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                className="pl-10 h-12 text-base border-2 border-gray-300 focus:border-blue-500 bg-white text-gray-900 placeholder:text-gray-400 font-medium"
              />
            </div>
          </CardContent>
        </Card>

        {/* 검색 결과 */}
        {isLoading && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[...Array(6)].map((_, i) => (
              <Card key={i} className="border-2">
                <CardHeader>
                  <Skeleton className="h-6 w-3/4 mb-2" />
                  <Skeleton className="h-4 w-1/2" />
                </CardHeader>
                <CardContent>
                  <Skeleton className="h-10 w-full" />
                </CardContent>
              </Card>
            ))}
          </div>
        )}

        {isError && (
          <div className="text-center py-20">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-red-100 rounded-full mb-4">
              <Search className="w-8 h-8 text-red-400" />
            </div>
            <p className="text-gray-700 text-lg font-semibold">
              검색에 실패했습니다.
            </p>
            <p className="text-gray-500 text-sm mt-2">
              다시 시도해주세요.
            </p>
          </div>
        )}

        {hasSearched && !isLoading && !isError && users.length === 0 && (
          <div className="text-center py-20">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
              <Search className="w-8 h-8 text-gray-400" />
            </div>
            <p className="text-gray-700 text-lg font-semibold">
              검색 결과가 없습니다.
            </p>
            <p className="text-gray-500 text-sm mt-2">
              다른 검색 조건을 시도해보세요.
            </p>
          </div>
        )}

        {users.length > 0 && !isLoading && (
          <>
            <div className="mb-4 text-sm text-gray-700 font-medium">
              총 <span className="font-bold text-blue-600">{users.length}</span>
              명의 사용자
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {users.map((user) => (
                <Card
                  key={user.loginId}
                  className="overflow-hidden cursor-pointer hover:shadow-2xl transition-all duration-300 border-2 hover:border-blue-400 group bg-gradient-to-br from-white to-gray-50"
                >
                  <CardHeader className="pb-3">
                    <div
                      onClick={() => handleNavigateToProfile(user.loginId)}
                      className="hover:opacity-80 transition-opacity"
                    >
                      <CardTitle className="text-lg font-bold text-gray-900 group-hover:text-blue-600 transition-colors line-clamp-1">
                        {user.nickname}
                      </CardTitle>
                      <CardDescription className="text-sm text-gray-600">
                        @{user.loginId}
                      </CardDescription>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <p className="text-xs text-gray-500">
                      📅 가입: {new Date(user.createdAt).toLocaleDateString()}
                    </p>
                    <div className="flex gap-2 pt-2">
                      <Button
                        variant="outline"
                        className="flex-1 font-semibold cursor-pointer border-gray-300 text-gray-700 hover:bg-blue-50 hover:border-blue-400 hover:text-blue-600 transition-all duration-200"
                        onClick={() => handleNavigateToProfile(user.loginId)}
                      >
                        프로필
                      </Button>
                      <div className="flex-1">
                        <FollowButton
                          targetLoginId={user.loginId}
                          variant="default"
                          size="default"
                          initialIsFollowing={followings?.some(f => f.loginId === user.loginId) ?? false}
                        />
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </>
        )}

        {!hasSearched && !isLoading && users.length === 0 && (
          <div className="text-center py-20">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-100 rounded-full mb-4">
              <Search className="w-8 h-8 text-blue-400" />
            </div>
            <p className="text-gray-700 text-lg font-semibold">
              사용자를 검색하세요.
            </p>
            <p className="text-gray-500 text-sm mt-2">
              위에 사용자 이름 또는 아이디를 입력하여 검색해주세요.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
