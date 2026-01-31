-- ============================================================================
-- 사용자 테스트 데이터 INSERT 쿼리
-- 총 10개: 다양한 사용자 더미 데이터
-- ============================================================================
--
-- 사용 방법:
-- 1. 이 SQL 파일 전체를 선택 (Ctrl+A 또는 Cmd+A)
-- 2. 실행 (Ctrl+Enter 또는 Cmd+Enter)
-- 3. 모든 INSERT 문이 실행된 후 마지막 SELECT로 결과 확인
--
-- 주의:
-- - 모든 사용자의 비밀번호는 "password"로 통일 (BCrypt 해시값)
-- - BCrypt 해시: $2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy
-- ============================================================================

-- BCrypt 해시된 비밀번호 "password"
-- $2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy

-- 1. 기본 사용자
INSERT INTO users (id, login_id, password, nickname, total_point, created_at, updated_at, created_by, last_modified_by)
VALUES (
    3,
    'minsu_kim',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy',
    '플래닛러',
    1000,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
);

-- 2. 테스트 사용자 1
INSERT INTO users (id, login_id, password, nickname, total_point, created_at, updated_at, created_by, last_modified_by)
VALUES (
    4,
    'jieun2024',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy',
    '챌린지마스터',
    2500,
    NOW() - INTERVAL '30 days',
    NOW() - INTERVAL '1 day',
    'SYSTEM',
    'SYSTEM'
);

-- 3. 테스트 사용자 2
INSERT INTO users (id, login_id, password, nickname, total_point, created_at, updated_at, created_by, last_modified_by)
VALUES (
    5,
    'workout_king',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy',
    '운동왕',
    3200,
    NOW() - INTERVAL '60 days',
    NOW() - INTERVAL '2 hours',
    'SYSTEM',
    'SYSTEM'
);

-- 4. 테스트 사용자 3
INSERT INTO users (id, login_id, password, nickname, total_point, created_at, updated_at, created_by, last_modified_by)
VALUES (
    6,
    'booklover92',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy',
    '독서광',
    1800,
    NOW() - INTERVAL '45 days',
    NOW() - INTERVAL '5 hours',
    'SYSTEM',
    'SYSTEM'
);

-- 5. 테스트 사용자 4
INSERT INTO users (id, login_id, password, nickname, total_point, created_at, updated_at, created_by, last_modified_by)
VALUES (
    7,
    'early_bird',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy',
    '아침형인간',
    2100,
    NOW() - INTERVAL '20 days',
    NOW() - INTERVAL '3 days',
    'SYSTEM',
    'SYSTEM'
);

-- 6. 테스트 사용자 5
INSERT INTO users (id, login_id, password, nickname, total_point, created_at, updated_at, created_by, last_modified_by)
VALUES (
    8,
    'healthkeeper',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy',
    '헬스키퍼',
    4500,
    NOW() - INTERVAL '90 days',
    NOW() - INTERVAL '30 minutes',
    'SYSTEM',
    'SYSTEM'
);

-- 7. 테스트 사용자 6
INSERT INTO users (id, login_id, password, nickname, total_point, created_at, updated_at, created_by, last_modified_by)
VALUES (
    9,
    'dev_master',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy',
    '코딩좋아',
    5000,
    NOW() - INTERVAL '120 days',
    NOW() - INTERVAL '1 hour',
    'SYSTEM',
    'SYSTEM'
);

-- 8. 테스트 사용자 7
INSERT INTO users (id, login_id, password, nickname, total_point, created_at, updated_at, created_by, last_modified_by)
VALUES (
    10,
    'habit_builder',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy',
    '습관형성중',
    1500,
    NOW() - INTERVAL '15 days',
    NOW() - INTERVAL '6 hours',
    'SYSTEM',
    'SYSTEM'
);

-- 9. 테스트 사용자 8
INSERT INTO users (id, login_id, password, nickname, total_point, created_at, updated_at, created_by, last_modified_by)
VALUES (
    11,
    'seojun_lee',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy',
    '성장중',
    800,
    NOW() - INTERVAL '7 days',
    NOW() - INTERVAL '2 days',
    'SYSTEM',
    'SYSTEM'
);

-- 10. 테스트 사용자 9
INSERT INTO users (id, login_id, password, nickname, total_point, created_at, updated_at, created_by, last_modified_by)
VALUES (
    12,
    'consistent_one',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy',
    '꾸준함',
    3800,
    NOW() - INTERVAL '100 days',
    NOW() - INTERVAL '4 hours',
    'SYSTEM',
    'SYSTEM'
);

-- 11. 테스트 사용자 10 (닉네임 없음)
INSERT INTO users (id, login_id, password, nickname, total_point, created_at, updated_at, created_by, last_modified_by)
VALUES (
    13,
    'newbie2026',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRQAuBWTRdpC6QaXJAhXO6jWLy',
    NULL,
    500,
    NOW() - INTERVAL '3 days',
    NOW() - INTERVAL '1 day',
    'SYSTEM',
    'SYSTEM'
);

-- 결과 확인
SELECT
    id,
    login_id,
    nickname,
    total_point,
    created_at,
    updated_at
FROM users
ORDER BY total_point DESC;

-- 사용자 수 확인
SELECT COUNT(*) as total_users FROM users;

-- 포인트 통계
SELECT
    AVG(total_point) as avg_point,
    MAX(total_point) as max_point,
    MIN(total_point) as min_point
FROM users;
