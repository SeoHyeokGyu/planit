export enum BadgeGrade {
  BRONZE = "BRONZE",
  SILVER = "SILVER",
  GOLD = "GOLD",
  PLATINUM = "PLATINUM",
}

export interface BadgeResponse {
  code: string;
  name: string;
  description: string;
  iconCode: string;
  grade: BadgeGrade;
  isAcquired: boolean;
  acquiredAt?: string; // ISO Date string
}
