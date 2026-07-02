package com.cocky.cockyserver.ai.exec;

import com.cocky.cockyserver.ai.dto.ExecRequest;
import com.cocky.cockyserver.ai.dto.ExecResult;
import com.cocky.cockyserver.ai.port.CodeExecutor;

/**
 * 툴체인이 없거나 ai.executor=demo 일 때의 폴백 실행기.
 * 실제 실행 없이 stdin을 그대로 돌려주어(에코) 검증 플로우가 끊기지 않게 한다.
 * 데모 문제의 예제 output도 동일 규칙(입력=출력)으로 생성되어 검증이 항상 통과한다.
 */
public class DemoExecutor implements CodeExecutor {

    @Override
    public ExecResult run(ExecRequest request) {
        String stdin = request.stdin() == null ? "" : request.stdin();
        return ExecResult.ok(stdin.strip());
    }

    @Override
    public boolean available() {
        return true;
    }
}
