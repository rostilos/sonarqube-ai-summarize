package org.perpectiveteam.plugins.aisummarize;

import java.util.ArrayList;
import java.util.List;

import org.perpectiveteam.plugins.aisummarize.ai.AIPromptBuilder;
import org.perpectiveteam.plugins.aisummarize.config.SummarizeConfigProperties;
import org.perpectiveteam.plugins.aisummarize.config.SummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.config.SummarizeProperySensor;
import org.perpectiveteam.plugins.aisummarize.ce.PostProjectAnalysisSummarize;
import org.perpectiveteam.plugins.aisummarize.analysis.PostAnalysisIssueVisitor;
import org.perpectiveteam.plugins.aisummarize.almclient.bitbucket.cloud.BitbucketCloudClientFactory;
import org.perpectiveteam.plugins.aisummarize.almclient.bitbucket.BitbucketConfiguration;
import org.perpectiveteam.plugins.aisummarize.almclient.HttpClientBuilderFactory;
import org.perpectiveteam.plugins.aisummarize.almclient.github.GitHubClientFactory;
import org.perpectiveteam.plugins.aisummarize.ce.SummarizeExecutorFactory;
import org.perpectiveteam.plugins.aisummarize.almclient.ALMClientFactory;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.core.extension.CoreExtension;

public class AiSummarizePlugin implements Plugin, CoreExtension {
    @Override
    public String getName() {
        return "AI Summarize Plugin";
    }

    @Override
    public void load(CoreExtension.Context context) {
        if (SonarQubeSide.COMPUTE_ENGINE == context.getRuntime().getSonarQubeSide()) {
            List<Object> extensions = new ArrayList<>(List.of(
                    PostProjectAnalysisSummarize.class,
                    PostAnalysisIssueVisitor.class,
                    ALMClientFactory.class,
                    GitHubClientFactory.class,
                    BitbucketConfiguration.class,
                    HttpClientBuilderFactory.class,
                    BitbucketCloudClientFactory.class,
                    SummarizeExecutorFactory.class,
                    SummarizeConfig.class,
                    AIPromptBuilder.class
                )
            );
            extensions.addAll(SummarizeConfigProperties.all());
            context.addExtensions(extensions);
        } else if (SonarQubeSide.SERVER == context.getRuntime().getSonarQubeSide()) {
            context.addExtensions(SummarizeConfigProperties.all());
        }
    }

    @Override
    public void define(Plugin.Context context) {
        context.addExtensions(
                SummarizeConfig.class,
                SummarizeProperySensor.class
        );
    }
}
