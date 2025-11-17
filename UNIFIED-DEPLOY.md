# 통합 배포 가이드

프론트엔드(Next.js)와 백엔드(Spring Boot)를 **하나의 JAR 파일**로 통합 배포합니다.

## 배포 구조

```
planit.jar
├── Spring Boot 백엔드 (Kotlin)
└── Next.js 프론트엔드 (정적 파일)
```

## 변경 사항

### 1. Next.js 설정 (frontend/next.config.ts)
- `output: 'export'` - 정적 HTML로 빌드
- `distDir: 'out'` - 빌드 결과 디렉토리

### 2. API URL (상대 경로 사용)
- ~~https://planit-api-y2ie.onrender.com~~ (분리 배포)
- `window.location.origin` (통합 배포)
- 같은 도메인에서 서빙되므로 CORS 불필요

### 3. Gradle 빌드 (build.gradle.kts)
빌드 시 자동으로 다음 작업 수행:
1. `installFrontend` - npm install
2. `buildFrontend` - npm run build (frontend/out 생성)
3. `copyFrontend` - out 폴더를 src/main/resources/static으로 복사
4. `processResources` - JAR에 포함

### 4. Spring Boot 설정
- **WebConfig.kt** - SPA 라우팅 (모든 경로를 index.html로 포워딩)
- **SecurityConfig.kt** - 정적 파일 접근 허용

## 로컬 개발

### 프론트엔드 개발
```bash
cd frontend
npm run dev
# http://localhost:3000
```

### 백엔드 개발
```bash
./gradlew bootRun
# http://localhost:8080
```

### 통합 빌드 테스트
```bash
./gradlew clean build
java -jar build/libs/planit-0.0.1-SNAPSHOT.jar
# http://localhost:8080 (프론트+백엔드 통합)
```

## Render 배포

### 설정 변경

**planit-frontend 서비스 삭제:**
1. Dashboard → planit-frontend → Settings
2. 맨 하단 "Delete Web Service"

**planit-api 설정 유지:**
- 기존 설정 그대로 사용
- Gradle 빌드 시 프론트엔드 자동 포함

### 배포 후 접속

**단일 URL:**
```
https://planit-api-y2ie.onrender.com
```

**프론트엔드:**
- 메인: https://planit-api-y2ie.onrender.com/
- API 테스트: https://planit-api-y2ie.onrender.com/api-test/

**백엔드 API:**
- Health: https://planit-api-y2ie.onrender.com/api/health
- Swagger: https://planit-api-y2ie.onrender.com/swagger-ui/index.html

## 장점

✅ **비용 절감**: 서버 1개만 필요 (Render 프리티어 활용)
✅ **CORS 불필요**: 같은 도메인
✅ **배포 간단**: JAR 파일 하나
✅ **관리 편의**: 단일 서비스

## 주의사항

⚠️ **Next.js SSR 불가**: 정적 빌드만 가능
⚠️ **독립 배포 불가**: 프론트 수정 시에도 전체 재배포
⚠️ **빌드 시간**: 프론트+백엔드 전체 빌드 (5~10분)

## 분리 배포로 되돌리기

필요 시 다음 커밋으로 복구:
```bash
git revert HEAD
```
