package org.perpectiveteam.plugins.aisummarize.almclient.bitbucket.cloud.comment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketSummarizeComment {
    @JsonProperty("content")
    private final BitbucketCommentContent content;


    public BitbucketSummarizeComment(BitbucketCommentContent content) {
        this.content = content;
    }


    public BitbucketCommentContent getContent() {
        return content;
    }

}
