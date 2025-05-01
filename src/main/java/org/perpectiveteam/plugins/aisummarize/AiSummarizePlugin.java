package org.perpectiveteam.plugins.aisummarize;

import java.util.Arrays;
import java.util.List;

import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.hooks.PostJobInScanner;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.ALMClientFactory;
import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.core.extension.CoreExtension;

public class AiSummarizePlugin implements Plugin, CoreExtension {

    private static final String CATEGORY = "AI Summarize";
    private static final String SUBCATEGORY_GITHUB = "GitHub";
    private static final String SUBCATEGORY_AI = "AI Providers";

    @Override
    public String getName() {
        return "AI Summarize Plugin";
    }

    @Override
    public void load(CoreExtension.Context context) {
        // Not used currently
    }

    @Override
    public void define(Plugin.Context context) {
        context.addExtensions(getExtensions());
    }

    private List<Object> getExtensions() {
        return Arrays.asList(
                AiSummarizeConfig.class,
                ALMClientFactory.class,
                PostJobInScanner.class,

                //TODO: get target branch from PR data
                PropertyDefinition.builder(AiSummarizeConfig.DEFAULT_TARGET_BRANCH)
                        .name("Default Target Branch")
                        .description("Default target branch for GitHub repositories")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY_GITHUB)
                        .defaultValue("main")
                        .onQualifiers(Qualifiers.PROJECT)
                        .index(2)
                        .build(),

                PropertyDefinition.builder(AiSummarizeConfig.FILE_LIMIT)
                        .name("File Limit")
                        .description("Maximum number of files to process in a pull request")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY_GITHUB)
                        .type(PropertyType.INTEGER)
                        .defaultValue("10")
                        .onQualifiers(Qualifiers.PROJECT)
                        .index(3)
                        .build(),

                // AI Provider settings
                PropertyDefinition.builder(AiSummarizeConfig.AI_PROVIDER)
                        .name("AI Provider")
                        .description("AI provider to use for summarization (currently only 'openai' is supported)")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY_AI)
                        .defaultValue("openai")
                        .onQualifiers(Qualifiers.PROJECT)
                        .index(1)
                        .build(),

                PropertyDefinition.builder(AiSummarizeConfig.OPENAI_API_KEY)
                        .name("OpenAI API Key")
                        .description("API key for OpenAI")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY_AI)
                        .type(PropertyType.PASSWORD)
                        .onQualifiers(Qualifiers.PROJECT)
                        .index(2)
                        .build()
        );
    }
}
