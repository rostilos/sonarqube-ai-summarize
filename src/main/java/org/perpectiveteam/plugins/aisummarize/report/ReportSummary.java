package org.perpectiveteam.plugins.aisummarize.report;

import java.math.BigDecimal;
import java.util.List;

public class ReportSummary {

    private final String projectKey;
    private final String dashboardUrl;
    private final String aiSummary;

    private ReportSummary(Builder builder){
        this.projectKey = builder.projectKey;
        this.dashboardUrl = builder.dashboardUrl;
        this.aiSummary = builder.aiSummary;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private String projectKey;
        private String aiSummary;
        private String dashboardUrl;

        private Builder() {
            super();
        }

        public Builder withProjectKey(String projectKey) {
            this.projectKey = projectKey;
            return this;
        }


        public Builder withDashboardUrl(String dashboardUrl) {
            this.dashboardUrl = dashboardUrl;
            return this;
        }

        public Builder withAiSummary(String aiSummary) {
            this.aiSummary = aiSummary;
            return this;
        }

        public ReportSummary build() {
            return new ReportSummary(this);
        }
    }
}
