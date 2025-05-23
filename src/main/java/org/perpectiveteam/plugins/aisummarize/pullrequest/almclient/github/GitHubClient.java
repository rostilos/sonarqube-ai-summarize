package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.FileDiff;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GitHubClient implements ALMClient {

    private static final Logger LOG = Loggers.get(GitHubClient.class);

    private final String githubToken;
    private final int fileLimit;
    private final String repoOwner;
    private final String repoName;
    private final String prNumber;

    public GitHubClient(
            String githubToken,
            int fileLimit,
            String repoOwner,
            String repoName,
            String prNumber
    ) {
        this.githubToken = githubToken;
        this.fileLimit = fileLimit;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.prNumber = prNumber;
    }

    //TODO: MVP DRY-violation ( request builder, OkHttpClient, etc. )
    @Override
    public List<FileDiff> fetchPullRequestFilesDiff() throws IOException {
        List<FileDiff> fileDiffs = new ArrayList<>();
        try {
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%s/files", repoOwner, repoName, prNumber);
            URL url = new URL(apiUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "token " + this.githubToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            Optional<String> targetBranch = getTargetBranch(prNumber);
            if (targetBranch.isEmpty()) {
                LOG.error("Error fetching pull request info, target branch");
                throw new RuntimeException("Error fetching pull request info, target branch");
            }

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

                    String patch = fileNode.get("patch") != null ? fileNode.get("patch").asText() : null;
                    fileDiff.changes = patch;

                    fileDiff.sha = fileNode.get("sha").asText();
                    //TODO: skip if new file
                    //TODO: add enum to the checks
                    if (!fileDiff.diffType.equals("added")) {
                        fileDiff.rawContent = fetchFileContent(targetBranch.get(), fileDiff.filePath);
                    } else {
                        fileDiff.rawContent = "There is no previous version, probably a new file";
                    }
                    fileDiffs.add(fileDiff);
                    fileCount++;
                }
            }
        } catch (Exception e) {
            LOG.error("Error fetching PR diff from GitHub", e);
            throw new IOException("Error fetching PR diff from GitHub", e);
        }
        return fileDiffs;
    }

    public String fetchFileContent(String ref, String path) {
        try {
            String encodedPath = path.replace(" ", "%20");
            String contentUrl = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s",
                    repoOwner, repoName, encodedPath, ref);
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
            //TODO: skip if new file
            //throw new RuntimeException("Error fetching file content from GitHub", e);
            return "";
        }
    }

    public Optional<String> getTargetBranch(String prNumber) {
        try {
            String contentUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%s",
                    repoOwner, repoName, prNumber);
            URL url = new URL(contentUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "token " + this.githubToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            try (InputStream in = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(in);
                return Optional.ofNullable(root.get("base").get("ref").asText());
            }
        } catch (IOException e) {
            LOG.error("Error fetching pull request info", e);
            throw new RuntimeException("Error fetching pull request info", e);
        }
    }

    //@Override
    public void postSummaryIssue(String comment) throws IOException {
        //TODO: delete old report
        try {
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/issues/%s/comments",
                    repoOwner, repoName, prNumber);
            URL url = new URL(apiUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "token " + this.githubToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("body", comment);

            String jsonBody = json.toString();

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            LOG.debug("Response code: " + code);
        } catch (IOException e) {
            LOG.error("Error fetching file content from GitHub", e);
            throw new IOException("Error fetching file content from GitHub", e);
        }
    }
    public String getPrNumber() {
        return prNumber;
    }
}
