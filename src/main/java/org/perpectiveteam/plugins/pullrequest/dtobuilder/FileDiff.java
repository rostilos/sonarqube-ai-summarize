package org.perpectiveteam.plugins.pullrequest.dtobuilder;

import java.util.List;

public class FileDiff {
    public String filePath;
    public String diffType; // "added", "modified", "removed", "renamed"
    public List<LineChange> changes;
}
