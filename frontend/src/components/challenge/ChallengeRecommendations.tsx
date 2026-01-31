"use client";

import { useEffect, useState } from "react";
import { challengeService } from "@/services/challengeService";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Sparkles, Plus, Loader2, RefreshCw } from "lucide-react";
import { themeStyles, badgeGradeColors, aiStyles } from "@/styles/common";
import { useNewChallengeRecommendations } from "@/hooks/useChallenge";
import { ChallengeRecommendationResponse } from "@/types/challenge";

interface ChallengeRecommendationsProps {
  onSelect: (challenge: ChallengeRecommendationResponse) => void;
}

export default function ChallengeRecommendations({ onSelect }: ChallengeRecommendationsProps) {
  const { 
    data: recommendations, 
    isLoading, 
    isError, 
    error, 
    refetch, 
    isRefetching 
  } = useNewChallengeRecommendations();

  const handleRefresh = () => {
    refetch();
  };

  const loading = isLoading || isRefetching;

  return (
    <div className="space-y-4">
      <div className={aiStyles.header}>
        <div className={aiStyles.titleGroup}>
          <div className={`${aiStyles.iconWrapper} ${themeStyles.info.bg}`}>
            <Sparkles className="w-4 h-4 text-white" />
          </div>
          <h3 className={aiStyles.title}>AI 추천 챌린지</h3>
        </div>
        <Button 
          variant="ghost" 
          size="sm" 
          onClick={handleRefresh} 
          disabled={loading}
          className={aiStyles.refreshBtn}
        >
          <RefreshCw className={`w-4 h-4 mr-1 ${loading ? "animate-spin" : ""}`} />
          새로고침
        </Button>
      </div>

      {loading && (!recommendations || recommendations.length === 0) ? (
        <Card className={aiStyles.emptyCard}>
          <CardContent className={aiStyles.centerContent}>
            <Loader2 className="w-8 h-8 animate-spin text-blue-500 mb-2" />
            <p className="text-sm text-gray-500 font-medium">AI가 챌린지 아이디어를 생각중입니다...</p>
          </CardContent>
        </Card>
      ) : isError ? (
         <Card className={aiStyles.errorCard}>
          <CardContent className={aiStyles.errorContent}>
            <p className={aiStyles.errorText}>{(error as Error)?.message || "추천 정보를 불러오는데 실패했습니다."}</p>
            <Button variant="link" size="sm" onClick={handleRefresh} className={aiStyles.retryLink}>
              다시 시도
            </Button>
          </CardContent>
        </Card>
      ) : !recommendations || recommendations.length === 0 ? (
        <Card className={aiStyles.emptyCard}>
          <CardContent className={aiStyles.centerContent}>
            <Sparkles className="w-8 h-8 text-gray-300 mb-2" />
            <p className="text-sm text-gray-500 font-medium">추천 가능한 챌린지가 없습니다.</p>
            <Button variant="link" size="sm" onClick={handleRefresh} className="text-blue-600 underline mt-1">
              다시 시도
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {recommendations.map((challenge, index) => (
            <Card 
              key={index} 
              className={aiStyles.card}
              onClick={() => onSelect(challenge)}
            >
              <CardContent className={aiStyles.cardContent}>
                <div className="flex justify-between items-start mb-2">
                  <div className={aiStyles.badgeGroup}>
                    <Badge variant="secondary" className="bg-indigo-50 text-indigo-700 text-xs">
                      {challenge.category}
                    </Badge>
                    <Badge variant="outline" className={`text-xs ${
                      challenge.difficulty === "HARD" ? badgeGradeColors.GOLD :
                      (challenge.difficulty === "MEDIUM" || challenge.difficulty === "NORMAL") ? badgeGradeColors.SILVER :
                      badgeGradeColors.BRONZE
                    }`}>
                      {challenge.difficulty}
                    </Badge>
                  </div>
                </div>
                
                <h4 className={aiStyles.cardTitle}>
                  {challenge.title}
                </h4>
                <p className={aiStyles.cardDesc}>
                  {challenge.description}
                </p>

                <div className={aiStyles.tipBox}>
                  <p className={aiStyles.tipText}>
                    <span className={aiStyles.tipLabel}>Tip:</span> {challenge.reason}
                  </p>
                </div>

                <Button 
                  size="sm" 
                  variant="outline" 
                  className={aiStyles.actionBtn}
                  onClick={(e) => {
                    e.stopPropagation();
                    onSelect(challenge);
                  }}
                >
                  <Plus className="w-3 h-3 mr-1" />
                  이 아이디어 사용하기
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
