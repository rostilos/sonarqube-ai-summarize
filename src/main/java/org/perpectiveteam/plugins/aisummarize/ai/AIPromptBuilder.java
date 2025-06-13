package org.perpectiveteam.plugins.aisummarize.ai;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.perpectiveteam.plugins.aisummarize.config.SummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.pullrequest.prdto.FileDiff;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.server.ServerSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerSide
@ComputeEngineSide
public class AIPromptBuilder {
    private final SummarizeConfig aiSummarizeConfig;
    private static final String DELIMITER = "========================================\n";

    public AIPromptBuilder(SummarizeConfig aiSummarizeConfig) {
        this.aiSummarizeConfig = aiSummarizeConfig;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AIPromptBuilder.class);

    public String buildPrompt(List<FileDiff> fileDiffs) {
        ParsedTemplate parsed = parseTemplate(aiSummarizeConfig);
        LOGGER.info("Building AI prompt for {} files", fileDiffs.size());
        StringBuilder sb = new StringBuilder();
        sb.append(parsed.intro);
        sb.append(DELIMITER);

        int validFileCount = 0;
        for (FileDiff fileDiff : fileDiffs) {
            if (fileDiff.getRawContent() == null || fileDiff.getFilePath().isEmpty() || fileDiff.getRawContent().isEmpty()) {
                LOGGER.debug("Missing previous file version for: {}", fileDiff.getFilePath());
                fileDiff.setRawContent("There is no previous version, probably a new file");
            }
            if (fileDiff.getFilePath().isEmpty() || fileDiff.getChanges().isEmpty()) {
                LOGGER.debug("Skipping file with missing data: {}", fileDiff.getFilePath());
                continue;
            }
            if (fileDiff.getChanges().split("\r?\n").length > aiSummarizeConfig.getFileMaxLines()) {
                LOGGER.debug("File will be skipped when generating prompt, too many lines, path: {}", fileDiff.getFilePath());
                continue;
            }
            validFileCount++;
            sb.append(DELIMITER);
            sb.append("Filename: ").append(fileDiff.getFilePath()).append("\n\n");

            sb.append("Original Content:\n");
            sb.append("-----\n");
            sb.append(getOriginalFileContent(fileDiff)).append("\n");
            sb.append("-----\n\n");

            sb.append("Patch:\n");
            sb.append("-----\n");
            sb.append(fileDiff.getChanges()).append("\n");
            sb.append("-----\n\n");
        }

        LOGGER.info("Included {} valid files in the prompt", validFileCount);
        sb.append(DELIMITER);
        sb.append(parsed.after);
        return sb.toString();
    }

    private String getOriginalFileContent(FileDiff fileDiff) {
        String rawContent = fileDiff.getRawContent();
        if (rawContent.split("\r?\n").length > aiSummarizeConfig.getFileMaxLines()) {
            return "Present but skipped for prompt ( too much content )";
        } else {
            return rawContent;
        }
    }

    public static class ParsedTemplate {
        public final String intro;
        public final String after;

        public ParsedTemplate(String intro, String after) {
            this.intro = intro;
            this.after = after;
        }
    }

    public static ParsedTemplate parseTemplate(SummarizeConfig aiSummarizeConfig) {
        String template = aiSummarizeConfig.getAiPromptTemplate();

        Pattern introPattern = Pattern.compile(SummarizeConfig.INTRO_REGEXP, Pattern.DOTALL);
        Pattern afterPattern = Pattern.compile(SummarizeConfig.AFTER_REGEXP, Pattern.DOTALL);

        Matcher introMatcher = introPattern.matcher(template);
        Matcher afterMatcher = afterPattern.matcher(template);

        String intro = introMatcher.find() ? introMatcher.group(1).trim() : "";
        String after = afterMatcher.find() ? afterMatcher.group(1).trim() : "";

        return new ParsedTemplate(intro, after);
    }
}
