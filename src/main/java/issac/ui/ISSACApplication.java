package issac.ui;

import javafx.stage.FileChooser;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.control.TextInputDialog;
import issac.analysis.AnalysisStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import issac.document.DocRead;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import issac.ai.LocalModel;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import issac.ai.ModelServer;

public class ISSACApplication extends Application {

    private final DocRead reader = new DocRead();
    private final ListView<String> documentList = new ListView<>();
    private final AnalysisStore analysisStore = new AnalysisStore();
    private final LocalModel model = new LocalModel();
    private final ModelServer modelServer = new ModelServer();
    private final TextArea analysisDisplay = new TextArea();
    private final Map<String, String> analyses = new HashMap<>();
    private final Map<String, String> analysisErrors = new HashMap<>();
    private boolean analysisRunning = false;

    @Override
    public void start(Stage primaryStage) {

        try {
            modelServer.startServer();
        } catch (IOException e) {
            System.err.println("Unable to start Qwen: " + e.getMessage());
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
        Button clearButton = new Button("Clear Session");

        clearButton.setOnAction(event -> confirmClearSession());
        uploadButton.setOnAction(event -> uploadFile(primaryStage));
        deleteButton.setOnAction(event -> deleteDocument());
        analyzeButton.setOnAction(event -> analyzeDocument());

        // Arrange the document list and analysis display side by side.
        HBox contentArea = new HBox(10);

        // Give the document list a preferred width.
        documentList.setPrefWidth(250);

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
                analyzeButton
        );

        // Stack the main content and button row vertically.
        VBox layout = new VBox(10);
        layout.getChildren().addAll(contentArea, buttonArea);
        Scene scene = new Scene(layout, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void analyzeDocument() {

        if (analysisRunning) {
            analysisDisplay.setText(
                    "Qwen is already analyzing a document. Please wait."
            );
            return;
        }


        // Get the document name currently selected in the ListView.
        String selectedDocument =
                documentList.getSelectionModel().getSelectedItem();

        // Do nothing if the user hasn't selected a document.
        if (selectedDocument == null) {
            return;
        }

        // Create a background job that will eventually return a String.
        Task<String> analysisTask = new Task<>() {

            @Override
            protected String call() throws Exception {

                // Retrieve the processed text using the selected document's name.
                String documentText = reader.readFile(selectedDocument);

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

        // Create a separate thread to execute our analysis task.
        Thread analysisThread = new Thread(analysisTask);
        analysisRunning = true;
        analysisThread.start();

        // Runs on the JavaFX Application Thread after the task succeeds.
        analysisTask.setOnSucceeded(event -> {

            analysisRunning = false;

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

            // Only display the result if its document is still selected.
            if (selectedDocument.equals(documentList.getSelectionModel().getSelectedItem())) {

                analysisDisplay.setText(result);
            }
        });

        // Runs on the JavaFX Application Thread if call() throws an exception.
        analysisTask.setOnFailed(event -> {

            analysisRunning = false;

            Throwable error = analysisTask.getException();

            String errorMessage = "Analysis failed: " + error.getMessage();

            // Remember which document expierenced the failure.
            analysisErrors.put(selectedDocument, errorMessage);

            // Only show the error if the failed document is still selected.
            if (selectedDocument.equals(documentList.getSelectionModel().getSelectedItem())) {

                analysisDisplay.setText(errorMessage);
            }
        });

        // Allow the application to close without waiting for this thread.
        analysisThread.setDaemon(true);

        // Begin executing the task in the background.
        analysisThread.start();
    }

    private void deleteDocument() {
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

            // If you've added the error map, clear that too.
            analysisErrors.remove(selectedDocument);

        } catch (IOException e) {
            System.out.println("Delete failed: " + e.getMessage());
        }

    }

    private void clearSession() {

        try {
            reader.clearAllDocuments();
            analysisStore.clearAllAnalyses();

            analyses.clear();
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

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Document");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        // Safeguard against no file uploaded
        if (selectedFile == null) {
            return; // Cancels attempt
        }

        TextInputDialog namePrompt = new TextInputDialog(selectedFile.getName());

        namePrompt.setTitle("Document Name");
        namePrompt.setHeaderText("Enter a name for this document:");

        Optional<String> result = namePrompt.showAndWait();

        // Safeguard against no name entered.
        if (result.isEmpty()) {
            return; // Cancels attempt
        }

        String documentName = result.get();

        System.out.println("ISSAC document name: " + documentName);

        Path sourcePath = selectedFile.toPath();
        Path uploadsDirectory = Path.of("src", "data", "uploads");
        Path destinationPath = uploadsDirectory.resolve(sourcePath.getFileName());

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
        // Copies source file into uploaded folder
        try {
            Files.createDirectories(uploadsDirectory);
            Files.copy(sourcePath, destinationPath);

            System.out.println("Copied to: " + destinationPath);

            reader.readDoc(destinationPath.toString(), documentName, overWrite);
            Files.delete(destinationPath);

            // Add the name to the viewable list if not already
            if (!overWrite) {
                documentList.getItems().add(documentName);
            }

        } catch (IOException e) {
            System.out.println("Upload failed: " + e.getMessage());
        }
    }

    private void invalidateAnalysis(String documentName) throws IOException {

        // Remove the saved analysis from disk first.
        analysisStore.deleteAnalysis(documentName);

        // Only clear the in-memory result after disk cleanup succeeds.
        analyses.remove(documentName);
    }

    @Override
    public void stop() {
        modelServer.stopServer();
    }
}
