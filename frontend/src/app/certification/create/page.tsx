"use client";

import { useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useCreateCertification, useUploadCertificationPhoto } from "@/hooks/useCertification";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CertificationCreateRequest } from "@/types/certification";

function CreateCertificationContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const challengeIdParam = searchParams.get("challengeId");

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);

  const createMutation = useCreateCertification();
  const uploadPhotoMutation = useUploadCertificationPhoto();

  const challengeId = challengeIdParam ? parseInt(challengeIdParam) : null;
  const isLoading = createMutation.isPending || uploadPhotoMutation.isPending;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (challengeId === null || isNaN(challengeId)) {
      setError("챌린지 ID가 없거나 유효하지 않습니다.");
      return;
    }
    if (!title.trim() || !content.trim()) {
      setError("제목과 내용은 비워둘 수 없습니다.");
      return;
    }

    try {
      // 1. Create certification (text only)
      const createRequest: CertificationCreateRequest = { challengeId, title, content };
      
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
            setError(uploadResponse.message || "사진 업로드에 실패했습니다.");
            // Even if photo upload fails, the certification was created, so we might still want to redirect or show a partial success message.
            // For now, let's just return and show the error.
            return;
          }
        }
        router.push(`/certification/${certificationId}`); // Redirect to detail page
      } else {
        setError(createResponse.message || "인증 생성에 실패했습니다.");
      }
    } catch (err: any) {
      setError(err.message || "예기치 않은 오류가 발생했습니다.");
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-50 dark:bg-gray-900">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-2xl font-bold">새 인증 생성</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="grid gap-4">
            {challengeIdParam === null && (
              <p className="text-red-500">
                에러: 챌린지 ID가 제공되지 않았습니다. 챌린지 페이지에서 이동해주세요.
              </p>
            )}
            <div>
              <Label htmlFor="challengeId">챌린지 ID</Label>
              <Input
                id="challengeId"
                type="text"
                value={challengeIdParam || ""}
                disabled
                className="bg-gray-100 dark:bg-gray-800"
              />
            </div>
            <div>
              <Label htmlFor="title">제목</Label>
              <Input
                id="title"
                type="text"
                placeholder="인증 제목"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />
            </div>
            <div>
              <Label htmlFor="content">내용</Label>
              <Textarea
                id="content"
                placeholder="인증 내용을 작성해주세요"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                required
              />
            </div>
            <div>
              <Label htmlFor="photo">사진 (선택 사항)</Label>
              <Input
                id="photo"
                type="file"
                accept="image/*"
                onChange={(e) => setFile(e.target.files ? e.target.files[0] : null)}
              />
            </div>
            {error && <p className="text-red-500 text-sm">{error}</p>}
            <Button type="submit" className="w-full" disabled={isLoading || challengeId === null}>
              {isLoading ? "생성 중..." : "인증 생성"}
            </Button>
          </form>
        </CardContent>
      </Card>
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