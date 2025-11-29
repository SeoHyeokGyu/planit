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
import { ArrowLeft } from "lucide-react";
import { ChallengeRequest } from "@/types/challenge";

export default function CreateChallengePage() {
    const router = useRouter();
    const createMutation = useCreateChallenge();

    const [formData, setFormData] = useState<ChallengeRequest>({
        title: "",
        description: "",
        category: "",
        startDate: "",
        endDate: "",
        maxParticipants: undefined,
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        createMutation.mutate(formData, {
            onSuccess: (response) => {
                router.push(`/challenges/${response.data.id}`); // response.data.data → response.data
            },
        });
    };

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    ) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: name === "maxParticipants" ? (value ? Number(value) : undefined) : value,
        }));
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-indigo-950 p-6">
            <div className="max-w-2xl mx-auto space-y-6">
                <Button variant="ghost" onClick={() => router.back()}>
                    <ArrowLeft className="w-4 h-4 mr-2" />
                    뒤로가기
                </Button>

                <Card>
                    <CardHeader>
                        <CardTitle>새 챌린지 만들기</CardTitle>
                        <CardDescription>
                            새로운 챌린지를 만들고 사람들과 함께 도전해보세요.
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <form onSubmit={handleSubmit} className="space-y-6">
                            <div className="space-y-2">
                                <Label htmlFor="title">챌린지 이름 *</Label>
                                <Input
                                    id="title"
                                    name="title"
                                    value={formData.title}
                                    onChange={handleChange}
                                    placeholder="예: 30일 운동 챌린지"
                                    required
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="description">설명 *</Label>
                                <Textarea
                                    id="description"
                                    name="description"
                                    value={formData.description}
                                    onChange={handleChange}
                                    placeholder="챌린지에 대한 설명을 입력하세요"
                                    rows={4}
                                    required
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="category">카테고리</Label>
                                {/*<Select*/}
                                {/*    value={formData.category}*/}
                                {/*    onValueChange={(value) =>*/}
                                {/*        setFormData((prev) => ({ ...prev, category: value }))*/}
                                {/*    }*/}
                                {/*>*/}
                                {/*    <SelectTrigger>*/}
                                {/*        <SelectValue placeholder="카테고리 선택" />*/}
                                {/*    </SelectTrigger>*/}
                                {/*    <SelectContent>*/}
                                {/*        <SelectItem value="health">건강</SelectItem>*/}
                                {/*        <SelectItem value="study">학습</SelectItem>*/}
                                {/*        <SelectItem value="hobby">취미</SelectItem>*/}
                                {/*        <SelectItem value="lifestyle">라이프스타일</SelectItem>*/}
                                {/*    </SelectContent>*/}
                                {/*</Select>*/}
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="startDate">시작일 *</Label>
                                    <Input
                                        id="startDate"
                                        name="startDate"
                                        type="date"
                                        value={formData.startDate}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="endDate">종료일 *</Label>
                                    <Input
                                        id="endDate"
                                        name="endDate"
                                        type="date"
                                        value={formData.endDate}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="maxParticipants">최대 참여 인원 (선택)</Label>
                                <Input
                                    id="maxParticipants"
                                    name="maxParticipants"
                                    type="number"
                                    min="1"
                                    value={formData.maxParticipants || ""}
                                    onChange={handleChange}
                                    placeholder="제한 없음"
                                />
                            </div>

                            {createMutation.isError && (
                                <p className="text-sm text-red-500">
                                    {createMutation.error.message}
                                </p>
                            )}

                            <Button
                                type="submit"
                                className="w-full bg-blue-600 hover:bg-blue-700"
                                disabled={createMutation.isPending}
                            >
                                {createMutation.isPending ? "생성 중..." : "챌린지 만들기"}
                            </Button>
                        </form>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}