# AI 기반 소셜 챌린지 트래커

> 작은 성취를 실시간으로 공유하고, AI가 다음 도전을 추천해주는 동기부여 플랫폼 (웹 기반)

## 📖 문서 안내

이 문서는 **상세 기술 문서**입니다. 설치 가이드, API 명세, 아키텍처 상세 정보를 포함합니다.

**📋 포트폴리오용 요약**: [README.md](README.md) - 프로젝트 최종 보고서 및 주요 성과

---

## 프로젝트 개요

**챌린지 트래커**는 사용자들이 개인의 작은 성취를 실시간으로 공유하며 함께 성장하는 소셜 플랫폼입니다.

### 핵심 기능
- **실시간 피드**: SSE 기반 실시간 인증 알림 ("OO님이 방금 인증했습니다!")
- **챌린지 시스템**: 챌린지 생성, 참여, 관리 기능
- **인증 시스템**: 챌린지별 인증 생성 및 관리
- **팔로우 시스템**: 사용자 팔로우/언팔로우 및 팔로잉 피드
- **알림 시스템**: DB 기반 알림 및 SSE 실시간 푸시
- **프로필 시스템**: 사용자 프로필 조회 및 수정
- **AI 추천 엔진**: Gemini AI 기반 챌린지 추천 (기존 챌린지 참여 시)
- **AI 챌린지 생성기**: 키워드/자연어 기반 Gemini AI 챌린지 자동 생성
- **AI 인증 분석**: Gemini AI로 인증 사진 자동 검증 및 적합성 판단
- **게임화**: 포인트 시스템, 배지 시스템, 스트릭 시스템
- **랭킹**: Redis Sorted Set 기반 실시간 SSE 랭킹

### 향후 계획
- **AI 동기부여 코치**: Gemini AI 기반 개인화 격려 메시지 생성 (계획 중)

### 기술 스택

**백엔드** (85% 비중)
- Spring Boot 3.2 + Kotlin 1.9.20
- PostgreSQL (주 데이터베이스)
- Redis (캐싱 + Pub/Sub)
- Spring Security + JWT
- SSE (Server-Sent Events)
- Google GenAI SDK (Gemini AI)

**프론트엔드**
- Next.js 16 + TypeScript
- Tailwind CSS 4 + Shadcn/ui
- Zustand (상태 관리)
- React Query (데이터 페칭)
- EventSource API (SSE 클라이언트)

**인프라**
- Docker + Docker Compose
- GitHub Actions (CI/CD)
- 로컬 파일 스토리지 (이미지 저장, 날짜별 디렉토리, 리사이징/압축)
- Render (배포)

---

## 25개 기능 섹션 상세 목록

### 1. 홈/랜딩 페이지 (Home/Landing Page) - 완료
- [x] 메인 랜딩 페이지 (비로그인 사용자용)
- [x] 서비스 소개 섹션 (6가지 기능 소개: AI 추천, 실시간 피드, AI 코치, 게임화, 소셜, AI 검증)
- [x] 통계 섹션 (주요 기능 통계)
- [x] CTA 버튼 (무료로 시작하기, 로그인)
- [x] 로그인 상태에서는 대시보드로 리다이렉트
- [x] Hero 섹션 (그라데이션 배경, 로고, 설명)
- [x] 개발자 도구 링크 (API 테스트, Swagger UI)

### 2. 네비게이션 (Navigation) - 완료
- [x] 상단 네비게이션 (대시보드, 챌린지, 피드, 랭킹)
- [x] 추가 메뉴 (통계, 배지, 스트릭, 사용자 찾기, 설정)
- [x] 알림 드롭다운 (실시간 알림 표시)
- [x] 프로필 드롭다운 (계정 관리)
- [x] 모바일 반응형 메뉴
- [x] 로고 및 브랜딩
- [x] 하단 푸터 (서비스 소개, 약관, 개인정보처리방침, 문의, 소셜 링크)

