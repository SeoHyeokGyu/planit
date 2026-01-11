"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { useCertificationsByUser, useCertificationsByDateRange } from "@/hooks/useCertification";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { CertificationCalendar } from "@/components/CertificationCalendar";
import { LayoutList, Calendar as CalendarIcon, Activity } from "lucide-react";
import { pageHeaderStyles, iconGradients } from "@/styles/pageHeader";
import { cn } from "@/lib/utils";

interface CertificationsSectionProps {
  userLoginId: string;
}

export default function CertificationsSection({ userLoginId }: CertificationsSectionProps) {
  const router = useRouter();
  const [viewMode, setViewMode] = useState<"list" | "calendar">("list");
  const [page, setPage] = useState(0);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [currentMonth, setCurrentMonth] = useState(new Date());

  const { data: listData, isLoading: isListLoading, error: listError } = useCertificationsByUser(userLoginId, page, 10, { enabled: viewMode === "list" });
  
  const startOfMonth = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), 1);
  const endOfMonth = new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 0, 23, 59, 59);
  
  const { data: calendarData, isLoading: isCalendarLoading, error: calendarError } = useCertificationsByDateRange(
    userLoginId, 
    startOfMonth.toISOString(), 
    endOfMonth.toISOString(),
    { enabled: viewMode === "calendar" }
  );

  const certificationsList = listData?.content || [];
  const totalPages = listData?.totalPages || 0;
  const certificationsCalendar = calendarData || [];

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
  };

  const selectedDateCertifications = selectedDate 
    ? certificationsCalendar.filter(c => formatDate(c.createdAt) === selectedDate)
    : [];

  const handleDateClick = (date: string) => {
    setSelectedDate(date);
  };

  const isLoading = viewMode === "list" ? isListLoading : isCalendarLoading;
  const error = viewMode === "list" ? listError : calendarError;

  if (error) {
    return (
      <Card className="shadow-lg rounded-xl">
        <CardHeader>
          <CardTitle className="text-gray-900">내 인증 목록</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-red-500">인증 목록을 불러오는 데 실패했습니다: {(error as Error).message}</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="shadow-lg rounded-xl bg-white">
      <CardHeader className="flex flex-col sm:flex-row items-start sm:items-center sm:justify-between pb-4">
        <div className="flex items-center space-x-3 mb-4 sm:mb-0">
          <div className={`${pageHeaderStyles.iconBase} ${iconGradients.certification}`}>
            <Activity className="w-6 h-6" />
          </div>
          <div>
            <CardTitle className="text-2xl font-bold text-gray-900">나의 활동</CardTitle>
            <CardDescription className="text-gray-600">나의 모든 챌린지 인증 기록입니다.</CardDescription>
          </div>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setViewMode("list")}
            className={cn(
              pageHeaderStyles.tabButton.base,
              viewMode === "list"
                ? pageHeaderStyles.tabButton.active
                : pageHeaderStyles.tabButton.inactive
            )}
          >
            <LayoutList className="h-4 w-4 mr-2" /> 리스트
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setViewMode("calendar")}
            className={cn(
              pageHeaderStyles.tabButton.base,
              viewMode === "calendar"
                ? pageHeaderStyles.tabButton.active
                : pageHeaderStyles.tabButton.inactive
            )}
          >
            <CalendarIcon className="h-4 w-4 mr-2" /> 캘린더
          </Button>
        </div>
      </CardHeader>
      
      <CardContent>
        {isLoading && (!listData && !calendarData) ? (
          <div className="space-y-4">
            <Skeleton className="h-24 w-full rounded-lg" />
            <Skeleton className="h-24 w-full rounded-lg" />
            <Skeleton className="h-24 w-full rounded-lg" />
          </div>
        ) : viewMode === "calendar" ? (
          <div className="space-y-4">
            <CertificationCalendar 
              certifications={certificationsCalendar} 
              onDateClick={handleDateClick}
              selectedDate={selectedDate}
              currentDate={currentMonth}
              onMonthChange={setCurrentMonth}
            />
            
            {selectedDate && (
              <div className="mt-4 border-t pt-4 animate-in fade-in slide-in-from-top-2">
                <h3 className="text-md font-semibold mb-3 text-gray-800">
                  {selectedDate} 인증 ({selectedDateCertifications.length}개)
                </h3>
                {selectedDateCertifications.length === 0 ? (
                  <p className="text-sm text-center text-gray-600 py-4">이 날짜에는 인증 기록이 없습니다.</p>
                ) : (
                  <div className="grid gap-3">
                    {selectedDateCertifications.map((cert) => (
                      <Card key={cert.id} className="p-3 hover:bg-blue-50 transition-colors cursor-pointer rounded-lg"
                        onClick={() => router.push(`/certification/${cert.id}`)}>
                        <div className="flex justify-between items-center">
                          <div>
                            <p className="font-semibold text-md text-gray-900">{cert.title}</p>
                            <p className="text-sm text-gray-600">{cert.challengeTitle}</p>
                          </div>
                          {cert.photoUrl && (
                             <div className="w-12 h-12 relative rounded-md overflow-hidden">
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
              <div className="text-center py-12">
                <p className="text-gray-600">아직 작성된 인증이 없습니다.</p>
                <Button className="mt-4" onClick={() => router.push("/challenge")}>챌린지 시작하기</Button>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {certificationsList.map((cert) => (
                  <Card key={cert.id} className="p-4 hover:shadow-md transition-shadow cursor-pointer bg-white"
                    onClick={() => router.push(`/certification/${cert.id}`)}>
                    {cert.photoUrl && (
                      <div className="mb-3 w-full h-40 relative">
                        <Image src={cert.photoUrl} alt={cert.title} layout="fill" objectFit="cover" className="rounded-md" />
                      </div>
                    )}
                    <CardTitle className="text-lg font-semibold text-gray-900">{cert.title}</CardTitle>
                    <CardDescription className="text-sm mt-1 text-gray-600">
                      {cert.challengeTitle} | {new Date(cert.createdAt).toLocaleDateString()}
                    </CardDescription>
                  </Card>
                ))}
              </div>
            )}
            {totalPages > 1 && (
              <div className="flex justify-center items-center gap-2 mt-6">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((prev) => Math.max(0, prev - 1))}
                  disabled={page === 0}
                >
                  이전
                </Button>
                <span className="text-sm font-medium">{page + 1} / {totalPages}</span>
                <Button
                  variant="outline"
                  size="sm"
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
