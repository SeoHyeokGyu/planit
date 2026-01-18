"use client";

import { useState } from "react";
import { CertificationResponse } from "@/types/certification";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { calendarStyles } from "@/styles/common";

interface CertificationCalendarProps {
  certifications: CertificationResponse[];
  onDateClick: (date: string) => void;
  selectedDate: string | null;
  currentDate: Date;
  onMonthChange: (date: Date) => void;
}

export function CertificationCalendar({
  certifications,
  onDateClick,
  selectedDate,
  currentDate,
  onMonthChange,
}: CertificationCalendarProps) {
  // 현재 월의 첫 날과 마지막 날 계산
  const firstDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
  const lastDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);

  // 달력 표시를 위한 이전/다음 달 이동 함수
  const prevMonth = () => {
    onMonthChange(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
  };

  const nextMonth = () => {
    onMonthChange(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
  };

  // 날짜 포맷팅 (YYYY-MM-DD)
  const formatDate = (date: Date) => {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
  };

  // 인증 데이터 날짜 매핑 (Set으로 빠른 조회)
  const certificationDates = new Set(
    certifications.map((cert) => {
      const date = new Date(cert.createdAt);
      return formatDate(date);
    })
  );

  // 달력 그리드 생성
  const generateCalendarDays = () => {
    const days = [];
    const startDay = firstDayOfMonth.getDay(); // 0: 일요일, 1: 월요일...
    const totalDays = lastDayOfMonth.getDate();

    // 빈 칸 채우기 (첫 주 시작 전)
    for (let i = 0; i < startDay; i++) {
      days.push(<div key={`empty-${i}`} className="h-14 bg-transparent"></div>);
    }

    // 날짜 채우기
    for (let i = 1; i <= totalDays; i++) {
      const dateObj = new Date(currentDate.getFullYear(), currentDate.getMonth(), i);
      const dateStr = formatDate(dateObj);
      const hasCertification = certificationDates.has(dateStr);
      const isSelected = selectedDate === dateStr;

      // 해당 날짜의 인증 갯수 (선택적 기능)
      const count = certifications.filter((c) => {
        const d = new Date(c.createdAt);
        return formatDate(d) === dateStr;
      }).length;

      days.push(
        <div
          key={dateStr}
          onClick={() => onDateClick(dateStr)}
          className={`${calendarStyles.day}

                  ${isSelected ? calendarStyles.selected : ""}

                  ${hasCertification ? calendarStyles.hasEvent : calendarStyles.empty}`}
        >
          <span>{i}</span>

          {hasCertification && (
            <div className="mt-1 w-1.5 h-1.5 rounded-full bg-green-500 />
          )}
          {hasCertification && count > 1 && (
            <span className="absolute top-0.5 right-1 text-[10px] text-green-600 opacity-70">
              x{count}
            </span>
          )}
        </div>
      );
    }

    return days;
  };

  const WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"];

  return (
    <Card className="w-full">
      <CardHeader className="flex flex-row items-center justify-between py-4">
        <Button variant="ghost" size="icon" onClick={prevMonth}>
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <CardTitle className="text-lg font-bold">
          {currentDate.getFullYear()}년 {currentDate.getMonth() + 1}월
        </CardTitle>
        <Button variant="ghost" size="icon" onClick={nextMonth}>
          <ChevronRight className="h-4 w-4" />
        </Button>
      </CardHeader>
      <CardContent>
        {/* 요일 헤더 */}
        <div className="grid grid-cols-7 mb-2 text-center">
          {WEEKDAYS.map((day) => (
            <div key={day} className="text-xs font-medium text-gray-500">
              {day}
            </div>
          ))}
        </div>
        {/* 날짜 그리드 */}
        <div className="grid grid-cols-7 gap-1">{generateCalendarDays()}</div>
        <div className="mt-4 flex items-center justify-end gap-2 text-xs text-gray-500">
          <div className="w-3 h-3 bg-green-100 border border-green-300 rounded-sm"></div>
          <span>인증 완료</span>
        </div>
      </CardContent>
    </Card>
  );
}
