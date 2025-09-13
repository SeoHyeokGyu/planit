# planit
# 여행 플래너 개발 계획 (API별 주차 분산, 12주)

## 개발 환경
- **백엔드**: 3명 개발자 (Spring Boot + PostgreSQL + Redis) - 우선 개발
- **프론트엔드**: AI 도구 활용 (Next.js + TypeScript) - 백엔드 완성 후 연동
- **배포**: 상시 배포 (CI/CD 파이프라인) - 단일 프로덕션 환경

---

## Week 1: 프로젝트 기반 구축

### 백엔드 (3명 공동 작업)
- [ ] Spring Boot 프로젝트 초기 설정
- [ ] 데이터베이스 스키마 설계 (User, Destination, TripPlan)
- [ ] Docker 개발 환경 구성 (PostgreSQL, Redis)
- [ ] Git 브랜치 전략 및 코드 리뷰 룰 수립
- [ ] **전체 API 명세서 작성 (Swagger) ← 프론트엔드 개발 기준 문서**

### 프론트엔드 준비
- [ ] Next.js 프로젝트 초기 설정
- [ ] AI 도구 선택 및 설정 (v0.dev, Claude, Cursor)
- [ ] 기술 스택 확정 (Tailwind, Shadcn/ui, Zustand, React Query)
- [ ] 컴포넌트 구조 설계
- [ ] **백엔드 API 명세서 기반 AI 프롬프트 템플릿 작성**

### 팀 협업 및 알림 설정
- [ ] **디스코드 서버 생성 및 목적별 채널 설정**
- [ ] **GitHub 리포지토리 활동-디스코드 웹훅 연동**
- [ ] **GitHub Projects-디스코드 연동 (Marketplace 앱 또는 Actions 활용)**

**완료 목표**: 개발 환경 완성 + 전체 API 설계 + 고도화된 팀 협업 환경 구축

---

## Week 2: CI/CD 파이프라인

### CI/CD 파이프라인 구축 (3명 공동 작업)
- [ ] GitHub Actions 워크플로우 설정
- [ ] 자동 테스트 파이프라인 구축
- [ ] Docker 이미지 자동 빌드
- [ ] 프로덕션 서버 자동 배포 설정 (Render 활용)
- [ ] 헬스체크 및 롤백 자동화
- [ ] 배포 알림 시스템 (슬랙/이메일)

**완료 목표**: 상시 배포 시스템 완성

---

## Week 3: 사용자 관리 API

### 사용자 관리 API
- [ ] User 엔티티 구현
- [ ] UserPreference 엔티티 구현
- [ ] 회원가입 API (POST /api/auth/register)
- [ ] 로그인 API (POST /api/auth/login) - JWT 없이
- [ ] 사용자 프로필 CRUD API
- [ ] 선호도 설정 API (POST /api/users/preferences)
- [ ] 단위 테스트 작성

### 첫 프론트엔드 연동
- [ ] 회원가입 페이지 (AI 생성)
- [ ] 로그인 페이지 (AI 생성)
- [ ] 프로필 설정 페이지 (AI 생성)

### AI 프롬프트 예시
```
"사용자 회원가입 페이지 만들어줘.
API: POST /api/auth/register
요청: {email, password, name}
응답: {success: boolean, message: string, userId: string}
Next.js + TypeScript + Tailwind + React Hook Form 사용해서."
```

**완료 목표**: 사용자 관리 완성 + 첫 화면 동작

---

## Week 4: 여행지 관리 API

### 여행지 관리 API
- [ ] Destination 엔티티 구현
- [ ] DestinationFeature 엔티티 구현
- [ ] 여행지 목록 조회 API (GET /api/destinations)
- [ ] 여행지 상세 조회 API (GET /api/destinations/{id})
- [ ] 여행지 검색 API (GET /api/destinations/search)
- [ ] 키워드 기반 검색 기능
- [ ] 더미 데이터 50개 입력
- [ ] 단위 테스트 작성

### 여행지 UI 연동
- [ ] 여행지 목록 페이지 (AI 생성)
- [ ] 여행지 상세 페이지 (AI 생성)
- [ ] 검색 기능 UI (AI 생성)

