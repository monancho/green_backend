# 알림 발송 문제 해결 가이드

## 문제: 위험 상태인 학생인데도 알림이 안 옴

### 원인
이미 위험 상태인 학생의 경우, 이전 위험도와 새 위험도가 동일하면 알림이 발송되지 않습니다.
- 예: 이전 위험도 = "CRITICAL", 새 위험도 = "CRITICAL" → 알림 안 감

### 해결 방법

#### 방법 1: DB에서 위험도 임시 변경 (테스트용)

```sql
-- 해당 학생의 위험도를 NORMAL로 변경
UPDATE ai_analysis_result_tb 
SET overall_risk = 'NORMAL' 
WHERE student_id = 2023000009;  -- 전하은 학생 ID

-- 그 다음 다시 분석 실행
POST http://localhost:8881/api/ai-analysis/analyze-all
Body: { "year": 2024, "semester": 2 }
```

#### 방법 2: 백엔드 로그 확인

분석 실행 후 백엔드 콘솔에서 다음 로그 확인:

```
위험도 분석 결과: 학생 ID=2023000009, 과목 ID=..., 이전 위험도=CRITICAL, 새 위험도=CRITICAL
위험도가 변경되지 않아 알림 발송 안 함: ...
```

또는

```
위험 알림 발송 조건 충족: 학생 ID=2023000009, 과목 ID=..., 이전 위험도=..., 새 위험도=CRITICAL
위험 알림 발송 완료: 학생=전하은, 과목=..., 위험도=CRITICAL
```

#### 방법 3: 알림 데이터 직접 확인

```sql
-- 위험 알림이 발송되었는지 확인
SELECT * FROM notification_tb 
WHERE type IN ('STUDENT_RISK_ALERT', 'PROFESSOR_RISK_ALERT')
ORDER BY created_at DESC;

-- 특정 학생의 알림 확인
SELECT * FROM notification_tb 
WHERE user_id = 2023000009  -- 학생 ID
ORDER BY created_at DESC;

-- 특정 교수의 알림 확인
SELECT * FROM notification_tb 
WHERE user_id = {교수ID}
ORDER BY created_at DESC;
```

#### 방법 4: 강제 알림 발송 (테스트용)

DB에서 직접 알림 생성:

```sql
-- 학생에게 알림
INSERT INTO notification_tb (user_id, type, message, is_read, created_at)
VALUES (
    2023000009,  -- 학생 ID
    'STUDENT_RISK_ALERT',
    '딥러닝의 기초 과목에서 심각 상태가 감지되었습니다. 상담을 받으시기 바랍니다.',
    0,
    NOW()
);

-- 교수에게 알림 (교수 ID 확인 필요)
SELECT professor_id FROM subject_tb WHERE id = {과목ID};

INSERT INTO notification_tb (user_id, type, message, is_read, created_at)
VALUES (
    {교수ID},
    'PROFESSOR_RISK_ALERT',
    '전하은 학생이 딥러닝의 기초 과목에서 심각 상태입니다. 상담이 필요합니다.',
    0,
    NOW()
);
```

## 위험도 계산 확인

```sql
-- 학생의 상세 위험도 확인
SELECT 
    ar.id,
    ar.student_id,
    s.name AS student_name,
    ar.subject_id,
    sub.name AS subject_name,
    ar.overall_risk,
    ar.attendance_status,
    ar.homework_status,
    ar.midterm_status,
    ar.final_status,
    ar.tuition_status,
    ar.counseling_status,
    ar.analyzed_at
FROM ai_analysis_result_tb ar
JOIN student_tb s ON ar.student_id = s.id
JOIN subject_tb sub ON ar.subject_id = sub.id
WHERE ar.student_id = 2023000009
ORDER BY ar.analyzed_at DESC
LIMIT 1;
```

## 빠른 테스트 체크리스트

1. [ ] 백엔드 로그에서 위험도 값 확인
2. [ ] DB에서 위험도가 실제로 RISK/CRITICAL인지 확인
3. [ ] 이전 위험도와 새 위험도가 다른지 확인
4. [ ] 알림 테이블에 데이터가 생성되었는지 확인
5. [ ] 학생/교수 ID가 올바른지 확인
6. [ ] 과목에 교수 정보가 있는지 확인

