"use client";

import { useState, useEffect } from "react";
import { useUpdateProfile, useUpdatePassword } from "@/hooks/useUser";
import { UserProfile } from "@/types/user";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { User, ShieldCheck } from "lucide-react";

interface AccountSettingsSectionProps {
  user: UserProfile;
}

export default function AccountSettingsSection({ user }: AccountSettingsSectionProps) {
  return (
    <div className="space-y-8">
      <NicknameForm user={user} />
      <PasswordForm />
    </div>
  );
}

function NicknameForm({ user }: { user: UserProfile }) {
  const [nickname, setNickname] = useState(user?.nickname || "");
  const updateProfileMutation = useUpdateProfile();

  useEffect(() => {
    if (user?.nickname) {
      setNickname(user.nickname);
    }
  }, [user]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfileMutation.mutate({ nickname });
  };

  return (
    <Card className="shadow-lg rounded-xl dark:bg-gray-800/50">
      <CardHeader className="flex flex-row items-center space-x-3">
        <div className="w-10 h-10 bg-gradient-to-r from-purple-500 to-pink-500 rounded-lg flex items-center justify-center text-white">
          <User className="w-6 h-6" />
        </div>
        <div>
          <CardTitle className="text-xl font-bold">닉네임 변경</CardTitle>
          <CardDescription>새로운 닉네임을 설정합니다.</CardDescription>
        </div>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="nickname" className="font-semibold">새 닉네임</Label>
            <Input
              id="nickname"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              disabled={updateProfileMutation.isPending}
              className="dark:bg-gray-700"
            />
          </div>
          {updateProfileMutation.isSuccess && (
            <p className="text-sm font-medium text-green-600 dark:text-green-400">닉네임이 성공적으로 변경되었습니다.</p>
          )}
          {updateProfileMutation.isError && (
            <p className="text-sm font-medium text-red-500 dark:text-red-400">{updateProfileMutation.error.message}</p>
          )}
          <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold" disabled={updateProfileMutation.isPending}>
            {updateProfileMutation.isPending ? "변경 중..." : "닉네임 저장"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

function PasswordForm() {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const updatePasswordMutation = useUpdatePassword();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      setPasswordError("새 비밀번호가 일치하지 않습니다.");
      return;
    }
    if (newPassword.length < 6) {
      setPasswordError("비밀번호는 6자 이상이어야 합니다.");
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
    <Card className="shadow-lg rounded-xl dark:bg-gray-800/50">
      <CardHeader className="flex flex-row items-center space-x-3">
        <div className="w-10 h-10 bg-gradient-to-r from-red-500 to-orange-500 rounded-lg flex items-center justify-center text-white">
          <ShieldCheck className="w-6 h-6" />
        </div>
        <div>
          <CardTitle className="text-xl font-bold">비밀번호 변경</CardTitle>
          <CardDescription>새로운 비밀번호를 설정합니다.</CardDescription>
        </div>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="currentPassword">현재 비밀번호</Label>
            <Input
              id="currentPassword"
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              disabled={updatePasswordMutation.isPending}
              className="dark:bg-gray-700"
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
              className="dark:bg-gray-700"
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
              className="dark:bg-gray-700"
            />
          </div>
          {updatePasswordMutation.isSuccess && (
            <p className="text-sm font-medium text-green-600 dark:text-green-400">비밀번호가 성공적으로 변경되었습니다.</p>
          )}
          {passwordError && <p className="text-sm font-medium text-red-500 dark:text-red-400">{passwordError}</p>}
          {updatePasswordMutation.isError && (
            <p className="text-sm font-medium text-red-500 dark:text-red-400">{updatePasswordMutation.error.message}</p>
          )}
          <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold" disabled={updatePasswordMutation.isPending}>
            {updatePasswordMutation.isPending ? "변경 중..." : "비밀번호 저장"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
