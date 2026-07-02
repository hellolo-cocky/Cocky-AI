package com.cocky.cockyserver.ai.dto;

/**
 * 예제 입출력 한 쌍. output은 정답 코드를 실제 실행해 확정한다.
 */
public record ExampleIo(String input, String output) {
}
