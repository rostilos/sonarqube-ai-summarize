package org.perpectiveteam.plugins.aisummarize.config;

import org.perpectiveteam.plugins.aisummarize.config.common.DefaultPromptTemplate;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;

import java.util.Optional;

@ServerSide
@ComputeEngineSide
public class SummarizeConfig {
    public static final String IS_ENABLED = "ai.summarize.enabled";
    public static final String AI_CLIENT_API_KEY = "ai.summarize.openai.apikey";
    public static final String FILE_LIMIT = "ai.summarize.file.limit";
    public static final String FILE_MAX_LINES = "ai.summarize.file_max_lines";
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

    public SummarizeConfig(Configuration configuration) {
        this.configuration = configuration;
    }

    private String getGlobalAiApiKey() {
        return configuration.get(AI_CLIENT_API_KEY).orElse("");
    }

    private int getGlobalFileLimit() {
        return configuration.getInt(FILE_LIMIT).orElse(DEFAULT_FILE_LIMIT);
    }

    private String getGlobalAiProvider() {
        return configuration.get(AI_PROVIDER).orElse(DEFAULT_AI_PROVIDER);
    }

    private String getGlobalAiModel() {
        return configuration.get(AI_MODEL).orElse("");
    }

    private String getGlobalAiPromptTemplate() {
        return configuration.get(AI_PROMPT_TEMPLATE).orElse(
                DefaultPromptTemplate.defaultTemplate()
        );
    }

    private String getGlobalIsEnabled() {
        return configuration.get(IS_ENABLED).orElse(
                "0"
        );
    }

    private String getGlobalFileMaxLines() {
        return configuration.get(FILE_MAX_LINES).orElse(
                "1000"
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

    public int getFileMaxLines() {
        return Integer.parseInt(getProjectSettingFromProjectAnalysis(FILE_MAX_LINES)
                .orElse(getGlobalFileMaxLines()));
    }

    public Boolean getIsEnabled(){
        return Boolean.valueOf(getProjectSettingFromProjectAnalysis(IS_ENABLED)
                .orElse(getGlobalIsEnabled()));
    }

    private Optional<String> getProjectSettingFromProjectAnalysis(String settingKey) {
        //TODO: refactor this later
        if (projectAnalysis == null) {
            throw new RuntimeException("The method getters must be accessed after the projectAnalysis is set");
        }
        return getScannerProperty(settingKey);
    }

    public Optional<String> getScannerProperty(String propertyName) {
        //TODO: refactor this later
        return Optional.ofNullable(projectAnalysis.getScannerContext().getProperties().get(propertyName));
    }
}
