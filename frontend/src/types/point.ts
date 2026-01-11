export interface DailyPointStatistics {
  date: string;
  pointsEarned: number;
  cumulativePoints: number;
  transactionCount: number;
}

export interface PointStatisticsSummary {
  totalPointsEarned: number;
  averagePointsPerDay: number;
  totalTransactions: number;
  periodStart: string;
  periodEnd: string;
}

export interface PointStatisticsResponse {
  statistics: DailyPointStatistics[];
  summary: PointStatisticsSummary;
}

export interface StatisticsDateRange {
  startDate: string;
  endDate: string;
}
