package org.perpectiveteam.plugins.aisummarize.ai;

import java.util.List;

import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.FileDiff;
import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.LineChange;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class AIPromptBuilder {
    private static final Logger LOG = Loggers.get(AIPromptBuilder.class);

    public String buildPrompt(List<FileDiff> fileDiffs) {
        //TODO: include SQ issues
        LOG.info("Building AI prompt for {} files", fileDiffs.size());
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze the following code changes across multiple files.\n");
        sb.append("For each file, summarize the change, explain the intent, and identify any potential risks.\n\n");

        int validFileCount = 0;
        for (FileDiff fileDiff : fileDiffs) {
            if (fileDiff.filePath.isEmpty() || fileDiff.rawContent.isEmpty() || fileDiff.changes.isEmpty()) {
                LOG.debug("Skipping file with missing data: {}", fileDiff.filePath);
                continue;
            }
            validFileCount++;
            sb.append("========================================\n");
            sb.append("Filename: ").append(fileDiff.filePath).append("\n\n");

            sb.append("Original Content:\n");
            sb.append("-----\n");
            sb.append(fileDiff.rawContent).append("\n");
            sb.append("-----\n\n");

            sb.append("Diff:\n");
            sb.append("-----\n");
            for (LineChange change : fileDiff.changes) {
                sb.append("old line: ").append(change.oldLineNumber)
                  .append(" new line: ").append(change.newLineNumber)
                  .append(" ").append(change.type)
                  .append(": ").append(change.content).append("\n");
            }
            sb.append("-----\n\n");
        }

        LOG.info("Included {} valid files in the prompt", validFileCount);
        sb.append("========================================\n");
        sb.append("Provide a consolidated summary of the changes above.\n");
        return sb.toString();
    }

    public String buildPromptWithContext(List<FileDiff> fileDiffs, String additionalContext) {
        String basePrompt = buildPrompt(fileDiffs);
        StringBuilder sb = new StringBuilder(basePrompt);
        
        if (additionalContext != null && !additionalContext.isEmpty()) {
            sb.append("\nAdditional Context:\n");
            sb.append(additionalContext).append("\n");
        }
        
        return sb.toString();
    }
}
