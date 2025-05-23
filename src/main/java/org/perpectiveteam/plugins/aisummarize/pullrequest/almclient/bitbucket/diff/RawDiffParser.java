package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.diff;

import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.FileDiff;

import java.util.ArrayList;
import java.util.List;

public class RawDiffParser {

    public List<FileDiff> execute(String rawDiff) {
        List<FileDiff> fileDiffs = new ArrayList<>();
        String[] lines = rawDiff.split("\n");
        FileDiff currentFile = null;
        StringBuilder patchBuilder = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("diff --git")) {
                // Save previous diff
                if (currentFile != null) {
                    currentFile.changes = patchBuilder.toString();
                    fileDiffs.add(currentFile);
                }

                // Start new file
                patchBuilder.setLength(0);
                currentFile = new FileDiff();
                String[] parts = line.split(" ");
                currentFile.filePath = parts[2].substring(2); // remove `a/`

            } else if (line.startsWith("new file mode")) {
                currentFile.diffType = "added";
            } else if (line.startsWith("deleted file mode")) {
                currentFile.diffType = "removed";
            } else if (line.startsWith("--- ") && line.contains("/dev/null")) {
                currentFile.diffType = "added";
            } else if (line.startsWith("--- ") && !line.contains("/dev/null")) {
                currentFile.diffType = "modified";
            }

            if (currentFile != null) {
                patchBuilder.append(line).append("\n");
            }
        }
        return fileDiffs;
    }
}
