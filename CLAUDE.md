# Nevo 백엔드 — 팀 개발 가이드

## 프로젝트 개요

**WalkCare** — 스마트폰 IMU 센서(가속도계 + 자이로스코프)로 보행 패턴을 실시간 분석해 뇌졸중 전조 증상을 감지하는 헬스케어 앱의 REST API 백엔드.

- **프로젝트명**: nevo
- **패키지**: `com.nevo.nevo`
- **Spring Boot**: 4.0.6 / **Java**: 21
- **DB**: PostgreSQL
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

---

## 팀 역할 분담

| 역할 | 담당 도메인 |
|------|------------|
| 인증/보행 담당 | users·wards INSERT, auth, session, report, Flyway V2~V8 |
| 사용자/알림 담당 | users·wards·notification_settings·User_Device_Tokens 테이블 SQL, FCM 알림, ward_guardian_link, locations, alerts |

> **주의**: 인증/보행 담당은 users·wards SQL 작성 금지 (JPA 엔티티만 작성).
> 사용자/알림 담당은 FCM 알림을 `@EventListener(StrokeDangerEvent.class)`로 수신해 처리.

---

## 로컬 개발 환경 설정

### 필수 환경변수

| 변수 | 설명 | 예시 |
|------|------|------|
| `JWT_SECRET` | JWT 서명 키 (32자 이상) | `my-secret-key-for-local-dev-only` |
| `MAIL_USERNAME` | 발송자 Gmail 계정 | `noreply@gmail.com` |
| `MAIL_PASSWORD` | Gmail 앱 비밀번호 16자리 | Google 계정 → 보안 → 앱 비밀번호 |

> DB 설정(`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`)은 기본값이 있어 로컬 PostgreSQL 기본 설정 그대로 사용 가능.

### 로컬 DB 기본값
```
host: localhost / port: 5432 / db: nevo
username: nevo / password: nevo_backend
```

### 실행 방법
```bash
./gradlew bootRun
```

---

## 전체 테이블 목록

| 테이블 | 담당 |
|--------|------|
| users | 사용자/알림 담당 (SQL 작성) |
| wards | 사용자/알림 담당 (SQL 작성) |
| notification_settings | 사용자/알림 담당 |
| User_Device_Tokens | 사용자/알림 담당 |
| ward_guardian_link | 사용자/알림 담당 |
| locations | 사용자/알림 담당 |
| alerts | 사용자/알림 담당 |
| refresh_tokens | 인증/보행 담당 (V2) |
| password_reset_tokens | 인증/보행 담당 (V3) |
| consents | 인증/보행 담당 (V4) |
| gait_sessions | 인증/보행 담당 (V5) |
| session_scores | 인증/보행 담당 (V6) |
| gait_reports | 인증/보행 담당 (V7) |
| daily_scores | 인증/보행 담당 (V8) |

---

## 전체 API 목록

### 인증 — public (JWT 불필요) / 담당: 인증/보행

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/auth/sign-up | 회원가입 |
| POST | /api/auth/login | 로그인 |
| POST | /api/auth/logout | 로그아웃 |
| POST | /api/auth/refresh | 토큰 갱신 |
| POST | /api/auth/password-reset/request | 비밀번호 재설정 요청 |
| POST | /api/auth/password-reset/confirm | 비밀번호 재설정 확인 |

### 보행 — JWT 필요 / 담당: 인증/보행

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | /api/gait/sessions/start | 보행 측정 시작 |
| POST | /api/gait/sessions/{sessionId}/data | 보행 데이터 전송 |
| POST | /api/gait/sessions/{sessionId}/stop | 보행 측정 종료 |
| GET  | /api/gait/sessions/active | 진행 중인 세션 조회 |
| POST | /api/gait/sessions/{sessionId}/analysis | 분석 결과 업로드 |

### 리포트 — JWT 필요 / 담당: 인증/보행

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | /api/gait/reports/{sessionId} | 단건 세션 리포트 조회 |
| GET | /api/gait/reports/weekly | 주간 보행 통계 조회 |

---

## 패키지 구조

