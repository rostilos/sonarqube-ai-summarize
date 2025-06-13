package org.perpectiveteam.plugins.aisummarize.pullrequest;

import java.io.IOException;
import java.util.List;

import org.perpectiveteam.plugins.aisummarize.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.pullrequest.prdto.FileDiff;
import org.perpectiveteam.plugins.aisummarize.pullrequest.prdto.PullRequestDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullRequestDiffFetcher {
    private static final Logger LOG = LoggerFactory.getLogger(PullRequestDiffFetcher.class);
    
    private final ALMClient almClient;

    public PullRequestDiffFetcher(ALMClient almClient) {
        this.almClient = almClient;
    }

    public PullRequestDiff fetchDiff() throws IOException {
        LOG.info("Fetching PR diff for PR #{}", almClient.getPrNumber());
        List<FileDiff> files = almClient.fetchPullRequestFilesDiff();
        LOG.info("Fetched {} files from PR", files.size());

        PullRequestDiff diff = new PullRequestDiff();
        diff.setFiles(files);
        return diff;
    }
}
