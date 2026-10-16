package issac.analysis;

import java.util.List;

public class AnalysisTimeEstimator {

    public long calculateAverageDuration(
            List<AnalysisTimingRecord> records
    ) {

        if (records == null || records.isEmpty()) {
            return -1;
        }

        long totalDuration = 0;

        for (AnalysisTimingRecord record : records) {
            totalDuration += record.getDurationMillis();
        }

        return totalDuration / records.size();
    }

    public long estimateDuration(
            List<AnalysisTimingRecord> records,
            int inputCharacterCount,
            String modelId
    ) {

        if (records == null
                || inputCharacterCount <= 0
                || modelId == null) {
            return -1;
        }

        long totalDuration = 0;
        int matchingRecords = 0;

        // Accept historical inputs within 25% of the new input size.
        long tolerance = Math.max(1L, inputCharacterCount / 4L);

        for (AnalysisTimingRecord record : records) {

            if (!modelId.equals(record.getModelId())) {
                continue;
            }

            long sizeDifference = Math.abs(
                    (long) record.getInputCharacterCount()
                            - inputCharacterCount
            );

            if (sizeDifference > tolerance) {
                continue;
            }

            if (record.getDurationMillis() < 0) {
                continue;
            }

            // Avoid overflow when accumulating durations.
            if (Long.MAX_VALUE - totalDuration
                    < record.getDurationMillis()) {
                return -1;
            }

            totalDuration += record.getDurationMillis();
            matchingRecords++;
        }

        // Require at least three comparable measurements.
        if (matchingRecords < 3) {
            return -1;
        }

        return totalDuration / matchingRecords;
    }
}