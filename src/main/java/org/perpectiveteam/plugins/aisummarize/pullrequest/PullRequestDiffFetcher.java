package org.perpectiveteam.plugins.aisummarize.pullrequest;

import java.io.IOException;
import java.util.List;

import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.ALMClient;
import org.perpectiveteam.plugins.aisummarize.pullrequest.prdto.FileDiff;
import org.perpectiveteam.plugins.aisummarize.pullrequest.prdto.PullRequestDiff;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class PullRequestDiffFetcher {
    private static final Logger LOG = Loggers.get(PullRequestDiffFetcher.class);
    
    private final ALMClient almClient;

    public PullRequestDiffFetcher(ALMClient almClient) {
        this.almClient = almClient;
    }

    public PullRequestDiff fetchDiff() throws IOException {
        LOG.info("Fetching PR diff for PR #{}", almClient.getPrNumber());
        List<FileDiff> files = almClient.fetchPullRequestFilesDiff();
        LOG.info("Fetched {} files from PR", files.size());

        PullRequestDiff diff = new PullRequestDiff();
        diff.files = files;
        return diff;
    }
}
