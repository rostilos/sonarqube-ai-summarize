package org.perpectiveteam.plugins.aisummarize.almclient;

import java.io.IOException;
import java.util.List;

import org.perpectiveteam.plugins.aisummarize.pullrequest.diff.FileDiff;

public interface ALMClient {
    List<FileDiff> fetchPullRequestFilesDiff() throws IOException;
    void postSummaryResult(String textContent) throws IOException;
    String getPrNumber();
    String getPullRequestTitle() throws IOException;
    String getPullRequestDescription() throws IOException;
}
