"use client";

import { useRouter } from "next/navigation";
import { useUserProfile } from "@/hooks/useUser";
import { useCertificationsByUser } from "@/hooks/useCertification";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { ArrowLeft, Calendar, FileText, Search, CheckCircle } from "lucide-react";
import Image from "next/image";
import { Badge } from "@/components/ui/badge";
import { useEffect } from "react";

export default function MyCertificationsPage() {
  const router = useRouter();
  const { data: user } = useUserProfile();
  const { data: certifications, isLoading } = useCertificationsByUser(
    user?.loginId || "",
    0,
    100, // 일단 충분히 많이 가져오도록 설정
    { enabled: !!user?.loginId }
  );

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-10 h-10 bg-gradient-to-r from-green-500 to-blue-500 rounded-lg flex items-center justify-center text-white">
              <CheckCircle className="w-6 h-6" />
            </div>
            <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              나의 인증 목록
            </h1>
          </div>
          <p className="text-gray-600 font-medium ml-13">
            내가 작성한 챌린지 인증 글 목록입니다
          </p>
        </div>

        {/* Certification List */}
        {isLoading ? (
          <CertificationListSkeleton />
        ) : certifications?.content && certifications?.content?.length > 0 ? (
          <>
             <div className="mb-4 text-sm text-gray-700 font-medium">
                  총 <span className="font-bold text-blue-600">{certifications?.totalElements || 0}</span>개의 인증글
             </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {certifications.content.map((cert: any) => (
                <CertificationCard
                  key={cert.id}
                  certification={cert}
                  onClick={() => router.push(`/certification/${cert.id}`)}
                />
              ))}
            </div>
          </>
        ) : (
          <div className="text-center py-20 bg-white rounded-2xl shadow-sm border-2 border-dashed border-gray-200">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-gray-50 rounded-full mb-4">
              <Search className="w-8 h-8 text-gray-300" />
            </div>
            <p className="text-gray-700 text-lg font-semibold">
              작성한 인증글이 없습니다.
            </p>
            <p className="text-gray-500 text-sm mt-2 mb-6">
              챌린지에 참여하고 첫 인증을 남겨보세요!
            </p>
            <Button
              onClick={() => router.push("/challenge/my")}
              className="bg-blue-600 hover:bg-blue-700"
            >
              참여 중인 챌린지 보기
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}

function CertificationCard({
  certification,
  onClick,
}: {
  certification: any;
  onClick: () => void;
}) {
  return (
    <Card
      className="cursor-pointer hover:shadow-2xl transition-all duration-300 border-2 hover:border-blue-300 group bg-white overflow-hidden"
      onClick={onClick}
    >
      <div className="relative h-48 w-full bg-gray-100">
        {certification.photoUrl ? (
          <Image
            src={certification.photoUrl}
            alt={certification.title}
            layout="fill"
            objectFit="cover"
            className="group-hover:scale-105 transition-transform duration-500"
          />
        ) : (
          <div className="flex items-center justify-center h-full text-gray-400 bg-gray-50">
            <FileText className="w-12 h-12 opacity-20" />
          </div>
        )}
        <div className="absolute top-2 right-2">
            <Badge variant="secondary" className="bg-white/90 text-blue-800 backdrop-blur-sm shadow-sm font-semibold">
                {certification.challengeTitle}
            </Badge>
        </div>
      </div>
      <CardHeader className="pb-2">
        <CardTitle className="text-lg font-bold text-gray-900 line-clamp-1 group-hover:text-blue-600 transition-colors">
          {certification.title}
        </CardTitle>
        <CardDescription className="flex items-center gap-1 text-xs text-gray-500 font-medium">
          <Calendar className="w-3 h-3" />
          {new Date(certification.createdAt).toLocaleDateString("ko-KR", {
            year: "numeric",
            month: "long",
            day: "numeric",
          })}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <p className="text-sm text-gray-600 line-clamp-2 font-medium leading-relaxed">
          {certification.content}
        </p>
      </CardContent>
    </Card>
  );
}

function CertificationListSkeleton() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {[...Array(3)].map((_, i) => (
        <Card key={i} className="border-2">
          <Skeleton className="h-48 w-full rounded-t-xl" />
          <CardHeader>
            <Skeleton className="h-6 w-3/4 mb-2" />
            <Skeleton className="h-4 w-1/2" />
          </CardHeader>
          <CardContent>
            <Skeleton className="h-4 w-full mb-2" />
            <Skeleton className="h-4 w-2/3" />
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
