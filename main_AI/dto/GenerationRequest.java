package com.cocky.cockyserver.ai.dto;

import java.util.List;

/**
 * 백엔드 → AI 문제 생성 요청 (계약).
 *
 * @param languages       생성 대상 언어(보통 PYTHON/C/JAVA 전부).
 * @param difficulties    생성 대상 난이도(보통 EASY/NORMAL/HARD 전부).
 * @param weeklyTopic     이번 주 대주제(예: "동적 프로그래밍").
 * @param roundSubtype    회차 세부 유형(예: "메모이제이션").
 * @param pastTypes       지난 출제 유형 목록(프롬프트 중복 방지 힌트).
 * @param pastStatements  지난 문제 지문(중복 검사 대조용 — 백엔드가 넘겨줌).
 */
public record GenerationRequest(
        List<Language> languages,
        List<Difficulty> difficulties,
        String weeklyTopic,
        String roundSubtype,
        List<String> pastTypes,
        List<String> pastStatements
) {
    public static GenerationRequest fullRound(String weeklyTopic, String roundSubtype,
                                              List<String> pastTypes, List<String> pastStatements) {
        return new GenerationRequest(
                List.of(Language.values()),
                List.of(Difficulty.values()),
                weeklyTopic,
                roundSubtype,
                pastTypes == null ? List.of() : pastTypes,
                pastStatements == null ? List.of() : pastStatements
        );
    }
}
