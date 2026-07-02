package com.cocky.cockyserver.ai.dto;

/**
 * 기간 총평.
 *
 * @param period          기간 단위.
 * @param summary         AI 총평(강점·약점 포함).
 * @param studyRecommend  다음 기간 예습 추천(ROUND는 null 가능).
 */
public record PeriodFeedback(Period period, String summary, String studyRecommend) {
}
