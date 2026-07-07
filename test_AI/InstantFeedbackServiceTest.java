package com.cocky.cockyserver.ai;

import com.cocky.cockyserver.ai.client.OpenAiClient;
import com.cocky.cockyserver.ai.client.OpenAiException;
import com.cocky.cockyserver.ai.config.AiProperties;
import com.cocky.cockyserver.ai.dto.Difficulty;
import com.cocky.cockyserver.ai.dto.InstantFeedback;
import com.cocky.cockyserver.ai.dto.Language;
import com.cocky.cockyserver.ai.dto.Submission;
import com.cocky.cockyserver.ai.port.InstantFeedbackFailedException;
import com.cocky.cockyserver.ai.service.InstantFeedbackService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstantFeedbackServiceTest {

    private static final String VALID_JSON = """
            {"items":[
              {"category":"정확성","score":"8.50","comment":"좋음"},
              {"category":"효율성","score":"7.00","comment":"보통"},
              {"category":"가독성","score":"9.00","comment":"깔끔"}
            ],"personality":"차분한 분석가"}
            """;

    private final AiProperties props = new AiProperties(null, null, null, null, null);

    private Submission submission() {
        return new Submission(Language.JAVA, Difficulty.EASY, "문제", "코드");
    }

    /** chatJson 호출마다 스크립트 순서대로 처리. 예외 항목은 던지고 문자열 항목은 응답으로 반환. */
    private static class ScriptedOpenAiClient extends OpenAiClient {
        private final Object[] script; // String(응답) 또는 RuntimeException(던짐)
        private int calls = 0;

        ScriptedOpenAiClient(AiProperties props, Object... script) {
            super(props);
            this.script = script;
        }

        @Override
        public String chatJson(String model, String systemPrompt, String userPrompt) {
            Object step = script[Math.min(calls++, script.length - 1)];
            if (step instanceof RuntimeException e) {
                throw e;
            }
            return (String) step;
        }

        int calls() {
            return calls;
        }
    }

    @Test
    void firstAttemptSuccess() {
        ScriptedOpenAiClient client = new ScriptedOpenAiClient(props, VALID_JSON);
        InstantFeedback fb = new InstantFeedbackService(client, props).evaluate(submission());
        assertEquals(3, fb.items().size());
        assertEquals(new BigDecimal("8.50"), fb.items().get(0).score());
        assertEquals(1, client.calls());
    }

    @Test
    void retriesThenSucceeds() {
        ScriptedOpenAiClient client = new ScriptedOpenAiClient(props,
                new OpenAiException("타임아웃"),
                new OpenAiException("429"),
                VALID_JSON);
        InstantFeedback fb = new InstantFeedbackService(client, props).evaluate(submission());
        assertEquals(3, fb.items().size());
        assertEquals(3, client.calls());
    }

    @Test
    void exhaustedRetriesThrowContractExceptionNotOpenAiException() {
        ScriptedOpenAiClient client = new ScriptedOpenAiClient(props,
                new OpenAiException("500"));
        InstantFeedbackFailedException ex = assertThrows(InstantFeedbackFailedException.class,
                () -> new InstantFeedbackService(client, props).evaluate(submission()));
        assertInstanceOf(OpenAiException.class, ex.getCause());
        assertEquals(props.generation().maxRetries(), client.calls());
    }

    @Test
    void invalidItemCountAlsoRetriedAndWrapped() {
        ScriptedOpenAiClient client = new ScriptedOpenAiClient(props,
                "{\"items\":[{\"category\":\"정확성\",\"score\":\"5\",\"comment\":\"x\"}],\"personality\":\"p\"}");
        assertThrows(InstantFeedbackFailedException.class,
                () -> new InstantFeedbackService(client, props).evaluate(submission()));
        assertEquals(props.generation().maxRetries(), client.calls());
    }
}
