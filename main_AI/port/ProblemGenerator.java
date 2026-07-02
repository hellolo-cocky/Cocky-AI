package com.cocky.cockyserver.ai.port;

import com.cocky.cockyserver.ai.dto.GeneratedProblem;
import com.cocky.cockyserver.ai.dto.GenerationRequest;

import java.util.List;

/**
 * 문제 자동 생성 포트. 백엔드는 이 인터페이스에만 의존한다.
 */
public interface ProblemGenerator {

    /**
     * 요청의 언어×난이도 조합마다 1문제씩 생성(전체 = 9문제/회차).
     * 각 문제는 중복검사·정답검증·난이도확인을 통과한 상태로 반환된다.
     *
     * @throws com.cocky.cockyserver.ai.service.GenerationFailedException 재시도 소진 시.
     */
    List<GeneratedProblem> generate(GenerationRequest request);
}
