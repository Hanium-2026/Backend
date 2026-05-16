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
| 인증/보행 담당 | auth, session, report, Flyway V4 이후 |
| 사용자/알림 담당 | users·wards·notification_settings·User_Device_Tokens 테이블 SQL, FCM 알림, ward_guardian_link, locations, alerts, Flyway V1~V3 |

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

| 테이블 | Flyway | 담당 |
|--------|--------|------|
| users | V1 | 사용자/알림 담당 (SQL 작성) |
| wards | V2 | 사용자/알림 담당 (SQL 작성) |
| wards user_id FK 추가 | V3 | 사용자/알림 담당 |
| users.fcm_token | V1 포함 | 사용자/알림 담당 (단일 기기 FCM 토큰, 별도 테이블 없음) |
| notification_settings | — | 사용자/알림 담당 |
| ward_guardian_link | — | 사용자/알림 담당 |
| locations | V9 | 사용자/알림 담당 |
| locations ward_id UNIQUE 제약 | V10 | 사용자/알림 담당 |
| alerts | — | 사용자/알림 담당 |
| refresh_tokens | V4 | 인증/보행 담당 |
| password_reset_tokens | V5 | 인증/보행 담당 |
| consents | V6 | 인증/보행 담당 |
| refresh_tokens token_hash 인덱스 | V7 | 인증/보행 담당 |
| refresh_tokens (user_id, device_id) 복합 인덱스 | V8 | 인증/보행 담당 |
| password_reset_tokens token_hash 인덱스 | V11 | 인증/보행 담당 |
| gait_sessions | V12 | 인증/보행 담당 |
| session_scores | V13 | 인증/보행 담당 |
| gait_reports | V14 | 인증/보행 담당 |
| daily_scores | V15 | 인증/보행 담당 |

---

## 전체 API 목록

### 인증 — public (JWT 불필요) / 담당: 인증/보행

| 메서드 | 경로 | 설명 | 구현 |
|--------|------|------|------|
| POST | /api/auth/sign-up | 회원가입 | ✅ |
| POST | /api/auth/login | 로그인 | ✅ |
| POST | /api/auth/logout | 로그아웃 | ✅ |
| POST | /api/auth/refresh | 토큰 갱신 | ✅ |
| POST | /api/auth/password-reset/request | 비밀번호 재설정 요청 | ✅ |
| POST | /api/auth/password-reset/confirm | 비밀번호 재설정 확인 | ✅ |

### 보행 — JWT 필요 / 담당: 인증/보행

| 메서드 | 경로 | 설명 | 구현 |
|--------|------|------|------|
| POST | /api/gait/sessions/start | 보행 측정 시작 | ✅ |
| POST | /api/gait/sessions/{sessionId}/data | 보행 데이터 전송 | ✅ |
| POST | /api/gait/sessions/{sessionId}/stop | 보행 측정 종료 | ✅ |
| GET  | /api/gait/sessions/active | 진행 중인 세션 조회 | ✅ |
| POST | /api/gait/sessions/{sessionId}/analysis | 분석 결과 업로드 | ✅ |

### 리포트 — JWT 필요 / 담당: 인증/보행

| 메서드 | 경로 | 설명 | 구현 |
|--------|------|------|------|
| GET | /api/gait/reports/{sessionId} | 단건 세션 리포트 조회 | 미구현 |
| GET | /api/gait/reports/weekly | 주간 보행 통계 조회 | 미구현 |

### 사용자 — JWT 필요 / 담당: 사용자/알림

| 메서드 | 경로 | 설명 | 구현 |
|--------|------|------|------|
| GET    | /api/users/me | 사용자 정보 조회 | ✅ |
| PUT    | /api/users/me | 사용자 정보 수정 | ✅ |
| DELETE | /api/users/me | 계정 탈퇴 | ✅ |
| POST   | /api/users/device-token | FCM 토큰 등록/갱신 | ✅ |
| DELETE | /api/users/device-token | FCM 토큰 삭제 (로그아웃 시 호출) | ✅ |

### 위치 — JWT 필요 / 담당: 사용자/알림

| 메서드 | 경로 | 설명 | 구현 |
|--------|------|------|------|
| POST | /api/locations | 노약자 현재 위치 업로드 (WARD 전용) | ✅ |
| GET  | /api/locations/stream/{wardId} | 실시간 위치 SSE 구독 (GUARDIAN 전용) | ✅ |

