package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient;

import java.io.IOException;
import java.util.List;

import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.FileDiff;

public interface ALMClient {
    List<FileDiff> fetchPullRequestFilesDiff() throws IOException;
    void postSummaryIssue(String comment) throws IOException;
    String getPrNumber();
}
