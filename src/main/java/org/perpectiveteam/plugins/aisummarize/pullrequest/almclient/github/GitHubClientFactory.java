package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.github;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.perpectiveteam.plugins.aisummarize.pullrequest.PatchParser;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.FileDiff;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sonar.db.alm.setting.ALM;

public class GitHubClientFactory implements ALMClient {
    private static final Logger LOG = Loggers.get(GitHubClientFactory.class);
    private static final String PROVIDER_NAME = "github";
    
    private final String githubToken;
    private final String targetBranch;
    private final int fileLimit;

    public GitHubClientFactory(String githubToken, String targetBranch, int fileLimit) {
        this.githubToken = githubToken;
        this.targetBranch = targetBranch;
        this.fileLimit = fileLimit;
    }

    @Override
    public List<FileDiff> fetchPullRequestFiles(String owner, String repo, String pullRequestNumber) {
        List<FileDiff> fileDiffs = new ArrayList<>();
        try {
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%s/files", owner, repo, pullRequestNumber);
            URL url = new URL(apiUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "token " + this.githubToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            try (InputStream in = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    try (InputStream errorStream = conn.getErrorStream()) {
                        String error = new String(errorStream.readAllBytes());
                        LOG.error("GitHub API error ({}): {}", responseCode, error);
                        throw new RuntimeException("GitHub API error (" + responseCode + "): " + error);
                    }
                }
                JsonNode root = mapper.readTree(in);

                int fileCount = 0;
                for (JsonNode fileNode : root) {
                    // Check if we've reached the file limit
                    if (fileLimit > 0 && fileCount >= fileLimit) {
                        LOG.info("Reached file limit of {}. Skipping remaining files.", fileLimit);
                        break;
                    }
                    
                    FileDiff fileDiff = new FileDiff();
                    fileDiff.filePath = fileNode.get("filename").asText();
                    fileDiff.diffType = fileNode.get("status").asText();
                    if ("removed".equals(fileDiff.diffType)) {
                        LOG.info("Skipping deleted file: {}", fileDiff.filePath);
                        continue;
                    }
                    fileDiff.changes = new ArrayList<>();

                    String patch = fileNode.get("patch") != null ? fileNode.get("patch").asText() : null;
                    if (patch != null) {
                        fileDiff.changes = PatchParser.parsePatch(patch);
                    }

                    fileDiff.sha = fileNode.get("sha").asText();
                    fileDiff.rawContent = fetchFileContent(owner, repo, this.targetBranch, fileDiff.filePath);
                    fileDiffs.add(fileDiff);
                    fileCount++;
                }
            }
        } catch (Exception e) {
            LOG.error("Error fetching PR diff from GitHub", e);
            throw new RuntimeException("Error fetching PR diff from GitHub", e);
        }
        return fileDiffs;
    }

    @Override
    public List<ALM> alm() {
        return Collections.singletonList(ALM.GITHUB);
    }

    //TODO: not sure, maybe we should retrieve it from SQ DB
    private String fetchFileContent(String owner, String repo, String ref, String path) {
        try {
            String encodedPath = path.replace(" ", "%20");
            String contentUrl = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s",
                    owner, repo, encodedPath, ref);
            URL url = new URL(contentUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "token " + this.githubToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            try (InputStream in = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(in);
                String contentBase64 = root.get("content").asText();
                String clean = contentBase64.replaceAll("[^A-Za-z0-9+/=]", "");
                return new String(java.util.Base64.getDecoder().decode(clean), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            LOG.error("Error fetching file content from GitHub", e);
            throw new RuntimeException("Error fetching file content from GitHub", e);
        }
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
    
    @Override
    public String getDefaultTargetBranch() {
        return targetBranch;
    }
}
