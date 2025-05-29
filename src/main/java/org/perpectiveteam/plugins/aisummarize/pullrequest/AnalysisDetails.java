package org.perpectiveteam.plugins.aisummarize.pullrequest;

import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.Project;

import java.util.List;

//TODO: add SQ issues to prompt
public class AnalysisDetails {

    private final String pullRequestId;
    private final List<PostAnalysisIssueVisitor.ComponentIssue> issues;
    private final PostProjectAnalysisTask.ProjectAnalysis projectAnalysis;

    public AnalysisDetails(
            String pullRequestId,
            List<PostAnalysisIssueVisitor.ComponentIssue> issues,
            PostProjectAnalysisTask.ProjectAnalysis projectAnalysis
    ) {
        super();
        this.pullRequestId = pullRequestId;
        this.issues = issues;
        this.projectAnalysis = projectAnalysis;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public String getAnalysisProjectKey() {
        return getProject().getKey();
    }

    public List<PostAnalysisIssueVisitor.ComponentIssue> getIssues() {
        return issues;
    }

    private Project getProject() {
        return projectAnalysis.getProject();
    }

}
