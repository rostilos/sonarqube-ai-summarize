package org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder;

import java.util.List;



public class FileDiff {
    public String filePath;
    //TODO: maybe add enum?
    public String diffType; // "added", "modified", "removed", "renamed"
    public String rawContent;
    public String sha;
    public String changes;
}
