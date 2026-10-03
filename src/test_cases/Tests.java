import issac.document.DocRead;

import javax.print.Doc;
import java.io.IOException;

public class Tests {
    public static void main(String[] args) throws IOException {
//        test1();
//        test2();
//        test3();
        test4();
    }
    public static void test1() throws IOException {
        System.out.println("Starting Test");
        DocRead reader = new DocRead();
        reader.readDoc("src/test_cases/test_documents/tests.txt", "document1", true);
        String content = reader.readFile("document1");
        System.out.println(content);
        reader.docDelete("document1");
        reader.isUploaded("document1");
        System.out.println("End of Test");
    }
    public static void test2() throws IOException {
        System.out.println("Starting Test");
        DocRead reader = new DocRead();
        reader.readDoc("src/test_cases/test_documents/testsmore.txt", "We1rd N@me:)", true);
        String content = reader.readFile("We1rd N@me:)");

        System.out.println(content);
        System.out.println("Document processed.");

        reader.docDelete("document1");
        reader.isUploaded("document1");
        reader.isUploaded("We1rd N@me:)");
        System.out.println("End of Test");
    }
    public static void test3() throws IOException {
        System.out.println("Starting Test");
        DocRead reader = new DocRead();
        reader.readDoc("src/test_cases/test_documents/emptytest.txt", "is it really empty?", true);
        String content = reader.readFile("is it really empty?");
        System.out.println(content);
        reader.isUploaded("is it really empty");
        reader.docDelete("isitreallyempty");
        reader.docDelete("is it really empty");
        System.out.println("End of Test");
    }
    public static void test4() throws IOException {
        System.out.println("Starting Test");
        DocRead reader = new DocRead();
        reader.readDoc("src/test_cases/test_documents/Statement for Joe's Crab Shack.pdf", "PDF Testing", true);
        String content = reader.readFile("PdF Testing");
        System.out.println(content);
        reader.readDoc("src/test_cases/test_documents/tests.txt", "PDF Testing", true);
        content = reader.readFile("PdF Testing");
        System.out.println(content);
        //reader.docDelete("PDf tESTING");
        System.out.println("End of Test");
    }
}
