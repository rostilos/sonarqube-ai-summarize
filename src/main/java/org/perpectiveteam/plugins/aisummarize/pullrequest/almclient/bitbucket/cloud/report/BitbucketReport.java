package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.cloud.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BitbucketReport {
    @JsonProperty("title")
    private String title;
    @JsonProperty("details")
    private String details;
    @JsonProperty("report_type")
    private String report_type;
    @JsonProperty("result")
    private String result;
    @JsonProperty("reporter")
    private String reporter;
    @JsonProperty("link")
    private final String link;
    @JsonProperty("data")
    private final List<BitbucketReportDataItem> data;


    public BitbucketReport(String title, String details, String report_type, String result, String reporter, String link, List<BitbucketReportDataItem> data) {
        this.title = title;
        this.details = details;
        this.report_type = report_type;
        this.result = result;
        this.reporter = reporter;
        this.link = link;
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }

    public String getReport_type() {
        return report_type;
    }

    public String getResult() {
        return result;
    }

    public String getReporter() {
        return reporter;
    }

    public String getLink() {
        return link;
    }

    public List<BitbucketReportDataItem> getData() {
        return data;
    }
}
