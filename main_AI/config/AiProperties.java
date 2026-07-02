package com.cocky.cockyserver.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 모듈 설정 바인딩. 값은 application.yml → 환경변수에서 온다.
 * 시크릿(api-key)은 절대 로그/프롬프트에 싣지 않는다.
 */
@ConfigurationProperties(prefix = "ai")
public record AiProperties(
        OpenAi openai,
        Models models,
        String executor,
        Exec exec,
        Generation generation
) {
    public record OpenAi(String apiKey, String baseUrl, long timeoutMs) {
    }

    public record Models(
            String generation,
            String instantFeedback,
            String roundFeedback,
            String weeklyFeedback,
            String monthlyFeedback
    ) {
    }

    public record Exec(long timeoutMs) {
    }

    public record Generation(int maxRetries, double similarityThreshold) {
    }

    public boolean demoMode() {
        return openai == null || openai.apiKey() == null || openai.apiKey().isBlank();
    }
}
