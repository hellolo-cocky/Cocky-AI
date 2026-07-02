package com.cocky.cockyserver.ai.port;

import com.cocky.cockyserver.ai.dto.ExecRequest;
import com.cocky.cockyserver.ai.dto.ExecResult;

/**
 * 코드 실행 포트. 정답 검증에 사용. 로컬 실행(LocalProcessExecutor)이 기본이며
 * 향후 Judge0 구현으로 교체 가능(계약 동일).
 */
public interface CodeExecutor {

    ExecResult run(ExecRequest request);

    /** 이 실행기가 현재 환경에서 사용 가능한지(툴체인 존재 등). */
    boolean available();
}
