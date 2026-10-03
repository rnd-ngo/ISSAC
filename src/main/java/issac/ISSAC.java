// Starts up the application in addition acts as a central hub
package main.java.issac;

import main.java.issac.document.DocRead;

import java.io.IOException;

public class ISSAC {

     public static void main(String[] args) throws IOException {

        DocRead reader = new DocRead();
        reader.readDoc("src/test_cases/test_documents/tests.txt", "document1");

        String content = reader.readFile("document1");

        System.out.println(content);
        System.out.println("Document processed.");

        reader.docDelete("document1");

    }

}