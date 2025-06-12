package org.perpectiveteam.plugins.aisummarize.almclient.bitbucket.cloud;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.perpectiveteam.plugins.aisummarize.almclient.ALMClientFactoryDelegate;
import org.perpectiveteam.plugins.aisummarize.almclient.bitbucket.BitbucketConfiguration;
import org.perpectiveteam.plugins.aisummarize.utils.PullRequestData;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.ce.posttask.Analysis;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.internal.Settings;
import org.sonar.api.server.ServerSide;
import org.sonar.db.alm.setting.ALM;
import org.sonar.db.alm.setting.AlmSettingDto;
import org.sonar.db.alm.setting.ProjectAlmSettingDto;

import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

@ServerSide
@ComputeEngineSide
public class BitbucketCloudClientFactory implements ALMClientFactoryDelegate {
    private final Settings settings;

    BitbucketCloudClientFactory(
            Settings settings
    ) {
        this.settings = settings;
    }

    @Override
    public ALM getAlm() {
        return ALM.BITBUCKET_CLOUD;
    }

    @Override
    public BitbucketCloudClient createClient(
            AlmSettingDto almSettingDto,
            ProjectAlmSettingDto projectAlmSettingDto,
            PostProjectAnalysisTask.ProjectAnalysis projectAnalysis,
            int fileLimit,
            OkHttpClient.Builder clientBuilder
    ) throws IOException {
        validateSettings(almSettingDto, projectAlmSettingDto);

        ObjectMapper objectMapper = createObjectMapper();

        String almRepo = projectAlmSettingDto.getAlmRepo();
        String appId = almSettingDto.getAppId();
        String clientId = almSettingDto.getClientId();
        String clientSecret = almSettingDto.getDecryptedClientSecret(settings.getEncryption());
        String bearerToken = BitbucketCloudClient.negotiateBearerToken(clientId, clientSecret, objectMapper, clientBuilder.build());
        String prNumber = PullRequestData.getPrNumber(projectAnalysis);

        Optional<Analysis> optionalAnalysis = projectAnalysis.getAnalysis();
        if (optionalAnalysis.isEmpty()) {
            throw new IOException("No analysis results were created for this project analysis. This is likely to be due to an earlier failure");
        }

        return new BitbucketCloudClient(
                createAuthorisingClient(clientBuilder, bearerToken),
                new BitbucketConfiguration(appId, almRepo),
                fileLimit,
                appId,
                almRepo,
                prNumber
        );
    }

    private void validateSettings(AlmSettingDto almSettingDto, ProjectAlmSettingDto projectAlmSettingDto) {
        if (almSettingDto.getDecryptedClientSecret(settings.getEncryption()) == null) {
            throw new IllegalArgumentException("No private key has been set for Bitbucket connections");
        }
        if (almSettingDto.getAppId() == null) {
            throw new IllegalArgumentException("No App ID has been set for Bitbucket connections");
        }
        if (projectAlmSettingDto.getAlmRepo() == null) {
            throw new IllegalArgumentException("No repository name has been set for Bitbucket connections");
        }
        if (almSettingDto.getClientId() == null) {
            throw new IllegalArgumentException("Client ID must be set in configuration");
        }
        if (projectAlmSettingDto.getAlmRepo() == null) {
            throw new IllegalArgumentException("Repository name must be in the format 'owner/repo'");
        }
    }

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .findAndRegisterModules();
    }

    public OkHttpClient createAuthorisingClient(OkHttpClient.Builder clientBuilder, String bearerToken) {
        return clientBuilder.addInterceptor(chain -> {
            Request newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", format("Bearer %s", bearerToken))
                    .addHeader("Accept", "application/json")
                    .build();
            return chain.proceed(newRequest);
        }).build();
    }
}
