package org.perpectiveteam.plugins.aisummarize.pullrequest;

import org.perpectiveteam.plugins.aisummarize.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.pullrequest.diff.FileDiff;
import org.perpectiveteam.plugins.aisummarize.pullrequest.diff.PullRequestDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class PullRequestFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(PullRequestFactory.class);
    private final ALMClient almClient;

    public PullRequestFactory(ALMClient almClient) {
        this.almClient = almClient;
    }

    public PullRequest createPullRequest() throws IOException {
        PullRequestDiff diff = fetchPullRequestFilesDiff();
        String title = almClient.getPullRequestTitle();
        String description = almClient.getPullRequestDescription();

        return new PullRequest(diff, title, description);
    }

    public PullRequestDiff fetchPullRequestFilesDiff() throws IOException {
        LOGGER.info("Fetching PR diff for PR #{}", almClient.getPrNumber());
        List<FileDiff> files = almClient.fetchPullRequestFilesDiff();
        LOGGER.info("Fetched {} files from PR", files.size());

        PullRequestDiff diff = new PullRequestDiff();
        diff.setFiles(files);
        return diff;
    }
}