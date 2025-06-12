package org.perpectiveteam.plugins.aisummarize.ai.providers.providers;

import org.perpectiveteam.plugins.aisummarize.config.SummarizeConfig;

public class OpenRouterProvider extends OpenAIProvider {
    private static final String PROVIDER_NAME = "openrouter";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    public OpenRouterProvider(
            SummarizeConfig aiSummarizeConfig,
            String apiUrl
    ) {
        super(aiSummarizeConfig, apiUrl);

    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    protected String getDefaultApiUrl() {
        return API_URL;
    }
}
