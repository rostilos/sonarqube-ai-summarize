package org.perpectiveteam.plugins.aisummarize.pullrequest;

import org.sonar.api.issue.IssueStatus;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.ce.task.projectanalysis.component.Component;
import org.sonar.ce.task.projectanalysis.issue.IssueVisitor;
import org.sonar.core.issue.DefaultIssue;
import org.sonar.db.protobuf.DbIssues;

import javax.annotation.CheckForNull;
import java.util.*;

public class PostAnalysisIssueVisitor extends IssueVisitor {

    private final List<ComponentIssue> collectedIssues = new ArrayList<>();

    @Override
    public void onIssue(Component component, DefaultIssue defaultIssue) {
        collectedIssues.add(new ComponentIssue(component, new LightIssue(defaultIssue)));
    }

    public List<ComponentIssue> getIssues() {
        return Collections.unmodifiableList(collectedIssues);
    }

    public static class ComponentIssue {

        private final Component component;
        private final LightIssue issue;

        ComponentIssue(Component component, LightIssue issue) {
            super();
            this.component = component;
            this.issue = issue;
        }

        public Component getComponent() {
            return component;
        }

        public LightIssue getIssue() {
            return issue;
        }
    }

    public static class LightIssue {

        private final String key;
        private final Integer line;
        private final String message;
        private final String resolution;
        private final IssueStatus status;
        private final Map<SoftwareQuality, Severity> impacts;
        private final DbIssues.Locations locations;

        LightIssue(DefaultIssue issue) {
            this.key = issue.key();
            this.line = issue.getLine();
            this.message = issue.getMessage();

            this.resolution = issue.resolution();
            this.status = issue.issueStatus();
            this.impacts = issue.impacts();
            this.locations = issue.getLocations();
        }

        public String key() {
            return key;
        }

        @CheckForNull
        public Integer getLine() {
            return line;
        }

        @CheckForNull
        public String getMessage() {
            return message;
        }

        @CheckForNull
        public String resolution() {
            return resolution;
        }

        public Map<SoftwareQuality, Severity> impacts() {
            return impacts;
        }

        public DbIssues.Locations getLocations() {
            return locations;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, line, message, resolution, status, impacts);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            LightIssue other = (LightIssue) obj;
            return Objects.equals(key, other.key)
                    && Objects.equals(line, other.line)
                    && Objects.equals(message, other.message)
                    && Objects.equals(resolution, other.resolution)
                    && Objects.equals(status, other.status)
                    && Objects.equals(impacts, other.impacts);
        }

    }
}
