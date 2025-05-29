package org.perpectiveteam.plugins.aisummarize.pullrequest.prdto;


import com.fasterxml.jackson.annotation.JsonCreator;

public class FileDiff {
    public String filePath;
    public DiffType diffType;
    public String rawContent;
    public String sha;
    public String changes;

    public enum DiffType {
        ADDED,
        MODIFIED,
        REMOVED,
        RENAMED;

        @JsonCreator
        public static DiffType fromValue(String value) {
            return DiffType.valueOf(value.toUpperCase());
        }
    }
}
