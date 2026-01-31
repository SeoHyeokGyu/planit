"use client";

import { useRandomUsers } from "@/hooks/useRandomUsers";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Users, TrendingUp } from "lucide-react";
import { useRouter } from "next/navigation";
import { Skeleton } from "@/components/ui/skeleton";

export function RecommendedUsers() {
  const { data: users, isLoading } = useRandomUsers(3);
  const router = useRouter();

  if (isLoading) {
    return (
      <Card className="border-0 bg-white">
        <CardHeader className="border-b border-gray-100 pb-4">
          <CardTitle className="text-lg font-semibold text-gray-900 flex items-center gap-2">
            <Users className="w-5 h-5 text-blue-600" />
            추천 사용자
          </CardTitle>
        </CardHeader>
        <CardContent className="p-4 space-y-3">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Skeleton className="w-10 h-10 rounded-full" />
                <div className="space-y-1">
                  <Skeleton className="h-4 w-20" />
                  <Skeleton className="h-3 w-16" />
                </div>
              </div>
              <Skeleton className="h-8 w-16" />
            </div>
          ))}
        </CardContent>
      </Card>
    );
  }

  if (!users || users.length === 0) {
    return null;
  }

  return (
    <Card className="border-0 bg-white">
      <CardHeader className="border-b border-gray-100 pb-4">
        <CardTitle className="text-lg font-semibold text-gray-900 flex items-center gap-2">
          <Users className="w-5 h-5 text-blue-600" />
          추천 사용자
        </CardTitle>
      </CardHeader>
      <CardContent className="p-4 space-y-3">
        {users.map((user) => (
          <div
            key={user.loginId}
            className="flex items-center justify-between hover:bg-gray-50 p-2 rounded-lg transition-colors cursor-pointer"
            onClick={() => router.push(`/profile/${user.loginId}`)}
          >
            <div className="flex items-center gap-3 flex-1 min-w-0">
              <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white font-semibold text-sm flex-shrink-0">
                {(user.nickname || user.loginId).charAt(0).toUpperCase()}
              </div>
              <div className="flex-1 min-w-0">
                <p className="font-medium text-gray-900 truncate">
                  {user.nickname || user.loginId}
                </p>
                <div className="flex items-center gap-1 text-xs text-gray-500">
                  <TrendingUp className="w-3 h-3" />
                  <span>{user.totalPoint.toLocaleString()}P</span>
                </div>
              </div>
            </div>
            <Button
              variant="outline"
              size="sm"
              className="text-xs flex-shrink-0"
              onClick={(e) => {
                e.stopPropagation();
                router.push(`/profile/${user.loginId}`);
              }}
            >
              프로필
            </Button>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
