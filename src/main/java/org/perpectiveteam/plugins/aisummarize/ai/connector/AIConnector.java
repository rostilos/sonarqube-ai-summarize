package org.perpectiveteam.plugins.aisummarize.ai.connector;

public interface AIConnector {

    String getCompletion(String prompt);

    String getProviderName();
}
