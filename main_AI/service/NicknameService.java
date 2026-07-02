package com.cocky.cockyserver.ai.service;

import com.cocky.cockyserver.ai.client.OpenAiClient;
import com.cocky.cockyserver.ai.config.AiProperties;
import com.cocky.cockyserver.ai.port.NicknameGenerator;
import com.cocky.cockyserver.ai.prompt.PromptTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 익명 닉네임 생성. OpenAI 사용, 실패 시 내장 예시로 폴백(플로우 유지).
 */
public class NicknameService implements NicknameGenerator {

    private static final Logger log = LoggerFactory.getLogger(NicknameService.class);

    private final OpenAiClient openAi;
    private final AiProperties props;

    public NicknameService(OpenAiClient openAi, AiProperties props) {
        this.openAi = openAi;
        this.props = props;
    }

    @Override
    public String generate() {
        try {
            String nickname = openAi.chatText(props.models().instantFeedback(),
                    PromptTemplates.NICKNAME_SYSTEM,
                    "새 익명 닉네임 하나 생성.").strip();
            if (!nickname.isBlank()) {
                return firstLine(nickname);
            }
        } catch (RuntimeException e) {
            log.warn("닉네임 실호출 실패, 폴백 사용: {}", e.getMessage());
        }
        return fallback();
    }

    private String firstLine(String s) {
        int nl = s.indexOf('\n');
        return (nl >= 0 ? s.substring(0, nl) : s).strip();
    }

    private String fallback() {
        var examples = PromptTemplates.NICKNAME_EXAMPLES;
        return examples.get(ThreadLocalRandom.current().nextInt(examples.size()));
    }
}