### 3. 사용자 관리 (User Management)
- [x] 회원가입 API (이메일, 비밀번호, 닉네임)
- [x] 이메일 중복 검증 API
- [x] JWT 토큰 발급/갱신 API
- [x] 로그인/로그아웃 API
- [x] 프로필 조회/수정 API
- [x] 비밀번호 변경 API (기존 비밀번호 검증)
- [x] 회원 탈퇴 API (연관 데이터 처리)
- [ ] 프로필 이미지 업로드 API (S3/CloudFront)

### 4. 챌린지 관리 (Challenge Management)
- [x] 챌린지 생성 API (제목, 설명, 카테고리, 기간, 난이도)
- [x] 챌린지 조회 API (목록/상세, 페이징, 필터링)
- [x] 챌린지 수정 API (작성자 권한 검증)
- [x] 챌린지 삭제 API (소프트 삭제)
- [x] 챌린지 참여/탈퇴 API
- [x] 챌린지 검색 API (키워드, Full-Text Search)
- [x] 조회수 증가 API (Redis 카운터)
- [x] 참여자 목록 API (페이징)
- [x] 챌린지 통계 API (참여자 수, 인증 수, 완료율)

### 5. 인증 작성/관리 (Certification Management)
- [x] 인증 생성 API (챌린지 ID, 제목, 내용)
- [x] 이미지 업로드 API (로컬 파일 저장, 날짜별 디렉토리)
- [x] 이미지 검증 (Gemini AI 기반 적합성 분석)
- [x] 이미지 리사이징 (Thumbnailator: 최대 1600px, 품질 80%)
- [x] 인증 조회 API (목록/상세, 페이징)
- [x] 인증 수정 API (작성자 권한)
- [x] 인증 삭제 API (작성자 권한, 소프트 삭제)
- [x] 사용자별 인증 목록 API
- [x] 챌린지별 인증 목록 API
- [x] 기간별 인증 목록 API (date-range)
- [x] AI 인증 재분석 API
- [x] 인증 사진 삭제 API
- [ ] 인증 통계 API (좋아요 수, 댓글 수)

### 6. 실시간 피드 - 핵심 기능 (Real-time Feed)
- [x] SSE 엔드포인트 (/api/feed/stream)
- [x] 실시간 알림 브로드캐스트 (SSE 기반)
- [x] 인증 생성 시 자동 알림 발행
- [x] SSE 연결 관리 (사용자별 세션 관리)
- [x] 하트비트 (30초마다 keep-alive)
- [x] 피드 조회 API (참여 중인 챌린지 기반, 페이징)
- [x] 피드 정렬 API (최신순, 좋아요순, 댓글순, 인기순)
- [x] 팔로잉 피드 API (팔로우한 사용자 인증)

### 7. AI 추천 엔진 - 핵심 기능 (AI Recommendation Engine)
- [x] 추천 API (Gemini AI 기반)
- [x] 기존 챌린지 추천 (참여 가능한 챌린지)
- [x] 자연어 쿼리 기반 기존 챌린지 추천
- [x] 추천 이유 생성 (reason 필드)
- [ ] 사용자 행동 로그 수집 (조회, 참여, 완료)
- [ ] 추천 결과 캐싱 (Redis, TTL 1시간)
- [ ] 추천 성능 측정 (CTR, 참여율)

### 8. 팔로우/팔로워 (Follow System) - 완료
- [x] 팔로우/언팔로우 API
- [x] 팔로워 목록 API (페이징)
- [x] 팔로잉 목록 API (페이징)
- [x] 팔로우 카운트 캐싱 (Redis)
- [x] 팔로우 관계 저장 (DB 인덱싱)
- [x] 팔로잉 피드 API (팔로우한 사용자 인증 조회)
- [x] 중복 팔로우 방지 (Unique 제약조건)