### AI 프롬프트 예시
```
"여행지 목록 페이지 만들어줘.
API: GET /api/destinations?page=0&size=20
응답: {destinations: [{id, name, country, city, categories, imageUrl, rating}], totalPages}
카드 형태로 표시하고 페이지네이션 포함해줘."
```

**완료 목표**: 여행지 조회 + 검색 완성

---

## Week 5: 일정 관리 API

### 일정 관리 API 
- [ ] TripPlan 엔티티 구현
- [ ] TripPlanItem 엔티티 구현
- [ ] 일정 생성 API (POST /api/trip-plans)
- [ ] 일정 조회 API (GET /api/trip-plans/{id})
- [ ] 일정 수정 API (PUT /api/trip-plans/{id})
- [ ] 일정 삭제 API (DELETE /api/trip-plans/{id})
- [ ] 일정 아이템 CRUD API
- [ ] 단위 테스트 작성

### 일정 관리 UI 연동
- [ ] 일정 생성 페이지 (AI 생성)
- [ ] 일정 목록 페이지 (AI 생성)
- [ ] 일정 상세 관리 페이지 (AI 생성)
- [ ] 드래그앤드롭 일정 편집 (AI 생성)

### AI 프롬프트 예시
```
"일정 관리 페이지 만들어줘.
API: GET /api/trip-plans/{id}
응답: {id, title, startDate, endDate, items: [{day, order, placeName, startTime, endTime}]}
드래그앤드롭으로 일정 순서 변경 가능하게 해줘."
```

**완료 목표**: 기본 일정 관리 완성

---

## Week 6: JWT 인증 API

### JWT 인증 시스템
- [ ] JWT 토큰 생성 서비스 구현
- [ ] JWT 토큰 검증 필터 구현
- [ ] Spring Security 설정
- [ ] 로그인 API 수정 (JWT 토큰 반환)
- [ ] 토큰 갱신 API (POST /api/auth/refresh)
- [ ] 로그아웃 API (POST /api/auth/logout)
- [ ] 인증 관련 통합 테스트

### JWT 프론트엔드 연동
- [ ] JWT 토큰 관리 커스텀 훅 (AI 생성)
- [ ] 인증이 필요한 페이지 보안 처리
- [ ] 자동 로그인/로그아웃 처리
- [ ] 토큰 만료 처리

### AI 프롬프트 예시
```
"JWT 토큰 관리하는 React 커스텀 훅 만들어줘.
로그인 API: POST /api/auth/login 응답: {token, refreshToken, user}
- 토큰을 localStorage에 저장
- API 호출시 자동으로 Authorization 헤더 추가
- 토큰 만료시 자동 갱신 또는 로그아웃"
```

**완료 목표**: 완전한 인증 시스템

---

## Week 7: Google Maps 연동 API

### Google Maps 연동 API
- [ ] Google Maps API 설정
- [ ] 거리 계산 API (POST /api/maps/distance)
- [ ] 경로 조회 API (POST /api/maps/directions)
- [ ] 장소 검색 API (GET /api/maps/places)
- [ ] 위치 정보 저장/조회 기능
- [ ] 지오코딩/리버스 지오코딩 API
- [ ] 외부 API 통합 테스트

### 지도 기능 UI 연동
- [ ] Google Maps 컴포넌트 구현 (AI 생성)
- [ ] 경로 표시 기능 (AI 생성)
- [ ] 장소 마커 표시 (AI 생성)
- [ ] 거리/시간 정보 표시 (AI 생성)

### AI 프롬프트 예시
```
"Google Maps 컴포넌트 만들어줘.
- 여러 여행지를 마커로 표시
- 경로를 라인으로 연결 표시
- 각 마커 클릭시 여행지 정보 팝업
- 거리와 예상 시간 정보 표시"
```

**완료 목표**: 지도 기능 완성

---

## Week 8: 추천 시스템 고도화 및 개인화

