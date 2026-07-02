package com.cocky.cockyserver.ai.dto;

/**
 * 코드 실행 요청.
 *
 * @param language   실행 언어.
 * @param sourceCode 소스 코드.
 * @param stdin      표준 입력.
 */
public record ExecRequest(Language language, String sourceCode, String stdin) {
}
