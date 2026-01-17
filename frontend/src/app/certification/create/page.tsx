"use client";

import { useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useCreateCertification, useUploadCertificationPhoto, useDeleteCertification } from "@/hooks/useCertification";
import { useChallenge } from "@/hooks/useChallenge";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { CertificationCreateRequest } from "@/types/certification";
import { ArrowLeft, Camera, FileText, CheckCircle, AlertCircle } from "lucide-react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { ALLOWED_IMAGE_EXTENSIONS_STRING } from "@/lib/imageUtils";
import { useImageUpload } from "@/hooks/useImageUpload";

function CreateCertificationContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const challengeIdParam = searchParams.get("challengeId");

  const { data: challenge, isLoading: isChallengeLoading } = useChallenge(challengeIdParam || "");

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [formError, setFormError] = useState<string | null>(null);

  const { 
    file, 
    isCompressing, 
    error: imageError, 
    handleFileChange 
  } = useImageUpload();

  const createMutation = useCreateCertification();
  const uploadPhotoMutation = useUploadCertificationPhoto();
  const deleteMutation = useDeleteCertification();

  const challengeId = challenge?.id;
  const isLoading = createMutation.isPending || uploadPhotoMutation.isPending || deleteMutation.isPending || isCompressing;

  // 챌린지 기간 체크
  const today = new Date();
  const startDate = challenge?.startDate ? new Date(challenge.startDate) : null;
  const endDate = challenge?.endDate ? new Date(challenge.endDate) : null;

  const isStarted = startDate ? startDate <= today : false;
  const isEnded = endDate ? endDate < today : false;
  const isClosed = !isStarted || isEnded;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (imageError) {
        setFormError("이미지 오류를 확인해주세요.");
        return;
    }

    if (isClosed) {
      setFormError(isEnded ? "이미 종료된 챌린지입니다." : "아직 시작되지 않은 챌린지입니다.");
      return;
    }

    if (!challengeId) {
      setFormError("챌린지 ID가 없거나 유효하지 않습니다.");
      return;
    }
    if (!title.trim() || !content.trim()) {
      setFormError("제목과 내용은 비워둘 수 없습니다.");
      return;
    }

    try {
      // 1. Create certification (text only)
      const createRequest: CertificationCreateRequest = { 
        challengeId: challengeId,
        title, 
        content 
      };
      
      const createResponse = await createMutation.mutateAsync(createRequest);

      if (createResponse.success && createResponse.data) {
        const certificationId = createResponse.data.id;

        // 2. Upload photo if available
        if (file) {
          const uploadResponse = await uploadPhotoMutation.mutateAsync({
            id: certificationId,
            file: file
          });
          
          if (!uploadResponse.success) {
            // 사진 업로드 실패 시 생성된 인증 삭제 (Rollback)
            await deleteMutation.mutateAsync({ 
                id: certificationId, 
                challengeId: challengeId 
            });
            setFormError(uploadResponse.message || "사진 업로드에 실패하여 인증 생성이 취소되었습니다.");
            return;
          }
        }
        router.push(`/certification/${certificationId}`); // Redirect to detail page
      } else {
        setFormError(createResponse.message || "인증 생성에 실패했습니다.");
      }
    } catch (err: any) {
      setFormError(err.message || "예기치 않은 오류가 발생했습니다.");
    }
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
              <div className="w-10 h-10 bg-gradient-to-r from-green-600 to-teal-600 rounded-lg flex items-center justify-center text-white">
                <CheckCircle className="w-6 h-6" />
              </div>
              <h1 className="text-4xl font-bold bg-gradient-to-r from-green-600 to-teal-600 bg-clip-text text-transparent">
                챌린지 인증하기
              </h1>
            </div>
            <p className="text-gray-700 font-medium ml-13">
                오늘의 노력을 기록하고 공유해보세요
            </p>
        </div>

        <Card className="border-2 shadow-xl bg-white">
            <CardHeader className="border-b bg-gradient-to-r from-green-50 to-teal-50">
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-gradient-to-r from-green-600 to-teal-600 rounded-lg flex items-center justify-center">
                        <CheckCircle className="w-5 h-5 text-white" />
                    </div>
                    <div>
                        <CardTitle className="text-xl font-bold text-gray-900">인증 내용 작성</CardTitle>
                        <CardDescription className="text-gray-700 font-medium font-bold">
                            챌린지:{" "}
                            {isChallengeLoading ? (
                                "불러오는 중..."
                            ) : (
                                <span 
                                    className="text-blue-600 hover:underline cursor-pointer font-bold"
                                    onClick={() => router.push(`/challenge/${challengeIdParam}`)}
                                >
                                    {challenge?.title || challengeIdParam || "N/A"}
                                </span>
                            )}
                        </CardDescription>
                    </div>
                </div>
            </CardHeader>
            <CardContent className="pt-6 bg-white">
                <form onSubmit={handleSubmit} className="space-y-6">
                    {challengeIdParam === null && (
                        <Alert variant="destructive">
                            <AlertCircle className="h-4 w-4" />
                            <AlertDescription>
                                챌린지 ID가 제공되지 않았습니다. 챌린지 페이지에서 이동해주세요.
                            </AlertDescription>
                        </Alert>
                    )}

                    {isClosed && (
                        <Alert variant="destructive" className="bg-red-50 border-red-200">
                            <AlertCircle className="h-4 w-4 text-red-600" />
                            <AlertDescription className="text-red-700 font-semibold">
                                {isEnded 
                                    ? "이 챌린지는 이미 종료되었습니다. 인증을 등록할 수 없습니다." 
                                    : "이 챌린지는 아직 시작되지 않았습니다. 시작일 이후에 인증해주세요."}
                            </AlertDescription>
                        </Alert>
                    )}

                    <div className="space-y-2">
                        <Label htmlFor="title" className="text-base font-bold flex items-center gap-2 text-gray-900">
                            <FileText className="w-4 h-4 text-green-600" />
                            제목
                        </Label>
                        <Input
                            id="title"
                            type="text"
                            placeholder="오늘의 인증 제목 (예: 1일차 인증합니다!)"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            required
                            className="h-12 border-2 border-gray-300 focus:border-green-500 bg-white text-gray-900 placeholder:text-gray-400"
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="content" className="text-base font-bold text-gray-900">
                            내용
                        </Label>
                        <Textarea
                            id="content"
                            placeholder="어떤 활동을 했는지 자세히 적어주세요."
                            value={content}
                            onChange={(e) => setContent(e.target.value)}
                            required
                            rows={5}
                            className="border-2 border-gray-300 focus:border-green-500 resize-none bg-white text-gray-900 placeholder:text-gray-400"
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="photo" className="text-base font-bold flex items-center gap-2 text-gray-900">
                            <Camera className="w-4 h-4 text-green-600" />
                            사진 첨부 (선택)
                        </Label>
                        <div className="flex items-center justify-center w-full">
                            <label htmlFor="photo" className="flex flex-col items-center justify-center w-full h-32 border-2 border-gray-300 border-dashed rounded-lg cursor-pointer bg-gray-50 hover:bg-gray-100 transition-colors">
                                <div className="flex flex-col items-center justify-center pt-5 pb-6">
                                    <Camera className="w-8 h-8 mb-2 text-gray-400" />
                                    <p className="text-sm text-gray-500 font-medium">
                                        {file ? file.name : "클릭하여 사진을 업로드하세요"}
                                    </p>
                                </div>
                                <Input
                                    id="photo"
                                    type="file"
                                    accept={ALLOWED_IMAGE_EXTENSIONS_STRING}
                                    className="hidden"
                                    onChange={handleFileChange}
                                />
                            </label>
                        </div>
                    </div>

                    {(formError || imageError) && (
                        <Alert variant="destructive" className="border-red-200">
                            <AlertDescription className="font-semibold">
                                {formError || imageError}
                            </AlertDescription>
                        </Alert>
                    )}

                    <Button 
                        type="submit" 
                        className="w-full h-12 bg-gradient-to-r from-green-600 to-teal-600 hover:from-green-700 hover:to-teal-700 shadow-lg hover:shadow-xl transition-all text-base font-bold text-white disabled:opacity-50 disabled:cursor-not-allowed"
                        disabled={isLoading || challengeId === null || isClosed}
                    >
                        {isCompressing ? "사진 압축 중..." : (isLoading ? "인증 올리는 중..." : "인증하기")}
                    </Button>
                </form>
            </CardContent>
        </Card>

        {/* Tips Section */}
        <div className="mt-6 p-4 bg-gradient-to-r from-green-50 to-teal-50 rounded-lg border border-green-200">
            <h3 className="font-bold text-green-900 mb-2 flex items-center gap-2">
                <CheckCircle className="w-4 h-4" />
                인증 팁
            </h3>
            <ul className="text-sm text-green-900 space-y-1 font-medium">
                <li>• 솔직하고 성실하게 활동 내용을 작성해주세요</li>
                <li>• 사진을 함께 올리면 인증 효과가 더 좋습니다</li>
                <li>• 꾸준한 인증은 습관 형성에 큰 도움이 됩니다</li>
            </ul>
        </div>
      </div>
    </div>
  );
}

export default function CreateCertificationPage() {
  return (
    <Suspense fallback={<div className="flex justify-center items-center min-h-screen">Loading...</div>}>
      <CreateCertificationContent />
    </Suspense>
  );
}