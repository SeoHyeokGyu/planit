# GitHub Secrets 설정 가이드

GitHub Actions CI/CD 파이프라인을 위해 필요한 Secrets 설정 방법입니다.

## Secrets 설정 경로

GitHub Repository → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

## 필수 Secrets 목록

### 1. Oracle Cloud 서버 접속 정보

#### `ORACLE_CLOUD_HOST`
- **설명**: Oracle Cloud 서버의 IP 주소
- **값**: `168.107.9.243`

#### `ORACLE_CLOUD_USER`
- **설명**: SSH 접속 사용자명
- **값**: `opc`

#### `ORACLE_CLOUD_SSH_KEY`
- **설명**: Oracle Cloud 서버 접속용 SSH 개인 키 (Private Key)
- **값 생성 방법**:

```bash
# 로컬 머신에서 SSH 키 생성 (아직 없는 경우)
ssh-keygen -t rsa -b 4096 -C "github-actions@planit"

# 저장 위치: ~/.ssh/planit_deploy_key
# 비밀번호는 설정하지 않음 (Enter로 스킵)

# 공개 키를 Oracle Cloud 서버에 복사
ssh-copy-id -i ~/.ssh/planit_deploy_key.pub opc@168.107.9.243

# 또는 수동으로 복사:
cat ~/.ssh/planit_deploy_key.pub
# → 출력된 내용을 복사하여 서버의 ~/.ssh/authorized_keys에 추가
```

**개인 키 복사 방법**:
```bash
# 개인 키 내용 출력 (전체 복사)
cat ~/.ssh/planit_deploy_key

# 출력 예시:
# -----BEGIN OPENSSH PRIVATE KEY-----
# b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAACFw...
# ...
# -----END OPENSSH PRIVATE KEY-----
```

- **GitHub Secret에 설정**: 위 전체 내용(BEGIN부터 END까지)을 복사하여 `ORACLE_CLOUD_SSH_KEY`에 붙여넣기

### 2. Discord 웹훅 (기존 설정 유지)

#### `DISCORD_WEBHOOK_URL`
- **설명**: GitHub Issues 알림용 Discord 웹훅 URL
- **값**: Discord 서버에서 생성한 웹훅 URL
- **용도**: 이슈 생성/수정 시 Discord 채널에 자동 알림

## SSH 키 설정 전체 과정

### 로컬 머신에서:

```bash
# 1. SSH 키 페어 생성
ssh-keygen -t rsa -b 4096 -C "github-actions-deploy"
# 파일 경로: ~/.ssh/planit_deploy_key
# 비밀번호: (Enter - 비밀번호 없이 생성)

# 2. 공개 키 확인
cat ~/.ssh/planit_deploy_key.pub
```

### Oracle Cloud 서버에서:

```bash
# 1. 서버 접속
ssh opc@168.107.9.243

# 2. .ssh 디렉토리 확인/생성
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# 3. authorized_keys 파일에 공개 키 추가
# (로컬에서 복사한 공개 키를 붙여넣기)
nano ~/.ssh/authorized_keys
# 또는
echo "공개_키_내용" >> ~/.ssh/authorized_keys

# 4. 권한 설정
chmod 600 ~/.ssh/authorized_keys

# 5. 서버에 프로젝트 클론 (아직 안 했다면)
cd ~
git clone https://github.com/your-username/planit.git
cd planit

# 6. 환경 변수 파일 생성
cp .env.prod.example .env.prod
nano .env.prod
# 실제 값 입력 (PostgreSQL 비밀번호, JWT Secret, API Keys 등)
```

### 로컬 머신에서 테스트:

```bash
# 1. SSH 접속 테스트
ssh -i ~/.ssh/planit_deploy_key opc@168.107.9.243

# 2. 성공하면 개인 키 내용 복사
cat ~/.ssh/planit_deploy_key
# 전체 내용을 복사하여 GitHub Secret에 등록
```

## GitHub에서 Secrets 등록

1. GitHub Repository 페이지로 이동
2. **Settings** 클릭
3. 왼쪽 메뉴에서 **Secrets and variables** → **Actions** 클릭
4. **New repository secret** 버튼 클릭
5. 각 Secret을 아래와 같이 등록:

| Name | Value |
|------|-------|
| `ORACLE_CLOUD_HOST` | `168.107.9.243` |
| `ORACLE_CLOUD_USER` | `opc` |
| `ORACLE_CLOUD_SSH_KEY` | (개인 키 전체 내용) |
| `DISCORD_WEBHOOK_URL` | (기존 Discord 웹훅 URL) |

## 배포 워크플로우 동작 방식

### 자동 배포 조건:
- `main` 브랜치에 코드를 푸시할 때
- 빌드 및 테스트가 성공한 경우

### 배포 과정:
1. **Build**: Gradle로 백엔드 빌드 및 테스트
2. **Docker Test**: Docker 이미지 빌드 및 헬스 체크
3. **Deploy**: Oracle Cloud 서버에 SSH 접속
   - Git pull로 최신 코드 가져오기
   - Docker Compose로 컨테이너 재배포
   - 헬스 체크로 배포 성공 확인

### 배포 실행 예시:

```bash
# 로컬에서 코드 변경 후
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin main

# → GitHub Actions가 자동으로:
# 1. 빌드 및 테스트
# 2. Docker 이미지 빌드
# 3. Oracle Cloud 서버에 배포
# 4. 헬스 체크
```

## 트러블슈팅

### SSH 연결 실패 시:

```bash
# 1. SSH 키 권한 확인
chmod 600 ~/.ssh/planit_deploy_key

# 2. SSH 에이전트에 키 추가
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/planit_deploy_key

# 3. 상세 로그로 연결 테스트
ssh -v -i ~/.ssh/planit_deploy_key opc@168.107.9.243
```

### GitHub Actions 로그 확인:

1. Repository → **Actions** 탭
2. 실패한 워크플로우 클릭
3. **deploy** job 클릭하여 상세 로그 확인

### 서버에서 수동 배포 테스트:

```bash
# 서버에 접속
ssh opc@168.107.9.243

cd ~/planit
git pull origin main

# 환경 변수 로드
export $(cat .env.prod | xargs)

# 배포
docker-compose -f docker-compose.prod.yml up -d --build

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f
```

## 보안 참고사항

1. **SSH 키 관리**
   - GitHub Secret에 등록한 개인 키는 절대 외부 공유 금지
   - 정기적으로 SSH 키 교체 권장

2. **환경 변수 보안**
   - `.env.prod` 파일은 Git에 커밋하지 말 것 (.gitignore에 추가됨)
   - 서버에만 실제 값 저장

3. **서버 접근 제한**
   - SSH는 키 기반 인증만 사용
   - 비밀번호 인증 비활성화 권장
   - 필요시 IP 화이트리스트 설정

## 관련 파일

- **CI/CD Workflow**: `.github/workflows/ci.yml`
- **Docker Compose**: `docker-compose.prod.yml`
- **환경 변수 예시**: `.env.prod.example`
- **배포 가이드**: `DEPLOY-ORACLE-CLOUD.md`
