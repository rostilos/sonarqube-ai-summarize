package org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder;

public class LineChange {
    public int oldLineNumber;
    public int newLineNumber;
    public String type; // "ADDED", "REMOVED"
    public String content;

    public LineChange(int oldLine, int newLine, String type, String content) {
        this.oldLineNumber = oldLine;
        this.newLineNumber = newLine;
        this.type = type;
        this.content = content;
    }
}
