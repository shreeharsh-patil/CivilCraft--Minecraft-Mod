package com.civilcraftai.agent.llm;

import com.civilcraftai.CivilCraftAI;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LLMClient {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final String[] FALLBACK_RESPONSES = {
        "Greetings traveler! I am busy tending to the town, but it is a fine day indeed.",
        "A fine day for building a civilization! Do you have any resources to trade?",
        "Our town boundaries are secure for now, but we must stay alert.",
        "I hope our mayors can maintain peaceful relations with our neighbors.",
        "Have you spoken with the other citizens? We are working hard to expand the town hall."
    };

    public static String generateResponse(String npcName, String personality, String conversationHistory, String playerMessage) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "llama3");
            
            String systemPrompt = "You are a Minecraft NPC named " + npcName + ". Your personality is: " + personality + ". " +
                    "Respond to the player in character. Keep it brief (1-3 sentences) to fit in game chat.\n" +
                    "Conversation history:\n" + conversationHistory;
            
            requestBody.addProperty("prompt", systemPrompt + "\nPlayer says: " + playerMessage + "\nResponse:");
            requestBody.addProperty("stream", false);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                return jsonResponse.get("response").getAsString().trim();
            }
        } catch (Exception e) {
            CivilCraftAI.LOGGER.warn("Ollama local connection failed. Using fallback roleplay dialogue. Error: " + e.getMessage());
        }

        // Return a randomized fallback dialogue based on inputs
        int index = Math.abs((npcName + playerMessage).hashCode()) % FALLBACK_RESPONSES.length;
        return FALLBACK_RESPONSES[index];
    }
}
