package org.perpectiveteam.plugins.aisummarize.almclient;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.perpectiveteam.plugins.aisummarize.config.SummarizeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ALMClientFactory.class);
    private final SummarizeConfig config;
    private final HttpClientBuilderFactory httpClientBuilderFactory;

    @Autowired
    public ALMClientFactory(
            List<ALMClientFactoryDelegate> delegates,
            SummarizeConfig config,
            HttpClientBuilderFactory httpClientBuilderFactory
    ) {
        this.delegateMap = delegates.stream().collect(Collectors.toMap(ALMClientFactoryDelegate::getAlm, d -> d));
        this.config = config;
        this.httpClientBuilderFactory = httpClientBuilderFactory;
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

        OkHttpClient.Builder baseHttpClient = createBaseClientBuilder();
        return delegate.createClient(almSettingDto, projectAlmSettingDto, projectAnalysis, fileLimit, baseHttpClient);
    }

    public OkHttpClient.Builder createBaseClientBuilder() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(LOGGER::debug);
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return httpClientBuilderFactory.createClientBuilder().addInterceptor(httpLoggingInterceptor);
    }
}
