package org.perpectiveteam.plugins.aisummarize.ai.connector.providers;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.perpectiveteam.plugins.aisummarize.ai.connector.AIConnector;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OpenAIConnector implements AIConnector {
    private static final Logger LOG = Loggers.get(OpenAIConnector.class);
    private static final String PROVIDER_NAME = "openai";
    private static final String DEFAULT_MODEL = "gpt-4";
    
    private final String apiKey;
    private final String model;

    public OpenAIConnector(String apiKey) {
        this(apiKey, DEFAULT_MODEL);
    }

    public OpenAIConnector(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }
    
    @Override
    public String getCompletion(String prompt) {
        //TODO: remove hardcoded model & API url
        try {
            URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode payload = mapper.createObjectNode();
            payload.put("model", "nvidia/llama-3.1-nemotron-ultra-253b-v1:free");
            payload.put("temperature", 0.7);

            ArrayNode messages = payload.putArray("messages");
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a code reviewer AI.");
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            // Send JSON payload
            try (OutputStream os = conn.getOutputStream()) {
                os.write(mapper.writeValueAsBytes(payload));
            }

            // Handle response
            int status = conn.getResponseCode();
            InputStream in = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
            JsonNode response = mapper.readTree(in);

            if (status >= 400) {
                throw new RuntimeException("OpenAI API error (" + status + "): " + response.toString());
            }

            return response.get("choices").get(0).get("message").get("content").asText();

        } catch (Exception e) {
            LOG.error("Error communicating with OpenAI", e);
            throw new RuntimeException("Error communicating with OpenAI", e);
        }
    }
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
}
