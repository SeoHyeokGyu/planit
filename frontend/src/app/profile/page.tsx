"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import {
  useUserProfile,
  useUpdateProfile,
  useUpdatePassword,
} from "@/hooks/useUser";
import { useLogout } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { UserProfile } from "@/types/user";

// --- Main Profile Page Component ---
export default function ProfilePage() {
  const router = useRouter();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const { data: user, isLoading, isError, error } = useUserProfile();

  useEffect(() => {
    if (!isAuthenticated) {
      router.replace("/login");
    }
  }, [isAuthenticated, router]);

  if (isLoading || !isAuthenticated || !user) {
    return <ProfilePageSkeleton />;
  }

  if (isError) {
    return (
      <div className="flex min-h-screen items-center justify-center text-red-500">
        <p>프로필 정보를 불러오는 데 실패했습니다: {error.message}</p>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-indigo-950 p-4">
      <Tabs defaultValue="info" className="w-full max-w-md">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="info" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">프로필 정보</TabsTrigger>
          <TabsTrigger value="update" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">회원정보 변경</TabsTrigger>
        </TabsList>

        <TabsContent value="info">
          <ProfileInfoTab user={user} />
        </TabsContent>
        <TabsContent value="update">
          <UpdateInfoTab user={user} />
        </TabsContent>
      </Tabs>
    </div>
  );
}

// --- Child Components for Tabs ---

function ProfileInfoTab({ user }: { user: UserProfile }) {
  const logout = useLogout();
  return (
    <Card>
      <CardHeader>
        <CardTitle>프로필 정보</CardTitle>
        <CardDescription>
          현재 회원님의 정보를 보여줍니다.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-1">
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">아이디</p>
          <p className="text-lg font-semibold">{user?.loginId}</p>
        </div>
        <div className="space-y-1">
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">닉네임</p>
          <p className="text-lg font-semibold">{user?.nickname}</p>
        </div>
        <div className="space-y-1">
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">가입일</p>
          <p className="text-lg font-semibold">
            {user ? new Date(user.createdAt).toLocaleDateString() : ""}
          </p>
        </div>
        <Button onClick={logout} variant="destructive" className="w-full mt-4">
          로그아웃
        </Button>
      </CardContent>
    </Card>
  );
}

function UpdateInfoTab({ user }: { user: UserProfile }) {
  const [nickname, setNickname] = useState(user?.nickname || "");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordError, setPasswordError] = useState<string | null>(null);

  const updateProfileMutation = useUpdateProfile();
  const updatePasswordMutation = useUpdatePassword();

  // FIX: user prop이 변경될 때마다 nickname 상태를 동기화합니다.
  useEffect(() => {
    if (user?.nickname) {
      setNickname(user.nickname);
    }
  }, [user]);

  const handleNicknameSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfileMutation.mutate({ nickname });
  };

  const handlePasswordSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      setPasswordError("새 비밀번호가 일치하지 않습니다.");
      return;
    }
    setPasswordError(null);
    updatePasswordMutation.mutate(
      { currentPassword, newPassword },
      {
        onSuccess: () => {
          setCurrentPassword("");
          setNewPassword("");
          setConfirmPassword("");
        },
      }
    );
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>회원정보 변경</CardTitle>
        <CardDescription>닉네임 또는 비밀번호를 변경할 수 있습니다.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-8">
        {/* Nickname Form */}
        <form onSubmit={handleNicknameSubmit} className="space-y-4">
          <h3 className="font-semibold text-lg border-b pb-2">닉네임 변경</h3>
          <div className="space-y-2">
            <Label htmlFor="nickname">새 닉네임</Label>
            <Input
              id="nickname"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              disabled={updateProfileMutation.isPending}
            />
          </div>
          {updateProfileMutation.isSuccess && (
            <p className="text-sm font-medium text-green-600">닉네임이 성공적으로 변경되었습니다.</p>
          )}
          {updateProfileMutation.isError && (
            <p className="text-sm font-medium text-red-500">{updateProfileMutation.error.message}</p>
          )}
          <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white" disabled={updateProfileMutation.isPending}>
            {updateProfileMutation.isPending ? "변경 중..." : "닉네임 변경"}
          </Button>
        </form>

        {/* Password Form */}
        <form onSubmit={handlePasswordSubmit} className="space-y-4">
          <h3 className="font-semibold text-lg border-b pb-2">비밀번호 변경</h3>
          <div className="space-y-2">
            <Label htmlFor="currentPassword">현재 비밀번호</Label>
            <Input
              id="currentPassword"
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              disabled={updatePasswordMutation.isPending}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="newPassword">새 비밀번호</Label>
            <Input
              id="newPassword"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              disabled={updatePasswordMutation.isPending}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="confirmPassword">새 비밀번호 확인</Label>
            <Input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              disabled={updatePasswordMutation.isPending}
            />
          </div>
          {updatePasswordMutation.isSuccess && (
            <p className="text-sm font-medium text-green-600">비밀번호가 성공적으로 변경되었습니다.</p>
          )}
          {passwordError && <p className="text-sm font-medium text-red-500">{passwordError}</p>}
          {updatePasswordMutation.isError && (
            <p className="text-sm font-medium text-red-500">{updatePasswordMutation.error.message}</p>
          )}
          <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white" disabled={updatePasswordMutation.isPending}>
            {updatePasswordMutation.isPending ? "변경 중..." : "비밀번호 변경"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

function ProfilePageSkeleton() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900 p-4">
      <div className="w-full max-w-md">
        <div className="flex space-x-1 border-b">
          <Skeleton className="h-10 w-1/2" />
          <Skeleton className="h-10 w-1/2" />
        </div>
        <Card className="mt-4">
          <CardHeader>
            <Skeleton className="h-8 w-3/4" />
            <Skeleton className="h-4 w-1/2 mt-2" />
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="space-y-2">
              <Skeleton className="h-4 w-1/4" />
              <Skeleton className="h-8 w-full" />
            </div>
            <div className="space-y-2">
              <Skeleton className="h-4 w-1/4" />
              <Skeleton className="h-8 w-full" />
            </div>
            <div className="space-y-2">
              <Skeleton className="h-4 w-1/4" />
              <Skeleton className="h-8 w-full" />
            </div>
            <Skeleton className="h-10 w-full mt-4" />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
