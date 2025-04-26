package org.perpectiveteam.plugins.pullrequest;


import org.perpectiveteam.plugins.pullrequest.almclient.github.GitProviderClient;
import org.perpectiveteam.plugins.pullrequest.dtobuilder.FileDiff;
import org.perpectiveteam.plugins.pullrequest.dtobuilder.PullRequestDiff;

import java.util.List;

public class PullRequestDiffFetcher {

    private final GitProviderClient gitClient;

    public PullRequestDiffFetcher(GitProviderClient gitClient) {
        this.gitClient = gitClient;
    }

    public PullRequestDiff fetchDiff(String repoOwner, String repoName, int pullRequestNumber) {
        List<FileDiff> files = gitClient.fetchPullRequestFiles(repoOwner, repoName, pullRequestNumber);

        PullRequestDiff diff = new PullRequestDiff();
        diff.files = files;
        return diff;
    }
}
