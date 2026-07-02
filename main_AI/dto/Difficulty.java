package com.cocky.cockyserver.ai.dto;

import java.math.BigDecimal;

/**
 * 난이도와 백엔드 채점 기본점(참고용 — 실제 채점은 백엔드).
 */
public enum Difficulty {
    EASY(30),
    NORMAL(50),
    HARD(70);

    private final int baseScore;

    Difficulty(int baseScore) {
        this.baseScore = baseScore;
    }

    public BigDecimal baseScore() {
        return BigDecimal.valueOf(baseScore).setScale(2);
    }
}
