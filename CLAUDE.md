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
| `FIREBASE_CREDENTIALS_PATH` | Firebase 서비스 계정 JSON 경로 (classpath 기준) | `firebase-service-account.json` |
| `REDIS_HOST` | Redis 호스트 (기본값: localhost) | `localhost` |
| `REDIS_PORT` | Redis 포트 (기본값: 6379) | `6379` |
| `REDIS_PASSWORD` | Redis 비밀번호 (로컬은 빈 값) | — |

> Firebase 서비스 계정 JSON: Firebase Console → 프로젝트 설정 → 서비스 계정 → 새 비공개 키 생성.
> `src/main/resources/firebase-service-account.json` 에 저장 후 `.gitignore` 에 추가.
> 파일이 없으면 FCM 기능만 비활성화되고 앱은 정상 기동됨.

> DB 설정(`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`)은 기본값이 있어 로컬 PostgreSQL 기본 설정 그대로 사용 가능.

### 로컬 DB 기본값
```
host: localhost / port: 5432 / db: nevo
username: nevo / password: nevo_backend
```

### 로컬 DB + Redis 실행
```bash
# PostgreSQL + Redis 동시 실행 (OS 무관)
docker-compose up -d

# 실행 확인
docker exec -it nevo-redis redis-cli ping  # PONG 응답 확인

# Redis 접속
docker exec -it nevo-redis redis-cli

# OTP 키 확인 (개발 중 디버깅)
docker exec -it nevo-redis redis-cli keys "sms:*"
docker exec -it nevo-redis redis-cli get "sms:SIGNUP:01012345678"
docker exec -it nevo-redis redis-cli ttl "sms:SIGNUP:01012345678"
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
| ward_guardian_link | V16 | 사용자/알림 담당 |
| ward_link_codes | V17 | 사용자/알림 담당 |
| locations | V9 | 사용자/알림 담당 |
| locations ward_id UNIQUE 제약 | V10 | 사용자/알림 담당 |
| alerts | V18 | 사용자/알림 담당 |
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
| daily_scores variability_score·asymmetry_score 컬럼 추가 | V19 | 인증/보행 담당 |
| daily_scores danger_count 컬럼 추가 | V20 | 인증/보행 담당 |
| gait_sessions(ward_id, status) 인덱스, session_scores(expires_at) partial 인덱스 | V21 | 인증/보행 담당 |
| users email → phone 컬럼 교체 | V22 | 인증/보행 담당 |
| password_reset_tokens DROP (SMS OTP로 대체) | V23 | 인증/보행 담당 |
| refresh_tokens(user_id) 인덱스 추가 | V24 | 인증/보행 담당 |

---

## 전체 API 목록

### 인증 — public (JWT 불필요) / 담당: 인증/보행

| 메서드 | 경로 | 설명 | 구현 |
|--------|------|------|------|
| POST | /api/auth/sms/send | SMS OTP 발송 (SIGNUP / PASSWORD_RESET) | ✅ |
| POST | /api/auth/sms/verify | SMS OTP 인증 | ✅ |
| POST | /api/auth/sign-up | 회원가입 (SMS 인증 완료 후) | ✅ |
| POST | /api/auth/login | 로그인 | ✅ |
| POST | /api/auth/logout | 로그아웃 | ✅ |
| POST | /api/auth/refresh | 토큰 갱신 | ✅ |
| POST | /api/auth/password-reset/request | 비밀번호 재설정 OTP 발송 | ✅ |
| POST | /api/auth/password-reset/confirm | 비밀번호 재설정 확인 (SMS 인증 완료 후) | ✅ |

### 보행 — JWT 필요 / 담당: 인증/보행 (모두 WARD 전용, GUARDIAN 호출 시 403)

| 메서드 | 경로 | 설명 | 구현 |
|--------|------|------|------|
| POST | /api/gait/sessions/start | 보행 측정 시작 | ✅ |
| POST | /api/gait/sessions/{sessionId}/data | 분당 보행 데이터 업로드 | ✅ |
| POST | /api/gait/sessions/{sessionId}/stop | 보행 측정 종료 | ✅ |
| GET  | /api/gait/sessions/active | 진행 중인 세션 조회 | ✅ |
| POST | /api/gait/sessions/{sessionId}/analysis | 세션 분석 결과 업로드 | ✅ |

### 리포트 — JWT 필요 / 담당: 인증/보행

| 메서드 | 경로 | 설명 | 역할 | 구현 | FE 화면 |
|--------|------|------|------|------|---------|
| GET | /api/gait/reports/{sessionId} | 세션 상세 리포트 조회 | WARD·GUARDIAN | ✅ | 01-04 결과 |
| GET | /api/gait/reports/daily | 최근 7일 일별 통계 + 세션 목록 | WARD | ✅ | 01-02 홈(오늘 점수), 01-05 기록 |
| GET | /api/gait/reports/ward/{wardId}/daily | 기간별(7·30·90일) 일별 통계 | GUARDIAN | ✅ | c2 노약자 상세 추이 |
| GET | /api/gait/reports/dashboard | 연동된 모든 노약자 최신 상태 요약 | GUARDIAN | ✅ | c1 대시보드 |

#### 미구현 (DB 필드 부재, AI팀 확정 대기)
| 화면 | 미구현 내용 | 이유 |
|------|------------|------|
| 01-02 홈, 01-04 결과 | 보폭·케이던스·보행속도 지표 | gait_reports에 해당 컬럼 없음 |
| 01-04 결과, c4 그래프 | 레이더 차트 | 보폭·케이던스·보행속도·한발지지 컬럼 없음 |
| c4 히트맵 | 날짜별 riskLevel 집계 | daily_scores에 riskLevel 컬럼 없음 |

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

### 보호자-노약자 연동 — JWT 필요 / 담당: 사용자/알림

| 메서드 | 경로 | 설명 | 역할 | 구현 |
|--------|------|------|------|------|
| POST | /api/ward-link/code | 연동 코드 생성 + 보호자에게 FCM 발송 | WARD | ✅ |
| POST | /api/ward-link | 연동 코드 입력해서 연결 | GUARDIAN | ✅ |
| DELETE | /api/ward-link/{wardId} | 연동 해제 | GUARDIAN | ✅ |
| GET | /api/ward-link/wards | 연결된 노약자 목록 조회 | GUARDIAN | ✅ |
| GET | /api/ward-link/guardians | 연결된 보호자 목록 조회 | WARD | ✅ |
| GET | /api/ward-link/{wardId}/alerts | 특정 노약자 알림 내역 조회 | GUARDIAN | ✅ |

---

## 패키지 구조

```
com.nevo.nevo/
├── auth/
│   ├── controller/       ← AuthController
│   ├── service/          ← AuthService, SmsService (Redis OTP 관리 + SMS 발송)
│   ├── dto/
│   │   ├── request/      ← AuthRequest (SignUp, Login, PasswordResetRequest, PasswordResetConfirm ... record)
│   │   └── response/     ← AuthResponse (SignUp, Login, ... record)
│   ├── entity/           ← RefreshToken, SmsVerificationPurpose (enum: SIGNUP / PASSWORD_RESET)
│   ├── repository/       ← RefreshTokenRepository
│   ├── jwt/              ← JwtUtil, JwtAuthenticationFilter, JwtAuthentication, JwtAuthenticationEntryPoint
│   └── exception/code/   ← AuthErrorCode, AuthSuccessCode
│
├── session/
│   ├── controller/       ← SessionController
│   ├── service/          ← SessionService, SessionCleanupService (@Scheduled, 매일 00:00 KST), SessionCleanupStepService (@Transactional 단계별 실행)
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
│   ├── controller/       ← WardController, WardLinkController
│   ├── service/          ← WardService, WardLinkService
│   ├── dto/
│   │   ├── request/      ← WardRequest, WardLinkRequest
│   │   └── response/     ← WardResponse, WardLinkResponse
│   ├── entity/           ← Ward, Gender, WardGuardianLink, WardLinkCode
│   ├── repository/       ← WardRepository, WardGuardianLinkRepository, WardLinkCodeRepository
│   └── exception/code/   ← WardErrorCode, WardSuccessCode
│
├── notification/         ← 사용자/알림 담당 소유
│   ├── service/          ← FcmService, AlertService, NotificationEventListener
│   ├── dto/
│   │   └── response/     ← AlertResponse
│   ├── entity/           ← Alert, AlertType
│   └── repository/       ← AlertRepository
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
    ├── config/           ← SecurityConfig, SwaggerConfig, AsyncConfig (@EnableAsync, @EnableScheduling, AsyncUncaughtExceptionHandler), JacksonConfig, FcmConfig, RedisConfig
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
{ "code": "AUTH409", "message": "이미 사용 중인 전화번호입니다.", "timestamp": "ISO-8601" }

