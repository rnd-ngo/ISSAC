package issac.analysis;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.time.Instant;
import java.util.Collections;

public class AnalysisTimingStore {

    private final Path timingFile =
            Path.of("src", "data", "timing", "analysis_timing.csv");

    private final List<AnalysisTimingRecord> records =
            new ArrayList<>();

    public void saveRecord(AnalysisTimingRecord record) throws IOException {

        Path directory = timingFile.getParent();

        Files.createDirectories(directory);

        Base64.Encoder encoder = Base64.getUrlEncoder();

        String documentName = encoder.encodeToString(
                record.getDocumentName().getBytes(StandardCharsets.UTF_8)
        );

        String modelId = encoder.encodeToString(
                record.getModelId().getBytes(StandardCharsets.UTF_8)
        );

        String line = String.join("\t",
                documentName,
                Integer.toString(record.getInputCharacterCount()),
                Long.toString(record.getDurationMillis()),
                record.getCompletedAt().toString(),
                modelId
        ) + System.lineSeparator();

        Files.writeString(
                timingFile,
                line,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );

        records.add(record);
    }

    public void loadRecords() throws IOException {

        if (!Files.exists(timingFile)) {
            records.clear();
            return;
        }

        List<AnalysisTimingRecord> loadedRecords = new ArrayList<>();
        Base64.Decoder decoder = Base64.getUrlDecoder();

        for (String line : Files.readAllLines(
                timingFile,
                StandardCharsets.UTF_8
        )) {

            if (line.isBlank()) {
                continue;
            }

            String[] fields = line.split("\t", -1);

            if (fields.length != 5) {
                throw new IOException("Invalid timing record format.");
            }

            try {
                String documentName = new String(
                        decoder.decode(fields[0]),
                        StandardCharsets.UTF_8
                );

                int inputCharacterCount = Integer.parseInt(fields[1]);
                long durationMillis = Long.parseLong(fields[2]);
                Instant completedAt = Instant.parse(fields[3]);

                String modelId = new String(
                        decoder.decode(fields[4]),
                        StandardCharsets.UTF_8
                );

                if (inputCharacterCount < 0 || durationMillis < 0) {
                    throw new IOException("Invalid timing measurement.");
                }

                loadedRecords.add(new AnalysisTimingRecord(
                        documentName,
                        inputCharacterCount,
                        durationMillis,
                        completedAt,
                        modelId
                ));

            } catch (IllegalArgumentException | java.time.DateTimeException e) {
                throw new IOException("Unable to parse timing record.", e);
            }
        }

        records.clear();
        records.addAll(loadedRecords);
    }

    public List<AnalysisTimingRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }
}