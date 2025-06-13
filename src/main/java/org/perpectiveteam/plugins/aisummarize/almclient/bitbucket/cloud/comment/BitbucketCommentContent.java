package org.perpectiveteam.plugins.aisummarize.almclient.bitbucket.cloud.comment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketCommentContent {
    private final String raw;

    @JsonCreator
    public BitbucketCommentContent(@JsonProperty("raw") String raw) {
        this.raw = raw;
    }

    public String getRaw() {
        return raw;
    }
}
