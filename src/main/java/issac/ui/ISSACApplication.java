package issac.ui;

import issac.analysis.AnalysisTimeEstimator;
import issac.analysis.AnalysisTimingRecord;
import issac.analysis.AnalysisTimingStore;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import issac.analysis.AnalysisStore;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import issac.document.DocRead;
import issac.ai.LocalModel;
import javafx.concurrent.Task;
import javafx.scene.layout.HBox;
import issac.ai.ModelServer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

public class ISSACApplication extends Application {

    private final DocRead reader = new DocRead();
    private final ListView<String> documentList = new ListView<>();
    private final AnalysisStore analysisStore = new AnalysisStore();
    private final AnalysisTimingStore analysisTimingStore = new AnalysisTimingStore();
    private final LocalModel model = new LocalModel();
    private final ModelServer modelServer = new ModelServer();
    private final TextArea analysisDisplay = new TextArea();
    private final Map<String, String> analyses = new HashMap<>();
    private final Map<String, String> analysisErrors = new HashMap<>();
    private final Map<String, Long> analysisDurations = new HashMap<>();
    private boolean analysisRunning = false;
    private Label aiStatusLabel;
    private Button restartAIButton = new Button("Restart AI");
    private String analyzingDocument = null;
    private Task<String> currentAnalysisTask;
    private boolean cancellationRequested = false;
    private boolean analysisWorkerFinished = false;
    private boolean analysisOutcomeHandled = false;
    private long cancellationStartTime = 0;
    private Timeline cancellationTimer;
    private String recentlyCancelledDocument = null;
    private PauseTransition cancellationConfirmation;
    private long analysisStartTime = 0;
    private long estimatedAnalysisDurationMillis = -1;
    private Timeline analysisTimer;
    private final AnalysisTimeEstimator analysisTimeEstimator = new AnalysisTimeEstimator();

