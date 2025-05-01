package org.perpectiveteam.plugins.aisummarize.ai.connector;

import org.perpectiveteam.plugins.aisummarize.ai.connector.providers.OpenAIConnector;
import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class AIConnectorFactory {
    private static final Logger LOG = Loggers.get(AIConnectorFactory.class);
    
    private final AiSummarizeConfig config;
    
    public AIConnectorFactory(AiSummarizeConfig config) {
        this.config = config;
    }

    public AIConnector createConnector() {
        String provider = config.getAiProvider();
        
        switch (provider.toLowerCase()) {
            case "openai":
                String apiKey = config.getOpenAiApiKey();
                if (apiKey.isEmpty()) {
                    LOG.error("OpenAI API key is not configured. Please set the {} property.", AiSummarizeConfig.OPENAI_API_KEY);
                    throw new IllegalStateException("OpenAI API key is not configured");
                }
                return new OpenAIConnector(apiKey);
            default:
                LOG.error("Unknown AI provider: {}. Defaulting to OpenAI.", provider);
                return createConnector("openai");
        }
    }
    
    public AIConnector createConnector(String provider) {
        if ("openai".equalsIgnoreCase(provider)) {
            String apiKey = config.getOpenAiApiKey();
            if (apiKey.isEmpty()) {
                LOG.error("OpenAI API key is not configured. Please set the {} property.", AiSummarizeConfig.OPENAI_API_KEY);
                throw new IllegalStateException("OpenAI API key is not configured");
            }
            return new OpenAIConnector(apiKey);
        }
        
        throw new IllegalArgumentException("Unknown AI provider: " + provider);
    }
}
