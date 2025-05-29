package org.perpectiveteam.plugins.aisummarize.ai.providers;

public interface AIProvider {

    String getCompletion(String prompt);

    String getProviderName();
}
