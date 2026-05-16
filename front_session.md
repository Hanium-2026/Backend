# 보행 세션 API — 앱 연동 가이드

## 기본 정보

- **Base URL**: `http://localhost:8080` (운영 도메인으로 교체)
- **인증**: 모든 세션 API는 `Authorization: Bearer {accessToken}` 헤더 필수
- **대상**: WARD 전용 — GUARDIAN JWT로 호출 시 403

---

## 점수 계산 책임

**앱이 직접 계산해서 백엔드로 전송.**
```
TFLite 모델
  └─ 2초 슬라이딩 윈도우 → 초당 score 출력

앱 (1분마다 집계)
  └─ 해당 분의 score 목록 → avg / min / max / dangerCount 계산
  └─ SQLite에 저장 (오프라인 대비)
  └─ 네트워크 연결 시 백엔드로 업로드
```

| 필드 | 단위 | 설명 |
|------|------|------|
| `avgScore` | Float | 해당 분 score 평균 |
| `minScore` | Float | 해당 분 score 최저 |
| `maxScore` | Float | 해당 분 score 최고 |
| `dangerCount` | Integer | 해당 분 위험 판정 횟수 |

---

## SQLite 로컬 저장 구조 (권장)

```
sessions 테이블
  - sessionId      ← 서버에서 받은 값, 앱 재시작 복원용
  - startedAt
  - status         (ACTIVE / COMPLETED)

pending_scores 테이블  ← 미업로드 데이터 보관
  - sessionId
  - minuteAt
  - avgScore
  - minScore
  - maxScore
  - dangerCount
  - uploaded       (boolean)
```

---

## 전체 앱 흐름

```
앱 실행
  └─ GET /active
       ├─ 200 ACTIVE → 기존 sessionId 복원 → 미업로드 데이터 배치 전송 후 재개
       └─ 404 없음   → 시작 버튼 활성화

시작 버튼 클릭
  └─ POST /start → sessionId, startedAt → SQLite 저장

측정 중 (백그라운드)
  └─ TFLite 분석 → 분당 avg/min/max/dangerCount 계산 → SQLite 저장
  └─ 네트워크 연결 시마다 POST /data 배치 업로드

종료 버튼 클릭
  └─ POST /stop
  └─ POST /data (마지막 잔여 데이터)
  └─ AI 분석 완료 후 POST /analysis

매일 00시 (백엔드 자동)
  └─ 남아있는 ACTIVE 세션 자동 COMPLETED 처리
```

---

## 시나리오별 세션 연속성 처리

### ① 정상 사용
```
/start → 측정 → /data 주기 업로드 → /stop → /data(잔여) → /analysis
```

### ② 배터리 방전 / 강제 종료
```
앱 재시작
  └─ GET /active
       ├─ 200 ACTIVE → 기존 sessionId 그대로 사용
       │    └─ SQLite 미업로드 데이터 → /data 배치 전송 후 재개
       └─ 404 → 00시 자동 종료된 경우
                └─ /start로 새 세션 시작
```

### ③ 네트워크 끊김
```
측정 중 네트워크 끊김
  └─ SQLite에 분당 데이터 계속 저장
  └─ 업로드 실패 데이터 pending_scores에 보관

네트워크 복구
  └─ pending_scores 전체 → /data 한 번에 배치 전송
  └─ 중복 전송해도 안전 (백엔드가 자동 무시)
```

### ④ 사용자가 직접 종료 후 재시작
```
/stop → 세션 COMPLETED
재시작 버튼 → GET /active → 404
  └─ /start → 새 sessionId 발급 (새 세션)
```

---

## /active vs SQLite sessionId 우선순위

**항상 서버(/active) 우선 확인.**

```
앱 시작 시 GET /active 호출
  ├─ 200 → 서버 sessionId 신뢰 → 로컬 SQLite 덮어쓰기
  └─ 404 → 시작 버튼 표시
```

