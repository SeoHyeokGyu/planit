# Oracle Cloud 서버 초기 설정 가이드

Oracle Cloud 서버(168.107.9.243)에 필요한 모든 도구를 설치하는 단계별 가이드입니다.

## 1. 서버 접속

```bash
ssh opc@168.107.9.243
```

## 2. 시스템 업데이트

```bash
# 시스템 패키지 업데이트
sudo dnf update -y
```

## 3. Git 설치

```bash
# Git 설치
sudo dnf install -y git

# 설치 확인
git --version
```

### Git 기본 설정

```bash
# Git 사용자 정보 설정
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# 설정 확인
git config --list
```

### GitHub Deploy Key 설정 (Private 저장소)

Private 저장소의 경우 Deploy Key를 설정해야 합니다:

```bash
# 1. SSH 키 생성
ssh-keygen -t ed25519 -C "oracle-cloud-deploy" -f ~/.ssh/github_deploy_key
# Enter 3번 (비밀번호 없음)

# 2. 공개 키 확인 (복사)
cat ~/.ssh/github_deploy_key.pub
```

**GitHub에서:**
1. 저장소 → Settings → Deploy keys
2. Add deploy key 클릭
3. Title: `Oracle Cloud Server`
4. Key: 공개 키 붙여넣기
5. ✅ **Allow write access** 체크
6. Add key 클릭

**서버에서 SSH config 설정:**

```bash
# vim으로 SSH config 편집
vim ~/.ssh/config

# i 키로 INSERT 모드, 아래 내용 입력
```

입력 내용:
```
# GitHub Deploy Key Configuration
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/github_deploy_key
    IdentitiesOnly yes
```

```bash
# Esc → :wq → Enter (저장하고 종료)

# 권한 설정
chmod 600 ~/.ssh/config
chmod 600 ~/.ssh/github_deploy_key

# 연결 테스트
ssh -T git@github.com
# "Hi SeoHyeokGyu/planit! You've successfully authenticated..." 메시지 확인
```

## 4. Docker 설치 (Oracle Linux)

```bash
# 기존 Docker 패키지 제거 (있다면)
sudo dnf remove -y docker docker-client docker-client-latest docker-common docker-latest docker-latest-logrotate docker-logrotate docker-engine podman runc

# 필수 패키지 설치
sudo dnf install -y dnf-utils

# Docker CE 저장소 추가
sudo dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# Docker CE 설치
sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Docker 서비스 시작 및 자동 시작 설정
sudo systemctl start docker
sudo systemctl enable docker

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker opc

# 그룹 변경 적용
newgrp docker

# 설치 확인
docker --version
docker ps
```

## 5. Docker Compose 확인

Docker CE를 설치하면 Docker Compose 플러그인이 자동으로 포함됩니다:

```bash
# Docker Compose 플러그인 버전 확인
docker compose version

# 기존 docker-compose 명령어도 호환됨
docker-compose --version
```

**참고**: 최신 Docker는 `docker compose` (플러그인)을 사용하지만, 기존 `docker-compose` 명령어도 작동합니다.

## 6. 유용한 도구 설치

```bash
# curl, wget, nano 등 기본 도구 설치
sudo dnf install -y curl wget nano vim htop net-tools

# 설치 확인
curl --version
wget --version
```

## 7. 방화벽 설정

### Option A: firewalld 사용 (권장)

```bash
# firewalld 상태 확인
sudo systemctl status firewalld

# firewalld가 실행 중이 아니면 시작
sudo systemctl start firewalld
sudo systemctl enable firewalld

# 필요한 포트 개방
sudo firewall-cmd --permanent --add-port=8080/tcp  # 백엔드
sudo firewall-cmd --permanent --add-port=3000/tcp  # 프론트엔드
sudo firewall-cmd --permanent --add-port=9999/tcp  # 로그 뷰어

# 변경사항 적용
sudo firewall-cmd --reload

# 설정 확인
sudo firewall-cmd --list-all
```

