package org.perpectiveteam.plugins.aisummarize.ai.providers;

import org.perpectiveteam.plugins.aisummarize.config.options.AiClientsList;
import org.perpectiveteam.plugins.aisummarize.ai.providers.providers.OpenAIProvider;
import org.perpectiveteam.plugins.aisummarize.ai.providers.providers.OpenRouterProvider;
import org.perpectiveteam.plugins.aisummarize.config.SummarizeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIProviderFactory {
    private static final Logger LOG = LoggerFactory.getLogger(AIProviderFactory.class);

    private final SummarizeConfig config;

    public AIProviderFactory(SummarizeConfig config) {
        this.config = config;
    }

    public AIProvider createConnector() {
        validateConfig();
        String provider = config.getAiProvider();
        switch (provider.toLowerCase()) {
            case AiClientsList.OPENAI:
                return new OpenAIProvider(config, null);
            case AiClientsList.OPENROUTER:
                return new OpenRouterProvider(config, null);
            default:
                LOG.error("Unknown AI provider: {}.", provider);
                throw new IllegalArgumentException("Unknown AI provider: " + provider);
        }
    }

    private void validateConfig() {
        String provider = config.getAiProvider();
        String apiKey = config.getAiApiKey();
        String model = config.getAiModel();
        if (apiKey.isEmpty()) {
            LOG.error("AI API key is not configured. Please set the {} property.", SummarizeConfig.AI_CLIENT_API_KEY);
            throw new IllegalStateException("AI API key is not configured");
        }
        if(provider.isEmpty()){
            LOG.error("AI Provider is not configured. Please set the {} property.", SummarizeConfig.AI_PROVIDER);
            throw new IllegalStateException("AI Provider is not configured");
        }
        if(model.isEmpty()){
            LOG.error("AI Model is not configured. Please set the {} property.", SummarizeConfig.AI_MODEL);
            throw new IllegalStateException("AI Model is not configured");
        }
    }
}
