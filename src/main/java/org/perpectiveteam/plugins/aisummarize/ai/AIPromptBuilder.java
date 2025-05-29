package org.perpectiveteam.plugins.aisummarize.ai;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.pullrequest.prdto.FileDiff;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

@ServerSide
@ComputeEngineSide
public class AIPromptBuilder {
    private final AiSummarizeConfig aiSummarizeConfig;

    public AIPromptBuilder(AiSummarizeConfig aiSummarizeConfig) {
        this.aiSummarizeConfig = aiSummarizeConfig;
    }

    private static final Logger LOG = Loggers.get(AIPromptBuilder.class);

    public String buildPrompt(List<FileDiff> fileDiffs) {
        //TODO: include SQ issues
        ParsedTemplate parsed = parseTemplate(aiSummarizeConfig);
        LOG.info("Building AI prompt for {} files", fileDiffs.size());
        StringBuilder sb = new StringBuilder();
        sb.append(parsed.intro);
        sb.append("========================================\n");

        int validFileCount = 0;
        for (FileDiff fileDiff : fileDiffs) {
            if (fileDiff.rawContent == null || fileDiff.filePath.isEmpty() || fileDiff.rawContent.isEmpty()) {
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
        sb.append(parsed.after);
        return sb.toString();
    }

    public static class ParsedTemplate {
        public final String intro;
        public final String after;

        public ParsedTemplate(String intro, String after) {
            this.intro = intro;
            this.after = after;
        }
    }

    public static ParsedTemplate parseTemplate(AiSummarizeConfig aiSummarizeConfig) {
        String template = aiSummarizeConfig.getAiPromptTemplate();

        Pattern introPattern = Pattern.compile(AiSummarizeConfig.INTRO_REGEXP, Pattern.DOTALL);
        Pattern afterPattern = Pattern.compile(AiSummarizeConfig.AFTER_REGEXP, Pattern.DOTALL);

        Matcher introMatcher = introPattern.matcher(template);
        Matcher afterMatcher = afterPattern.matcher(template);

        String intro = introMatcher.find() ? introMatcher.group(1).trim() : "";
        String after = afterMatcher.find() ? afterMatcher.group(1).trim() : "";

        return new ParsedTemplate(intro, after);
    }
}
