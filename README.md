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

## 개발 일정 (12주)

### Week 1-2: 프로젝트 기반 구축 및 인프라

#### 백엔드 설정
- [ ] Spring Boot 프로젝트 초기 설정
- [ ] PostgreSQL, Redis Docker 환경 구성
- [ ] 데이터베이스 스키마 설계 (20+ 엔티티)
- [ ] JPA 엔티티 구현
- [ ] Swagger/OpenAPI 문서 설정

#### 인프라 구축
- [ ] GitHub Actions CI/CD 파이프라인
- [ ] Docker 이미지 자동 빌드
- [ ] Render 자동 배포 설정
- [ ] 헬스체크 및 롤백 자동화

#### 실시간 통신 기반
- [ ] SSE 엔드포인트 설정
- [ ] Redis Pub/Sub 구성
- [ ] 실시간 메시지 브로드캐스트 테스트

**완료 목표**: 개발 환경 완성 + CI/CD 파이프라인 + 실시간 인프라

---

### Week 3: 사용자 관리 & 인증 시스템

#### API 개발
- [ ] User 엔티티 구현
- [ ] 회원가입 API (POST /api/auth/register)
- [ ] 로그인 API (POST /api/auth/login)
- [ ] JWT 토큰 발급/갱신 API
- [ ] 프로필 조회/수정 API
- [ ] 비밀번호 변경 API
- [ ] 회원 탈퇴 API
- [ ] 프로필 이미지 업로드 (S3)

#### 보안
- [ ] Spring Security + JWT 필터 구현
- [ ] 비밀번호 암호화 (BCrypt)
- [ ] Rate Limiting 설정 (Bucket4j)

#### 프론트엔드
- [ ] 회원가입/로그인 페이지
- [ ] 프로필 페이지
- [ ] JWT 토큰 관리 훅

**완료 목표**: 완전한 사용자 인증 시스템

---

### Week 4: 챌린지 관리 시스템

#### API 개발
- [ ] Challenge 엔티티 구현
- [ ] UserChallenge 엔티티 (참여 관계)
- [ ] 챌린지 생성 API (POST /api/challenges)
- [ ] 챌린지 목록/상세 조회 API
- [ ] 챌린지 수정/삭제 API (소프트 삭제)
- [ ] 챌린지 참여/탈퇴 API
- [ ] 챌린지 검색 API (Full-Text Search)
- [ ] 조회수 API (Redis 카운터)
- [ ] 챌린지 통계 API

#### 프론트엔드
- [ ] 챌린지 목록 페이지
- [ ] 챌린지 상세 페이지
- [ ] 챌린지 생성/수정 페이지

**완료 목표**: 기본 챌린지 관리 완성

---

### Week 5: 인증 작성 & 이미지 관리

#### API 개발
- [ ] Certification 엔티티 구현
- [ ] 인증 생성 API (POST /api/certifications)
- [ ] 이미지 업로드 API (S3 멀티파트)
- [ ] 이미지 검증 (파일 타입, 크기, MIME)
- [ ] 이미지 리사이징 (썸네일 생성)
- [ ] 인증 조회 API (목록/상세, 페이징)
- [ ] 인증 수정/삭제 API (24시간 제한)
- [ ] 사용자별/챌린지별 인증 목록 API

#### 프론트엔드
- [ ] 인증 작성 페이지
- [ ] 이미지 업로드 UI
- [ ] 인증 목록/상세 페이지

**완료 목표**: 인증 작성 및 이미지 관리 시스템

---

### Week 6: 실시간 피드 ⭐ 핵심 기능

#### API 개발
- [ ] SSE 엔드포인트 (GET /api/feed/stream)
- [ ] Redis Pub/Sub 기반 브로드캐스트
- [ ] 인증 생성 시 자동 알림 발행
- [ ] SSE 연결 관리 (사용자별 세션)
- [ ] 하트비트 (30초 keep-alive)
- [ ] 피드 조회 API (페이징, 정렬)
- [ ] 팔로잉 피드 API

#### 프론트엔드
- [ ] EventSource API 연결
- [ ] 실시간 토스트 알림 ("OO님이 방금 인증했습니다!")
- [ ] 피드 페이지 (무한 스크롤)
- [ ] 연결 상태 표시

