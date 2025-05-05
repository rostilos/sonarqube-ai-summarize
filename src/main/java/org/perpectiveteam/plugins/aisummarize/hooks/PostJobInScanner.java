package org.perpectiveteam.plugins.aisummarize.hooks;

import java.util.List;
import java.util.Optional;

import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.pullrequest.AnalysisDetails;
import org.perpectiveteam.plugins.aisummarize.pullrequest.PostAnalysisIssueVisitor;
import org.perpectiveteam.plugins.aisummarize.pullrequest.PullRequestDiffFetcher;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.ALMClientFactory;
import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.PullRequestDiff;
import org.perpectiveteam.plugins.aisummarize.summarize.SummarizeWithAI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ce.posttask.Branch;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.Configuration;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.alm.setting.AlmSettingDto;
import org.sonar.db.alm.setting.ProjectAlmSettingDto;

public class PostJobInScanner implements PostProjectAnalysisTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostJobInScanner.class);

    private final DbClient dbClient;
    private final PostAnalysisIssueVisitor postAnalysisIssueVisitor;
    private final Configuration configuration;
    private final ALMClientFactory almClientFactory;

    public PostJobInScanner(
            DbClient dbClient,
            Configuration configuration,
            ALMClientFactory almClientFactory
    ) {
        this.postAnalysisIssueVisitor = new PostAnalysisIssueVisitor();
        this.dbClient = dbClient;
        this.configuration = configuration;
        this.almClientFactory = almClientFactory;
    }

    @Override
    public void finished(Context context) {
        //TODO: filter by included in analysis code only (??)
        LOGGER.info("PostJobInScanner.finished method called");
        ProjectAnalysis projectAnalysis = context.getProjectAnalysis();

        Optional<Branch> optionalPullRequest =
                projectAnalysis.getBranch().filter(branch -> Branch.Type.PULL_REQUEST == branch.getType());
        if (optionalPullRequest.isEmpty()) {
            LOGGER.trace("Current analysis is not for a Pull Request. Task being skipped");
            return;
        }

        Optional<String> optionalPullRequestId = optionalPullRequest.get().getName();
        if (optionalPullRequestId.isEmpty()) {
            LOGGER.warn("No pull request ID has been submitted with the Pull Request. Analysis will be skipped");
            return;
        }

        ProjectAlmSettingDto projectAlmSettingDto;
        Optional<AlmSettingDto> optionalAlmSettingDto;
        try (DbSession dbSession = dbClient.openSession(false)) {
            Optional<ProjectAlmSettingDto> optionalProjectAlmSettingDto =
                    dbClient.projectAlmSettingDao().selectByProject(dbSession, projectAnalysis.getProject().getUuid());

            if (optionalProjectAlmSettingDto.isEmpty()) {
                LOGGER.debug("No ALM has been set on the current project");
                return;
            }

            projectAlmSettingDto = optionalProjectAlmSettingDto.get();
            String almSettingUuid = projectAlmSettingDto.getAlmSettingUuid();
            optionalAlmSettingDto = dbClient.almSettingDao().selectByUuid(dbSession, almSettingUuid);

        }

        if (optionalAlmSettingDto.isEmpty()) {
            LOGGER.warn("The ALM configured for this project could not be found");
            return;
        }
        AlmSettingDto almSettingDto = optionalAlmSettingDto.get();

        String currentAlmId = almSettingDto.getAlm().getId();
        if (currentAlmId.isEmpty()) {
            LOGGER.info("No alm platform found for this Pull Request");
            return;
        }

        try {
            AiSummarizeConfig aiConfig = new AiSummarizeConfig(configuration);
            String prNumber = optionalPullRequestId.get();

            ALMClient almClient = almClientFactory.createClient(currentAlmId, almSettingDto, projectAlmSettingDto);

            PullRequestDiff pullRequestDiff = new PullRequestDiffFetcher(almClient)
                    .fetchDiff(prNumber);

            SummarizeWithAI summarizer = new SummarizeWithAI(prNumber, aiConfig);

            String summary = summarizer.execute(pullRequestDiff);
            LOGGER.info("AI summarization completed successfully");
            LOGGER.info("Summary: {}", summary);

            //TODO: add SQ issues to prompt
            AnalysisDetails analysisDetails =
                    new AnalysisDetails(optionalPullRequestId.get(), postAnalysisIssueVisitor.getIssues(), projectAnalysis);

            almClient.postSummaryIssue(prNumber, summary);


            // TODO: Store the summary in SonarQube or send it as a report to the ALM platform
        } catch (Exception e) {
            LOGGER.error("Error during AI summarization", e);
        }
    }

    @Override
    public String getDescription() {
        return "Post Analysis Task for PR AI Summarization";
    }
}
