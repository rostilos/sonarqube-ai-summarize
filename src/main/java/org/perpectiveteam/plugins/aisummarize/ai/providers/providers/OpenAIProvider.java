package org.perpectiveteam.plugins.aisummarize.ai.providers.providers;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.perpectiveteam.plugins.aisummarize.ai.providers.AIProvider;
import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OpenAIProvider implements AIProvider {
    private static final Logger LOG = Loggers.get(OpenAIProvider.class);
    private static final String PROVIDER_NAME = "openai";

    private final AiSummarizeConfig aiSummarizeConfig;

    public OpenAIProvider(AiSummarizeConfig aiSummarizeConfig) {
        this.aiSummarizeConfig = aiSummarizeConfig;
    }
    
    @Override
    public String getCompletion(String prompt) {
        String apiKey = aiSummarizeConfig.getAiApiKey();
        String model = aiSummarizeConfig.getAiModel();
        
        try {
            URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
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
