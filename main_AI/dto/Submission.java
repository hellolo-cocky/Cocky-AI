package com.cocky.cockyserver.ai.dto;

/**
 * 학생 제출 (즉시 피드백 입력).
 *
 * @param language   제출 언어.
 * @param difficulty 문제 난이도.
 * @param problemStatement 문제 지문(맥락).
 * @param code       제출 코드.
 */
public record Submission(
        Language language,
        Difficulty difficulty,
        String problemStatement,
        String code
) {
}
