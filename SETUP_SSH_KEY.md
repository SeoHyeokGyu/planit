# SSH 키 설정 가이드

## 현재 상황
- SSH 키 파일: `/Users/seohyeokgyu/Downloads/keys/ssh-key-2025-11-23.key`
- 서버 IP: `168.107.9.243`
- 사용자: `opc`
- 문제: 공개키가 서버에 등록되지 않아 인증 실패

## 공개키 정보
```
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC2KeafUTSawFmwUGpPpqKYjvNStCdfUkZv5tLaSQGG2MnhDU37ec5tYAksSKW4d2uXWqOyif7/78wWMM3deirILHA7Ro2s1wRNuUrgOiS/1AV7LaLiVpXu1dzbdJjPeFtvjYpbw6K+yD5Nn2EoBgSopC1IPum/GZYcLTZyRPyAcmyzc2eOYTVP7nch98x32vyPVET7PUtMnUW8e3QbKgMB8VXSYlcuhJ0TpfJqq39KwMQscSXaoca2CJsD/OMm9d04VY7QgMsGfsFnBkpyiUlou9MXObHwH+Ij0NPAuJs+02YUe1s4RiGG6Nir8+Gv2r1463LWlPIN1I8ZROvNR1Tr ssh-key-2025-11-23
```

## 해결 방법 1: Oracle Cloud Console 사용 (권장)

### 단계 1: Oracle Cloud Console에 로그인
1. https://cloud.oracle.com 접속
2. 로그인

### 단계 2: 인스턴스 콘솔 접속
1. **Compute** → **Instances** 메뉴 이동
2. 해당 인스턴스 클릭
3. **Console Connection** 섹션에서 **Launch Cloud Shell Connection** 클릭

### 단계 3: 공개키 등록
Cloud Shell에서 다음 명령어 실행:

```bash
# 1. SSH 디렉토리 생성 (이미 있을 수 있음)
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# 2. 기존 authorized_keys 백업 (안전을 위해)
if [ -f ~/.ssh/authorized_keys ]; then
    cp ~/.ssh/authorized_keys ~/.ssh/authorized_keys.backup
fi

# 3. 공개키 추가 (아래 명령어를 한 줄로 실행)
echo "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC2KeafUTSawFmwUGpPpqKYjvNStCdfUkZv5tLaSQGG2MnhDU37ec5tYAksSKW4d2uXWqOyif7/78wWMM3deirILHA7Ro2s1wRNuUrgOiS/1AV7LaLiVpXu1dzbdJjPeFtvjYpbw6K+yD5Nn2EoBgSopC1IPum/GZYcLTZyRPyAcmyzc2eOYTVP7nch98x32vyPVET7PUtMnUW8e3QbKgMB8VXSYlcuhJ0TpfJqq39KwMQscSXaoca2CJsD/OMm9d04VY7QgMsGfsFnBkpyiUlou9MXObHwH+Ij0NPAuJs+02YUe1s4RiGG6Nir8+Gv2r1463LWlPIN1I8ZROvNR1Tr ssh-key-2025-11-23" >> ~/.ssh/authorized_keys

# 4. 권한 설정
chmod 600 ~/.ssh/authorized_keys

# 5. 확인
ls -la ~/.ssh/
cat ~/.ssh/authorized_keys
```

## 해결 방법 2: 로컬에서 ssh-copy-id 사용 (비밀번호 인증이 가능한 경우)

```bash
cd /Users/seohyeokgyu/Downloads/keys
ssh-copy-id -i ssh-key-2025-11-23.key.pub opc@168.107.9.243
```

## 해결 방법 3: Oracle Cloud Console에서 직접 편집

1. Oracle Cloud Console에서 인스턴스 페이지 이동
2. **More Actions** → **Edit** 클릭
3. **Add SSH Keys** 섹션에서 위의 공개키 추가
4. 저장

## 로컬에서 SSH 연결 테스트

공개키 등록 후 다음 명령어로 테스트:

```bash
cd /Users/seohyeokgyu/Downloads/keys
ssh -v -i ssh-key-2025-11-23.key opc@168.107.9.243
```

성공하면 다음과 같이 표시됩니다:
```
debug1: Authentication succeeded (publickey).
```

## GitHub Secrets 설정

로컬 SSH 연결이 성공하면, GitHub에 다음 Secrets를 설정하세요:

### 1. ORACLE_CLOUD_SSH_KEY
```bash
# 개인키 전체 내용 복사
cat /Users/seohyeokgyu/Downloads/keys/ssh-key-2025-11-23.key
```

복사한 내용을 GitHub Secret으로 등록:
- 이름: `ORACLE_CLOUD_SSH_KEY`
- 값: 개인키 전체 내용 (BEGIN부터 END까지)

### 2. ORACLE_CLOUD_HOST
```
168.107.9.243
```

### 3. ORACLE_CLOUD_USER
```
opc
```

## GitHub Secrets 설정 방법

1. GitHub 저장소 페이지: https://github.com/[username]/planit
2. **Settings** 탭 클릭
3. 왼쪽 메뉴에서 **Secrets and variables** → **Actions** 클릭
4. **New repository secret** 버튼 클릭
5. 각 Secret을 하나씩 추가

## 주의사항

### 개인키 복사 시
- **전체 내용**을 복사해야 합니다 (줄바꿈 포함)
- `-----BEGIN RSA PRIVATE KEY-----`로 시작
- `-----END RSA PRIVATE KEY-----`로 끝남
- 중간에 공백이나 특수문자 없이 그대로 복사

### 테스트 전에
```bash
# 로컬에서 먼저 테스트
ssh -i /Users/seohyeokgyu/Downloads/keys/ssh-key-2025-11-23.key opc@168.107.9.243 "echo 'SSH connection successful!'"
```

성공하면 "SSH connection successful!" 메시지가 출력됩니다.

## 서버에 planit 저장소 클론 (SSH 연결 성공 후)

```bash
ssh -i /Users/seohyeokgyu/Downloads/keys/ssh-key-2025-11-23.key opc@168.107.9.243

# 서버에 접속한 후
cd ~
git clone https://github.com/[username]/planit.git
cd planit

# .env.prod 파일 생성 (환경 변수 설정)
nano .env.prod
```

## 체크리스트

- [ ] Oracle Cloud Console에서 공개키 등록
- [ ] 로컬에서 SSH 연결 테스트 성공
- [ ] GitHub Secret: ORACLE_CLOUD_SSH_KEY 설정
- [ ] GitHub Secret: ORACLE_CLOUD_HOST 설정 (168.107.9.243)
- [ ] GitHub Secret: ORACLE_CLOUD_USER 설정 (opc)
- [ ] 서버에 planit 저장소 클론
- [ ] 서버에 .env.prod 파일 생성
- [ ] 서버에 docker-compose.prod.yml 파일 확인
- [ ] GitHub Actions 워크플로우 실행 테스트

## 다음 단계

SSH 설정이 완료되면:
1. 로컬에서 변경사항 커밋
2. GitHub에 푸시
3. GitHub Actions 워크플로우 실행 확인
4. 배포 성공 여부 확인
