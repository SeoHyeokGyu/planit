"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image"; // Import Image component
import { useState } from "react"; // Ensure useState is imported
import { useAuthStore } from "@/stores/authStore";
import {
  useUserProfile,
  useUpdateProfile,
  useUpdatePassword,
} from "@/hooks/useUser";
import { useLogout } from "@/hooks/useAuth";
import { useCertificationsByUser, useCertificationsByDateRange } from "@/hooks/useCertification"; // Import the new hook
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
import { CertificationCalendar } from "@/components/CertificationCalendar"; // Import Calendar
import { LayoutList, Calendar as CalendarIcon } from "lucide-react"; // Import icons

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
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="info" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">프로필 정보</TabsTrigger>
          <TabsTrigger value="update" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">회원정보 변경</TabsTrigger>
          <TabsTrigger value="certifications" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">내 인증 목록</TabsTrigger>
        </TabsList>

        <TabsContent value="info">
          <ProfileInfoTab user={user} />
        </TabsContent>
        <TabsContent value="update">
          <UpdateInfoTab user={user} />
        </TabsContent>
        <TabsContent value="certifications">
          <MyCertificationsTab userLoginId={user.loginId} />
        </TabsContent>
      </Tabs>
    </div>
  );
}

// --- Child Components for Tabs ---

function MyCertificationsTab({ userLoginId }: { userLoginId: string }) {
  const router = useRouter();
  const [viewMode, setViewMode] = useState<"list" | "calendar">("list");
  const [page, setPage] = useState(0);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [currentMonth, setCurrentMonth] = useState(new Date());

  // 리스트 뷰 데이터 조회 (페이징)
  const { 
    data: listData, 
    isLoading: isListLoading, 
    error: listError 
  } = useCertificationsByUser(userLoginId, page, 10, { enabled: viewMode === "list" });

  // 달력 뷰 데이터 조회 (월별 범위)
  const startOfMonth = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), 1);
  const endOfMonth = new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 0, 23, 59, 59); // 마지막 날의 끝
  
  const { 
    data: calendarData, 
    isLoading: isCalendarLoading, 
    error: calendarError 
  } = useCertificationsByDateRange(
    userLoginId, 
    startOfMonth.toISOString(), 
    endOfMonth.toISOString(),
    { enabled: viewMode === "calendar" }
  );

  const certificationsList = listData?.content || [];
  const totalPages = listData?.totalPages || 0;
  
  const certificationsCalendar = calendarData || [];

  // 날짜 포맷팅 함수 (YYYY-MM-DD)
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
  };

  // 선택된 날짜의 인증 목록 필터링 (달력 뷰 데이터에서)
  const selectedDateCertifications = selectedDate 
    ? certificationsCalendar.filter(c => formatDate(c.createdAt) === selectedDate)
    : [];

  const handleDateClick = (date: string) => {
    setSelectedDate(date);
  };

  const isLoading = viewMode === "list" ? isListLoading : isCalendarLoading;
  const error = viewMode === "list" ? listError : calendarError;

  if (isLoading && !calendarData && !listData) { // 데이터가 아예 없을 때만 로딩 표시
    return (
      <Card className="mt-4">
        <CardHeader>
          <CardTitle>내 인증 목록</CardTitle>
          <CardDescription>회원님이 작성한 인증 목록입니다.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <Skeleton className="h-20 w-full" />
          <Skeleton className="h-20 w-full" />
          <Skeleton className="h-20 w-full" />
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className="mt-4">
        <CardHeader>
          <CardTitle>내 인증 목록</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-red-500">인증 목록을 불러오는 데 실패했습니다: {(error as Error).message}</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="mt-4">
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <div className="space-y-1">
          <CardTitle>내 인증 목록</CardTitle>
          <CardDescription>회원님이 작성한 인증 목록입니다.</CardDescription>
        </div>
        <div className="flex bg-gray-100 dark:bg-gray-800 p-1 rounded-lg">
          <Button
            variant={viewMode === "list" ? "default" : "ghost"}
            size="sm"
            onClick={() => setViewMode("list")}
            className="h-8 px-2"
          >
            <LayoutList className="h-4 w-4 mr-1" /> 리스트
          </Button>
          <Button
            variant={viewMode === "calendar" ? "default" : "ghost"}
            size="sm"
            onClick={() => setViewMode("calendar")}
            className="h-8 px-2"
          >
            <CalendarIcon className="h-4 w-4 mr-1" /> 달력
          </Button>
        </div>
      </CardHeader>
      
      <CardContent className="space-y-4 pt-4">
        {viewMode === "calendar" ? (
          <div className="space-y-4">
            <CertificationCalendar 
              certifications={certificationsCalendar} 
              onDateClick={handleDateClick}
              selectedDate={selectedDate}
              currentDate={currentMonth}
              onMonthChange={setCurrentMonth}
            />
            
            {/* 선택된 날짜의 인증 목록 표시 */}
            {selectedDate && (
              <div className="mt-4 border-t pt-4 animate-in fade-in slide-in-from-top-2">
                <h3 className="text-sm font-semibold mb-2 text-gray-700 dark:text-gray-300">
                  {selectedDate} 인증 ({selectedDateCertifications.length}개)
                </h3>
                {selectedDateCertifications.length === 0 ? (
                  <p className="text-sm text-gray-500">이 날짜에는 인증 기록이 없습니다.</p>
                ) : (
                  <div className="grid gap-2">
                    {selectedDateCertifications.map((cert) => (
                      <Card key={cert.id} className="p-3 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors cursor-pointer"
                        onClick={() => router.push(`/certification/${cert.id}`)}>
                        <div className="flex justify-between items-center">
                          <div>
                            <p className="font-medium text-sm">{cert.title}</p>
                            <p className="text-xs text-gray-500">{cert.challengeTitle}</p>
                          </div>
                          {cert.photoUrl && (
                             <div className="w-10 h-10 relative rounded overflow-hidden">
                               <Image src={cert.photoUrl} alt="thumb" layout="fill" objectFit="cover" />
                             </div>
                          )}
                        </div>
                      </Card>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        ) : (
          <>
            {certificationsList.length === 0 ? (
              <p className="text-center text-gray-500">아직 작성된 인증이 없습니다.</p>
            ) : (
              certificationsList.map((cert) => (
                <Card key={cert.id} className="p-4 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors cursor-pointer"
                  onClick={() => router.push(`/certification/${cert.id}`)}>
                  <CardTitle className="text-lg">{cert.title}</CardTitle>
                  <CardDescription className="text-sm">
                    챌린지: {cert.challengeTitle} | {new Date(cert.createdAt).toLocaleDateString()}
                  </CardDescription>
                  {cert.photoUrl && (
                    <div className="mt-2 w-full h-32 relative">
                      <Image src={cert.photoUrl} alt={cert.title} layout="fill" objectFit="cover" className="rounded-md" />
                    </div>
                  )}
                </Card>
              ))
            )}
            {totalPages > 1 && (
              <div className="flex justify-center items-center gap-2 mt-4">
                <Button
                  variant="outline"
                  onClick={() => setPage((prev) => Math.max(0, prev - 1))}
                  disabled={page === 0}
                >
                  이전
                </Button>
                <span>{page + 1} / {totalPages}</span>
                <Button
                  variant="outline"
                  onClick={() => setPage((prev) => Math.min(totalPages - 1, prev + 1))}
                  disabled={page === totalPages - 1}
                >
                  다음
                </Button>
              </div>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}

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
