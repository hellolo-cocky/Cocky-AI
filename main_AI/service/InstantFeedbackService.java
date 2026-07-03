package com.cocky.cockyserver.ai.service;

import com.cocky.cockyserver.ai.client.OpenAiClient;
import com.cocky.cockyserver.ai.config.AiProperties;
import com.cocky.cockyserver.ai.dto.FeedbackItem;
import com.cocky.cockyserver.ai.dto.InstantFeedback;
import com.cocky.cockyserver.ai.dto.Submission;
import com.cocky.cockyserver.ai.port.InstantFeedbackProvider;
import com.cocky.cockyserver.ai.prompt.PromptTemplates;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 제출 즉시 피드백. 3항목 각 0.00~10.00, 합 최대 30.00.
 */
public class InstantFeedbackService implements InstantFeedbackProvider {

    private static final BigDecimal MAX_ITEM_SCORE = new BigDecimal("10.00");
    /** 평가 항목 수 고정(3×10.00=30.00). 초과/미달 응답은 신뢰 불가 — 합계 상한 붕괴 방지. */
    private static final int REQUIRED_ITEM_COUNT = 3;

    private final OpenAiClient openAi;
    private final AiProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    public InstantFeedbackService(OpenAiClient openAi, AiProperties props) {
        this.openAi = openAi;
        this.props = props;
    }

    @Override
    public InstantFeedback evaluate(Submission submission) {
        String json = openAi.chatJson(props.models().instantFeedback(),
                PromptTemplates.INSTANT_SYSTEM,
                PromptTemplates.instantUser(submission));
        try {
            JsonNode root = mapper.readTree(json);
            List<FeedbackItem> items = new ArrayList<>();
            for (JsonNode item : root.path("items")) {
                BigDecimal score = clampScore(parseScore(item.path("score")));
                items.add(new FeedbackItem(
                        item.path("category").asText(""),
                        score,
                        item.path("comment").asText("")));
            }
            if (items.size() != REQUIRED_ITEM_COUNT) {
                throw new IllegalStateException(
                        "피드백 항목은 정확히 " + REQUIRED_ITEM_COUNT + "개여야 함: " + items.size());
            }
            return new InstantFeedback(items, root.path("personality").asText(""));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("즉시 피드백 JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

    private BigDecimal parseScore(JsonNode node) {
        try {
            return new BigDecimal(node.asText("0"));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal clampScore(BigDecimal raw) {
        BigDecimal v = raw == null ? BigDecimal.ZERO : raw;
        if (v.compareTo(BigDecimal.ZERO) < 0) {
            v = BigDecimal.ZERO;
        } else if (v.compareTo(MAX_ITEM_SCORE) > 0) {
            v = MAX_ITEM_SCORE;
        }
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
