// Reads the document and identifies the file type.
// Stores the document in "readable"
package issac.document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocRead {

    private final Map<String,Path> documents = new HashMap<>();
    private final Path documentsDirectory = Path.of("src", "data", "documents");
    private final Path uploadsDirectory = Path.of("src", "data", "uploads"); // Once it has been built all initial documents should go here

    public void readDoc(String filePath, String saveAs, boolean overWrite) throws IOException {

        System.out.println("Uploading...");

        if (isUploaded(saveAs)) {
            System.out.println(saveAs + " is already uploaded as " + convertName(saveAs) + ". \nOverwrite? Y/N");
            if (!overWrite) {
                System.out.println("Canceled process.");
                return;
            }
            System.out.println("Overwriting...");
            docDelete(saveAs);
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

            default:

        }
        Files.writeString(output,content); // Writes into readable
        documents.put(fileName, output); // Store document into a Map

        System.out.println("Uploaded : " + saveAs + ".\nDocument name : " + fileName + ".txt");

    }

    private String readPDF(Path path) throws IOException {
        PDDocument document = Loader.loadPDF(path.toFile());
        PDFTextStripper stripper = new PDFTextStripper();
        String content = stripper.getText(document);
        document.close();
        return content;
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

        boolean uploaded = documents.containsKey(convertName(docName));

        if (uploaded) {
            System.out.println(docName + " has already been Uploaded as " + convertName(docName) + ".");
            return true;
        }
        System.out.println("Error: " + docName + " does not exist. Check the file's name for any errors.");
        return false;
    }

    public String readFile(String fileName) throws IOException {
        return Files.readString(documents.get(convertName(fileName)));
    }

    public void docDelete(String docName) throws IOException {
        Path document = documents.remove(convertName(docName));

        if (document != null) {
            Files.delete(document);
            System.out.println("Successfully Deleted " + convertName(docName));
        }
        else {
            System.out.println("Error: " + convertName(docName) + " could not be located.");
        }
    }

}