// 성공
{ "code": "AUTH201", "message": "회원가입에 성공했습니다.", "timestamp": "ISO-8601", "data": { ... } }
```

---

## 배포 시 수정 필요 사항

### 1. SMS API 연동 (필수)
현재 OTP와 비밀번호 변경 알림은 서버 로그로만 출력됨. 실제 SMS 발송으로 교체 필요.

**파일**: `auth/service/SmsService.java`

```java
// [DEV ONLY] 로그 2곳을 네이버 클라우드 SMS API 호출로 교체

// 1. sendOtp() 내부
log.info("[SMS][DEV ONLY] phone={}, purpose={}, code={}", phone, purpose, code);
// → 네이버 클라우드 SMS API 호출로 교체

// 2. sendPasswordChangedNotification() 내부
log.info("[SMS][DEV ONLY] 비밀번호 변경 알림 발송 → phone={}", phone);
// → 네이버 클라우드 SMS API 호출로 교체
```

네이버 클라우드 SMS API 연동 시 추가 환경변수:
| 변수 | 설명 |
|------|------|
| `NCP_ACCESS_KEY` | 네이버 클라우드 Access Key |
| `NCP_SECRET_KEY` | 네이버 클라우드 Secret Key |
| `NCP_SMS_SERVICE_ID` | SMS 서비스 ID |
| `NCP_SMS_SENDER` | 발신번호 |

### 2. Redis 운영 서버 설정 (필수)
로컬은 패스워드 없이 사용하지만, 운영 환경에서는 반드시 설정 필요.
```
REDIS_HOST=운영서버주소
REDIS_PORT=6379
REDIS_PASSWORD=강력한패스워드
```

### 3. OTP 로그 레벨 확인 (필수)
SMS API 연동 완료 후 `[DEV ONLY]` 로그 코드 삭제. 운영 로그에 OTP 코드가 남으면 보안 위협.

---

## 핵심 설계 결정

- **JWT payload**: `{ userId, wardId, role }` — GUARDIAN은 `wardId: null`
- **JWT 만료**: 액세스 토큰 30분 / 리프레시 토큰 30일
- **보행 API**: JWT에서 wardId 직접 추출 (추가 DB 조회 없음)
- **RefreshToken**: SHA-256 해시값만 DB 저장, 원본은 클라이언트 반환 / `device_id`로 멀티 디바이스 지원 / `revoked`(로그아웃), `used`(재사용 방지) 플래그 / `@ManyToOne User user` 연관관계
- **logout**: refreshToken을 body로 받는 public 엔드포인트 — 액세스 토큰 불필요 (만료 상태에서도 로그아웃 가능)
- **public URL 관리**: `SecurityConfig.PUBLIC_URLS`가 단일 소스 → `JwtAuthenticationFilter` 생성자에 전달 / `AntPathMatcher`로 패턴 매칭 / 미인증 접근 시 `JwtAuthenticationEntryPoint`가 커스텀 에러 형식 반환
- **비밀번호 정책**: 8자 이상, 영문·숫자·특수문자 조합 필수 (`@Pattern` — 회원가입·재설정 동일 정책)
- **SMS OTP 인증**: Redis에 저장 (TTL 5분 자동 만료) / Key 형식: `sms:{PURPOSE}:{phone}` / 인증 완료 시 OTP 키 삭제 + verified 키 저장 (TTL 10분) / 재발송 시 기존 OTP 덮어씌워 자동 무효화 / SMS 발송 `@Async` 비동기 처리 / 개발 단계에서는 로그로 대체 (TODO: 네이버 클라우드 SMS API 연동)
- **회원가입 플로우**: `/sms/send(SIGNUP)` → `/sms/verify(SIGNUP)` → `/sign-up` (verified 확인 후 가입)
- **비밀번호 재설정**: `/sms/send(PASSWORD_RESET)` → `/sms/verify(PASSWORD_RESET)` → `/password-reset/confirm` / 재설정 완료 후 전체 RefreshToken 삭제(강제 로그아웃)
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
- **자정 스케줄러 트랜잭션 분리**: `SessionCleanupService`(@Scheduled)는 "언제 실행"만 담당, 실제 DB 작업은 `SessionCleanupStepService`(@Transactional) 3개 메서드로 분리 — 각 단계가 독립 트랜잭션으로 동작해 한 단계 실패 시 다른 단계는 정상 커밋
- **분석 업로드 시 (`/analysis`)**: `gait_reports` 저장 + `daily_scores` UPSERT (가중 평균 누적) 자동 처리 — `/stop`이 아닌 `/analysis` 호출 시점에 처리됨
- **daily_scores UPSERT 설계**: JdbcTemplate 직접 실행, ON CONFLICT DO UPDATE로 레이스 컨디션 없는 원자적 처리 / `variability_score`·`asymmetry_score` NULL 입력 시 CASE WHEN으로 기존 값 보호 / `danger_count` 세션별 누적 합산
- **asymmetryScore 변환**: DB에는 `asymmetry_score` (0~1 raw 임상값)로 저장, API 응답에는 `symmetryScore = (1 - asymmetryScore) * 100` (0~100%)로 변환해 노출 — `gait_reports`, `daily_scores` 모두 동일
- **variabilityScore·asymmetryScore nullable**: AI팀 필드 확정 전까지 nullable 허용. AI팀이 항상 값을 보내는 것으로 확정되면: `SessionRequest.AnalysisUpload`에 `@NotNull` 추가 + Flyway로 `gait_reports`·`daily_scores` 컬럼 NOT NULL 제약 추가 + `upsertDailyScore()` CASE WHEN 제거하고 단순 가중 평균으로 정리
- **report_summary**: 앱/AI 팀이 TFLite 분석 후 생성하는 텍스트 요약, nullable
- **위험 감지 이벤트**: 인증/보행 담당이 `StrokeDangerEvent` 발행 → `NotificationEventListener`가 `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` + `@Transactional`로 수신 → alerts 테이블 저장 + 연결된 보호자 전원에게 FCM 발송
- **보호자-노약자 연동**: 노약자가 보호자 이메일 입력 → 6자리 코드 생성(10분 만료) + 보호자에게 FCM 알림 발송 → 보호자가 코드 입력해 연결 / `ward_guardian_link` 다대다 / `ward_link_codes` 임시 코드 저장
- **연동 코드 문자셋**: `ABCDEFGHJKLMNPQRSTUVWXYZ23456789` (혼동 문자 I·O·0·1 제외)
- **FCM 인프라**: `FcmConfig`가 앱 시작 시 Firebase 초기화 (서비스 계정 JSON 없으면 FCM만 비활성화, 앱은 정상 기동) / `FcmService.send()` `@Async`로 비동기 처리
- **알림 내역**: `alerts` 테이블에 위험 감지 알림 영구 저장 → 보호자가 `GET /api/ward-link/{wardId}/alerts`로 조회 (연결된 노약자만 조회 가능)
- **실시간 위치**: DB UPSERT(ward당 최신 1건) + SSE 메모리 push 병행
  - `SseEmitterManager`가 wardId별 `Set<SseEmitter>` 관리 → 보호자 여러 명 동시 구독 가능
  - SSE 타임아웃 1시간, 재연결은 클라이언트 책임
  - 보호자 구독 즉시 DB 최신 위치 전송 (빈 화면 방지)
  - POST(WARD 전용) → UPSERT + SSE push / GET stream(GUARDIAN 전용) → SSE 구독

---

## 알려진 이슈 (수정 예정)

- **N+1: WARD 로그인·토큰 갱신**: WARD 역할 사용자 로그인/refresh 시 User 조회 후 Ward 조회로 쿼리 2회 발생.
  해결 방법: `User` 엔티티에 `@OneToOne(mappedBy="user") Ward ward` 추가 후 fetch join 적용.
  **사용자/알림 담당 팀원과 협의 후 수정 필요** (User 엔티티 공동 소유).
