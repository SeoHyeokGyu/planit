"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useCreateChallenge } from "@/hooks/useChallenge";
import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
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
import { ChallengeRequest } from "@/types/challenge";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { toast } from "sonner";

export default function CreateChallengePage() {
    const router = useRouter();
    const createMutation = useCreateChallenge();

    // 오늘 날짜를 YYYY-MM-DD 형식으로
    const getTodayString = () => {
        const today = new Date();
        return today.toISOString().split('T')[0];
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
            startDate: startDateTime,  // "2024-12-30T00:00:00"
            endDate: endDateTime,      // "2024-12-31T23:59:59"
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

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    ) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    return (
        <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
            <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <Button
                    variant="ghost"
                    onClick={() => router.back()}
                    className="mb-6 hover:bg-blue-50"
                >
                    <ArrowLeft className="w-4 h-4 mr-2" />
                    뒤로가기
                </Button>

                <div className="mb-8">
                    <div className="flex items-center gap-3 mb-3">
                        <div className="w-10 h-10 bg-gradient-to-r from-amber-500 to-orange-500 rounded-lg flex items-center justify-center text-white">
                            <Sparkles className="w-6 h-6" />
                        </div>
                        <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                            새 챌린지 만들기
                        </h1>
                    </div>
                    <p className="text-gray-700 font-medium ml-13">
                        새로운 챌린지를 만들고 사람들과 함께 도전해보세요
                    </p>
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
                                    모든 필수 항목을 입력해주세요
                                </CardDescription>
                            </div>
                        </div>
                    </CardHeader>
                    <CardContent className="pt-6 bg-white">
                        <form onSubmit={handleSubmit} className="space-y-6">
                            {/* Title */}
                            <div className="space-y-2">
                                <Label htmlFor="title" className="text-base font-bold flex items-center gap-2 text-gray-900">
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
                                        onValueChange={(value) =>
                                            setFormData((prev) => ({ ...prev, category: value }))
                                        }
                                    >
                                        <SelectTrigger className="h-12 border-2 border-gray-300 bg-white text-gray-900 font-medium">
                                            <SelectValue placeholder="카테고리 선택" />
                                        </SelectTrigger>
                                        <SelectContent className="bg-white">
                                            <SelectItem value="HEALTH" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">🏃 건강</SelectItem>
                                            <SelectItem value="STUDY" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">📚 학습</SelectItem>
                                            <SelectItem value="HOBBY" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">🎨 취미</SelectItem>
                                            <SelectItem value="LIFESTYLE" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">🌱 라이프스타일</SelectItem>
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
                                            <SelectItem value="EASY" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">⭐ 쉬움</SelectItem>
                                            <SelectItem value="MEDIUM" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">⭐⭐ 보통</SelectItem>
                                            <SelectItem value="HARD" className="text-gray-900 font-medium cursor-pointer hover:bg-gray-100">⭐⭐⭐ 어려움</SelectItem>
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
                                        {createMutation.error.message || "챌린지 생성에 실패했습니다. 다시 시도해주세요."}
                                    </AlertDescription>
                                </Alert>
                            )}

                            {/* Submit Button */}
                            <Button
                                type="submit"
                                className="w-full h-12 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 shadow-lg hover:shadow-xl transition-all text-base font-bold"
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

                {/* Additional Tips */}
                <div className="mt-6 p-4 bg-gradient-to-r from-purple-50 to-blue-50 rounded-lg border border-purple-200">
                    <h3 className="font-bold text-purple-900 mb-2 flex items-center gap-2">
                        <Sparkles className="w-4 h-4" />
                        챌린지 생성 팁
                    </h3>
                    <ul className="text-sm text-purple-900 space-y-1 font-medium">
                        <li>• 명확하고 구체적인 목표를 설정하세요</li>
                        <li>• 달성 가능한 난이도를 선택하세요</li>
                        <li>• 충분한 설명으로 참여자들의 이해를 도와주세요</li>
                    </ul>
                </div>
            </div>
        </div>
    );
}