# SSH 배포 문제 해결 가이드

## 현재 오류
```
ssh: handshake failed: ssh: unable to authenticate, attempted methods [none publickey], no supported methods remain
```

## 해결 단계

### 1. SSH 키 쌍 생성

로컬 터미널에서 실행:

```bash
# Ed25519 키 생성 (권장)
ssh-keygen -t ed25519 -C "github-actions@planit" -f ~/.ssh/planit_deploy

# 또는 RSA 키 생성 (호환성이 더 좋음)
ssh-keygen -t rsa -b 4096 -C "github-actions@planit" -f ~/.ssh/planit_deploy
```

패스프레이즈는 비워두세요 (GitHub Actions가 자동으로 사용할 수 있도록).

### 2. 공개키를 Oracle Cloud 서버에 등록

```bash
# 1. 공개키 내용 확인
cat ~/.ssh/planit_deploy.pub

# 2. Oracle Cloud 서버에 접속 (현재 가능한 방법으로)
# - Oracle Cloud Console의 웹 터미널
# - 기존에 등록된 다른 SSH 키
# - 비밀번호 인증 (활성화된 경우)

# 3. 서버에서 공개키 등록
mkdir -p ~/.ssh
chmod 700 ~/.ssh
echo "여기에_공개키_붙여넣기" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# 4. 권한 확인
ls -la ~/.ssh/
# authorized_keys 파일이 600 권한이어야 함
```

### 3. GitHub Secrets 설정

1. GitHub 저장소 페이지로 이동
2. **Settings** → **Secrets and variables** → **Actions**
3. 다음 Secrets를 설정:

#### ORACLE_CLOUD_SSH_KEY
```bash
# 개인키 전체 내용 복사 (BEGIN부터 END까지 모두)
cat ~/.ssh/planit_deploy
```
복사한 내용을 Secret 값으로 입력

**주의사항:**
- 줄바꿈 포함하여 전체 내용을 복사
- `-----BEGIN OPENSSH PRIVATE KEY-----`로 시작
- `-----END OPENSSH PRIVATE KEY-----`로 끝남
- 중간에 공백이나 특수문자가 없어야 함

#### ORACLE_CLOUD_HOST
Oracle Cloud 인스턴스의 공개 IP 주소 또는 도메인
```
예: 123.456.789.012
```

#### ORACLE_CLOUD_USER
SSH 접속 사용자명
```
Ubuntu: ubuntu
Oracle Linux: opc
CentOS: centos
```

### 4. 로컬에서 SSH 연결 테스트

GitHub Actions에 적용하기 전에 로컬에서 먼저 테스트:

```bash
# SSH 연결 테스트
ssh -i ~/.ssh/planit_deploy $ORACLE_CLOUD_USER@$ORACLE_CLOUD_HOST

# 성공하면 다음 명령어 실행
whoami
hostname
pwd
exit
```

### 5. Oracle Cloud 방화벽 설정 확인

Oracle Cloud Console에서:
1. **Compute** → **Instances** → 인스턴스 선택
2. **Virtual Cloud Network** 클릭
3. **Security Lists** → **Default Security List** 클릭
4. **Ingress Rules**에서 SSH (Port 22) 허용 확인:
   - Source CIDR: `0.0.0.0/0` (모든 IP) 또는 GitHub Actions IP 범위
   - Destination Port: `22`
   - Protocol: `TCP`

### 6. 서버 SSH 설정 확인

Oracle Cloud 서버에서:

```bash
# SSH 설정 확인
sudo cat /etc/ssh/sshd_config | grep -E "PubkeyAuthentication|PasswordAuthentication"

# PubkeyAuthentication yes 여야 함
# 만약 no라면 변경:
sudo vi /etc/ssh/sshd_config
# PubkeyAuthentication yes

# SSH 서비스 재시작
sudo systemctl restart sshd
```

### 7. GitHub Actions 실행 및 디버깅

CI/CD 워크플로우가 다음 단계로 진행됩니다:
1. **Verify SSH Secrets**: Secrets가 모두 설정되었는지 확인
2. **Test SSH Connection**: 실제 SSH 연결 테스트
3. **Deploy to Oracle Cloud**: 배포 스크립트 실행

각 단계의 로그를 확인하여 어디서 실패하는지 파악하세요.

## 일반적인 문제와 해결책

### 문제: "Permission denied (publickey)"
- **원인**: 공개키가 서버에 제대로 등록되지 않음
- **해결**: `authorized_keys` 파일과 `.ssh` 디렉토리 권한 확인

### 문제: "Host key verification failed"
- **원인**: 서버의 호스트 키가 변경됨
- **해결**:
  ```yaml
  # ci.yml에 추가
  ssh-keyscan ${{ secrets.ORACLE_CLOUD_HOST }} >> ~/.ssh/known_hosts
  ```

### 문제: "Connection timeout"
- **원인**: 방화벽 또는 보안 그룹에서 SSH 차단
- **해결**: Oracle Cloud 보안 규칙에서 Port 22 허용

### 문제: SSH 키 형식 오류
- **원인**: 잘못된 키 형식 또는 줄바꿈 문제
- **해결**: 키를 다시 생성하고 복사할 때 전체 내용 확인

## 추가 디버깅

더 자세한 로그가 필요한 경우:

```bash
# 로컬에서 상세 로그와 함께 SSH 연결
ssh -vvv -i ~/.ssh/planit_deploy $ORACLE_CLOUD_USER@$ORACLE_CLOUD_HOST
```

GitHub Actions 워크플로우에서 `debug: true` 설정이 이미 활성화되어 있어 상세 로그를 볼 수 있습니다.

## 체크리스트

- [ ] SSH 키 쌍 생성 완료
- [ ] 공개키를 Oracle Cloud 서버의 `~/.ssh/authorized_keys`에 등록
- [ ] `~/.ssh/authorized_keys` 파일 권한 600 설정
- [ ] `~/.ssh` 디렉토리 권한 700 설정
- [ ] GitHub Secrets 설정:
  - [ ] ORACLE_CLOUD_SSH_KEY (개인키 전체)
  - [ ] ORACLE_CLOUD_HOST (서버 IP)
  - [ ] ORACLE_CLOUD_USER (사용자명)
- [ ] 로컬에서 SSH 연결 테스트 성공
- [ ] Oracle Cloud 방화벽에서 Port 22 허용
- [ ] 서버 SSH 설정에서 PubkeyAuthentication 활성화
- [ ] GitHub Actions 워크플로우 실행 및 로그 확인

## 참고 자료

- [GitHub Actions SSH Action 문서](https://github.com/appleboy/ssh-action)
- [Oracle Cloud 방화벽 설정](https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/securitylists.htm)
- [SSH 키 기반 인증](https://www.ssh.com/academy/ssh/public-key-authentication)
