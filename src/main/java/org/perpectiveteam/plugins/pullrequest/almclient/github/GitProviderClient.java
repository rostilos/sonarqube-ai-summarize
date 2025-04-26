package org.perpectiveteam.plugins.pullrequest.almclient.github;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.perpectiveteam.plugins.pullrequest.PatchParser;
import org.perpectiveteam.plugins.pullrequest.dtobuilder.FileDiff;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;

public class GitProviderClient {

    private final String githubToken; // Your GitHub personal access token

    public GitProviderClient(String githubToken) {
        this.githubToken = githubToken;
    }

    public List<FileDiff> fetchPullRequestFiles(String owner, String repo, int pullRequestNumber) {
        List<FileDiff> fileDiffs = new ArrayList<>();
        try {
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%d/files", owner, repo, pullRequestNumber);
            URL url = new URL(apiUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "token " + githubToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            try (InputStream in = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    try (InputStream errorStream = conn.getErrorStream()) {
                        String error = new String(errorStream.readAllBytes());
                        throw new RuntimeException("GitHub API error (" + responseCode + "): " + error);
                    }
                }
                JsonNode root = mapper.readTree(in);

                for (JsonNode fileNode : root) {
                    FileDiff fileDiff = new FileDiff();
                    fileDiff.filePath = fileNode.get("filename").asText();
                    fileDiff.diffType = fileNode.get("status").asText();
                    fileDiff.changes = new ArrayList<>();

                    String patch = fileNode.get("patch") != null ? fileNode.get("patch").asText() : null;
                    if (patch != null) {
                        fileDiff.changes = PatchParser.parsePatch(patch);
                    }

                    fileDiffs.add(fileDiff);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching PR diff from GitHub", e);
        }
        return fileDiffs;
    }
}
