package org.perpectiveteam.plugins.aisummarize.summarize;

import org.perpectiveteam.plugins.aisummarize.ai.AIClient;
import org.perpectiveteam.plugins.aisummarize.ai.AIPromptBuilder;
import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.pullrequest.PullRequestDiffFetcher;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.pullrequest.prdto.PullRequestDiff;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;

public class SummarizeExecutor {
    private static final Logger LOGGER = Loggers.get(SummarizeExecutor.class);
    private final ALMClient almClient;
    private final AiSummarizeConfig aiSummarizeConfig;
    private final AIPromptBuilder promptBuilder;
    private final PostProjectAnalysisTask.ProjectAnalysis projectAnalysis;

    public SummarizeExecutor(
            ALMClient almClient,
            PostProjectAnalysisTask.ProjectAnalysis projectAnalysis,
            AiSummarizeConfig aiSummarizeConfig,
            AIPromptBuilder promptBuilder
    ) {
        this.almClient = almClient;
        this.projectAnalysis = projectAnalysis;
        this.aiSummarizeConfig = aiSummarizeConfig;
        this.promptBuilder = promptBuilder;
    }

    public void analyzeAndSummarize() throws IOException {
        PullRequestDiff pullRequestDiff = new PullRequestDiffFetcher(almClient)
                .fetchDiff();
        try {
            LOGGER.info("Starting AI summarization");

            String prompt = promptBuilder.buildPrompt(pullRequestDiff.files);

            AIClient aiClient = new AIClient(aiSummarizeConfig);
            LOGGER.info("Using AI provider: {}", aiClient.getProviderName());

            String summary = aiClient.getCompletion(prompt);
            LOGGER.info("AI summarization completed successfully");

            almClient.postSummaryComment(summary);

        } catch (Exception e) {
            LOGGER.error("Error during AI summarization", e);
            throw new RuntimeException("Error during AI summarization", e);
        }
    }
}
