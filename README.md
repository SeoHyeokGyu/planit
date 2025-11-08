# AI 기반 소셜 챌린지 트래커

> 작은 성취를 실시간으로 공유하고, AI가 다음 도전을 추천해주는 동기부여 플랫폼 (웹 기반)

---

## 프로젝트 개요

**챌린지 트래커**는 사용자들이 개인의 작은 성취를 실시간으로 공유하며 함께 성장하는 소셜 플랫폼입니다.

### 핵심 기능
- **실시간 피드**: SSE 기반 실시간 인증 알림 ("OO님이 방금 인증했습니다!")
- **AI 추천 엔진**: 5가지 알고리즘으로 맞춤 챌린지 추천
- **AI 챌린지 생성기**: 키워드만 입력하면 GPT가 챌린지 자동 생성
- **AI 인증 분석**: Google Cloud Vision으로 인증 사진 자동 검증
- **AI 동기부여 코치**: 개인화된 격려 메시지 생성
- **게임화**: 포인트, 레벨, 배지, 스트릭 시스템

### 기술 스택

**백엔드** (85% 비중)
- Spring Boot 3.x + Java 17
- PostgreSQL (주 데이터베이스)
- Redis (캐싱 + Pub/Sub)
- Spring Security + JWT
- SSE (Server-Sent Events)
- OpenAI GPT API
- Google Cloud Vision API

**프론트엔드**
- Next.js 14 + TypeScript
- Tailwind CSS + Shadcn/ui
- Zustand (상태 관리)
- React Query (데이터 페칭)
- EventSource API (SSE 클라이언트)

**인프라**
- Docker + Docker Compose
- GitHub Actions (CI/CD)
- AWS S3 (이미지 저장)
- Render (배포)

---

## 주요 기능 (26개 섹션)

### 1-2. 기본 UI
- 홈/랜딩 페이지
- 네비게이션

### 3-5. 핵심 API
- 사용자 관리 (JWT 인증)
- 챌린지 관리 (생성, 조회, 검색, 통계)
- 인증 작성/관리 (이미지 업로드, 리사이징, S3)

### 6-7. 실시간 & AI ⭐
- **실시간 피드** (SSE + Redis Pub/Sub)
- **AI 추천 엔진** (5가지 알고리즘)

### 8-10. 소셜 기능
- 팔로우/팔로워
- 댓글 & 좋아요
- 알림 시스템 (SSE 기반)

### 11-14. 게임화
- 포인트/레벨 시스템
- 배지 시스템
- 스트릭 (연속 인증 추적)
- 랭킹 (Redis Sorted Set)

### 15-18. 분석 & 관리
- 통계/분석
- 고급 검색 (Full-Text Search)
- 개인 대시보드
- 관리자 기능

### 19-23. 백엔드 인프라
- 에러 처리 (전역 예외 처리)
- Redis 캐싱 전략 (TTL, 무효화, 히트율)
- 보안 시스템 (Rate Limiting, IP 차단, 암호화)
- API 성능 최적화 (쿼리 최적화, 인덱싱, 커넥션 풀)
- 배치 작업/스케줄링 (6개 배치 작업)

### 24-26. AI 기능 ⭐
- **AI 인증 분석** (Google Cloud Vision)
- **AI 동기부여 코치** (OpenAI GPT)
- **AI 챌린지 생성기** (OpenAI GPT)

---

## AI 기능 상세

### 1. AI 챌린지 생성기
```
입력: "매일 책 읽기"

출력:
{
  title: "매일 책 읽기 30분 챌린지",
  description: "바쁜 일상 속에서도 독서 습관을 만들어보세요...",
  tags: ["#독서", "#자기계발", "#습관"],
  category: "STUDY"
}
```

### 2. AI 인증샷 분석기
```
사진 업로드 시 자동 분석
- 운동 사진 → "운동 인증이 확인되었습니다!"
- 물 마시기 사진 → "물 마시기 인증이 확인되었습니다!"
- 신뢰도 80% 이상 시 "AI 인증" 배지 부여
```

### 3. AI 동기부여 코치
```
사용자 인증 후 자동 메시지 생성
- "5km 완주했군요! 꾸준함이 멋집니다!"
- 레벨, 스트릭 기반 개인화
- 다양한 표현으로 매번 다른 메시지
```

### 4. AI 추천 엔진 (5가지 알고리즘)
- 쿼리 기반: 참가자 수 TOP
- 콘텐츠 기반: 태그 유사도 (코사인 유사도)
- 협업 필터링: 공통 참여 챌린지 (User-Based CF)
- 개인화: 행동 로그 분석
- 트렌드: 24시간 급상승 (Redis Sorted Set)

---

## 실시간 피드 아키텍처

```
사용자 A가 인증 작성
    ↓
백엔드: POST /api/certifications
    ↓
Redis Publish (인증 이벤트)
    ↓
모든 구독자에게 SSE로 브로드캐스트
    ↓
프론트엔드: EventSource로 수신
    ↓
토스트 알림: "OO님이 방금 인증했습니다!" (3초 후 자동 사라짐)
```

