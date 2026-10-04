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
import java.util.Optional;
import issac.document.DocRead;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;

public class ISSACApplication extends Application {

    private final DocRead reader = new DocRead();
    private final ListView<String> documentList = new ListView<>();


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("ISSAC");


        Button uploadButton = new Button("Upload Document(s)");
        Button deleteButton = new Button("Delete Document(s)");
        Button analyzeButton = new Button("Analyze Document(s)");

        uploadButton.setOnAction(event -> uploadFile(primaryStage));
        deleteButton.setOnAction(event -> deleteDocument());
        // analyzeButton.setOnAction(event -> analyzeDocument());
        VBox layout = new VBox(10);
        layout.getChildren().addAll(
                documentList,
                uploadButton,
                deleteButton,
                analyzeButton
        );
        Scene scene = new Scene(layout, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.show();
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

        } catch (IOException e) {
            System.out.println("Delete failed: " + e.getMessage());
        }
    }

    private void uploadFile(Stage primaryStage) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Document");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        // Safeguard against no file uploaded
        if (selectedFile == null) {
            return; // Cancels attempt
        }

        TextInputDialog namePrompt =
                new TextInputDialog(selectedFile.getName());

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
}
