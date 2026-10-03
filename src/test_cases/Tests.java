import main.java.issac.document.DocRead;
import java.io.IOException;

public class Tests {
    public static void main(String[] args) throws IOException {

        DocRead reader = new DocRead();
        reader.readDoc("src/test_cases/test_documents/tests.txt", "document1");
        reader.readDoc("scr/test_cases/test_documents/testsmore.txt", "We1rd N@me:)");
        String content = reader.readFile("We1rd N@me:)");

        System.out.println(content);
        System.out.println("Document processed.");

        reader.docDelete("document1");
        reader.isUploaded("document1");
        reader.isUploaded("We1rd N@me:)");
    }
}
