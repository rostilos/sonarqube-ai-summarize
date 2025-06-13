package org.perpectiveteam.plugins.aisummarize.utils;

import org.sonar.api.ce.posttask.Branch;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;

import java.util.Optional;

public class PullRequestData {

    private PullRequestData() {
        throw new IllegalStateException("Utility class");
    }

    public static String getPrNumber(PostProjectAnalysisTask.ProjectAnalysis projectAnalysis) {
        Optional<Branch> optionalPullRequest =
                projectAnalysis.getBranch().filter(branch -> Branch.Type.PULL_REQUEST == branch.getType());
        if (optionalPullRequest.isEmpty()) {
            throw new IllegalArgumentException("Current analysis is not for a Pull Request. Task being skipped");
        }

        Optional<String> optionalPullRequestId = optionalPullRequest.get().getName();
        if (optionalPullRequestId.isEmpty()) {
            throw new IllegalArgumentException("No pull request ID has been submitted with the Pull Request. Analysis will be skipped");
        }

        return optionalPullRequestId.get();
    }
}
