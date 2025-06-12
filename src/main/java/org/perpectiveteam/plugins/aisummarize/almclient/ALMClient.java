package org.perpectiveteam.plugins.aisummarize.almclient;

import java.io.IOException;
import java.util.List;

import org.perpectiveteam.plugins.aisummarize.pullrequest.prdto.FileDiff;

public interface ALMClient {
    List<FileDiff> fetchPullRequestFilesDiff() throws IOException;
    void postSummaryComment(String comment) throws IOException;
    String getPrNumber();
}