### 9. 댓글 & 좋아요 (Comments & Likes)
- [x] 댓글 생성 API
- [x] 댓글 조회 API (인증별)
- [x] 댓글 삭제 API (작성자 권한 검증)
- [x] 댓글 수 카운트 (응답에 포함)
- [x] 좋아요 생성/취소 API (토글)
- [x] 좋아요 카운트 (응답에 포함)
- [ ] 좋아요 목록 API (사용자 목록)
- [x] 중복 좋아요 방지 (Unique 제약조건)
- [x] 좋아요 알림 발송 (비동기)

### 10. 알림 (Notifications) - 완료
- [x] 알림 생성 API (팔로우, 인증 활동)
- [x] 알림 조회 API (목록, 페이징)
- [x] 미읽음 알림 카운트 API (Redis 캐싱)
- [x] 알림 읽음 처리 API (개별/일괄)
- [x] 알림 삭제 API (개별/읽은 알림 일괄 삭제)
- [ ] 알림 설정 API (타입별 수신 여부)
- [x] SSE 기반 실시간 알림 푸시

### 11. 포인트 시스템 (Points System) - 완료
- [x] 포인트(Point) 규칙 정의 (인증 +10, 댓글 +2, 좋아요 +1, 배지 +5) - 상점용 재화
- [x] 포인트 적립 API (행동별 포인트 부여)
- [x] 포인트 히스토리 API (페이징, 필터링)
- [x] 포인트 차감 API (향후 상점 기능 대비)
- [x] 통계 API (일별 포인트, 날짜 범위 지정)
- [x] 포인트 통계 UI (일별 획득, 누적 추이 차트)

### 12. 배지 시스템 (Badge System) - 완료
- [x] 배지 정의 API (배지 목록, 조건, 등급)
- [x] 배지 획득 감지 로직 (조건 체크)
- [x] 배지 자동 부여 API (비동기 처리)
- [x] 배지 조회 API (사용자별 획득/미획득)
- [x] 배지 종류 (첫 인증, 누적 인증, 포인트, 팔로워 등)
- [x] 배지 등급 시스템 (브론즈, 실버, 골드, 플래티넘)
- [x] 배지 획득 알림 발송 (SSE)
- [x] BadgeChecker 인터페이스 및 팩토리 패턴
- [x] 연속 인증 스트릭 체크 로직 (CertificationStreakChecker 구현 완료)

### 13. 스트릭 (Streak System) - 완료
- [x] 스트릭 계산 로직 (연속 인증일 수)
- [x] 스트릭 조회 API (현재, 최고, 기록)
- [x] 스트릭 캘린더 API (최근 30일 데이터)
- [x] 스트릭 검증 배치 작업 (매일 자정 실행)
- [x] 스트릭 끊김 경고 알림 (오늘 미인증 시)
- [x] 스트릭 통계 API (일별/주별/월별)
- [x] 스트릭 리더보드 API (챌린지별)
- [x] GitHub 스타일 활동 잔디 UI
- [x] 일별 활동 기록 추적 (DailyActivity 엔티티)

### 14. 랭킹 (Ranking/Leaderboard)
- [x] 랭킹 계산 API (Redis Sorted Set 활용)
- [x] 랭킹 조회 API (주간/월간/전체, 페이징)
- [x] 랭킹 동기화 배치 작업 (전체: 매일 4시, 주간/월간: 아카이브)
- [x] 본인 순위 조회 API (특정 사용자 순위)
- [x] 순위 변동 추적 (SSE를 통한 실시간 추적)
- [x] SSE 기반 실시간 순위 업데이트 (Top 10 변경 시 푸시)

### 15. 통계/분석 (Statistics & Analytics)
- [x] 개인 통계 API (총 인증 수, 포인트, 배지, 스트릭)
- [x] 챌린지 통계 API (참여자 수, 인증 수, 완료율, 조회수)
- [x] 포인트 통계 차트 API (일별 획득, 누적 추이)
- [ ] 통계 데이터 캐싱 (Redis, TTL 1시간)
- [ ] CSV 내보내기 API (개인 데이터 다운로드)