---

## 주요 API 엔드포인트 (100+)

### 사용자 관리
- `POST /api/auth/register` - 회원가입
- `POST /api/auth/login` - 로그인 (JWT 반환)
- `POST /api/auth/refresh` - 토큰 갱신
- `GET /api/users/{id}` - 프로필 조회
- `PUT /api/users/{id}` - 프로필 수정

### 챌린지 관리
- `POST /api/challenges` - 챌린지 생성
- `GET /api/challenges` - 목록 조회 (페이징, 필터링)
- `GET /api/challenges/{id}` - 상세 조회
- `POST /api/challenges/{id}/join` - 참여
- `GET /api/challenges/search` - 검색 (Full-Text Search)

### 인증 관리
- `POST /api/certifications` - 인증 생성
- `POST /api/certifications/upload` - 이미지 업로드 (S3)
- `GET /api/certifications` - 목록 조회
- `GET /api/certifications/{id}` - 상세 조회

### 실시간 피드
- `GET /api/feed/stream` - SSE 스트림
- `GET /api/feed` - 피드 조회 (페이징)
- `GET /api/feed/following` - 팔로잉 피드

### AI 기능
- `POST /api/ai/challenges/generate` - AI 챌린지 생성
- `POST /api/ai/certifications/analyze` - AI 인증 분석
- `POST /api/ai/motivate` - AI 격려 메시지
- `GET /api/ai/recommendations` - AI 추천 (5가지)

### 소셜 기능
- `POST /api/users/{id}/follow` - 팔로우
- `POST /api/certifications/{id}/comments` - 댓글 작성
- `POST /api/certifications/{id}/like` - 좋아요

### 게임화
- `GET /api/users/{id}/stats` - 통계 조회
- `GET /api/rankings` - 랭킹 조회
- `GET /api/badges` - 배지 목록
- `GET /api/streaks/{userId}` - 스트릭 조회

---

## Redis 캐싱 전략

| 데이터 | TTL | 용도 |
|--------|-----|------|
| 랭킹 | 1시간 | Redis Sorted Set |
| 추천 결과 | 1시간 | 추천 성능 향상 |
| 조회수/좋아요 | 실시간 | Redis Counter |
| 검색 결과 | 10분 | 검색 성능 향상 |
| 대시보드 | 5분 | 빠른 로딩 |

---

## 배치 작업 (6개)

| 작업 | 실행 주기 | 설명 |
|------|-----------|------|
| 스트릭 검증 | 매일 자정 | 연속 인증일 수 갱신 |
| 랭킹 갱신 | 1시간마다 | Redis Sorted Set 갱신 |
| 통계 집계 | 매일 새벽 2시 | 일별 통계 데이터 생성 |
| 만료 알림 삭제 | 매주 일요일 | 30일 지난 알림 삭제 |
| 챌린지 종료 처리 | 매시간 | 종료된 챌린지 상태 변경 |
| 주간 리포트 발송 | 매주 월요일 | 이메일 발송 |

---

## 보안 시스템

### Rate Limiting (Bucket4j)
- API별 요청 제한: 분당 100회
- 사용자별 요청 제한: 분당 50회
- IP별 요청 제한: 분당 200회

### 보안 기능
- JWT 토큰 검증 필터
- IP 차단 시스템 (Redis 블랙리스트)
- XSS 방지 (HTML 이스케이프)
- SQL Injection 방지 (Prepared Statement)
- CSRF 토큰 검증
- 비밀번호 암호화 (BCrypt)
- 민감 정보 암호화 (AES-256)

---

## 프로젝트 통계

| 항목 | 수치 |
|------|------|
| **총 기능 섹션** | 26개 |
| **총 API 엔드포인트** | 100+ |
| **백엔드 기능 비중** | 85% |
| **프론트엔드 페이지** | 25+ |
| **전체 체크리스트** | 210+ |
| **배치 작업** | 6개 |
| **외부 API 연동** | 4개 (OpenAI GPT, Google Cloud Vision, Google OAuth, Kakao OAuth) |

---

## 개발 환경 설정

### 필수 요구사항
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+

### 백엔드 실행
```bash
# Docker로 PostgreSQL, Redis 실행
docker-compose up -d

# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

### 프론트엔드 실행
```bash
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

### 환경 변수 설정
```env
# 데이터베이스
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/planit
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# AWS S3
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_S3_BUCKET=your-bucket-name

# OpenAI
OPENAI_API_KEY=your-openai-api-key

# Google Cloud Vision
GOOGLE_APPLICATION_CREDENTIALS=/path/to/credentials.json
```

---

## API 문서

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 팀 협업

- **Discord**: GitHub 웹훅 연동 (이슈, PR, 커밋 알림)
- **GitHub Projects**: 칸반 보드로 작업 관리
- **코드 리뷰**: 모든 PR 필수 리뷰
- **브랜치 전략**: Git Flow

---

## 라이선스

MIT License

---

**백엔드 중심 AI 기반 소셜 챌린지 트래커** 🚀
