package org.perpectiveteam.plugins.aisummarize.summarize;

import org.perpectiveteam.plugins.aisummarize.ai.AIPromptBuilder;
import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.ALMClientFactory;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask.ProjectAnalysis;
import org.sonar.api.server.ServerSide;
import org.sonar.db.alm.setting.AlmSettingDto;
import org.sonar.db.alm.setting.ProjectAlmSettingDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@ServerSide
@ComputeEngineSide
public class SummarizeExecutorFactory {
    private final ALMClientFactory almClientFactory;
    private final AiSummarizeConfig aiSummarizeConfig;
    private final AIPromptBuilder promptBuilder;

    @Autowired
    public SummarizeExecutorFactory(
            ALMClientFactory almClientFactory,
            AiSummarizeConfig aiSummarizeConfig,
            AIPromptBuilder promptBuilder
    ) {
        this.almClientFactory = almClientFactory;
        this.aiSummarizeConfig = aiSummarizeConfig;
        this.promptBuilder = promptBuilder;
    }

    public SummarizeExecutor createExecutor(
            String currentAlmId,
            AlmSettingDto almSettingDto,
            ProjectAnalysis projectAnalysis,
            ProjectAlmSettingDto projectAlmSettingDto
    ) throws IOException {
        ALMClient almClient = almClientFactory.createClient(currentAlmId, almSettingDto, projectAnalysis, projectAlmSettingDto);
        return new SummarizeExecutor(almClient, projectAnalysis, aiSummarizeConfig, promptBuilder);
    }
}
