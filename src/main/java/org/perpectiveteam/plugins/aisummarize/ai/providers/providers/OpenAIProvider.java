package org.perpectiveteam.plugins.aisummarize.ai.providers.providers;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.perpectiveteam.plugins.aisummarize.ai.providers.AIProvider;
import org.perpectiveteam.plugins.aisummarize.config.SummarizeConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OpenAIProvider implements AIProvider {
    private static final String PROVIDER_NAME = "openai";
    private final String apiUrl;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final SummarizeConfig aiSummarizeConfig;

    public OpenAIProvider(
            SummarizeConfig aiSummarizeConfig,
            String apiUrl
    ) {
        this.aiSummarizeConfig = aiSummarizeConfig;
        this.apiUrl = (apiUrl != null) ? apiUrl : getDefaultApiUrl();
    }

    protected String getDefaultApiUrl() {
        return API_URL;
    }
    
    @Override
    public String getCompletion(String prompt) {
        String apiKey = aiSummarizeConfig.getAiApiKey();
        String model = aiSummarizeConfig.getAiModel();
        
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode payload = mapper.createObjectNode();
            payload.put("model", model);
            payload.put("temperature", 0.7);

            ArrayNode messages = payload.putArray("messages");
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a code reviewer AI.");
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(mapper.writeValueAsBytes(payload));
            }

            int status = conn.getResponseCode();
            InputStream in = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
            JsonNode response = mapper.readTree(in);

            if (status >= 400) {
                throw new AiProviderException("AI Provider error (" + status + "): " + response.toString());
            }

            return response.get("choices").get(0).get("message").get("content").asText();

        } catch (Exception e) {
            throw new AiProviderException(String.format("Error communicating with AI Provider: %s", e.getMessage()));
        }
    }
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
}
