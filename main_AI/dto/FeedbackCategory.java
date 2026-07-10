package com.cocky.cockyserver.ai.dto;

/**
 * 즉시 피드백 평가 항목. label은 프롬프트({@code PromptTemplates.INSTANT_SYSTEM})가
 * 모델에 지시하는 category 문자열과 정확히 일치해야 한다.
 */
public enum FeedbackCategory {
    TIME("시간복잡도 효율"),
    READABILITY("코드 가독성"),
    ORIGINALITY("풀이 독창성");

    private final String label;

    FeedbackCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /**
     * 모델 응답의 category 문자열을 enum으로 변환. 미지의 값이면
     * {@link IllegalArgumentException} — 호출부(서비스) 재시도 경로로 흘러간다.
     */
    public static FeedbackCategory fromLabel(String label) {
        String trimmed = label == null ? "" : label.trim();
        for (FeedbackCategory c : values()) {
            if (c.label.equals(trimmed)) {
                return c;
            }
        }
        throw new IllegalArgumentException("알 수 없는 피드백 category: " + label);
    }
}
