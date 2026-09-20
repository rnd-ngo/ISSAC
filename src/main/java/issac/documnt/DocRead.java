// Reads the docuement and identifies the file type.
// Stores the document in "readable"
package issac.document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DocRead {

    public void readDoc(String filePath) throws IOException {

        Path output = Path.of("readable.txt"); // Housing of Content
        Path path = Path.of(filePath); // File location
        String content = Files.readString(path); // Reads content

        Files.writeString(output,content); // Writes into readable

    }

}