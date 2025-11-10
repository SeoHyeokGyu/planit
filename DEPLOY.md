# Render 자동 배포 가이드

Planit 프로젝트를 Render 플랫폼에 배포하는 방법입니다.

## 목차
1. [사전 준비](#사전-준비)
2. [Render 계정 설정](#render-계정-설정)
3. [데이터베이스 생성](#데이터베이스-생성)
4. [웹 서비스 배포](#웹-서비스-배포)
5. [환경 변수 설정](#환경-변수-설정)
6. [배포 확인](#배포-확인)
7. [트러블슈팅](#트러블슈팅)

---

## 사전 준비

### 1. GitHub 저장소 준비
```bash
# 변경사항 커밋 및 푸시
git add .
git commit -m "feat: Render 배포 설정 추가"
git push origin main
```

### 2. 필요한 API 키 준비
- Google OAuth 클라이언트 ID/Secret
- Kakao OAuth 클라이언트 ID/Secret
- Google Maps API 키
- Weather API 키

---

## Render 계정 설정

### 1. Render 가입
1. https://render.com 접속
2. GitHub 계정으로 로그인
3. GitHub 저장소 접근 권한 허용

### 2. Blueprint 사용 (권장)
Render는 `render.yaml` 파일을 자동으로 감지하여 모든 리소스를 한 번에 생성합니다.

1. Render Dashboard → **Blueprints** 메뉴
2. **New Blueprint Instance** 클릭
3. GitHub 저장소 선택: `planit`
4. 브랜치 선택: `main`
5. **Apply** 클릭

Render가 자동으로 다음을 생성합니다:
- PostgreSQL 데이터베이스 (`planit-db`)
- Web Service (`planit-api`)

---

## 수동 설정 (Blueprint 미사용 시)

### 1. PostgreSQL 데이터베이스 생성

1. **New +** → **PostgreSQL**
2. 설정:
   - **Name**: `planit-db`
   - **Database**: `planit`
   - **User**: `planit`
   - **Region**: Singapore (또는 Oregon)
   - **Plan**: Free
3. **Create Database** 클릭
4. 데이터베이스가 생성되면 **Internal Database URL** 복사

### 2. Web Service 생성

1. **New +** → **Web Service**
2. GitHub 저장소 연결: `planit` 선택
3. 설정:
   - **Name**: `planit-api`
   - **Region**: Singapore (DB와 동일)
   - **Branch**: `main`
   - **Runtime**: Docker
   - **Plan**: Free

4. **Build Settings**:
   - **Dockerfile Path**: `./Dockerfile` (자동 감지)

5. **Advanced** → **Health Check Path**: `/api/health`

6. **Create Web Service** 클릭

---

## 환경 변수 설정

Web Service 생성 후 **Environment** 탭에서 다음 환경 변수를 추가합니다.

### 필수 환경 변수

| 키 | 값 | 설명 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | Spring Profile |
| `DATABASE_URL` | (자동) | PostgreSQL 연결 문자열 |
| `JWT_SECRET` | (생성) | JWT 서명 키 (32자 이상) |

### OAuth2 환경 변수 (선택사항)

| 키 | 값 | 설명 |
|---|---|---|
| `GOOGLE_CLIENT_ID` | `your-google-client-id` | Google OAuth |
| `GOOGLE_CLIENT_SECRET` | `your-google-client-secret` | Google OAuth |
| `KAKAO_CLIENT_ID` | `your-kakao-client-id` | Kakao OAuth |
| `KAKAO_CLIENT_SECRET` | `your-kakao-client-secret` | Kakao OAuth |

### 외부 API 환경 변수 (선택사항)

| 키 | 값 | 설명 |
|---|---|---|
| `GOOGLE_MAPS_API_KEY` | `your-maps-api-key` | Google Maps API |
| `WEATHER_API_KEY` | `your-weather-api-key` | 날씨 API |

### 환경 변수 설정 방법

1. **Environment** 탭 → **Add Environment Variable**
2. 키와 값 입력
3. **Save Changes** 클릭

#### DATABASE_URL 연결 (수동 설정 시)
1. PostgreSQL 서비스 → **Info** 탭
2. **Internal Database URL** 복사
3. Web Service → **Environment** 탭
4. `DATABASE_URL` 키에 URL 붙여넣기

#### JWT_SECRET 생성
```bash
# 랜덤 시크릿 생성 (로컬에서 실행)
openssl rand -base64 32
```

또는 Render에서 자동 생성:
1. **Environment** 탭
2. **Add Environment Variable**
3. 키: `JWT_SECRET`
4. **Generate** 버튼 클릭

---

## 배포 확인

### 1. 빌드 로그 확인
1. Web Service → **Logs** 탭
2. 빌드 진행 상황 모니터링
3. "Live" 상태가 되면 배포 완료

### 2. 헬스 체크
```bash
# Render 할당 URL (예시)
curl https://planit-api.onrender.com/api/health

# 응답 예시
{
  "status": "UP",
  "timestamp": "2025-11-10T15:30:00",
  "service": "Planit API"
}
```

### 3. Swagger UI 접속
```
https://planit-api.onrender.com/swagger-ui/index.html
```

---

## 자동 배포 설정

Render는 `render.yaml`에 `autoDeploy: true` 설정으로 **자동 배포**가 활성화됩니다.

### 작동 방식
1. `main` 브랜치에 코드 푸시
2. Render가 자동으로 감지
3. Docker 이미지 빌드
4. 새 버전 배포
5. Health Check 통과 시 트래픽 전환

### 수동 배포
1. Web Service → **Manual Deploy**
2. **Deploy latest commit** 클릭

---

## Redis 설정 (선택사항)

Render의 Redis는 유료입니다 ($10/월). 무료 대안으로 **Upstash Redis**를 권장합니다.

### Upstash Redis 사용

1. https://upstash.com 가입
2. **Create Database**
3. Region: Singapore (Render와 동일)
4. **REST API** 정보 복사:
   - `UPSTASH_REDIS_REST_URL`
   - `UPSTASH_REDIS_REST_TOKEN`

5. `application.yml` 수정:
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

6. Render 환경 변수 추가:
```
REDIS_HOST=your-upstash-url
REDIS_PORT=6379
REDIS_PASSWORD=your-upstash-token
```

---

## 비용 안내

### Free Tier 제한
- **Web Service**: 750시간/월 (무료)
  - 15분 비활성 시 자동 슬립
  - 첫 요청 시 콜드 스타트 (~1분)
- **PostgreSQL**: 90일 무료 체험
  - 이후 $7/월 (Starter Plan)

### Starter Plan 권장 ($7/월)
- 슬립 모드 없음
- 더 빠른 응답 속도
- 프로덕션 사용 가능

---

## 트러블슈팅

### 1. 빌드 실패

#### 문제: `Gradle build failed`
```bash
# 로컬에서 확인
./gradlew clean build

# Dockerfile 문제 확인
docker build -t planit-test .
```

#### 문제: `Out of memory`
Render Free Tier는 메모리 512MB 제한이 있습니다.

`Dockerfile` 수정:
```dockerfile
# JVM 메모리 최적화
ENV JAVA_OPTS="-Xmx400m -Xms200m"
```

### 2. 데이터베이스 연결 실패

#### 문제: `Unable to connect to database`
1. `DATABASE_URL` 환경 변수 확인
2. PostgreSQL 서비스 상태 확인 (Running인지)
3. Web Service와 DB Region이 동일한지 확인

#### 해결: Internal URL 사용
- External URL 대신 **Internal Database URL** 사용
- Render 내부 네트워크가 더 빠름

### 3. Health Check 실패

#### 문제: `Health check failed`
```bash
# 로컬에서 헬스체크 엔드포인트 확인
curl http://localhost:8080/api/health
```

`render.yaml` 확인:
```yaml
healthCheckPath: /api/health  # 정확한 경로
```

### 4. 콜드 스타트 문제 (Free Tier)

Free Tier는 15분 비활성 시 슬립 모드로 전환됩니다.

#### 해결 방법:
1. **Starter Plan 업그레이드** ($7/월)
2. **Cron Job으로 주기적 호출** (외부 서비스)
   ```bash
   # 예: UptimeRobot, Cron-job.org
   curl https://planit-api.onrender.com/api/health
   ```

### 5. 환경 변수 문제

#### 문제: OAuth2/JWT가 작동하지 않음
1. **Environment** 탭에서 모든 변수 확인
2. 값에 공백이나 특수문자 있는지 확인
3. 변경 후 **Manual Deploy** 필수

---

## 로그 확인

### 실시간 로그
```
Web Service → Logs 탭 → "Live" 선택
```

### 로그 레벨 조정
`application-prod.yml`:
```yaml
logging:
  level:
    com.planit: INFO  # DEBUG에서 INFO로 변경 (로그 감소)
```

---

## 모니터링

### Render 대시보드
- CPU 사용량
- 메모리 사용량
- 응답 시간
- 요청 수

### 외부 모니터링 (권장)
- **Sentry**: 에러 트래킹
- **DataDog**: APM 모니터링
- **UptimeRobot**: 서비스 가용성 모니터링

---

## 다음 단계

1. **커스텀 도메인 설정**
   - Web Service → Settings → Custom Domain
   - 예: `api.planit.com`

2. **HTTPS 설정**
   - Render가 자동으로 Let's Encrypt SSL 인증서 발급

3. **백업 설정**
   - PostgreSQL → Backups 탭
   - 자동 백업 활성화 (Starter Plan 이상)

4. **성능 최적화**
   - JPA 쿼리 최적화
   - Redis 캐싱 활용
   - Connection Pool 튜닝

---

## 참고 자료

- [Render 공식 문서](https://render.com/docs)
- [Render Blueprint 스펙](https://render.com/docs/blueprint-spec)
- [Docker 최적화 가이드](https://docs.docker.com/develop/dev-best-practices/)
- [Spring Boot Production 가이드](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)

---

## 문의

배포 관련 문제가 있다면 GitHub Issues에 등록해주세요.
