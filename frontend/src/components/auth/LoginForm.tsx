"use client";

import { useState } from "react";
import { useLogin } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import Link from "next/link";

export function LoginForm() {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const loginMutation = useLogin();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loginMutation.mutate({ loginId, password });
  };

  // Redirection on success is handled by the useLogin hook

  return (
    <Card className="w-full max-w-sm">
      <CardHeader>
        <CardTitle className="text-2xl">로그인</CardTitle>
        <CardDescription>
          아이디와 비밀번호를 입력하여 로그인하세요.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="loginId">아이디</Label>
            <Input
              id="loginId"
              type="text"
              placeholder="아이디를 입력하세요"
              required
              value={loginId}
              onChange={(e) => setLoginId(e.target.value)}
              disabled={loginMutation.isPending}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="password">비밀번호</Label>
            <Input
              id="password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loginMutation.isPending}
            />
          </div>
          {loginMutation.error && (
            <p className="text-sm font-medium text-red-500">
              {loginMutation.error.message || "로그인 중 오류가 발생했습니다."}
            </p>
          )}
          <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white" disabled={loginMutation.isPending}>
            {loginMutation.isPending ? "로그인 중..." : "로그인"}
          </Button>
        </form>
        <div className="mt-4 text-center text-sm">
          계정이 없으신가요?{" "}
          <Link href="/signup" className="underline">
            회원가입
          </Link>
        </div>
      </CardContent>
    </Card>
  );
}
