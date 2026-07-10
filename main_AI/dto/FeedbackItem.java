package com.cocky.cockyserver.ai.dto;

import java.math.BigDecimal;

/**
 * 즉시 피드백 항목 하나. score는 0.00~10.00 (BigDecimal, scale 2).
 *
 * @param category 평가 항목(TIME / READABILITY / ORIGINALITY).
 * @param score    점수(최대 10.00).
 * @param comment  코멘트.
 */
public record FeedbackItem(FeedbackCategory category, BigDecimal score, String comment) {
}
