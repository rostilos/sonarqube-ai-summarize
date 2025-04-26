package org.perpectiveteam.plugins.pullrequest;


import org.perpectiveteam.plugins.pullrequest.dtobuilder.LineChange;

import java.util.ArrayList;
import java.util.List;

public class PatchParser {

    public static List<LineChange> parsePatch(String patch) {
        List<LineChange> changes = new ArrayList<>();
        String[] lines = patch.split("\n");

        for (String line : lines) {
            if (line.startsWith("+") && !line.startsWith("+++")) {
                changes.add(new LineChange(-1, -1, "ADDED", line.substring(1)));
            } else if (line.startsWith("-") && !line.startsWith("---")) {
                changes.add(new LineChange(-1, -1, "REMOVED", line.substring(1)));
            } else {
                // context line or metadata, can ignore for now
            }
        }
        return changes;
    }
}