    @Override
    public void start(Stage primaryStage) {

        aiStatusLabel = new Label();
        updateAIStatus("STARTING");

        try {
            modelServer.startServer();
        } catch (IOException e) {
            updateAIStatus("OFFLINE");

            System.err.println("Unable to start Qwen: " + e.getMessage());
        } finally {
            // Begin checking whether Qwen is ready.
            monitorAIStatus();
        }

        // Restore historical analysis timing measurements.
        try {
            analysisTimingStore.loadRecords();

            System.out.println(
                    "Loaded "
                            + analysisTimingStore.getRecords().size()
                            + " analysis timing records."
            );

        } catch (IOException e) {
            System.err.println(
                    "Failed to load analysis timing history: "
                            + e.getMessage()
            );
        }

        try {
            // Rebuild the document registry from files saved on disk.
            reader.loadSavedDocuments();

            // Add the recovered document names to our JavaFX ListView.
            documentList.getItems().addAll(reader.getDocumentNames());

            for (String documentName : reader.getDocumentNames()) {

                if (analysisStore.hasAnalysis(documentName)) {

                    String savedAnalysis =
                            analysisStore.loadAnalysis(documentName);

                    analyses.put(documentName, savedAnalysis);
                }
            }

        } catch (IOException e) {
            // Report a startup recovery problem without crashing ISSAC.
            System.err.println("Failed to restore documents: " + e.getMessage());
        }

        // Prevent users from accidentally modifying AI-generated analysis.
        analysisDisplay.setEditable(false);

        // Automatically wrap long lines instead of requiring horizontal scrolling.
        analysisDisplay.setWrapText(true);

        // Display instructions before any document has been analyzed.
        analysisDisplay.setPromptText("Select a document and click Analyze.");

        primaryStage.setTitle("ISSAC");

        Button uploadButton = new Button("Upload Document(s)");
        Button deleteButton = new Button("Delete Document(s)");
        Button analyzeButton = new Button("Analyze Document(s)");
        Button clearAnalysisButton = new Button("Clear Analysis");
        Button clearButton = new Button("Clear Session");

        clearButton.setOnAction(event -> confirmClearSession());
        uploadButton.setOnAction(event -> uploadFile(primaryStage));
        deleteButton.setOnAction(event -> deleteDocument());
        analyzeButton.setOnAction(event -> analyzeDocument());
        clearAnalysisButton.setOnAction(event -> clearSelectedAnalysis());
        restartAIButton.setOnAction(event -> restartAI());


        // Arrange the document list and analysis display side by side.
        HBox contentArea = new HBox(10);

        // Give the document list a preferred width.
        documentList.setPrefWidth(250);

        // Customize each document row to display its analysis status.
        // Display each document's analysis status and contextual controls.
        documentList.setCellFactory(listView -> new ListCell<>() {

            @Override
            protected void updateItem(String documentName, boolean empty) {
                super.updateItem(documentName, empty);

                if (empty || documentName == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label documentLabel = new Label(documentName);
                Label statusLabel = new Label();

                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);

                if (documentName.equals(analyzingDocument)) {

                    if (cancellationRequested) {

                        long now = System.nanoTime();

                        long totalSeconds =
                                (now - analysisStartTime) / 1_000_000_000L;

                        long cancellationSeconds =
                                (now - cancellationStartTime) / 1_000_000_000L;

                        statusLabel.setText(
                                String.format(
                                        "◌ Cancelling... Total: %02d:%02d | Cancel: %02d:%02d",
                                        totalSeconds / 60,
                                        totalSeconds % 60,
                                        cancellationSeconds / 60,
                                        cancellationSeconds % 60
                                )
                        );

                    } else {

                    long elapsedSeconds =
                            (System.nanoTime() - analysisStartTime)
                                    / 1_000_000_000L;

                    long minutes = elapsedSeconds / 60;
                    long seconds = elapsedSeconds % 60;

                    // Build the normal elapsed-time display first.
                    String statusText = String.format(
                            "◌ Analyzing... %02d:%02d",
                            minutes,
                            seconds
                    );

                    // Append an estimate only when sufficient history exists.
                    if (estimatedAnalysisDurationMillis >= 0) {

                        long estimatedTotalSeconds =
                                estimatedAnalysisDurationMillis / 1000L;

                        if (elapsedSeconds < estimatedTotalSeconds) {

                            long remainingSeconds =
                                    estimatedTotalSeconds - elapsedSeconds;

                            long remainingMinutes = remainingSeconds / 60;
                            long remainingSecondsPart = remainingSeconds % 60;

                            statusText += String.format(
                                    " | Est. remaining: ~%02d:%02d",
                                    remainingMinutes,
                                    remainingSecondsPart
                            );

                        } else {

                            statusText += " | Estimate exceeded";
                        }
                    }

                    // Update the label once, with the complete message.
                    statusLabel.setText(statusText);
                }

                    row.getChildren().addAll(documentLabel, statusLabel);

                    // Show Cancel only while cancellation has not been requested.
                    if (!cancellationRequested) {

                        Button cancelButton = new Button("Cancel");

                        cancelButton.setOnAction(event -> {

                            // Ensure this button belongs to the active document.
                            if (documentName.equals(analyzingDocument)) {
                                cancelAnalysis();
                            }
                        });

                        row.getChildren().add(cancelButton);
                    }

                } else if (documentName.equals(recentlyCancelledDocument)) {

                    statusLabel.setText("✓ Cancelled");
                    row.getChildren().addAll(documentLabel, statusLabel);

                } else if (analysisErrors.containsKey(documentName)) {

                    statusLabel.setText("✕ Failed");
                    row.getChildren().addAll(documentLabel, statusLabel);

                } else if (analyses.containsKey(documentName)) {

                    Long duration = analysisDurations.get(documentName);

                    if (duration != null) {

                        long minutes = duration / 60;
                        long seconds = duration % 60;

                        statusLabel.setText(
                                String.format(
                                        "✓ Analyzed in %02d:%02d",
                                        minutes,
                                        seconds
                                )
                        );

                    } else {

                        statusLabel.setText("✓ Analyzed");
                    }
                    row.getChildren().addAll(documentLabel, statusLabel);

                } else {

                    statusLabel.setText("○ Not Analyzed");
                    row.getChildren().addAll(documentLabel, statusLabel);
                }

                setText(null);
                setGraphic(row);
            }
        });

        documentList.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {

                    // newValue is the document the user just selected.
                    if (newValue == null) {
                        analysisDisplay.clear();
                        return;
                    }

                    // Retrieve any previously stored analysis for this document.
                    String savedAnalysis = analyses.get(newValue);

                    if (savedAnalysis != null) {
                        analysisDisplay.setText(savedAnalysis);
                    } else {
                        analysisDisplay.setText("This document has not been analyzed yet.");
                    }
                });

