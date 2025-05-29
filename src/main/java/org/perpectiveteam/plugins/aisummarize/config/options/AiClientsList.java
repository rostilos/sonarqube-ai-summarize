package org.perpectiveteam.plugins.aisummarize.config.options;

import java.util.Arrays;
import java.util.List;

public class AiClientsList {
    public static final String OPENAI = "openai";
    public static final String OPENROUTER = "openrouter";

    public static List<String> options() {
        return Arrays.asList(OPENAI, OPENROUTER);
    }
}