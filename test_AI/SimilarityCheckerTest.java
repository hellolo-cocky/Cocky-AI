package com.cocky.cockyserver.ai;

import com.cocky.cockyserver.ai.service.SimilarityChecker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimilarityCheckerTest {

    private final SimilarityChecker checker = new SimilarityChecker();

    @Test
    void identicalTextIsDuplicate() {
        String text = "배열을 정렬하고 최댓값을 출력하라";
        assertTrue(checker.isDuplicate(text, List.of(text), 0.80));
    }

    @Test
    void differentTextIsNotDuplicate() {
        assertFalse(checker.isDuplicate(
                "그래프에서 최단 경로를 BFS로 구하라",
                List.of("피보나치 수를 메모이제이션으로 계산하라"),
                0.80));
    }
}
