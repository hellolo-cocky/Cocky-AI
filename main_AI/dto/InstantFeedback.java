package com.cocky.cockyserver.ai.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 즉시 피드백. 3항목(각 최대 10.00) 합계 최대 30.00 + 개성 코멘트.
 *
 * @param items          평가 3항목.
 * @param personality    개성 코멘트.
 */
public record InstantFeedback(List<FeedbackItem> items, String personality) {

    /** 3항목 점수 합계(scale 2). */
    public BigDecimal total() {
        return items.stream()
                .map(FeedbackItem::score)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2);
    }
}
