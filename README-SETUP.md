# Planit 프로젝트 설정 가이드

## 프로젝트 구조

```
planit/
├── src/
│   ├── main/
│   │   ├── kotlin/com/planit/
│   │   │   ├── PlanitApplication.kt
│   │   │   ├── config/          # 설정 클래스
│   │   │   ├── controller/      # REST 컨트롤러
│   │   │   ├── service/         # 비즈니스 로직
│   │   │   ├── repository/      # 데이터 접근 계층
│   │   │   ├── domain/          # 엔티티 모델
│   │   │   ├── dto/             # 데이터 전송 객체
│   │   │   ├── exception/       # 예외 처리
│   │   │   └── security/        # 보안 관련
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/kotlin/com/planit/  # 테스트
├── build.gradle.kts
├── settings.gradle.kts
├── docker-compose.yml
└── .env.example
```

## 필수 요구사항

- Java 17 이상
- Docker & Docker Compose
- Gradle 8.5 이상 (Wrapper 사용 시 자동 설치)

## 로컬 개발 환경 설정

### 1. 환경 변수 설정

`.env.example` 파일을 복사하여 `.env` 파일 생성:

```bash
cp .env.example .env
```

필요한 API 키와 시크릿 값을 `.env` 파일에 입력하세요.

### 2. Docker 컨테이너 실행

PostgreSQL과 Redis를 Docker로 실행:

```bash
docker-compose up -d
```

컨테이너 상태 확인:

```bash
docker-compose ps
```

### 3. 애플리케이션 실행

Gradle을 사용하여 애플리케이션 실행:

```bash
./gradlew bootRun
```

또는 IntelliJ IDEA에서 `PlanitApplication.kt`를 실행합니다.

### 4. API 문서 확인

애플리케이션 실행 후 Swagger UI에 접속:

```
http://localhost:8080/swagger-ui/index.html
```

### 5. Health Check

```bash
curl http://localhost:8080/api/health
```

예상 응답:
```json
{
  "status": "UP",
  "timestamp": "2025-10-14T21:00:00",
  "service": "Planit API"
}
```

## 빌드 및 테스트

### 프로젝트 빌드

```bash
./gradlew build
```

### 테스트 실행

```bash
./gradlew test
```

### 클린 빌드

```bash
./gradlew clean build
```

## Docker 관리

### 컨테이너 시작

```bash
docker-compose up -d
```

### 컨테이너 중지

```bash
docker-compose down
```

### 컨테이너 및 볼륨 삭제 (데이터 초기화)

```bash
docker-compose down -v
```

### 로그 확인

```bash
# PostgreSQL 로그
docker-compose logs -f postgres

# Redis 로그
docker-compose logs -f redis
```

## 데이터베이스 접속

### PostgreSQL

```bash
docker exec -it planit-postgres psql -U planit -d planit
```

또는 DB 클라이언트 사용:
- Host: localhost
- Port: 5432
- Database: planit
- Username: planit
- Password: planit123

### Redis

```bash
docker exec -it planit-redis redis-cli
```

## 프로파일 설정

### 개발 환경 (기본)

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 프로덕션 환경

```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## 주요 설정 파일

### application.yml
- 공통 애플리케이션 설정
- 데이터베이스 연결 정보
- Redis 설정
- OAuth2 설정

### application-dev.yml
- 개발 환경 전용 설정
- `ddl-auto: create-drop` (DB 자동 재생성)
- 상세한 로깅

### application-prod.yml
- 프로덕션 환경 전용 설정
- `ddl-auto: validate` (스키마 검증만)
- 최소한의 로깅

## 트러블슈팅

### Gradle 빌드 오류

```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Docker 포트 충돌

기존 PostgreSQL이나 Redis가 5432/6379 포트를 사용 중이면:
1. 기존 서비스 중지
2. 또는 `docker-compose.yml`에서 포트 변경

### IntelliJ IDEA 설정

1. File > Project Structure > Project SDK: Java 17 설정
2. Settings > Build > Build Tools > Gradle: Gradle JVM을 Java 17로 설정

## 다음 단계

1. **3주차**: 사용자 관리 API 구현
2. **4주차**: 여행지 관리 API 구현
3. **5주차**: 일정 관리 API 구현

자세한 내용은 `README.md`의 12주 개발 계획을 참고하세요.
