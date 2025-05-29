package org.perpectiveteam.plugins.aisummarize;

import java.util.ArrayList;
import java.util.List;

import org.perpectiveteam.plugins.aisummarize.ai.AIPromptBuilder;
import org.perpectiveteam.plugins.aisummarize.config.AISummarizeConfigProperties;
import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeConfig;
import org.perpectiveteam.plugins.aisummarize.config.AiSummarizeProperySensor;
import org.perpectiveteam.plugins.aisummarize.hooks.PostProjectAnalysisSummarize;
import org.perpectiveteam.plugins.aisummarize.pullrequest.PostAnalysisIssueVisitor;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.cloud.BitbucketCloudClientFactory;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.BitbucketConfiguration;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.HttpClientBuilderFactory;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.github.GitHubClientFactory;
import org.perpectiveteam.plugins.aisummarize.summarize.SummarizeExecutorFactory;
import org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.ALMClientFactory;
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
            context.addExtensions(PostAnalysisIssueVisitor.class);
        }
    }

    @Override
    public void define(Plugin.Context context) {
        context.addExtensions(getExtensions());
    }

    //TODO: split extensions by scope ( ce, server, scanner )
    private List<Object> getExtensions() {
        List<Object> extensions = new ArrayList<>(List.of(
                AiSummarizeConfig.class,
                ALMClientFactory.class,
                PostProjectAnalysisSummarize.class,
                GitHubClientFactory.class,
                BitbucketConfiguration.class,
                HttpClientBuilderFactory.class,
                BitbucketCloudClientFactory.class,
                SummarizeExecutorFactory.class,
                AIPromptBuilder.class,
                AiSummarizeProperySensor.class,
                AISummarizeConfigProperties.all()
        ));
        extensions.addAll(AISummarizeConfigProperties.all());
        return extensions;
    }
}
