"use client";

import { useParams, useRouter } from "next/navigation";
import { useChallenge, useJoinChallenge, useWithdrawChallenge, useMyChallenges } from "@/hooks/useChallenge";
import { challengeService } from "@/services/challengeService";
import { Button } from "@/components/ui/button";
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
import {
    ArrowLeft,
    Calendar,
    Users,
    Eye,
    Award,
    Clock,
    User,
    TrendingUp,
    Trophy
} from "lucide-react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useEffect, useState } from "react";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { toast } from "sonner";

export default function ChallengeDetailPage() {
    const params = useParams();
    const router = useRouter();
    const challengeId = params.id as string;
    const [isWithdrawDialogOpen, setIsWithdrawDialogOpen] = useState(false);

    useEffect(() => {
        const incrementViewCount = async () => {
            if (!challengeId) return;

            const sessionKey = `viewed_challenge_${challengeId}`;
            const lockKey = `lock_${sessionKey}`;

            // 이미 처리 중이면 스킵 (Strict Mode 중복 방지)
            if (sessionStorage.getItem(lockKey)) {
                console.log('Already processing, skipping:', challengeId);
                return;
            }

            const lastViewTime = sessionStorage.getItem(sessionKey);

            // 마지막 방문 시간 확인
            if (lastViewTime) {
                const timeDiff = Date.now() - parseInt(lastViewTime);
                const fiveMinutes = 1 * 60 * 1000; // 5분을 밀리초로

                // 5분 이내 재방문은 카운트 안 함
                if (timeDiff < fiveMinutes) {
                    console.log('Viewed within 5 minutes, skipping:', challengeId);
                    return;
                }

                // 5분 경과 → 조회수 증가
                console.log('5 minutes passed, incrementing:', challengeId);
            }

            try {
                // 처리 중 플래그 설정
                sessionStorage.setItem(lockKey, 'true');

                await challengeService.incrementViewCount(challengeId);

                // 현재 시간 저장
                sessionStorage.setItem(sessionKey, Date.now().toString());
                console.log('View count incremented:', challengeId);

                // 100ms 후 lock 해제 (Strict Mode 중복 방지)
                setTimeout(() => {
                    sessionStorage.removeItem(lockKey);
                }, 100);
            } catch (error) {
                sessionStorage.removeItem(lockKey);
                console.error('Failed to increment view count:', error);
            }
        };

        incrementViewCount();
    }, [challengeId]);

    const { data: challenge, isLoading, error } = useChallenge(challengeId);
    const { data: myChallenges } = useMyChallenges();
    const joinMutation = useJoinChallenge();
    const withdrawMutation = useWithdrawChallenge();

    const isParticipating = myChallenges?.some((c) => c.id === challengeId);

    const handleJoin = () => {
        joinMutation.mutate(challengeId, {
            onSuccess: () => {
                toast.success("챌린지에 참여했습니다!");
                window.location.reload();
            },
            onError: (error: any) => {
                toast.error(error.message || "참여에 실패했습니다.");
            }
        });
    };

    const handleWithdraw = () => {
        withdrawMutation.mutate(challengeId, {
            onSuccess: () => {
                toast.success("챌린지를 포기했습니다.");
                window.location.reload();
            },
            onError: (error: any) => {
                toast.error(error.message || "포기에 실패했습니다.");
            }
        });
    };

    const getStatusInfo = (startDate: string, endDate: string) => {
        const now = new Date();
        const start = new Date(startDate);
        const end = new Date(endDate);

        if (now < start) {
            return {
                badge: <Badge variant="secondary" className="bg-blue-100 text-blue-800 border-blue-200 font-semibold">예정</Badge>,
                text: "시작 예정"
            };
        }
        if (now > end) {
            return {
                badge: <Badge variant="outline" className="border-gray-400 text-gray-700 font-semibold">종료</Badge>,
                text: "종료됨"
            };
        }
        return {
            badge: <Badge className="bg-gradient-to-r from-green-500 to-emerald-500 border-0 text-white font-semibold">진행중</Badge>,
            text: "진행중"
        };
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
            HEALTH: "🏃 건강",
            STUDY: "📚 학습",
            HOBBY: "🎨 취미",
            LIFESTYLE: "🌱 라이프스타일",
        };
        return labels[category] || category;
    };

    if (error) {
        return (
            <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50 p-6">
                <div className="max-w-4xl mx-auto">
                    <Alert variant="destructive">
                        <AlertDescription className="text-gray-900 font-semibold">
                            챌린지를 불러오는데 실패했습니다.
                        </AlertDescription>
                    </Alert>
                    <Button onClick={() => router.back()} className="mt-4">
                        <ArrowLeft className="w-4 h-4 mr-2" />
                        뒤로가기
                    </Button>
                </div>
            </div>
        );
    }

    if (isLoading) {
        return <ChallengeDetailSkeleton />;
    }

    if (!challenge) {
        return (
            <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50 p-6">
                <div className="max-w-4xl mx-auto text-center py-20">
                    <p className="text-gray-700 font-semibold text-lg">챌린지를 찾을 수 없습니다.</p>
                    <Button onClick={() => router.back()} className="mt-4">
                        <ArrowLeft className="w-4 h-4 mr-2" />
                        뒤로가기
                    </Button>
                </div>
            </div>
        );
    }

    const statusInfo = getStatusInfo(challenge.startDate, challenge.endDate);

    return (
        <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
            <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Back Button */}
                <Button
                    variant="ghost"
                    onClick={() => router.push("/challenge")}
                    className="mb-6 hover:bg-blue-50 text-gray-900 font-semibold"
                >
                    <ArrowLeft className="w-4 h-4 mr-2" />
                    목록으로
                </Button>

                {/* Header */}
                <div className="mb-8">
                    <div className="flex items-center gap-3 mb-3">
                        <div className="w-10 h-10 bg-gradient-to-r from-amber-500 to-orange-500 rounded-lg flex items-center justify-center text-white">
                            <Trophy className="w-6 h-6" />
                        </div>
                        <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                            챌린지 상세
                        </h1>
                    </div>
                    <p className="text-gray-600 font-medium ml-13">
                        챌린지 정보를 확인하고 참여하세요
                    </p>
                </div>

                {/* Header Card */}
                <Card className="mb-6 border-2 shadow-xl bg-white">
                    <CardHeader className="border-b bg-gradient-to-r from-blue-50 to-purple-50">
                        <div className="flex justify-between items-start gap-4">
                            <div className="flex-1">
                                <div className="flex items-center gap-2 mb-3">
                                    {statusInfo.badge}
                                    {getDifficultyBadge(challenge.difficulty)}
                                    <Badge variant="outline" className="border-blue-200 text-blue-800 font-semibold">
                                        {getCategoryLabel(challenge.category)}
                                    </Badge>
                                </div>
                                <CardTitle className="text-3xl mb-2 text-gray-900 font-bold">{challenge.title}</CardTitle>
                                <CardDescription className="flex items-center gap-2 text-base text-gray-700 font-medium">
                                    <User className="w-4 h-4" />
                                    생성자: {challenge.createdId}
                                </CardDescription>
                            </div>
                        </div>
                    </CardHeader>

                    <CardContent className="pt-6 bg-white">
                        {/* Description */}
                        <div className="mb-6">
                            <h3 className="text-lg font-bold mb-3 text-gray-900">챌린지 설명</h3>
                            <p className="text-gray-800 leading-relaxed whitespace-pre-wrap font-medium">
                                {challenge.description}
                            </p>
                        </div>

                        {/* Period */}
                        <div className="mb-6">
                            <h3 className="text-lg font-bold mb-3 text-gray-900 flex items-center gap-2">
                                <Calendar className="w-5 h-5 text-blue-600" />
                                챌린지 기간
                            </h3>
                            <div className="bg-gray-50 rounded-lg p-4 border-2 border-gray-300">
                                <div className="flex items-center justify-between">
                                    <div className="flex items-center gap-2">
                                        <Clock className="w-4 h-4 text-gray-600" />
                                        <span className="text-gray-800 font-bold">시작:</span>
                                        <span className="text-gray-900 font-medium">
                                            {new Date(challenge.startDate).toLocaleString("ko-KR", {
                                                year: "numeric",
                                                month: "long",
                                                day: "numeric",
                                                hour: "2-digit",
                                                minute: "2-digit",
                                            })}
                                        </span>
                                    </div>
                                </div>
                                <div className="flex items-center justify-between mt-2">
                                    <div className="flex items-center gap-2">
                                        <Clock className="w-4 h-4 text-gray-600" />
                                        <span className="text-gray-800 font-bold">종료:</span>
                                        <span className="text-gray-900 font-medium">
                                            {new Date(challenge.endDate).toLocaleString("ko-KR", {
                                                year: "numeric",
                                                month: "long",
                                                day: "numeric",
                                                hour: "2-digit",
                                                minute: "2-digit",
                                            })}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Statistics */}
                        <div>
                            <h3 className="text-lg font-bold mb-3 text-gray-900 flex items-center gap-2">
                                <TrendingUp className="w-5 h-5 text-blue-600" />
                                통계
                            </h3>
                            <div className="grid grid-cols-3 gap-4">
                                <div className="bg-blue-50 rounded-lg p-4 border-2 border-blue-300 text-center">
                                    <Users className="w-6 h-6 text-blue-600 mx-auto mb-2" />
                                    <p className="text-2xl font-bold text-gray-900">{challenge.participantCnt}</p>
                                    <p className="text-sm text-gray-700 font-semibold">참여자</p>
                                </div>
                                <div className="bg-purple-50 rounded-lg p-4 border-2 border-purple-300 text-center">
                                    <Award className="w-6 h-6 text-purple-600 mx-auto mb-2" />
                                    <p className="text-2xl font-bold text-gray-900">{challenge.certificationCnt}</p>
                                    <p className="text-sm text-gray-700 font-semibold">인증</p>
                                </div>
                                <div className="bg-gray-50 rounded-lg p-4 border-2 border-gray-300 text-center">
                                    <Eye className="w-6 h-6 text-gray-600 mx-auto mb-2" />
                                    <p className="text-2xl font-bold text-gray-900">{challenge.viewCnt}</p>
                                    <p className="text-sm text-gray-700 font-semibold">조회수</p>
                                </div>
                            </div>
                        </div>
                    </CardContent>

                    <CardFooter className="border-t bg-gray-50 flex gap-3">
                        {!isParticipating ? (
                            <Button
                                onClick={handleJoin}
                                disabled={joinMutation.isPending}
                                className="flex-1 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white h-12 font-bold"
                            >
                                {joinMutation.isPending ? "참여 중..." : "챌린지 참여하기"}
                            </Button>
                        ) : (
                            <>
                                <Button
                                    onClick={() => router.push(`/certification/create?challengeId=${challengeId}`)}
                                    className="flex-1 bg-green-600 hover:bg-green-700 text-white h-12 font-bold"
                                >
                                    인증하기
                                </Button>
                                <Button
                                    onClick={() => setIsWithdrawDialogOpen(true)}
                                    disabled={withdrawMutation.isPending}
                                    variant="outline"
                                    className="flex-1 border-2 border-red-400 text-red-700 hover:bg-red-50 h-12 font-bold"
                                >
                                    {withdrawMutation.isPending ? "포기 중..." : "챌린지 포기"}
                                </Button>
                            </>
                        )}
                    </CardFooter>
                </Card>

                <AlertDialog open={isWithdrawDialogOpen} onOpenChange={setIsWithdrawDialogOpen}>
                    <AlertDialogContent>
                        <AlertDialogHeader>
                            <AlertDialogTitle>챌린지 포기</AlertDialogTitle>
                            <AlertDialogDescription>
                                정말로 이 챌린지를 포기하시겠습니까? 포기 후에도 다시 참여할 수 있습니다.
                            </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                            <AlertDialogCancel>취소</AlertDialogCancel>
                            <AlertDialogAction
                                onClick={handleWithdraw}
                                className="bg-red-600 hover:bg-red-700 text-white"
                            >
                                포기하기
                            </AlertDialogAction>
                        </AlertDialogFooter>
                    </AlertDialogContent>
                </AlertDialog>
            </div>
        </div>
    );
}

