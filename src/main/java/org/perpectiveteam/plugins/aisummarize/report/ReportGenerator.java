package org.perpectiveteam.plugins.aisummarize.report;

import org.perpectiveteam.plugins.aisummarize.pullrequest.AnalysisDetails;
import org.sonar.api.platform.Server;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ReportGenerator {
    private final Server server;
    public ReportGenerator(Server server) {
        this.server = server;
    }
    public ReportSummary createReportSummary(AnalysisDetails analysisDetails, String aiSummary) {

        return ReportSummary.builder()
                .withProjectKey(analysisDetails.getAnalysisProjectKey())
                .withDashboardUrl(getDashboardUrl(analysisDetails))
                .withAiSummary(aiSummary)
                .build();
    }

    private String getDashboardUrl(AnalysisDetails analysisDetails) {
        return server.getPublicRootUrl() + "/dashboard?id=" + URLEncoder.encode(analysisDetails.getAnalysisProjectKey(), StandardCharsets.UTF_8) + "&pullRequest=" + analysisDetails.getPullRequestId();
    }
}
