"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useFollowers, useFollowings } from "@/hooks/useFollow";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Users, Loader2 } from "lucide-react";
import FollowButton from "./FollowButton";

interface FollowListModalProps {
  userLoginId: string;
  isOpen: boolean;
  onClose: () => void;
  defaultTab?: "followers" | "followings";
}

/**
 * 팔로워/팔로잉 목록을 보여주는 모달 컴포넌트
 */
export default function FollowListModal({
  userLoginId,
  isOpen,
  onClose,
  defaultTab = "followers",
}: FollowListModalProps) {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<"followers" | "followings">(defaultTab);
  const [currentPage, setCurrentPage] = useState(0);

  // 팔로워/팔로잉 목록 조회
  const {
    data: followers,
    isLoading: isLoadingFollowers,
    isError: isErrorFollowers,
  } = useFollowers(userLoginId, currentPage, 20);

  const {
    data: followings,
    isLoading: isLoadingFollowings,
    isError: isErrorFollowings,
  } = useFollowings(userLoginId, currentPage, 20);

  const currentList = activeTab === "followers" ? followers : followings;
  const isLoading = activeTab === "followers" ? isLoadingFollowers : isLoadingFollowings;
  const isError = activeTab === "followers" ? isErrorFollowers : isErrorFollowings;

  const handleNavigateToProfile = (loginId: string) => {
    router.push(`/profile/${loginId}`);
    onClose();
  };

  const handleNextPage = () => {
    setCurrentPage((prev) => prev + 1);
  };

  const handlePrevPage = () => {
    setCurrentPage((prev) => Math.max(0, prev - 1));
  };

  // 탭 변경 시 페이지 초기화
  const handleTabChange = (tab: "followers" | "followings") => {
    setActiveTab(tab);
    setCurrentPage(0);
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md max-h-[80vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <div className="flex items-center space-x-2">
            <Users className="w-5 h-5 text-blue-600" />
            <DialogTitle>팔로우 목록</DialogTitle>
          </div>
          <DialogDescription>팔로워와 팔로잉 목록을 확인하세요.</DialogDescription>
        </DialogHeader>

        {/* 탭 */}
        <Tabs
          value={activeTab}
          onValueChange={(value) => handleTabChange(value as "followers" | "followings")}
          className="flex-1 flex flex-col"
        >
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="followers">팔로워</TabsTrigger>
            <TabsTrigger value="followings">팔로잉</TabsTrigger>
          </TabsList>

          {/* 팔로워 탭 */}
          <TabsContent value="followers" className="flex-1 overflow-y-auto mt-4">
            {isLoading ? (
              <div className="flex items-center justify-center h-32">
                <Loader2 className="w-6 h-6 animate-spin text-blue-600" />
              </div>
            ) : isError ? (
              <div className="text-center py-8 text-red-500">팔로워 목록을 불러올 수 없습니다.</div>
            ) : followers && followers.length > 0 ? (
              <div className="space-y-3">
                {followers.map((user) => (
                  <div
                    key={user.loginId}
                    className="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                  >
                    <div
                      className="flex-1 cursor-pointer"
                      onClick={() => handleNavigateToProfile(user.loginId)}
                    >
                      <p className="font-semibold text-gray-900">{user.nickname}</p>
                      <p className="text-sm text-blue-600">@{user.loginId}</p>
                    </div>
                    <FollowButton targetLoginId={user.loginId} variant="outline" size="sm" />
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-blue-600">
                팔로워가 없습니다.
              </div>
            )}
          </TabsContent>

          {/* 팔로잉 탭 */}
          <TabsContent value="followings" className="flex-1 overflow-y-auto mt-4">
            {isLoading ? (
              <div className="flex items-center justify-center h-32">
                <Loader2 className="w-6 h-6 animate-spin text-blue-600" />
              </div>
            ) : isError ? (
              <div className="text-center py-8 text-red-500">팔로잉 목록을 불러올 수 없습니다.</div>
            ) : followings && followings.length > 0 ? (
              <div className="space-y-3">
                {followings.map((user) => (
                  <div
                    key={user.loginId}
                    className="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                  >
                    <div
                      className="flex-1 cursor-pointer"
                      onClick={() => handleNavigateToProfile(user.loginId)}
                    >
                      <p className="font-semibold text-gray-900">{user.nickname}</p>
                      <p className="text-sm text-blue-600">@{user.loginId}</p>
                    </div>
                    <FollowButton targetLoginId={user.loginId} variant="outline" size="sm" />
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-blue-600">
                팔로잉이 없습니다.
              </div>
            )}
          </TabsContent>
        </Tabs>

        {/* 페이지네이션 */}
        {currentList && currentList.length > 0 && (
          <div className="flex items-center justify-between pt-4 border-t">
            <Button
              variant="outline"
              size="sm"
              onClick={handlePrevPage}
              disabled={currentPage === 0}
            >
              이전
            </Button>
            <span className="text-sm text-gray-600">
              {currentPage + 1} 페이지
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={handleNextPage}
              disabled={currentList.length < 20}
            >
              다음
            </Button>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
