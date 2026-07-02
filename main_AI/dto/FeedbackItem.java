package com.cocky.cockyserver.ai.dto;

import java.math.BigDecimal;

/**
 * 즉시 피드백 항목 하나. score는 0.00~10.00 (BigDecimal, scale 2).
 *
 * @param category 항목명(시간복잡도 효율 / 코드 가독성 / 풀이 독창성).
 * @param score    점수(최대 10.00).
 * @param comment  코멘트.
 */
public record FeedbackItem(String category, BigDecimal score, String comment) {
}
