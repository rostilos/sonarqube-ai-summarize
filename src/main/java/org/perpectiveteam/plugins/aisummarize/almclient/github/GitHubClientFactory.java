package org.perpectiveteam.plugins.aisummarize.almclient.github;

import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Supplier;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.kohsuke.github.GHAppInstallationToken;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;
import org.perpectiveteam.plugins.aisummarize.almclient.ALMClientFactoryDelegate;
import org.perpectiveteam.plugins.aisummarize.almclient.HttpClientBuilderFactory;
import org.perpectiveteam.plugins.aisummarize.utils.PullRequestData;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask.ProjectAnalysis;
import org.sonar.api.server.ServerSide;
import org.sonar.db.alm.setting.ALM;
import org.sonar.db.alm.setting.AlmSettingDto;
import org.sonar.db.alm.setting.ProjectAlmSettingDto;
import org.sonar.api.config.internal.Settings;
import org.springframework.beans.factory.annotation.Autowired;

import static java.lang.String.format;

@ServerSide
@ComputeEngineSide
public class GitHubClientFactory implements ALMClientFactoryDelegate {
    private final Clock clock;
    private final Supplier<GitHubBuilder> gitHubBuilderSupplier;
    private final Settings settings;
    private final HttpClientBuilderFactory httpClientBuilderFactory;


    @Autowired
    public GitHubClientFactory(
            Clock clock,
            Settings settings,
            HttpClientBuilderFactory httpClientBuilderFactory
    ) {
        this.clock = clock;
        this.settings = settings;
        this.gitHubBuilderSupplier = GitHubBuilder::new;
        this.httpClientBuilderFactory = httpClientBuilderFactory;
    }

    @Override
    public ALM getAlm() {
        return ALM.GITHUB;
    }

    @Override
    public GitHubClient createClient(
            AlmSettingDto almSettingDto,
            ProjectAlmSettingDto projectAlmSettingDto,
            ProjectAnalysis projectAnalysis,
            int fileLimit,
            OkHttpClient.Builder clientBuilder
    ) throws IOException {
        String almRepo = projectAlmSettingDto.getAlmRepo();
        assert almRepo != null;
        String[] parts = almRepo.split("/");

        String repoOwner = parts[0];
        String repoName = parts[1];
        String prNumber = PullRequestData.getPrNumber(projectAnalysis);

        validateSettings(almSettingDto, projectAlmSettingDto);
        String token = generateAuthToken(almSettingDto, projectAlmSettingDto);
        return new GitHubClient(createAuthorisingClient(clientBuilder, token), fileLimit, repoOwner, repoName, prNumber);
    }

    private void validateSettings(AlmSettingDto almSettings, ProjectAlmSettingDto projectSettings) {
        if (almSettings.getUrl() == null) {
            throw new IllegalArgumentException("No URL has been set for GitHub connections");
        }
        if (almSettings.getDecryptedPrivateKey(settings.getEncryption()) == null) {
            throw new IllegalArgumentException("No private key has been set for GitHub connections");
        }
        if (almSettings.getAppId() == null) {
            throw new IllegalArgumentException("No App ID has been set for GitHub connections");
        }
        if (projectSettings.getAlmRepo() == null) {
            throw new IllegalArgumentException("No repository name has been set for GitHub connections");
        } else if(projectSettings.getAlmRepo() == null || !projectSettings.getAlmRepo().contains("/")) {
            throw new IllegalArgumentException("Repository name must be in the format 'owner/repo'");
        }
    }

    private String generateAuthToken(AlmSettingDto almSettings, ProjectAlmSettingDto projectSettings) throws IOException {
        try {
            RepositoryCoordinates repoCoords = parseRepositoryPath(projectSettings.getAlmRepo());
            String jwtToken = createJwtToken(almSettings.getAppId(),
                    almSettings.getDecryptedPrivateKey(settings.getEncryption()));

            GitHub tempGitHub = gitHubBuilderSupplier.get()
                    .withEndpoint(almSettings.getUrl())
                    .withConnector(createConnector())
                    .withJwtToken(jwtToken)
                    .build();

            GHAppInstallationToken installationToken = tempGitHub.getApp()
                    .getInstallationByRepository(repoCoords.owner, repoCoords.repo)
                    .createToken()
                    .create();

            return installationToken.getToken();
        } catch (IOException ex) {
            throw new IOException("Failed to authenticate with GitHub: " + ex.getMessage(), ex);
        }
    }

    private String createJwtToken(String appId, String privateKeyPem) {
        Instant issuedAt = clock.instant().minus(10, ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plus(2, ChronoUnit.MINUTES);

        return new DefaultJwtBuilder()
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("iss", appId)
                .signWith(parsePrivateKey(privateKeyPem), Jwts.SIG.RS256)
                .compact();
    }

    private PrivateKey parsePrivateKey(String privateKeyPem) {
        try (PEMParser pemParser = new PEMParser(new StringReader(privateKeyPem))) {
            Object object = pemParser.readObject();
            if (object == null) {
                throw new IllegalArgumentException("Private key could not be parsed");
            }
            PEMKeyPair keyPair = (PEMKeyPair) object;
            return new JcaPEMKeyConverter().getPrivateKey(keyPair.getPrivateKeyInfo());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse private key", e);
        }
    }

    private OkHttpGitHubConnector createConnector() {
        OkHttpClient client = httpClientBuilderFactory.createClientBuilder().build();
        return new OkHttpGitHubConnector(client);
    }

    private RepositoryCoordinates parseRepositoryPath(String path) {
        if(path == null) {
            throw new IllegalArgumentException("No repository name has been set for GitHub connections");
        }
        String[] parts = path.split("/", 2);
        return new RepositoryCoordinates(parts[0], parts[1]);
    }

    private static class RepositoryCoordinates {
        final String owner;
        final String repo;

        RepositoryCoordinates(String owner, String repo) {
            this.owner = owner;
            this.repo = repo;
        }
    }

    public OkHttpClient createAuthorisingClient(OkHttpClient.Builder clientBuilder, String bearerToken) {
        return clientBuilder.addInterceptor(chain -> {
            Request newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", format("Bearer %s", bearerToken))
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .build();
            return chain.proceed(newRequest);
        }).build();
    }
}
