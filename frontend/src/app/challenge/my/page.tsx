"use client";

import { useRouter } from "next/navigation";
import { useMyChallenges } from "@/hooks/useChallenge";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Users, Eye, Calendar, Award, Search, ArrowLeft, Trophy } from "lucide-react";
import { ChallengeListResponse } from "@/types/challenge";
import { Button } from "@/components/ui/button";

export default function MyChallengesPage() {
  const router = useRouter();
  const { data: challenges, isLoading } = useMyChallenges();

  const getStatusBadge = (startDate: string, endDate: string) => {
    const now = new Date();
    const start = new Date(startDate);
    const end = new Date(endDate);

    if (now < start)
      return (
        <Badge
          variant="secondary"
          className="bg-blue-100 text-blue-800 border-blue-200 font-semibold"
        >
          예정
        </Badge>
      );
    if (now > end)
      return (
        <Badge variant="outline" className="border-gray-400 text-gray-700 font-semibold">
          종료
        </Badge>
      );
    return (
      <Badge className="bg-gradient-to-r from-green-500 to-emerald-500 border-0 text-white font-semibold">
        진행중
      </Badge>
    );
  };

  const getDifficultyBadge = (difficulty: string) => {
    const variants: Record<string, { label: string; className: string }> = {
      EASY: {
        label: "쉬움",
        className: "bg-green-100 text-green-800 border-green-200 font-semibold",
      },
      MEDIUM: {
        label: "보통",
        className: "bg-yellow-100 text-yellow-800 border-yellow-200 font-semibold",
      },
      HARD: { label: "어려움", className: "bg-red-100 text-red-800 border-red-200 font-semibold" },
    };

    const config = variants[difficulty] || variants.MEDIUM;
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
    <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-10 h-10 bg-gradient-to-r from-amber-500 to-orange-500 rounded-lg flex items-center justify-center text-white">
              <Trophy className="w-6 h-6" />
            </div>
            <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              참여 중인 챌린지
            </h1>
          </div>
          <p className="text-gray-600 font-medium ml-13">현재 진행 중인 나의 챌린지 목록입니다</p>
        </div>

        {/* Challenge List */}
        {isLoading ? (
          <ChallengeListSkeleton />
        ) : challenges && challenges.length > 0 ? (
          <>
            <div className="mb-4 text-sm text-gray-700 font-medium">
              총 <span className="font-bold text-blue-600">{challenges.length}</span>개의 참여 중인
              챌린지
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {challenges.map((challenge, index) => (
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
          <div className="text-center py-20 bg-white rounded-2xl shadow-sm border-2 border-dashed border-gray-200">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-50 rounded-full mb-4">
              <Search className="w-8 h-8 text-gray-300" />
            </div>
            <p className="text-gray-700 text-lg font-semibold">참여 중인 챌린지가 없습니다.</p>
            <p className="text-gray-500 text-sm mt-2 mb-6">새로운 챌린지에 도전해보세요!</p>
            <Button
              onClick={() => router.push("/challenge")}
              className="bg-blue-600 hover:bg-blue-700"
            >
              챌린지 둘러보기
            </Button>
          </div>
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
      className="cursor-pointer hover:shadow-2xl transition-all duration-300 border-2 hover:border-blue-300 group bg-gradient-to-br from-white to-gray-50"
      onClick={onClick}
    >
      <CardHeader className="pb-3">
        <div className="flex justify-between items-start gap-2 mb-2">
          <CardTitle className="text-lg font-bold text-gray-900 line-clamp-2 group-hover:text-blue-600 transition-colors">
            {challenge.title}
          </CardTitle>
          {statusBadge}
        </div>
        <CardDescription className="flex items-center gap-1 text-xs text-gray-600 font-medium">
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
          <div className="flex items-center gap-1 hover:text-blue-600 transition-colors">
            <Users className="w-4 h-4" />
            <span className="font-bold">{challenge.participantCnt}</span>
          </div>
          <div className="flex items-center gap-1 hover:text-purple-600 transition-colors">
            <Award className="w-4 h-4" />
            <span className="font-bold">{challenge.certificationCnt}</span>
          </div>
          <div className="flex items-center gap-1 hover:text-gray-900 transition-colors">
            <Eye className="w-4 h-4" />
            <span className="font-bold">{challenge.viewCnt}</span>
          </div>
        </div>
      </CardFooter>
    </Card>
  );
}

function ChallengeListSkeleton() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {[...Array(3)].map((_, i) => (
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