### Option B: iptables 사용

```bash
# 포트 개방
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 3000 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 9999 -j ACCEPT

# 설정 저장
sudo service iptables save

# 또는
sudo /etc/init.d/iptables save
```

## 8. Oracle Cloud 콘솔에서 Security List 설정

Oracle Cloud Console에서도 포트를 열어야 합니다:

1. Oracle Cloud Console 로그인
2. **Networking** → **Virtual Cloud Networks** 선택
3. 사용 중인 VCN 클릭
4. **Security Lists** 클릭
5. **Default Security List** 클릭
6. **Add Ingress Rules** 클릭

다음 규칙들을 추가:

| Source CIDR | IP Protocol | Source Port Range | Destination Port Range | Description |
|-------------|-------------|-------------------|------------------------|-------------|
| 0.0.0.0/0 | TCP | All | 8080 | Backend API |
| 0.0.0.0/0 | TCP | All | 3000 | Frontend |
| 0.0.0.0/0 | TCP | All | 9999 | Log Viewer (Optional) |

## 9. SSH 키 설정 (GitHub Actions용)

```bash
# .ssh 디렉토리 생성
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# authorized_keys 파일 생성 또는 편집
nano ~/.ssh/authorized_keys

# 로컬에서 생성한 공개 키를 붙여넣기
# (로컬 머신에서: cat ~/.ssh/planit_deploy_key.pub)

# 권한 설정
chmod 600 ~/.ssh/authorized_keys
```

### 로컬 머신에서 SSH 키 생성 (아직 안 했다면)

```bash
# SSH 키 페어 생성
ssh-keygen -t rsa -b 4096 -C "github-actions-deploy"
# 파일 경로: ~/.ssh/planit_deploy_key
# 비밀번호: (Enter - 비밀번호 없음)

# 공개 키 출력
cat ~/.ssh/planit_deploy_key.pub
# → 이 내용을 서버의 ~/.ssh/authorized_keys에 붙여넣기

# 연결 테스트
ssh -i ~/.ssh/planit_deploy_key opc@168.107.9.243
```

## 10. 프로젝트 클론

Deploy Key 설정이 완료되었다면 SSH로 클론:

```bash
# 홈 디렉토리로 이동
cd ~

# SSH URL로 프로젝트 클론
git clone git@github.com:SeoHyeokGyu/planit.git

# 프로젝트 디렉토리로 이동
cd planit

# 확인
ls -la
git status
```

## 11. 환경 변수 설정

```bash
# .env.prod 파일 생성
cp .env.prod.example .env.prod

# vim으로 환경 변수 편집
vim .env.prod
# i 키로 INSERT 모드, 값 수정 후 Esc → :wq
```

`.env.prod` 파일에 실제 값 입력:

```bash
# Database Configuration
POSTGRES_USER=planit
POSTGRES_PASSWORD=강력한_비밀번호_여기에_입력

# Redis Configuration
REDIS_PASSWORD=강력한_Redis_비밀번호_여기에_입력

# JWT Secret (랜덤 키 생성)
JWT_SECRET=$(openssl rand -base64 32)
# 또는 직접 입력
JWT_SECRET=your_very_long_and_secure_random_jwt_secret_key_here

# Google Maps API Key
GOOGLE_MAPS_API_KEY=your_actual_google_maps_api_key

# Weather API Key
WEATHER_API_KEY=your_actual_weather_api_key
```

### JWT Secret 생성 방법

```bash
# 랜덤 JWT Secret 생성
openssl rand -base64 32

# 출력된 값을 복사하여 .env.prod의 JWT_SECRET에 붙여넣기
```

## 12. 초기 배포

```bash
# 환경 변수 로드
export $(cat .env.prod | xargs)

# Docker Compose로 애플리케이션 실행
docker-compose -f docker-compose.prod.yml up -d --build

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f
```

## 13. 배포 확인

