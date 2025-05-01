package org.perpectiveteam.plugins.aisummarize.summarize;

import org.perpectiveteam.plugins.aisummarize.ai.AIClient;
import org.perpectiveteam.plugins.aisummarize.ai.AIPromptBuilder;
import org.perpectiveteam.plugins.aisummarize.ai.connector.AIConnectorFactory;
import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.PullRequestDiff;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;


public class SummarizeWithAI {
    private static final Logger LOG = Loggers.get(SummarizeWithAI.class);
    
    private final String repoOwner;
    private final String repoName;
    private final String pullRequestId;
    private final AIPromptBuilder promptBuilder;
    private final AiSummarizeConfig config;
    private final AIConnectorFactory aiConnectorFactory;

    public SummarizeWithAI(
            String repoOwner,
            String repoName,
            String pullRequestId,
            AiSummarizeConfig config
    ) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.pullRequestId = pullRequestId;
        this.config = config;
        this.promptBuilder = new AIPromptBuilder();
        this.aiConnectorFactory = new AIConnectorFactory(config);
    }

    public String execute(PullRequestDiff pullRequestDiff) {
        try {
            LOG.info("Starting AI summarization for {}/{} PR #{}", repoOwner, repoName, pullRequestId);
            
            String prompt = promptBuilder.buildPrompt(pullRequestDiff.files);
            
            AIClient aiClient = new AIClient(config);
            LOG.info("Using AI provider: {}", aiClient.getProviderName());
            
            String summary = aiClient.getCompletion(prompt);
            LOG.info("AI summarization completed successfully");
            
            return summary;
        } catch (Exception e) {
            LOG.error("Error during AI summarization", e);
            throw new RuntimeException("Error during AI summarization", e);
        }
    }
}
