package org.perpectiveteam.plugins.hooks;

import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.perpectiveteam.plugins.summarize.SummarizeWithAI;

public class PostJobInScanner implements PostProjectAnalysisTask {
    private static final Logger LOG = Loggers.get(PostJobInScanner.class);

    private static final String SONAR_HOST = "http://localhost:9000"; // Change if needed
    private static final String SONAR_TOKEN = "your-sonarqube-token";  // Secure this better in real projects

    @Override
    public void finished(Context context) {
        //TODO: add tokens from setting
        //TODO: define almplatform
        //TODO: filter by included in analysis code only
        LOG.info("PostJobInScanner.finished method called");

        String projectKey = context.getProjectAnalysis().getProject().getKey();
        String pullRequestKey = context.getProjectAnalysis().getBranch().toString(); // For PR branches

        SummarizeWithAI summarizer = new SummarizeWithAI(SONAR_HOST, SONAR_TOKEN);
        try {
            summarizer.execute(projectKey, pullRequestKey);
            LOG.info("AI summarization completed successfully");
        } catch (Exception e) {
            LOG.error("Error during AI summarization", e);
        }
    }

    @Override
    public String getDescription() {
        return "Post Analysis Task for PR AI Summarization";
    }
}