---

## 패키지 구조

```
com.nevo.nevo/
├── auth/
│   ├── controller/       ← AuthController
│   ├── service/          ← AuthService, MailService, PasswordResetTokenCleanupService
│   ├── dto/
│   │   ├── request/      ← AuthRequest (SignUp, Login, PasswordResetRequest, PasswordResetConfirm ... record)
│   │   └── response/     ← AuthResponse (SignUp, Login, ... record)
│   ├── entity/           ← RefreshToken, PasswordResetToken
│   ├── repository/       ← RefreshTokenRepository, PasswordResetTokenRepository
│   ├── jwt/              ← JwtUtil, JwtAuthenticationFilter, JwtAuthentication, JwtAuthenticationEntryPoint
│   └── exception/code/   ← AuthErrorCode, AuthSuccessCode
│
├── session/
│   ├── controller/       ← SessionController
│   ├── service/          ← SessionService
│   ├── dto/
│   │   ├── request/      ← SessionRequest
│   │   └── response/     ← SessionResponse
│   ├── entity/           ← GaitSession, SessionScore
│   ├── repository/       ← GaitSessionRepository, SessionScoreRepository
│   ├── event/            ← StrokeDangerEvent
│   └── exception/code/   ← SessionErrorCode, SessionSuccessCode
│
├── report/
│   ├── controller/       ← ReportController
│   ├── service/          ← ReportService
│   ├── dto/
│   │   └── response/     ← ReportResponse
│   ├── entity/           ← GaitReport, DailyScore
│   ├── repository/       ← GaitReportRepository, DailyScoreRepository
│   └── exception/code/   ← ReportErrorCode, ReportSuccessCode
│
├── location/             ← 사용자/알림 담당 소유
│   ├── controller/       ← LocationController
│   ├── service/          ← LocationService
│   ├── sse/              ← SseEmitterManager
│   ├── dto/
│   │   ├── request/      ← LocationRequest
│   │   └── response/     ← LocationResponse
│   ├── entity/           ← Location
│   ├── repository/       ← LocationRepository
│   └── exception/code/   ← LocationErrorCode, LocationSuccessCode
│
├── ward/                 ← 사용자/알림 담당 소유
│   ├── controller/       ← WardController
│   ├── service/          ← WardService
│   ├── dto/              ← WardRequest, WardResponse
│   ├── entity/           ← Ward, Gender
│   ├── repository/       ← WardRepository
│   └── exception/code/   ← WardErrorCode, WardSuccessCode
│
├── user/
│   ├── controller/       ← UserController (사용자/알림 담당)
│   ├── service/          ← UserService (사용자/알림 담당)
│   ├── dto/              ← UserRequest, UserResponse (사용자/알림 담당)
│   ├── entity/           ← User, Role, Consent, ConsentType
│   ├── repository/       ← UserRepository, ConsentRepository
│   └── exception/code/   ← UserErrorCode, UserSuccessCode
│
└── global/
    ├── config/           ← SecurityConfig, SwaggerConfig, AsyncConfig (@EnableAsync, @EnableScheduling), JacksonConfig
    ├── entity/           ← BaseEntity (createdAt, updatedAt)
    └── exception/
        ├── CustomException, ErrorResponse, SuccessResponse, GlobalExceptionHandler
        └── code/         ← ErrorCode(인터페이스), SuccessCode(인터페이스), GlobalErrorCode
```

---

## 코딩 컨벤션

### DTO 구조
- **Request/Response**를 `dto/request/`, `dto/response/` 패키지로 분리
- 각 클래스 안에 기능별 `record`로 구분, `@Builder` 적용

```java
// auth/dto/request/AuthRequest.java
public class AuthRequest {
    public record SignUp(...) {}
    public record Login(...) {}
}

// auth/dto/response/AuthResponse.java
public class AuthResponse {
    @Builder
    public record SignUp(String accessToken, String refreshToken, String role) {}
}
```

