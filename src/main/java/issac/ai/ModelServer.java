package issac.ai;

import java.io.IOException;
import java.nio.file.Path;

public class ModelServer {

    private Process serverProcess;

    public void startServer() throws IOException {

        Path serverDirectory = Path.of("C:", "ISSAC", "llama");

        Path executable = serverDirectory.resolve("llama-server.exe");

        Path modelPath = Path.of(
                "C:", "ISSAC", "models", "Qwen3.5-0.8B-Q4_0.gguf"
        );
        ProcessBuilder builder = new ProcessBuilder(
                executable.toString(),
                "-m", modelPath.toString(),
                "--host", "127.0.0.1",
                "--port", "8080",
                "-c", "2048"
        );

        builder.directory(serverDirectory.toFile());

        // Write server output to a log instead of opening a terminal.
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);

        serverProcess = builder.start();
    }

    public void stopServer() {

        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroy();
        }
    }
}