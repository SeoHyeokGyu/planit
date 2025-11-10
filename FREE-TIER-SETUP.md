# 완전 무료 배포 설정 가이드

Render Web Service(무료) + Neon PostgreSQL(무료) + Upstash Redis(무료)를 사용하여 완전히 무료로 배포하는 방법입니다.

## 목차
1. [Neon PostgreSQL 설정](#1-neon-postgresql-설정)
2. [Upstash Redis 설정](#2-upstash-redis-설정)
3. [Render 배포](#3-render-배포)
4. [환경 변수 설정](#4-환경-변수-설정)
5. [비용 정리](#5-비용-정리)

---

## 1. Neon PostgreSQL 설정

### 가입 및 프로젝트 생성

1. **Neon 가입**
   - https://neon.tech 접속
   - GitHub 계정으로 로그인

2. **프로젝트 생성**
   - **New Project** 클릭
   - **Project name**: `planit`
   - **Region**: Singapore (Render와 동일 권장)
   - **Postgres version**: 16
   - **Create Project** 클릭

3. **연결 문자열 복사**
   ```
   Dashboard → Connection Details → Connection string
   ```
   
   예시:
   ```
   postgresql://username:password@ep-xxx.ap-southeast-1.aws.neon.tech/planit?sslmode=require
   ```

### 무료 플랜 제한

- **저장공간**: 3GB
- **연결 수**: 100 동시 연결
- **무제한 기간**: 영구 무료 ✅
- **자동 슬립**: 5분 비활성 시 (첫 쿼리 시 자동 재시작)

---

## 2. Upstash Redis 설정

### 가입 및 데이터베이스 생성

1. **Upstash 가입**
   - https://upstash.com 접속
   - GitHub 계정으로 로그인

2. **Redis 데이터베이스 생성**
   - **Create Database** 클릭
   - **Name**: `planit-redis`
   - **Type**: Regional
   - **Region**: Singapore (Render와 동일 권장)
   - **Create** 클릭

3. **연결 정보 복사**
   ```
   Dashboard → Database → Details 탭
   ```
   
   필요한 정보:
   - **Endpoint**: `xxx.upstash.io`
   - **Port**: `6379`
   - **Password**: `xxxxxxxxxxxxxxxx`

### 무료 플랜 제한

- **명령어**: 10,000 commands/day
- **저장공간**: 256MB
- **무제한 기간**: 영구 무료 ✅
- **동시 연결**: 100

---

## 3. Render 배포

### Blueprint로 배포

1. **Render 가입**
   - https://render.com 접속
   - GitHub 계정으로 로그인

2. **Blueprint 배포**
   - Dashboard → **Blueprints** 메뉴
   - **New Blueprint Instance** 클릭
   - 저장소 선택: `planit`
   - 브랜치: `main`
   - **Apply** 클릭

3. **배포 확인**
   - Web Service가 자동으로 생성됨
   - 빌드 로그에서 진행 상황 확인

### 무료 플랜 제한

- **빌드 시간**: 월 500분
- **실행 시간**: 월 750시간
- **메모리**: 512MB
- **대역폭**: 100GB/월
- **슬립 모드**: 15분 비활성 시 (첫 요청 시 ~1분 콜드 스타트)

---

## 4. 환경 변수 설정

Render Dashboard → Web Service → **Environment** 탭에서 다음 환경 변수를 추가합니다.

### 필수 환경 변수

| 키 | 값 | 설명 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | (자동 설정됨) |
| `DATABASE_URL` | Neon 연결 문자열 | 1단계에서 복사한 값 |
| `JWT_SECRET` | (Generate 버튼) | Render에서 자동 생성 |

### Redis 환경 변수

| 키 | 값 | 설명 |
|---|---|---|
| `REDIS_HOST` | Upstash Endpoint | 예: `xxx.upstash.io` |
| `REDIS_PORT` | `6379` | (자동 설정됨) |
| `REDIS_PASSWORD` | Upstash Password | 2단계에서 복사한 값 |

### 외부 API (선택사항)

| 키 | 값 | 설명 |
|---|---|---|
| `GOOGLE_MAPS_API_KEY` | Google API 키 | 7주차 구현 시 필요 |
| `WEATHER_API_KEY` | 날씨 API 키 | 8주차 구현 시 필요 |

### 설정 방법

1. **Environment** 탭 클릭
2. **Add Environment Variable** 클릭
3. 키와 값 입력
4. **Save Changes** 클릭
5. 자동으로 재배포됨

---

## 5. 비용 정리

### 완전 무료 구성

| 서비스 | 플랜 | 비용 | 제한 |
|--------|------|------|------|
| **Render Web Service** | Free | $0/월 | 750시간, 15분 슬립 |
| **Neon PostgreSQL** | Free | $0/월 | 3GB, 영구 무료 |
| **Upstash Redis** | Free | $0/월 | 10K commands/day |
| **총 비용** | - | **$0/월** | ✅ 완전 무료 |

### 기존 Render 구성 (비교)

| 서비스 | 플랜 | 비용 | 제한 |
|--------|------|------|------|
| Render Web Service | Free | $0/월 | 750시간 |
| Render PostgreSQL | Free → Paid | $0 → $7/월 | 90일 후 유료 |
| Render Redis | Starter | $10/월 | 최소 요금제 |
| **총 비용** | - | **$17/월** | ❌ |

### 절약 효과

**월 $17 → $0** (100% 절약)

---

## 배포 확인

### 1. 헬스 체크

```bash
curl https://planit-api.onrender.com/api/health

# 응답 예시
{
  "status": "UP",
  "timestamp": "2025-11-10T15:30:00",
  "service": "Planit API"
}
```

### 2. Swagger UI

```
https://planit-api.onrender.com/swagger-ui/index.html
```

### 3. 데이터베이스 연결 확인

Render Logs에서 다음 로그 확인:
```
HikariPool-1 - Start completed.
Initialized JPA EntityManagerFactory
```

### 4. Redis 연결 확인

Render Logs에서 다음 로그 확인:
```
Lettuce ConnectionFactory initialized
```

---

## 트러블슈팅

### 1. Neon 연결 실패

**문제**: `connection refused` 또는 `SSL required`

**해결**:
- DATABASE_URL에 `?sslmode=require` 포함 확인
- Neon Dashboard → Settings → IP Allow에서 `0.0.0.0/0` 추가

### 2. Redis 연결 실패

**문제**: `Unable to connect to Redis`

**해결**:
1. Upstash Dashboard에서 연결 정보 재확인
2. REDIS_PASSWORD에 공백 없는지 확인
3. Upstash Free Tier daily limit 확인

### 3. 콜드 스타트 느림

**문제**: 15분 비활성 후 첫 요청이 느림 (~1분)

**해결 (선택사항)**:
1. **UptimeRobot** (https://uptimerobot.com) 무료 사용
   - 5분마다 헬스체크 요청
   - 슬립 모드 방지

2. **Cron-job.org** (https://cron-job.org)
   - 주기적으로 API 호출
   - 무료 플랜

### 4. Neon 슬립 모드

**문제**: 5분 비활성 시 Neon이 슬립

**해결**:
- 자동으로 첫 쿼리 시 재시작 (1-2초)
- Connection Pool 설정으로 미리 연결 유지:
  ```yaml
  spring:
    datasource:
      hikari:
        minimum-idle: 1  # 최소 1개 연결 유지
  ```

---

## 성능 최적화

### 1. JVM 메모리 최적화

Render Free Tier는 512MB 메모리 제한이 있습니다.

`Dockerfile` 확인:
```dockerfile
ENV JAVA_OPTS="-Xmx400m -Xms200m -XX:+UseG1GC"
```

### 2. Connection Pool 설정

`application-prod.yml`:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 5  # Free Tier에서는 작게
      minimum-idle: 1
      connection-timeout: 20000
```

### 3. Redis 캐싱 전략

무료 플랜 10K commands/day 제한:
- 자주 조회되는 데이터만 캐싱
- TTL 설정으로 불필요한 데이터 자동 삭제
- 검색 결과, 추천 결과에만 캐싱 사용

---

## 추가 무료 서비스

### 모니터링

- **UptimeRobot** (https://uptimerobot.com)
  - 서비스 가용성 모니터링
  - 무료: 50개 모니터

- **Sentry** (https://sentry.io)
  - 에러 트래킹
  - 무료: 5K errors/month

### CI/CD

- **GitHub Actions**
  - 이미 설정됨 (`.github/workflows/ci.yml`)
  - 무료: Public 저장소 무제한

### 파일 저장

여행 사진 등 파일 저장이 필요하다면:

- **Cloudinary** (https://cloudinary.com)
  - 이미지/비디오 저장 및 변환
  - 무료: 25GB 저장공간

---

## 프로덕션 사용 고려사항

Free Tier는 개발/테스트에 적합하며, 프로덕션 사용 시 다음을 고려하세요:

### 언제 유료로 업그레이드?

1. **트래픽 증가**
   - 일일 활성 사용자 100명 이상
   - 슬립 모드가 사용자 경험에 악영향

2. **데이터 증가**
   - PostgreSQL 3GB 근접
   - Redis 10K commands/day 초과

3. **안정성 필요**
   - 24/7 가용성 필요
   - 콜드 스타트 허용 불가

### 업그레이드 옵션

- **Render Web Service**: Starter ($7/월) - 슬립 모드 없음
- **Neon PostgreSQL**: Pro ($19/월) - 자동 확장, 더 많은 저장공간
- **Upstash Redis**: Pay-as-you-go - 사용량만큼 과금

---

## 참고 자료

- [Neon 공식 문서](https://neon.tech/docs)
- [Upstash 공식 문서](https://docs.upstash.com)
- [Render 공식 문서](https://render.com/docs)
- [Spring Boot Production Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)

---

## 문의

배포 관련 문제가 있다면 GitHub Issues에 등록해주세요.
