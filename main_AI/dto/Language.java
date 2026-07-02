package com.cocky.cockyserver.ai.dto;

/**
 * 지원 언어. judge0Id는 향후 Judge0 실행기 대비 보존(현재는 로컬 실행).
 */
public enum Language {
    PYTHON(71),
    C(50),
    JAVA(62);

    private final int judge0Id;

    Language(int judge0Id) {
        this.judge0Id = judge0Id;
    }

    public int judge0Id() {
        return judge0Id;
    }
}
