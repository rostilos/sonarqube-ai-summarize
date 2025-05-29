package org.perpectiveteam.plugins.aisummarize.config;

import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;

import java.util.Optional;

@ServerSide
@ComputeEngineSide
public class AiSummarizeConfig {
    public static final String AI_CLIENT_API_KEY = "ai.summarize.openai.apikey";
    public static final String FILE_LIMIT = "ai.summarize.file.limit";
    public static final String AI_PROVIDER = "ai.summarize.ai.provider";
    public static final String AI_MODEL = "ai.summarize.ai.model";
    public static final String AI_PROMPT_TEMPLATE = "ai.summarize.ai_prompt_template";
    public static final String INTRO_REGEXP = "###Intro###\\s*(.*?)\\s*###Intro end###";
    public static final String AFTER_REGEXP = "###After###\\s*(.*?)\\s*###After end###";

    private static final String DEFAULT_AI_PROVIDER = "openai";

    private static final int DEFAULT_FILE_LIMIT = 10;

    private final Configuration configuration;

    private static PostProjectAnalysisTask.ProjectAnalysis projectAnalysis;

    //TODO: refactor this later
    public static void setProjectAnalysis(PostProjectAnalysisTask.ProjectAnalysis currentProjectAnalysis) {
        projectAnalysis = currentProjectAnalysis;
    }

    public AiSummarizeConfig(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getGlobalAiApiKey() {
        return configuration.get(AI_CLIENT_API_KEY).orElse("");
    }

    public int getGlobalFileLimit() {
        return configuration.getInt(FILE_LIMIT).orElse(DEFAULT_FILE_LIMIT);
    }

    public String getGlobalAiProvider() {
        return configuration.get(AI_PROVIDER).orElse(DEFAULT_AI_PROVIDER);
    }

    public String getGlobalAiModel() {
        return configuration.get(AI_MODEL).orElse("");
    }

    public String getGlobalAiPromptTemplate() {
        return configuration.get(AI_PROMPT_TEMPLATE).orElse(
                DefaultPromptTemplate.defaultTemplate()
        );
    }

    // Project-specific configuration methods
    public String getAiApiKey() {
        return getProjectSettingFromProjectAnalysis(AI_CLIENT_API_KEY)
                .orElse(getGlobalAiApiKey());
    }

    public int getFileLimit() {
        return getProjectSettingFromProjectAnalysis(FILE_LIMIT)
                .map(Integer::parseInt)
                .orElse(getGlobalFileLimit());
    }

    public String getAiProvider() {
        return getProjectSettingFromProjectAnalysis(AI_PROVIDER)
                .orElse(getGlobalAiProvider());
    }

    public String getAiModel() {
        return getProjectSettingFromProjectAnalysis(AI_MODEL)
                .orElse(getGlobalAiModel());
    }

    public String getAiPromptTemplate() {
        return getProjectSettingFromProjectAnalysis(AI_PROMPT_TEMPLATE)
                .orElse(getGlobalAiPromptTemplate());
    }

    private Optional<String> getProjectSettingFromProjectAnalysis(String settingKey) {
        //TODO: refactor this later
        if(projectAnalysis == null) {
            throw new RuntimeException("The method getters must be accessed after the projectAnalysis is set");
        }
        return getScannerProperty(settingKey);
    }

    public Optional<String> getScannerProperty(String propertyName) {
        //TODO: refactor this later
        return Optional.ofNullable(projectAnalysis.getScannerContext().getProperties().get(propertyName));
    }
}
