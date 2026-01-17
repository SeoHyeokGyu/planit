"use client";

import { useState } from "react";
import { useSignUp } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { inputStyles, themeStyles } from "@/styles/common";

export function SignUpForm() {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [formError, setFormError] = useState<string | null>(null);

  const signUpMutation = useSignUp();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      setFormError("비밀번호가 일치하지 않습니다.");
      return;
    }
    setFormError(null);
    signUpMutation.mutate({ loginId, password, nickname });
  };

  return (
    <div className="w-full space-y-6">
      {/* Form Title */}
      <div>
        <h2 className="text-2xl font-bold text-gray-900">회원가입</h2>
        <p className="text-gray-500 text-sm mt-2">새로운 계정을 만들어보세요.</p>
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
            placeholder="사용할 아이디"
            required
            value={loginId}
            onChange={(e) => setLoginId(e.target.value)}
            disabled={signUpMutation.isPending}
            className={inputStyles.auth}
          />
        </div>

        {/* Nickname Field */}
        <div className="space-y-2">
          <label htmlFor="nickname" className="block text-sm font-medium text-gray-700">
            닉네임
          </label>
          <Input
            id="nickname"
            type="text"
            placeholder="표시될 닉네임"
            required
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            disabled={signUpMutation.isPending}
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
            disabled={signUpMutation.isPending}
            className={inputStyles.auth}
          />
        </div>

        {/* Confirm Password Field */}
        <div className="space-y-2">
          <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
            비밀번호 확인
          </label>
          <Input
            id="confirmPassword"
            type="password"
            placeholder="비밀번호를 다시 입력하세요"
            required
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            disabled={signUpMutation.isPending}
            className={inputStyles.auth}
          />
        </div>

        {/* Error Message */}
        {(formError || signUpMutation.error) && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm font-medium text-red-600">
              {formError || signUpMutation.error?.message || "회원가입 중 오류가 발생했습니다."}
            </p>
          </div>
        )}

        {/* Submit Button */}
        <Button
          type="submit"
          disabled={signUpMutation.isPending}
          className={`w-full py-3 mt-2 ${themeStyles.primary.btn} text-white font-semibold rounded-lg transition-all shadow-sm hover:shadow-md`}
        >
          {signUpMutation.isPending ? "가입 처리 중..." : "회원가입"}
        </Button>
      </form>
    </div>
  );
}
