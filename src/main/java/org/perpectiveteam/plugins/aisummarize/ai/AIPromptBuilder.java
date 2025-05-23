package org.perpectiveteam.plugins.aisummarize.ai;

import java.util.List;

import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.FileDiff;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class AIPromptBuilder {
    private static final Logger LOG = Loggers.get(AIPromptBuilder.class);

    public String buildPrompt(List<FileDiff> fileDiffs) {
        //TODO: include SQ issues
        //TODO: Allow to define prompt and enabled data from the admin panel (e.g. by templating).
        LOG.info("Building AI prompt for {} files", fileDiffs.size());
        StringBuilder sb = new StringBuilder();
        sb.append("Please review the following code below:\n");
        sb.append("Consider:\n" +
                "1. Code quality and adherence to best practices\n" +
                "2. Potential bugs or edge cases\n" +
                "3. Performance optimizations\n" +
                "4. Readability and maintainability\n" +
                "5. Any security concerns\n" +
                "Suggest improvements and explain your reasoning for each suggestion.\n.");
        sb.append("Provide a summary in markdown markup.\n");

        int validFileCount = 0;
        for (FileDiff fileDiff : fileDiffs) {
            if(fileDiff.rawContent == null || fileDiff.filePath.isEmpty() || fileDiff.rawContent.isEmpty()) {
                fileDiff.rawContent = "There is no previous version, probably a new file";
                LOG.debug("Missing previous file version for: " + fileDiff.filePath);
            }
            if (fileDiff.filePath.isEmpty() || fileDiff.changes.isEmpty()) {
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

            sb.append("Patch:\n");
            sb.append("-----\n");
            sb.append(fileDiff.changes).append("\n");
            sb.append("-----\n\n");
        }

        LOG.info("Included {} valid files in the prompt", validFileCount);
        sb.append("========================================\n");
        sb.append("Provide a consolidated summary of the changes above.\n");
        return sb.toString();
    }

    //TODO: Allow to define prompt and enabled data from the admin panel (e.g. by templating).
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
