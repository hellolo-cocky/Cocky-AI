package com.cocky.cockyserver.ai.exec;

import com.cocky.cockyserver.ai.config.AiProperties;
import com.cocky.cockyserver.ai.dto.ExecRequest;
import com.cocky.cockyserver.ai.dto.ExecResult;
import com.cocky.cockyserver.ai.dto.Language;
import com.cocky.cockyserver.ai.port.CodeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 로컬 프로세스로 코드를 실행해 정답을 검증한다.
 *
 * <p>주의: AI 생성 코드를 임의 실행하므로 타임아웃·임시디렉토리 격리·강제종료로
 * 방어한다. 완전한 샌드박스(네트워크 차단 등)는 이 모듈 범위 밖이며, 신뢰 경계는
 * 운영에서 별도 확보해야 한다.
 */
public class LocalProcessExecutor implements CodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(LocalProcessExecutor.class);
    private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private final long timeoutMs;

    public LocalProcessExecutor(AiProperties props) {
        this.timeoutMs = props.exec().timeoutMs();
    }

    @Override
    public boolean available() {
        // 하나라도 실행 가능하면 로컬 실행기를 채택한다(언어별 부재는 run 시 compileError로 반환).
        return commandWorks(pythonCmd(), "--version")
                || commandWorks("java", "-version")
                || commandWorks("gcc", "--version");
    }

    @Override
    public ExecResult run(ExecRequest request) {
        Path dir = null;
        try {
            dir = Files.createTempDirectory("cocky-exec-");
            return switch (request.language()) {
                case PYTHON -> runPython(dir, request);
                case C -> runC(dir, request);
                case JAVA -> runJava(dir, request);
            };
        } catch (IOException e) {
            log.warn("로컬 실행 준비 실패: {}", e.getMessage());
            return new ExecResult("", -1, false, "실행 준비 실패: " + e.getMessage());
        } finally {
            deleteRecursively(dir);
        }
    }

    private ExecResult runPython(Path dir, ExecRequest req) throws IOException {
        Path src = writeSource(dir, "main.py", req.sourceCode());
        return execute(dir, req.stdin(), pythonCmd(), src.toString());
    }

    private ExecResult runC(Path dir, ExecRequest req) throws IOException {
        writeSource(dir, "main.c", req.sourceCode());
        String binName = WINDOWS ? "main.exe" : "main";
        ExecResult compile = execute(dir, "", "gcc", "main.c", "-o", binName);
        if (!compile.success()) {
            return new ExecResult("", compile.exitCode(), compile.timedOut(),
                    compile.compileError() != null ? compile.compileError() : "C 컴파일 실패");
        }
        String binPath = dir.resolve(binName).toString();
        return execute(dir, req.stdin(), binPath);
    }

    private ExecResult runJava(Path dir, ExecRequest req) throws IOException {
        // JEP 330 단일 파일 실행: 파일명-클래스명 불일치 허용.
        Path src = writeSource(dir, "Main.java", req.sourceCode());
        return execute(dir, req.stdin(), "java", src.toString());
    }

    private Path writeSource(Path dir, String name, String code) throws IOException {
        Path src = dir.resolve(name);
        Files.writeString(src, code == null ? "" : code, StandardCharsets.UTF_8);
        return src;
    }

    /**
     * 프로세스를 실행하고 stdout을 캡처한다. 타임아웃 시 강제 종료.
     * stderr는 컴파일/런타임 오류 진단용으로 캡처하되 stdout과 분리한다.
     */
    private ExecResult execute(Path workDir, String stdin, String... command) {
        ProcessBuilder pb = new ProcessBuilder(command).directory(workDir.toFile());
        Process proc;
        try {
            proc = pb.start();
        } catch (IOException e) {
            // 실행 파일/컴파일러 부재 등.
            return new ExecResult("", -1, false, "명령 실행 불가(" + command[0] + "): " + e.getMessage());
        }

        StreamReader out = new StreamReader(proc.getInputStream());
        StreamReader err = new StreamReader(proc.getErrorStream());
        Thread outThread = new Thread(out);
        Thread errThread = new Thread(err);
        outThread.start();
        errThread.start();

        try (OutputStream os = proc.getOutputStream()) {
            if (stdin != null && !stdin.isEmpty()) {
                os.write(stdin.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ignoredBrokenPipe) {
            // 프로세스가 stdin을 읽기 전 종료할 수 있음 — 무시하고 결과로 판단.
            log.debug("stdin 쓰기 중 파이프 종료: {}", ignoredBrokenPipe.getMessage());
        }

        boolean finished;
        try {
            finished = proc.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            proc.destroyForcibly();
            return new ExecResult("", -1, false, "실행 인터럽트");
        }

        if (!finished) {
            proc.destroyForcibly();
            joinQuietly(outThread);
            joinQuietly(errThread);
            return new ExecResult(out.content(), -1, true, null);
        }

        joinQuietly(outThread);
        joinQuietly(errThread);
        int exit = proc.exitValue();
        String compileError = exit != 0 && !err.content().isBlank() ? err.content().strip() : null;
        return new ExecResult(out.content(), exit, false, compileError);
    }

    private static void joinQuietly(Thread t) {
        try {
            t.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String pythonCmd() {
        return WINDOWS ? "python" : "python3";
    }

    private boolean commandWorks(String... command) {
        try {
            Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
            boolean done = p.waitFor(3, TimeUnit.SECONDS);
            if (!done) {
                p.destroyForcibly();
                return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void deleteRecursively(Path dir) {
        if (dir == null) {
            return;
        }
        try (var paths = Files.walk(dir)) {
            List<Path> sorted = new ArrayList<>(paths.sorted(Comparator.reverseOrder()).toList());
            for (Path p : sorted) {
                Files.deleteIfExists(p);
            }
        } catch (IOException e) {
            log.debug("임시 디렉토리 정리 실패: {}", e.getMessage());
        }
    }

    /** 데드락 방지를 위해 스트림을 별도 스레드에서 소진한다. */
    private static final class StreamReader implements Runnable {
        private final java.io.InputStream in;
        private final StringBuilder sb = new StringBuilder();

        StreamReader(java.io.InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) != -1) {
                    sb.append(new String(buf, 0, n, StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                // 프로세스 강제 종료 시 스트림이 닫힐 수 있음 — 수집분까지만 사용.
                log.debug("출력 스트림 조기 종료: {}", e.getMessage());
            }
        }

        String content() {
            return sb.toString();
        }
    }
}