        // Allow the analysis display to occupy more horizontal space.
        analysisDisplay.setPrefWidth(500);

        // Add both controls to the horizontal layout.
        contentArea.getChildren().addAll(documentList, analysisDisplay);

        // Arrange the action buttons horizontally.
        HBox buttonArea = new HBox(10);
        buttonArea.getChildren().addAll(
                uploadButton,
                deleteButton,
                analyzeButton,
                clearAnalysisButton,
                clearButton,
                restartAIButton
        );

        // Stack the main content and button row vertically.
        VBox layout = new VBox(10);
        layout.getChildren().addAll(contentArea, buttonArea, aiStatusLabel);
        Scene scene = new Scene(layout, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void clearSelectedAnalysis() {

        // Identify the currently selected document.
        String selectedDocument = documentList.getSelectionModel().getSelectedItem();

        // Make sure a document is selected.
        if (selectedDocument == null) {
            return;
        }

        // Prevent changes while an analysis is running.
        if (analysisRunning) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Analysis in Progress");
            warning.setHeaderText("Cannot clear analysis while the AI is working.");
            warning.showAndWait();
            return;
        }

        // Ask the user to confirm the deletion.
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Clear Analysis");
        confirmation.setHeaderText("Remove the saved analysis?");
        confirmation.setContentText(
                "The document \"" + selectedDocument + "\" will be preserved."
        );

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        try {

            // Remove the analysis from memory and disk.
            invalidateAnalysis(selectedDocument);

            // Refresh the document list to reflect the cleared analysis.
            documentList.refresh();

            // Remove any previous analysis error.
            analysisErrors.remove(selectedDocument);
            analysisDurations.remove(selectedDocument);

            // Update the interface only after successful deletion.
            analysisDisplay.setText("This document has not been analyzed yet.");

        } catch (IOException e) {

            // Inform the user if the saved analysis could not be deleted.
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Clear Analysis Failed");
            errorAlert.setHeaderText("Could not remove the saved analysis.");
            errorAlert.setContentText(e.getMessage());
            errorAlert.showAndWait();

        }
    }

