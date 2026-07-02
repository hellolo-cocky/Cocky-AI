package com.cocky.cockyserver.ai.demo;

import com.cocky.cockyserver.ai.dto.Period;
import com.cocky.cockyserver.ai.dto.PeriodFeedback;
import com.cocky.cockyserver.ai.dto.PeriodStats;
import com.cocky.cockyserver.ai.port.PeriodFeedbackProvider;

/**
 * 데모 기간 총평. 주간/월간은 예습 추천 포함.
 */
public class DemoPeriodFeedback implements PeriodFeedbackProvider {

    @Override
    public PeriodFeedback summarize(Period period, PeriodStats stats) {
        String summary = "(데모) 이번 %s 참여 통계 기반 총평입니다. 강점: 꾸준한 참여. 약점: 어려운 유형 보강 필요."
                .formatted(period);
        String recommend = period == Period.ROUND
                ? null
                : "(데모) 다음 기간 주제(%s) 예습을 추천합니다.".formatted(
                        stats.nextTopic() == null ? "미정" : stats.nextTopic());
        return new PeriodFeedback(period, summary, recommend);
    }
}