**완료 목표**: 실시간 인증 공유 시스템

---

### Week 7: AI 추천 엔진 ⭐

#### API 개발
- [ ] UserBehavior 엔티티 (행동 로그)
- [ ] 추천 API (5가지 알고리즘)
  - 쿼리 기반 (참가자 수 TOP)
  - 콘텐츠 기반 (태그 유사도)
  - 협업 필터링 (User-Based CF)
  - 개인화 (행동 로그 분석)
  - 트렌드 (24시간 급상승)
- [ ] 추천 이유 생성 API
- [ ] 사용자 행동 로그 수집
- [ ] 추천 결과 캐싱 (Redis, TTL 1시간)
- [ ] 추천 성능 측정 (CTR, 참여율)

#### 프론트엔드
- [ ] AI 추천 페이지
- [ ] 추천 이유 카드
- [ ] 추천 새로고침 기능

**완료 목표**: 지능형 추천 시스템

---

### Week 8: 소셜 기능

#### API 개발
- [ ] UserFollowing 엔티티
- [ ] 팔로우/언팔로우 API
- [ ] 팔로워/팔로잉 목록 API
- [ ] 팔로우 카운트 캐싱 (Redis)
- [ ] Comment 엔티티
- [ ] 댓글 생성/조회/삭제 API
- [ ] 댓글 수 카운트 (Redis)
- [ ] 좋아요 생성/취소 API (토글)
- [ ] 좋아요 카운트 API (Redis)
- [ ] 좋아요 목록 API

#### 프론트엔드
- [ ] 팔로우 버튼
- [ ] 팔로워/팔로잉 목록 모달
- [ ] 댓글 시스템
- [ ] 좋아요 버튼

**완료 목표**: 소셜 상호작용 시스템

---

### Week 9: 게임화 시스템

#### API 개발
- [ ] UserLevel 엔티티
- [ ] Badge, UserBadge 엔티티
- [ ] Streak 엔티티
- [ ] 포인트 부여 로직 (인증 +10, 댓글 +2, 좋아요 +1)
- [ ] 레벨 계산 로직
- [ ] 배지 획득 조건 및 부여 로직
- [ ] 스트릭 계산 로직
- [ ] 랭킹 API (Redis Sorted Set)
- [ ] 통계 API (개인/챌린지)

#### 프론트엔드
- [ ] 레벨 표시
- [ ] 배지 모음 페이지
- [ ] 스트릭 캘린더
- [ ] 랭킹 리더보드

**완료 목표**: 게임화를 통한 참여 극대화

---

### Week 10: AI 기능 강화 ⭐

#### API 개발
- [ ] AI 챌린지 생성 API (OpenAI GPT)
- [ ] 키워드 기반 챌린지 자동 생성
- [ ] GPT 프롬프트 엔지니어링
- [ ] 생성 미리보기 API
- [ ] AI 인증 분석 API (Google Cloud Vision)
- [ ] 라벨/텍스트/안전 탐지
- [ ] AI 신뢰도 점수 계산
- [ ] AI 배지 자동 부여
- [ ] AI 격려 메시지 생성 API
- [ ] 개인화 메시지 생성

#### 프론트엔드
- [ ] AI 챌린지 생성 페이지
- [ ] AI 분석 결과 표시
- [ ] AI 격려 메시지 팝업

**완료 목표**: 차별화된 AI 기능 완성

---

### Week 11: 알림 & 소셜 로그인

#### API 개발
- [ ] Notification 엔티티
- [ ] 알림 생성 API (팔로우, 댓글, 좋아요, 배지, 레벨업)
- [ ] 알림 조회 API (목록, 페이징)
- [ ] 미읽음 알림 카운트 API (Redis)
- [ ] 알림 읽음 처리 API
- [ ] 알림 설정 API
- [ ] SSE 기반 실시간 알림 푸시
- [ ] OAuth 2.0 연동 (Google, Kakao)
- [ ] 소셜 로그인 콜백 API
- [ ] 소셜 계정 연동/해제 API

#### 프론트엔드
- [ ] 알림 벨 아이콘
- [ ] 알림 드롭다운
- [ ] 알림 목록 페이지
- [ ] 소셜 로그인 버튼

**완료 목표**: 알림 시스템 + 소셜 로그인

---