    private void analyzeDocument() {

        if (analysisRunning) {
            analysisDisplay.setText(
                    "Qwen is already analyzing a document. Please wait."
            );
            return;
        }

        // Prevent analysis requests when Qwen is unavailable.
        if (!modelServer.isServerReady()) {

            Alert warning = new Alert(Alert.AlertType.WARNING);

            warning.setTitle("AI Unavailable");
            warning.setHeaderText("Qwen is not ready.");
            warning.setContentText(
                    "Please wait for the AI status to show ONLINE before analyzing."
            );

            warning.showAndWait();

            return;
        }

        // Get the document name currently selected in the ListView.
        String selectedDocument =
                documentList.getSelectionModel().getSelectedItem();

        // Do nothing if the user hasn't selected a document.
        if (selectedDocument == null) {
            return;
        }

        // Clear any previous cancellation confirmation for this document.
        if (selectedDocument.equals(recentlyCancelledDocument)) {

            if (cancellationConfirmation != null) {
                cancellationConfirmation.stop();
                cancellationConfirmation = null;
            }

            recentlyCancelledDocument = null;
        }

        // Holds the input size measured by the background task.
        AtomicInteger inputCharacterCount = new AtomicInteger(0);

        // Create a background job that will eventually return a String.
        Task<String> analysisTask = new Task<>() {

            @Override
            protected String call() throws Exception {

                // Retrieve the processed text using the selected document's name.
                String documentText = reader.readFile(selectedDocument);

                inputCharacterCount.set(documentText.length());

                // Notify the JavaFX thread once the document size is known.
                Platform.runLater(() -> {

                    if (!analysisRunning
                            || !selectedDocument.equals(analyzingDocument)
                            || cancellationRequested) {
                        return;
                    }

                    long estimatedMillis = estimateAnalysisDuration(
                            inputCharacterCount.get()
                    );

                    estimatedAnalysisDurationMillis = estimatedMillis;

                    if (estimatedMillis >= 0) {
                        System.out.println(
                                "Estimated total analysis time: "
                                        + estimatedMillis / 1000
                                        + " seconds."
                        );
                    } else {
                        System.out.println(
                                "Analysis time estimate unavailable: "
                                        + "insufficient comparable history."
                        );
                    }
                });

                // Give Qwen instructions along with the document's contents.
                String prompt =

                        // Establish the model's role.
                        "You are a document analysis assistant.\n" +

                                // Explain the overall objective.
                                "Analyze the document and identify its purpose.\n" +

                                // Specify the information we want extracted.
                                "Identify important people, organizations, locations, " +
                                "dates, times, events, activities, and numerical values.\n" +

                                // Preserve relationships between extracted information.
                                "Explain what each detail refers to. " +
                                "Do not confuse event dates with reservation dates.\n" +

                                // Reduce unsupported conclusions.
                                "Only use information explicitly stated in the document. " +
                                "Do not invent missing details.\n" +

                                // Give the model an output structure.
                                "Organize your response into:\n" +
                                "1. Document Type\n" +
                                "2. Document Purpose\n" +
                                "3. Key Information\n" +
                                "4. Important Dates and Locations\n" +
                                "5. Missing or Unclear Information\n\n" +

                                // Separate our instructions from the source material.
                                "DOCUMENT:\n" +
                                documentText;

                // Send the prompt to Qwen and wait for its response.
                return model.analyzeText(prompt);
            }
        };
        // Let the user know that analysis is currently running.
        analysisDisplay.setText("Analyzing document... Please wait.");

        // Run the analysis on a background thread.
        Thread analysisThread = new Thread(() -> {

            try {
                analysisTask.run();

            } finally {

                // This executes when the background thread has returned.
                Platform.runLater(() -> {

                    analysisWorkerFinished = true;

                    // Check whether the JavaFX outcome has also been handled.
                    checkAnalysisCompletion();
                });
            }
        });

        // Remember which task belongs to the active analysis.
        currentAnalysisTask = analysisTask;

        analysisRunning = true;
        cancellationRequested = false;
        analysisWorkerFinished = false;
        analysisOutcomeHandled = false;
        analyzingDocument = selectedDocument;

        // Start measuring total analysis time.
        analysisStartTime = System.nanoTime();

        // Each new analysis starts without a prediction.
        estimatedAnalysisDurationMillis = -1;

        analysisTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    if (analysisRunning) {
                        documentList.refresh();
                    }
                })
        );

        analysisTimer.setCycleCount(Timeline.INDEFINITE);
        analysisTimer.play();

        documentList.refresh();

        // Runs on the JavaFX Application Thread after the task succeeds.
        analysisTask.setOnSucceeded(event -> {

            if (cancellationRequested || analysisTask.isCancelled()) {
                return;
            }

            // Record how long the successful analysis took.
            long elapsedMillis =
                    (System.nanoTime() - analysisStartTime)
                            / 1_000_000L;

            long elapsedSeconds = elapsedMillis / 1000L;

            analysisDurations.put(selectedDocument, elapsedSeconds);

            // Preserve the measurement for future estimates.
            AnalysisTimingRecord timingRecord = new AnalysisTimingRecord(
                    selectedDocument,
                    inputCharacterCount.get(),
                    elapsedMillis,
                    Instant.now(),
                    "Qwen3.5-0.8B"
            );

            try {
                analysisTimingStore.saveRecord(timingRecord);
            } catch (IOException e) {
                System.err.println(
                        "Failed to save timing record: " + e.getMessage()
                );
            }

            // Clear any previous analysis error for this document.
            analysisErrors.remove(selectedDocument);

            // Retrieve the completed analysis.
            String result = analysisTask.getValue();

            // Save the analysis in memory.
            analyses.put(selectedDocument, result);

            // Save the analysis to disk.
            try {
                analysisStore.saveAnalysis(selectedDocument, result);
            } catch (IOException e) {
                System.err.println("Failed to save analysis for " + selectedDocument + ": " + e.getMessage()
                );
            }

            // Refresh the document list after recording the completed analysis.
            documentList.refresh();

            // Only display the result if its document is still selected.
            if (selectedDocument.equals(documentList.getSelectionModel().getSelectedItem())) {

                analysisDisplay.setText(result);
            }
            // The successful result has been processed.
            analysisOutcomeHandled = true;
            checkAnalysisCompletion();
        });

        analysisTask.setOnCancelled(event -> {

            // Cancellation was requested, but the worker may still be running.
            if (selectedDocument.equals(
                    documentList.getSelectionModel().getSelectedItem())) {

                analysisDisplay.setText(
                        "Cancellation requested. Waiting for analysis to stop..."
                );
            }

            // JavaFX has processed the cancellation outcome.
            analysisOutcomeHandled = true;

            // This will only reset ISSAC if the worker has also finished.
            checkAnalysisCompletion();
        });

        // Runs on the JavaFX Application Thread if call() throws an exception.
        analysisTask.setOnFailed(event -> {

            // Cancellation is not an analysis failure.
            if (cancellationRequested || analysisTask.isCancelled()) {
                return;
            }

            Throwable error = analysisTask.getException();

            String errorMessage = "Analysis failed: " + error.getMessage();

            // Remember which document experienced the failure.
            analysisErrors.put(selectedDocument, errorMessage);

            // Refresh the document list to show the failed status.
            documentList.refresh();

            // Only show the error if the failed document is still selected.
            if (selectedDocument.equals(
                    documentList.getSelectionModel().getSelectedItem())) {

                analysisDisplay.setText(errorMessage);
            }

            // The failure has been processed.
            analysisOutcomeHandled = true;
            checkAnalysisCompletion();
        });

        // Allow the application to close without waiting for this thread.
        analysisThread.setDaemon(true);

        // Begin executing the task in the background.
        analysisThread.start();
    }

    private void deleteDocument() {

        // Prevent document deletion while an analysis is running.
        if (analysisRunning) {

            Alert warning = new Alert(Alert.AlertType.WARNING);

            warning.setTitle("Analysis in Progress");
            warning.setHeaderText("Cannot delete documents during analysis.");
            warning.setContentText(
                    "Please wait for the current analysis to finish."
            );

            warning.showAndWait();

            return;
        }

        String selectedDocument = documentList.getSelectionModel().getSelectedItem();

        if (selectedDocument == null) {
            return;
        }
        // Does delete things
        try {
            reader.docDelete(selectedDocument);
            documentList.getItems().remove(selectedDocument);
            analysisStore.deleteAnalysis(selectedDocument);

            // Remove the analysis from the in-memory cache as well.
            analyses.remove(selectedDocument);
            analysisDurations.remove(selectedDocument);

        } catch (IOException e) {
            System.out.println("Delete failed: " + e.getMessage());
        }

    }

    private void clearSession() {

        try {
            reader.clearAllDocuments();
            analysisStore.clearAllAnalyses();

            analyses.clear();
            analysisDurations.clear();
            documentList.getItems().clear();
            analysisDisplay.clear();

            System.out.println("ISSAC session cleared.");

        } catch (IOException e) {
            System.err.println(
                    "Session cleanup incomplete: " + e.getMessage()
            );
        }
    }

    private void confirmClearSession() {

        if (analysisRunning) {

            Alert warning = new Alert(Alert.AlertType.WARNING, "Please wait for the current analysis to finish before clearing the session.", ButtonType.OK
            );

            warning.setHeaderText("Analysis in Progress");
            warning.showAndWait();

            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete all temporary documents and analyses?",
                ButtonType.YES,
                ButtonType.NO
        );

        confirmation.setHeaderText("Clear ISSAC Session");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                clearSession();
            }
        });
    }

    private void uploadFile(Stage primaryStage) {

        // Safeguard against already running analysis
        if (analysisRunning) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Analysis in Progress");
            warning.setHeaderText("Please wait until Qwen finishes analyzing.");
            warning.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Document");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        // Safeguard against no file uploaded
        if (selectedFile == null) {
            return; // Cancels attempt
        }

        String originalFileName = selectedFile.getName();

        int extensionIndex = originalFileName.lastIndexOf('.');

        String suggestedName = extensionIndex > 0
                ? originalFileName.substring(0, extensionIndex)
                : originalFileName;

        TextInputDialog namePrompt = new TextInputDialog(suggestedName);
        namePrompt.setTitle("Document Name");
        namePrompt.setHeaderText("Enter a name for this document:");

        Optional<String> result = namePrompt.showAndWait();

        // Safeguard against no name entered.
        if (result.isEmpty()) {
            return; // Cancels attempt
        }

        String enteredName = result.get();

        // Safeguard against just "___"
        if (enteredName.isBlank()) {
            Alert invalidName = new Alert(Alert.AlertType.WARNING);
            invalidName.setTitle("Invalid Document Name");
            invalidName.setHeaderText("Document name cannot be empty.");
            invalidName.showAndWait();
            return;
        }

        String documentName = reader.convertName(enteredName);

        // Safeguard against invalid name entered
        if (documentName.isBlank()) {
            Alert invalidName = new Alert(Alert.AlertType.WARNING);
            invalidName.setTitle("Invalid Document Name");
            invalidName.setHeaderText("Please enter a valid document name.");
            invalidName.showAndWait();
            return;
        }

        System.out.println("ISSAC document name: " + documentName);

        Path sourcePath = selectedFile.toPath();
        Path uploadsDirectory = Path.of("src", "data", "uploads");

        String extension = extensionIndex > 0
                ? originalFileName.substring(extensionIndex)
                : ".tmp";
        // Prompts overwrite document feature
        boolean overWrite = false;

        if (reader.isUploaded(documentName)) {

            Alert overwritePrompt = new Alert(Alert.AlertType.CONFIRMATION);

            overwritePrompt.setTitle("Document Already Exists");
            overwritePrompt.setHeaderText(documentName + " already exists.");
            overwritePrompt.setContentText("Do you want to replace the existing document?");

            Optional<ButtonType> choice = overwritePrompt.showAndWait();

            if (choice.isEmpty() || choice.get() != ButtonType.OK) {
                return;
            }

            overWrite = true;
        }

        Path destinationPath = null;

        // Copies source file into uploaded folder
        try {
            Files.createDirectories(uploadsDirectory);

            destinationPath = Files.createTempFile(
                    uploadsDirectory,
                    "issac_",
                    extension
            );

            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Copied to: " + destinationPath);

            reader.readDoc(destinationPath.toString(), documentName, overWrite);

            // Invalidate the previous analysis if the document was overwritten.
            if (overWrite) {
                invalidateAnalysis(documentName);

                // Only update the display if this document is selected.
                if (documentName.equals(documentList.getSelectionModel().getSelectedItem())) {
                    analysisDisplay.setText("This document has not been analyzed yet.");
                }
            }

            // Add the name to the viewable list if not already
            if (!overWrite) {
                documentList.getItems().add(documentName);
            }

        } catch (IOException e) {
            System.out.println("Upload failed: " + e.getMessage());
        } finally {
            if (destinationPath != null) {
                try {
                    Files.deleteIfExists(destinationPath);
                } catch (IOException e) {
                    System.err.println("Temporary file cleanup failed: " + e.getMessage());
                }
            }
        }
    }

    private void invalidateAnalysis(String documentName) throws IOException {

        // Remove the saved analysis from disk first.
        analysisStore.deleteAnalysis(documentName);

        // Only clear the in-memory result after disk cleanup succeeds.
        analyses.remove(documentName);
        analysisDurations.remove(documentName);
    }

    private void restartAI() {

        // Prevent repeated clicks while a restart is underway.
        restartAIButton.setDisable(true);

        // Let the user know we're attempting to start Qwen.
        updateAIStatus("STARTING");

        Thread restartThread = new Thread(() -> {

            try {

                // Avoid launching another process if Qwen is already running.
                if (!modelServer.isServerRunning()) {
                    modelServer.startServer();
                }

            } catch (IOException e) {

                Platform.runLater(() -> {
                    updateAIStatus("OFFLINE");

                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("AI Startup Failed");
                    errorAlert.setHeaderText("Unable to start Qwen.");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();
                });

            } finally {

                Platform.runLater(() -> restartAIButton.setDisable(false));
            }
        });

        restartThread.setDaemon(true);
        restartThread.start();
    }

    private void updateAIStatus(String status) {

        aiStatusLabel.setText("AI Status: " + status);

        switch (status) {

            case "ONLINE":
                aiStatusLabel.setStyle(
                        "-fx-text-fill: green; -fx-font-weight: bold;"
                );
                break;

            case "STARTING":
                aiStatusLabel.setStyle(
                        "-fx-text-fill: orange; -fx-font-weight: bold;"
                );
                break;

            case "PORT CONFLICT":
                aiStatusLabel.setStyle(
                        "-fx-text-fill: #B8860B; -fx-font-weight: bold;"
                );
                break;

            default:
                aiStatusLabel.setStyle(
                        "-fx-text-fill: red; -fx-font-weight: bold;"
                );
                break;
        }
    }

    private void monitorAIStatus() {

        Thread monitorThread = new Thread(() -> {

            while (!Thread.currentThread().isInterrupted()) {

                boolean running = modelServer.isServerRunning();
                boolean ready = running && modelServer.isServerReady();

                String status;

                if (ready) {

                    status = "ONLINE";

                } else if (running) {

                    status = "STARTING";

                } else if (modelServer.isPortOccupied()) {

                    status = "PORT CONFLICT";

                } else {

                    status = "OFFLINE";
                }

                Platform.runLater(() -> updateAIStatus(status));

                try {
                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void finishAnalysisTask() {

        // Stop the analysis stopwatch.
        if (analysisTimer != null) {
            analysisTimer.stop();
            analysisTimer = null;
        }

        analysisStartTime = 0;

        // Remember whether this analysis was cancelled before resetting its state.
        boolean wasCancelled = cancellationRequested;

        // Remember which document was being analyzed.
        String completedDocument = analyzingDocument;

        // Stop updating the cancellation timer.
        if (cancellationTimer != null) {
            cancellationTimer.stop();
            cancellationTimer = null;
        }

        cancellationStartTime = 0;

        analysisRunning = false;
        cancellationRequested = false;
        analysisWorkerFinished = false;
        analysisOutcomeHandled = false;
        analyzingDocument = null;
        currentAnalysisTask = null;

        documentList.refresh();

        // Confirm cancellation only after the analysis worker has finished.
        if (wasCancelled && completedDocument != null) {
            showCancellationConfirmation(completedDocument);
        }
    }

    private void cancelAnalysis() {

        // Only cancel an active analysis.
        if (!analysisRunning || currentAnalysisTask == null) {
            return;
        }

        // Prevent duplicate cancellation requests.
        if (cancellationRequested) {
            return;
        }

        // Mark cancellation as requested.
        cancellationRequested = true;

        // Start tracking how long cancellation takes.
        startCancellationTimer();

        // Update the active document's status.
        documentList.refresh();

        // Inform the analyst that cancellation is underway.
        analysisDisplay.setText(
                "Cancelling analysis... Please wait."
        );

        // Request cancellation of the background task.
        currentAnalysisTask.cancel(true);

        // Keep analysisRunning true until the worker finishes.
    }

    private void showCancellationConfirmation(String documentName) {

        // Stop any previous confirmation timer.
        if (cancellationConfirmation != null) {
            cancellationConfirmation.stop();
        }

        recentlyCancelledDocument = documentName;
        documentList.refresh();

        // Keep the confirmation visible for two seconds.
        cancellationConfirmation =
                new PauseTransition(Duration.seconds(2));

        cancellationConfirmation.setOnFinished(event -> {

            recentlyCancelledDocument = null;
            cancellationConfirmation = null;

            documentList.refresh();
        });

        cancellationConfirmation.play();
    }

    private void startCancellationTimer() {

        cancellationStartTime = System.nanoTime();

        // Stop an existing timer before creating another.
        if (cancellationTimer != null) {
            cancellationTimer.stop();
        }

        cancellationTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    if (cancellationRequested) {
                        documentList.refresh();
                    }
                })
        );

        cancellationTimer.setCycleCount(Timeline.INDEFINITE);
        cancellationTimer.play();
    }

    private long estimateAnalysisDuration(
            int inputCharacterCount
    ) {
        return analysisTimeEstimator.estimateDuration(
                analysisTimingStore.getRecords(),
                inputCharacterCount,
                "Qwen3.5-0.8B"
        );
    }

    private void checkAnalysisCompletion() {

        // Only reset ISSAC when both parts of the task are finished.
        if (analysisWorkerFinished && analysisOutcomeHandled) {

            finishAnalysisTask();
        }
    }

    @Override
    public void stop() {
        modelServer.stopServer();
    }
}