### 16. 고급 검색 (Advanced Search)
- [x] 챌린지 검색 API (키워드, Full-Text Search)
- [x] 필터링 API (카테고리, 난이도, 기간, 상태)
- [x] 정렬 API (LATEST, NAME, DIFFICULTY, POPULAR)
- [ ] 검색 인덱싱 (PostgreSQL tsvector, GIN 인덱스)
- [ ] 검색 결과 캐싱 (Redis, TTL 10분)
- [ ] 검색어 자동완성 API
- [ ] 인기 검색어 API (Redis Sorted Set)

### 17. 개인 대시보드 (Personal Dashboard) - 완료
- [x] 대시보드 데이터 API (챌린지 수, 인증 수, 팔로워 수, 팔로잉 수)
- [x] 참여 중인 챌린지 API (진행 중인 챌린지 목록)
- [x] 최근 활동 피드 API (최근 3개 피드)
- [x] 대시보드 추천 챌린지 API (Gemini AI 기반)
- [x] 대시보드 프론트엔드 페이지 (통계 카드, 최근 피드)

**참고**: 포인트, 배지, 스트릭은 각각의 전용 API 제공 (GET /api/points/me, /api/badges/my, /api/streaks)
**캐싱**: 팔로워/팔로잉 수는 이미 Redis 캐싱, 나머지는 단순 COUNT 쿼리로 충분히 빠름

### 18. 에러 처리 (Error Handling) - 완료
- [x] 전역 예외 처리 (@RestControllerAdvice)
- [x] 커스텀 예외 클래스 정의
- [x] HTTP 상태 코드 매핑 (4xx, 5xx)
- [x] 에러 응답 표준화 (code, message, timestamp)
- [x] 에러 로깅 (Logback)

### 19. Redis 캐싱 전략 (Redis Caching Strategy)
- [x] Redis 연결 설정 (Lettuce)
- [x] 캐시 키 전략 정의 (네이밍 규칙)
- [x] TTL 정책 설정 (데이터별)
- [ ] 캐시 프리로딩 (서버 시작 시)
- [ ] 캐시 무효화 전략 (Write-Through, Write-Behind)
- [ ] 캐시 히트율 모니터링

### 20. 보안 시스템 (Security System)
- [x] JWT 토큰 검증 필터
- [ ] Rate Limiting (Bucket4j)
  - API별 요청 제한 (분당 100회)
  - 사용자별 요청 제한 (분당 50회)
  - IP별 요청 제한 (분당 200회)
- [ ] IP 차단 시스템 (Redis 블랙리스트)
- [x] CORS 설정 (허용 오리진, 메서드, 헤더)
- [ ] XSS 방지 (HTML 이스케이프)
- [ ] SQL Injection 방지 (Prepared Statement)
- [ ] CSRF 토큰 검증
- [x] 권한 관리 (Spring Security, Role 기반)
- [x] 비밀번호 암호화 (BCrypt)
- [ ] 민감 정보 암호화 (AES-256)

### 21. API 성능 최적화 (API Performance Optimization)
- [ ] 쿼리 최적화 (N+1 문제 해결, Fetch Join)
- [ ] 데이터베이스 인덱싱 (복합 인덱스, 부분 인덱스)
- [ ] 페이징 최적화 (Cursor 기반)
- [ ] 응답 압축 (Gzip)
- [ ] API 응답 시간 모니터링
- [ ] Slow Query 로그 분석
- [ ] 커넥션 풀 최적화 (HikariCP)
- [ ] 트랜잭션 범위 최소화

