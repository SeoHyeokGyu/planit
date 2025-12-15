"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useChallenges, useSearchChallenges } from "@/hooks/useChallenge";
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
import { Search, Plus, Users, Eye, Calendar, Award, Filter } from "lucide-react";
import { ChallengeListResponse } from "@/types/challenge";

export default function ChallengesPage() {
    const router = useRouter();
    const [searchKeyword, setSearchKeyword] = useState("");
    const [category, setCategory] = useState<string | undefined>();
    const [difficulty, setDifficulty] = useState<string | undefined>();

    const { data: challenges, isLoading } = useChallenges({
        category,
        difficulty
    });

    const shouldSearch = searchKeyword.trim().length >= 1;
    const { data: searchResults } = useSearchChallenges(searchKeyword);

    const displayChallenges = (shouldSearch && searchResults) ? searchResults : challenges;

    const getStatusBadge = (startDate: string, endDate: string) => {
        const now = new Date();
        const start = new Date(startDate);
        const end = new Date(endDate);

        if (now < start) return <Badge variant="secondary" className="bg-blue-100 text-blue-800 border-blue-200 font-semibold">예정</Badge>;
        if (now > end) return <Badge variant="outline" className="border-gray-400 text-gray-700 font-semibold">종료</Badge>;
        return <Badge className="bg-gradient-to-r from-green-500 to-emerald-500 border-0 text-white font-semibold">진행중</Badge>;
    };

    const getDifficultyBadge = (difficulty: string) => {
        const variants: Record<string, { label: string; className: string }> = {
            EASY: { label: "쉬움", className: "bg-green-100 text-green-800 border-green-200 font-semibold" },
            MEDIUM: { label: "보통", className: "bg-yellow-100 text-yellow-800 border-yellow-200 font-semibold" },
            HARD: { label: "어려움", className: "bg-red-100 text-red-800 border-red-200 font-semibold" },
        };

        const config = variants[difficulty] || variants.MEDIUM;
        return <Badge variant="outline" className={config.className}>{config.label}</Badge>;
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
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8">
                    <div>
                        <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-2">
                            챌린지
                        </h1>
                        <p className="text-gray-700 font-medium">
                            다양한 챌린지에 참여하고 함께 성장하세요
                        </p>
                    </div>
                    <Button
                        onClick={() => router.push("/challenge/create")}
                        className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 shadow-lg hover:shadow-xl transition-all group"
                    >
                        <Plus className="w-4 h-4 mr-2 group-hover:rotate-90 transition-transform" />
                        챌린지 만들기
                    </Button>
                </div>

                {/* Filters */}
                <Card className="mb-8 border-2 shadow-lg bg-white">
                    <CardContent className="pt-6 bg-white">
                        <div className="flex flex-col gap-4">
                            <div className="relative">
                                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                                <Input
                                    placeholder="챌린지 검색..."
                                    value={searchKeyword}
                                    onChange={(e) => setSearchKeyword(e.target.value)}
                                    className="pl-10 h-12 text-base border-2 border-gray-300 focus:border-blue-500 bg-white text-gray-900 placeholder:text-gray-400 font-medium"
                                />
                            </div>
                            <div className="flex flex-col sm:flex-row gap-3">
                                <div className="flex items-center gap-2 flex-1">
                                    <Filter className="w-4 h-4 text-gray-600" />
                                    <Select
                                        value={category}
                                        onValueChange={(value) => setCategory(value === "all" ? undefined : value)}
                                    >
                                        <SelectTrigger className="border-2 border-gray-300 bg-white text-gray-900 font-medium">
                                            <SelectValue placeholder="카테고리" className="text-gray-900" />
                                        </SelectTrigger>
                                        <SelectContent className="bg-white">
                                            <SelectItem value="all" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">전체</SelectItem>
                                            <SelectItem value="HEALTH" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">🏃 건강</SelectItem>
                                            <SelectItem value="STUDY" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">📚 학습</SelectItem>
                                            <SelectItem value="HOBBY" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">🎨 취미</SelectItem>
                                            <SelectItem value="LIFESTYLE" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">🌱 라이프스타일</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>
                                <Select
                                    value={difficulty}
                                    onValueChange={(value) => setDifficulty(value === "all" ? undefined : value)}
                                >
                                    <SelectTrigger className="flex-1 border-2 border-gray-300 bg-white text-gray-900 font-medium">
                                        <SelectValue placeholder="난이도" className="text-gray-900" />
                                    </SelectTrigger>
                                    <SelectContent className="bg-white">
                                        <SelectItem value="all" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">전체</SelectItem>
                                        <SelectItem value="EASY" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">⭐ 쉬움</SelectItem>
                                        <SelectItem value="MEDIUM" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">⭐⭐ 보통</SelectItem>
                                        <SelectItem value="HARD" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">⭐⭐⭐ 어려움</SelectItem>
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
                            총 <span className="font-bold text-blue-600">{displayChallenges.length}</span>개의 챌린지
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
                    <div className="text-center py-20">
                        <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-full mb-4">
                            <Search className="w-8 h-8 text-gray-400" />
                        </div>
                        <p className="text-gray-700 text-lg font-semibold">챌린지가 없습니다.</p>
                        <p className="text-gray-500 text-sm mt-2">다른 검색 조건을 시도해보세요.</p>
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
                        day: "numeric"
                    })} ~ {new Date(challenge.endDate).toLocaleDateString("ko-KR", {
                    month: "short",
                    day: "numeric"
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