### 추천 시스템 API
- [ ] UserBehavior 엔티티 구현 (클릭, 찜 등 행동 추적)
- [ ] **Survey, Question, Answer, UserSurveyResponse 엔티티 구현**
- [ ] **외부 날씨 API 연동**
- [ ] 개인화 추천 API (GET /api/recommendations/personal)
- [ ] 유사 여행지 추천 API (GET /api/destinations/{id}/similar)
- [ ] 인기 여행지 API (GET /api/destinations/popular)
- [ ] 찜하기 API (POST /api/users/favorites)
- [ ] **콘텐츠 기반 + 연령, 시기, 날씨, 설문 기반 추천 알고리즘 구현**
- [ ] 추천 시스템 단위 테스트

### 추천 기능 UI 연동
- [ ] **상세/간편 타입별 설문 페이지 (AI 생성)**
- [ ] 개인화 추천 페이지 (AI 생성)
- [ ] 찜하기 기능 UI (AI 생성)
- [ ] 유사 여행지 표시 (AI 생성)
- [ ] 인기 여행지 섹션 (AI 생성)
- [ ] **날씨, 시기, 연령 기반 추천 결과 UI (AI 생성)**

### AI 프롬프트 예시
```
"개인화 추천 페이지 만들어줘.
API: GET /api/recommendations/personal?age=30&season=summer
응답: [{id, name, category, price, rating, imageUrl, reason}]
카드 형태로 표시하고, '30대 여름 여행으로 추천' 같은 이유도 함께 보여줘."
```

**완료 목표**: 다양한 변수를 고려하는 지능형 추천 시스템 완성

---

## Week 9: 소셜 로그인 API

### 소셜 로그인 API
- [ ] OAuth2 설정 (구글, 카카오)
- [ ] 소셜 로그인 콜백 API (GET /api/auth/oauth/{provider}/callback)
- [ ] 소셜 회원가입 처리
- [ ] 기존 계정 연동 기능
- [ ] 프로필 이미지 업로드 API (POST /api/users/profile-image)
- [ ] 소셜 계정 연동 해제 API
- [ ] 소셜 로그인 통합 테스트

### 소셜 로그인 UI 연동
- [ ] 구글 로그인 버튼 (AI 생성)
- [ ] 카카오 로그인 버튼 (AI 생성)
- [ ] 소셜 로그인 콜백 페이지 (AI 생성)
- [ ] 프로필 이미지 업로드 UI (AI 생성)

### AI 프롬프트 예시
```
"소셜 로그인 버튼들 만들어줘.
구글 로그인: GET /api/auth/oauth/google (리다이렉트)
카카오 로그인: GET /api/auth/oauth/kakao (리다이렉트)
각각의 브랜드 컬러와 아이콘 사용해서 버튼 디자인해줘."
```

**완료 목표**: 소셜 로그인 완성

---

## Week 10: 고급 검색 및 필터링 API

### 고급 검색 API 
- [ ] 고급 검색 API (POST /api/destinations/advanced-search)
- [ ] 필터링 기능 (카테고리, 가격, 평점, 지역)
- [ ] 정렬 기능 (인기도, 평점, 가격, 거리)
- [ ] 검색 결과 캐싱 (Redis)
- [ ] 실시간 인기도 업데이트 시스템
- [ ] 조회수 기반 트렌딩 API
- [ ] 검색 성능 최적화

### 고급 검색 UI 연동
- [ ] 고급 검색 필터 컴포넌트 (AI 생성)
- [ ] 실시간 검색 결과 업데이트 (AI 생성)
- [ ] 필터 태그 표시 (AI 생성)
- [ ] 정렬 옵션 UI (AI 생성)

### AI 프롬프트 예시
```
"고급 검색 필터 컴포넌트 만들어줘.
- 카테고리 체크박스 (문화, 자연, 도시, 해변 등)
- 가격대 슬라이더 (1-5단계)
- 평점 선택 (3점 이상, 4점 이상 등)
- 지역 선택 드롭다운
필터 변경시 실시간으로 검색 결과 업데이트"
```

**완료 목표**: 고급 검색 완성

---