### 22. 스케줄링 (Scheduling) - 완료
- [x] 스케줄러 설정 (@Scheduled)
- [x] 스케줄러 목록 (5개):
  - [x] 스트릭 검증 (매일 자정) - StreakScheduler
  - [x] 스트릭 리마인더 (매일 저녁 8시) - StreakScheduler
  - [x] 랭킹 동기화 (매일 새벽 4시, 5시) - RankingScheduler
  - [x] 랭킹 아카이브 (주간: 일요일 23:55, 월간: 월말 23:55) - RankingScheduler
  - [x] 파일 정리 (매일 새벽 3시, 고아 파일 삭제) - FileCleanupTask
  - [x] 챌린지 종료 리마인더 (매일 9시: 1주일 전, 3일 전, 당일) - ChallengeReminderScheduler
  - [x] 조회수 동기화 (1시간마다) - ViewCountScheduler

### 23. AI 인증 분석 (AI Certification Analysis) - 완료
- [x] Gemini AI 기반 이미지 분석
- [x] 인증 사진 업로드 시 자동 분석
- [x] 주제 적합성 판단 (isSuitable 필드)
- [x] 분석 결과 저장 (analysisResult 필드)
- [x] AI 인증 재분석 API

### 24. AI 동기부여 코치 (AI Motivational Coach)
- [ ] AI 격려 메시지 생성 API (Gemini AI 연동)
- [ ] 사용자 컨텍스트 수집 (레벨, 스트릭, 최근 활동)
- [ ] 개인화 메시지 생성 (사용자 데이터 기반)
- [ ] 메시지 템플릿 엔진 (다양한 표현)
- [ ] 메시지 저장 및 이력 관리
- [ ] 메시지 조회 API (최근 10개)
- [ ] 알림 시스템 연동 (자동 발송)

### 25. AI 챌린지 생성기 (AI Challenge Generator) - 부분 완료
- [x] AI 챌린지 생성 API (Gemini AI 연동)
- [x] 키워드 기반 챌린지 자동 생성 (제목, 설명, 카테고리, 난이도)
- [x] 자연어 쿼리 기반 챌린지 생성
- [x] 프롬프트 엔지니어링 (구조화된 출력)
- [x] 생성 결과 검증 (TypeReference 기반 파싱)
- [x] 생성 미리보기 (폼 자동 채우기)
- [ ] 생성 이력 저장 (사용자별, 통계)
- [ ] API 호출 제한 (Rate Limiting)
- [ ] 생성 실패 시 재시도 로직

---

## 주요 API 엔드포인트 (50+)

### 인증 관리 (AuthController)
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인 (JWT 반환)
- `POST /api/auth/logout` - 로그아웃

### 사용자 관리 (UserController)
- `GET /api/users/me` - 현재 사용자 정보 조회
- `GET /api/users/{loginId}/profile` - 사용자 프로필 조회
- `PUT /api/users/me/update-profile` - 프로필 수정
- `DELETE /api/users/me` - 회원 탈퇴
- `GET /api/users` - 사용자 목록 조회 (페이지)

### 챌린지 관리 (ChallengeController)
- `POST /api/challenge` - 챌린지 생성
- `GET /api/challenge` - 목록 조회 (필터링, 페이징)
- `GET /api/challenge/{challengeId}` - 상세 조회
- `GET /api/challenge/search` - 검색
- `PUT /api/challenge/{challengeId}` - 수정
- `DELETE /api/challenge/{challengeId}` - 삭제
- `POST /api/challenge/{challengeId}/join` - 참여
- `POST /api/challenge/{challengeId}/withdraw` - 탈퇴
- `POST /api/challenge/{challengeId}/view` - 조회수 증가
- `GET /api/challenge/{challengeId}/participants` - 참여자 목록
- `GET /api/challenge/{challengeId}/statistics` - 통계
- `GET /api/challenge/my` - 내 챌린지 목록
- `GET /api/challenge/recommend` - AI 기존 챌린지 추천
- `GET /api/challenge/recommend/query` - 자연어 기반 기존 챌린지 추천
- `GET /api/challenge/recommend-existing` - AI 기존 챌린지 추천 (참여용)
- `GET /api/challenge/recommend-existing/query` - 자연어 기반 기존 챌린지 추천 (참여용)

