package org.perpectiveteam.plugins.aisummarize.ce;

import org.perpectiveteam.plugins.aisummarize.ai.AIClient;
import org.perpectiveteam.plugins.aisummarize.ai.AIPromptBuilder;
import org.perpectiveteam.plugins.aisummarize.config.SummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.pullrequest.PullRequestDiffFetcher;
import org.perpectiveteam.plugins.aisummarize.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.pullrequest.prdto.PullRequestDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SummarizeExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SummarizeExecutor.class);
    private final ALMClient almClient;
    private final SummarizeConfig aiSummarizeConfig;
    private final AIPromptBuilder promptBuilder;

    public SummarizeExecutor(
            ALMClient almClient,
            SummarizeConfig aiSummarizeConfig,
            AIPromptBuilder promptBuilder
    ) {
        this.almClient = almClient;
        this.aiSummarizeConfig = aiSummarizeConfig;
        this.promptBuilder = promptBuilder;
    }

    public void analyzeAndSummarize() throws IOException {
        PullRequestDiff pullRequestDiff = new PullRequestDiffFetcher(almClient)
                .fetchDiff();
        try {
            LOGGER.info("Starting AI summarization");

            String prompt = promptBuilder.buildPrompt(pullRequestDiff.getFiles());

            AIClient aiClient = new AIClient(aiSummarizeConfig);
            LOGGER.info("Using AI provider: {}", aiClient.getProviderName());

            String summary = aiClient.getCompletion(prompt);
            LOGGER.info("AI summarization completed successfully");

            almClient.postSummaryResult(summary);

        } catch (Exception e) {
            LOGGER.error("Error during AI summarization", e);
            throw new SummarizeExecutorException("Error during AI summarization");
        }
    }
}
