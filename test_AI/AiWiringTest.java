package com.cocky.cockyserver.ai;

import com.cocky.cockyserver.ai.client.OpenAiClient;
import com.cocky.cockyserver.ai.config.AiConfig;
import com.cocky.cockyserver.ai.config.ExecutorConfig;
import com.cocky.cockyserver.ai.demo.DemoProblemGenerator;
import com.cocky.cockyserver.ai.port.ProblemGenerator;
import com.cocky.cockyserver.ai.service.ProblemGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 키 유무에 따른 데모/실구현 자동 주입 검증(DB 컨텍스트 없이).
 */
class AiWiringTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(AiConfig.class, ExecutorConfig.class)
            .withPropertyValues(
                    "ai.executor=demo",
                    "ai.openai.base-url=https://api.openai.com/v1",
                    "ai.openai.timeout-ms=1000",
                    "ai.exec.timeout-ms=5000",
                    "ai.generation.max-retries=3",
                    "ai.generation.similarity-threshold=0.8");

    @Test
    void noKeyWiresDemoImplementations() {
        runner.withPropertyValues("ai.openai.api-key=")
                .run(ctx -> {
                    assertInstanceOf(DemoProblemGenerator.class, ctx.getBean(ProblemGenerator.class));
                    assertEquals(0, ctx.getBeanNamesForType(OpenAiClient.class).length);
                });
    }

    @Test
    void withKeyWiresRealImplementations() {
        runner.withPropertyValues("ai.openai.api-key=sk-test-not-real")
                .run(ctx -> {
                    assertNotNull(ctx.getBean(OpenAiClient.class));
                    assertInstanceOf(ProblemGeneratorService.class, ctx.getBean(ProblemGenerator.class));
                });
    }
}
