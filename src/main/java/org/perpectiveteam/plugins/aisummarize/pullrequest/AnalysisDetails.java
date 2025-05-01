package org.perpectiveteam.plugins.aisummarize.pullrequest;


import org.sonar.api.ce.posttask.Analysis;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.Project;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.issue.IssueStatus;
import org.sonar.ce.task.projectanalysis.component.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//TODO: add SQ issues to prompt
public class AnalysisDetails {

    private final String pullRequestId;
    private final String commitId;
    private final List<PostAnalysisIssueVisitor.ComponentIssue> issues;
    private final QualityGate qualityGate;
    private final PostProjectAnalysisTask.ProjectAnalysis projectAnalysis;

    public AnalysisDetails(String pullRequestId, String commitId, List<PostAnalysisIssueVisitor.ComponentIssue> issues,
                           QualityGate qualityGate, PostProjectAnalysisTask.ProjectAnalysis projectAnalysis) {
        super();
        this.pullRequestId = pullRequestId;
        this.commitId = commitId;
        this.issues = issues;
        this.qualityGate = qualityGate;
        this.projectAnalysis = projectAnalysis;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public String getCommitSha() {
        return commitId;
    }

    public QualityGate.Status getQualityGateStatus() {
        return qualityGate.getStatus();
    }

    public List<QualityGate.Condition> findFailedQualityGateConditions() {
        return qualityGate.getConditions().stream()
                .filter(c -> c.getStatus() == QualityGate.EvaluationStatus.ERROR)
                .collect(Collectors.toList());
    }

    public Optional<String> getScannerProperty(String propertyName) {
        return Optional.ofNullable(projectAnalysis.getScannerContext().getProperties().get(propertyName));
    }

    public Date getAnalysisDate() {
        return getAnalysis().getDate();
    }

    public String getAnalysisId() {
        return getAnalysis().getAnalysisUuid();
    }

    public String getAnalysisProjectKey() {
        return getProject().getKey();
    }

    public String getAnalysisProjectName() {
        return getProject().getName();
    }

    public List<PostAnalysisIssueVisitor.ComponentIssue> getIssues() {
        return issues;
    }

    public List<PostAnalysisIssueVisitor.ComponentIssue> getScmReportableIssues() {
        return getIssues().stream()
                .filter(i -> i.getComponent().getReportAttributes().getScmPath().isPresent())
                .filter(i -> i.getComponent().getType() == Component.Type.FILE)
                .filter(i -> i.getIssue().resolution() == null)
                .filter(i -> i.getIssue().issueStatus() == IssueStatus.OPEN)
                .collect(Collectors.toList());
    }

    public Optional<QualityGate.Condition> findQualityGateCondition(String metricKey) {
        return qualityGate.getConditions().stream().filter(c -> metricKey.equals(c.getMetricKey())).findFirst();
    }

    private Analysis getAnalysis() {
        return projectAnalysis.getAnalysis().orElseThrow();
    }

    private Project getProject() {
        return projectAnalysis.getProject();
    }

}
