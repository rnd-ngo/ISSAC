package issac.analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AnalysisStore {

    private final Path analysisDirectory = Path.of("src", "data", "analysis");

    public void clearAllAnalyses() throws IOException {

        if (!Files.isDirectory(analysisDirectory)) {
            return;
        }

        try (var files = Files.list(analysisDirectory)) {

            for (Path file : files.toList()) {
                if (Files.isRegularFile(file)
                        && file.getFileName().toString().endsWith(".txt")) {

                    Files.delete(file);
                }
            }
        }
    }

    public void saveAnalysis(String documentName, String analysis)
            throws IOException {

        Files.createDirectories(analysisDirectory);

        Path analysisPath = analysisDirectory.resolve(documentName + ".txt");

        Files.writeString(analysisPath, analysis);
    }

    public boolean hasAnalysis(String documentName) {

        Path analysisPath =
                analysisDirectory.resolve(documentName + ".txt");

        return Files.isRegularFile(analysisPath);
    }

    public String loadAnalysis(String documentName) throws IOException {

        Path analysisPath = analysisDirectory.resolve(documentName + ".txt");

        return Files.readString(analysisPath);
    }

    public void deleteAnalysis(String documentName) throws IOException {

        Path analysisPath =
                analysisDirectory.resolve(documentName + ".txt");

        Files.deleteIfExists(analysisPath);
    }
}