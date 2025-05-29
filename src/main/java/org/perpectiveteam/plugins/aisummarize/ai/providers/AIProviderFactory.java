package org.perpectiveteam.plugins.aisummarize.ai.providers;

import org.perpectiveteam.plugins.aisummarize.config.options.AiClientsList;
import org.perpectiveteam.plugins.aisummarize.ai.providers.providers.OpenAIProvider;
import org.perpectiveteam.plugins.aisummarize.ai.providers.providers.OpenRouterProvider;
import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class AIProviderFactory {
    private static final Logger LOG = Loggers.get(AIProviderFactory.class);

    private final AiSummarizeConfig config;

    public AIProviderFactory(AiSummarizeConfig config) {
        this.config = config;
    }

    public AIProvider createConnector() {
        validateConfig();
        String provider = config.getAiProvider();
        switch (provider.toLowerCase()) {
            case AiClientsList.OPENAI:
                return new OpenAIProvider(config);
            case AiClientsList.OPENROUTER:
                return new OpenRouterProvider(config);
            default:
                LOG.error("Unknown AI provider: {}. Defaulting to OpenAI.", provider);
                throw new IllegalArgumentException("Unknown AI provider: " + provider);
        }
    }

    private void validateConfig() {
        String provider = config.getAiProvider();
        String apiKey = config.getAiApiKey();
        String model = config.getAiModel();
        if (apiKey.isEmpty()) {
            LOG.error("AI API key is not configured. Please set the {} property.", AiSummarizeConfig.AI_CLIENT_API_KEY);
            throw new IllegalStateException("AI API key is not configured");
        }
        if(provider.isEmpty()){
            LOG.error("AI Provider is not configured. Please set the {} property.", AiSummarizeConfig.AI_PROVIDER);
            throw new IllegalStateException("AI Provider is not configured");
        }
        if(model.isEmpty()){
            LOG.error("AI Model is not configured. Please set the {} property.", AiSummarizeConfig.AI_MODEL);
            throw new IllegalStateException("AI Model is not configured");
        }
    }
}
