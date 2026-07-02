package com.cocky.cockyserver.ai.dto;

/**
 * 코드 실행 결과.
 *
 * @param stdout      표준 출력(끝 공백 트림된 원문 비교용은 호출부에서 처리).
 * @param exitCode    종료 코드(컴파일 실패 등은 음수/비0).
 * @param timedOut    타임아웃 여부.
 * @param compileError 컴파일 오류 메시지(없으면 null).
 */
public record ExecResult(String stdout, int exitCode, boolean timedOut, String compileError) {

    public boolean success() {
        return !timedOut && exitCode == 0 && compileError == null;
    }

    public static ExecResult ok(String stdout) {
        return new ExecResult(stdout, 0, false, null);
    }
}