function ChallengeDetailSkeleton() {
    return (
        <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
            <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <Skeleton className="h-10 w-32 mb-6" />
                <Card className="mb-6 border-2 bg-white">
                    <CardHeader className="border-b">
                        <div className="flex gap-2 mb-3">
                            <Skeleton className="h-6 w-16" />
                            <Skeleton className="h-6 w-16" />
                            <Skeleton className="h-6 w-20" />
                        </div>
                        <Skeleton className="h-8 w-3/4 mb-2" />
                        <Skeleton className="h-4 w-1/3" />
                    </CardHeader>
                    <CardContent className="pt-6">
                        <Skeleton className="h-6 w-32 mb-3" />
                        <Skeleton className="h-4 w-full mb-2" />
                        <Skeleton className="h-4 w-full mb-2" />
                        <Skeleton className="h-4 w-2/3 mb-6" />

                        <Skeleton className="h-6 w-32 mb-3" />
                        <Skeleton className="h-24 w-full mb-6" />

                        <Skeleton className="h-6 w-32 mb-3" />
                        <div className="grid grid-cols-3 gap-4">
                            <Skeleton className="h-24" />
                            <Skeleton className="h-24" />
                            <Skeleton className="h-24" />
                        </div>
                    </CardContent>
                    <CardFooter className="border-t">
                        <Skeleton className="h-12 w-full" />
                    </CardFooter>
                </Card>
            </div>
        </div>
    );
}