"use client";

import { useState, useMemo } from "react";
import { useAuthStore } from "@/stores/authStore";
import { usePointStatistics } from "@/hooks/usePoint";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { Zap, Calendar } from "lucide-react";

export default function StatsPage() {
  const token = useAuthStore((state) => state.isAuthenticated);
  const [period, setPeriod] = useState<"7" | "30" | "90">("7");

  // 날짜 범위 계산
  const dateRange = useMemo(() => {
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - parseInt(period) + 1);

    return {
      startDate: start.toISOString().split("T")[0],
      endDate: end.toISOString().split("T")[0],
    };
  }, [period]);

  const { data: pointStats, isLoading: pointLoading } = usePointStatistics(dateRange);

  if (!token) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <main className="max-w-7xl mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">포인트 통계</h1>
              <p className="text-gray-600 mt-1">포인트 활동을 확인하세요</p>
            </div>
            <Select value={period} onValueChange={(v) => setPeriod(v as "7" | "30" | "90")}>
              <SelectTrigger className="w-[180px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="7">최근 7일</SelectItem>
                <SelectItem value="30">최근 30일</SelectItem>
                <SelectItem value="90">최근 90일</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        {/* 요약 카드 */}
        {!pointLoading && pointStats && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-gray-600">총 포인트 획득</CardTitle>
                <Zap className="h-4 w-4 text-yellow-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {pointStats.summary.totalPointsEarned.toLocaleString()}
                </div>
                <p className="text-xs text-gray-500 mt-1">
                  일평균 {pointStats.summary.averagePointsPerDay.toFixed(1)}
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-gray-600">총 활동</CardTitle>
                <Calendar className="h-4 w-4 text-green-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{pointStats.summary.totalTransactions}</div>
                <p className="text-xs text-gray-500 mt-1">포인트 거래 수</p>
              </CardContent>
            </Card>
          </div>
        )}

        {/* 포인트 차트 */}
        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>일별 포인트 획득</CardTitle>
              <CardDescription>
                기간: {dateRange.startDate} ~ {dateRange.endDate}
              </CardDescription>
            </CardHeader>
            <CardContent>
              {pointLoading ? (
                <div className="h-[300px] flex items-center justify-center">
                  <p className="text-gray-500">로딩 중...</p>
                </div>
              ) : pointStats ? (
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={pointStats.statistics}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                      dataKey="date"
                      tickFormatter={(value) => {
                        const date = new Date(value);
                        return `${date.getMonth() + 1}/${date.getDate()}`;
                      }}
                    />
                    <YAxis />
                    <Tooltip
                      labelFormatter={(value) => `날짜: ${value}`}
                      formatter={(value) => [`${value}P`, "획득 포인트"]}
                    />
                    <Legend />
                    <Bar dataKey="pointsEarned" fill="#facc15" name="획득 포인트" />
                  </BarChart>
                </ResponsiveContainer>
              ) : null}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>누적 포인트 추이</CardTitle>
              <CardDescription>시간에 따른 누적 포인트 변화</CardDescription>
            </CardHeader>
            <CardContent>
              {pointLoading ? (
                <div className="h-[300px] flex items-center justify-center">
                  <p className="text-gray-500">로딩 중...</p>
                </div>
              ) : pointStats ? (
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={pointStats.statistics}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                      dataKey="date"
                      tickFormatter={(value) => {
                        const date = new Date(value);
                        return `${date.getMonth() + 1}/${date.getDate()}`;
                      }}
                    />
                    <YAxis />
                    <Tooltip
                      labelFormatter={(value) => `날짜: ${value}`}
                      formatter={(value) => [`${value}P`, "누적 포인트"]}
                    />
                    <Legend />
                    <Line
                      type="monotone"
                      dataKey="cumulativePoints"
                      stroke="#facc15"
                      strokeWidth={2}
                      name="누적 포인트"
                    />
                  </LineChart>
                </ResponsiveContainer>
              ) : null}
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  );
}
