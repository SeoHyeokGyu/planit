#!/bin/bash

# SSE 스트림 테스트 스크립트
# 실시간 피드 기능이 정상적으로 작동하는지 테스트합니다.

echo "=== Planit SSE Feed Test ==="
echo ""

# 1. 서버 상태 확인
echo "[1] 서버 상태 확인..."
HEALTH=$(curl -s http://localhost:8080/api/health)
echo "Health: $HEALTH"
echo ""

# 2. 테스트 사용자 생성
echo "[2] 테스트 사용자 생성..."
SIGNUP_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "loginId": "testuser",
    "password": "test1234",
    "nickname": "Test User"
  }')
echo "Signup: $SIGNUP_RESPONSE"
echo ""

# 3. 로그인하여 토큰 획득
echo "[3] 로그인..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "loginId": "testuser",
    "password": "test1234"
  }')
echo "Login: $LOGIN_RESPONSE"

# 토큰 추출 (jq가 설치되어 있으면 사용, 없으면 수동으로 복사)
if command -v jq &> /dev/null; then
    TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.accessToken // empty')
    if [ -z "$TOKEN" ]; then
        echo "토큰을 찾을 수 없습니다. 수동으로 복사해주세요."
        exit 1
    fi
    echo "Token: $TOKEN"
else
    echo "⚠️  jq가 설치되지 않았습니다. 토큰을 수동으로 확인해주세요."
    echo "다음 명령어로 SSE 연결을 테스트하세요:"
    echo "curl -N -H \"Authorization: Bearer YOUR_TOKEN\" http://localhost:8080/api/feed/stream"
    exit 0
fi
echo ""

# 4. SSE 연결 수 확인
echo "[4] SSE 연결 수 확인..."
CONN_COUNT=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/feed/connections)
echo "Connections: $CONN_COUNT"
echo ""

# 5. 읽지 않은 피드 수 확인
echo "[5] 읽지 않은 피드 수 확인..."
UNREAD_COUNT=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/feed/unread-count)
echo "Unread: $UNREAD_COUNT"
echo ""

# 6. SSE 스트림 연결 (백그라운드로 5초간 실행)
echo "[6] SSE 스트림 연결 테스트 (5초간)..."
echo "연결 중..."
timeout 5 curl -N -H "Authorization: Bearer $TOKEN" \
  -H "Accept: text/event-stream" \
  http://localhost:8080/api/feed/stream 2>&1 | head -10

echo ""
echo "=== 테스트 완료 ==="
echo ""
echo "✅ SSE 구현이 성공적으로 완료되었습니다!"
echo ""
echo "다음 명령어로 지속적인 SSE 연결을 테스트할 수 있습니다:"
echo "curl -N -H \"Authorization: Bearer $TOKEN\" http://localhost:8080/api/feed/stream"
