package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient;

import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask.ProjectAnalysis;
import org.sonar.api.server.ServerSide;
import org.sonar.db.alm.setting.AlmSettingDto;
import org.sonar.db.alm.setting.ALM;
import org.sonar.db.alm.setting.ProjectAlmSettingDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ServerSide
@ComputeEngineSide
public class ALMClientFactory {
    private final AiSummarizeConfig config;

    @Autowired
    public ALMClientFactory(List<ALMClientFactoryDelegate> delegates, AiSummarizeConfig config) {
        this.delegateMap = delegates.stream().collect(Collectors.toMap(ALMClientFactoryDelegate::getAlm, d -> d));
        this.config = config;
    }
    
    private final Map<ALM, ALMClientFactoryDelegate> delegateMap;

    public ALMClient createClient(
            String currentAlmId,
            AlmSettingDto almSettingDto,
            ProjectAnalysis projectAnalysis,
            ProjectAlmSettingDto projectAlmSettingDto
    ) throws IOException {
        ALM alm = ALM.fromId(currentAlmId);
        int fileLimit = config.getFileLimit();
        ALMClientFactoryDelegate delegate = delegateMap.get(alm);
        if (delegate == null) {
            throw new IllegalArgumentException("No factory for ALM: " + currentAlmId);
        }
        return delegate.createClient(almSettingDto, projectAlmSettingDto, projectAnalysis, fileLimit);
    }
}
