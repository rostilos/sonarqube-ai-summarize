package org.perpectiveteam.plugins.summarize;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
import org.perpectiveteam.plugins.pullrequest.PullRequestDiffFetcher;
import org.perpectiveteam.plugins.pullrequest.almclient.github.GitProviderClient;
import org.perpectiveteam.plugins.pullrequest.dtobuilder.PullRequestDiff;

public class SummarizeWithAI {
    private static final Logger LOG = Loggers.get(SummarizeWithAI.class);
    private final String sonarHostUrl;
    private final String sonarToken;

    //TODO: add tokens from setting
    //TODO: define almplatform
    //TODO: filter by included in analysis code only
    //TODO: fix constructor
    public SummarizeWithAI(String sonarHostUrl, String sonarToken) {
        this.sonarHostUrl = sonarHostUrl;
        this.sonarToken = sonarToken;
    }

    public void execute(String projectKey, String pullRequestKey) {
        try {
            // TODO: remove hardcode
            LOG.info("Fetching PR files for project '{}', PR '{}'", projectKey, pullRequestKey);
            GitProviderClient gitclient = new GitProviderClient("");
            PullRequestDiff pullRequestDiff = new PullRequestDiffFetcher(gitclient).fetchDiff("", "", 2);
            String test = "test";
        } catch (Exception e) {
            LOG.error("Error fetching PR files", e);
        }
    }

    private void parseAndSummarizeFiles(String json, String pullRequestKey) {
        JSONObject root = new JSONObject(json);
        JSONArray components = root.getJSONArray("components");

        LOG.info("{} files detected in PR", components.length());

        for (int i = 0; i < components.length(); i++) {
            JSONObject component = components.getJSONObject(i);
            String fileKey = component.getString("key");
            String filePath = component.getString("path");

            try {
                int lines = fetchFileLines(fileKey, pullRequestKey);

                LOG.info("File '{}' (key: {}) - {} lines in PR", filePath, fileKey, lines);
            } catch (Exception e) {
                LOG.error("Failed to fetch file {}", fileKey, e);
            }
        }
    }

    private int fetchFileLines(String fileKey, String pullRequestKey) throws Exception {
        String urlStr = sonarHostUrl + "/api/sources/lines" +
                "?key=" + fileKey +
                "&pullRequest=" + pullRequestKey;

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((sonarToken + ":").getBytes()));

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to get file lines: HTTP code " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject root = new JSONObject(response.toString());
        JSONArray lines = root.getJSONArray("lines");

        return lines.length();
    }
}