## Week 11: 사용자 참여 기능 및 고급 기능

### 예산 관리 및 리뷰 API
- [ ] Budget 엔티티 구현 및 관련 API
- [ ] Review 엔티티 구현 및 관련 API
- [ ] 리뷰 평점 집계 시스템

### 뱃지, 챌린지, 랭킹 API
- [ ] **Badge, Challenge, UserBadge 엔티티 구현**
- [ ] **뱃지/챌린지 획득 조건 로직 및 부여 API**
- [ ] **여행지 랭킹 집계 로직 (Redis 활용)**
- [ ] **여행지 랭킹 API (GET /api/rankings/destinations)**

### UI 연동
- [ ] 예산 관리 대시보드 (AI 생성)
- [ ] 리뷰 작성/표시 컴포넌트 (AI 생성)
- [ ] **뱃지/챌린지 목록 및 '나의 뱃지' 페이지 (AI 생성)**
- [ ] **여행지 랭킹 리더보드 페이지 (AI 생성)**

### AI 프롬프트 예시
```
"여행지 랭킹 리더보드 페이지 만들어줘.
API: GET /api/rankings/destinations?period=weekly
응답: [{rank, name, country, score, imageUrl}]
- 1위부터 100위까지 순위 표시
- 주간/월간 필터링 기능 포함"
```

**완료 목표**: 예산 관리, 리뷰, 뱃지, 랭킹 등 사용자 참여 기능 완성

---

## Week 12: 성능 최적화 및 런칭 준비

### 성능 최적화 (3명 공동 작업)
**백엔드 최적화**
- [ ] 데이터베이스 인덱스 최적화
- [ ] Redis 캐싱 전략 구현 
- [ ] API 응답 시간 개선 (목표: 500ms 이하)
- [ ] N+1 문제 해결
- [ ] API Rate Limiting 구현

**프론트엔드 최적화**
- [ ] 코드 스플리팅 적용 (AI 생성)
- [ ] 이미지 lazy loading (AI 생성)
- [ ] API 호출 최적화 (React Query 활용)
- [ ] 불필요한 리렌더링 방지

### 최종 완성
- [ ] 전체 E2E 테스트
- [ ] 사용자 대시보드 완성 (AI 생성)
- [ ] 사용자 가이드 작성
- [ ] 정식 서비스 런칭

### AI 프롬프트 예시
```
"사용자 대시보드 페이지 만들어줘.
- 나의 여행 계획 목록
- 찜한 여행지 목록
- 작성한 리뷰 목록  
- 여행 통계 (총 여행 수, 방문 국가 수 등)
깔끔하고 직관적인 레이아웃으로 구성해줘."
```

**완료 목표**: 완전한 여행 플래너 서비스 정식 런칭

---

## API 개발 타임라인 요약

```
Week 1-2: 기반 구축 (환경 + CI/CD)
Week 3: 사용자 관리 API + 첫 UI
Week 4: 여행지 관리 API + UI
Week 5: 일정 관리 API + UI  
Week 6: JWT 인증 API + UI
Week 7: Google Maps API + UI
Week 8: 추천 시스템 고도화 API + UI
Week 9: 소셜 로그인 API + UI
Week 10: 고급 검색 API + UI
Week 11: 사용자 참여(뱃지, 랭킹) 및 고급 기능(예산, 리뷰) API + UI
Week 12: 성능 최적화 + 런칭
```

## 주차별 API 집중의 장점

### ✅ **깊이 있는 개발**
- 각 API 영역에 충분한 시간 할당
- 단위 테스트와 통합 테스트 시간 확보
- 완성도 높은 API 구현 가능

### ✅ **안정적인 연동**
- 각 주차마다 백엔드 API 완성 후 즉시 프론트엔드 연동
- AI를 활용한 빠른 UI 개발
- 단계별 테스트로 안정성 확보

### ✅ **체계적인 학습**
- 각 기술 영역을 집중적으로 학습
- 점진적 복잡도 증가
- 팀원별 전문성 향상

**12주로 완성되는 고품질 여행 플래너 서비스!** 🚀
