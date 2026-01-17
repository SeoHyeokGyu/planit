"use client";

import { useState, useEffect, useMemo } from "react";
import { useParams, useRouter } from "next/navigation";
import { useChallenge, useUpdateChallenge, useDeleteChallenge } from "@/hooks/useChallenge";
import { useUserProfile } from "@/hooks/useUser";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ArrowLeft, Info, Sparkles, Calendar, Target, Trash2 } from "lucide-react";
import { ChallengeRequest } from "@/types/challenge";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { toast } from "sonner";
import { Skeleton } from "@/components/ui/skeleton";

export default function EditChallengePage() {
  const params = useParams();
  const router = useRouter();
  const challengeId = params.id as string;

  // 기존 챌린지 데이터 조회
  const { data: challenge, isLoading } = useChallenge(challengeId);
  const updateMutation = useUpdateChallenge(challengeId);
  const deleteMutation = useDeleteChallenge();

  const [formData, setFormData] = useState<ChallengeRequest>({
    title: "",
    description: "",
    category: "",
    difficulty: "",
    startDate: "",
    endDate: "",
    loginId: "",
  });

  // 챌린지 데이터 로드되면 폼에 채우기
  useEffect(() => {
    if (challenge) {
      // LocalDateTime을 datetime-local 형식으로 변환
      const formatDateTimeLocal = (dateTime: string) => {
        // "2024-01-15T10:30:00" 형식을 그대로 사용
        return dateTime.slice(0, 16); // "2024-01-15T10:30"
      };

      setFormData({
        title: challenge.title,
        description: challenge.description,
        category: challenge.category,
        difficulty: challenge.difficulty,
        startDate: formatDateTimeLocal(challenge.startDate),
        endDate: formatDateTimeLocal(challenge.endDate),
        loginId: "",
      });
    }
  }, [challenge]);

  // 챌린지가 진행 중인지 확인 (백엔드 값 + 프론트엔드 검증)
  const { isActive, isUpcoming, isEnded } = useMemo(() => {
    const now = new Date();
    const startDate = challenge ? new Date(challenge.startDate) : null;
    const endDate = challenge ? new Date(challenge.endDate) : null;

    // 백엔드 값과 프론트엔드 계산을 모두 확인 (명시적으로 boolean 변환)
    const active = !!(
      challenge?.isActive ||
      (startDate && endDate && now >= startDate && now <= endDate)
    );
    const upcoming = !!(challenge?.isUpcoming || (startDate && now < startDate));
    const ended = !!(challenge?.isEnded || (endDate && now > endDate));

    // 디버깅 로그
    console.log("Challenge Edit - Status Check:", {
      challengeId,
      now: now.toISOString(),
      startDate: startDate?.toISOString(),
      endDate: endDate?.toISOString(),
      backendIsActive: challenge?.isActive,
      backendIsUpcoming: challenge?.isUpcoming,
      backendIsEnded: challenge?.isEnded,
      calculatedIsActive: startDate && endDate && now >= startDate && now <= endDate,
      calculatedIsUpcoming: startDate && now < startDate,
      calculatedIsEnded: endDate && now > endDate,
      finalIsActive: active,
      finalIsUpcoming: upcoming,
      finalIsEnded: ended,
    });

    return {
      isActive: active,
      isUpcoming: upcoming,
      isEnded: ended,
    };
  }, [challenge, challengeId]);

  // 현재 로그인한 사용자 정보 가져오기
  const { data: currentUser } = useUserProfile();
  const currentUserLoginId = currentUser?.loginId;

  // 생성자 확인 (createdId는 loginId)
  const isCreator = challenge && currentUserLoginId && challenge.createdId === currentUserLoginId;

  // 권한 체크: 생성자가 아니면 리다이렉트
  useEffect(() => {
    if (challenge && currentUserLoginId && !isCreator) {
      toast.error("챌린지를 수정할 권한이 없습니다.");
      router.push(`/challenge/${challengeId}`);
    }

    // 종료된 챌린지는 수정 불가
    if (challenge && isEnded) {
      toast.error("종료된 챌린지는 수정할 수 없습니다.");
      router.push(`/challenge/${challengeId}`);
    }
  }, [challenge, isCreator, currentUserLoginId, challengeId, router, isEnded]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // 유효성 검증
    if (!formData.category || !formData.difficulty) {
      toast.error("카테고리와 난이도를 선택해주세요.");
      return;
    }

    // 날짜 유효성 검증
    const start = new Date(formData.startDate);
    const end = new Date(formData.endDate);

    if (end <= start) {
      toast.error("종료일은 시작일보다 늦어야 합니다.");
      return;
    }

    // 날짜만 전송
    const requestData = {
      ...formData,
      startDate: formData.startDate,
      endDate: formData.endDate,
    };

    updateMutation.mutate(requestData, {
      onSuccess: (response) => {
        toast.success("챌린지가 수정되었습니다.");
        router.push(`/challenge/${challengeId}`);
      },
      onError: (error: any) => {
        console.error("챌린지 수정 실패:", error);
        toast.error(error?.response?.data?.message || "챌린지 수정에 실패했습니다.");
      },
    });
  };

  const handleDelete = () => {
    // 확인 다이얼로그
    if (!window.confirm("정말로 이 챌린지를 삭제하시겠습니까?\n참여자들에게 알림이 발송됩니다.")) {
      return;
    }

    deleteMutation.mutate(challengeId, {
      onSuccess: () => {
        toast.success("챌린지가 삭제되었습니다.");
        router.push("/dashboard");
      },
      onError: (error: any) => {
        console.error("챌린지 삭제 실패:", error);
        toast.error(error?.response?.data?.message || "챌린지 삭제에 실패했습니다.");
      },
    });
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // 로딩 중
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
        <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Skeleton className="h-10 w-32 mb-6" />
          <Skeleton className="h-40 w-full mb-4" />
          <Skeleton className="h-96 w-full" />
        </div>
      </div>
    );
  }

  // 챌린지를 찾을 수 없음
  if (!challenge) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
        <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Alert variant="destructive">
            <AlertDescription>챌린지를 찾을 수 없습니다.</AlertDescription>
          </Alert>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Button variant="ghost" onClick={() => router.back()} className="mb-6 hover:bg-blue-50">
          <ArrowLeft className="w-4 h-4 mr-2" />
          뒤로가기
        </Button>

        <div className="mb-8">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-10 h-10 bg-gradient-to-r from-amber-500 to-orange-500 rounded-lg flex items-center justify-center text-white">
              <Sparkles className="w-6 h-6" />
            </div>
            <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              챌린지 수정하기
            </h1>
          </div>
          <p className="text-gray-700 font-medium ml-13">챌린지 정보를 수정하세요</p>
        </div>

        <Card className="border-2 shadow-xl bg-white">
          <CardHeader className="border-b bg-gradient-to-r from-blue-50 to-purple-50">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg flex items-center justify-center">
                <Sparkles className="w-5 h-5 text-white" />
              </div>
              <div>
                <CardTitle className="text-xl font-bold text-gray-900">챌린지 정보</CardTitle>
                <CardDescription className="text-gray-700 font-medium">
                  수정할 내용을 입력해주세요
                </CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent className="pt-6 bg-white">
            {/* 진행 중 챌린지 안내 */}
            {isActive && (
              <Alert className="mb-6 border-amber-200 bg-amber-50">
                <Info className="h-4 w-4 text-amber-600" />
                <AlertDescription className="text-amber-800 font-medium">
                  진행 중인 챌린지는 <strong>설명만 수정</strong>할 수 있습니다. 제목, 카테고리,
                  난이도, 날짜는 변경할 수 없습니다.
                </AlertDescription>
              </Alert>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Title */}
              <div className="space-y-2">
                <Label
                  htmlFor="title"
                  className="text-base font-bold flex items-center gap-2 text-gray-900"
                >
                  <Target className="w-4 h-4 text-blue-600" />
                  챌린지 이름 *
                </Label>
                <Input
                  id="title"
                  name="title"
                  value={formData.title}
                  onChange={handleChange}
                  placeholder="예: 30일 운동 챌린지"
                  className="h-12 border-2 border-gray-300 focus:border-blue-500 bg-white text-gray-900 placeholder:text-gray-400"
                  disabled={isActive}
                  required
                />
                {isActive && (
                  <p className="text-xs text-amber-600 font-medium">
                    ⚠️ 진행 중인 챌린지는 제목을 수정할 수 없습니다.
                  </p>
                )}
              </div>

              {/* Description */}
              <div className="space-y-2">
                <Label htmlFor="description" className="text-base font-bold text-gray-900">
                  설명 *
                </Label>
                <Textarea
                  id="description"
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  placeholder="챌린지에 대한 자세한 설명을 입력하세요. 목표, 규칙, 기대 효과 등을 포함하면 좋아요!"
                  rows={5}
                  className="border-2 border-gray-300 focus:border-blue-500 resize-none bg-white text-gray-900 placeholder:text-gray-400"
                  required
                />
                <p className="text-xs text-gray-700 font-medium">
                  {formData.description.length} / 500자
                </p>
              </div>

              {/* Category & Difficulty */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="category" className="text-base font-bold text-gray-900">
                    카테고리 *
                  </Label>
                  <Select
                    key={`category-${formData.category}`}
                    value={formData.category}
                    onValueChange={(value) => setFormData((prev) => ({ ...prev, category: value }))}
                    disabled={isActive}
                  >
                    <SelectTrigger className="h-12 border-2 border-gray-300 bg-white text-gray-900 font-medium">
                      <SelectValue placeholder="카테고리 선택" />
                    </SelectTrigger>
                    <SelectContent className="bg-white">
                      <SelectItem
                        value="HEALTH"
                        className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100"
                      >
                        🏃 건강
                      </SelectItem>
                      <SelectItem
                        value="STUDY"
                        className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100"
                      >
                        📚 학습
                      </SelectItem>
                      <SelectItem
                        value="HOBBY"
                        className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100"
                      >
                        🎨 취미
                      </SelectItem>
                      <SelectItem
                        value="LIFESTYLE"
                        className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100"
                      >
                        🌱 라이프스타일
                      </SelectItem>
                    </SelectContent>
                  </Select>
                  {isActive && (
                    <p className="text-xs text-amber-600 font-medium">
                      ⚠️ 진행 중인 챌린지는 카테고리를 수정할 수 없습니다.
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="difficulty" className="text-base font-bold text-gray-900">
                    난이도 *
                  </Label>
                  <Select
                    key={`difficulty-${formData.difficulty}`}
                    value={formData.difficulty}
                    onValueChange={(value) =>
                      setFormData((prev) => ({ ...prev, difficulty: value }))
                    }
                    disabled={isActive}
                  >
                    <SelectTrigger className="h-12 border-2 border-gray-300 bg-white text-gray-900 font-medium">
                      <SelectValue placeholder="난이도 선택" />
                    </SelectTrigger>
                    <SelectContent className="bg-white">
                      <SelectItem
                        value="EASY"
                        className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100"
                      >
                        ⭐ 쉬움
                      </SelectItem>
                      <SelectItem
                        value="MEDIUM"
                        className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100"
                      >
                        ⭐⭐ 보통
                      </SelectItem>
                      <SelectItem
                        value="HARD"
                        className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100"
                      >
                        ⭐⭐⭐ 어려움
                      </SelectItem>
                    </SelectContent>
                  </Select>
                  {isActive && (
                    <p className="text-xs text-amber-600 font-medium">
                      ⚠️ 진행 중인 챌린지는 난이도를 수정할 수 없습니다.
                    </p>
                  )}
                </div>
              </div>

              {/* Dates */}
              <div className="space-y-2">
                <Label className="text-base font-bold flex items-center gap-2 text-gray-900">
                  <Calendar className="w-4 h-4 text-blue-600" />
                  챌린지 기간 *
                  {isActive && (
                    <span className="ml-2 px-2 py-1 text-xs font-semibold text-gray-600 bg-gray-100 rounded">
                      진행 중 - 날짜 수정 불가
                    </span>
                  )}
                </Label>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="startDate" className="text-sm text-gray-800 font-bold">
                      시작일시
                    </Label>
                    <Input
                      id="startDate"
                      name="startDate"
                      type="datetime-local"
                      value={formData.startDate}
                      onChange={handleChange}
                      className="border-2 border-gray-300 focus:border-blue-500 bg-white text-gray-900 font-medium disabled:bg-gray-100 disabled:cursor-not-allowed"
                      disabled={isActive}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="endDate" className="text-sm text-gray-800 font-bold">
                      종료일시
                    </Label>
                    <Input
                      id="endDate"
                      name="endDate"
                      type="datetime-local"
                      value={formData.endDate}
                      onChange={handleChange}
                      min={formData.startDate}
                      className="border-2 border-gray-300 focus:border-blue-500 bg-white text-gray-900 font-medium disabled:bg-gray-100 disabled:cursor-not-allowed"
                      disabled={isActive}
                      required
                    />
                  </div>
                </div>
              </div>

              {/* Info Alert */}
              {isActive ? (
                <Alert className="border-amber-200 bg-amber-50">
                  <Info className="h-4 w-4 text-amber-600" />
                  <AlertDescription className="text-sm text-amber-900 font-semibold">
                    ⚠️ 진행 중인 챌린지입니다. 제목과 설명만 수정할 수 있으며, 카테고리, 난이도,
                    날짜는 수정할 수 없습니다.
                  </AlertDescription>
                </Alert>
              ) : isUpcoming ? (
                <Alert className="border-green-200 bg-green-50">
                  <Info className="h-4 w-4 text-green-600" />
                  <AlertDescription className="text-sm text-green-900 font-semibold">
                    ✅ 시작 전 챌린지입니다. 모든 정보를 자유롭게 수정할 수 있습니다.
                  </AlertDescription>
                </Alert>
              ) : null}

              {/* Error Alert */}
              {updateMutation.isError && (
                <Alert variant="destructive" className="border-red-200">
                  <AlertDescription className="font-semibold">
                    {updateMutation.error?.message ||
                      "챌린지 수정에 실패했습니다. 다시 시도해주세요."}
                  </AlertDescription>
                </Alert>
              )}

              {/* Submit Button */}
              <div className="flex gap-3">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => router.back()}
                  className="flex-1 h-12 border-2"
                >
                  취소
                </Button>
                <Button
                  type="submit"
                  className="flex-1 h-12 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 shadow-lg hover:shadow-xl transition-all text-base font-bold"
                  disabled={updateMutation.isPending}
                >
                  {updateMutation.isPending ? (
                    <>
                      <span className="animate-spin mr-2">⏳</span>
                      수정 중...
                    </>
                  ) : (
                    <>
                      <Sparkles className="w-5 h-5 mr-2" />
                      수정 완료
                    </>
                  )}
                </Button>
              </div>

              {/* 삭제 버튼 - 시작 전 챌린지만 */}
              {isUpcoming && (
                <div className="mt-8 pt-6 border-t border-gray-200">
                  <div className="bg-red-50 border-2 border-red-200 rounded-lg p-4">
                    <h3 className="text-red-800 font-bold text-sm mb-2 flex items-center gap-2">
                      <Trash2 className="w-4 h-4" />
                      위험 구역
                    </h3>
                    <p className="text-red-700 text-xs mb-4">
                      챌린지를 삭제하면 되돌릴 수 없으며, 참여자들에게 알림이 발송됩니다.
                    </p>
                    <Button
                      type="button"
                      variant="destructive"
                      onClick={handleDelete}
                      disabled={deleteMutation.isPending}
                      className="w-full h-10 bg-red-600 hover:bg-red-700 text-white font-semibold"
                    >
                      {deleteMutation.isPending ? (
                        <>
                          <span className="animate-spin mr-2">⏳</span>
                          삭제 중...
                        </>
                      ) : (
                        <>
                          <Trash2 className="w-4 h-4 mr-2" />
                          챌린지 삭제
                        </>
                      )}
                    </Button>
                  </div>
                </div>
              )}
            </form>
          </CardContent>
        </Card>

        {/* Additional Tips */}
        <div className="mt-6 p-4 bg-gradient-to-r from-purple-50 to-blue-50 rounded-lg border border-purple-200">
          <h3 className="font-bold text-purple-900 mb-2 flex items-center gap-2">
            <Sparkles className="w-4 h-4" />
            수정 시 주의사항
          </h3>
          <ul className="text-sm text-purple-900 space-y-1 font-medium">
            <li>
              • <strong>시작 전</strong>: 모든 정보를 자유롭게 수정할 수 있습니다
            </li>
            <li>
              • <strong>진행 중</strong>: 제목과 설명만 수정 가능하며, 카테고리/난이도/날짜는 수정할
              수 없습니다
            </li>
            <li>
              • <strong>종료됨</strong>: 챌린지를 수정할 수 없습니다
            </li>
            <li>• 챌린지 삭제는 시작 전에만 가능합니다</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
