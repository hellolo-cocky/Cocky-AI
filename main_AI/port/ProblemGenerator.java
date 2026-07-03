package com.cocky.cockyserver.ai.port;

import com.cocky.cockyserver.ai.dto.GenerationOutcome;
import com.cocky.cockyserver.ai.dto.GenerationRequest;

/**
 * 문제 자동 생성 포트. 백엔드는 이 인터페이스에만 의존한다.
 */
public interface ProblemGenerator {

    /**
     * 요청의 언어×난이도 조합마다 1문제씩 생성(전체 = 9문제/회차).
     * 각 문제는 중복검사·정답검증·난이도확인을 통과한 상태로 반환된다.
     *
     * <p>일부 조합이 재시도 소진으로 실패해도 예외를 던지지 않고 성공분을 보존한다
     * — 결과의 failures()가 비어있지 않으면 백엔드가 관리자 알림을 보낸다.
     */
    GenerationOutcome generate(GenerationRequest request);
}
