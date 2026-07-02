# AI 모듈 — 백엔드 통합 가이드

백엔드에서 AI 기능(문제 생성 · 즉시 피드백 · 기간 총평 · 익명 닉네임)을 사용하기 위한 가이드입니다.

## 개요

백엔드는 `ai.port` 패키지의 **인터페이스 4개만 생성자 주입**하면 됩니다.
구현체(실제 OpenAI / 데모)는 API 키 유무에 따라 자동 선택되므로 백엔드에서 신경 쓸 것이 없습니다.

| 인터페이스 | 용도 | 호출 시점 |
|---|---|---|
| `ProblemGenerator` | 문제 생성 (9개) | 밤 23시 스케줄러 |
| `InstantFeedbackProvider` | 제출 코드 즉시 피드백 | 제출 직후 |
| `PeriodFeedbackProvider` | 회차/주간/월간 총평 | 기간 마감 시 |
| `NicknameGenerator` | 익명 닉네임 생성 | 익명 선택 시 |

> `CodeExecutor`는 모듈 내부용이므로 백엔드에서 직접 사용하지 않습니다.

## 사용법

### 1. 문제 생성 — 밤 23시 스케줄러에서

```java
private final ProblemGenerator problemGenerator;  // 생성자 주입

var req = GenerationRequest.fullRound(
        "동적 프로그래밍",          // 이번 주 대주제
        "메모이제이션",             // 회차 세부 유형
        pastTypes,                 // 지난 출제 유형 목록
        pastStatements);           // 지난 문제 지문 (중복검사용, DB에서 조회)

try {
    List<GeneratedProblem> problems = problemGenerator.generate(req); // 9개 반환
    // 저장은 백엔드 담당. statement / examples / answerCode / language / difficulty 포함.
} catch (GenerationFailedException e) {
    // 재시도 3회 소진 시 발생 — e.language() / e.difficulty() 로 실패 조합 확인 후 관리자 알림 발송
}
```

### 2. 즉시 피드백 — 제출 직후

```java
private final InstantFeedbackProvider instantFeedback;

InstantFeedback fb = instantFeedback.evaluate(
        new Submission(Language.PYTHON, Difficulty.EASY, statement, studentCode));

fb.total();   // BigDecimal, 30.00 만점 — 기본점(30/50/70)에 더해 저장
fb.items();   // 3개 항목, 각 점수 + 코멘트
```

### 3. 기간 총평 — 회차/주간/월간 마감 시

```java
private final PeriodFeedbackProvider periodFeedback;

PeriodFeedback pf = periodFeedback.summarize(Period.WEEKLY,
        new PeriodStats(langCounts, diffCounts, wrongTypeCounts, "그래프 탐색"));
```

- `PeriodStats`는 **백엔드가 집계해서 채워** 전달합니다.
- `studyRecommend`는 `WEEKLY` / `MONTHLY`에서만 non-null입니다.

### 4. 익명 닉네임 — 익명 선택 시

```java
private final NicknameGenerator nickname;

String name = nickname.generate();   // 예: "파란 재귀함수"
```

## 백엔드 체크리스트

- [ ] import는 `com.cocky.cockyserver.ai.port.*` + `ai.dto.*` 만 사용 — `service/`, `demo/`, `client/` 직접 참조 금지
- [ ] 응답 봉투 `{data, error, status}` 래핑은 백엔드 컨트롤러 몫
- [ ] `GenerationFailedException` catch 시 관리자 알림 발송 (백엔드 담당)

## 환경 변수

| 변수 | 설명 |
|---|---|
| `OPENAI_API_KEY` | 없으면 자동 **데모 모드**로 동작 (오류 아님) |

> ⚠️ Spring은 `.env` 파일을 자동으로 읽지 않습니다. OS 환경변수로 설정하거나 `spring-dotenv`를 사용하세요.
