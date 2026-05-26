# WalkCare 데이터 흐름 및 API 시나리오

## 1. 데이터 흐름

### TFLite → 앱 (N초마다, 슬라이딩 윈도우)
```
score      보행 점수
isDanger   위험 판정 여부 (true/false)
```

### 앱 → 백엔드: POST /api/gait/sessions/{id}/data (분당 집계, 네트워크 복구 시 배치 전송)
```json
{
  "data": [
    {
      "minuteAt": "2026-05-19T09:01:00",
      "avgScore": 78.5,
      "minScore": 62.0,
      "maxScore": 91.0,
      "dangerCount": 0
    }
  ]
}
```
- `avgScore` / `minScore` / `maxScore`: 해당 분 score 집계
- `dangerCount`: 해당 분 isDanger=true 횟수
- 중복 전송 자동 무시 (ON CONFLICT DO NOTHING)
- 응답: `{ "saved": 1, "skipped": 0 }`

### 앱 → 백엔드: POST /api/gait/sessions/{id}/analysis (세션 종료 후 TFLite 분석 완료 시)
```json
{
  "riskLevel": "NORMAL",
  "avgScore": 78.5,
  "minScore": 62.0,
  "maxScore": 91.0,
  "dangerCount": 0,
  "reportSummary": "전반적으로 안정적인 보행 패턴입니다."
}
```
- `riskLevel`: NORMAL 또는 SUSPECTED (뇌졸중 의심)
- `reportSummary`: 앱/AI 팀이 생성하는 텍스트 요약, nullable

---

## 2. 사용자 시나리오별 API 흐름

### 시나리오 1: 정상 측정 흐름
```
1. 앱 실행 및 로그인
   POST /api/auth/login
   → accessToken(30분), refreshToken(30일) 발급

2. 보행 측정 시작
   POST /api/gait/sessions/start
   → sessionId, startedAt 반환
   → 앱이 SQLite에 sessionId 저장

3. 측정 중 (하루 종일)
   TFLite가 N초마다 score, isDanger 출력
   앱이 분단위로 SQLite에 누적
   네트워크 연결 시마다:
     POST /api/gait/sessions/{id}/data (배치 업로드)
     → saved, skipped 반환

4. 측정 종료 (사용자가 수동 종료)
   POST /api/gait/sessions/{id}/stop
   → status: COMPLETED

5. TFLite 전체 분석 완료 후
   POST /api/gait/sessions/{id}/analysis
   → gait_reports 저장
   → daily_scores UPSERT (당일 누적 통계 갱신)
   → dangerCount > 0이면 FCM 알림 이벤트 발행
   → dangerCount = 0이면 session_scores 7일 후 자동 삭제 예약
```

---

### 시나리오 2: 배터리 방전 후 앱 재시작
```
1. 앱 재시작
   GET /api/gait/sessions/active
   → 기존 sessionId 반환 (ACTIVE 세션 유지 중)
   → 앱이 로컬 sessionId 복원

2. 오프라인 중 SQLite에 쌓인 데이터 업로드
   POST /api/gait/sessions/{id}/data
   → 중복 데이터 자동 무시

3. 이후 정상 흐름 이어서 진행
```

---

### 시나리오 3: 하루에 세션 2번 (의도적 종료 후 재시작)
```
오전 측정
   POST /start → sessionId: 1
   POST /data (분당 업로드)
   POST /stop → COMPLETED
   POST /sessions/1/analysis

오후 재측정
   POST /start → sessionId: 2 (새 세션, 기존 ACTIVE 없으므로 생성 가능)
   POST /data (분당 업로드)
   POST /stop → COMPLETED
   POST /sessions/2/analysis

결과:
   daily_scores.session_count = 2
   daily_scores.avg_score = 두 세션 평균 재계산
```

---

### 시나리오 4: 자정 자동 종료
```
00:00 스케줄러 (SessionCleanupService) 실행
   1. 만료된 session_scores 삭제
   2. 잔여 ACTIVE 세션 → COMPLETED (endedAt = 자정)
   3. 분석 미업로드 세션의 scores 7일 후 만료 처리 (안전망)

다음날 앱 실행
   GET /active → 404 (ACTIVE 세션 없음)
   POST /start → 새 세션 생성
```

---

### 시나리오 5: 위험 감지 (dangerCount > 0)
```
POST /sessions/{id}/analysis (dangerCount > 0)
   → gait_sessions.stroke_detected = true
   → StrokeDangerEvent 발행
     → 사용자/알림 팀: FCM 보호자 알림 발송
   → session_scores.expires_at = NULL (영구 보관)
```

