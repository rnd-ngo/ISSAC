package issac.analysis;

import java.time.Instant;

public class AnalysisTimingRecord {

    private final String documentName;
    private final int inputCharacterCount;
    private final long durationMillis;
    private final Instant completedAt;
    private final String modelId;

    public AnalysisTimingRecord(
            String docuementName,
            int inputCharacterCount,
            long durationMillis,
            Instant completedAt,
            String modelId
    ) {
        this.documentName = docuementName;
        this.inputCharacterCount = inputCharacterCount;
        this.durationMillis = durationMillis;
        this.completedAt = completedAt;
        this.modelId = modelId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public int getInputCharacterCount() {
        return inputCharacterCount;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getModelId() {
        return modelId;
    }

}