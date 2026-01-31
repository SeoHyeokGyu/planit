"use client";

import { useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Sparkles, Plus, Loader2, RefreshCw } from "lucide-react";
import { themeStyles, badgeGradeColors, aiStyles, aiRecommendationStyles } from "@/styles/common";
import { useNewChallengeRecommendations, useNewChallengeRecommendationsWithQuery } from "@/hooks/useChallenge";
import { ChallengeRecommendationResponse } from "@/types/challenge";

interface ChallengeRecommendationsProps {
  onSelect: (challenge: ChallengeRecommendationResponse) => void;
}

export default function ChallengeRecommendations({ onSelect }: ChallengeRecommendationsProps) {
  // AI Recommendation States
  const [aiPrompt, setAiPrompt] = useState("");
  const [activeAiQuery, setActiveAiQuery] = useState<string>("");
  const [showRecommendations, setShowRecommendations] = useState(false);

  const {
    data: defaultRecommendations,
    isLoading: isDefaultRecLoading,
    refetch: refetchDefaultRec,
    isRefetching: isDefaultRecRefetching
  } = useNewChallengeRecommendations({ enabled: showRecommendations });

  const {
    data: queryRecommendations,
    isLoading: isQueryRecLoading,
    refetch: refetchQueryRec,
    isRefetching: isQueryRecRefetching
  } = useNewChallengeRecommendationsWithQuery(activeAiQuery);

  const isRecLoading = activeAiQuery
    ? isQueryRecLoading || isQueryRecRefetching
    : isDefaultRecLoading || isDefaultRecRefetching;

  const recommendations = activeAiQuery ? queryRecommendations : defaultRecommendations;

  const handleAiSearch = () => {
    if (!aiPrompt.trim()) return;
    setActiveAiQuery(aiPrompt);
    setShowRecommendations(true);
  };

  const handleStartRecommendations = () => {
    setShowRecommendations(true);
  };

  const handleRefreshRecommendations = () => {
    if (activeAiQuery) {
      refetchQueryRec();
    } else {
      refetchDefaultRec();
    }
  };

  return (
    <div className={aiRecommendationStyles.container}>
      <div className="flex flex-col gap-4 mb-6">
        <div className="flex items-center justify-between">
          <div className={aiRecommendationStyles.header}>
            <Sparkles className={aiRecommendationStyles.icon} />
            <h3 className="text-lg font-bold text-gray-900">
              {activeAiQuery ? "요청하신 분위기의 챌린지예요!" : "AI 추천 챌린지"}
            </h3>
          </div>
          <Button 
            variant="ghost" 
            size="sm" 
            onClick={handleRefreshRecommendations} 
            disabled={isRecLoading}
            className={aiStyles.refreshBtn}
          >
            <RefreshCw className={`w-4 h-4 mr-1 ${isRecLoading ? "animate-spin" : ""}`} />
            다른 추천 보기
          </Button>
        </div>

        {/* AI Query Input */}
        <div className="relative">
          <Input
            placeholder="예: 다이어트를 위한 식단 챌린지 추천해줘 (비워두면 내 이력 기반 추천)"
            value={aiPrompt}
            onChange={(e) => setAiPrompt(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                if (aiPrompt.trim()) handleAiSearch();
                else handleStartRecommendations();
              }
            }}
            className="pr-32 border-indigo-200 focus:border-indigo-500 focus:ring-indigo-200 h-12 text-base shadow-sm"
          />
          <Button 
            className={`absolute right-1 top-1 h-10 ${
              aiPrompt.trim() ? "bg-indigo-600 hover:bg-indigo-700" : "bg-purple-600 hover:bg-purple-700"
            } text-white transition-colors duration-200`}
            onClick={() => {
              if (aiPrompt.trim()) handleAiSearch();
              else handleStartRecommendations();
            }}
            disabled={isRecLoading}
          >
            <Sparkles className="w-4 h-4 mr-2" />
            {aiPrompt.trim() ? "AI 검색" : "내 취향 추천"}
          </Button>
        </div>
      </div>

      {!showRecommendations && !activeAiQuery ? (
         <div className="text-center py-8 bg-gradient-to-b from-indigo-50/30 to-transparent rounded-xl border-2 border-dashed border-indigo-100">
           <div className="bg-white p-3 rounded-full shadow-sm inline-block mb-3">
             <Sparkles className="w-6 h-6 text-indigo-500 animate-pulse" />
           </div>
           <h3 className="text-base font-bold text-gray-900 mb-1">어떤 챌린지를 만들지 고민되시나요?</h3>
           <p className="text-xs text-gray-500 max-w-md mx-auto leading-relaxed">
             위 입력창에 <span className="font-bold text-indigo-600">만들고 싶은 주제</span>를 적거나,<br/>
             그냥 <span className="font-bold text-purple-600">내 취향 추천</span> 버튼을 눌러보세요!
           </p>
         </div>
      ) : isRecLoading ? (
        <Card className="border-2 border-dashed bg-white/50 p-12 flex flex-col items-center justify-center">
          <Loader2 className="w-10 h-10 animate-spin text-blue-500 mb-4" />
          <p className="text-gray-600 font-semibold text-lg">AI가 챌린지 아이디어를 생각중입니다...</p>
          <p className="text-gray-500 text-sm mt-1">
            {activeAiQuery ? `"${activeAiQuery}"에 맞는 챌린지를 생성 중...` : "잠시만 기다려주세요..."}
          </p>
        </Card>
      ) : !recommendations || recommendations.length === 0 ? (
        <Card className={aiStyles.emptyCard}>
          <CardContent className={aiStyles.centerContent}>
            <Sparkles className="w-8 h-8 text-gray-300 mb-2" />
            <p className="text-sm text-gray-500 font-medium">추천 가능한 챌린지가 없습니다.</p>
            <Button variant="link" size="sm" onClick={handleRefreshRecommendations} className="text-blue-600 underline mt-1">
              다시 시도하기
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