---

### 시나리오 6: 보호자 실시간 위치 조회
```
보호자 앱
   GET /api/locations/stream/{wardId} (SSE 구독)
   → 구독 즉시 DB 최신 위치 수신 (빈 화면 방지)
   → 이후 노약자 위치 업로드 시마다 실시간 push

노약자 앱 (백그라운드)
   POST /api/locations { latitude, longitude }
   → DB UPSERT (ward당 최신 1건 유지)
   → 구독 중인 보호자 전원에게 SSE push
```

---

### 시나리오 7: 보호자-피보호자 연결
```
노약자 앱
   POST /api/ward-link/code
   → 연결 코드 발급 (시간 제한)

보호자 앱
   POST /api/ward-link { code }
   → 연결 완료 (ward_guardian_link 생성)

연결 후
   GET /api/ward-link/wards      → 보호자가 자신의 피보호자 목록 조회
   GET /api/ward-link/guardians  → 노약자가 자신의 보호자 목록 조회
   DELETE /api/ward-link/{wardId} → 보호자가 연결 해제
```

---

### 시나리오 8: 보호자 알림 내역 조회
```
POST /sessions/{id}/analysis (dangerCount > 0)
   → FCM 알림 발송 + alerts 테이블에 STROKE_DANGER 기록

보호자 앱
   GET /api/ward-link/{wardId}/alerts
   → 해당 피보호자의 위험 알림 내역 조회 (최신순)
```

---

### 시나리오 9: 토큰 만료 처리
```
API 호출 → 401 응답
   POST /api/auth/refresh (refreshToken 전송)
   → 새 accessToken + refreshToken 발급 (Rotation)
   → 원래 요청 재시도

refreshToken도 만료된 경우
   POST /api/auth/login 재로그인
```

---

### 시나리오 10: 비밀번호 재설정
```
POST /api/auth/password-reset/request { email }
   → 이메일로 재설정 링크 발송 (비동기)
   → 기존 미사용 토큰 전체 무효화
   → 유효기간 15분

POST /api/auth/password-reset/confirm { token, newPassword }
   → 비밀번호 변경
   → 전체 RefreshToken 삭제 (강제 로그아웃)
```

---

## 3. 현재 구현된 API 목록

### 인증
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/auth/sign-up | 회원가입 |
| POST | /api/auth/login | 로그인 |
| POST | /api/auth/logout | 로그아웃 |
| POST | /api/auth/refresh | 토큰 갱신 |
| POST | /api/auth/password-reset/request | 비밀번호 재설정 요청 |
| POST | /api/auth/password-reset/confirm | 비밀번호 재설정 확인 |

### 보행 세션 (WARD 전용)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/gait/sessions/start | 세션 시작 |
| GET  | /api/gait/sessions/active | 진행 중 세션 조회 |
| POST | /api/gait/sessions/{id}/stop | 세션 종료 |
| POST | /api/gait/sessions/{id}/data | 분당 데이터 업로드 |
| POST | /api/gait/sessions/{id}/analysis | 분석 결과 업로드 |

### 사용자 (사용자/알림 팀)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET    | /api/users/me | 사용자 정보 조회 |
| PUT    | /api/users/me | 사용자 정보 수정 |
| DELETE | /api/users/me | 계정 탈퇴 |
| POST   | /api/users/device-token | FCM 토큰 등록/갱신 |
| DELETE | /api/users/device-token | FCM 토큰 삭제 |

### 피보호자 신체정보 (사용자/알림 팀, WARD 전용)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | /api/wards/me/physical-info | 신체정보 조회 |
| PUT | /api/wards/me/physical-info | 신체정보 등록/수정 |

### 보호자-피보호자 연결 (사용자/알림 팀)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST   | /api/ward-link/code | 연결 코드 발급 (WARD 전용) |
| POST   | /api/ward-link | 코드로 연결 (GUARDIAN 전용) |
| DELETE | /api/ward-link/{wardId} | 연결 해제 (GUARDIAN 전용) |
| GET    | /api/ward-link/wards | 내 피보호자 목록 (GUARDIAN 전용) |
| GET    | /api/ward-link/guardians | 내 보호자 목록 (WARD 전용) |
| GET    | /api/ward-link/{wardId}/alerts | 위험 알림 내역 조회 (GUARDIAN 전용) |

### 위치 (사용자/알림 팀)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/locations | 노약자 위치 업로드 (WARD 전용) |
| GET  | /api/locations/stream/{wardId} | 실시간 위치 SSE 구독 (GUARDIAN 전용) |