### Week 12: 성능 최적화 & 런칭

#### 백엔드 최적화
- [ ] 데이터베이스 인덱싱 (복합 인덱스, 부분 인덱스)
- [ ] 쿼리 최적화 (N+1 문제 해결, Fetch Join)
- [ ] Redis 캐싱 전략 최적화
- [ ] 페이징 최적화 (Cursor 기반)
- [ ] API 응답 시간 개선 (목표: 500ms 이하)
- [ ] 커넥션 풀 최적화 (HikariCP)
- [ ] 배치 작업 설정 (6개)
  - 스트릭 검증 (매일 자정)
  - 랭킹 갱신 (1시간마다)
  - 통계 집계 (매일 새벽 2시)
  - 만료 알림 삭제 (매주 일요일)
  - 챌린지 종료 처리 (매시간)
  - 주간 리포트 발송 (매주 월요일)

#### 보안 강화
- [ ] IP 차단 시스템 (Redis 블랙리스트)
- [ ] CORS 설정
- [ ] XSS 방지
- [ ] SQL Injection 방지
- [ ] CSRF 토큰 검증
- [ ] 민감 정보 암호화 (AES-256)

#### 테스트
- [ ] 단위 테스트 (Service, Util)
- [ ] 통합 테스트 (Controller, DB, Redis)
- [ ] E2E 테스트 (전체 플로우)

#### 프론트엔드 최적화
- [ ] 코드 스플리팅
- [ ] 이미지 lazy loading
- [ ] API 호출 최적화
- [ ] 불필요한 리렌더링 방지

#### 런칭 준비
- [ ] 사용자 온보딩 플로우
- [ ] 관리자 대시보드
- [ ] 에러 페이지 (404, 500, 403)
- [ ] 로깅/모니터링 (Sentry, Prometheus)
- [ ] 정식 서비스 런칭

**완료 목표**: 완전한 소셜 챌린지 트래커 서비스

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
- `GET /api/challenges` - 목록 조회
- `GET /api/challenges/{id}` - 상세 조회
- `POST /api/challenges/{id}/join` - 참여
- `GET /api/challenges/search` - 검색

### 인증 관리
- `POST /api/certifications` - 인증 생성
- `POST /api/certifications/upload` - 이미지 업로드
- `GET /api/certifications` - 목록 조회

### 실시간 피드
- `GET /api/feed/stream` - SSE 스트림
- `GET /api/feed` - 피드 조회

### AI 기능
- `POST /api/ai/challenges/generate` - AI 챌린지 생성
- `POST /api/ai/certifications/analyze` - AI 인증 분석
- `POST /api/ai/motivate` - AI 격려 메시지
- `GET /api/ai/recommendations` - AI 추천

### 소셜 기능
- `POST /api/users/{id}/follow` - 팔로우
- `POST /api/certifications/{id}/comments` - 댓글
- `POST /api/certifications/{id}/like` - 좋아요

### 게임화
- `GET /api/users/{id}/stats` - 통계
- `GET /api/rankings` - 랭킹
- `GET /api/badges` - 배지
- `GET /api/streaks/{userId}` - 스트릭

---

## 핵심 데이터 모델

```
User (사용자)
├── UserChallenge (챌린지 참여)
├── Certification (인증)
├── Comment (댓글)
├── UserFollowing (팔로우)
├── UserLevel (레벨/경험치)
├── UserBadge (배지)
├── Notification (알림)
└── UserBehavior (행동 로그)

Challenge (챌린지)
├── Certification (인증들)
└── Comment (댓글들)

Badge (배지 마스터)
└── UserBadge (사용자 획득 배지)

Streak (연속 기록)
```

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

## 프로젝트 통계

| 항목 | 수치 |
|------|------|
| **총 기능 섹션** | 26개 |
| **총 API 엔드포인트** | 100+ |
| **백엔드 기능 비중** | 85% |
| **프론트엔드 페이지** | 25+ |
| **전체 체크리스트** | 210+ |
| **개발 기간** | 12주 |
| **배치 작업** | 6개 |
| **외부 API 연동** | 4개 (OpenAI GPT, Google Cloud Vision, Google OAuth, Kakao OAuth) |

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

**12주로 완성하는 백엔드 중심 AI 기반 소셜 챌린지 트래커** 🚀