```bash
# 컨테이너 상태 확인
docker-compose -f docker-compose.prod.yml ps

# 헬스 체크
curl http://localhost:8080/api/health

# 외부에서 접속 확인
curl http://168.107.9.243:8080/api/health
```

웹 브라우저에서 접속:
- **백엔드 Swagger**: http://168.107.9.243:8080/swagger-ui/index.html
- **프론트엔드**: http://168.107.9.243:3000
- **로그 뷰어**: http://168.107.9.243:9999

## 14. 자동 시작 설정 (선택사항)

시스템 재부팅 시 자동으로 애플리케이션 시작:

```bash
# systemd 서비스 파일 생성
sudo nano /etc/systemd/system/planit.service
```

다음 내용 입력:

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
# systemd 데몬 리로드
sudo systemctl daemon-reload

# 서비스 활성화 (부팅 시 자동 시작)
sudo systemctl enable planit.service

# 서비스 시작
sudo systemctl start planit.service

# 서비스 상태 확인
sudo systemctl status planit.service
```

## 15. 유용한 명령어 모음

### Docker 관리

```bash
# 애플리케이션 중지
docker-compose -f docker-compose.prod.yml down

# 애플리케이션 시작
docker-compose -f docker-compose.prod.yml up -d

# 애플리케이션 재시작
docker-compose -f docker-compose.prod.yml restart

# 로그 확인 (실시간)
docker-compose -f docker-compose.prod.yml logs -f

# 특정 서비스 로그만 확인
docker-compose -f docker-compose.prod.yml logs -f backend

# 컨테이너 상태 확인
docker-compose -f docker-compose.prod.yml ps

# 리소스 사용량 확인
docker stats
```

### 시스템 관리

```bash
# 디스크 사용량 확인
df -h

# 메모리 사용량 확인
free -h

# 실행 중인 프로세스 확인
htop

# 네트워크 포트 확인
sudo netstat -tlnp

# Docker 디스크 정리
docker system prune -a --volumes
```

### Git 관리

```bash
# 최신 코드 가져오기
git pull origin main

# 브랜치 확인
git branch

# 상태 확인
git status

# 로그 확인
git log --oneline -10
```

## 16. 트러블슈팅

### Git 명령어를 찾을 수 없는 경우
```bash
sudo dnf install -y git
```

### Docker 권한 오류
```bash
sudo usermod -aG docker opc
newgrp docker
```

### 포트 접속 불가
```bash
# 방화벽 확인
sudo firewall-cmd --list-all

# iptables 확인
sudo iptables -L -n

# Oracle Cloud Console에서 Security List 확인
```

### 컨테이너가 시작되지 않는 경우
```bash
# 로그 확인
docker-compose -f docker-compose.prod.yml logs

# 환경 변수 확인
cat .env.prod

# 컨테이너 재생성
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d --force-recreate
```

## 완료 체크리스트

- [ ] Git 설치 완료
- [ ] Docker 설치 완료
- [ ] Docker Compose 설치 완료
- [ ] 방화벽 포트 개방 (서버)
- [ ] Oracle Cloud Security List 설정 (콘솔)
- [ ] SSH 키 설정 완료
- [ ] 프로젝트 클론 완료
- [ ] .env.prod 파일 설정 완료
- [ ] 애플리케이션 배포 완료
- [ ] 헬스 체크 성공
- [ ] 외부 접속 확인 완료

## 다음 단계

모든 설정이 완료되면:
1. GitHub에 코드를 푸시하여 자동 배포 테스트
2. `.github/GITHUB_SECRETS.md`를 참고하여 GitHub Secrets 설정
3. 자동 배포가 정상 작동하는지 확인

## 참고 문서

- **배포 가이드**: `DEPLOY-ORACLE-CLOUD.md`
- **GitHub Secrets 설정**: `.github/GITHUB_SECRETS.md`
- **CI/CD Workflow**: `.github/workflows/ci.yml`
