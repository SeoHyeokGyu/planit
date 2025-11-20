# Planit Frontend

여행 플래너 애플리케이션의 프론트엔드 프로젝트입니다.

## 기술 스택

- **프레임워크**: Next.js 16 (App Router)
- **언어**: TypeScript
- **스타일링**: Tailwind CSS
- **UI 컴포넌트**: Shadcn/ui
- **상태 관리**: Zustand
- **데이터 패칭**: TanStack React Query
- **아이콘**: Lucide React

## 시작하기

### 의존성 설치

```bash
npm install
```

### 개발 서버 실행

```bash
npm run dev
```

브라우저에서 [http://localhost:3000](http://localhost:3000)을 열어서 결과를 확인하세요.

### 빌드

```bash
npm run build
```

### 프로덕션 모드 실행

```bash
npm start
```

## 환경 변수

`.env.local` 파일을 생성하고 다음 환경 변수를 설정하세요:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

프로덕션 환경에서는:

```env
NEXT_PUBLIC_API_URL=https://planit-api-y2ie.onrender.com
```

## 프로젝트 구조

```
src/
├── app/              # Next.js App Router 페이지
├── components/       # 재사용 가능한 컴포넌트
├── providers/        # Context Providers (React Query)
├── stores/           # Zustand 상태 관리
├── services/         # API 서비스 레이어
├── hooks/            # 커스텀 React Hooks
├── lib/              # 유틸리티 함수
├── types/            # TypeScript 타입 정의
└── constants/        # 상수 정의
```

## API 연동

백엔드 API와의 통신은 `src/lib/api.ts`의 API 클라이언트를 통해 이루어집니다.

### 사용 예시

```typescript
import { authService } from "@/services/authService";

// 로그인
const response = await authService.login({
  loginId: "user123",
  password: "password123",
});

// 프로필 조회
const profile = await authService.getProfile();
```

## Shadcn/ui 컴포넌트 추가

```bash
npx shadcn@latest add button
npx shadcn@latest add input
npx shadcn@latest add card
```

## 라이센스

이 프로젝트는 MIT 라이센스로 배포됩니다.
