package com.cocky.cockyserver.ai.port;

import com.cocky.cockyserver.ai.dto.InstantFeedback;
import com.cocky.cockyserver.ai.dto.Submission;

/**
 * 제출 즉시 피드백 포트. 3항목 × 최대 10.00 = 최대 30.00.
 */
public interface InstantFeedbackProvider {

    InstantFeedback evaluate(Submission submission);
}