### 인증 관리 (CertificationController)
- `POST /api/certifications` - 인증 생성
- `GET /api/certifications` - 목록 조회
- `GET /api/certifications/{certId}` - 상세 조회
- `PUT /api/certifications/{certId}` - 수정
- `DELETE /api/certifications/{certId}` - 삭제
- `POST /api/certifications/{certId}/photo` - 인증 사진 업로드
- `POST /api/certifications/{certId}/analyze` - AI 인증 재분석
- `DELETE /api/certifications/{certId}/photo` - 인증 사진 삭제

### 댓글 & 좋아요 (LikeCommentController)
- `POST /api/certifications/{certId}/likes` - 좋아요 토글
- `POST /api/certifications/{certId}/comments` - 댓글 생성
- `GET /api/certifications/{certId}/comments` - 댓글 목록
- `DELETE /api/certifications/{certId}/comments/{commentId}` - 댓글 삭제

### 팔로우 시스템 (FollowController)
- `POST /api/follows/{followingLoginId}` - 팔로우
- `DELETE /api/follows/{followingLoginId}` - 언팔로우
- `GET /api/follows/{userLoginId}/followers` - 팔로워 목록 (페이징)
- `GET /api/follows/{userLoginId}/followings` - 팔로잉 목록 (페이징)
- `GET /api/follows/{userLoginId}/follower-count` - 팔로워 수
- `GET /api/follows/{userLoginId}/following-count` - 팔로잉 수

### 실시간 피드 (FeedController)
- `GET /api/feed` - 피드 조회 (페이징)
- `GET /api/feed/stream` - SSE 스트림

### 알림 (NotificationController)
- `GET /api/subscribe` - SSE 실시간 알림 구독
- `GET /api/notifications` - 알림 목록 조회 (페이징, 필터링)
- `GET /api/notifications/unread-count` - 미읽음 알림 수
- `PATCH /api/notifications/{notificationId}/read` - 알림 읽음 표시
- `PATCH /api/notifications/read-all` - 모든 알림 읽음 표시
- `DELETE /api/notifications/{notificationId}` - 알림 삭제
- `DELETE /api/notifications/read` - 읽은 알림 일괄 삭제

### 포인트 (PointController)
- `GET /api/points/me` - 내 포인트 요약 조회
- `GET /api/points/me/history` - 내 포인트 히스토리 (페이징)
- `POST /api/points/me/deduct` - 포인트 차감 (상점 구매용)
- `GET /api/points/me/statistics` - 포인트 통계 (일별, 날짜 범위)

### 배지 (BadgeController)
- `POST /api/badges/check-all` - 모든 배지 조건 체크
- `GET /api/badges` - 전체 배지 목록 조회 (획득 여부 포함)
- `GET /api/badges/my` - 내가 획득한 배지 목록
- `GET /api/badges/user/{loginId}` - 특정 사용자의 배지 목록

### 랭킹 (RankingController, RankingSseController)
- `GET /api/rankings` - 랭킹 조회 (통합, type 파라미터)
- `GET /api/rankings/weekly` - 주간 랭킹
- `GET /api/rankings/monthly` - 월간 랭킹
- `GET /api/rankings/alltime` - 전체 랭킹
- `GET /api/rankings/me` - 내 랭킹 조회
- `GET /api/rankings/stream` - 실시간 랭킹 SSE 스트림
- `GET /api/rankings/stream/status` - SSE 연결 상태

### 스트릭 (StreakController)
- `GET /api/streaks/{challengeId}` - 특정 챌린지 스트릭 조회
- `GET /api/streaks` - 사용자의 모든 스트릭 조회
- `POST /api/streaks/{challengeId}/record` - 인증 기록
- `GET /api/streaks/calendar` - 잔디 캘린더 데이터 (연도별)
- `GET /api/streaks/statistics` - 스트릭 통계 (일별/주별/월별)
- `GET /api/streaks/{challengeId}/leaderboard` - 스트릭 리더보드

