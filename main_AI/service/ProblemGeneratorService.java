package com.cocky.cockyserver.ai.service;

import com.cocky.cockyserver.ai.client.OpenAiClient;
import com.cocky.cockyserver.ai.config.AiProperties;
import com.cocky.cockyserver.ai.dto.Difficulty;
import com.cocky.cockyserver.ai.dto.ExampleIo;
import com.cocky.cockyserver.ai.dto.ExecRequest;
import com.cocky.cockyserver.ai.dto.ExecResult;
import com.cocky.cockyserver.ai.dto.GeneratedProblem;
import com.cocky.cockyserver.ai.dto.GenerationItem;
import com.cocky.cockyserver.ai.dto.GenerationOutcome;
import com.cocky.cockyserver.ai.dto.GenerationRequest;
import com.cocky.cockyserver.ai.dto.Language;
import com.cocky.cockyserver.ai.exec.DemoExecutor;
import com.cocky.cockyserver.ai.port.CodeExecutor;
import com.cocky.cockyserver.ai.port.ProblemGenerator;
import com.cocky.cockyserver.ai.prompt.PromptTemplates;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 문제 생성 오케스트레이션: 생성 → 중복검사 → 정답검증 → 난이도확인, 조합당 재시도.
 */
public class ProblemGeneratorService implements ProblemGenerator {

    private static final Logger log = LoggerFactory.getLogger(ProblemGeneratorService.class);

    private final OpenAiClient openAi;
    private final CodeExecutor executor;
    private final SimilarityChecker similarity;
    private final AiProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    public ProblemGeneratorService(OpenAiClient openAi, CodeExecutor executor,
                                   SimilarityChecker similarity, AiProperties props) {
        this.openAi = openAi;
        this.executor = executor;
        this.similarity = similarity;
        this.props = props;
    }

    @Override
    public GenerationOutcome generate(GenerationRequest request) {
        List<GenerationItem> items = new ArrayList<>();
        List<String> seenStatements = new ArrayList<>(request.pastStatements());
        for (Language lang : request.languages()) {
            for (Difficulty diff : request.difficulties()) {
                GenerationItem item = generateOne(request, lang, diff, seenStatements);
                items.add(item);
                if (item.success()) {
                    seenStatements.add(item.problem().statement());
                }
            }
        }
        return new GenerationOutcome(items);
    }

    /**
     * 조합 하나 생성. 재시도 소진 시 예외 대신 실패 레코드 반환
     * — 다른 조합의 성공분을 보존한다(백엔드 ai_generation_log 부분 저장 대응).
     */
    private GenerationItem generateOne(GenerationRequest req, Language lang, Difficulty diff,
                                       List<String> seenStatements) {
        int maxRetries = props.generation().maxRetries();
        String lastReason = "알 수 없음";
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String json = openAi.chatJson(props.models().generation(),
                        PromptTemplates.GENERATION_SYSTEM,
                        PromptTemplates.generationUser(req, lang, diff));
                Parsed parsed = parse(json);

                if (similarity.isDuplicate(parsed.statement, seenStatements,
                        props.generation().similarityThreshold())) {
                    lastReason = "기존 문제와 유사도 초과";
                    log.info("[{}/{}] 재생성 — {} (시도 {})", lang, diff, lastReason, attempt);
                    continue;
                }

                List<ExampleIo> confirmed = verifyExamples(lang, parsed);
                if (confirmed == null) {
                    lastReason = "정답 코드가 예제를 통과하지 못함";
                    log.info("[{}/{}] 재생성 — {} (시도 {})", lang, diff, lastReason, attempt);
                    continue;
                }

                recheckDifficulty(diff, parsed.statement);
                GeneratedProblem problem = new GeneratedProblem(lang, diff, parsed.title,
                        parsed.statement, confirmed, parsed.answerCode, req.roundSubtype());
                return GenerationItem.success(problem, attempt);
            } catch (RuntimeException e) {
                lastReason = e.getMessage();
                log.warn("[{}/{}] 생성 시도 {} 실패: {}", lang, diff, attempt, e.getMessage());
            }
        }
        log.error("[{}/{}] 재시도 {}회 소진 — 실패 조합으로 보고(성공분 보존): {}",
                lang, diff, maxRetries, lastReason);
        return GenerationItem.failure(lang, diff, maxRetries, lastReason);
    }

    /** 각 예제 input으로 정답 코드를 실행해 declared output과 일치하면 실행 출력으로 확정. */
    private List<ExampleIo> verifyExamples(Language lang, Parsed parsed) {
        if (executor instanceof DemoExecutor) {
            // 로컬 툴체인 부재 등 — 실행 검증 생략(선언된 예제 신뢰). 운영에서 local 실행기 권장.
            log.warn("데모 실행기 사용 중 — 정답 실행 검증을 생략한다");
            return parsed.examples;
        }
        List<ExampleIo> confirmed = new ArrayList<>();
        for (ExampleIo ex : parsed.examples) {
            ExecResult r = executor.run(new ExecRequest(lang, parsed.answerCode, ex.input()));
            if (!r.success()) {
                return null;
            }
            if (!normalize(r.stdout()).equals(normalize(ex.output()))) {
                return null;
            }
            confirmed.add(new ExampleIo(ex.input(), r.stdout().strip()));
        }
        return confirmed.isEmpty() ? null : confirmed;
    }

    private void recheckDifficulty(Difficulty requested, String statement) {
        try {
            String answer = openAi.chatText(props.models().generation(),
                    PromptTemplates.DIFFICULTY_SYSTEM,
                    PromptTemplates.difficultyUser(statement)).strip().toUpperCase();
            Difficulty rated = Difficulty.valueOf(answer);
            if (rated.ordinal() < requested.ordinal()) {
                // 운영 규칙: 낮게 나와도 허용하되 로그만 남긴다.
                log.info("난이도 자문 낮음: 요청={} 자문={} (허용)", requested, rated);
            }
        } catch (RuntimeException e) {
            // 자문 파싱 실패(예: 예상 밖 응답)나 호출 오류는 비치명 — 원 난이도 유지.
            log.debug("난이도 자문 스킵: {}", e.getMessage());
        }
    }

    private Parsed parse(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            String title = root.path("title").asText("");
            String statement = root.path("statement").asText("");
            String answerCode = root.path("answerCode").asText("");
            if (statement.isBlank() || answerCode.isBlank()) {
                throw new IllegalStateException("생성 JSON 필수 필드 누락");
            }
            List<ExampleIo> examples = new ArrayList<>();
            for (JsonNode ex : root.path("examples")) {
                examples.add(new ExampleIo(ex.path("input").asText(""), ex.path("output").asText("")));
            }
            if (examples.isEmpty()) {
                throw new IllegalStateException("예제가 없음");
            }
            return new Parsed(title, statement, answerCode, examples);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("생성 JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

    private String normalize(String s) {
        return s == null ? "" : s.strip().replace("\r\n", "\n");
    }

    private record Parsed(String title, String statement, String answerCode, List<ExampleIo> examples) {
    }
}
