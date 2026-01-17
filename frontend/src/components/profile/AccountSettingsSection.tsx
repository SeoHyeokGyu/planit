"use client";

import { useState, useEffect } from "react";
import { useUpdateProfile, useUpdatePassword, useDeleteAccount } from "@/hooks/useUser";
import { UserProfile } from "@/types/user";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { User, ShieldCheck, UserX } from "lucide-react";

interface AccountSettingsSectionProps {
  user: UserProfile;
}

export default function AccountSettingsSection({ user }: AccountSettingsSectionProps) {
  return (
    <div className="space-y-8">
      <NicknameForm user={user} key={user.nickname} />
      <PasswordForm />
      <DeleteAccountForm />
    </div>
  );
}

function NicknameForm({ user }: { user: UserProfile }) {
  const [nickname, setNickname] = useState(user?.nickname || "");
  const updateProfileMutation = useUpdateProfile();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfileMutation.mutate({ nickname });
  };

  return (
    <Card className="bg-white border-0 shadow-sm rounded-2xl overflow-hidden">
      <CardHeader className="bg-gradient-to-r from-purple-50 to-pink-50 border-b border-gray-100">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-r from-purple-500 to-pink-500 rounded-lg flex items-center justify-center text-white shadow-md">
            <User className="w-5 h-5" />
          </div>
          <div>
            <CardTitle className="text-lg font-bold text-gray-900">닉네임 변경</CardTitle>
            <CardDescription className="text-gray-600 text-sm">
              새로운 닉네임을 설정합니다
            </CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent className="p-6">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="nickname" className="font-semibold text-gray-800">
              새 닉네임
            </Label>
            <Input
              id="nickname"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              disabled={updateProfileMutation.isPending}
              className="h-11 bg-white border-2 border-gray-200 hover:border-blue-400 focus:border-blue-500 transition-colors duration-200"
            />
          </div>
          {updateProfileMutation.isSuccess && (
            <p className="text-sm font-medium text-green-600">
              닉네임이 성공적으로 변경되었습니다.
            </p>
          )}
          {updateProfileMutation.isError && (
            <p className="text-sm font-medium text-red-500">
              {updateProfileMutation.error.message}
            </p>
          )}
          <Button
            type="submit"
            className="w-full h-11 bg-blue-600 hover:bg-blue-700 text-white font-semibold shadow-md hover:shadow-lg transition-all duration-200"
            disabled={updateProfileMutation.isPending}
          >
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
    <Card className="bg-white border-0 shadow-sm rounded-2xl overflow-hidden">
      <CardHeader className="bg-gradient-to-r from-red-50 to-orange-50 border-b border-gray-100">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-r from-red-500 to-orange-500 rounded-lg flex items-center justify-center text-white shadow-md">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <CardTitle className="text-lg font-bold text-gray-900">비밀번호 변경</CardTitle>
            <CardDescription className="text-gray-600 text-sm">
              새로운 비밀번호를 설정합니다
            </CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent className="p-6">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="currentPassword" className="font-semibold text-gray-800">
              현재 비밀번호
            </Label>
            <Input
              id="currentPassword"
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              disabled={updatePasswordMutation.isPending}
              className="h-11 bg-white border-2 border-gray-200 hover:border-blue-400 focus:border-blue-500 transition-colors duration-200"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="newPassword" className="font-semibold text-gray-800">
              새 비밀번호
            </Label>
            <Input
              id="newPassword"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              disabled={updatePasswordMutation.isPending}
              className="h-11 bg-white border-2 border-gray-200 hover:border-blue-400 focus:border-blue-500 transition-colors duration-200"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="confirmPassword" className="font-semibold text-gray-800">
              새 비밀번호 확인
            </Label>
            <Input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              disabled={updatePasswordMutation.isPending}
              className="h-11 bg-white border-2 border-gray-200 hover:border-blue-400 focus:border-blue-500 transition-colors duration-200"
            />
          </div>
          {updatePasswordMutation.isSuccess && (
            <p className="text-sm font-medium text-green-600">
              비밀번호가 성공적으로 변경되었습니다.
            </p>
          )}
          {passwordError && <p className="text-sm font-medium text-red-500">{passwordError}</p>}
          {updatePasswordMutation.isError && (
            <p className="text-sm font-medium text-red-500">
              {updatePasswordMutation.error.message}
            </p>
          )}
          <Button
            type="submit"
            className="w-full h-11 bg-blue-600 hover:bg-blue-700 text-white font-semibold shadow-md hover:shadow-lg transition-all duration-200"
            disabled={updatePasswordMutation.isPending}
          >
            {updatePasswordMutation.isPending ? "변경 중..." : "비밀번호 저장"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

function DeleteAccountForm() {
  const [password, setPassword] = useState("");
  const [isOpen, setIsOpen] = useState(false);
  const deleteAccountMutation = useDeleteAccount();

  const handleDelete = () => {
    if (!password) {
      return;
    }
    deleteAccountMutation.mutate(
      { password },
      {
        onSuccess: () => {
          setPassword("");
          setIsOpen(false);
        },
      }
    );
  };

  return (
    <Card className="bg-white border-2 border-red-200 shadow-sm rounded-2xl overflow-hidden">
      <CardHeader className="bg-gradient-to-r from-red-50 to-red-100 border-b border-red-200">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-r from-red-600 to-red-800 rounded-lg flex items-center justify-center text-white shadow-md">
            <UserX className="w-5 h-5" />
          </div>
          <div>
            <CardTitle className="text-lg font-bold text-gray-900">회원 탈퇴</CardTitle>
            <CardDescription className="text-gray-700 text-sm">
              계정을 영구적으로 삭제합니다. 이 작업은 되돌릴 수 없습니다
            </CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent className="p-6">
        <AlertDialog open={isOpen} onOpenChange={setIsOpen}>
          <AlertDialogTrigger asChild>
            <Button
              variant="destructive"
              className="w-full h-11 font-semibold shadow-md hover:shadow-lg transition-all duration-200"
            >
              회원 탈퇴
            </Button>
          </AlertDialogTrigger>
          <AlertDialogContent className="max-w-md">
            <AlertDialogHeader>
              <AlertDialogTitle className="text-xl font-bold text-gray-900">
                정말로 탈퇴하시겠습니까?
              </AlertDialogTitle>
              <AlertDialogDescription className="space-y-4 pt-2">
                <p className="text-gray-700 font-medium">
                  계정을 삭제하면 모든 데이터가 영구적으로 삭제되며, 복구할 수 없습니다.
                </p>
                <div className="space-y-2">
                  <Label htmlFor="deletePassword" className="font-semibold text-gray-800">
                    비밀번호 확인
                  </Label>
                  <Input
                    id="deletePassword"
                    type="password"
                    placeholder="비밀번호를 입력하세요"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    disabled={deleteAccountMutation.isPending}
                    className="h-11 bg-white border-2 border-gray-200 hover:border-red-400 focus:border-red-500 transition-colors duration-200"
                  />
                </div>
                {deleteAccountMutation.isError && (
                  <p className="text-sm font-medium text-red-500">
                    {deleteAccountMutation.error.message}
                  </p>
                )}
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel
                disabled={deleteAccountMutation.isPending}
                className="h-11 font-medium"
              >
                취소
              </AlertDialogCancel>
              <Button
                variant="destructive"
                onClick={handleDelete}
                disabled={!password || deleteAccountMutation.isPending}
                className="h-11 font-semibold shadow-md hover:shadow-lg transition-all duration-200"
              >
                {deleteAccountMutation.isPending ? "탈퇴 처리 중..." : "탈퇴하기"}
              </Button>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </CardContent>
    </Card>
  );
}
