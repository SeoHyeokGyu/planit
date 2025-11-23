# Oracle Cloud 배포 가이드

이 문서는 Planit 애플리케이션을 Oracle Cloud Infrastructure(OCI)에 Docker를 사용하여 배포하는 방법을 설명합니다.

## 서버 정보
- **IP 주소**: 168.107.9.243
- **사용자**: opc
- **백엔드 URL**: http://168.107.9.243:8080
- **프론트엔드 URL**: http://168.107.9.243:3000
- **Swagger UI**: http://168.107.9.243:8080/swagger-ui/index.html
- **로그 뷰어**: http://168.107.9.243:9999

## 사전 요구사항

### 1. Oracle Cloud 인스턴스 설정
```bash
# SSH로 서버 접속
ssh opc@168.107.9.243

# 시스템 업데이트
sudo dnf update -y
```

### 2. Docker 설치
```bash
# Docker 설치
sudo dnf install -y docker-engine

# Docker 서비스 시작 및 자동 시작 설정
sudo systemctl start docker
sudo systemctl enable docker

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker opc

# 재로그인 필요 (또는 newgrp docker)
exit
ssh opc@168.107.9.243
```

### 3. Docker Compose 설치
```bash
# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 실행 권한 부여
sudo chmod +x /usr/local/bin/docker-compose

# 설치 확인
docker-compose --version
```

### 4. 방화벽 설정 (OCI Security List)

Oracle Cloud Console에서 다음 포트를 열어야 합니다:
- **8080** (백엔드 API)
- **3000** (프론트엔드)
- **9999** (로그 뷰어, 선택사항)

#### 인스턴스 방화벽 설정
```bash
# firewalld 포트 개방
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=3000/tcp
sudo firewall-cmd --permanent --add-port=9999/tcp
sudo firewall-cmd --reload

# 또는 iptables 사용 시
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 3000 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 9999 -j ACCEPT
sudo netfilter-persistent save
```

## 배포 단계

### 1. 프로젝트 복사

서버로 프로젝트 파일을 전송합니다:

```bash
# 로컬 머신에서 실행
scp -r /path/to/planit opc@168.107.9.243:~/
```

또는 Git을 사용:

```bash
# 서버에서 실행
cd ~
git clone https://github.com/your-username/planit.git
cd planit
```

### 2. 환경 변수 설정

```bash
# 프로덕션 환경 변수 파일 생성
cp .env.prod.example .env.prod

# 환경 변수 편집
vi .env.prod
```

`.env.prod` 파일에서 다음 값을 설정:

```bash
# Database Configuration
POSTGRES_USER=planit
POSTGRES_PASSWORD=강력한_비밀번호_설정

# Redis Configuration
REDIS_PASSWORD=강력한_Redis_비밀번호

# JWT Secret (보안 키 생성)
JWT_SECRET=$(openssl rand -base64 32)

# API Keys
GOOGLE_MAPS_API_KEY=your_actual_google_maps_api_key
WEATHER_API_KEY=your_actual_weather_api_key
```

### 3. 환경 변수 로드

```bash
# 환경 변수를 현재 세션에 로드
export $(cat .env.prod | xargs)
```

### 4. 애플리케이션 빌드 및 실행

```bash
# 프로덕션 모드로 Docker Compose 실행
docker-compose -f docker-compose.prod.yml up -d --build

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f

# 특정 서비스 로그만 확인
docker-compose -f docker-compose.prod.yml logs -f backend
docker-compose -f docker-compose.prod.yml logs -f frontend
```

### 5. 배포 확인

```bash
# 컨테이너 상태 확인
docker-compose -f docker-compose.prod.yml ps

# 헬스 체크
curl http://localhost:8080/api/health

# 웹 브라우저에서 확인
# 백엔드 Swagger: http://168.107.9.243:8080/swagger-ui/index.html
# 프론트엔드: http://168.107.9.243:3000
# 로그 뷰어: http://168.107.9.243:9999
```

## 운영 관리

### 애플리케이션 중지
```bash
docker-compose -f docker-compose.prod.yml down
```

### 애플리케이션 재시작
```bash
docker-compose -f docker-compose.prod.yml restart
```

### 특정 서비스만 재시작
```bash
docker-compose -f docker-compose.prod.yml restart backend
docker-compose -f docker-compose.prod.yml restart frontend
```

