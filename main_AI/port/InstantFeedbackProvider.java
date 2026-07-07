package com.cocky.cockyserver.ai.port;

import com.cocky.cockyserver.ai.dto.InstantFeedback;
import com.cocky.cockyserver.ai.dto.Submission;

/**
 * 제출 즉시 피드백 포트. 3항목 × 최대 10.00 = 최대 30.00.
 */
public interface InstantFeedbackProvider {

    /**
     * @throws InstantFeedbackFailedException 모듈 내부 재시도 소진 후 최종 실패 시.
     *         내부 구현 예외(OpenAiException 등)는 이 포트를 넘지 않는다.
     */
    InstantFeedback evaluate(Submission submission);
}
