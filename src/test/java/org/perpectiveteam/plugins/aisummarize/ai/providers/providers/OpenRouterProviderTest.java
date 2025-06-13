package org.perpectiveteam.plugins.aisummarize.ai.providers.providers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.perpectiveteam.plugins.aisummarize.config.SummarizeConfig;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled("Requires actual API keys and network access - run as integration test")
class OpenRouterProviderTest {

    @Mock
    private HttpClient httpClient;
    
    @Mock
    private SummarizeConfig config;
    
    @Mock
    private HttpResponse<String> httpResponse;
    
    @InjectMocks
    private OpenRouterProvider openRouterProvider;

    @Test
    void testGetCompletionSuccess() throws Exception {
        when(config.getAiApiKey()).thenReturn("test-api-key");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"choices\":[{\"message\":{\"content\":\"test response\"}}]}");
        
        String result = openRouterProvider.getCompletion("test prompt");
        
        assertEquals("test response", result);
        
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any());
        
        HttpRequest request = requestCaptor.getValue();
        assertTrue(request.headers().firstValue("Authorization").get().startsWith("Bearer test-api-key"));
        assertTrue(request.headers().firstValue("Content-Type").get().contains("application/json"));
        assertTrue(request.headers().firstValue("HTTP-Referer").isPresent());
        assertTrue(request.headers().firstValue("X-Title").isPresent());
    }

    @Test
    void testGetCompletionError() throws Exception {
        when(config.getAiApiKey()).thenReturn("test-api-key");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn("{\"error\":{\"message\":\"invalid request\"}}");
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            openRouterProvider.getCompletion("test prompt");
        });
        
        assertTrue(exception.getMessage().contains("invalid request"));
    }
}
