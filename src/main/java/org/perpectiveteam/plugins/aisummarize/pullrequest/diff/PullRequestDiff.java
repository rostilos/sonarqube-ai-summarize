package org.perpectiveteam.plugins.aisummarize.pullrequest.diff;

import java.util.List;

public class PullRequestDiff {
    private List<FileDiff> files;

    public List<FileDiff> getFiles() {
        return files;
    }

    public void setFiles(List<FileDiff> files) {
        this.files = files;
    }
}

