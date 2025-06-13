package org.perpectiveteam.plugins.aisummarize.config;

import org.perpectiveteam.plugins.aisummarize.config.options.AiClientsList;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;

import java.util.List;

import static java.lang.String.valueOf;
import static org.sonar.api.PropertyType.SINGLE_SELECT_LIST;
import static org.sonar.api.PropertyType.TEXT;

public class SummarizeConfigProperties {
    private static final String CATEGORY = "AI Summarize";
    private static final String SUBCATEGORY_GITHUB = "GitHub";
    private static final String SUBCATEGORY_AI = "AI Providers";

    private SummarizeConfigProperties() {
        // only static stuff
    }

    public static List<PropertyDefinition> all() {
        return List.of(
                PropertyDefinition.builder(SummarizeConfig.IS_ENABLED)
                        .name("Enable Plugin")
                        .description("Enable Plugin")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY_AI)
                        .defaultValue(valueOf(false))
                        .type(PropertyType.BOOLEAN)
                        .onConfigScopes(PropertyDefinition.ConfigScope.PROJECT)
                        .index(0)
                        .build(),
                // AI Provider settings
                PropertyDefinition.builder(SummarizeConfig.AI_PROVIDER)
                        .name("AI Provider")
                        .description("AI provider to use for summarization")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY_AI)
                        .defaultValue("openai")
                        .options(AiClientsList.options())
                        .type(SINGLE_SELECT_LIST)
                        .onConfigScopes(PropertyDefinition.ConfigScope.PROJECT)
                        .index(1)
                        .build(),
                PropertyDefinition.builder(SummarizeConfig.AI_MODEL)
                        .name("AI Model")
                        .description("Project AI model to use for summarization")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY_AI)
                        .onConfigScopes(PropertyDefinition.ConfigScope.PROJECT)
                        .index(2)
                        .build(),

                PropertyDefinition.builder(SummarizeConfig.AI_CLIENT_API_KEY)
                        .name("AI client API Key")
                        .description("API key for selected client")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY_AI)
                        .type(PropertyType.PASSWORD)
                        .onConfigScopes(PropertyDefinition.ConfigScope.PROJECT)
                        .index(3)
                        .build(),

                PropertyDefinition.builder(SummarizeConfig.FILE_LIMIT)
                        .name("File Limit")
                        .description("Maximum number of files to process in a pull request")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY_GITHUB)
                        .type(PropertyType.INTEGER)
                        .defaultValue("10")
                        .onConfigScopes(PropertyDefinition.ConfigScope.PROJECT)
                        .index(4)
                        .build(),

                PropertyDefinition.builder(SummarizeConfig.FILE_MAX_LINES)
                        .name("File Max Lines")
                        .description("In case the number of lines of the source file or patch is higher than this value - it will be skipped for the prompt")
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY_GITHUB)
                        .type(PropertyType.INTEGER)
                        .defaultValue("1000")
                        .onConfigScopes(PropertyDefinition.ConfigScope.PROJECT)
                        .index(5)
                        .build(),

                PropertyDefinition.builder(SummarizeConfig.AI_PROMPT_TEMPLATE)
                        .name("AI prompt template")
                        .description("Prompt template.\n" +
                                " ###Intro###\n" +
                                " ...content...\n" +
                                "##Intro end###\n" +
                                "###After###\n" +
                                "...content..\n" +
                                "###After end###\n" +
                                "Content of the form “Filename : <filename> + Original content : <original_content> + Patch : <patch >” will be inserted between the introduction and end data"
                        )
                        .category(CATEGORY)
                        .subCategory(SUBCATEGORY_AI)
                        .type(TEXT)
                        .onConfigScopes(PropertyDefinition.ConfigScope.PROJECT)
                        .index(6)
                        .build()
        );
    }
}
