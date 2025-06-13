package org.perpectiveteam.plugins.aisummarize.ai.providers.providers;

public class AiProviderException extends RuntimeException {
    private final String message;
    public AiProviderException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return String.format("Error communicating with AI Provider: %s", message);
    }
}
