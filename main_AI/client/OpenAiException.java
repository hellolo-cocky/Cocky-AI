package com.cocky.cockyserver.ai.client;

/** OpenAI 호출/응답 오류. */
public class OpenAiException extends RuntimeException {

    public OpenAiException(String message) {
        super(message);
    }

    public OpenAiException(String message, Throwable cause) {
        super(message, cause);
    }
}
