// Starts up the application in addition acts as a central hub
import java.io.IOException;

public class ISSAC {

    public static void main(String[] args) throw IOEception {

        DocRead reader = new DocRead();

        reader.readDoc("test_documnet/tests.txt");

        System.out.println("Documenet processed.");

    }

}