"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useCertification, useDeleteCertification, useUpdateCertification } from "@/hooks/useCertification";
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import Image from "next/image";
import { useUserProfile } from "@/hooks/useUser";
import { ArrowLeft, CheckCircle, Calendar, User, FileText, Edit2, Trash2, Save, X } from "lucide-react";
import { Badge } from "@/components/ui/badge";
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

export default function CertificationDetailPage() {
  const router = useRouter();
  const params = useParams();
  const certificationId = Number(params?.id);
  const { data: user } = useUserProfile();

  const [isEditing, setIsEditing] = useState(false);
  const [editedTitle, setEditedTitle] = useState("");
  const [editedContent, setEditedContent] = useState("");
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

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
    try {
      await deleteMutation.mutateAsync({ 
        id: certificationId, 
        challengeId: data?.challengeId
      });
      toast.success("인증이 성공적으로 삭제되었습니다!");
      router.push("/");
    } catch (err) {
      console.error("인증 삭제 실패:", err);
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

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Button
            variant="ghost"
            onClick={() => router.back()}
            className="mb-6 hover:bg-blue-50 text-gray-700 font-medium"
        >
            <ArrowLeft className="w-4 h-4 mr-2" />
            뒤로가기
        </Button>

        <div className="mb-8">
            <div className="flex items-center gap-3 mb-3">
              <div className="w-10 h-10 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-lg flex items-center justify-center text-white">
                <CheckCircle className="w-6 h-6" />
              </div>
              <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
                인증 상세
              </h1>
            </div>
            <p className="text-gray-600 font-medium ml-13">
                챌린지 인증 내용을 확인하세요
            </p>
        </div>

        <Card className="border-2 shadow-xl bg-white overflow-hidden">
            <CardHeader className="border-b bg-gradient-to-r from-blue-50 to-indigo-50 p-6">
                <div className="flex items-start justify-between gap-4">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-lg flex items-center justify-center shrink-0">
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
                    <Badge variant="outline" className="border-blue-200 text-blue-700 bg-blue-50 hover:bg-blue-100 transition-colors">
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
                {data.photoUrl && (
                    <div className="relative w-full h-[400px] rounded-xl overflow-hidden border border-gray-200 bg-gray-50 shadow-inner">
                        <Image 
                            src={data.photoUrl} 
                            alt="Certification Photo" 
                            layout="fill" 
                            objectFit="contain" 
                            className="hover:scale-105 transition-transform duration-500"
                        />
                    </div>
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
                                disabled={updateMutation.isPending}
                                className="border-gray-300 hover:bg-gray-100"
                            >
                                <X className="w-4 h-4 mr-2" />
                                취소
                            </Button>
                            <Button 
                                onClick={handleUpdate} 
                                disabled={updateMutation.isPending}
                                className="bg-blue-600 hover:bg-blue-700 text-white"
                            >
                                {updateMutation.isPending ? (
                                    <span className="animate-spin mr-2">⏳</span>
                                ) : (
                                    <Save className="w-4 h-4 mr-2" />
                                )}
                                저장하기
                            </Button>
                        </>
                    ) : (
                        <>
                            <Button 
                                variant="outline" 
                                onClick={() => setIsEditing(true)}
                                className="border-gray-300 hover:bg-white hover:border-blue-400 hover:text-blue-600 transition-all"
                            >
                                <Edit2 className="w-4 h-4 mr-2" />
                                수정
                            </Button>
                            <Button 
                                variant="destructive" 
                                onClick={() => setIsDeleteDialogOpen(true)} 
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

        <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
            <AlertDialogContent>
                <AlertDialogHeader>
                    <AlertDialogTitle>인증 삭제</AlertDialogTitle>
                    <AlertDialogDescription>
                        정말로 이 인증을 삭제하시겠습니까? 삭제된 인증은 복구할 수 없습니다.
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel>취소</AlertDialogCancel>
                    <AlertDialogAction 
                        onClick={handleDelete}
                        className="bg-red-600 hover:bg-red-700 text-white"
                    >
                        삭제
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
      </div>
    </div>
  );
}
