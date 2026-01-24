"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useCreateChallenge } from "@/hooks/useChallenge";
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
import { ArrowLeft, Info, Sparkles, Calendar, Target } from "lucide-react";
import { ChallengeRequest, ChallengeRecommendationResponse } from "@/types/challenge";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { toast } from "sonner";
import { layoutStyles, headerStyles, cardStyles, buttonStyles, themeStyles } from "@/styles/common";
import ChallengeRecommendations from "@/components/challenge/ChallengeRecommendations";

export default function CreateChallengePage() {
  const router = useRouter();
  const createMutation = useCreateChallenge();

  // 오늘 날짜를 YYYY-MM-DD 형식으로
  const getTodayString = () => {
    const today = new Date();
    return today.toISOString().split("T")[0];
  };

  const [formData, setFormData] = useState<ChallengeRequest>({
    title: "",
    description: "",
    category: "",
    difficulty: "",
    startDate: "",
    endDate: "",
    loginId: "",
  });

  const handleSelectRecommendation = (challenge: ChallengeRecommendationResponse) => {
    setFormData((prev) => ({
      ...prev,
      title: challenge.title,
      description: challenge.description,
      category: challenge.category,
      difficulty: challenge.difficulty,
    }));
    toast.success("AI 추천 내용을 적용했습니다! 기간을 설정해주세요.");
    // 스크롤을 맨 위로 부드럽게 이동하여 폼 확인 유도
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // 유효성 검증
    if (!formData.category || !formData.difficulty) {
      toast.error("카테고리와 난이도를 선택해주세요.");
      return;
    }

    // 날짜 유효성 검증
    if (!formData.startDate || !formData.endDate) {
      toast.error("시작일과 종료일을 선택해주세요.");
      return;
    }

    const start = new Date(formData.startDate);
    const end = new Date(formData.endDate);

    if (end <= start) {
      toast.error("종료일은 시작일보다 늦어야 합니다.");
      return;
    }

    // LocalDateTime 형식으로 시작일은 00:00:00, 종료일은 23:59:59로 설정
    const startDateTime = `${formData.startDate}T00:00:00`;
    const endDateTime = `${formData.endDate}T23:59:59`;

    const requestData = {
      ...formData,
      startDate: startDateTime, // "2024-12-30T00:00:00"
      endDate: endDateTime, // "2024-12-31T23:59:59"
    };

    createMutation.mutate(requestData, {
      onSuccess: (response) => {
        router.push(`/challenge/${response.data.id}`);
      },
      onError: (error) => {
        console.error("챌린지 생성 실패:", error);
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

  return (
    <div className={layoutStyles.pageRoot}>
      <div className={layoutStyles.containerXl}>
        <Button variant="ghost" onClick={() => router.back()} className={buttonStyles.back}>
          <ArrowLeft className="w-4 h-4 mr-2" />
          뒤로가기
        </Button>

        <div className={headerStyles.wrapper}>
          <div className={headerStyles.content}>
            <div className={`${headerStyles.icon} ${themeStyles.warning.bg}`}>
              <Sparkles className="w-6 h-6" />
            </div>
            <h1 className={`${headerStyles.title} ${themeStyles.warning.text}`}>
              새 챌린지 만들기
            </h1>
          </div>
          <p className={headerStyles.description}>
            새로운 챌린지를 만들고 사람들과 함께 도전해보세요
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column: Form */}
          <div className="lg:col-span-2">
            <Card className={cardStyles.base}>
              <CardHeader className={`${cardStyles.headerGradient} ${themeStyles.warning.headerBg}`}>
                <div className="flex items-center gap-3">
                  <div
                    className={`w-10 h-10 ${themeStyles.primary.bg} rounded-lg flex items-center justify-center`}
                  >
                    <Sparkles className="w-5 h-5 text-white" />
                  </div>
                  <div>
                    <CardTitle className="text-xl font-bold text-gray-900">챌린지 정보</CardTitle>
                    <CardDescription className="text-gray-700 font-medium">
                      모든 필수 항목을 입력해주세요
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="pt-6 bg-white">
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
                      required
                    />
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
                        value={formData.category || undefined}
                        onValueChange={(value) => setFormData((prev) => ({ ...prev, category: value }))}
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
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="difficulty" className="text-base font-bold text-gray-900">
                        난이도 *
                      </Label>
                      <Select
                        value={formData.difficulty || undefined}
                        onValueChange={(value) =>
                          setFormData((prev) => ({ ...prev, difficulty: value }))
                        }
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
                    </div>
                  </div>

                  {/* Dates */}
                  <div className="space-y-2">
                    <Label className="text-base font-bold flex items-center gap-2 text-gray-900">
                      <Calendar className="w-4 h-4 text-blue-600" />
                      챌린지 기간 *
                    </Label>
                    <p className="text-xs text-gray-600 font-medium mb-2">
                      시작일은 00:00부터, 종료일은 23:59까지 자동 설정됩니다.
                    </p>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label htmlFor="startDate" className="text-sm text-gray-800 font-bold">
                          시작일
                        </Label>
                        <Input
                          id="startDate"
                          name="startDate"
                          type="date"
                          value={formData.startDate}
                          onChange={handleChange}
                          min={getTodayString()}
                          className="border-2 border-gray-300 focus:border-blue-500 bg-white text-gray-900 font-medium"
                          required
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="endDate" className="text-sm text-gray-800 font-bold">
                          종료일
                        </Label>
                        <Input
                          id="endDate"
                          name="endDate"
                          type="date"
                          value={formData.endDate}
                          onChange={handleChange}
                          min={formData.startDate || getTodayString()}
                          className="border-2 border-gray-300 focus:border-blue-500 bg-white text-gray-900 font-medium"
                          required
                        />
                      </div>
                    </div>
                  </div>

                  {/* Info Alert */}
                  <Alert className="border-blue-200 bg-blue-50">
                    <Info className="h-4 w-4 text-blue-600" />
                    <AlertDescription className="text-sm text-blue-900 font-semibold">
                      선택한 날짜의 시작일부터 종료일까지 챌린지가 진행됩니다.
                    </AlertDescription>
                  </Alert>

                  {/* Error Alert */}
                  {createMutation.isError && (
                    <Alert variant="destructive" className="border-red-200">
                      <AlertDescription className="font-semibold">
                        {createMutation.error.message ||
                          "챌린지 생성에 실패했습니다. 다시 시도해주세요."}
                      </AlertDescription>
                    </Alert>
                  )}

                  {/* Submit Button */}
                  <Button
                    type="submit"
                    className={`${buttonStyles.submit} ${themeStyles.primary.btn}`}
                    disabled={createMutation.isPending}
                  >
                    {createMutation.isPending ? (
                      <>
                        <span className="animate-spin mr-2">⏳</span>
                        생성 중...
                      </>
                    ) : (
                      <>
                        <Sparkles className="w-5 h-5 mr-2" />
                        챌린지 만들기
                      </>
                    )}
                  </Button>
                </form>
              </CardContent>
            </Card>
          </div>

          {/* Right Column: Recommendations & Tips */}
          <div className="space-y-6">
             <ChallengeRecommendations onSelect={handleSelectRecommendation} />

            {/* Additional Tips */}
            <div className="p-5 bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl border border-purple-200 shadow-sm">
              <h3 className="font-bold text-purple-900 mb-3 flex items-center gap-2 text-lg">
                <Sparkles className="w-5 h-5" />
                챌린지 생성 팁
              </h3>
              <ul className="text-sm text-purple-800 space-y-2 font-medium">
                <li className="flex items-start gap-2">
                  <span className="text-purple-600 mt-1">•</span>
                  명확하고 구체적인 목표를 설정하면 참여율이 높아집니다.
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-purple-600 mt-1">•</span>
                  처음 시작한다면 '보통' 난이도로 많은 사람을 모아보세요.
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-purple-600 mt-1">•</span>
                  설명에는 규칙과 인증 방법을 상세히 적어주세요.
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
