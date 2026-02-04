# 📸 스크린샷 가이드

이 문서는 README.md에 추가할 스크린샷의 파일명과 내용을 정리한 가이드입니다.

## 📁 저장 위치
```
docs/images/
```

## 📋 필요한 스크린샷 목록 (총 21개)

### 1. 홈 & 인증 (3개)
- `01-landing.png` - 랜딩 페이지 (Hero, 기능 소개, 통계)
- `01-login.png` - 로그인 페이지
- `01-signup.png` - 회원가입 페이지

### 2. 대시보드 (1개)
- `02-dashboard.png` - 메인 대시보드 (통계, 챌린지, AI 추천)

### 3. 챌린지 (3개)
- `03-challenge-list.png` - 챌린지 목록 (필터링, 검색, AI 추천)
- `03-challenge-detail.png` - 챌린지 상세 (정보, 통계, 참여자)
- `03-ai-generator.png` - ⭐ AI 챌린지 생성기

### 4. 인증 (3개)
- `04-certification-create.png` - 인증 작성 (AI 분석)
- `04-certification-detail.png` - 인증 상세 (좋아요, 댓글)
- `04-my-certifications.png` - 내 인증 목록

### 5. 실시간 피드 (2개)
- `05-feed.png` - ⭐ 전체 피드 (SSE 실시간 알림)
- `05-following-feed.png` - 팔로잉 피드

### 6. 랭킹 (1개)
- `06-ranking.png` - ⭐ 리더보드 (포디움, SSE 실시간 업데이트)

### 7. 프로필 (2개)
- `07-my-profile.png` - 내 프로필 (탭 네비게이션)
- `07-user-profile.png` - 다른 사용자 프로필 (팔로우 버튼)

### 8. 게임화 요소 (3개)
- `08-stats.png` - 포인트 통계 (Recharts 차트)
- `08-badges.png` - 배지 시스템
- `08-streak.png` - 스트릭 캘린더 (GitHub 스타일 잔디)

### 9. 알림 (1개)
- `09-notifications.png` - 알림 드롭다운 (SSE)

### 10. 사용자 찾기 (1개)
- `10-users.png` - 사용자 목록 (검색, 팔로우)

### 11. 설정 (1개)
- `11-settings.png` - 계정 설정

---

## 💡 스크린샷 촬영 팁

### 권장 사항
1. **해상도**: 1920x1080 또는 1440x900 (Retina)
2. **브라우저**: Chrome 또는 Safari (개발자 도구 반응형 모드)
3. **테마**: 라이트 모드 (가독성)
4. **줌**: 100% (기본)

### 캡처 범위
- **전체 페이지**: 랜딩, 대시보드, 챌린지 목록
- **주요 영역**: 폼, 카드, 모달 등

### 중요 화면 (⭐ 표시)
포트폴리오에서 특히 강조할 화면:
1. `03-ai-generator.png` - AI 챌린지 생성 (혁신성)
2. `05-feed.png` - 실시간 피드 (기술력)
3. `06-ranking.png` - 실시간 랭킹 (성능)

### 데이터 준비
- **테스트 데이터**: 실제처럼 보이는 샘플 데이터 준비
- **사용자명**: 개인정보 제거
- **통계**: 의미 있는 숫자 (너무 작거나 크지 않게)

---

## 📤 스크린샷 추가 후

1. 파일명이 정확한지 확인
2. Git에 추가
   ```bash
   git add docs/images/*.png
   git commit -m "docs: 프로젝트 스크린샷 추가"
   ```
3. README.md 확인 (이미지가 올바르게 표시되는지)
4. GitHub에 푸시

---

## 🎨 이미지 최적화 (선택 사항)

용량이 큰 경우 압축 권장:
```bash
# ImageMagick 사용
convert input.png -quality 85 -resize 1920x output.png

# 또는 TinyPNG 웹사이트 사용
# https://tinypng.com/
```

---

**생성일**: 2026-02-03