> 로컬 SQLite에만 의존하면 안 되는 이유:
> 00시 자동 종료 후에도 로컬에 sessionId가 남아있을 수 있어 서버 상태를 항상 먼저 확인해야 함.

---

## API 상세

### 세션 시작
```
POST /api/gait/sessions/start
Body: 없음

Response 201:
{
  "sessionId": 1,
  "startedAt": "2026-05-17T03:00:06"
}
```

| 에러 코드 | 상황 |
|----------|------|
| SESSION409 | 이미 ACTIVE 세션 존재 → /active로 기존 세션 복원 |
| SESSION403 | GUARDIAN 계정으로 호출 |

---

### 진행 중 세션 조회
```
GET /api/gait/sessions/active
Body: 없음

Response 200:
{
  "sessionId": 1,
  "startedAt": "2026-05-17T03:00:06"
}
```

| 에러 코드 | 상황 |
|----------|------|
| SESSION404 | 진행 중인 세션 없음 → 시작 버튼 표시 |

---

### 분당 보행 데이터 업로드
```
POST /api/gait/sessions/{sessionId}/data

Body:
{
  "data": [
    {
      "minuteAt": "2026-05-17T03:01:00",
      "avgScore": 75.5,
      "minScore": 60.0,
      "maxScore": 90.0,
      "dangerCount": 1
    },
    {
      "minuteAt": "2026-05-17T03:02:00",
      "avgScore": 80.0,
      "minScore": 70.0,
      "maxScore": 95.0,
      "dangerCount": 0
    }
  ]
}

Response 200:
{
  "saved": 2,
  "skipped": 0
}
```

**규칙**:
- `minuteAt`은 `yyyy-MM-ddTHH:mm:ss` 형식, 분 단위로 전송 권장
  - `03:01:47` 전송해도 서버에서 `03:01:00`으로 처리
- 세션 시작 이전 시각 / 미래 시각 데이터는 서버에서 자동 필터링 (skipped 처리)
- **중복 전송 안전**: 같은 minuteAt 재전송해도 중복 저장 안 됨
- COMPLETED 세션에도 업로드 가능 (오프라인 중 쌓인 데이터 뒤늦게 전송)

---

### 세션 종료
```
POST /api/gait/sessions/{sessionId}/stop
Body: 없음

Response 200 (이미 종료된 세션도 200 반환 — 멱등)
```

---

### 분석 결과 업로드
```
POST /api/gait/sessions/{sessionId}/analysis

Body:
{
  "riskLevel": "NORMAL",      ← "NORMAL" 또는 "SUSPECTED" 대문자 정확히
  "avgScore": 77.5,           ← 세션 전체 평균
  "minScore": 60.0,           ← 세션 전체 최저
  "maxScore": 95.0,           ← 세션 전체 최고
  "dangerCount": 1,           ← 세션 전체 위험 횟수 합산, 0 이상 필수
  "reportSummary": "보행 패턴 정상"  ← AI 팀이 생성, 없으면 null
}

Response 200 (중복 업로드도 200 반환 — 멱등)
```

> `dangerCount > 0`이면 보호자에게 FCM 알림 자동 발송됨.

---

## 에러 코드

| 코드 | HTTP | 상황 | 앱 처리 |
|------|------|------|--------|
| SESSION403 | 403 | GUARDIAN 계정 또는 타인 세션 접근 | 접근 불가 안내 |
| SESSION404 | 404 | 세션 없음 또는 진행 중 세션 없음 | 시작 버튼 표시 |
| SESSION409 | 409 | ACTIVE 세션 중복 생성 시도 | /active로 기존 세션 복원 |

---

## 권장 업로드 주기

| 상황 | 업로드 시점 |
|------|------------|
| 네트워크 정상 | 매 분 또는 5분 간격 실시간 업로드 |
| 네트워크 복구 | SQLite 미업로드 전체 즉시 배치 전송 |
| 세션 종료 직후 | 잔여 데이터 즉시 전송 후 /analysis 호출 |
