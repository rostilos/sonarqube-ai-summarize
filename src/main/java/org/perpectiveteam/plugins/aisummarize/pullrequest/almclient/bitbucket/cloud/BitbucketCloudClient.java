package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.cloud.comment.BitbucketSummarizeComment;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.cloud.comment.BitbucketCommentContent;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.diff.RawDiffParser;
import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.FileDiff;
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
    private final String commitId;
    private final String dashboardUrl;

    private final OkHttpClient okHttpClient;
    private final BitbucketConfiguration bitbucketConfiguration;

    BitbucketCloudClient(
            OkHttpClient okHttpClient,
            BitbucketConfiguration bitbucketConfiguration,
            int fileLimit,
            String appId,
            String almRepo,
            String prNumber,
            String commitId,
            String dashboardUrl
    ) {
        this.okHttpClient = okHttpClient;
        this.bitbucketConfiguration = bitbucketConfiguration;
        this.fileLimit = fileLimit;
        this.appId = appId;
        this.almRepo = almRepo;
        this.prNumber = prNumber;
        this.commitId = commitId;
        this.dashboardUrl = dashboardUrl;

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
    public void postSummaryIssue(String comment) throws IOException {
        //TODO : delete old comment
        //deleteExistingComment(commit, reportKey);
        ObjectMapper objectMapper = new ObjectMapper();

        BitbucketCommentContent commentContent = new BitbucketCommentContent(comment);
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
        List<FileDiff> fileDiffs = new ArrayList<>();

        try (Response response = okHttpClient.newCall(req).execute()) {
            String rawDiff = validate(response);
            RawDiffParser diffParser = new RawDiffParser();
            fileDiffs = diffParser.execute(rawDiff);
            //TODO: add file content
        }
        return fileDiffs;
    }

    String validate(Response response) throws IOException {
        if (!response.isSuccessful()) {
            String error = Optional.ofNullable(response.body()).map(b -> {
                try {
                    return b.string();
                } catch (IOException e) {
                    throw new IllegalStateException("Could not retrieve response content", e);
                }
            }).orElse("Request failed but Bitbucket didn't respond with a proper error message");
            //throw new BitbucketCloudException(response.code(), error);
            throw new IOException(error);
        } else {
            //TODO:refactor
            return response.body().string();
        }
    }

    private String fetchFileContentFromBitbucket(String branch, String filePath) {
        try {
            // Encode the file path (especially if it has slashes or spaces)
            String encodedFilePath = URLEncoder.encode(filePath, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20"); // Bitbucket expects %20, not +

            String apiUrl = String.format(
                    "https://api.bitbucket.org/2.0/repositories/%s/%s/src/%s/%s",
                    appId, almRepo, branch, encodedFilePath
            );

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .get()
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                validate(response); // Assume you throw if not 200 OK

                if (response.body() != null) {
                    return response.body().string();
                } else {
                    LOGGER.warn("Empty body when fetching content for: {}", filePath);
                    return "";
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error fetching file content from Bitbucket: {}", filePath, e);
            return "Error fetching file content: " + e.getMessage();
        }
    }

    public String getPrNumber() {
        return prNumber;
    }

    public BitbucketSummarizeComment createSummarizeComment(
            BitbucketCommentContent commentData
    ) {
        return new BitbucketSummarizeComment(commentData);
    }
}
