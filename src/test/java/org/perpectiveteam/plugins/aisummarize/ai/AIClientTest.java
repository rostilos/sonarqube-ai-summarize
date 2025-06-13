package org.perpectiveteam.plugins.aisummarize.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.perpectiveteam.plugins.aisummarize.ai.providers.AIProvider;
import org.perpectiveteam.plugins.aisummarize.config.SummarizeConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIClientTest {

    @Mock
    private SummarizeConfig config;
    
    @Mock
    private AIProvider aiProvider;
    
    private AIClient aiClient;

    @BeforeEach
    void setup() {
        aiClient = new AIClient(null);
        aiClient.setConnector(aiProvider);
    }

    @Test
    void testGetCompletion() {
        String testResponse = "test response";
        when(aiProvider.getCompletion(anyString())).thenReturn(testResponse);
        
        String result = aiClient.getCompletion("test prompt");
        
        assertEquals(AIClient.AI_SUMMARIZE_MARKER + "\n" + testResponse, result);
        verify(aiProvider, times(1)).getCompletion(anyString());
    }

    @Test
    void testGetProviderName() {
        String providerName = "TestProvider";
        when(aiProvider.getProviderName()).thenReturn(providerName);
        
        String result = aiClient.getProviderName();
        
        assertEquals(providerName, result);
    }
}
