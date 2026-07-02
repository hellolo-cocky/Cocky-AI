package com.cocky.cockyserver.ai.dto;

import java.util.List;

/**
 * AI → 백엔드 생성 문제 (계약). 저장·정답검증 재사용은 백엔드 몫.
 *
 * @param language   대상 언어.
 * @param difficulty 난이도.
 * @param title      문제 제목.
 * @param statement  문제 지문.
 * @param examples   예제 입출력(output은 실행으로 확정됨).
 * @param answerCode 정답 코드(검증 통과 확인됨).
 * @param subtype    회차 세부 유형.
 */
public record GeneratedProblem(
        Language language,
        Difficulty difficulty,
        String title,
        String statement,
        List<ExampleIo> examples,
        String answerCode,
        String subtype
) {
}
