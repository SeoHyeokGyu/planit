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
// import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Search, Plus, Users, Eye, Calendar } from "lucide-react";
import { ChallengeListResponse } from "@/types/challenge";

export default function ChallengesPage() {
    const router = useRouter();
    const [searchKeyword, setSearchKeyword] = useState("");
    const [category, setCategory] = useState<string | undefined>();
    const [status, setStatus] = useState<string | undefined>();

    const { data: challenges, isLoading } = useChallenges({ category, status } as any);
    const { data: searchResults } = useSearchChallenges(searchKeyword);

    const displayChallenges = searchKeyword ? searchResults : challenges;

    const getStatusBadge = (startDate: string, endDate: string) => {
        const now = new Date();
        const start = new Date(startDate);
        const end = new Date(endDate);

        // if (now < start) return <Badge variant="secondary">예정</Badge>;
        // if (now > end) return <Badge variant="outline">종료</Badge>;
        // return <Badge className="bg-green-500">진행중</Badge>;
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-indigo-950 p-6">
            <div className="max-w-6xl mx-auto space-y-6">
                {/* Header */}
                <div className="flex justify-between items-center">
                    <h1 className="text-3xl font-bold">챌린지</h1>
                    <Button
                        onClick={() => router.push("/challenges/create")}
                        className="bg-blue-600 hover:bg-blue-700"
                    >
                        <Plus className="w-4 h-4 mr-2" />
                        챌린지 만들기
                    </Button>
                </div>

                {/* Filters */}
                <Card>
                    <CardContent className="pt-6">
                        <div className="flex flex-col md:flex-row gap-4">
                            <div className="relative flex-1">
                                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                                <Input
                                    placeholder="챌린지 검색..."
                                    value={searchKeyword}
                                    onChange={(e) => setSearchKeyword(e.target.value)}
                                    className="pl-10"
                                />
                            </div>
                            <Select value={category} onValueChange={setCategory}>
                                <SelectTrigger className="w-full md:w-[180px]">
                                    <SelectValue placeholder="카테고리" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="all">전체</SelectItem>
                                    <SelectItem value="health">건강</SelectItem>
                                    <SelectItem value="study">학습</SelectItem>
                                    <SelectItem value="hobby">취미</SelectItem>
                                    <SelectItem value="lifestyle">라이프스타일</SelectItem>
                                </SelectContent>
                            </Select>
                            <Select value={status} onValueChange={setStatus}>
                                <SelectTrigger className="w-full md:w-[180px]">
                                    <SelectValue placeholder="상태" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="all">전체</SelectItem>
                                    <SelectItem value="UPCOMING">예정</SelectItem>
                                    <SelectItem value="ONGOING">진행중</SelectItem>
                                    <SelectItem value="ENDED">종료</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                    </CardContent>
                </Card>

                {/* Challenge List */}
                {isLoading ? (
                    <ChallengeListSkeleton />
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {/*{displayChallenges?.map((challenge) => (*/}
                        {/*    <ChallengeCard*/}
                        {/*        key={challenge.id}*/}
                        {/*        challenge={challenge}*/}
                        {/*        // statusBadge={getStatusBadge(challenge.startDate, challenge.endDate)}*/}
                        {/*        onClick={() => router.push(`/challenges/${challenge.id}`)}*/}
                        {/*    />*/}
                        {/*))}*/}
                        {displayChallenges?.length === 0 && (
                            <div className="col-span-full text-center py-12 text-gray-500">
                                챌린지가 없습니다.
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

function ChallengeCard({
                           challenge,
                           statusBadge,
                           onClick,
                       }: {
    challenge: ChallengeListResponse;
    statusBadge: React.ReactNode;
    onClick: () => void;
}) {
    return (
        <Card
            className="cursor-pointer hover:shadow-lg transition-shadow"
            onClick={onClick}
        >
            <CardHeader>
                <div className="flex justify-between items-start">
                    <CardTitle className="text-lg line-clamp-1">{challenge.title}</CardTitle>
                    {statusBadge}
                </div>
                <CardDescription className="flex items-center gap-1">
                    <Calendar className="w-3 h-3" />
                    {new Date(challenge.startDate).toLocaleDateString()} ~{" "}
                    {new Date(challenge.endDate).toLocaleDateString()}
                </CardDescription>
            </CardHeader>
            <CardContent>
                {/*{challenge.category && (*/}
                {/*    <Badge variant="outline" className="mb-2">*/}
                {/*        {challenge.category}*/}
                {/*    </Badge>*/}
                {/*)}*/}
            </CardContent>
            <CardFooter className="flex justify-between text-sm text-gray-500">
                <div className="flex items-center gap-1">
                    <Users className="w-4 h-4" />
                    {challenge.currentParticipants}
                    {challenge.maxParticipants && `/${challenge.maxParticipants}`}
                </div>
                <div className="flex items-center gap-1">
                    <Eye className="w-4 h-4" />
                    {challenge.viewCount}
                </div>
            </CardFooter>
        </Card>
    );
}

function ChallengeListSkeleton() {
    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[...Array(6)].map((_, i) => (
                <Card key={i}>
                    <CardHeader>
                        <Skeleton className="h-6 w-3/4" />
                        <Skeleton className="h-4 w-1/2 mt-2" />
                    </CardHeader>
                    <CardContent>
                        <Skeleton className="h-6 w-20" />
                    </CardContent>
                    <CardFooter>
                        <Skeleton className="h-4 w-full" />
                    </CardFooter>
                </Card>
            ))}
        </div>
    );
}