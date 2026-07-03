package com.cocky.cockyserver.ai.demo;

import com.cocky.cockyserver.ai.dto.Difficulty;
import com.cocky.cockyserver.ai.dto.ExampleIo;
import com.cocky.cockyserver.ai.dto.GeneratedProblem;
import com.cocky.cockyserver.ai.dto.GenerationItem;
import com.cocky.cockyserver.ai.dto.GenerationOutcome;
import com.cocky.cockyserver.ai.dto.GenerationRequest;
import com.cocky.cockyserver.ai.dto.Language;
import com.cocky.cockyserver.ai.port.ProblemGenerator;

import java.util.List;

/**
 * 데모 문제 생성기(키 없을 때). 언어×난이도 조합마다 결정적 더미 문제 생성.
 * 예제는 input==output(에코) 규칙이라 DemoExecutor 검증과도 정합한다.
 */
public class DemoProblemGenerator implements ProblemGenerator {

    private static final int DEMO_ATTEMPTS = 1;

    @Override
    public GenerationOutcome generate(GenerationRequest request) {
        List<GenerationItem> items = request.languages().stream()
                .flatMap(lang -> request.difficulties().stream()
                        .map(diff -> GenerationItem.success(build(request, lang, diff), DEMO_ATTEMPTS)))
                .toList();
        return new GenerationOutcome(items);
    }

    private GeneratedProblem build(GenerationRequest req, Language lang, Difficulty diff) {
        String title = "[데모] %s %s - %s".formatted(req.weeklyTopic(), diff, lang);
        String statement = """
                (데모 문제) 주제: %s / 세부: %s
                표준입력으로 받은 문자열을 그대로 출력하라.
                """.formatted(req.weeklyTopic(), req.roundSubtype());
        List<ExampleIo> examples = List.of(
                new ExampleIo("hello", "hello"),
                new ExampleIo("42", "42"));
        return new GeneratedProblem(lang, diff, title, statement, examples,
                echoCode(lang), req.roundSubtype());
    }

    private String echoCode(Language lang) {
        return switch (lang) {
            case PYTHON -> "import sys\nprint(sys.stdin.read().strip())";
            case C -> "#include <stdio.h>\nint main(){char b[1024];fgets(b,sizeof b,stdin);printf(\"%s\",b);return 0;}";
            case JAVA -> "import java.util.*;public class Main{public static void main(String[] a){"
                    + "System.out.print(new Scanner(System.in).nextLine());}}";
        };
    }
}
