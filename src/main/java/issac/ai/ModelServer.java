package issac.ai;

import java.io.IOException;
import java.nio.file.Path;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ModelServer {

    private Process serverProcess;

    public synchronized void startServer() throws IOException {

        // Prevent ISSAC from launching a second Qwen process.
        if (isServerRunning()) {
            System.out.println("Qwen is already running. Skipping startup.");
            return;
        }

        // Checks if Port 8080 is occupied.
        if (isPortOccupied()) {

            throw new IOException(
                    "Port 8080 is already occupied. "
                            + "ISSAC will not launch another AI server."
            );
        }

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

    public boolean isServerRunning() {

        return serverProcess != null && serverProcess.isAlive();
    }

    public boolean isPortOccupied() {

        try (Socket socket = new Socket()) {

            socket.connect(
                    new InetSocketAddress("127.0.0.1", 8080),
                    500
            );

            return true;

        } catch (IOException e) {

            return false;
        }
    }

    public synchronized void stopServer() {

        if (serverProcess == null) {
            return;
        }

        if (serverProcess.isAlive()) {

            System.out.println("Stopping ISSAC's Qwen server...");

            serverProcess.destroy();

            try {

                // Give Qwen up to 3 seconds to shut down normally.
                boolean stopped = serverProcess.waitFor(
                        3, TimeUnit.SECONDS
                );

                if (!stopped) {

                    System.out.println(
                            "Qwen did not stop normally. Forcing shutdown."
                    );

                    serverProcess.destroyForcibly();

                    // Confirm the forced shutdown has completed.
                    serverProcess.waitFor();
                }

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                serverProcess.destroyForcibly();
            }
        }

        serverProcess = null;
    }

    public boolean isServerReady() {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:8080/health"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();

        try {

            HttpResponse<Void> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.discarding()
            );

            return response.statusCode() == 200;

        } catch (IOException e) {

            return false;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            return false;
        }
    }
}