```
com.nevo.nevo/
├── auth/
│   ├── controller/       ← AuthController
│   ├── service/          ← AuthService, PasswordResetService
│   ├── dto/              ← AuthRequest, AuthResponse (내부 record로 구분)
│   ├── entity/           ← RefreshToken, PasswordResetToken
│   ├── repository/       ← RefreshTokenRepository, PasswordResetTokenRepository
│   ├── jwt/              ← JwtUtil, JwtAuthenticationFilter, JwtAuthentication
│   └── exception/code/   ← AuthErrorCode, AuthSuccessCode
│
├── session/
│   ├── controller/       ← SessionController
│   ├── service/          ← SessionService
│   ├── dto/              ← SessionRequest, SessionResponse
│   ├── entity/           ← GaitSession, SessionScore
│   ├── repository/       ← GaitSessionRepository, SessionScoreRepository
│   ├── event/            ← StrokeDangerEvent
│   └── exception/code/   ← SessionErrorCode, SessionSuccessCode
│
├── report/
│   ├── controller/       ← ReportController
│   ├── service/          ← ReportService
│   ├── dto/              ← ReportResponse
│   ├── entity/           ← GaitReport, DailyScore
│   ├── repository/       ← GaitReportRepository, DailyScoreRepository
│   └── exception/code/   ← ReportErrorCode, ReportSuccessCode
│
├── user/
│   ├── entity/           ← User(변경예정), Ward, Consent, ConsentType, Role
│   ├── repository/       ← UserRepository, WardRepository, ConsentRepository
│   └── exception/code/   ← UserErrorCode, UserSuccessCode
│
└── global/
    ├── config/           ← SecurityConfig, SwaggerConfig
    ├── entity/           ← BaseEntity (createdAt, updatedAt)
    ├── util/             ← SecurityUtil
    └── exception/
        ├── CustomException, ErrorResponse, SuccessResponse, GlobalExceptionHandler
        └── code/         ← ErrorCode(인터페이스), SuccessCode(인터페이스), GlobalErrorCode
```

---

## 코딩 컨벤션

### DTO 구조
- **Request**: 하나의 클래스에 기능별 `record`로 묶어서 관리
  ```java
  // AuthRequest.java
  public class AuthRequest {
      public record SignUp(...) {}
      public record Login(...) {}
  }
  ```
- **Response**: 동일 방식
  ```java
  // AuthResponse.java
  public class AuthResponse {
      public record SignUp(String accessToken, String refreshToken, String role) {}
      public record Login(String accessToken, String refreshToken, String role) {}
  }
  ```

### 에러/성공 코드 형식
- **형식**: 도메인 + HTTP 상태코드 (같은 상태코드 중복 시 숫자 추가)
- **에러 예시**: `AUTH409`, `AUTH4011`, `AUTH4012`, `SESSION404`
- **성공 예시**: `AUTH201`, `AUTH200`, `SESSION200`

| 도메인 | 에러코드 위치 | 성공코드 위치 |
|--------|-------------|-------------|
| 공통 | `global/exception/code/GlobalErrorCode` | — |
| 인증 | `auth/exception/code/AuthErrorCode` | `auth/exception/code/AuthSuccessCode` |
| 보행 | `session/exception/code/SessionErrorCode` | `session/exception/code/SessionSuccessCode` |
| 리포트 | `report/exception/code/ReportErrorCode` | `report/exception/code/ReportSuccessCode` |
| 사용자 | `user/exception/code/UserErrorCode` | `user/exception/code/UserSuccessCode` |

### 응답 형식
```json
// 에러
{ "code": "AUTH409", "message": "이미 사용 중인 이메일입니다.", "timestamp": "ISO-8601" }

// 성공
{ "code": "AUTH201", "message": "회원가입에 성공했습니다.", "timestamp": "ISO-8601", "data": { ... } }
```

---

## 핵심 설계 결정

- **JWT payload**: `{ userId, wardId, role }` — GUARDIAN은 `wardId: null`
- **보행 API**: JWT에서 wardId 직접 추출 (추가 DB 조회 없음)
- **세션 데이터 보관**:
  - 위험 세션 `session_scores.expires_at = NULL` (영구 보관)
  - 정상 세션 `session_scores.expires_at = NOW() + 7일`
  - 매일 새벽 3시 만료 데이터 자동 삭제 (`@Scheduled`)
- **세션 종료 시**: 서버가 `daily_scores` UPSERT 자동 처리
- **위험 감지 이벤트**: 인증/보행 담당이 `StrokeDangerEvent` 발행 → 사용자/알림 담당이 `@EventListener`로 수신 후 FCM 처리
