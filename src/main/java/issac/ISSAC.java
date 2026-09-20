// Starts up the application in addition acts as a central hub
package issac;

import java.io.IOException;
import issac.document.DocRead;

public class ISSAC {

    public static void main(String[] args) throws IOEception {

        DocRead reader = new DocRead();
        reader.readDoc("src/test_documnets/tests.txt");

        System.out.println("Documenet processed.");

    }

}