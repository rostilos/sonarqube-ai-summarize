package org.perpectiveteam.plugins.aisummarize.almclient.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.json.JSONObject;
import org.perpectiveteam.plugins.aisummarize.ai.AIClient;
import org.perpectiveteam.plugins.aisummarize.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.almclient.github.comment.GithubPRComment;
import org.perpectiveteam.plugins.aisummarize.pullrequest.prdto.FileDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GitHubClient implements ALMClient {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubClient.class);
    private static final String GH_API_ERR_MESSAGE = "Error fetching file content from GitHub";
    private static final MediaType APPLICATION_JSON_MEDIA_TYPE = MediaType.get("application/json");
    private final int fileLimit;
    private final String repoOwner;
    private final String repoName;
    private final String prNumber;
    private final OkHttpClient okHttpClient;

    public GitHubClient(
            OkHttpClient okHttpClient,
            int fileLimit,
            String repoOwner,
            String repoName,
            String prNumber
    ) {
        this.okHttpClient = okHttpClient;
        this.fileLimit = fileLimit;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.prNumber = prNumber;
    }

    @Override
    public List<FileDiff> fetchPullRequestFilesDiff() throws IOException {
        List<FileDiff> fileDiffs = new ArrayList<>();

        String apiUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%s/files", repoOwner, repoName, prNumber);

        Request request = new Request.Builder()
                .get()
                .url(apiUrl)
                .build();

        Optional<String> targetBranch = getTargetBranch(prNumber);
        if (targetBranch.isEmpty()) {
            LOG.error("Error fetching pull request info, target branch");
            throw new GithubClientException("Error fetching pull request info, target branch");
        }

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                LOG.error("GitHub API error ({}): {}", response.code(), errorBody);
                throw new GithubClientException("GitHub API error (" + response.code() + "): " + errorBody);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body().string());

            int fileCount = 0;
            for (JsonNode fileNode : root) {
                if (fileLimit > 0 && fileCount >= fileLimit) {
                    LOG.info("Reached file limit of {}. Skipping remaining files.", fileLimit);
                    break;
                }

                FileDiff fileDiff = new FileDiff();
                fileDiff.filePath = fileNode.get("filename").asText();

                String status = fileNode.get("status").asText();
                FileDiff.DiffType diffType = getDiffTypeFromResponseData(status, fileDiff.filePath);
                if (diffType == null) {
                    continue;
                }
                fileDiff.diffType = diffType;

                if (fileDiff.diffType == FileDiff.DiffType.REMOVED) {
                    LOG.info("Skipping deleted file: {}", fileDiff.filePath);
                    continue;
                }

                fileDiff.changes = fileNode.get("patch") != null ? fileNode.get("patch").asText() : null;
                fileDiff.sha = fileNode.get("sha").asText();

                if (fileDiff.diffType == FileDiff.DiffType.ADDED) {
                    fileDiff.rawContent = "There is no previous version, probably a new file";
                } else {
                    fileDiff.rawContent = fetchFileContent(targetBranch.get(), fileDiff.filePath);
                }
                fileDiffs.add(fileDiff);
                fileCount++;
            }
        } catch (Exception e) {
            LOG.error("Error fetching PR diff from GitHub", e);
            throw new IOException("Error fetching PR diff from GitHub", e);
        }

        return fileDiffs;
    }

    public FileDiff.DiffType getDiffTypeFromResponseData(String responseStatus, String filePath) {
        try {
            return FileDiff.DiffType.fromValue(responseStatus);
        } catch (IllegalArgumentException e) {
            LOG.warn("Unknown diff type '{}', skipping file: {}", responseStatus, filePath);
        }
        return null;
    }

    public String fetchFileContent(String ref, String path) {
        String encodedPath = path.replace(" ", "%20");
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s",
                repoOwner, repoName, encodedPath, ref);

        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    return "";
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseBody.string());

                JsonNode nameNode = root.path("content");
                String contentBase64 = nameNode.asText();
                String clean = contentBase64.replaceAll("[^A-Za-z0-9+/=]", "");
                return new String(java.util.Base64.getDecoder().decode(clean), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            LOG.error(GH_API_ERR_MESSAGE, e);
            throw new GithubClientException(GH_API_ERR_MESSAGE + e.getMessage());
        }
    }

    public Optional<String> getTargetBranch(String prNumber) {

        String apiUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%s",
                repoOwner, repoName, prNumber);

        Request request = new Request.Builder()
                .get()
                .url(apiUrl)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    throw new IOException("Empty response body");
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseBody.string());

                JsonNode nameNode = root.path("base").path("ref");
                if (nameNode.isMissingNode()) {
                    throw new IOException("Missing 'destination.branch.name' in response");
                }

                return Optional.ofNullable(nameNode.asText());
            }
        } catch (IOException e) {
            LOG.error("Error fetching pull request info", e);
            throw new GithubClientException("Error fetching pull request info");
        }
    }

    @Override
    public void postSummaryResult(String textContent) throws IOException {
        try {
            removePreviousAISummarizeComments();
        } catch (IOException e) {
            LOG.debug("Failed to delete previous comments, but the process of posting a new comment will happen next");
        }

        String apiUrl = String.format("https://api.github.com/repos/%s/%s/issues/%s/comments",
                repoOwner, repoName, prNumber);

        JSONObject json = new JSONObject();
        json.put("body", textContent);
        String jsonBody = json.toString();

        Request request = new Request.Builder()
                .post(RequestBody.create(jsonBody, APPLICATION_JSON_MEDIA_TYPE))
                .url(apiUrl)
                .build();
        LOG.info("Post summarize comment on github: {}", apiUrl);

        try (Response response = okHttpClient.newCall(request).execute()) {
            if(!response.isSuccessful()) {
                String error = Optional.ofNullable(response.body()).map(b -> {
                    try {
                        return b.string();
                    } catch (IOException e) {
                        throw new IllegalStateException("Could not retrieve response content", e);
                    }
                }).orElse("Request failed but Github didn't respond with a proper error message");
                LOG.debug(error);
            }
        }
    }

    public String getPrNumber() {
        return prNumber;
    }

    public void removePreviousAISummarizeComments() throws IOException {
        List<GithubPRComment> aiSummarizeComments = fetchAISummarizeComments();

        for (GithubPRComment comment : aiSummarizeComments) {
            deleteComment(comment.getId());
            LOG.info("Deleted AI summarize comment with ID: {}", comment.getId());
        }

        LOG.info("Removed {} previous AI summarize comments", aiSummarizeComments.size());
    }

    private List<GithubPRComment> fetchAISummarizeComments() throws IOException {
        List<GithubPRComment> aiSummarizeComments = new ArrayList<>();

        String apiUrl = String.format("https://api.github.com/repos/%s/%s/issues/%s/comments",
                repoOwner, repoName, prNumber);

        Request request = new Request.Builder()
                .get()
                .url(apiUrl)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                LOG.error("GitHub API error fetching comments ({}): {}", response.code(), errorBody);
                throw new IOException("GitHub API error fetching comments (" + response.code() + "): " + errorBody);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body().string());

            for (JsonNode commentNode : root) {
                String commentBody = commentNode.get("body").asText();
                if (commentBody.contains(AIClient.AI_SUMMARIZE_MARKER)) {
                    GithubPRComment comment = new GithubPRComment(
                            commentNode.get("id").asLong(),
                            commentBody
                    );
                    aiSummarizeComments.add(comment);
                }
            }
        } catch (Exception e) {
            LOG.error("Error fetching comments from GitHub", e);
            throw new IOException("Error fetching comments from GitHub", e);
        }

        return aiSummarizeComments;
    }

    private void deleteComment(long commentId) throws IOException {
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/issues/comments/%d",
                repoOwner, repoName, commentId);

        Request request = new Request.Builder()
                .delete()
                .url(apiUrl)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                LOG.error("GitHub API error deleting comment {} ({}): {}", commentId, response.code(), errorBody);
                throw new IOException("GitHub API error deleting comment " + commentId + " (" + response.code() + "): " + errorBody);
            }
        }
    }
}
