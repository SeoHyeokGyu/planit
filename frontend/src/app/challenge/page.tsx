"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useChallenges, useSearchChallenges, useRecommendedExistingChallenges, useRecommendedExistingChallengesWithQuery } from "@/hooks/useChallenge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Search, Plus, Users, Eye, Calendar, Award, Filter, Trophy, ArrowUpDown, Check, Sparkles, RefreshCw, Loader2 } from "lucide-react";
import { ChallengeListResponse, ChallengeSortType } from "@/types/challenge";
import {
  pageHeaderStyles,
  iconGradients,
  layoutStyles,
  aiRecommendationStyles,
  filterStyles,
  challengeStyles,
} from "@/styles/common";
import { EmptyState } from "@/components/ui/empty-state";

export default function ChallengesPage() {
  const router = useRouter();
  const [searchKeyword, setSearchKeyword] = useState("");
  const [category, setCategory] = useState<string | undefined>();
  const [difficulty, setDifficulty] = useState<string | undefined>();
  const [sortBy, setSortBy] = useState<ChallengeSortType>("LATEST");

  // AI Recommendation States
  const [aiPrompt, setAiPrompt] = useState("");
  const [activeAiQuery, setActiveAiQuery] = useState<string>("");
  const [showRecommendations, setShowRecommendations] = useState(false);

  const {
    data: defaultRecommendations,
    isLoading: isDefaultRecLoading,
    refetch: refetchDefaultRec,
    isRefetching: isDefaultRecRefetching
  } = useRecommendedExistingChallenges({ enabled: showRecommendations });

  const {
    data: queryRecommendations,
    isLoading: isQueryRecLoading,
    refetch: refetchQueryRec,
    isRefetching: isQueryRecRefetching
  } = useRecommendedExistingChallengesWithQuery(activeAiQuery);

  const isRecLoading = activeAiQuery
    ? isQueryRecLoading || isQueryRecRefetching
    : isDefaultRecLoading || isDefaultRecRefetching;

  const recommendations = activeAiQuery ? queryRecommendations : defaultRecommendations;

  const handleAiSearch = () => {
    if (!aiPrompt.trim()) return;
    setActiveAiQuery(aiPrompt);
    setShowRecommendations(true); // 검색 시 추천 섹션 활성화
  };

  const handleRefreshRecommendations = () => {
    if (activeAiQuery) {
      refetchQueryRec();
    } else {
      refetchDefaultRec();
    }
  };

  const handleStartRecommendations = () => {
    setShowRecommendations(true);
  };

  // 디버깅: sortBy 변경 시 확인
  useEffect(() => {
    console.log("정렬 조건 변경:", { category, difficulty, sortBy });
  }, [category, difficulty, sortBy]);

  // category, difficulty, sortBy가 변경되면 자동으로 재조회됨 (React Query의 queryKey 의존성)
  const { data: challenges, isLoading, isFetching } = useChallenges({
    category,
    difficulty,
    sortBy,
  });

  // 디버깅: 데이터 조회 상태 확인
  useEffect(() => {
    console.log("챌린지 데이터:", challenges?.slice(0, 3)?.map(c => ({ title: c.title, participants: c.participantCnt })));
    console.log("로딩 상태:", { isLoading, isFetching });
  }, [challenges, isLoading, isFetching]);

  const shouldSearch = searchKeyword.trim().length >= 1;
  const { data: searchResults } = useSearchChallenges(searchKeyword);

  // 검색 결과에도 정렬 적용
  const getSortedChallenges = (challenges: ChallengeListResponse[] | undefined) => {
    if (!challenges) return undefined;

    switch (sortBy) {
      case "NAME":
        return [...challenges].sort((a, b) => a.title.localeCompare(b.title));
      case "DIFFICULTY":
        const difficultyOrder = { EASY: 1, NORMAL: 2, MEDIUM: 2, HARD: 3 };
        return [...challenges].sort((a, b) => {
          const orderA = difficultyOrder[a.difficulty as keyof typeof difficultyOrder] || 99;
          const orderB = difficultyOrder[b.difficulty as keyof typeof difficultyOrder] || 99;
          return orderA === orderB ? a.title.localeCompare(b.title) : orderA - orderB;
        });
      case "POPULAR":
        return [...challenges].sort((a, b) => b.participantCnt - a.participantCnt);
      default: // LATEST
        return challenges; // 기본적으로 최신순으로 오므로 그대로 사용
    }
  };

  const displayChallenges = shouldSearch
      ? getSortedChallenges(searchResults)
      : challenges;

  const getStatusBadge = (startDate: string, endDate: string) => {
    const now = new Date();
    const start = new Date(startDate);
    const end = new Date(endDate);

    if (now < start)
      return (
          <Badge
              variant="secondary"
              className={challengeStyles.statusBadge.scheduled}
          >
            예정
          </Badge>
      );
    if (now > end)
      return (
          <Badge variant="outline" className={challengeStyles.statusBadge.ended}>
            종료
          </Badge>
      );
    return (
        <Badge className={challengeStyles.statusBadge.ongoing}>
          진행중
        </Badge>
    );
  };

  const getDifficultyBadge = (difficulty: string) => {
    const variants: Record<string, { label: string; className: string }> = {
      EASY: {
        label: "쉬움",
        className: challengeStyles.difficultyBadge.easy,
      },
      NORMAL: {
        label: "보통",
        className: challengeStyles.difficultyBadge.medium,
      },
      MEDIUM: {
        label: "보통",
        className: challengeStyles.difficultyBadge.medium,
      },
      HARD: {
        label: "어려움",
        className: challengeStyles.difficultyBadge.hard
      },
    };

    const config = variants[difficulty] || variants.NORMAL;
    return (
        <Badge variant="outline" className={config.className}>
          {config.label}
        </Badge>
    );
  };

  const getCategoryLabel = (category: string) => {
    const labels: Record<string, string> = {
      HEALTH: "건강",
      STUDY: "학습",
      HOBBY: "취미",
      LIFESTYLE: "라이프스타일",
    };
    return labels[category] || category;
  };

  return (
      <div className={layoutStyles.pageRoot}>
        <div className={layoutStyles.containerXl}>
          {/* Header */}
          <div className={pageHeaderStyles.container}>
            <div className={pageHeaderStyles.wrapper}>
              <div className={pageHeaderStyles.titleSection}>
                <div className={pageHeaderStyles.titleWrapper}>
                  <div className={`${pageHeaderStyles.iconBase} ${iconGradients.challenge}`}>
                    <Trophy className="w-6 h-6" />
                  </div>
                  <div>
                    <h1 className={pageHeaderStyles.title}>챌린지</h1>
                    <p className={pageHeaderStyles.description}>
                      다양한 챌린지에 참여하고 함께 성장하세요
                    </p>
                  </div>
                </div>
              </div>
              <Button
                  onClick={() => router.push("/challenge/create")}
                  className={pageHeaderStyles.actionButton}
              >
                <Plus className="w-4 h-4 mr-2 group-hover:rotate-90 transition-transform" />
                챌린지 만들기
              </Button>
            </div>
          </div>

          {/* AI Recommendations */}
          <div className={aiRecommendationStyles.container}>
            <div className="flex flex-col gap-4 mb-6">
              <div className="flex items-center justify-between">
                <div className={aiRecommendationStyles.header}>
                  <Sparkles className={aiRecommendationStyles.icon} />
                  <h2 className={aiRecommendationStyles.title}>
                    {activeAiQuery ? "요청하신 분위기의 챌린지예요!" : "AI가 회원님을 위해 골랐어요!"}
                  </h2>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleRefreshRecommendations}
                  disabled={isRecLoading}
                  className="text-gray-500 hover:text-blue-600"
                >
                  <RefreshCw className={`w-4 h-4 mr-1 ${isRecLoading ? "animate-spin" : ""}`} />
                  다른 추천 보기
                </Button>
              </div>
              
              {/* AI Query Input */}
              <div className="relative">
                <Input
                  placeholder="예: 요즘 무기력해서 활력이 필요해 (비워두면 내 이력 기반 추천)"
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
               <div className="text-center py-12 bg-gradient-to-b from-indigo-50/30 to-transparent rounded-xl border-2 border-dashed border-indigo-100">
                 <div className="bg-white p-4 rounded-full shadow-md inline-block mb-4">
                   <Sparkles className="w-8 h-8 text-indigo-500 animate-pulse" />
                 </div>
                 <h3 className="text-lg font-bold text-gray-900 mb-2">어떤 챌린지를 찾고 계신가요?</h3>
                 <p className="text-gray-500 max-w-md mx-auto leading-relaxed">
                   위 입력창에 <span className="font-bold text-indigo-600">현재 기분</span>을 적거나,<br/>
                   그냥 <span className="font-bold text-purple-600">내 취향 추천</span> 버튼을 눌러보세요!
                 </p>
               </div>
            ) : isRecLoading ? (
              <Card className="border-2 border-dashed bg-white/50 p-12 flex flex-col items-center justify-center">
                <Loader2 className="w-10 h-10 animate-spin text-blue-500 mb-4" />
                <p className="text-gray-600 font-semibold text-lg">AI가 맞춤형 챌린지를 분석하고 있습니다</p>
                <p className="text-gray-500 text-sm mt-1">
                  {activeAiQuery ? `"${activeAiQuery}"에 맞는 챌린지를 찾는 중...` : "잠시만 기다려주세요..."}
                </p>
              </Card>
            ) : recommendations && recommendations.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {recommendations.map((rec) => (
                  <div key={`${rec.challenge.id}-rec`} className={aiRecommendationStyles.cardWrapper}>
                    <div className={aiRecommendationStyles.glowEffect}></div>
                    <div className={aiRecommendationStyles.cardInner}>
                      <ChallengeCard
                        challenge={rec.challenge}
                        statusBadge={getStatusBadge(
                          rec.challenge.startDate,
                          rec.challenge.endDate
                        )}
                        difficultyBadge={getDifficultyBadge(rec.challenge.difficulty)}
                        categoryLabel={getCategoryLabel(rec.challenge.category)}
                        onClick={() => router.push(`/challenge/${rec.challenge.id}`)}
                      />
                      <div className={aiRecommendationStyles.reasonBox}>
                        <p className={aiRecommendationStyles.reasonText}>
                          <Sparkles className={aiRecommendationStyles.reasonIcon} />
                          {rec.reason}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <Card className="border-2 border-dashed bg-gray-50 p-8 flex flex-col items-center justify-center text-center">
                <p className="text-gray-500 font-medium">추천할 챌린지가 없습니다.</p>
                <Button variant="link" onClick={handleRefreshRecommendations} className="text-blue-600 mt-2">
                  다시 시도하기
                </Button>
              </Card>
            )}
          </div>

          {/* Filters */}
          <Card className="mb-8 border-2 shadow-lg bg-white">
            <CardContent className="pt-6 bg-white">
              <div className="flex flex-col gap-4">
                <div className={filterStyles.searchWrapper}>
                  <Search className={filterStyles.searchIcon} />
                  <Input
                      placeholder="챌린지 검색..."
                      value={searchKeyword}
                      onChange={(e) => setSearchKeyword(e.target.value)}
                      className={filterStyles.searchInput}
                  />
                </div>
                <div className={filterStyles.filterGroup}>
                  <div className="flex items-center gap-2 flex-1">
                    <Filter className="w-4 h-4 text-gray-600" />
                    <Select
                        value={category}
                        onValueChange={(value) => setCategory(value === "all" ? undefined : value)}
                    >
                      <SelectTrigger className={filterStyles.selectTrigger}>
                        <SelectValue placeholder="카테고리" className="text-gray-900" />
                      </SelectTrigger>
                      <SelectContent className="bg-white">
                        <SelectItem
                            value="all"
                            className={filterStyles.selectItem}
                        >
                          전체
                        </SelectItem>
                        <SelectItem
                            value="HEALTH"
                            className={filterStyles.selectItem}
                        >
                          🏃 건강
                        </SelectItem>
                        <SelectItem
                            value="STUDY"
                            className={filterStyles.selectItem}
                        >
                          📚 학습
                        </SelectItem>
                        <SelectItem
                            value="HOBBY"
                            className={filterStyles.selectItem}
                        >
                          🎨 취미
                        </SelectItem>
                        <SelectItem
                            value="LIFESTYLE"
                            className={filterStyles.selectItem}
                        >
                          🌱 라이프스타일
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <Select
                      value={difficulty}
                      onValueChange={(value) => setDifficulty(value === "all" ? undefined : value)}
                  >
                    <SelectTrigger className={filterStyles.selectTriggerFlex}>
                      <SelectValue placeholder="난이도" className="text-gray-900" />
                    </SelectTrigger>
                    <SelectContent className="bg-white">
                      <SelectItem
                          value="all"
                          className={filterStyles.selectItem}
                      >
                        전체
                      </SelectItem>
                      <SelectItem
                          value="EASY"
                          className={filterStyles.selectItem}
                      >
                        ⭐ 쉬움
                      </SelectItem>
                      <SelectItem
                          value="NORMAL"
                          className={filterStyles.selectItem}
                      >
                        ⭐⭐ 보통
                      </SelectItem>
                      <SelectItem
                          value="HARD"
                          className={filterStyles.selectItem}
                      >
                        ⭐⭐⭐ 어려움
                      </SelectItem>
                    </SelectContent>
                  </Select>
                  <Select value={sortBy} onValueChange={(value) => setSortBy(value as ChallengeSortType)}>
                    <SelectTrigger className={`flex-1 ${pageHeaderStyles.standardButton}`}>
                      <div className="flex items-center gap-2">
                        <ArrowUpDown className="w-4 h-4 text-blue-600" />
                        <SelectValue placeholder="정렬" />
                      </div>
                    </SelectTrigger>
                    <SelectContent className="bg-white border-2 border-gray-300 shadow-xl rounded-lg p-1 animate-in fade-in-0 zoom-in-95">
                      <SelectItem
                          value="LATEST"
                          className="cursor-pointer rounded-md text-gray-800 hover:bg-blue-500 hover:text-white focus:bg-blue-500 focus:text-white transition-colors duration-150 data-[state=checked]:bg-blue-600 data-[state=checked]:text-white font-semibold py-2.5"
                      >
                        <div className="flex items-center justify-between w-full">
                          <span>최신순</span>
                          {sortBy === "LATEST" && <Check className="w-4 h-4 ml-2" />}
                        </div>
                      </SelectItem>
                      <SelectItem
                          value="NAME"
                          className="cursor-pointer rounded-md text-gray-800 hover:bg-blue-500 hover:text-white focus:bg-blue-500 focus:text-white transition-colors duration-150 data-[state=checked]:bg-blue-600 data-[state=checked]:text-white font-semibold py-2.5"
                      >
                        <div className="flex items-center justify-between w-full">
                          <span>이름순</span>
                          {sortBy === "NAME" && <Check className="w-4 h-4 ml-2" />}
                        </div>
                      </SelectItem>
                      <SelectItem
                          value="DIFFICULTY"
                          className="cursor-pointer rounded-md text-gray-800 hover:bg-blue-500 hover:text-white focus:bg-blue-500 focus:text-white transition-colors duration-150 data-[state=checked]:bg-blue-600 data-[state=checked]:text-white font-semibold py-2.5"
                      >
                        <div className="flex items-center justify-between w-full">
                          <span>난이도순</span>
                          {sortBy === "DIFFICULTY" && <Check className="w-4 h-4 ml-2" />}
                        </div>
                      </SelectItem>
                      <SelectItem
                          value="POPULAR"
                          className="cursor-pointer rounded-md text-gray-800 hover:bg-blue-500 hover:text-white focus:bg-blue-500 focus:text-white transition-colors duration-150 data-[state=checked]:bg-blue-600 data-[state=checked]:text-white font-semibold py-2.5"
                      >
                        <div className="flex items-center justify-between w-full">
                          <span>인기순</span>
                          {sortBy === "POPULAR" && <Check className="w-4 h-4 ml-2" />}
                        </div>
                      </SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Challenge List */}
          {isLoading ? (
              <ChallengeListSkeleton />
          ) : displayChallenges && displayChallenges.length > 0 ? (
              <>
                <div className="mb-4 text-sm text-gray-700 font-medium">
                  이 <span className="font-bold text-blue-600">{displayChallenges.length}</span>개의
                  챌린지
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {displayChallenges.map((challenge, index) => (
                      <ChallengeCard
                          key={`${challenge.id}-${index}`}
                          challenge={challenge}
                          statusBadge={getStatusBadge(challenge.startDate, challenge.endDate)}
                          difficultyBadge={getDifficultyBadge(challenge.difficulty)}
                          categoryLabel={getCategoryLabel(challenge.category)}
                          onClick={() => router.push(`/challenge/${challenge.id}`)}
                      />
                  ))}
                </div>
              </>
          ) : (
              <EmptyState
                  icon={Search}
                  title="챌린지가 없습니다."
                  description="다른 검색 조건을 시도해보세요."
              />
          )}
        </div>
      </div>
  );
}

