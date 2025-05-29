package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket;

import okhttp3.OkHttpClient;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.server.ServerSide;

@ServerSide
@ComputeEngineSide
public class HttpClientBuilderFactory {
    public OkHttpClient.Builder createClientBuilder() {
        return new OkHttpClient.Builder();
    }
}
