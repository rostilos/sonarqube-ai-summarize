package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.cloud.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketReportDataItem {
    private final String title;
    private final DataValue value;
    @JsonProperty("type")
    private final String type;

    @JsonCreator
    public BitbucketReportDataItem(@JsonProperty("title") String title, @JsonProperty("value") DataValue value) {
        this.title = title;
        this.value = value;
        this.type = typeFrom(value);
    }

    private static String typeFrom(DataValue value) {
        if (value instanceof DataValue.Link || value instanceof DataValue.CloudLink) {
            return "LINK";
        } else if (value instanceof DataValue.Percentage) {
            return "PERCENTAGE";
        } else {
            return "TEXT";
        }
    }

    public String getTitle() {
        return title;
    }

    public DataValue getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
