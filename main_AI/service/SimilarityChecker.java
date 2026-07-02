package com.cocky.cockyserver.ai.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 토큰 Jaccard 유사도로 문제 중복을 판정한다.
 */
public class SimilarityChecker {

    /** a·b의 토큰 집합 Jaccard 유사도(0.0~1.0). */
    public double similarity(String a, String b) {
        Set<String> ta = tokenize(a);
        Set<String> tb = tokenize(b);
        if (ta.isEmpty() && tb.isEmpty()) {
            return 1.0;
        }
        Set<String> intersection = new HashSet<>(ta);
        intersection.retainAll(tb);
        Set<String> union = new HashSet<>(ta);
        union.addAll(tb);
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /** candidate가 기존 문제들 중 하나와 threshold 이상 유사하면 true. */
    public boolean isDuplicate(String candidate, List<String> existing, double threshold) {
        for (String prev : existing) {
            if (similarity(candidate, prev) >= threshold) {
                return true;
            }
        }
        return false;
    }

    private Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        if (text == null) {
            return tokens;
        }
        for (String t : text.toLowerCase().split("[^\\p{L}\\p{N}]+")) {
            if (!t.isBlank()) {
                tokens.add(t);
            }
        }
        return tokens;
    }
}
