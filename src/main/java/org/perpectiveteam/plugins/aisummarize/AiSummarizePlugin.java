package org.perpectiveteam.plugins.aisummarize;

import java.util.Arrays;
import java.util.List;

import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.hooks.PostJobInScanner;
import org.perpectiveteam.plugins.aisummarize.pullrequest.PostAnalysisIssueVisitor;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.cloud.BitbucketCloudClientFactory;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.cloud.BitbucketConfiguration;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.cloud.HttpClientBuilderFactory;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.github.GitHubClientFactory;
import org.perpectiveteam.plugins.aisummarize.summarize.SummarizeExecutorFactory;
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
        if (SonarQubeSide.COMPUTE_ENGINE == context.getRuntime().getSonarQubeSide()) {
            context.addExtensions(PostAnalysisIssueVisitor.class);
        }
        // Not used currently
    }

    @Override
    public void define(Plugin.Context context) {
        context.addExtensions(getExtensions());
    }

    //TODO: split extensions by scope ( ce, server, scanner )
    private List<Object> getExtensions() {
        return Arrays.asList(
                AiSummarizeConfig.class,
                ALMClientFactory.class,
                PostJobInScanner.class,
                GitHubClientFactory.class,
                BitbucketConfiguration.class,
                HttpClientBuilderFactory.class,
                BitbucketCloudClientFactory.class,
                SummarizeExecutorFactory.class,

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
