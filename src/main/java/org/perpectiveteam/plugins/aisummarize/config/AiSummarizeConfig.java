package org.perpectiveteam.plugins.aisummarize.config;

import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;

@ServerSide
@ComputeEngineSide
public class AiSummarizeConfig {
    public static final String OPENAI_API_KEY = "ai.summarize.openai.apikey";
    public static final String DEFAULT_TARGET_BRANCH = "ai.summarize.github.target.branch";
    public static final String FILE_LIMIT = "ai.summarize.file.limit";
    public static final String AI_PROVIDER = "ai.summarize.ai.provider";
    
    private static final String DEFAULT_AI_PROVIDER = "openai";

    //TODO: get target branch from PR data
    private static final String DEFAULT_BRANCH = "main";
    private static final int DEFAULT_FILE_LIMIT = 10;
    
    private final Configuration configuration;
    
    public AiSummarizeConfig(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getOpenAiApiKey() {
        return configuration.get(OPENAI_API_KEY).orElse("");
    }

    //TODO: get target branch from PR data
    public String getDefaultTargetBranch() {
        return configuration.get(DEFAULT_TARGET_BRANCH).orElse(DEFAULT_BRANCH);
    }

    public int getFileLimit() {
        return configuration.getInt(FILE_LIMIT).orElse(DEFAULT_FILE_LIMIT);
    }

    public String getAiProvider() {
        return configuration.get(AI_PROVIDER).orElse(DEFAULT_AI_PROVIDER);
    }
}
