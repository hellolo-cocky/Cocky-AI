package com.cocky.cockyserver.ai.port;

import com.cocky.cockyserver.ai.dto.Period;
import com.cocky.cockyserver.ai.dto.PeriodFeedback;
import com.cocky.cockyserver.ai.dto.PeriodStats;

/**
 * 기간(회차/주간/월간) 총평 포트. 통계 수치는 백엔드가 집계해 넘긴다.
 */
public interface PeriodFeedbackProvider {

    PeriodFeedback summarize(Period period, PeriodStats stats);
}
