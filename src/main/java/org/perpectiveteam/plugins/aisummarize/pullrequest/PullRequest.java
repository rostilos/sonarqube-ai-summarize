package org.perpectiveteam.plugins.aisummarize.pullrequest;

import org.perpectiveteam.plugins.aisummarize.pullrequest.diff.PullRequestDiff;
import org.sonar.api.ce.ComputeEngineSide;

@ComputeEngineSide
public class PullRequest {
    private final PullRequestDiff diff;
    private final String title;
    private final String description;

    public PullRequest(
            PullRequestDiff diff,
            String title,
            String description
    ) {
        this.diff = diff;
        this.title = title;
        this.description = description;
    }

    public PullRequestDiff getDiff() {
        return diff;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description.isEmpty() ? "No description provided" : description;
    }
}
