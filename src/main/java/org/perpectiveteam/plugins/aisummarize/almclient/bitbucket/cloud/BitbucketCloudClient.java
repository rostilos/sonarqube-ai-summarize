package org.perpectiveteam.plugins.aisummarize.almclient.bitbucket.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.perpectiveteam.plugins.aisummarize.ai.AIClient;
import org.perpectiveteam.plugins.aisummarize.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.almclient.bitbucket.BitbucketCloudException;
import org.perpectiveteam.plugins.aisummarize.almclient.bitbucket.BitbucketConfiguration;
import org.perpectiveteam.plugins.aisummarize.almclient.bitbucket.cloud.comment.BitbucketSummarizeComment;
import org.perpectiveteam.plugins.aisummarize.almclient.bitbucket.cloud.comment.BitbucketCommentContent;
import org.perpectiveteam.plugins.aisummarize.almclient.bitbucket.diff.RawDiffParser;
import org.perpectiveteam.plugins.aisummarize.pullrequest.prdto.FileDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.String.format;

public class BitbucketCloudClient implements ALMClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitbucketCloudClient.class);
    private static final MediaType APPLICATION_JSON_MEDIA_TYPE = MediaType.get("application/json");
    private final int fileLimit;
    private final String appId;
    private final String almRepo;
    private final String prNumber;

    private final OkHttpClient okHttpClient;
    private final BitbucketConfiguration bitbucketConfiguration;

    BitbucketCloudClient(
            OkHttpClient okHttpClient,
            BitbucketConfiguration bitbucketConfiguration,
            int fileLimit,
            String appId,
            String almRepo,
            String prNumber
    ) {
        this.okHttpClient = okHttpClient;
        this.bitbucketConfiguration = bitbucketConfiguration;
        this.fileLimit = fileLimit;
        this.appId = appId;
        this.almRepo = almRepo;
        this.prNumber = prNumber;
    }

    static String negotiateBearerToken(String clientId, String clientSecret, ObjectMapper objectMapper, OkHttpClient okHttpClient) {
        Request request = new Request.Builder()
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)))
                .url("https://bitbucket.org/site/oauth2/access_token")
                .post(RequestBody.create("grant_type=client_credentials", MediaType.parse("application/x-www-form-urlencoded")))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            BitbucketCloudClient.AuthToken authToken = objectMapper.readValue(
                    Optional.ofNullable(response.body()).orElseThrow(() -> new IllegalStateException("No response returned by Bitbucket Oauth")).string(), BitbucketCloudClient.AuthToken.class);
            return authToken.getAccessToken();
        } catch (IOException ex) {
            throw new IllegalStateException("Could not retrieve bearer token", ex);
        }
    }

    private static class AuthToken {

        private final String accessToken;

        AuthToken(@JsonProperty("access_token") String accessToken) {
            this.accessToken = accessToken;
        }

        String getAccessToken() {
            return accessToken;
        }
    }

    @Override
    public void postSummaryResult(String textContent) throws IOException {
        deleteOldSummarizeComments();
        ObjectMapper objectMapper = new ObjectMapper();

        BitbucketCommentContent commentContent = new BitbucketCommentContent(textContent);
        BitbucketSummarizeComment summarizeReport = createSummarizeComment(commentContent);

        String body = objectMapper.writeValueAsString(summarizeReport);
        String apiUrl = format("https://api.bitbucket.org/2.0/repositories/%s/%s/pullrequests/%s/comments", bitbucketConfiguration.getProject(), bitbucketConfiguration.getRepository(), prNumber);
        Request req = new Request.Builder()
                .post(RequestBody.create(body, APPLICATION_JSON_MEDIA_TYPE))
                .url(apiUrl)
                .build();

        LOGGER.info("Create report on bitbucket cloud: {}", apiUrl);

        try (Response response = okHttpClient.newCall(req).execute()) {
            validate(response);
        }
    }

    @Override
    public List<FileDiff> fetchPullRequestFilesDiff() throws IOException {
        String apiUrl = String.format(
                "https://api.bitbucket.org/2.0/repositories/%s/%s/pullrequests/%s/diff",
                appId, almRepo, prNumber
        );

        Request req = new Request.Builder()
                .get()
                .url(apiUrl)
                .build();
        List<FileDiff> fileDiffs;

        try (Response response = okHttpClient.newCall(req).execute()) {
            String rawDiff = validate(response);
            RawDiffParser diffParser = new RawDiffParser();
            fileDiffs = diffParser.execute(rawDiff);

            //fetch original content
            String targetBranch = getTargetBranch();
            if (targetBranch == null || targetBranch.isEmpty()) {
                LOGGER.error("Error fetching pull request info, target branch");
                throw new BitbucketCloudException("Error fetching pull request info, target branch");
            }
            int fileCount = 0;
            for (FileDiff fileDiff : fileDiffs) {
                if (fileLimit > 0 && fileCount >= fileLimit) {
                    LOGGER.info("Reached file limit of {}. Skipping remaining files.", fileLimit);
                    break;
                }

                if (fileDiff.getDiffType() != FileDiff.DiffType.ADDED) {
                    String fileRawContentFromBitbucket = fetchFileContent(targetBranch, fileDiff.getFilePath());
                    fileDiff.setRawContent(fileRawContentFromBitbucket);
                } else {
                    fileDiff.setRawContent("There is no previous version, probably a new file");
                }
                fileCount++;
            }
        }
        return fileDiffs;
    }

    private String getTargetBranch() throws IOException {
        String apiUrl = String.format(
                "https://api.bitbucket.org/2.0/repositories/%s/%s/pullrequests/%s",
                appId, almRepo, prNumber
        );

        Request req = new Request.Builder()
                .get()
                .url(apiUrl)
                .build();

        try (Response response = okHttpClient.newCall(req).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    throw new IOException("Empty response body");
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseBody.string());

                JsonNode nameNode = root.path("destination").path("branch").path("name");
                if (nameNode.isMissingNode()) {
                    throw new IOException("Missing 'destination.branch.name' in response");
                }

                return nameNode.asText();
            }
        }
    }

    private String fetchFileContent(String branch, String filePath) {
        try {
            String encodedFilePath = URLEncoder.encode(filePath, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");

            String apiUrl = String.format(
                    "https://api.bitbucket.org/2.0/repositories/%s/%s/src/%s/%s",
                    appId, almRepo, branch, encodedFilePath
            );

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .get()
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                return validate(response);
            }
        } catch (IOException e) {
            LOGGER.error("IO error fetching file content from Bitbucket: {}", filePath, e);
            return "Error fetching file content: " + e.getMessage();
        } catch (Exception e) {
            LOGGER.error("Unexpected error fetching file content from Bitbucket: {}", filePath, e);
            return "Error fetching file content: " + e.getMessage();
        }
    }

    public String getPrNumber() {
        return prNumber;
    }

    public BitbucketSummarizeComment createSummarizeComment(BitbucketCommentContent commentData) {
        return new BitbucketSummarizeComment(commentData);
    }

    private String validate(Response response) throws IOException {
        if (!response.isSuccessful()) {
            String error = Optional.ofNullable(response.body()).map(b -> {
                try {
                    return b.string();
                } catch (IOException e) {
                    throw new IllegalStateException("Could not retrieve response content", e);
                }
            }).orElse("Request failed but Bitbucket didn't respond with a proper error message");
            throw new IOException(error);
        } else {
            assert response.body() != null;
            return response.body().string();
        }
    }

    private void deleteComment(JsonNode comment) throws IOException {
        JsonNode content = comment.path("content").path("raw");
        if (content.asText().contains(AIClient.AI_SUMMARIZE_MARKER)) {
            String deleteUrl = comment.path("links").path("self").path("href").asText();

            Request deleteRequest = new Request.Builder()
                    .delete()
                    .url(deleteUrl)
                    .build();

            try (Response deleteResponse = okHttpClient.newCall(deleteRequest).execute()) {
                if (deleteResponse.isSuccessful()) {
                    LOGGER.debug("Deleted comment ID: {}", comment.get("id").asInt());
                } else {
                    LOGGER.debug("Failed to delete comment ID: {}", comment.get("id").asInt());
                }
            }
        }
    }

    private void deleteOldSummarizeComments() throws IOException {
        String apiUrl = format("https://api.bitbucket.org/2.0/repositories/%s/%s/pullrequests/%s/comments", bitbucketConfiguration.getProject(), bitbucketConfiguration.getRepository(), prNumber);

        while (apiUrl != null) {
            Request request = new Request.Builder()
                    .get()
                    .url(apiUrl)
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to fetch comments: " + response.code());
                }
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(response.body().string());
                JsonNode comments = root.get("values");

                if (comments != null && comments.isArray()) {
                    for (JsonNode comment : comments) {
                        deleteComment(comment);
                    }
                }

                JsonNode nextNode = root.get("next");
                apiUrl = nextNode != null ? nextNode.asText() : null;
            }
        }
    }
}
