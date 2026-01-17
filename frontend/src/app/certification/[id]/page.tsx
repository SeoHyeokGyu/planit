"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import {
  useCertification,
  useDeleteCertification,
  useUpdateCertification,
  useUploadCertificationPhoto,
  useDeleteCertificationPhoto,
} from "@/hooks/useCertification";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
  CardFooter,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import Image from "next/image";
import { useUserProfile } from "@/hooks/useUser";
import {
  ArrowLeft,
  CheckCircle,
  Calendar,
  User,
  FileText,
  Edit2,
  Trash2,
  Save,
  X,
  Camera,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import { FallbackImage } from "@/components/ui/fallback-image";
import { ALLOWED_IMAGE_EXTENSIONS_STRING } from "@/lib/imageUtils";
import { useImageUpload } from "@/hooks/useImageUpload";
import { layoutStyles, headerStyles, cardStyles, buttonStyles, themeStyles } from "@/styles/common";
import { useConfirm } from "@/hooks/useConfirm";

export default function CertificationDetailPage() {
  const router = useRouter();
  const params = useParams();
  const certificationId = Number(params?.id);
  const { data: user } = useUserProfile();

  const [isEditing, setIsEditing] = useState(false);
  const [editedTitle, setEditedTitle] = useState("");
  const [editedContent, setEditedContent] = useState("");

  const { confirm, ConfirmDialog } = useConfirm();

  const {
    file: editedFile,
    previewUrl,
    isCompressing,
    handleFileChange,
    setPreviewUrl,
    setFile,
    reset: resetImageUpload,
  } = useImageUpload();

  const { data, isLoading, error, refetch } = useCertification(certificationId);

  const updateMutation = useUpdateCertification();
  const deleteMutation = useDeleteCertification();
  const uploadPhotoMutation = useUploadCertificationPhoto();
  const deletePhotoMutation = useDeleteCertificationPhoto();

  const handleEditClick = () => {
    if (data) {
      setEditedTitle(data.title);
      setEditedContent(data.content);
      resetImageUpload();
      setPreviewUrl(data.photoUrl || null);
      setIsEditing(true);
    }
  };

  const handleDeletePhoto = async (e: React.MouseEvent) => {
    e.preventDefault();

    if (editedFile) {
      setFile(null);
      setPreviewUrl(data?.photoUrl || null);
      return;
    }

    if (
      await confirm({
        title: "사진 삭제",
        description: "정말로 사진을 삭제하시겠습니까? 삭제된 사진은 복구할 수 없습니다.",
        variant: "destructive",
      })
    ) {
      try {
        if (data?.photoUrl) {
          await deletePhotoMutation.mutateAsync(certificationId);
        }
        setPreviewUrl(null);
        setFile(null);
        refetch();
      } catch (err) {
        console.error("사진 삭제 실패:", err);
      }
    }
  };

  const handleUpdate = async () => {
    try {
      if (editedFile) {
        const uploadResponse = await uploadPhotoMutation.mutateAsync({
          id: certificationId,
          file: editedFile,
        });

        if (!uploadResponse.success) {
          toast.error(uploadResponse.message || "사진 업로드에 실패했습니다.");
          return;
        }
      }

      await updateMutation.mutateAsync({
        id: certificationId,
        data: { title: editedTitle, content: editedContent },
      });

      toast.success("인증이 성공적으로 수정되었습니다!");
      setIsEditing(false);
      refetch();
    } catch (err) {
      console.error("인증 업데이트 실패:", err);
    }
  };

  const handleDelete = async () => {
    if (
      await confirm({
        title: "인증 삭제",
        description: "정말로 이 인증을 삭제하시겠습니까? 삭제된 인증은 복구할 수 없습니다.",
        variant: "destructive",
      })
    ) {
      try {
        await deleteMutation.mutateAsync({
          id: certificationId,
          challengeId: data?.challengeId,
        });
        toast.success("인증이 성공적으로 삭제되었습니다!");
        router.push("/");
      } catch (err) {
        console.error("인증 삭제 실패:", err);
      }
    }
  };

  if (isNaN(certificationId)) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-50">
        <div className="text-center p-8 bg-white rounded-xl shadow-md">
          <p className="text-red-500 font-bold mb-4">유효하지 않은 인증 ID입니다.</p>
          <Button onClick={() => router.back()}>뒤로가기</Button>
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-50">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-50">
        <div className="text-center p-8 bg-white rounded-xl shadow-md">
          <p className="text-red-500 font-bold mb-4">오류: {(error as Error).message}</p>
          <Button onClick={() => router.back()}>뒤로가기</Button>
        </div>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-50">
        <div className="text-center p-8 bg-white rounded-xl shadow-md">
          <p className="text-gray-700 font-bold mb-4">인증을 찾을 수 없습니다.</p>
          <Button onClick={() => router.back()}>뒤로가기</Button>
        </div>
      </div>
    );
  }

  const isAuthor = user?.nickname === data.authorNickname;
  const isUpdating =
    updateMutation.isPending ||
    uploadPhotoMutation.isPending ||
    deletePhotoMutation.isPending ||
    isCompressing;

  return (
    <div className={layoutStyles.pageRoot}>
      <div className={layoutStyles.containerMd}>
        <Button variant="ghost" onClick={() => router.back()} className={buttonStyles.back}>
          <ArrowLeft className="w-4 h-4 mr-2" />
          뒤로가기
        </Button>

        <div className={headerStyles.wrapper}>
          <div className={headerStyles.content}>
            <div className={`${headerStyles.icon} ${themeStyles.info.bg}`}>
              <CheckCircle className="w-6 h-6" />
            </div>
            <h1 className={`${headerStyles.title} ${themeStyles.info.text}`}>인증 상세</h1>
          </div>
          <p className={headerStyles.description}>챌린지 인증 내용을 확인하세요</p>
        </div>

        <Card className={cardStyles.base}>
          <CardHeader className={`${cardStyles.headerGradient} ${themeStyles.info.headerBg}`}>
            <div className="flex items-start justify-between gap-4">
              <div className="flex items-center gap-3">
                <div
                  className={`w-10 h-10 ${themeStyles.info.bg} rounded-lg flex items-center justify-center shrink-0`}
                >
                  <CheckCircle className="w-5 h-5 text-white" />
                </div>
                <div className="space-y-1">
                  {isEditing ? (
                    <Input
                      value={editedTitle}
                      onChange={(e) => setEditedTitle(e.target.value)}
                      className="text-xl font-bold h-10 border-blue-300 focus:border-blue-500"
                    />
                  ) : (
                    <CardTitle className="text-2xl font-bold text-gray-900 leading-tight">
                      {data.title}
                    </CardTitle>
                  )}
                  <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-gray-600 font-medium">
                    <span className="flex items-center gap-1">
                      <User className="w-3.5 h-3.5" />
                      {data.authorNickname}
                    </span>
                    <span className="text-gray-300">|</span>
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3.5 h-3.5" />
                      {new Date(data.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                </div>
              </div>
              <Badge
                variant="outline"
                className="border-blue-200 text-blue-700 bg-blue-50 hover:bg-blue-100 transition-colors"
              >
                <span
                  className="cursor-pointer hover:underline font-bold"
                  onClick={(e) => {
                    e.stopPropagation();
                    router.push(`/challenge/${data.challengeId}`);
                  }}
                >
                  {data.challengeTitle}
                </span>
              </Badge>
            </div>
          </CardHeader>

          <CardContent className="p-6 space-y-6">
            {isEditing ? (
              <div className="space-y-4">
                <div className="flex items-center justify-center w-full">
                  <div className="relative w-full">
                    <label
                      htmlFor="photo-upload"
                      className={`flex flex-col items-center justify-center w-full h-64 border-2 ${previewUrl ? "border-solid border-gray-200" : "border-dashed border-blue-300"} rounded-lg cursor-pointer bg-blue-50 hover:bg-blue-100 transition-colors relative overflow-hidden group`}
                    >
                      {previewUrl ? (
                        <FallbackImage
                          src={previewUrl}
                          alt="Preview"
                          fill
                          className="object-contain opacity-100 group-hover:opacity-75 transition-opacity"
                          sizes="(max-width: 768px) 100vw, 50vw"
                        />
                      ) : (
                        <div className="flex flex-col items-center justify-center pt-5 pb-6">
                          <Camera className="w-10 h-10 mb-3 text-blue-500" />
                          <p className="mb-2 text-sm text-blue-700 font-semibold">
                            클릭하여 사진 등록/변경
                          </p>
                        </div>
                      )}
                      <input
                        id="photo-upload"
                        type="file"
                        accept={ALLOWED_IMAGE_EXTENSIONS_STRING}
                        className="hidden"
                        onChange={handleFileChange}
                      />
                      {previewUrl && (
                        <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity bg-black/30 text-white font-bold pointer-events-none">
                          <Camera className="w-8 h-8 mb-2" />
                          <span className="ml-2">사진 변경하기</span>
                        </div>
                      )}
                    </label>

                    {previewUrl && (
                      <Button
                        type="button"
                        variant="destructive"
                        size="icon"
                        className="absolute top-2 right-2 h-8 w-8 rounded-full shadow-md z-10"
                        onClick={handleDeletePhoto}
                        title="사진 삭제"
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            ) : (
              data.photoUrl && (
                <div className="relative w-full h-[400px] rounded-xl overflow-hidden border border-gray-200 bg-gray-50 shadow-inner">
                  <FallbackImage
                    src={data.photoUrl}
                    alt="Certification Photo"
                    fill
                    className="object-contain hover:scale-105 transition-transform duration-500"
                    sizes="(max-width: 768px) 100vw, 800px"
                  />
                </div>
              )
            )}

            <div className="bg-gray-50 rounded-xl p-5 border border-gray-100">
              <div className="flex items-center gap-2 mb-3 text-gray-900 font-bold">
                <FileText className="w-4 h-4 text-blue-600" />
                인증 내용
              </div>
              {isEditing ? (
                <Textarea
                  value={editedContent}
                  onChange={(e) => setEditedContent(e.target.value)}
                  rows={8}
                  className="bg-white border-blue-300 focus:border-blue-500 resize-none"
                />
              ) : (
                <p className="text-gray-700 leading-relaxed whitespace-pre-wrap font-medium">
                  {data.content}
                </p>
              )}
            </div>
          </CardContent>

          {isAuthor && (
            <CardFooter className="bg-gray-50 border-t p-4 flex justify-end gap-3">
              {isEditing ? (
                <>
                  <Button
                    variant="outline"
                    onClick={() => setIsEditing(false)}
                    disabled={isUpdating}
                    className="border-gray-300 hover:bg-gray-100"
                  >
                    <X className="w-4 h-4 mr-2" />
                    취소
                  </Button>
                  <Button
                    onClick={handleUpdate}
                    disabled={isUpdating}
                    className="bg-blue-600 hover:bg-blue-700 text-white"
                  >
                    {isUpdating ? (
                      <span className="animate-spin mr-2">⏳</span>
                    ) : (
                      <Save className="w-4 h-4 mr-2" />
                    )}
                    {isCompressing ? "사진 압축 중..." : "저장하기"}
                  </Button>
                </>
              ) : (
                <>
                  <Button
                    variant="outline"
                    onClick={handleEditClick}
                    className="border-gray-300 hover:bg-white hover:border-blue-400 hover:text-blue-600 transition-all"
                  >
                    <Edit2 className="w-4 h-4 mr-2" />
                    수정
                  </Button>
                  <Button
                    variant="destructive"
                    onClick={handleDelete}
                    disabled={deleteMutation.isPending}
                    className="bg-red-50 text-red-600 border border-red-200 hover:bg-red-100 hover:border-red-300 shadow-none hover:shadow-sm transition-all"
                  >
                    {deleteMutation.isPending ? (
                      <span className="animate-spin mr-2">⏳</span>
                    ) : (
                      <Trash2 className="w-4 h-4 mr-2" />
                    )}
                    삭제
                  </Button>
                </>
              )}
            </CardFooter>
          )}
        </Card>

        <ConfirmDialog />
      </div>
    </div>
  );
}