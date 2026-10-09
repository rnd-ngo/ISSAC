// Reads the document and identifies the file type.
// Stores the document in "readable"
package issac.document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

public class DocRead {

    private final Map<String,Path> documents = new HashMap<>();
    private final Path documentsDirectory = Path.of("src", "data", "documents");

    public void readDoc(String filePath, String saveAs, boolean overWrite) throws IOException {

        System.out.println("Uploading...");

        if (isUploaded(saveAs)) {
            System.out.println(saveAs + " is already uploaded as " + convertName(saveAs) + ". \nOverwrite? Y/N");
            if (!overWrite) {
                System.out.println("Canceled process.");
                return;
            }
            System.out.println("Overwriting...");
        }

        String fileName = convertName(saveAs);
        Path output = documentsDirectory.resolve(fileName + ".txt"); // Housing of Content
        Path path = Path.of(filePath); // File location
        String fileType = getFileType(path);
        String content = "";

        switch (fileType) {
            case "txt":
                content = Files.readString(path); // Reads content
                break;
            case "pdf":
                content = readPDF(path);
                break;
            case "docx":
                content = readDOCX(path);
                break;
            default:
                System.out.println("Unsupported File Type.");
                return;
        }
        Files.writeString(output,content); // Writes into readable
        documents.put(fileName, output); // Store document into a Map

        System.out.println("Uploaded : " + saveAs + ".\nDocument name : " + fileName + ".txt");

    }

    private String readPDF(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    private String readDOCX(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path);
             XWPFDocument document = new XWPFDocument(input);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)
        ) {
            return extractor.getText();
        }
    }

    private String getFileType(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int dotLocation = fileName.lastIndexOf(".");
        return fileName.substring(dotLocation+1).toLowerCase();
    }

    private String convertName(String name) {
        String fileName = name.toLowerCase(); // Lowercases letters
        fileName = fileName.replace(" ","_"); // Need to refine these
        fileName = fileName.replaceAll("[^a-z0-9_-]",""); // Replace special characters
        return fileName;
    }

    public boolean isUploaded(String docName) {
        return documents.containsKey(convertName(docName));
    }

    public String readFile(String fileName) throws IOException {
        return Files.readString(documents.get(convertName(fileName)));
    }

    public void docDelete(String docName) throws IOException {
        Path document = documents.get(convertName(docName));

        if (document != null) {
            Files.delete(document);
            documents.remove(convertName(docName));
            System.out.println("Successfully Deleted " + convertName(docName));
        }
        else {
            System.out.println("Error: " + convertName(docName) + " could not be located.");
        }
    }

    public void clearAllDocuments() throws IOException {

        // Delete the extracted documents.
        clearTextFiles(documentsDirectory);

        // Delete the original files staged during upload.
        clearFiles(Path.of("src", "data", "uploads"));

        // Only forget the registry after cleanup succeeds.
        documents.clear();
    }

    private void clearTextFiles(Path directory) throws IOException {

        if (!Files.isDirectory(directory)) {
            return;
        }

        try (var files = Files.list(directory)) {
            for (Path file : files.toList()) {
                if (Files.isRegularFile(file) && file.getFileName().toString().endsWith(".txt")) {
                    Files.delete(file);
                }
            }
        }
    }

    private void clearFiles(Path directory) throws IOException {

        if (!Files.isDirectory(directory)) {
            return;
        }

        try (var files = Files.list(directory)) {
            for (Path file : files.toList()) {
                if (Files.isRegularFile(file)) {
                    Files.delete(file);
                }
            }
        }
    }

    public Set<String> getDocumentNames() {
        // Return a copy so the UI cannot modify our internal registry.
        return new HashSet<>(documents.keySet());
    }
    public void loadSavedDocuments() throws IOException {

        // Make sure the storage directory exists.
        Files.createDirectories(documentsDirectory);

        // Open a stream of files inside the document directory.
        try (var files = Files.list(documentsDirectory)) {

            files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .forEach(path -> {

                        // Remove the .txt extension to recover the document key.
                        String fileName = path.getFileName().toString();
                        String documentName =
                                fileName.substring(0, fileName.length() - 4);

                        // Rebuild the document registry.
                        documents.put(documentName, path);
                    });
        }
    }

}