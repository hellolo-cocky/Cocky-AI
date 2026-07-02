package com.cocky.cockyserver.ai.demo;

import com.cocky.cockyserver.ai.port.NicknameGenerator;
import com.cocky.cockyserver.ai.prompt.PromptTemplates;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 데모 닉네임 생성. 내장 예시에서 무작위 선택.
 */
public class DemoNicknameGenerator implements NicknameGenerator {

    @Override
    public String generate() {
        var examples = PromptTemplates.NICKNAME_EXAMPLES;
        return examples.get(ThreadLocalRandom.current().nextInt(examples.size()));
    }
}
