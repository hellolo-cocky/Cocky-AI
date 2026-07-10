package com.cocky.cockyserver.ai.demo;

import com.cocky.cockyserver.ai.dto.FeedbackCategory;
import com.cocky.cockyserver.ai.dto.FeedbackItem;
import com.cocky.cockyserver.ai.dto.InstantFeedback;
import com.cocky.cockyserver.ai.dto.Submission;
import com.cocky.cockyserver.ai.port.InstantFeedbackProvider;

import java.math.BigDecimal;
import java.util.List;

/**
 * 데모 즉시 피드백. 3항목 각 10.00 = 합 30.00.
 */
public class DemoInstantFeedback implements InstantFeedbackProvider {

    private static final BigDecimal FULL = new BigDecimal("10.00");

    @Override
    public InstantFeedback evaluate(Submission submission) {
        List<FeedbackItem> items = List.of(
                new FeedbackItem(FeedbackCategory.TIME, FULL, "(데모) 효율적인 접근입니다."),
                new FeedbackItem(FeedbackCategory.READABILITY, FULL, "(데모) 가독성이 좋습니다."),
                new FeedbackItem(FeedbackCategory.ORIGINALITY, FULL, "(데모) 독창적인 풀이입니다."));
        return new InstantFeedback(items, "(데모) 꾸준함이 느껴지는 풀이네요!");
    }
}
