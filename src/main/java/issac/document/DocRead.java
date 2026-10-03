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

    public void readDoc(String filePath, String saveAs) throws IOException {

        String fileName = saveAs.toLowerCase(); // Lowercases letters
        fileName = fileName.replace(" ","_"); // Replaces " " with "_" to make it more file-like
        fileName = fileName.replaceAll("[^a-z0-9]",""); // Replace special characters

        Path output = Path.of(fileName + ".txt"); // Housing of Content
        Path path = Path.of(filePath); // File location
        String content = Files.readString(path); // Reads content

        Files.writeString(output,content); // Writes into readable
        documents.put(saveAs, output); // Store document into a List

        System.out.println("Success ful upload of File : " + saveAs + ". Document name : " + fileName);

    }

    public boolean isUploaded(String docName) {

        boolean uploaded = documents.containsKey(docName);

        if (uploaded) {
            System.out.println(docName + " has already been Uploaded");
            return true;
        }
        System.out.println(docName + " does not exist. Check the file's name for any errors.");
        return false;
    }

    public String readFile(String fileName) throws IOException {

        return Files.readString((Path.of(fileName + ".txt")));

    }

    public void docDelete(String docName) throws IOException {
        Path document = documents.remove(docName);

        if (document != null) {
            Files.delete(document);
            documents.remove(docName);
        }
    }

}