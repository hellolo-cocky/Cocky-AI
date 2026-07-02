package com.cocky.cockyserver.ai.prompt;

import com.cocky.cockyserver.ai.dto.Difficulty;
import com.cocky.cockyserver.ai.dto.GenerationRequest;
import com.cocky.cockyserver.ai.dto.Language;
import com.cocky.cockyserver.ai.dto.Period;
import com.cocky.cockyserver.ai.dto.PeriodStats;
import com.cocky.cockyserver.ai.dto.Submission;

import java.util.List;

/**
 * 프롬프트 템플릿 모음. 시크릿(키·토큰)은 절대 포함하지 않는다.
 */
public final class PromptTemplates {

    private PromptTemplates() {
    }

    public static final String GENERATION_SYSTEM = """
            너는 교내 AI 코딩 배틀의 출제자다. 주어진 언어·난이도·주제에 맞는
            알고리즘 문제 하나를 생성한다. 반드시 아래 JSON 스키마만 출력한다.
            {
              "title": string,
              "statement": string,          // 문제 지문(제약·입출력 형식 포함)
              "examples": [ {"input": string, "output": string} ],  // 1~3개
              "answerCode": string          // 지정 언어의 정답 소스코드(표준입출력 사용)
            }
            정답 코드는 표준입력을 읽어 표준출력으로 답을 낸다. 예제 input을 그대로
            표준입력에 넣으면 output이 나와야 한다.
            """;

    public static String generationUser(GenerationRequest req, Language lang, Difficulty diff) {
        String pastTypes = req.pastTypes().isEmpty() ? "(없음)" : String.join(", ", req.pastTypes());
        return """
                언어: %s
                난이도: %s
                이번 주 대주제: %s
                회차 세부 유형: %s
                지난 출제 유형(중복 회피): %s
                요구사항: 위 유형과 겹치지 않는 새로운 문제. 난이도에 맞는 복잡도.
                JSON 스키마로만 응답.
                """.formatted(lang, diff, req.weeklyTopic(), req.roundSubtype(), pastTypes);
    }

    public static final String DIFFICULTY_SYSTEM = """
            너는 문제 난이도를 EASY/NORMAL/HARD 중 하나로만 답하는 심사자다.
            다른 텍스트 없이 단어 하나만 출력한다.
            """;

    public static String difficultyUser(String statement) {
        return "다음 문제의 난이도(EASY/NORMAL/HARD):\n" + statement;
    }

    public static final String INSTANT_SYSTEM = """
            너는 학생 제출 코드를 3항목으로 평가한다. 반드시 아래 JSON만 출력한다.
            각 score는 0~10 사이 소수(소수점 2자리). category는 정확히 아래 이름 사용.
            {
              "items": [
                {"category": "시간복잡도 효율", "score": number, "comment": string},
                {"category": "코드 가독성",   "score": number, "comment": string},
                {"category": "풀이 독창성",   "score": number, "comment": string}
              ],
              "personality": string
            }
            """;

    public static String instantUser(Submission s) {
        return """
                언어: %s
                난이도: %s
                문제: %s
                제출 코드:
                ```
                %s
                ```
                """.formatted(s.language(), s.difficulty(), s.problemStatement(), s.code());
    }

    public static final String PERIOD_SYSTEM = """
            너는 코딩 배틀 기간 총평을 쓴다. 통계를 바탕으로 학생 집단의 강점·약점을
            짚고 격려하는 한국어 총평을 쓴다. 주간/월간이면 다음 기간 예습 추천을 포함한다.
            반드시 아래 JSON만 출력한다.
            { "summary": string, "studyRecommend": string }
            (ROUND면 studyRecommend는 빈 문자열)
            """;

    public static String periodUser(Period period, PeriodStats stats) {
        String next = stats.nextTopic() == null ? "(없음)" : stats.nextTopic();
        return """
                기간: %s
                언어별 제출: %s
                난이도별 선택: %s
                틀린 유형 빈도: %s
                다음 기간 대주제: %s
                """.formatted(period, stats.languageCounts(), stats.difficultyCounts(),
                stats.wrongTypeCounts(), next);
    }

    public static final String NICKNAME_SYSTEM = """
            너는 익명 닉네임 생성기다. "형용사 + 알고리즘/자료구조 용어" 형태의
            한국어 닉네임 하나만 출력한다. 예: "파란 재귀함수", "조용한 힙정렬".
            다른 텍스트 없이 닉네임만.
            """;

    public static final List<String> NICKNAME_EXAMPLES = List.of(
            "파란 재귀함수", "조용한 힙정렬", "느긋한 이진탐색", "성실한 스택",
            "용감한 그리디", "차분한 큐", "명랑한 해시맵", "우아한 그래프"
    );
}
