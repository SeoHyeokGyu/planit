'use client';

import { useState } from 'react';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

interface ApiResponse {
  status: number;
  data: any;
  error?: string;
  timestamp: string;
}

export default function ApiTestPage() {
  const [responses, setResponses] = useState<Record<string, ApiResponse>>({});
  const [loading, setLoading] = useState<Record<string, boolean>>({});
  const [token, setToken] = useState<string>('');

  // 회원가입 폼 상태
  const [signupForm, setSignupForm] = useState({
    loginId: '',
    password: '',
    nickname: '',
  });

  // 로그인 폼 상태
  const [loginForm, setLoginForm] = useState({
    loginId: '',
    password: '',
  });

  const testApi = async (key: string, url: string, options?: RequestInit) => {
    setLoading((prev) => ({ ...prev, [key]: true }));
    try {
      const response = await fetch(`${API_BASE_URL}${url}`, {
        ...options,
        headers: {
          'Content-Type': 'application/json',
          ...(token && { Authorization: `Bearer ${token}` }),
          ...options?.headers,
        },
      });

      const data = await response.json().catch(() => null);

      setResponses((prev) => ({
        ...prev,
        [key]: {
          status: response.status,
          data,
          timestamp: new Date().toLocaleTimeString('ko-KR'),
        },
      }));

      // 로그인 성공 시 토큰 저장
      if (key === 'login' && data?.data?.accessToken) {
        setToken(data.data.accessToken);
      }
    } catch (error: any) {
      setResponses((prev) => ({
        ...prev,
        [key]: {
          status: 0,
          data: null,
          error: error.message,
          timestamp: new Date().toLocaleTimeString('ko-KR'),
        },
      }));
    } finally {
      setLoading((prev) => ({ ...prev, [key]: false }));
    }
  };

  const generateRandomData = () => {
    const randomString = Math.random().toString(36).substring(7);
    setSignupForm({
      loginId: `user_${randomString}`,
      password: `pass_${randomString}`,
      nickname: `닉네임_${randomString}`,
    });
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-6xl mx-auto">
        <h1 className="text-3xl font-bold mb-2">Planit API 테스트</h1>
        <p className="text-gray-600 mb-6">백엔드: {API_BASE_URL}</p>

        {/* 토큰 표시 */}
        {token && (
          <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg">
            <p className="text-sm font-semibold text-green-800 mb-2">인증 토큰:</p>
            <code className="text-xs text-green-700 break-all">{token}</code>
            <button
              onClick={() => setToken('')}
              className="ml-4 text-xs text-red-600 hover:text-red-800"
            >
              토큰 삭제
            </button>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Health Check */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-xl font-semibold mb-4">1. Health Check</h2>
            <button
              onClick={() => testApi('health', '/api/health')}
              disabled={loading.health}
              className="w-full bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 disabled:bg-gray-400"
            >
              {loading.health ? '테스트 중...' : 'GET /api/health'}
            </button>
            {responses.health && (
              <ResponseBox response={responses.health} />
            )}
          </div>

          {/* 회원가입 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-xl font-semibold mb-4">2. 회원가입</h2>
            <div className="space-y-3">
              <input
                type="text"
                placeholder="로그인 ID"
                value={signupForm.loginId}
                onChange={(e) =>
                  setSignupForm({ ...signupForm, loginId: e.target.value })
                }
                className="w-full px-3 py-2 border rounded"
              />
              <input
                type="password"
                placeholder="비밀번호"
                value={signupForm.password}
                onChange={(e) =>
                  setSignupForm({ ...signupForm, password: e.target.value })
                }
                className="w-full px-3 py-2 border rounded"
              />
              <input
                type="text"
                placeholder="닉네임"
                value={signupForm.nickname}
                onChange={(e) =>
                  setSignupForm({ ...signupForm, nickname: e.target.value })
                }
                className="w-full px-3 py-2 border rounded"
              />
              <div className="flex gap-2">
                <button
                  onClick={generateRandomData}
                  className="flex-1 bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
                >
                  랜덤 생성
                </button>
                <button
                  onClick={() =>
                    testApi('signup', '/api/auth/signup', {
                      method: 'POST',
                      body: JSON.stringify(signupForm),
                    })
                  }
                  disabled={loading.signup}
                  className="flex-1 bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 disabled:bg-gray-400"
                >
                  {loading.signup ? '처리 중...' : 'POST /api/auth/signup'}
                </button>
              </div>
            </div>
            {responses.signup && (
              <ResponseBox response={responses.signup} />
            )}
          </div>

          {/* 로그인 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-xl font-semibold mb-4">3. 로그인</h2>
            <div className="space-y-3">
              <input
                type="text"
                placeholder="로그인 ID"
                value={loginForm.loginId}
                onChange={(e) =>
                  setLoginForm({ ...loginForm, loginId: e.target.value })
                }
                className="w-full px-3 py-2 border rounded"
              />
              <input
                type="password"
                placeholder="비밀번호"
                value={loginForm.password}
                onChange={(e) =>
                  setLoginForm({ ...loginForm, password: e.target.value })
                }
                className="w-full px-3 py-2 border rounded"
              />
              <div className="flex gap-2">
                <button
                  onClick={() => {
                    setLoginForm({
                      loginId: signupForm.loginId,
                      password: signupForm.password,
                    });
                  }}
                  className="flex-1 bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
                >
                  회원가입 정보 사용
                </button>
                <button
                  onClick={() =>
                    testApi('login', '/api/auth/login', {
                      method: 'POST',
                      body: JSON.stringify(loginForm),
                    })
                  }
                  disabled={loading.login}
                  className="flex-1 bg-purple-500 text-white px-4 py-2 rounded hover:bg-purple-600 disabled:bg-gray-400"
                >
                  {loading.login ? '처리 중...' : 'POST /api/auth/login'}
                </button>
              </div>
            </div>
            {responses.login && (
              <ResponseBox response={responses.login} />
            )}
          </div>

          {/* 내 정보 조회 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-xl font-semibold mb-4">4. 내 정보 조회</h2>
            <p className="text-sm text-gray-600 mb-3">
              {token ? '토큰이 설정되어 있습니다.' : '로그인이 필요합니다.'}
            </p>
            <button
              onClick={() => testApi('me', '/api/users/me')}
              disabled={loading.me || !token}
              className="w-full bg-indigo-500 text-white px-4 py-2 rounded hover:bg-indigo-600 disabled:bg-gray-400"
            >
              {loading.me ? '조회 중...' : 'GET /api/users/me'}
            </button>
            {responses.me && (
              <ResponseBox response={responses.me} />
            )}
          </div>

          {/* Swagger UI 링크 */}
          <div className="bg-white p-6 rounded-lg shadow lg:col-span-2">
            <h2 className="text-xl font-semibold mb-4">5. Swagger UI</h2>
            <a
              href={`${API_BASE_URL}/swagger-ui/index.html`}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-block bg-orange-500 text-white px-6 py-3 rounded hover:bg-orange-600"
            >
              Swagger UI 열기 →
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}

function ResponseBox({ response }: { response: ApiResponse }) {
  const isSuccess = response.status >= 200 && response.status < 300;
  const isError = response.status >= 400 || response.error;

  return (
    <div
      className={`mt-4 p-4 rounded border ${
        isSuccess
          ? 'bg-green-50 border-green-200'
          : isError
          ? 'bg-red-50 border-red-200'
          : 'bg-gray-50 border-gray-200'
      }`}
    >
      <div className="flex justify-between items-center mb-2">
        <span
          className={`font-semibold ${
            isSuccess
              ? 'text-green-800'
              : isError
              ? 'text-red-800'
              : 'text-gray-800'
          }`}
        >
          {response.error ? 'Network Error' : `HTTP ${response.status}`}
        </span>
        <span className="text-xs text-gray-500">{response.timestamp}</span>
      </div>
      <pre className="text-xs overflow-auto max-h-40 bg-white p-2 rounded">
        {JSON.stringify(response.error || response.data, null, 2)}
      </pre>
    </div>
  );
}
