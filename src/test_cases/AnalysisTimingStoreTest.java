

import issac.analysis.AnalysisTimeEstimator;
import issac.analysis.AnalysisTimingRecord;
import issac.analysis.AnalysisTimingStore;

import java.time.Instant;
import java.util.List;

public class AnalysisTimingStoreTest {

    public static void main(String[] args) {

        AnalysisTimeEstimator estimator = new AnalysisTimeEstimator();

        List<AnalysisTimingRecord> records = List.of(
                new AnalysisTimingRecord(
                        "Report_A", 1800, 30000,
                        Instant.now(), "Qwen3.5-0.8B"
                ),
                new AnalysisTimingRecord(
                        "Report_B", 2000, 45000,
                        Instant.now(), "Qwen3.5-0.8B"
                ),
                new AnalysisTimingRecord(
                        "Report_C", 2200, 60000,
                        Instant.now(), "Qwen3.5-0.8B"
                ),
                new AnalysisTimingRecord(
                        "Report_D", 8000, 120000,
                        Instant.now(), "Qwen3.5-0.8B"
                ),
                new AnalysisTimingRecord(
                        "Report_E", 2100, 10000,
                        Instant.now(), "Other-Model"
                )
        );

        long validEstimate = estimator.estimateDuration(
                records, 2000, "Qwen3.5-0.8B"
        );

        long insufficientData = estimator.estimateDuration(
                records, 8000, "Qwen3.5-0.8B"
        );

        long wrongModel = estimator.estimateDuration(
                records, 2000, "Unknown-Model"
        );

        System.out.println("Valid estimate: " + validEstimate);
        System.out.println("Insufficient data: " + insufficientData);
        System.out.println("Wrong model: " + wrongModel);

        if (validEstimate != 45000
                || insufficientData != -1
                || wrongModel != -1) {
            throw new AssertionError("Size-aware estimator test failed!");
        }

        System.out.println("All estimator tests PASSED!");
    }
}