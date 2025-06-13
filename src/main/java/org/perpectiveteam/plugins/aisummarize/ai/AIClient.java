package org.perpectiveteam.plugins.aisummarize.ai;

import org.perpectiveteam.plugins.aisummarize.ai.providers.AIProvider;
import org.perpectiveteam.plugins.aisummarize.ai.providers.AIProviderFactory;
import org.perpectiveteam.plugins.aisummarize.config.SummarizeConfig;

public class AIClient {
    private AIProvider connector;
    public static final String AI_SUMMARIZE_MARKER = "[SQ AI Summarize]";

    public AIClient(SummarizeConfig config) {
        if (config != null) {
            AIProviderFactory factory = new AIProviderFactory(config);
            this.connector = factory.createConnector();
        }
    }

    public String getCompletion(String prompt) {
        return AI_SUMMARIZE_MARKER + "\n" + connector.getCompletion(prompt);
    }

    public String getProviderName() {
        return connector.getProviderName();
    }
    
    // For testing purposes only
    void setConnector(AIProvider connector) {
        this.connector = connector;
    }
}
