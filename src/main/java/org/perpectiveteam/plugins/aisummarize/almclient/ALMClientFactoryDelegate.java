package org.perpectiveteam.plugins.aisummarize.almclient;

import okhttp3.OkHttpClient;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask.ProjectAnalysis;
import org.sonar.db.alm.setting.ALM;
import org.sonar.db.alm.setting.AlmSettingDto;
import org.sonar.db.alm.setting.ProjectAlmSettingDto;

import java.io.IOException;

public interface ALMClientFactoryDelegate {
    ALM getAlm();

    ALMClient createClient(
            AlmSettingDto almSettingDto,
            ProjectAlmSettingDto projectAlmSettingDto,
            ProjectAnalysis projectAnalysis,
            int fileLimit,
            OkHttpClient.Builder clientBuilder
    ) throws IOException;

    OkHttpClient createAuthorisingClient(
            OkHttpClient.Builder clientBuilder, String bearerToken
    );
}
