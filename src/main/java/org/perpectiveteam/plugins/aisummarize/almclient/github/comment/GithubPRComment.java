package org.perpectiveteam.plugins.aisummarize.almclient.github.comment;

public class GithubPRComment {
    private final long id;
    private final String body;

    public GithubPRComment(long id, String body) {
        this.id = id;
        this.body = body;
    }

    public long getId() {
        return id;
    }

    public String getBody() {
        return body;
    }
}
