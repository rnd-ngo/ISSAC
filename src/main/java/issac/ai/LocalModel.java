package issac.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class LocalModel implements AIModel {

    // Reusable HTTP client for communicating with llama-server.
    private final HttpClient client = HttpClient.newHttpClient();

    // Address of the locally running AI server.
    private final URI endpoint =
            URI.create("http://127.0.0.1:8080/v1/chat/completions");

    // Required by AIModel: eventually sends text to Qwen for analysis.
    @Override
    public String analyzeText(String text) {

        // Convert the supplied text into the JSON format Qwen expects.
        String jsonBody = buildRequestBody(text);

        // Construct an HTTP request addressed to our local llama-server.
        HttpRequest request = HttpRequest.newBuilder()

                // Specify the destination API endpoint.
                .uri(endpoint)

                // Tell llama-server that we're sending JSON.
                .header("Content-Type", "application/json")

                // Send our JSON string as the body of a POST request.
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))

                // Finish constructing the request.
                .build();

        try {

            // Send the request and wait for the complete server response.
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            // HTTP 200 indicates success; other status codes may indicate errors.
            if (response.statusCode() != 200) {

                // Return an understandable error instead of parsing invalid response data.
                return "AI server error (HTTP " + response.statusCode() + ")";
            }

            // Convert the JSON response string into an object Java can navigate.
            JsonObject responseJson =
                    com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();

            // Find the first generated response in the "choices" array.
            JsonObject choice = responseJson
                    .getAsJsonArray("choices")
                    .get(0)
                    .getAsJsonObject();

            // Get the assistant's message from that response.
            JsonObject message = choice.getAsJsonObject("message");

            // Extract only the final answer, excluding reasoning_content.
            return message.get("content").getAsString();

        } catch (IOException e) {

            // Handle failures such as the server being unavailable.
            return "Connection error: " + e.getMessage();

        } catch (InterruptedException e) {

            // Restore the thread's interrupted status.
            Thread.currentThread().interrupt();

            return "Request interrupted.";
        }
    }

    // Required by AIModel, but image analysis isn't implemented yet.
    @Override
    public String analyzeImage(java.nio.file.Path imagePath) {
        throw new UnsupportedOperationException("Image analysis not implemented yet.");
    }

    // Helper method that creates the JSON request body.
    private String buildRequestBody(String text) {

        // Create the outer JSON object.
        JsonObject body = new JsonObject();

        // Specify the model.
        body.addProperty("model", "Qwen3.5-0.8B");

        // Create the list of conversation messages.
        JsonArray messages = new JsonArray();

        // Create the user message.
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", text);

        // Add the message to the list.
        messages.add(message);

        // Attach the list to the request.
        body.add("messages", messages);

        // Limit the number of tokens Qwen can generate.
        body.addProperty("max_tokens", 300);

        // Request the full response instead of streaming.
        body.addProperty("stream", false);

        // Create optional settings for the model's chat template.
        JsonObject templateSettings = new JsonObject();

        // Request that Qwen skip its extended thinking process.
        templateSettings.addProperty("enable_thinking", false);

        // Attach the settings to the outgoing JSON request.
        body.add("chat_template_kwargs", templateSettings);

        // Serialize the Java JSON object into a String.
        return body.toString();
    }
}