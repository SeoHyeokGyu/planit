import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";
import { PointStatisticsResponse, StatisticsDateRange } from "@/types/point";

export const pointService = {
  async getPointStatistics(
    dateRange: StatisticsDateRange
  ): Promise<ApiResponse<PointStatisticsResponse>> {
    const params = new URLSearchParams({
      startDate: dateRange.startDate,
      endDate: dateRange.endDate,
    });
    return api.get(`/api/points/me/statistics?${params.toString()}`);
  },
};
