package org.perpectiveteam.plugins.aisummarize.ce;

import java.util.Optional;

import org.perpectiveteam.plugins.aisummarize.config.SummarizeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.alm.setting.AlmSettingDto;
import org.sonar.db.alm.setting.ProjectAlmSettingDto;

public class PostProjectAnalysisSummarize implements PostProjectAnalysisTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostProjectAnalysisSummarize.class);

    private final DbClient dbClient;
    private final SummarizeConfig aiSummarizeConfig;
    private final SummarizeExecutorFactory summarizeExecutorFactory;

    public PostProjectAnalysisSummarize(
            DbClient dbClient,
            SummarizeExecutorFactory summarizeExecutorFactory,
            SummarizeConfig aiSummarizeConfig
    ) {
        super();
        this.aiSummarizeConfig = aiSummarizeConfig;
        this.dbClient = dbClient;
        this.summarizeExecutorFactory = summarizeExecutorFactory;
    }

    @Override
    public void finished(Context context) {
        LOGGER.info("PostJobInScanner.finished method called");
        ProjectAnalysis projectAnalysis = context.getProjectAnalysis();
        SummarizeConfig.setProjectAnalysisContext(projectAnalysis);

        try {
            executeAnalysis(projectAnalysis);
        } finally {
            SummarizeConfig.clearProjectAnalysisContext();
        }
    }

    private void executeAnalysis(ProjectAnalysis projectAnalysis) {
        if (Boolean.FALSE.equals(aiSummarizeConfig.getIsEnabled())) {
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
            SummarizeExecutor summarizeExecutor = summarizeExecutorFactory.createExecutor(
                    currentAlmId, almSettingDto, projectAnalysis, projectAlmSettingDto);
            summarizeExecutor.analyzeAndSummarize();
        } catch (Exception e) {
            LOGGER.error("Error during AI summarization", e);
        }
    }

    @Override
    public String getDescription() {
        return "Post Analysis Task for PR AI Summarization";
    }
}