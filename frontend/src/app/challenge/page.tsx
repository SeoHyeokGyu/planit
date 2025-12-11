"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import Header from "@/components/layout/Header";

export default function ChallengePage() {
  const router = useRouter();
  const token = useAuthStore((state) => state.token);

  useEffect(() => {
    if (!token) {
      router.push("/login");
    }
  }, [token, router]);

  if (!token) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-7xl mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">챌린지</h1>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div className="bg-white rounded-xl shadow-sm p-6 text-center">
            <div className="text-gray-400 text-6xl mb-4">+</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              새 챌린지 만들기
            </h3>
            <p className="text-gray-500 text-sm mb-4">
              나만의 챌린지를 시작해보세요
            </p>
            <button className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 font-medium">
              챌린지 생성
            </button>
          </div>
        </div>

        <div className="mt-8 bg-white rounded-xl shadow-sm p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            인기 챌린지
          </h2>
          <div className="text-center py-8 text-gray-500">
            아직 참여할 수 있는 챌린지가 없습니다.
          </div>
        </div>
      </main>
    </div>
  );
}
