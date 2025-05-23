package org.perpectiveteam.plugins.aisummarize.summarize;

import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.pullrequest.PullRequestDiffFetcher;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.PullRequestDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SummarizeExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SummarizeExecutor.class);
    private final ALMClient almClient;
    private final AiSummarizeConfig aiSummarizeConfig;

    public SummarizeExecutor(ALMClient almClient, AiSummarizeConfig aiSummarizeConfig) {
        this.almClient = almClient;
        this.aiSummarizeConfig = aiSummarizeConfig;
    }

    public void analyzeAndSummarize() throws IOException {
        //almClient.fetchPullRequestFilesDiff();
        PullRequestDiff pullRequestDiff = new PullRequestDiffFetcher(almClient)
                .fetchDiff();

        SummarizeWithAI summarizer = new SummarizeWithAI(aiSummarizeConfig);
        String summary = summarizer.execute(pullRequestDiff);
        almClient.postSummaryIssue(summary);
        //TODO return smth
    }
}
