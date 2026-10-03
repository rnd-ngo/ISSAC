import main.java.issac.document.DocRead;
import java.io.IOException;

public class Tests {
    public static void main(String[] args) throws IOException {
        test1();
        test2();
    }
    public static void test1() throws IOException {
        System.out.println("Starting Test");
        DocRead reader = new DocRead();
        reader.readDoc("src/test_cases/test_documents/tests.txt", "document1");
        String content = reader.readFile("document1");
        System.out.println(content);
        reader.docDelete("document1");
        reader.isUploaded("document1");
        System.out.println("End of Test");
    }
    public static void test2() throws IOException {
        System.out.println("Starting Test");
        DocRead reader = new DocRead();
        reader.readDoc("src/test_cases/test_documents/testsmore.txt", "We1rd N@me:)");
        String content = reader.readFile("We1rd N@me:)");

        System.out.println(content);
        System.out.println("Document processed.");

        reader.docDelete("document1");
        reader.isUploaded("document1");
        reader.isUploaded("We1rd N@me:)");
        reader.docDelete("we1rd_nme");
        System.out.println("End of Test");
    }
}
