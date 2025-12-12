"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useCertification, useDeleteCertification, useUpdateCertification } from "@/hooks/useCertification";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import Image from "next/image";
import { useUserProfile } from "@/hooks/useUser"; // Import useUserProfile
// import { useAuthStore } from "@/stores/authStore"; // No longer needed for user object

interface CertificationDetailPageProps {
  params: {
    id: string;
  };
}

export default function CertificationDetailPage({ params }: CertificationDetailPageProps) {
  const router = useRouter();
  const certificationId = parseInt(params.id);
  const { data: user } = useUserProfile(); // Get current user profile

  const [isEditing, setIsEditing] = useState(false);
  const [editedTitle, setEditedTitle] = useState("");
  const [editedContent, setEditedContent] = useState("");

  const { data, isLoading, error } = useCertification(certificationId);

  useEffect(() => {
    if (data) {
      setEditedTitle(data.title);
      setEditedContent(data.content);
    }
  }, [data]);

  const updateMutation = useUpdateCertification();
  const deleteMutation = useDeleteCertification();

  const handleUpdate = async () => {
    try {
      await updateMutation.mutateAsync({
        id: certificationId,
        data: { title: editedTitle, content: editedContent }
      });
      setIsEditing(false);
    } catch (err) {
      console.error("인증 업데이트 실패:", err);
    }
  };

  const handleDelete = async () => {
    if (confirm("이 인증을 삭제하시겠습니까?")) {
      try {
        await deleteMutation.mutateAsync(certificationId);
        alert("인증이 성공적으로 삭제되었습니다!");
        router.push("/"); // Redirect to a suitable page after deletion (e.g., home or challenge page)
      } catch (err) {
        console.error("인증 삭제 실패:", err);
      }
    }
  };

  if (isNaN(certificationId)) {
    return <div className="text-center py-8">유효하지 않은 인증 ID입니다.</div>;
  }

  if (isLoading) {
    return <div className="text-center py-8">인증 불러오는 중...</div>;
  }

  if (error) {
    return <div className="text-center py-8 text-red-500">오류: {(error as Error).message}</div>;
  }

  if (!data) {
    return <div className="text-center py-8">인증을 찾을 수 없습니다.</div>;
  }

  const isAuthor = user?.nickname === data.authorNickname;

  return (
    <div className="flex justify-center p-4">
      <Card className="w-full max-w-2xl">
        <CardHeader>
          {isEditing ? (
            <Input value={editedTitle} onChange={(e) => setEditedTitle(e.target.value)} className="text-2xl font-bold" />
          ) : (
            <CardTitle className="text-2xl font-bold">{data.title}</CardTitle>
          )}
          <CardDescription>
            작성자: {data.authorNickname} / 챌린지: {data.challengeTitle}
          </CardDescription>
          <CardDescription className="text-sm text-gray-500">
            생성일: {new Date(data.createdAt).toLocaleString()} | 최근 수정일: {new Date(data.updatedAt).toLocaleString()}
          </CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4">
          {data.photoUrl && (
            <div className="relative w-full h-80">
              <Image src={data.photoUrl} alt="Certification Photo" layout="fill" objectFit="contain" className="rounded-md" />
            </div>
          )}
          <div>
            <h3 className="font-semibold mb-2">내용:</h3>
            {isEditing ? (
              <Textarea value={editedContent} onChange={(e) => setEditedContent(e.target.value)} rows={6} />
            ) : (
              <p className="text-gray-700 dark:text-gray-300">{data.content}</p>
            )}
          </div>
        </CardContent>
        {isAuthor && (
          <CardFooter className="flex justify-end gap-2">
            {isEditing ? (
              <>
                <Button variant="outline" onClick={() => setIsEditing(false)} disabled={updateMutation.isPending}>
                  취소
                </Button>
                <Button onClick={handleUpdate} disabled={updateMutation.isPending}>
                  {updateMutation.isPending ? "저장 중..." : "변경 사항 저장"}
                </Button>
              </>
            ) : (
              <>
                <Button variant="outline" onClick={() => setIsEditing(true)}>
                  수정
                </Button>
                <Button variant="destructive" onClick={handleDelete} disabled={deleteMutation.isPending}>
                  {deleteMutation.isPending ? "삭제 중..." : "삭제"}
                </Button>
              </>
            )}
          </CardFooter>
        )}
      </Card>
    </div>
  );
}
