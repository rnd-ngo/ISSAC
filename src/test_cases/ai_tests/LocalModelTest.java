package ai_tests;

import issac.ai.LocalModel;

public class LocalModelTest {

    public static void main(String[] args) {

        // Create an instance of our local AI connector.
        LocalModel model = new LocalModel();

        // Send a test message to Qwen and store its response.
        String response = model.analyzeText(
                "Reply with exactly: Hello ISSAC!"
        );

        // Display the response in IntelliJ's Run console.
        System.out.println(response);
    }
}