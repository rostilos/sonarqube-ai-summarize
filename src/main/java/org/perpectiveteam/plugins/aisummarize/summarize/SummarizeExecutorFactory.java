package org.perpectiveteam.plugins.aisummarize.summarize;

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
    //TODO: log issues
    //private static final Logger LOG = Loggers.get(ALMClientFactory.class);
    private final AiSummarizeConfig aiSummarizeConfig;
    private final ALMClientFactory almClientFactory;

    @Autowired
    public SummarizeExecutorFactory(
            //List<SummarizeExecutorFactoryDelegate> delegates,
            AiSummarizeConfig aiSummarizeConfig,
            ALMClientFactory almClientFactory
    ) {
        //this.delegateMap = delegates.stream().collect(Collectors.toMap(SummarizeExecutorFactoryDelegate::getAlm, d -> d));
        this.aiSummarizeConfig = aiSummarizeConfig;
        this.almClientFactory = almClientFactory;
    }


    //private final Map<ALM, SummarizeExecutorFactoryDelegate> delegateMap;

    public SummarizeExecutor createExecutor(
            String currentAlmId,
            AlmSettingDto almSettingDto,
            ProjectAnalysis projectAnalysis,
            ProjectAlmSettingDto projectAlmSettingDto
    ) throws IOException {
//        ALM alm = ALM.fromId(currentAlmId);
//        int fileLimit = config.getFileLimit();
//
//        SummarizeExecutorFactoryDelegate delegate = delegateMap.get(alm);
//        if (delegate == null) {
//            throw new IllegalArgumentException("No factory for ALM: " + currentAlmId);
//        }
        ALMClient almClient = almClientFactory.createClient(currentAlmId, almSettingDto, projectAnalysis, projectAlmSettingDto);
        return new SummarizeExecutor(almClient, aiSummarizeConfig);
    }
}
