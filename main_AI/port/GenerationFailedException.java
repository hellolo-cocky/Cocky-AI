package com.cocky.cockyserver.ai.port;

import com.cocky.cockyserver.ai.dto.Difficulty;
import com.cocky.cockyserver.ai.dto.Language;

/**
 * 재시도 소진 후에도 유효한 문제를 만들지 못했을 때. 백엔드가 잡아 관리자 알림을 보낸다.
 * 포트 계약의 일부이므로 port 패키지에 둔다(백엔드는 port/dto만 참조).
 */
public class GenerationFailedException extends RuntimeException {

    private final transient Language language;
    private final transient Difficulty difficulty;

    public GenerationFailedException(Language language, Difficulty difficulty, String reason) {
        super("문제 생성 실패 [%s/%s]: %s".formatted(language, difficulty, reason));
        this.language = language;
        this.difficulty = difficulty;
    }

    public Language language() {
        return language;
    }

    public Difficulty difficulty() {
        return difficulty;
    }
}
