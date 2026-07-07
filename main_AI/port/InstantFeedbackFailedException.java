package com.cocky.cockyserver.ai.port;

/**
 * 즉시 피드백 최종 실패 계약 예외. 모듈 내부 재시도 소진 후에만 던진다.
 * 백엔드는 이 타입만 캐치하면 된다 — 내부 구현 예외(OpenAiException 등)는 port를 넘지 않는다.
 */
public class InstantFeedbackFailedException extends RuntimeException {

    public InstantFeedbackFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
