export interface DailyPointStatistics {
  date: string;
  pointsEarned: number;
  cumulativePoints: number;
  transactionCount: number;
}

export interface DailyExperienceStatistics {
  date: string;
  experienceEarned: number;
  cumulativeExperience: number;
  level: number;
  levelUpOccurred: boolean;
  transactionCount: number;
}

export interface PointStatisticsSummary {
  totalPointsEarned: number;
  averagePointsPerDay: number;
  totalTransactions: number;
  periodStart: string;
  periodEnd: string;
}

export interface ExperienceStatisticsSummary {
  totalExperienceEarned: number;
  averageExperiencePerDay: number;
  totalTransactions: number;
  levelUpsCount: number;
  startLevel: number;
  endLevel: number;
  periodStart: string;
  periodEnd: string;
}

export interface PointStatisticsResponse {
  statistics: DailyPointStatistics[];
  summary: PointStatisticsSummary;
}

export interface ExperienceStatisticsResponse {
  statistics: DailyExperienceStatistics[];
  summary: ExperienceStatisticsSummary;
}

export interface StatisticsDateRange {
  startDate: string;
  endDate: string;
}
