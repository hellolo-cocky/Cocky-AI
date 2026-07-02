package com.cocky.cockyserver.ai.port;

/**
 * 익명 닉네임 생성 포트 (예: "파란 재귀함수"). 저장·전환은 백엔드.
 */
public interface NicknameGenerator {

    String generate();
}
