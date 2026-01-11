"use client";

import React from "react";
import { BadgeResponse, BadgeGrade } from "@/types/badge";
import BadgeIcon from "./BadgeIcon";
import { cn } from "@/lib/utils";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";

interface BadgeItemProps {
  badge: BadgeResponse;
}

const gradeColors: Record<BadgeGrade, string> = {
  [BadgeGrade.BRONZE]:
    "text-amber-600 bg-amber-100 border-amber-200 dark:bg-amber-900/20 dark:border-amber-700/50 dark:text-amber-500",
  [BadgeGrade.SILVER]:
    "text-slate-500 bg-slate-100 border-slate-200 dark:bg-slate-800 dark:border-slate-700 dark:text-slate-400",
  [BadgeGrade.GOLD]:
    "text-yellow-600 bg-yellow-100 border-yellow-200 dark:bg-yellow-900/20 dark:border-yellow-700/50 dark:text-yellow-500",
  [BadgeGrade.PLATINUM]:
    "text-cyan-600 bg-cyan-100 border-cyan-200 dark:bg-cyan-900/20 dark:border-cyan-700/50 dark:text-cyan-400",
};

export default React.memo(function BadgeItem({ badge }: BadgeItemProps) {
  const isAcquired = badge.isAcquired;

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <div
          tabIndex={0}
          role="img"
          aria-label={`${badge.name} 배지 - ${isAcquired ? "획득함" : "미획득"}`}
          className={cn(
            "flex flex-col items-center justify-center p-4 rounded-xl border-2 transition-all duration-300 relative group cursor-default focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent",
            isAcquired
              ? cn(
                  "border-transparent shadow-sm hover:shadow-md hover:-translate-y-1",
                  gradeColors[badge.grade]
                )
              : "bg-gray-100 border-gray-300 text-gray-700 dark:bg-gray-800/50 dark:border-gray-700 dark:text-gray-400 grayscale"
          )}
        >
          <div
            className={cn(
              "w-12 h-12 flex items-center justify-center rounded-full mb-3 transition-transform duration-300",
              isAcquired
                ? "bg-white/80 dark:bg-black/20 group-hover:scale-110"
                : "bg-gray-300 dark:bg-gray-700"
            )}
          >
            <BadgeIcon
              iconCode={badge.iconCode}
              className={cn(
                "w-6 h-6",
                !isAcquired && "text-gray-600 dark:text-gray-400"
              )}
            />
          </div>

          <h3 className="font-bold text-sm text-center mb-1 line-clamp-1 break-all px-1">
            {badge.name}
          </h3>

          {isAcquired && badge.acquiredAt ? (
            <span className="text-[10px] opacity-70 font-medium whitespace-nowrap">
              {new Date(badge.acquiredAt).toLocaleString("ko-KR", {
                year: "numeric",
                month: "2-digit",
                day: "2-digit",
                hour: "numeric",
                minute: "2-digit",
                hour12: true,
              })}
            </span>
          ) : (
            <div className="w-full mt-2 px-1">
              <div className="flex justify-between text-[10px] text-gray-700 dark:text-gray-300 mb-1 font-medium">
                <span>진행률</span>
                <span>
                  {Math.floor((badge.currentValue / badge.requiredValue) * 100)}%
                </span>
              </div>
              <div className="w-full h-1.5 bg-gray-300 dark:bg-gray-700 rounded-full overflow-hidden">
                <div
                  className="h-full bg-blue-600 dark:bg-blue-500 transition-all duration-300"
                  style={{
                    width: `${Math.min(
                      100,
                      (badge.currentValue / badge.requiredValue) * 100
                    )}%`,
                  }}
                />
              </div>
              <div className="text-[10px] text-center text-gray-600 dark:text-gray-400 mt-1 font-medium">
                {badge.currentValue} / {badge.requiredValue}
              </div>
            </div>
          )}
        </div>
      </TooltipTrigger>
      <TooltipContent className="max-w-[200px] text-center p-3 bg-white dark:bg-gray-950">
        <p className="font-bold mb-1">{badge.name}</p>
        <p className="text-xs text-muted-foreground">{badge.description}</p>
        {!isAcquired && (
          <p className="text-xs text-blue-500 mt-2 font-medium">
            현재: {badge.currentValue} / 목표: {badge.requiredValue}
          </p>
        )}
      </TooltipContent>
    </Tooltip>
  );
});
