package com.cocky.cockyserver.ai.config;

import com.cocky.cockyserver.ai.exec.DemoExecutor;
import com.cocky.cockyserver.ai.exec.LocalProcessExecutor;
import com.cocky.cockyserver.ai.port.CodeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 코드 실행기 선택: ai.executor=demo 이거나 로컬 툴체인 부재면 DemoExecutor, 아니면 로컬.
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
        log.warn("로컬 툴체인(python/java/gcc) 없음 → DemoExecutor 폴백");
        return new DemoExecutor();
    }
}
