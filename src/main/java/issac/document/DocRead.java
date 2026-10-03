// Reads the document and identifies the file type.
// Stores the document in "readable"
package main.java.issac.document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DocRead {

    private final Map<String,Path> documents = new HashMap<>();
    private final Path documentsDirectory = Path.of("data", "documents");

    public void readDoc(String filePath, String saveAs) throws IOException {

        String fileName = convertName(saveAs);
        System.out.println(fileName);

        Path output = documentsDirectory.resolve(fileName + ".txt"); // Housing of Content
        Path path = Path.of(filePath); // File location
        String content = Files.readString(path); // Reads content

        Files.writeString(output,content); // Writes into readable
        documents.put(fileName, output); // Store document into a Map

        System.out.println("Success ful upload of File : " + saveAs + ".\nDocument name : " + fileName + ".txt");

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
        System.out.println(docName + " does not exist. Check the file's name for any errors.");
        return false;
    }

    public String readFile(String fileName) throws IOException {

        return Files.readString((Path.of(convertName(fileName) + ".txt")));

    }

    public void docDelete(String docName) throws IOException {
        Path document = documents.remove(convertName(docName));

        if (document != null) {
            Files.delete(document);
            System.out.println("Successfully Deleted " + convertName(docName));
        }
        else {
            System.out.println(convertName(docName) + " has already need deleted or does not exist.");
        }
    }

}