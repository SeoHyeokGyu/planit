"use client";

import React from "react";
import { BadgeResponse } from "@/types/badge";
import BadgeIcon from "./BadgeIcon";
import { cn } from "@/lib/utils";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { badgeGradeColors } from "@/styles/common";

interface BadgeItemProps {
  badge: BadgeResponse;
}

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
                  badgeGradeColors[badge.grade]
                )
              : "bg-gray-100 border-gray-300 text-gray-700 grayscale"
          )}
        >
          <div
            className={cn(
              "w-12 h-12 flex items-center justify-center rounded-full mb-3 transition-transform duration-300",
              isAcquired
                ? "bg-white/80 group-hover:scale-110"
                : "bg-gray-300"
            )}
          >
            <BadgeIcon
              iconCode={badge.iconCode}
              className={cn("w-6 h-6", !isAcquired && "text-gray-600")}
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
              <div className="flex justify-between text-[10px] text-gray-700 mb-1 font-medium">
                <span>진행률</span>
                <span>{Math.floor((badge.currentValue / badge.requiredValue) * 100)}%</span>
              </div>
              <div className="w-full h-1.5 bg-gray-300 rounded-full overflow-hidden">
                <div
                  className="h-full bg-blue-600 transition-all duration-300"
                  style={{
                    width: `${Math.min(100, (badge.currentValue / badge.requiredValue) * 100)}%`,
                  }}
                />
              </div>
              <div className="text-[10px] text-center text-gray-600 mt-1 font-medium">
                {badge.currentValue} / {badge.requiredValue}
              </div>
            </div>
          )}
        </div>
      </TooltipTrigger>
      <TooltipContent className="max-w-[200px] text-center p-3 bg-white">
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
