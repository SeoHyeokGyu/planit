"use client";

import { useState } from "react";
import { useLogin } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { inputStyles, themeStyles } from "@/styles/common";

export function LoginForm() {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const loginMutation = useLogin();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loginMutation.mutate({ loginId, password });
  };

  return (
    <div className="w-full space-y-6">
      {/* Form Title */}
      <div>
        <h2 className="text-2xl font-bold text-gray-900">로그인</h2>
        <p className="text-gray-500 text-sm mt-2">계정에 접속하세요.</p>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Login ID Field */}
        <div className="space-y-2">
          <label htmlFor="loginId" className="block text-sm font-medium text-gray-700">
            아이디
          </label>
          <Input
            id="loginId"
            type="text"
            placeholder="아이디를 입력하세요"
            required
            value={loginId}
            onChange={(e) => setLoginId(e.target.value)}
            disabled={loginMutation.isPending}
            className={inputStyles.auth}
          />
        </div>

        {/* Password Field */}
        <div className="space-y-2">
          <label htmlFor="password" className="block text-sm font-medium text-gray-700">
            비밀번호
          </label>
          <Input
            id="password"
            type="password"
            placeholder="비밀번호를 입력하세요"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={loginMutation.isPending}
            className={inputStyles.auth}
          />
        </div>

        {/* Error Message */}
        {loginMutation.error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm font-medium text-red-600">
              {loginMutation.error.message || "로그인 중 오류가 발생했습니다."}
            </p>
          </div>
        )}

        {/* Submit Button */}
        <Button
          type="submit"
          disabled={loginMutation.isPending}
          className={`w-full py-3 mt-2 ${themeStyles.primary.btn} text-white font-semibold rounded-lg transition-all shadow-sm hover:shadow-md`}
        >
          {loginMutation.isPending ? "로그인 중..." : "로그인"}
        </Button>
      </form>
    </div>
  );
}