### 엔티티 규칙
- 클래스 레벨에 `@Builder @AllArgsConstructor @NoArgsConstructor` 함께 사용
- `BaseEntity` 상속 시 `created_at`, `updated_at` 자동 관리 (JPA Auditing)
- 기본값이 있는 필드는 `@Builder.Default` 사용
- 엔티티 간 참조는 `Long id` 대신 JPA 연관관계 애노테이션 사용 (`@ManyToOne`, `@OneToOne`, `@OneToMany`)
- 페치 전략 기본값: `FetchType.LAZY`

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
| 피보호자 | `ward/exception/code/WardErrorCode` | `ward/exception/code/WardSuccessCode` |
| 위치 | `location/exception/code/LocationErrorCode` | `location/exception/code/LocationSuccessCode` |

### Service @Transactional 패턴
- 클래스 레벨: `@Transactional(readOnly = true)` 기본 적용
- 쓰기 메서드(INSERT/UPDATE/DELETE): `@Transactional` 개별 오버라이드

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
- **JWT 만료**: 액세스 토큰 30분 / 리프레시 토큰 30일
- **보행 API**: JWT에서 wardId 직접 추출 (추가 DB 조회 없음)
- **RefreshToken**: SHA-256 해시값만 DB 저장, 원본은 클라이언트 반환 / `device_id`로 멀티 디바이스 지원 / `revoked`(로그아웃), `used`(재사용 방지) 플래그 / `@ManyToOne User user` 연관관계
- **logout**: refreshToken을 body로 받는 public 엔드포인트 — 액세스 토큰 불필요 (만료 상태에서도 로그아웃 가능)
- **public URL 관리**: `SecurityConfig.PUBLIC_URLS`가 단일 소스 → `JwtAuthenticationFilter` 생성자에 전달 / `AntPathMatcher`로 패턴 매칭 / 미인증 접근 시 `JwtAuthenticationEntryPoint`가 커스텀 에러 형식 반환
- **비밀번호 정책**: 8자 이상, 영문·숫자·특수문자 조합 필수 (`@Pattern` — 회원가입·재설정 동일 정책)
- **비밀번호 재설정**: SHA-256 해시만 DB 저장 / 만료 15분 / 새 요청 시 기존 미사용 토큰 전체 무효화 / 재설정 완료 후 전체 RefreshToken 삭제(강제 로그아웃) / 이메일 발송 `@Async` 비동기 처리 / 만료 토큰 매일 새벽 3시 자동 삭제
- **ConsentType**: `TERMS`, `PRIVACY`, `SMS`, `MEDICAL`
- **SessionStatus**: `ACTIVE` / `COMPLETED` 두 가지만 존재
- **하루 1세션 설계**: 사용자가 하루 한 번 수동 `/start`, 매일 00시 스케줄러가 남은 ACTIVE 세션 자동 COMPLETED 처리
- **오프라인 우선**: 앱이 SQLite에 분당 데이터 보관 → 네트워크 복구 시 `/data` 배치 업로드 (ON CONFLICT DO NOTHING으로 중복 무시)
- **앱 재시작 복원**: 배터리 방전 등 비의도적 종료 시 `/active`로 기존 sessionId 복원해 같은 세션 이어서 진행
- **세션 데이터 보관**:
  - 위험 세션 `session_scores.expires_at = NULL` (영구 보관)
  - 정상 세션 `session_scores.expires_at = NOW() + 7일`
  - 매일 00시 만료 데이터 자동 삭제 (`SessionCleanupService @Scheduled`)
  - 고아 scores 안전망: 분석 미업로드 세션의 scores를 7일 후 만료 처리
- **세션 종료 시**: 서버가 `daily_scores` UPSERT 자동 처리
- **report_summary**: 앱/AI 팀이 TFLite 분석 후 생성하는 텍스트 요약, nullable
- **위험 감지 이벤트**: 인증/보행 담당이 `StrokeDangerEvent` 발행 → 사용자/알림 담당이 `@EventListener`로 수신 후 FCM 처리
- **실시간 위치**: DB UPSERT(ward당 최신 1건) + SSE 메모리 push 병행
  - `SseEmitterManager`가 wardId별 `Set<SseEmitter>` 관리 → 보호자 여러 명 동시 구독 가능
  - SSE 타임아웃 1시간, 재연결은 클라이언트 책임
  - 보호자 구독 즉시 DB 최신 위치 전송 (빈 화면 방지)
  - POST(WARD 전용) → UPSERT + SSE push / GET stream(GUARDIAN 전용) → SSE 구독
