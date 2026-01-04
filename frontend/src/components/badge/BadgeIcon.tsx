"use client";

import React from "react";
import {
  Footprints,
  Medal,
  Trophy,
  Flame,
  Zap, // Use Zap for 'Fire' as Lucide doesn't have exactly 'Fire' (Flame is close)
  User,
  Users,
  Crown,
  Coins,
  Banknote,
  Award,
  Star,
  Shield,
  HelpCircle,
} from "lucide-react";

interface BadgeIconProps {
  iconCode: string;
  className?: string;
}

export default function BadgeIcon({ iconCode, className }: BadgeIconProps) {
  switch (iconCode) {
    // Certification
    case "FOOTPRINT":
      return <Footprints className={className} />;
    case "RUNNING_SHOE": // Lucide doesn't have running shoe, map to Footprints or Activity
      return <Footprints className={className} />; // Fallback
    case "MEDAL":
      return <Medal className={className} />;
    case "TROPHY":
      return <Trophy className={className} />;

    // Streak
    case "FLAME":
      return <Flame className={className} />;
    case "FIRE":
      return <Zap className={className} />; // Zap or Flame
    case "PHOENIX":
      return <Award className={className} />; // Abstract mapping

    // Social
    case "USER":
      return <User className={className} />;
    case "GROUP":
      return <Users className={className} />;
    case "CROWN":
      return <Crown className={className} />;

    // Point
    case "COIN":
      return <Coins className={className} />;
    case "MONEY_BAG":
      return <Banknote className={className} />;

    default:
      return <HelpCircle className={className} />;
  }
}