### 로그 확인
```bash
# 전체 로그
docker-compose -f docker-compose.prod.yml logs

# 실시간 로그 (tail -f와 유사)
docker-compose -f docker-compose.prod.yml logs -f

# 특정 서비스 로그
docker-compose -f docker-compose.prod.yml logs backend

# 또는 Dozzle 웹 UI 사용
# http://168.107.9.243:9999
```

### 데이터베이스 백업
```bash
# PostgreSQL 백업
docker exec planit-postgres pg_dump -U planit planit > backup_$(date +%Y%m%d_%H%M%S).sql

# 백업 복원
docker exec -i planit-postgres psql -U planit planit < backup_20240101_120000.sql
```

### 업데이트 배포
```bash
# 최신 코드 가져오기
git pull origin main

# 환경 변수 다시 로드
export $(cat .env.prod | xargs)

# 애플리케이션 재빌드 및 재시작
docker-compose -f docker-compose.prod.yml up -d --build

# 이전 이미지 정리
docker image prune -f
```

### 시스템 리소스 모니터링
```bash
# Docker 리소스 사용량
docker stats

# 디스크 사용량
df -h

# 메모리 사용량
free -h

# Docker 디스크 정리
docker system prune -a --volumes
```

## 문제 해결

### 컨테이너가 시작되지 않는 경우
```bash
# 컨테이너 상태 확인
docker-compose -f docker-compose.prod.yml ps

# 로그에서 오류 확인
docker-compose -f docker-compose.prod.yml logs

# 개별 컨테이너 검사
docker inspect planit-backend
```

### 데이터베이스 연결 실패
```bash
# PostgreSQL 컨테이너 확인
docker exec -it planit-postgres psql -U planit -d planit

# 연결 테스트
docker exec planit-backend curl postgres:5432
```

### 네트워크 문제
```bash
# 네트워크 확인
docker network ls
docker network inspect planit_planit-network

# 방화벽 규칙 확인
sudo firewall-cmd --list-all
sudo iptables -L -n
```

### 포트 충돌
```bash
# 포트 사용 확인
sudo netstat -tlnp | grep :8080
sudo netstat -tlnp | grep :3000

# 또는
sudo lsof -i :8080
sudo lsof -i :3000
```

## 보안 권장사항

1. **SSH 키 기반 인증 사용**
   ```bash
   # 로컬에서 SSH 키 생성
   ssh-keygen -t rsa -b 4096

   # 공개 키를 서버에 복사
   ssh-copy-id opc@168.107.9.243
   ```

2. **비밀번호 관리**
   - `.env.prod` 파일을 Git에 커밋하지 마세요
   - 강력한 비밀번호 사용
   - 정기적으로 비밀번호 변경

3. **HTTPS 설정 (권장)**
   ```bash
   # Nginx와 Let's Encrypt 사용
   # 추후 SSL 인증서 설정 가이드 참조
   ```

4. **정기 업데이트**
   ```bash
   # 시스템 패키지 업데이트
   sudo dnf update -y

   # Docker 이미지 업데이트
   docker-compose -f docker-compose.prod.yml pull
   docker-compose -f docker-compose.prod.yml up -d
   ```

## 자동 시작 설정

시스템 재부팅 시 자동으로 Docker Compose를 시작하도록 설정:

```bash
# systemd 서비스 파일 생성
sudo vi /etc/systemd/system/planit.service
```

내용:
```ini
[Unit]
Description=Planit Application
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/opc/planit
EnvironmentFile=/home/opc/planit/.env.prod
ExecStart=/usr/local/bin/docker-compose -f docker-compose.prod.yml up -d
ExecStop=/usr/local/bin/docker-compose -f docker-compose.prod.yml down
User=opc

[Install]
WantedBy=multi-user.target
```

서비스 활성화:
```bash
sudo systemctl daemon-reload
sudo systemctl enable planit.service
sudo systemctl start planit.service
```

## 참고 자료

- [Oracle Cloud 문서](https://docs.oracle.com/en-us/iaas/Content/home.htm)
- [Docker 문서](https://docs.docker.com/)
- [Docker Compose 문서](https://docs.docker.com/compose/)
- [Spring Boot 배포 가이드](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)
- [Next.js 배포 가이드](https://nextjs.org/docs/deployment)
