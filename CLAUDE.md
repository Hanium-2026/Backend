# Nevo 백엔드 — Claude 참고 문서

## 프로젝트 개요

WalkCare — 스마트폰 IMU 센서로 보행 패턴을 분석해 뇌졸중 전조 증상을 감지하는 헬스케어 앱의 REST API 백엔드.

- **프로젝트명**: nevo
- **패키지**: com.nevo.nevo
- **Spring Boot**: 4.0.6 / **Java**: 21

---

## ⚠️ 담당 범위

### 내가 구현
- 테이블 7개 (Flyway SQL)
- API 13개

### 절대 건드리지 말 것 (다른 팀원 담당)
- users, wards, notification_settings, User_Device_Tokens 테이블 SQL
- ward_guardian_link, locations, alerts 테이블
- FCM 알림 발송 로직 (StrokeDangerEvent 발행만 하고 구현 안 함)

### 변경 예정 (다른 팀원)
- User 엔티티: 현재 임시 상태, 팀원이 ERD 기준으로 수정 예정

---

## 기술 스택

- Java 21, Spring Boot 4.0.6, Spring Data JPA, PostgreSQL
- Flyway (ddl-auto=validate)
- Spring Security + jjwt 0.12 (JWT Access 1h / Refresh 30d, Rotation)
- Spring Boot Starter Mail (비밀번호 재설정)
- Springdoc OpenAPI 2 (Swagger UI: /swagger-ui/index.html)
- Gradle Groovy DSL, JUnit 5 + Mockito

---

## 패키지 구조

```
com.nevo.nevo/
├── auth/
│   ├── controller/, service/, dto/, entity/, repository/, jwt/
│   └── exception/code/   ← AuthErrorCode, AuthSuccessCode
├── session/
│   ├── controller/, service/, dto/, entity/, repository/, event/
│   └── exception/code/   ← SessionErrorCode, SessionSuccessCode
├── report/
│   ├── controller/, service/, dto/, entity/, repository/
│   └── exception/code/   ← ReportErrorCode, ReportSuccessCode
├── user/
│   ├── entity/(User, Ward, Consent), repository/
│   └── exception/code/   ← UserErrorCode, UserSuccessCode
└── global/
    ├── config/           ← SecurityConfig, SwaggerConfig
    ├── entity/           ← BaseEntity
    ├── util/             ← SecurityUtil
    └── exception/
        ├── CustomException, ErrorResponse, SuccessResponse, GlobalExceptionHandler
        └── code/         ← ErrorCode(인터페이스), SuccessCode(인터페이스), GlobalErrorCode
```

---

## 에러/성공 코드 형식

- 에러: 도메인 + HTTP 상태코드 (중복 시 끝에 숫자)
  - 예: `AUTH409`, `AUTH4011`, `AUTH4012`, `AUTH4013`
- 성공: 동일 형식
  - 예: `AUTH200`, `AUTH201`, `SESSION200`

### GlobalErrorCode (COMMON)
| 상수 | 코드 | 상태 |
|------|------|------|
| BAD_REQUEST | COMMON400 | 400 |
| UNAUTHORIZED | COMMON401 | 401 |
| FORBIDDEN | COMMON403 | 403 |
| NOT_FOUND | COMMON404 | 404 |
| INTERNAL_SERVER_ERROR | COMMON500 | 500 |

### AuthErrorCode
| 상수 | 코드 | 상태 |
|------|------|------|
| INVALID_RESET_TOKEN | AUTH400 | 400 |
| INVALID_CREDENTIALS | AUTH4011 | 401 |
| INVALID_TOKEN | AUTH4012 | 401 |
| EXPIRED_TOKEN | AUTH4013 | 401 |
| EMAIL_DUPLICATED | AUTH409 | 409 |

### SessionErrorCode
| 상수 | 코드 | 상태 |
|------|------|------|
| SESSION_FORBIDDEN | SESSION403 | 403 |
| SESSION_NOT_FOUND | SESSION404 | 404 |
| SESSION_ALREADY_ACTIVE | SESSION409 | 409 |

### UserErrorCode
| 상수 | 코드 | 상태 |
|------|------|------|
| WARD_NOT_FOUND | USER404 | 404 |

---

## 내가 만드는 테이블 7개

1. **refresh_tokens** — token_hash(SHA-256), device_id, revoked, user_id FK
2. **password_reset_tokens** — token_hash, expires_at(30분), used, user_id FK
3. **consents** — consent_type_enum(TERMS/PRIVACY/SMS/MEDICAL), agreed, user_id FK
4. **gait_sessions** — ward_id FK, stroke_detected, status(ACTIVE/COMPLETED/FAILED)
5. **session_scores** — minute_at, avg_score, min_score, danger_count, expires_at, session_id FK
6. **gait_reports** — risk_level(NORMAL/SUSPECTED), variability_score, asymmetry_score, session_id UNIQUE FK, ward_id FK
7. **daily_scores** — date, avg/min/max_score, session_count, ward_id FK + UNIQUE(ward_id, date)

---

## API 13개

### 인증 (public)
| 메서드 | 경로 |
|--------|------|
| POST | /api/auth/sign-up |
| POST | /api/auth/login |
| POST | /api/auth/logout |
| POST | /api/auth/refresh |
| POST | /api/auth/password-reset/request |
| POST | /api/auth/password-reset/confirm |

### 보행 (JWT 필요)
| 메서드 | 경로 |
|--------|------|
| POST | /api/gait/sessions/start |
| POST | /api/gait/sessions/{sessionId}/data |
| POST | /api/gait/sessions/{sessionId}/stop |
| GET  | /api/gait/sessions/active |
| POST | /api/gait/sessions/{sessionId}/analysis |

### 리포트 (JWT 필요)
| 메서드 | 경로 |
|--------|------|
| GET | /api/gait/reports/{sessionId} |
| GET | /api/gait/reports/weekly |

---

## 핵심 설계 결정

- JWT payload: `{ userId, wardId, role }` — GUARDIAN은 wardId: null
- 보행 API는 JWT에서 wardId 직접 추출 (추가 DB 조회 없음)
- 위험 세션 session_scores: `expires_at = NULL` (영구 보관)
- 정상 세션 session_scores: `expires_at = NOW() + 7일`
- `@Scheduled(cron = "0 0 3 * * *")` 매일 새벽 3시 만료 데이터 삭제
- 세션 종료(stop) 시 서버가 daily_scores UPSERT 자동 처리
- 위험 감지 시 `StrokeDangerEvent` 발행만 — FCM은 다른 팀원이 `@EventListener`로 처리

---

## 에러 응답 형식

```json
{ "code": "AUTH409", "message": "이미 사용 중인 이메일입니다.", "timestamp": "ISO-8601" }
```
