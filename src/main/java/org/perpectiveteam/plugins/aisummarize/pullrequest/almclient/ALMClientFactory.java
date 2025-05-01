package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient;

import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.github.GitHubClientFactory;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.config.internal.Settings;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.db.alm.setting.AlmSettingDto;
import org.sonar.db.alm.setting.ALM;

@ServerSide
@ComputeEngineSide
public class ALMClientFactory {
    private static final Logger LOG = Loggers.get(ALMClientFactory.class);

    private final AiSummarizeConfig config;
    private final Settings settings;

    public ALMClientFactory(AiSummarizeConfig config, Settings settings) {
        this.settings = settings;
        this.config = config;
    }

    public ALMClient createGitHubClient(AlmSettingDto almSettingDto) {
        String token = almSettingDto.getDecryptedClientSecret(settings.getEncryption());
        if (token.isEmpty()) {
            LOG.error("GitHub token is not configured. Please set the {} property.");
            throw new IllegalStateException("GitHub token is not configured");
        }

        //TODO: get target branch from PR data
        return new GitHubClientFactory(
                token,
                config.getDefaultTargetBranch(),
                config.getFileLimit()
        );
    }

    public ALMClient createClient(String currentAlmId, AlmSettingDto almSettingDto) {
        ALM alm = ALM.fromId(currentAlmId);

        switch (alm) {
            case GITHUB:
                return createGitHubClient(almSettingDto);
            case GITLAB:
                break;
            default:
                throw new IllegalArgumentException("Unsupported ALM: " + currentAlmId);
        }
        return null;
    }
}