---

## 구현된 프론트엔드 페이지 (18+)

- **홈페이지** `/` - 로그인 상태에 따라 대시보드 또는 랜딩 페이지
- **인증** `/login`, `/signup` - 로그인/회원가입
- **대시보드** `/dashboard` - 사용자 통계, 참여 중인 챌린지
- **챌린지**
  - `/challenge` - 챌린지 목록 (필터링, 페이지, AI 추천)
  - `/challenge/[id]` - 챌린지 상세보기
  - `/challenge/[id]/edit` - 챌린지 편집
  - `/challenge/create` - 챌린지 생성 (AI 챌린지 생성기 연동)
  - `/challenge/my` - 내 챌린지 목록
- **인증**
  - `/certification/[id]` - 인증 상세보기
  - `/certification/create` - 인증 생성
  - `/certification/my` - 내 인증 목록
- **피드** `/feed` - 팔로우 사용자의 실시간 인증 피드 (무한 스크롤, SSE)
- **랭킹** `/ranking` - 주간/월간/전체 랭킹 (실시간 SSE 업데이트, 포디움 UI)
- **프로필**
  - `/profile` - 내 프로필 (인증, 팔로워/팔로잉, 배지, 스트릭)
  - `/profile/[loginId]` - 다른 사용자 프로필 (팔로우 가능)
- **사용자** `/users` - 사용자 목록 (검색, 페이지, 팔로우 가능)
- **통계** `/stats` - 포인트 통계 (일별 차트, 누적 추이)
- **설정** `/settings` - 계정 설정

---

## 핵심 데이터 모델

```
User (사용자)
├── UserChallenge (챌린지 참여)
├── Certification (인증)
├── Comment (댓글)
├── UserFollowing (팔로우)
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

## 스케줄러 (7개)

| 작업 | 실행 주기 | 설명 |
|------|-----------|------|
| 스트릭 검증 | 매일 자정 | 연속 인증일 수 갱신 |
| 스트릭 리마인더 | 매일 저녁 8시 | 오늘 미인증 시 알림 |
| 랭킹 동기화 | 매일 새벽 4시, 5시 | Redis → DB 동기화 (전체/주간/월간) |
| 랭킹 아카이브 | 주간: 일요일 23:55, 월간: 월말 23:55 | 기간별 랭킹 최종 결과 저장 |
| 파일 정리 | 매일 새벽 3시 | 고아 파일(DB 미연결) 삭제 |
| 챌린지 리마인더 | 매일 오전 9시 | 종료 1주일 전, 3일 전, 당일 알림 |
| 조회수 동기화 | 1시간마다 | Redis → DB 동기화 |

---

## 개발 환경 설정

### 필수 요구사항
- Java 17+ (Kotlin 1.9.20)
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

# 파일 저장
FILE_UPLOAD_DIR=./uploads
FILE_UPLOAD_URL_PATH=/images

# Google GenAI (Gemini AI)
GOOGLE_GENAI_API_KEY=your-genai-api-key

# AWS S3 (선택 사항, 현재 미사용)
# AWS_ACCESS_KEY_ID=your-access-key
# AWS_SECRET_ACCESS_KEY=your-secret-key
# AWS_S3_BUCKET=your-bucket-name
```

---

## 프로젝트 통계

| 항목 | 수치 |
|------|------|
| **구현된 API 엔드포인트** | 50+ |
| **구현된 프론트엔드 페이지** | 18+ |
| **백엔드 컨트롤러** | 14개 (Auth, User, Challenge, Certification, Follow, Feed, Notification, LikeComment, Badge, Point, Ranking, RankingSse, Streak, Health) |
| **스케줄러** | 5개 (Streak, Ranking, FileCleanup, ChallengeReminder, ViewCount) |
| **구현된 기능** | 사용자 인증, 챌린지 관리, 인증 시스템, 팔로우, 실시간 피드, 알림, 포인트, 배지, 스트릭, 랭킹(실시간 SSE), AI 추천, AI 챌린지 생성, AI 인증 분석, 파일 관리 |
| **완료율** | ~70% (핵심 기능 및 AI 기능 완료) |
| **현재 기술 스택** | Spring Boot + Kotlin, Next.js 16, PostgreSQL, Redis, SSE, Gemini AI, Recharts, Thumbnailator |
| **외부 API 연동** | 1개 (Google GenAI SDK) |

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

