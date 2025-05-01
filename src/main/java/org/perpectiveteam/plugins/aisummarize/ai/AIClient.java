package org.perpectiveteam.plugins.aisummarize.ai;

import org.perpectiveteam.plugins.aisummarize.ai.connector.AIConnector;
import org.perpectiveteam.plugins.aisummarize.ai.connector.AIConnectorFactory;
import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class AIClient {
    private static final Logger LOG = Loggers.get(AIClient.class);
    
    private final AIConnector connector;

    public AIClient(AiSummarizeConfig config) {
        AIConnectorFactory factory = new AIConnectorFactory(config);
        this.connector = factory.createConnector();
        LOG.info("Initialized AI client with provider: {}", connector.getProviderName());
    }

    public AIClient(AIConnector connector) {
        this.connector = connector;
    }

    public String getCompletion(String prompt) {
        return connector.getCompletion(prompt);
    }
    
    public String getProviderName() {
        return connector.getProviderName();
    }
}
