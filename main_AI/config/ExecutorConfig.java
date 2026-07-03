package com.cocky.cockyserver.ai.config;

import com.cocky.cockyserver.ai.exec.DemoExecutor;
import com.cocky.cockyserver.ai.exec.LocalProcessExecutor;
import com.cocky.cockyserver.ai.port.CodeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 코드 실행기 선택.
 * - ai.executor=demo → DemoExecutor(명시적 선택).
 * - 데모 모드(키 없음) + 툴체인 없음 → DemoExecutor 폴백(어차피 데모 생성기라 검증 미사용).
 * - 실키 모드 + 툴체인 없음 → 부팅 실패. 검증이 조용히 꺼진 채 배포되는 것을 막는다
 *   (예: Railway에 python/gcc 없음). 의도적으로 끄려면 AI_EXECUTOR=demo 명시.
 */
@Configuration
public class ExecutorConfig {

    private static final Logger log = LoggerFactory.getLogger(ExecutorConfig.class);

    @Bean
    public CodeExecutor codeExecutor(AiProperties props) {
        if ("demo".equalsIgnoreCase(props.executor())) {
            log.info("코드 실행기: DemoExecutor (ai.executor=demo)");
            return new DemoExecutor();
        }
        LocalProcessExecutor local = new LocalProcessExecutor(props);
        if (local.available()) {
            log.info("코드 실행기: LocalProcessExecutor");
            return local;
        }
        if (props.demoMode()) {
            log.warn("로컬 툴체인 없음 + 데모 모드 → DemoExecutor 폴백");
            return new DemoExecutor();
        }
        throw new IllegalStateException(
                "ai.executor=local이지만 로컬 툴체인(python/java/gcc)이 없어 정답 검증이 불가능합니다. "
                        + "실키 모드에서 검증 없는 문제 생성은 금지 — 배포 환경에 툴체인을 설치하거나 "
                        + "AI_EXECUTOR=demo를 명시하세요.");
    }
}