**실시간 SSE 기반 소셜 챌린지 트래커 - 구현 진행 중**

---

## 주요 기능 요약

### 완료된 기능

#### 백엔드
- **사용자 인증 & 프로필 관리**: 회원가입, 로그인, JWT, 프로필 조회/수정, 비밀번호 변경, 회원 탈퇴
- **챌린지 시스템**: 생성, 조회(목록/상세), 수정, 삭제, 참여/탈퇴, 검색, 통계
- **인증 시스템**: 생성, 조회, 수정, 삭제, 사진 업로드, 사용자별/챌린지별/기간별 목록
- **실시간 피드**: SSE 기반 실시간 알림 브로드캐스트, 하트비트, 연결 관리
- **팔로우 시스템**: 팔로우/언팔로우, 팔로워/팔로잉 목록, 카운트 캐싱, 팔로잉 피드
- **댓글 & 좋아요**: 댓글 생성/조회/삭제, 좋아요 토글, 중복 방지, 알림 발송
- **알림 시스템**: DB 기반 알림 저장, SSE 실시간 푸시, 읽음 처리, 미읽음 카운트
- **포인트**: 적립, 차감, 히스토리, 통계 조회
- **배지 시스템**: 배지 정의, 조건 체크, 자동 부여, 알림 발송, BadgeChecker 패턴
- **스트릭 시스템**: 연속 인증 추적, 캘린더, 리더보드, 리마인더
- **랭킹 시스템**: Redis Sorted Set 기반, SSE 실시간 업데이트, 주간/월간/전체
- **AI 추천 엔진**: Gemini AI 기반 기존 챌린지 추천 (자연어 쿼리 지원)
- **AI 챌린지 생성기**: Gemini AI 기반 키워드/자연어 챌린지 자동 생성
- **AI 인증 분석**: Gemini AI 기반 인증 사진 분석 및 적합성 판단, 재분석
- **Redis 캐싱**: 조회수, 팔로우 수, 미읽음 알림 수, 랭킹
- **모니터링**: LoggingAspect (Service/Repository 로깅 + MDC)
- **스케줄링**: 5개 스케줄러 (스트릭, 랭킹, 챌린지 리마인더, 조회수 동기화, 파일 정리)

#### 프론트엔드
- **인증 페이지**: 로그인, 회원가입
- **대시보드**: 사용자 통계, 참여 중인 챌린지
- **챌린지**: 목록(필터링/페이지, AI 추천), 상세, 편집, 생성(AI 생성기 연동), 내 챌린지
- **인증**: 상세, 생성, 내 인증 목록
- **피드**: 실시간 SSE 기반 피드, 팔로잉 피드, 무한 스크롤
- **랭킹**: 주간/월간/전체, SSE 실시간 업데이트, 포디움 UI, 순위 변동 표시
- **프로필**: 내 프로필, 다른 사용자 프로필, 팔로우 기능, 인증/팔로워/팔로잉/배지/스트릭 탭
- **사용자 목록**: 검색, 페이지, 팔로우
- **통계 페이지**: Recharts 기반 차트 (일별 포인트, 누적 추이)
- **설정 페이지**: 계정 설정

### 진행 중 & 계획 중
- 이미지 업로드 및 S3 연동
- 댓글 수/좋아요 수 Redis 캐싱
- AI 동기부여 코치 (Gemini AI 기반)
- 고급 검색 (인덱싱, 자동완성)
- 성능 최적화 (쿼리, 인덱싱, 페이지)