function ChallengeCard({
                         challenge,
                         statusBadge,
                         difficultyBadge,
                         categoryLabel,
                         onClick,
                       }: {
  challenge: ChallengeListResponse;
  statusBadge: React.ReactNode;
  difficultyBadge: React.ReactNode;
  categoryLabel: string;
  onClick: () => void;
}) {
  return (
      <Card
          className={challengeStyles.card}
          onClick={onClick}
      >
        <CardHeader className="pb-3">
          <div className="flex justify-between items-start gap-2 mb-2">
            <CardTitle className={challengeStyles.cardTitle}>
              {challenge.title}
            </CardTitle>
            {statusBadge}
          </div>
          <CardDescription className={challengeStyles.cardDate}>
            <Calendar className="w-3 h-3" />
            {new Date(challenge.startDate).toLocaleDateString("ko-KR", {
              month: "short",
              day: "numeric",
            })}{" "}
            ~{" "}
            {new Date(challenge.endDate).toLocaleDateString("ko-KR", {
              month: "short",
              day: "numeric",
            })}
          </CardDescription>
        </CardHeader>
        <CardContent className="pb-3">
          <div className="flex gap-2 mb-3">
            <Badge variant="outline" className="border-blue-200 text-blue-800 font-semibold">
              {categoryLabel}
            </Badge>
            {difficultyBadge}
          </div>
          <p className="text-sm text-gray-700 line-clamp-2 leading-relaxed font-medium">
            {challenge.description}
          </p>
        </CardContent>
        <CardFooter className="pt-3 border-t">
          <div className="flex justify-between w-full text-sm text-gray-700">
            <div className={`${challengeStyles.statItem} hover:text-blue-600`}>
              <Users className="w-4 h-4" />
              <span className={challengeStyles.statValue}>{challenge.participantCnt}</span>
            </div>
            <div className={`${challengeStyles.statItem} hover:text-purple-600`}>
              <Award className="w-4 h-4" />
              <span className={challengeStyles.statValue}>{challenge.certificationCnt}</span>
            </div>
            <div className={`${challengeStyles.statItem} hover:text-gray-900`}>
              <Eye className="w-4 h-4" />
              <span className={challengeStyles.statValue}>{challenge.viewCnt}</span>
            </div>
          </div>
        </CardFooter>
      </Card>
  );
}

function ChallengeListSkeleton() {
  return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {[...Array(6)].map((_, i) => (
            <Card key={i} className="border-2">
              <CardHeader>
                <Skeleton className="h-6 w-3/4 mb-2" />
                <Skeleton className="h-4 w-1/2" />
              </CardHeader>
              <CardContent>
                <div className="flex gap-2 mb-3">
                  <Skeleton className="h-6 w-20" />
                  <Skeleton className="h-6 w-16" />
                </div>
                <Skeleton className="h-4 w-full mb-2" />
                <Skeleton className="h-4 w-2/3" />
              </CardContent>
              <CardFooter className="border-t pt-3">
                <div className="flex justify-between w-full">
                  <Skeleton className="h-4 w-12" />
                  <Skeleton className="h-4 w-12" />
                  <Skeleton className="h-4 w-12" />
                </div>
              </CardFooter>
            </Card>
        ))}
      </div>
  );
}
