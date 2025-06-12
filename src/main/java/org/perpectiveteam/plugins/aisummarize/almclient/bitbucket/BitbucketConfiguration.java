package org.perpectiveteam.plugins.aisummarize.almclient.bitbucket;

public class BitbucketConfiguration {

    private final String repository;
    private final String project;

    public BitbucketConfiguration(String project, String repository) {
        this.repository = repository;
        this.project = project;
    }

    public String getRepository() {
        return repository;
    }

    public String getProject() {
        return project;
    }
}
