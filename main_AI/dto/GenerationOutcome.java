package com.cocky.cockyserver.ai.dto;

import java.util.List;

/**
 * 회차 생성 결과. 일부 조합이 실패해도 성공분은 보존해 반환한다.
 * 백엔드: problems()는 저장, failures()가 비어있지 않으면 관리자 알림.
 */
public record GenerationOutcome(List<GenerationItem> items) {

    /** 성공한 문제들(저장 대상). */
    public List<GeneratedProblem> problems() {
        return items.stream().filter(GenerationItem::success).map(GenerationItem::problem).toList();
    }

    /** 실패한 조합들(관리자 알림 대상). */
    public List<GenerationItem> failures() {
        return items.stream().filter(i -> !i.success()).toList();
    }

    public boolean complete() {
        return items.stream().allMatch(GenerationItem::success);
    }
}
