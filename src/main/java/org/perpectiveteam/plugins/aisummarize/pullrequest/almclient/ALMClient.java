package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient;

import java.util.List;

import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.FileDiff;

public interface ALMClient {
    List<FileDiff> fetchPullRequestFilesDiff(String pullRequestNumber);
    void postSummaryIssue(String prNumber, String comment);
}
