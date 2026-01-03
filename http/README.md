# API 테스트 HTTP 파일

IntelliJ IDEA의 REST Client 플러그인을 사용하여 API를 테스트하는 HTTP 파일들입니다.

## 파일 구조

```
http/
├── auth/
│   └── auth.http              # 인증 관련 API (회원가입, 로그인, 로그아웃)
├── user/
│   └── user.http              # 사용자 프로필/검색
├── challenge/
│   └── challenge.http         # 챌린지 생성/조회/관리
├── certification/
│   └── certification.http     # 인증 작성/관리
├── point/
│   └── point.http             # 포인트/경험치/레벨 시스템
├── follow/
│   └── follow.http            # 팔로우/팔로워 시스템
├── badge/
│   └── badge.http             # 배지 관련
├── notification/
│   └── notification.http      # 알림 조회/관리
├── http-client.env.json       # 환경 변수 설정
└── README.md                  # 이 파일
```

## 사용 방법

### 1. 환경 설정
`http-client.env.json` 파일에서 환경 변수를 설정합니다.

```json
{
  "dev": {
    "host": "http://localhost:8080"
  },
  "prod": {
    "host": "https://planit-api-y2ie.onrender.com/"
  }
}
```

### 2. HTTP 파일 실행
각 HTTP 파일의 요청을 선택하고 IntelliJ IDEA의 REST Client에서 실행합니다.

```
▶ (재생 버튼을 클릭) 또는 Ctrl+Alt+R
```

### 3. 토큰 변수
`auth.http` 파일의 로그인 요청을 먼저 실행하여 `{{auth_token}}` 변수를 설정합니다.
이후 다른 인증이 필요한 요청들에서 이 토큰이 자동으로 사용됩니다.

## API 파일별 설명

### auth/auth.http
- **회원가입**: `POST /api/auth/signup`
- **로그인**: `POST /api/auth/login` (토큰 발급)
- **로그아웃**: `POST /api/auth/logout`
- **프로필 확인**: `GET /api/users/me`

### user/user.http
- **프로필 조회**: `GET /api/users/{loginId}/profile`
- **프로필 수정**: `PUT /api/users/me/update-profile`
- **비밀번호 변경**: `PATCH /api/users/me/password`
- **사용자 검색**: `GET /api/users/search`
- **대시보드 통계**: `GET /api/users/me/stats`

### challenge/challenge.http ⭐ NEW
- **목록 조회**: `GET /api/challenge` (필터링, 페이징)
- **검색**: `GET /api/challenge/search`
- **상세 조회**: `GET /api/challenge/{id}`
- **생성**: `POST /api/challenge`
- **수정**: `PUT /api/challenge/{id}`
- **삭제**: `DELETE /api/challenge/{id}`
- **참여**: `POST /api/challenge/{id}/join`
- **탈퇴**: `POST /api/challenge/{id}/withdraw`
- **조회수 증가**: `POST /api/challenge/{id}/view`
- **참여자 목록**: `GET /api/challenge/{id}/participants`
- **통계**: `GET /api/challenge/{id}/statistics`
- **내 챌린지**: `GET /api/challenge/my`

### certification/certification.http
- **생성**: `POST /api/certifications`
- **목록**: `GET /api/certifications`
- **상세**: `GET /api/certifications/{id}`
- **수정**: `PUT /api/certifications/{id}`
- **삭제**: `DELETE /api/certifications/{id}`

### point/point.http ⭐ NEW
- **포인트 조회**: `GET /api/points/me`
- **포인트 히스토리**: `GET /api/points/me/history`
- **경험치/레벨 조회**: `GET /api/points/experience/me`
- **경험치 히스토리**: `GET /api/points/experience/me/history`
- **종합 진행도**: `GET /api/points/me/progress`

### follow/follow.http
- **팔로우**: `POST /api/follows/{loginId}`
- **언팔로우**: `DELETE /api/follows/{loginId}`
- **팔로잉 목록**: `GET /api/follows/{loginId}/followings`
- **팔로워 목록**: `GET /api/follows/{loginId}/followers`
- **팔로우 수**: `GET /api/follows/{loginId}/following-count`
- **팔로워 수**: `GET /api/follows/{loginId}/follower-count`

### badge/badge.http
- **전체 배지 조회**: `GET /api/badges`
- **내 배지 조회**: `GET /api/badges/my`
- **타인 배지 조회**: `GET /api/badges/user/{loginId}`

### notification/notification.http
- **알림 목록**: `GET /api/notifications`
- **미읽음 수**: `GET /api/notifications/unread-count`
- **읽음 표시**: `PUT /api/notifications/{id}/read`
- **알림 삭제**: `DELETE /api/notifications/{id}`

## 팁

1. **변수 설정**: `{{auth_token}}` 등 변수는 자동으로 설정되지만, 필요시 REQUEST section의 스크립트에서 수동 설정 가능
2. **응답 보기**: 요청 실행 후 Response 패널에서 결과 확인
3. **Debug**: Response section에서 응답 바디를 검사하고 필요시 변수 설정

## 주의사항

- **`{id}`, `{loginId}`, `{challengeId}` 등** 값은 실제 값으로 대체해야 합니다.
- `auth/auth.http`에서 로그인 후 `auth_token`이 설정되어야 인증이 필요한 요청 실행 가능
- 서버가 실행 중이어야 요청 가능 (기본 포트: 8080)
- REST Client 플러그인 설치 필요 (IntelliJ IDEA 내장)
